package sledgehammer.conversion;

import com.mongodb.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLiteToMongo {

    public static SQLiteToMongo instance;

    private Connection sql;
    private MongoClient client;
    private DBCollection collectionMongoPlayers;
    private DBCollection collectionBans;
    private List<SQLPlayer> listSQLPlayers;
    private List<SQLBanIP> listBannedIPs;
    private List<SQLBanID> listBannedIDs;
    private List<DBObject> listMongoPlayers;
    private List<DBObject> listMongoBans;
    private List<DBObject> listPlayersToInsert;
    private List<DBObject> listBansToInsert;
    private JFrame jFrame;
    private MainWindow mainWindow;
    private ConsoleTextArea console;
    private String filePath;

    // @formatter:off
    private static String testStart = "#######################################################\n" +
                                      "# CONVERSION STARTED                                   \n" +
                                      "#######################################################\n" ;
    private static String testError = "#######################################################\n" +
                                      "# ERROR                                                \n" +
                                      "#######################################################\n" ;
    private static String testCompl = "#######################################################\n" +
                                      "# CONVERSION COMPLETED                                 \n" +
                                      "#######################################################\n" ;
    // @formatter:on

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
                System.out.println("test");
                String path = "mongoconversiontool" + File.separator + "src" + File.separator + "main"
                        + File.separator + "resources" + File.separator + "favicon.png";
                System.out.println("Path: " + path);
                imageIcon = ImageIO.read(new File(path));
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
        console = mainWindow.getConsole();
        console.clear();
        console.println(testStart);
        try {
            long timeStarted = System.currentTimeMillis();
            loadSQLite();
            loadMongoDB();
            convert();
            long timeFinished = System.currentTimeMillis();
            double seconds = (timeFinished - timeStarted) / 1000.0D;
            console.println(testCompl, "", "Done! Took " + seconds + " Seconds.");
        } catch (Exception e) {
            console.println(testError);
            console.printStackTrace(e);
            e.printStackTrace();
        }
        freeResources();
    }

    private void loadSQLite() throws SQLException {
        Statement s;
        ResultSet rs;
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("SQLite DB file not set.");
        }
        File fileDB = new File(filePath);
        if (!fileDB.exists()) {
            throw new IllegalArgumentException("SQLite DB file does not exist.");
        }
        if (fileDB.isDirectory()) {
            throw new IllegalArgumentException("SQLite DB file set is a directory and not a file.");
        }
        if (!fileDB.getName().toLowerCase().endsWith(".db")) {
            throw new IllegalArgumentException("SQLite DB file given is not a valid DB file. "
                    + "SQLite DB files end with a '.db' extension.");
        }
        listSQLPlayers = new ArrayList<>();
        listBannedIPs = new ArrayList<>();
        listBannedIDs = new ArrayList<>();
        sql = connect(fileDB);
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
        console.println("Loaded " + listBannedIPs.size() + " IP ban" + (listBannedIPs.size() == 1 ? "" : "s")
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
    }

    @SuppressWarnings({"deprecated"})
    private void loadMongoDB() {
        loadMongoClient();
        DB db = client.getDB(mainWindow.getDatabaseDatabase());
        collectionMongoPlayers = db.getCollection("sledgehammer_players");
        listMongoPlayers = new ArrayList<>();
        console.println("Players already in MongoDB:");
        DBCursor cursor = collectionMongoPlayers.find();
        while (cursor.hasNext()) {
            DBObject object = cursor.next();
            listMongoPlayers.add(object);
            console.println(object.get("username"));
        }
        cursor.close();
        console.println("Loaded " + listMongoPlayers.size() + " MongoDB Player entr" +
                (listMongoPlayers.size() == 1 ? "y" : "ies") + ".", "");
        collectionBans = db.getCollection("sledgehammer_bans");
        listMongoBans = new ArrayList<>();
        cursor = collectionBans.find();
        while (cursor.hasNext()) {
            listMongoBans.add(cursor.next());
        }
        cursor.close();
        console.println("Loaded " + listMongoBans.size() + " MongoDB Ban entr" +
                (listMongoBans.size() == 1 ? "y" : "ies") + ".", "");
    }

    private void convert() {
        listPlayersToInsert = new ArrayList<>();
        listBansToInsert = new ArrayList<>();
        console.println("Converting players to MongoDB...");
        for (SQLPlayer player : listSQLPlayers) {
            String sqlPlayerUsername = player.getUsername();
            boolean exists = false;
            for (DBObject objectPlayer : listMongoPlayers) {
                String mongoPlayerUsername = (String) objectPlayer.get("username");
                exists = sqlPlayerUsername.equalsIgnoreCase(mongoPlayerUsername);
                if (exists) {
                    break;
                }
            }
            if (exists) {
                console.println("Player already exists in the MongoDB server: \"" + player.getUsername() + "\".");
                continue;
            }
            String ownerId = player.getOwnerId() == null ? "-1" : player.getOwnerId();
            DBObject object = new BasicDBObject();
            // @formatter:off
            object.put("id"               , UUID.randomUUID()                   );
            object.put("username"         , player.getUsername()                );
            object.put("passwordEncrypted", player.getEncryptedPassword()       );
            object.put("admin"            , player.isAdministrator() ? "1" : "0");
            object.put("banned"           , player.isBanned() ? "1" : "0"       );
            object.put("timeConnectedLast", player.getLastConnection() + ""  );
            object.put("steamID"          , player.getSteamId()                 );
            object.put("steamIDOwner"     , ownerId                             );
            object.put("metadata"         , new BasicDBObject()                 );
            listPlayersToInsert.add(object);
            // @formatter:on
        }
        insertDocuments(collectionMongoPlayers, listPlayersToInsert);
        console.println("Converted " + listPlayersToInsert.size() + " player" +
                (listPlayersToInsert.size() == 1 ? "" : "s") + " to MongoDB.", "");

        console.println("Converting IP Bans to MongoDB format...");
        for (SQLBanIP ban : listBannedIPs) {
            boolean exists = false;
            for (DBObject o : listMongoBans) {
                exists = ban.getIP().equalsIgnoreCase((String) o.get("id"));
                if (exists) {
                    break;
                }
            }
            if (exists) {
                console.println("Skipping IP Ban (Conflict in MongoDB): " + ban.toString());
                continue;
            }
            DBObject object = new BasicDBObject();
            // @formatter:off
            object.put("id"      , ban.getIP()      );
            object.put("username", ban.getUsername());
            object.put("steam"   , false         );
            object.put("reason"  , ban.getReason()  );
            object.put("banned"  , true          );
            // @formatter:on
            listBansToInsert.add(object);
            console.println("Added Ban: " + ban.toString());
        }
        int size = listBansToInsert.size();
        console.println("Converted " + size + " IP Ban" + (size == 1 ? "" : "s") + ".", "");
        console.println("Converting SteamID Bans to MongoDB format...");
        for (SQLBanID ban : listBannedIDs) {
            boolean exists = false;
            for (DBObject o : listMongoBans) {
                exists = ban.getID().equalsIgnoreCase((String) o.get("id"));
                if (exists) {
                    break;
                }
            }
            if (exists) {
                console.println("Skipping SteamID Ban (Conflict in MongoDB): " + ban.toString());
                continue;
            }
            DBObject object = new BasicDBObject();
            // @formatter:off
            object.put("id"      , ban.getID()      );
            object.put("username", ban.getUsername());
            object.put("steam"   , true          );
            object.put("reason"  , ban.getReason()  );
            object.put("banned"  , true          );
            // @formatter:on
            listBansToInsert.add(object);
            console.println("Added Ban: " + ban.toString());
        }
        size = listBansToInsert.size() - size;
        console.println("Converted " + size + " SteamID Ban" + (size == 1 ? "" : "s") + ".", "");
        console.println("Saving MongoDB bans...");
        // Insert the SQL Bans into the MongoDB Collection.
        insertDocuments(collectionBans, listBansToInsert);
        console.println("Saved MongoDB bans.", "");
        // Add IP & ID Bans
        console.println("Conversion completed.");
        client.close();
    }

    private void freeResources() {
        console.println("Attempting to free resources...");
        // Clear all of the fields. @formatter:off
        listSQLPlayers      = null;
        listBannedIDs       = null;
        listBannedIPs       = null;
        listBansToInsert    = null;
        listPlayersToInsert = null;
        listMongoPlayers    = null;
        listMongoBans       = null;
        // @formatter:on
        try {
            if (sql != null && !sql.isClosed()) {
                sql.close();
            }
            sql = null;
            if (client != null) {
                client.close();
            }
            client = null;
            collectionMongoPlayers = null;
            collectionBans = null;
        } catch (Exception e) {
            console.println(testError);
            console.printStackTrace(e);
            e.printStackTrace();
        }
    }

    private void insertDocuments(DBCollection collection, List<DBObject> listDocuments) {
        if (listDocuments.size() > 0) {
            DBObject objects[] = new DBObject[listDocuments.size()];
            for (int index = 0; index < listDocuments.size(); index++) {
                objects[index] = listDocuments.get(index);
            }
            collection.insert(objects);
        }
    }

    private void loadMongoClient() {
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
    public Connection connect(File file) {
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

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new SQLiteToMongo();
    }

    /**
     * (Private Method)
     *
     * @param jar    The File Object of the Jar File.
     * @param source The source path inside the Jar File.
     * @return Returns an InputStream of the Jar File Entry.
     * @throws IOException Thrown with File Exceptions.
     */
    private static InputStream getStream(File jar, String source) throws IOException {
        return new URL("jar:file:" + jar.getAbsolutePath() + "!/" + source).openStream();
    }
}