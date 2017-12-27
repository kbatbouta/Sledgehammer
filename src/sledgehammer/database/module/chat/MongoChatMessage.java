package sledgehammer.database.module.chat;

import java.util.UUID;

import com.mongodb.DBObject;

import sledgehammer.database.MongoCollection;
import sledgehammer.database.document.MongoUniqueDocument;
import zombie.Lua.LuaManager;

/**
 * TODO: Document
 * 
 * @author Jab
 *
 */
public class MongoChatMessage extends MongoUniqueDocument {

	private UUID uniqueId;
	private UUID channelId;
	private UUID playerId;
	private UUID editorId = null;
	private UUID deleterId = null;
	private String origin;
	private String playerName;
	private String message;
	private String messageOriginal;
	private String timestampPrinted;
	private long timestamp;
	private long timestampModified;
	private int type;
	private boolean edited = false;
	private boolean deleted = false;

	/**
	 * New constructor.
	 * 
	 * @param collection
	 */
	public MongoChatMessage(MongoCollection collection) {
		super(collection);
	}

	/**
	 * MongoDB load constructor.
	 * 
	 * @param collection
	 * @param object
	 */
	public MongoChatMessage(MongoCollection collection, DBObject object) {
		super(collection, object);
		onLoad(object);
	}

	@Override
	public void onLoad(DBObject object) {
		setChannelId((UUID) object.get("channel_id"), false);
		setPlayerId((UUID) object.get("player_id"), false);
		setEditorId((UUID) object.get("editor_id"), false);
		setDeleterId((UUID) object.get("deleter_id"), false);
		setOrigin((String) object.get("origin"), false);
		setCachedPlayerName((String) object.get("player_name"), false);
		setMessage((String) object.get("message"), false);
		setOriginalMessage((String) object.get("message_original"), false);
		setTimestamp((long) object.get("timestamp"), false);
		setModifiedTimestamp((long) object.get("timestamp_modified"), false);
		setPrintedTimestamp((String) object.get("timestamp_printed"), false);
		setType((int) object.get("type"), false);
		setEdited((Boolean) object.get("edited"), false);
		setDeleted((Boolean) object.get("deleted"), false);
	}

	@Override
	public void onSave(DBObject object) {
		// @formatter:off
		object.put("channel_id"        , getChannelId()        );
		object.put("player_id"         , getPlayerId()         );
		object.put("editor_id"         , getEditorId()         );
		object.put("deleter_id"        , getDeleterId()        );
		object.put("origin"            , getOrigin()           );
		object.put("player_name"       , getCachedPlayerName() );
		object.put("message"           , getMessage()          );
		object.put("message_original"  , getOriginalMessage()  );
		object.put("timestamp"         , getTimestamp()        );
		object.put("timestamp_modified", getModifiedTimestamp());
		object.put("timestamp_printed" , getPrintedTimestamp() );
		object.put("type"              , getType()             );
		object.put("edited"            , isEdited()            );
		object.put("deleted"           , isDeleted()           );
		// @formatter:on
	}

	public int getType() {
		return this.type;
	}

	public void setType(int type, boolean save) {
		this.type = type;
		if (save) {
			save();
		}
	}

	public UUID getPlayerId() {
		return this.playerId;
	}

	public void setPlayerId(UUID playerId, boolean save) {
		this.playerId = playerId;
		if (save) {
			save();
		}
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(long timestamp, boolean save) {
		this.timestamp = timestamp;
		if (save) {
			save();
		}
	}

	public long getModifiedTimestamp() {
		return this.timestampModified;
	}

	public void setModifiedTimestamp(long timestampModified, boolean save) {
		this.timestampModified = timestampModified;
		if (save) {
			save();
		}
	}

	public String getPrintedTimestamp() {
		return this.timestampPrinted;
	}

	public void setPrintedTimestamp(String timestampPrinted, boolean save) {
		this.timestampPrinted = timestampPrinted;
		if (save) {
			save();
		}
	}

	public void setPrintedTimestamp(boolean save) {
		setPrintedTimestamp(LuaManager.getHourMinuteJava(), save);
	}

	public String getCachedPlayerName() {
		return this.playerName;
	}

	public void setCachedPlayerName(String playerName, boolean save) {
		this.playerName = playerName;
		if (save) {
			save();
		}
	}

	public UUID getDeleterId() {
		return this.deleterId;
	}

	public void setDeleterId(UUID deleterId, boolean save) {
		this.deleterId = deleterId;
		if (save) {
			save();
		}
	}

	public boolean isDeleted() {
		return this.deleted;
	}

	public void setDeleted(boolean deleted, boolean save) {
		this.deleted = deleted;
		if (save) {
			save();
		}
	}

	public UUID getEditorId() {
		return this.editorId;
	}

	public void setEditorId(UUID editorId, boolean save) {
		this.editorId = editorId;
		if (save) {
			save();
		}
	}

	public boolean isEdited() {
		return this.edited;
	}

	public void setEdited(boolean edited, boolean save) {
		this.edited = edited;
		if (save) {
			save();
		}
	}

	public String getOriginalMessage() {
		return this.messageOriginal;
	}

	public void setOriginalMessage(String messageOriginal, boolean save) {
		this.messageOriginal = messageOriginal;
		if (save) {
			save();
		}
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message, boolean save) {
		this.message = message;
		if (save) {
			save();
		}
	}

	public UUID getChannelId() {
		return this.channelId;
	}

	public void setChannelId(UUID channelId, boolean save) {
		this.channelId = channelId;
		if (save) {
			save();
		}
	}

	public String getOrigin() {
		return this.origin;
	}

	public void setOrigin(String origin, boolean save) {
		this.origin = origin;
		if (save) {
			save();
		}
	}

	public UUID getUniqueId() {
		return this.uniqueId;
	}

	public void setUniqueId(UUID uniqueId, boolean save) {
		this.uniqueId = uniqueId;
		if (save) {
			save();
		}
	}
}