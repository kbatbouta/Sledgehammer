package sledgehammer.database.core;

/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
*/

import com.mongodb.DBObject;

import sledgehammer.database.MongoCollection;
import sledgehammer.database.document.MongoDocument;

/**
 * TODO: Document
 * 
 * @author Jab
 */
public class MongoBan extends MongoDocument {

	private String id;
	private String username;
	private String reason;
	boolean steam = false;
	boolean banned = true;

	public MongoBan(MongoCollection collection) {
		super(collection, "id");
	}

	public MongoBan(MongoCollection collection, String id, String username, String reason, boolean steam,
			boolean banned) {
		super(collection, "id");
		setID(id);
		setUsername(username);
		setReason(reason);
		setSteam(steam);
		setBanned(banned);
	}

	@Override
	public void onLoad(DBObject object) {
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
	public void onSave(DBObject object) {
		// @formatter:off
		object.put("id"      , id                    );
		object.put("username", getUsername()         );
		object.put("steam"   , isSteam() ? "1" : "0" );
		object.put("reason"  , getReason()           );
		object.put("banned"  , isBanned() ? "1" : "0");
		// @formatter:on
	}

	@Override
	public void delete() {
		setBanned(false);
		super.delete();
	}

	@Override
	public Object getFieldValue() {
		return getID();
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
	 * @return Returns the raw ID of the ban.
	 */
	private String getID() {
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

	public void setBanned(boolean flag) {
		if (flag != this.banned) {
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