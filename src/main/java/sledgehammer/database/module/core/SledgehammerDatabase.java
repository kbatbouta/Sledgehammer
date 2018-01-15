/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

package sledgehammer.database.module.core;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import sledgehammer.Settings;
import sledgehammer.database.MongoCollection;
import sledgehammer.database.MongoDatabase;
import sledgehammer.lua.core.Player;
import zombie.core.znet.SteamUtils;
import zombie.sledgehammer.util.MD5;

public class SledgehammerDatabase extends MongoDatabase {

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private DB dbSledgehammer;

    private MongoCollection collectionPlayers;
    private MongoCollection collectionBans;

    private Map<String, MongoPlayer> mapPlayersByUsername;
    private Map<UUID, MongoPlayer> mapPlayersByUUID;
    private Map<Long, MongoPlayer> mapPlayersBySteamID;

    public SledgehammerDatabase() {
        super();
        // @formatter: off
        mapPlayersByUsername = new HashMap<>();
        mapPlayersByUUID = new HashMap<>();
        mapPlayersBySteamID = new HashMap<>();
        // @formatter: on
    }

    @Override
    public void connect(String url) {
        super.connect(url);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onConnection(MongoClient client) {
        dbSledgehammer = client.getDB(Settings.getInstance().getDatabaseDatabase());
        setDatabase(dbSledgehammer);
        collectionPlayers = createMongoCollection("sledgehammer_players");
        collectionBans = createMongoCollection("sledgehammer_bans");
    }

    @Override
    public void reset() {

    }

    /**
     * Checks to see if a Player exists.
     *
     * @param username The username of the player.
     * @return Returns true if the Player exists.
     */
    public boolean playerExists(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("SledgehammerDatabase: Username given is null or empty!");
        }
        boolean returned;
        returned = mapPlayersByUsername.containsKey(username);
        if (!returned) {
            DBCursor cursor = collectionPlayers.find(new BasicDBObject("username", username));
            returned = cursor.hasNext();
            cursor.close();
        }
        return returned;
    }

    /**
     * Checks to see if a Player exists.
     *
     * @param uniqueId The Unique ID of the Player.
     * @return Returns true if the Player exists.
     */
    public boolean playerExists(UUID uniqueId) {
        if (uniqueId == null) {
            throw new IllegalArgumentException("SledgehammerDatabase: uniqueId given is null!");
        }
        boolean returned;
        returned = mapPlayersByUUID.containsKey(uniqueId);
        if (!returned) {
            DBCursor cursor = collectionPlayers.find(new BasicDBObject("uuid", uniqueId.toString()));
            returned = cursor.hasNext();
            cursor.close();
        }
        return returned;
    }

    public MongoPlayer getMongoPlayer(UUID uniqueId) {
        if (uniqueId == null) {
            throw new IllegalArgumentException("SledgehammerDatabase: uniqueId given is null!");
        }
        MongoPlayer player;
        player = mapPlayersByUUID.get(uniqueId);
        if (player == null) {
            DBCursor cursor = collectionPlayers.find(new BasicDBObject("uuid", uniqueId.toString()));
            if (cursor.hasNext()) {
                player = new MongoPlayer(collectionPlayers, cursor.next());
                registerPlayer(player);
            }
            cursor.close();
        }
        return player;
    }

