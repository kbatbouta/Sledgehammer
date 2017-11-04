package sledgehammer.database;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import sledgehammer.SledgeHammer;
import sledgehammer.objects.Player;
import sledgehammer.util.StringUtils;
import zombie.core.znet.SteamUtils;

public class SledgehammerDatabase extends MongoDatabase {

	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private DB dbSledgehammer;
	private DBCollection collectionPlayers;
	private DBCollection collectionBans;
	
	private Map<String, MongoPlayer> mapPlayersByUsername;
	private Map<UUID, MongoPlayer> mapPlayersByUUID;
	private Map<Long, MongoPlayer> mapPlayersBySteamID;

	public SledgehammerDatabase() {
		super();
		mapPlayersByUsername = new HashMap<>();
		mapPlayersByUUID = new HashMap<>();
		mapPlayersBySteamID = new HashMap<>();
	}

	@Override
	public void connect(String url) {
		super.connect(url);
		println("Connected.");
	}
	
	@Override
	public void reset() {

	}

	/**
	 * Checks to see if a Player exists.
	 * 
	 * @param username
	 *            The username of the player.
	 * @return
	 */
	public boolean playerExists(String username) {
		if (username == null || username.isEmpty()) {
			throw new IllegalArgumentException("SledgehammerDatabase: Username given is null or empty!");
		}
		boolean returned = false;
		returned = mapPlayersByUsername.containsKey(username);
		if(!returned) {			
			DBCursor cursor = collectionPlayers.find(new BasicDBObject("username", username));
			returned = cursor.hasNext();
			cursor.close();
		}
		System.out.println("SledgehammerDatabase->playerExists(" + username + ") -> " + returned + ";");
		return returned;
	}

	/**
	 * Checks to see if a Player exists.
	 * 
	 * @param uniqueId
	 *            The UUID of the Player.
	 * @return
	 */
	public boolean playerExists(UUID uniqueId) {
		if (uniqueId == null) {
			throw new IllegalArgumentException("SledgehammerDatabase: uniqueId given is null!");
		}
		boolean returned = false;
		returned = mapPlayersByUUID.containsKey(uniqueId);
		if(!returned) {			
			DBCursor cursor = collectionPlayers.find(new BasicDBObject("uuid", uniqueId.toString()));
			returned = cursor.hasNext();
			cursor.close();
		}
		System.out.println("SledgehammerDatabase->playerExists(" + uniqueId.toString() + ") -> " + returned + ";");
		return returned;
	}
	
	
	public MongoPlayer getMongoPlayer(UUID uniqueId) {
		if (uniqueId == null) {
			throw new IllegalArgumentException("SledgehammerDatabase: uniqueId given is null!");
		}
		MongoPlayer player = null;
		System.out.println("SledgehammerDatabase->getMongoPlayer(" + uniqueId.toString() + ");");
		player = mapPlayersByUUID.get(uniqueId);
		if(player == null) {			
			DBCursor cursor = collectionPlayers.find(new BasicDBObject("uuid", uniqueId.toString()));
			if(cursor.hasNext()) {
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
		MongoPlayer player = null;
		System.out.println("SledgehammerDatabase->getMongoPlayer(" + username + ");");
		player = mapPlayersByUsername.get(username);
		if(player == null) {			
			DBCursor cursor = collectionPlayers.find(new BasicDBObject("username", username));
			if(cursor.hasNext()) {
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
		MongoPlayer player = null;
		System.out.println("SledgehammerDatabase->getMongoPlayer(" + username + ", " + password + ");");
		player = mapPlayersByUsername.get(username);
		if(player != null && !player.passwordsMatch(password)) {
			player = null;
		}
		if(player == null) {			
			DBCursor cursor = collectionPlayers.find(new BasicDBObject("username", username).append("password", StringUtils.md5(password)));
			if(cursor.hasNext()) {
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
		System.out.println("SledgehammerDatabase->getMongoPlayer(" + steamID + ");");
		DBCursor cursor = collectionPlayers.find(new BasicDBObject("steamID", "" + steamID));
		if(cursor.hasNext()) {
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
	@SuppressWarnings("deprecation")
	public void onConnection(MongoClient client) {
		dbSledgehammer = client.getDB(SledgeHammer.instance.getSettings().getDatabase());
		setDatabase(dbSledgehammer);
		collectionPlayers = dbSledgehammer.getCollection("sledgehammer_players");
		collectionBans    = dbSledgehammer.getCollection("sledgehammer_bans");
	}

	@Override
	public String getName() {
		return "SledgehammerDatabase";
	}

	public MongoPlayer createPlayer(String user, String pass) {
		if (user == null || user.isEmpty()) {
			throw new IllegalArgumentException("SledgehammerDatabase: Username is null or empty!");
		}
		System.out.println("SledgehammerDatabase->createPlayer(\"" + user + "\", \"" + StringUtils.md5(pass) + "\");");
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
		if(cursor.hasNext()) {
			returned = new MongoBan(collectionBans);
			returned.load(cursor.next());
		}
		cursor.close();
		return returned;
	}

	/**
	 * @param steamID
	 *            The Steam ID being used to search for accounts.
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
	 * Creates an unsaved <MongoBan> document.
	 * @param id The ID of the Ban.
	 * @param username
	 * @param reason
	 * @param steam
	 * @param banned
	 * @return
	 */
	public MongoBan createBan(String id, String username, String reason, boolean steam, boolean banned) {
		MongoBan ban = new MongoBan(collectionBans, id, username, reason, steam, banned);
		return ban;
	}

	public UUID getPlayerID(String username) {
		if (username == null || username.isEmpty()) {
			throw new IllegalArgumentException("SledgehammerDatabase: Username is null or empty!");
		}
		UUID returned = null;
		DBCursor cursor = collectionPlayers.find(new BasicDBObject("username", username));
		if(cursor.hasNext()) {
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
		if(SteamUtils.isSteamModeEnabled()) {
			this.mapPlayersBySteamID.put(player.getSteamId(), player);
		}
	}
	
	public Map<String, Long> getAllMongoPlayers() {
		Map<String, Long> returned = new HashMap<>();
		DBCursor cursor = collectionPlayers.find();
		while(cursor.hasNext()) {
			DBObject object = cursor.next();
			String username = object.get("username").toString();
			long time = Long.parseLong(object.get("timeConnectedLast").toString());
			if(time > -1L) {
				returned.put(username, time);
			}
		}
		cursor.close();
		return returned;
	}

}