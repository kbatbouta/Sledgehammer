package sledgehammer.database;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoBan extends MongoDocument {

	private String id;
	private String username;
	private String reason;
	boolean steam = false;
	boolean banned = true;

	public MongoBan(DBCollection collection) {
		super(collection);
	}
	
	public MongoBan(DBCollection collection, String id, String username, String reason, boolean steam, boolean banned) {
		super(collection);
		setID(id);
		setUsername(username);
		setReason(reason);
		setSteam(steam);
		setBanned(banned);
	}

	@Override
	public void load(DBObject object) {
		Object oSteam = object.get("steam");
		if (oSteam != null) {
			setSteam(oSteam.toString().equals("1"));
		}
		Object oID = object.get("id");
		if (oID != null) {
			setID(oID.toString());
		}
		Object oUsername = object.get("username");
		if (oUsername != null) {
			setUsername(oUsername.toString());
		}
		Object oReason = object.get("reason");
		if (oReason != null) {
			setReason(oReason.toString());
		}
		Object oBanned = object.get("banned");
		if (oBanned != null) {
			setBanned(object.toString().equals("1"));
		}
	}
	
	@Override
	public void save() {
		// @formatter:off
		DBObject object = new BasicDBObject();
		object.put("id"      , id                    );
		object.put("username", getUsername()         );
		object.put("steam"   , isSteam() ? "1" : "0" );
		object.put("reason"  , getReason()           );
		object.put("banned"  , isBanned() ? "1" : "0");
		MongoDatabase.upsert(getCollection(), "id", object);
		// @formatter:on
	}

	public boolean isSteam() {
		return this.steam;
	}

	private void setSteam(boolean flag) {
		this.steam = flag;
	}

	/**
	 * @return The IP address of the Ban.
	 * @throws Throws
	 *             <IllegalStateException> if the Ban is set to Steam ID.
	 */
	public String getIPAddress() {
		if (isSteam()) {
			throw new IllegalStateException("Requested IP when Ban is a Steam ban.");
		}
		return this.id;
	}

	/**
	 * @return The Steam ID of the Ban.
	 * @throws Throws
	 *             <IllegalStateException> if the Ban is not set to Steam ID.
	 */
	public String getSteamID() {
		if (!isSteam()) {
			throw new IllegalStateException("Requesed Steam ID when Ban is IP Address.");
		}
		return this.id;
	}

	/**
	 * (Internal Method)
	 * 
	 * Sets the ID of the ban.
	 * 
	 * @param id
	 */
	private void setID(String id) {
		this.id = id;
	}

	public String getReason() {
		return this.reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * Removes the <MongoBan>.
	 */
	public void delete() {
		MongoDatabase.delete(getCollection(), "id", id);
		banned = false;
	}
	
	public void setBanned(boolean flag) {
		if(flag != this.banned) {			
			this.banned = flag;
			save();
		}
	}
	
	public boolean isBanned() {
		return this.banned;
	}
	
	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}