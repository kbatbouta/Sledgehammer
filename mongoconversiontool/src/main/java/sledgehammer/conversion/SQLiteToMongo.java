package sledgehammer.conversion;

import com.mongodb.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class SQLiteToMongo {

    public static SQLiteToMongo instance;
    private MainWindow mainWindow;
    JFrame jFrame;
    private String filePath;
    private MongoClient client;
    private Connection sql;

    public SQLiteToMongo() {
        instance = this;
        mainWindow = new MainWindow();
        jFrame = new JFrame("Sledgehammer Database Conversion Utility V1.00");
        jFrame.setSize(800, 600);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int x = dim.width / 2 - jFrame.getSize().width / 2;
        int y = dim.height / 2 - jFrame.getSize().height / 2;
        jFrame.setLocation(x, y);
        URL iconURL = null;
        Image imageIcon = null;
        try {
            iconURL = getClass().getResource("favicon.png");
        } catch (NullPointerException e) {
        }
        if (iconURL != null) {
            imageIcon = new ImageIcon(iconURL).getImage();
        } else {
            try {
                imageIcon = ImageIO.read(new File("mongoconversiontool" + File.separator + "favicon.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        jFrame.setIconImage(imageIcon);
        jFrame.setContentPane(mainWindow.panelMain);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setVisible(true);
    }

    public void run() {
        ConsoleTextArea console = mainWindow.getConsole();
        console.clear();

        // @formatter:off
        String testStart = "#######################################################\n" +
                           "# RUNNING CONVERSION                                  #\n" +
                           "#######################################################\n" ;
        // @formatter:on
        console.println(testStart);
        if (filePath == null || filePath.isEmpty()) {
            console.println("ERROR: SQLite DB file not set.");
            return;
        }
        File fileDB = new File(filePath);
        if (!fileDB.exists()) {
            console.println("ERROR: SQLite DB file does not exist.");
            return;
        }
        if (fileDB.isDirectory()) {
            console.println("ERROR: SQLite DB file set is a directory and not a file!");
            return;
        }
        if (!fileDB.getName().toLowerCase().endsWith(".db")) {
            console.println("ERROR: SQLite DB file given is not a valid DB file.");
            console.println("SQLite DB files end with a '.db' extension. ");
            return;
        }

        List<SQLPlayer> listSQLPlayers = new ArrayList<>();
        List<SQLBanIP> listBannedIPs = new ArrayList<>();
        List<SQLBanID> listBannedIDs = new ArrayList<>();
        Statement s;
        ResultSet rs;
        try {
            sql = connect(console, fileDB);
            console.println("Loading SQLite Players from the whitelist...");
            s = sql.createStatement();
            rs = s.executeQuery("SELECT * FROM whitelist");
            while (rs.next()) {
                // @formatter:off
                String username       = rs.getString("username"      );
                boolean encrypted     = rs.getString("encryptedPwd"  ).equalsIgnoreCase("true");
                String password       = rs.getString("password"      );
                boolean administrator = rs.getString("admin"         ).equalsIgnoreCase("true");
                boolean banned        = rs.getString("banned"        ).equalsIgnoreCase("true");
                String steamID        = rs.getString("steamid"       );
                String ownerID        = rs.getString("ownerid"       );
                String lastConnection = rs.getString("lastConnection");
                // @formatter:on
                // Make sure not to load the admin account. Sledgehammer deals with that already.
                if (username.equalsIgnoreCase("admin")) continue;
                console.println("Loaded player: " + username);
                SQLPlayer player = new SQLPlayer(username, password, encrypted);
                player.setAdministrator(administrator);
                player.setBanned(banned);
                player.setSteamId(steamID);
                player.setOwnerId(ownerID);
                player.setLastConnection(lastConnection);
                listSQLPlayers.add(player);
            }
            rs.close();
            s.close();
            console.println("Loaded " + listSQLPlayers.size() + " player" + (listSQLPlayers.size() == 1 ? "" : "s")
                    + ".", "");
            console.println("Loading IP bans...");
            s = sql.createStatement();
            rs = s.executeQuery("SELECT * FROM bannedip");
            while (rs.next()) {
                // @formatter:off
                String ip       = rs.getString("ip"      );
                String username = rs.getString("username");
                String reason   = rs.getString("reason"  );
                // @formatter:on
                console.println("Loaded IP Ban: " + ip + " (Username: " + username + ").");
                SQLBanIP ban = new SQLBanIP(ip, username, reason);
                listBannedIPs.add(ban);
            }
            rs.close();
            s.close();
            console.println("Loaded " + listBannedIPs.size() + "IP ban" + (listBannedIPs.size() == 1 ? "" : "s")
                    + ".", "");
            console.println("Loading Steam ID bans...");
            s = sql.createStatement();
            rs = s.executeQuery("SELECT * FROM bannedid");
            while (rs.next()) {
                // @formatter:off
                String steamid  = rs.getString("steamid" );
                String username = rs.getString("username");
                String reason   = rs.getString("reason"  );
                // @formatter:on
                console.println("Loaded Steam ID Ban: " + steamid + " (Username: " + username + ").");
                SQLBanID ban = new SQLBanID(steamid, username, reason);
                listBannedIDs.add(ban);
            }
            rs.close();
            s.close();
            console.println("Loaded " + listBannedIDs.size() + " Steam ID ban" + (listBannedIPs.size() == 1 ? "" : "s")
                    + ".", "");
            sql.close();
        } catch (Exception e) {
            console.println("ERROR: " + e.toString());
            e.printStackTrace();
            return;
        }

        try {
            loadMongoClient(console);
            console.println("Successfully connected to the MongoDB server..");
        } catch (Exception e) {
            console.println("ERROR: Failed to connect to MongoDB server.");
            console.println(e);
            e.printStackTrace();
            return;
        }


        DB db = client.getDB(mainWindow.getDatabaseDatabase());
        DBCollection collectionMongoPlayers = db.getCollection("sledgehammer_players");

        List<DBObject> listMongoPlayers = new ArrayList<>();
        console.println("Players already in MongoDB:");
        DBCursor cursor = collectionMongoPlayers.find();
        while (cursor.hasNext()) {
            DBObject object = cursor.next();
            listMongoPlayers.add(object);
            console.println(object.get("username"));
        }
        cursor.close();

        List<DBObject> listPlayersToInsert = new ArrayList<>();

        console.println("Converting players to MongoDB...");
        for (SQLPlayer player : listSQLPlayers) {
            String sqlPlayerUsername = player.getUsername();
            boolean exists = false;
            for (DBObject objectPlayer : listMongoPlayers) {
                String mongoPlayerUsername = (String) objectPlayer.get("username");
                exists = sqlPlayerUsername.equalsIgnoreCase(mongoPlayerUsername);
                if(exists) {
                    break;
                }
            }
            if (exists) {
                console.println("Player already exists in the MongoDB server: \"" + player.getUsername() + "\".");
                continue;
            }

            console.println("Adding player: " + player.getUsername());

            String ownerId = player.getOwnerId() == null ? "-1" : player.getOwnerId();
            DBObject object = new BasicDBObject();
            // @formatter:off
            object.put("id"               , UUID.randomUUID()                   );
            object.put("username"         , player.getUsername()                );
            object.put("passwordEncrypted", player.getEncryptedPassword()       );
            object.put("admin"            , player.isAdministrator() ? "1" : "0");
            object.put("banned"           , player.isBanned() ? "1" : "0"       );
            object.put("steamID"          , player.getSteamId()                 );
            object.put("steamIDOwner"     , ownerId                             );
            object.put("metadata"         , new BasicDBObject()                 );
            listPlayersToInsert.add(object);
            // @formatter:on
        }

        DBObject objects[] = new DBObject[listPlayersToInsert.size()];
        for(int index = 0; index < listPlayersToInsert.size(); index++) {
            objects[index] = listPlayersToInsert.get(index);
        }
        collectionMongoPlayers.insert(objects);



        console.println("Conversion completed.");
        client.close();
    }

    private void loadMongoClient(ConsoleTextArea console) {
        String mongoURL = getConnectionURL();
        console.println("URL: " + mongoURL);
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.SEVERE);
        if (client == null) {
            client = new MongoClient(new MongoClientURI(mongoURL));
        }
    }

    /**
     * Connect to a sample database
     */
    public Connection connect(ConsoleTextArea console, File file) {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:" + file.getAbsolutePath();
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            console.println("Connection to the SQLite database has been established.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public String getConnectionURL() {
        // @formatter:off
        String username = mainWindow.getDatabaseUsername();
        String password = mainWindow.getDatabasePassword();
        String url      = mainWindow.getDatabaseURL()     ;
        String port     = mainWindow.getDatabasePort()    ;
        // @formatter:on
        return "mongodb://" + username + ":" + password + "@" + url + ":" + port;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new SQLiteToMongo();
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
        System.out.println("Set path to: " + filePath);
    }

    private Image getScaledImage(Image srcImg, int w, int h) {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();

        return resizedImg;
    }
}