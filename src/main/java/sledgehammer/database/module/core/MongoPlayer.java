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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import sledgehammer.database.MongoCollection;
import sledgehammer.database.document.MongoUniqueDocument;
import zombie.sledgehammer.util.MD5;

/**
 * MongoDocument for Player information.
 * <p>
 * TODO: Document
 *
 * @author Jab
 */
public class MongoPlayer extends MongoUniqueDocument {

    private Map<String, String> mapMetadata;
    private String username;
    private String nickname;
    private String passwordEncrypted;
    private long steamIdOwner = -1L;
    private long steamId = -1L;
    private long timeConnectedLast;
    private long timeConnected;
    private long timeCreated;
    private boolean newAccount;
    private boolean admin;
    private boolean banned;

    /**
     * Empty constructor for lookups. (Steam IDs)
     *
     * @param collection The MongoCollection storing the MongoDocument.
     */
    public MongoPlayer(MongoCollection collection) {
        super(collection);
        reset();
    }

    /**
     * Main constructor.
     *
     * @param collection The MongoCollection storing the MongoDocument.
     * @param username   The name of the Player.
     */
    public MongoPlayer(MongoCollection collection, String username) {
        super(collection);
        // Reset all fields to a new player.
        reset();
        // Set the username given.
        setUsername(username);
        // Check if the player exists. If the player exists, load.
        DBCursor cursor = collection.getDBCollection().find(new BasicDBObject("username", username));
        if (cursor.hasNext()) {
            onLoad(cursor.next());
        }
        cursor.close();
    }

    public MongoPlayer(MongoCollection collection, DBObject object) {
        super(collection, (UUID) object.get("id"));
        reset();
        onLoad(object);
    }

    public MongoPlayer(MongoCollection collection, UUID uuid) {
        super(collection, uuid);
        reset();
        DBCursor cursor = collection.getDBCollection().find(new BasicDBObject(getFieldId(), getFieldValue()));
        if (cursor.hasNext()) {
            onLoad(cursor.next());
        }
        cursor.close();
    }

    public MongoPlayer(MongoCollection collection, String username, String password) {
        super(collection);
        // Reset all fields to a new player.
        reset();
        // Set the username given.
        setUsername(username);
        if (password != null && !password.isEmpty()) {
            setPassword(password);
        }
    }

    @Override
    public void onLoad(DBObject object) {
        // We are loading an existing account. This is now false.
        setNewAccount(false);
        // Load the uuid for the player.
        UUID uuid = (UUID) object.get("id");
        if (uuid != null) {
            setUniqueId(uuid, false);
        }
        // Load the username for the player.
        Object oUsername = object.get("username");
        if (oUsername != null) {
            setUsername(oUsername.toString());
        }
        // Load the nickname for the player.
        Object oNickname = object.get("nickname");
        if (oNickname != null) {
            setNickname(oNickname.toString());
        }
        // Load the encrypted password for the player.
        Object oPasswordEncrypted = object.get("passwordEncrypted");
        if (oPasswordEncrypted != null) {
            setEncryptedPassword(oPasswordEncrypted.toString());
        }
        // Load the admin flag for the player.
        Object oAdmin = object.get("admin");
        if (oAdmin != null) {
            setAdministrator(oAdmin.toString().equals("1"), false);
        }
        // Load the ban flag for the player.
        Object oBanned = object.get("banned");
        if (oBanned != null) {
            setBanned(oBanned.toString().equals("1"));
        }
        // Load the Time the player has connected last.
        Object oTimeConnectedLast = object.get("timeConnectedLast");
        if (oTimeConnectedLast != null) {
            setTimeConnectedLast(Long.parseLong(oTimeConnectedLast.toString()));
        }
        // Load the Time the player first joined.
        Object oTimeCreated = object.get("timeCreated");
        if (oTimeCreated != null) {
            setTimeCreated(Long.parseLong(oTimeCreated.toString()));
        }
        // Load the Steam ID.
        Object oSteamID = object.get("steamID");
        if (oSteamID != null) {
            setSteamId(Long.parseLong(oSteamID.toString()));
        }
        // Load the Owner ID for Steam.
        Object oSteamIDOwner = object.get("steamIDOwner");
        if (oSteamIDOwner != null) {
            setSteamOwnerId(Long.parseLong(oSteamIDOwner.toString()));
        }
        // Load the metadata.
        loadMetadata(object);
    }

    @Override
    public void onSave(DBObject object) {
        // @formatter:off
		object.put("username"         , getUsername()                 );
		object.put("nickname"         , getNickname()                 );
		object.put("passwordEncrypted", getEncryptedPassword()        );
		object.put("admin"            , isAdministrator() ? "1" : "0" );
		object.put("banned"           , isBanned() ? "1" : "0"        );
		object.put("timeConnectedLast", getTimeConnectedLast() + "");
		object.put("steamID"          , getSteamId() + ""          );
		object.put("steamIDOwner"     , getSteamOwnerId() + ""     );
		object.put("metadata"         , createMetadataDocument()      );
		// @formatter:on
    }

    private void reset() {
        setNewAccount(true);
        this.username = null;
        this.mapMetadata = new HashMap<>();
        this.steamIdOwner = -1L;
        this.steamId = -1L;
        this.timeConnectedLast = -1L;
        setTimeConnected(System.currentTimeMillis());
        this.timeCreated = -1L;
        this.admin = false;
        this.banned = false;
    }