    public MongoPlayer getMongoPlayer(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("SledgehammerDatabase: Username given is null or empty!");
        }
        MongoPlayer player;
        player = mapPlayersByUsername.get(username);
        if (player == null) {
            DBCursor cursor = collectionPlayers.find(new BasicDBObject("username", username));
            if (cursor.hasNext()) {
                player = new MongoPlayer(collectionPlayers, cursor.next());
                registerPlayer(player);
            }
            cursor.close();
        }
        return player;
    }

    public MongoPlayer getMongoPlayer(String username, String password) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("SledgehammerDatabase: Username given is null or empty!");
        }
        MongoPlayer player;
        player = mapPlayersByUsername.get(username);
        if (player != null && !player.passwordsMatch(password)) {
            player = null;
        }
        if (player == null) {
            DBCursor cursor = collectionPlayers
                    .find(new BasicDBObject("username", username).append("password", MD5.encrypt(password)));
            if (cursor.hasNext()) {
                player = new MongoPlayer(collectionPlayers, cursor.next());
                registerPlayer(player);
            }
            cursor.close();
        }
        return player;
    }

    public MongoPlayer getMongoPlayer(long steamID) {
        if (steamID == -1L) {
            throw new IllegalArgumentException("SledgehammerDatabase: Steam ID is invalid: " + steamID);
        }
        MongoPlayer player = null;
        DBCursor cursor = collectionPlayers.find(new BasicDBObject("steamID", "" + steamID));
        if (cursor.hasNext()) {
            player = new MongoPlayer(collectionPlayers, cursor.next());
            registerPlayer(player);
        }
        cursor.close();
        return player;
    }

    public MongoPlayer getMongoPlayer(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("SledgehammerDatabase: Player is null!");
        }
        return getMongoPlayer(player.getUsername());
    }

    @Override
    public String getName() {
        return "SledgehammerDatabase";
    }

    public MongoPlayer createPlayer(String user, String pass) {
        if (user == null || user.isEmpty()) {
            throw new IllegalArgumentException("SledgehammerDatabase: Username is null or empty!");
        }
        MongoPlayer player = new MongoPlayer(collectionPlayers, user, pass);
        player.save();
        registerPlayer(player);
        return player;
    }

    public MongoBan getBan(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("SledgehammerDatabase: Ban ID is null or empty!");
        }
        MongoBan returned = null;
        DBCursor cursor = collectionBans.find(new BasicDBObject("id", id));
        if (cursor.hasNext()) {
            returned = new MongoBan(collectionBans);
            returned.onLoad(cursor.next());
        }
        cursor.close();
        return returned;
    }

    /**
     * @param steamID The Steam ID being used to search for accounts.
     * @return The number of accounts registered under the given Steam ID.
     */
    public int getNumberOfAccounts(long steamID) {
        if (steamID == -1L) {
            throw new IllegalArgumentException("SledgehammerDatabase: Steam ID is invalid: " + steamID);
        }
        DBCursor cursor = collectionPlayers.find(new BasicDBObject("steamID", "" + steamID));
        int size = cursor.size();
        cursor.close();
        return size;
    }

    /**
     * Creates an unsaved MongoBan document.
     *
     * @param id       The ID of the Ban.
     * @param username The user-name of the Player.
     * @param reason   The reason for the Ban.
     * @param steam    True if the Ban is SteamID. False if the Ban is I.P. Address.
     * @param banned   Flag to set the ban.
     * @return Returns the created MongoBan.
     */
    public MongoBan createBan(String id, String username, String reason, boolean steam, boolean banned) {
        return new MongoBan(collectionBans, id, username, reason, steam, banned);
    }

    public UUID getPlayerID(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("SledgehammerDatabase: Username is null or empty!");
        }
        UUID returned = null;
        DBCursor cursor = collectionPlayers.find(new BasicDBObject("username", username));
        if (cursor.hasNext()) {
            DBObject object = cursor.next();
            returned = UUID.fromString(object.get("uuid").toString());
        }
        cursor.close();
        return returned;
    }

    public DBCollection getCollection(String collectionName) {
        if (collectionName == null || collectionName.isEmpty()) {
            throw new IllegalArgumentException("SledgehammerDatabase: Player is null!");
        }
        return this.getDatabase().getCollection(collectionName);
    }

    private void registerPlayer(MongoPlayer player) {
        if (player == null) {
            throw new IllegalArgumentException("SledgehammerDatabase: Player is null!");
        }
        this.mapPlayersByUsername.put(player.getUsername(), player);
        this.mapPlayersByUUID.put(player.getUniqueId(), player);
        if (SteamUtils.isSteamModeEnabled()) {
            this.mapPlayersBySteamID.put(player.getSteamId(), player);
        }
    }

    public Map<String, Long> getAllMongoPlayers() {
        Map<String, Long> returned = new HashMap<>();
        DBCursor cursor = collectionPlayers.find();
        while (cursor.hasNext()) {
            DBObject object = cursor.next();
            String username = object.get("username").toString();
            long time = Long.parseLong(object.get("timeConnectedLast").toString());
            if (time > -1L) {
                returned.put(username, time);
            }
        }
        cursor.close();
        return returned;
    }

    public static String getConnectionURL() {
        Settings settings = Settings.getInstance();
        String username = settings.getDatabaseUsername();
        String password = settings.getDatabasePassword();
        String url = settings.getDatabaseURL();
        int port = settings.getDatabasePort();
        return "mongodb://" + username + ":" + password + "@" + url + ":" + port;
    }
}