    public boolean passwordsMatch(String passwordGiven) {
        boolean returned = false;
        String passwordEncrypted = getEncryptedPassword();
        // If the password stored & given is empty.
        if ((passwordEncrypted == null || passwordEncrypted.isEmpty())
                && (passwordGiven == null || passwordGiven.isEmpty())) {
            returned = true;
        }
        // Check to see if given password matches.
        if (!returned && passwordEncrypted != null) {
            // Attempt the Zomboid MD5 encryption first.
            String passwordGivenEncrypted = MD5.encrypt(passwordGiven);
            returned = passwordEncrypted.equals(passwordGivenEncrypted);
        }
        return returned;
    }

    /**
     * Creates a metadata BasicDBObject.
     *
     * @return Returns the new metadata BasicDBObject.
     */
    private DBObject createMetadataDocument() {
        Map<String, String> mapMetadata = getMetadata();
        DBObject metadata = new BasicDBObject();
        for (String key : mapMetadata.keySet()) {
            metadata.put(key, mapMetadata.get(key));
        }
        return metadata;
    }

    private void loadMetadata(DBObject object) {
        Map<String, String> mapMetadata = new HashMap<>();
        Object oMetadata = object.get("metadata");
        if (oMetadata != null) {
            DBObject dbMetadata = (DBObject) oMetadata;
            for (String key : dbMetadata.keySet()) {
                mapMetadata.put(key, dbMetadata.get(key).toString());
            }
        }
        setMetadata(mapMetadata);
    }

    private void saveMetadata() {
        DBObject object = new BasicDBObject(getFieldId(), getFieldValue());
        object.put("metadata", createMetadataDocument());
        getCollection().upsert(object, getFieldId(), this);
    }

    private String getNickname() {
        return this.nickname;
    }

    private void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * @return Returns the nickname if valid. If the nickname is not valid, use the
     * username instead.
     */
    public String getDisplayedName() {
        String preferred = getNickname();
        if (preferred == null || preferred.isEmpty()) {
            preferred = getUsername();
        }
        return preferred;
    }

    /**
     * Changes the username of the MongoPlayer
     *
     * @param newUsername The String user-name to set.
     */
    public void changeUsername(String newUsername) {
        setUsername(newUsername);
        save();
    }

    /**
     * Changes the nickname of the MongoPlayer
     *
     * @param newNickname The String nick-name to set.
     */
    public void changeNickname(String newNickname) {
        setNickname(newNickname);
        save();
    }

    /**
     * Sets a new password for the MongoPlayer.
     *
     * @param password The new Password.
     * @return Whether or not the action is successful.
     */
    public boolean setPassword(String password) {
        if (password == null || password.isEmpty()) {
            setEncryptedPassword("");
        }
        setEncryptedPassword(MD5.encrypt(password));
        return true;
    }

    public void setMetaData(String field, String value, boolean save) {
        this.mapMetadata.put(field, value);
        if (save) {
            saveMetadata();
        }
    }

    private Map<String, String> getMetadata() {
        return this.mapMetadata;
    }

    public String getMetaData(String field) {
        return this.mapMetadata.get(field);
    }

    private void setMetadata(Map<String, String> mapMetadata) {
        this.mapMetadata = mapMetadata;
    }

    public long getTimeConnected() {
        return this.timeConnected;
    }

    private void setTimeConnected(long timeConnected) {
        this.timeConnected = timeConnected;
    }

    public boolean isNewAccount() {
        return this.newAccount;
    }

    private void setNewAccount(boolean flag) {
        this.newAccount = flag;
    }

    public long getTimeCreated() {
        return this.timeCreated;
    }

    private void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public long getSteamOwnerId() {
        return this.steamIdOwner;
    }

    public void setSteamOwnerId(long steamIdOwner) {
        this.steamIdOwner = steamIdOwner;
    }

    public long getSteamId() {
        return this.steamId;
    }

    public void setSteamId(long steamId) {
        this.steamId = steamId;
    }

    public long getTimeConnectedLast() {
        return this.timeConnectedLast;
    }

    private void setTimeConnectedLast(long timeConnectedLast) {
        this.timeConnectedLast = timeConnectedLast;
    }

    public boolean isBanned() {
        return this.banned;
    }

    public void setBanned(boolean flag) {
        this.banned = flag;
    }

    public boolean isAdministrator() {
        return this.admin;
    }

    public void setAdministrator(boolean flag, boolean save) {
        this.admin = flag;
        if(save) {
            save();
        }
    }

    public String getEncryptedPassword() {
        return this.passwordEncrypted;
    }

    public void setEncryptedPassword(String passwordEncrypted) {
        this.passwordEncrypted = passwordEncrypted;
    }

    public String getUsername() {
        return this.username;
    }

    /**
     * (Private method)
     * <p>
     * Sets the username for the player.
     *
     * @param username The String user-name to set.
     */
    private void setUsername(String username) {
        this.username = username;
    }

    public boolean hasPassword() {
        String password = getEncryptedPassword();
        return password != null && !password.isEmpty();
    }

    public void setLastConnection(long time) {
        this.timeConnectedLast = time;
    }
}