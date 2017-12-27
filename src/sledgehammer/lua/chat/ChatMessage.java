package sledgehammer.lua.chat;

import java.util.UUID;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.database.MongoCollection;
import sledgehammer.database.module.chat.MongoChatMessage;
import sledgehammer.lua.MongoLuaObject;
import sledgehammer.lua.core.Player;

/**
 * TODO: Document.
 * 
 * @author Jab
 */
public class ChatMessage extends MongoLuaObject<MongoChatMessage> {

	// @formatter:off
	public static final String ORIGIN_CLIENT  = "client" ;
	public static final String ORIGIN_SERVER  = "server" ;
	public static final String ORIGIN_MODULE  = "module" ;
	public static final String ORIGIN_CORE    = "core"   ;
	public static final String ORIGIN_DISCORD = "discord";
	// @formatter:on

	private Player player;

	public ChatMessage(MongoChatMessage mongoDocument) {
		super(mongoDocument, "ChatMessage");
	}

	public ChatMessage(MongoChatMessage mongoDocument, KahluaTable table) {
		super(mongoDocument, "ChatMessage");
		onLoad(table);
	}

	public ChatMessage(MongoChatMessage mongoDocument, UUID channelId, UUID playerId, UUID editorId, UUID deleterId,
			String origin, String playerName, String message, String messageOriginal, String timestampPrinted,
			long timestamp, long timestampModified, int type) {
		super(mongoDocument, "ChatMessage");
		setChannelId(channelId, false);
		setPlayerId(playerId, false);
		setEditorId(editorId, false);
		setDeleterId(deleterId, false);
		setOrigin(origin, false);
		setCachedPlayerName(playerName, false);
		setMessage(message, false);
		setOriginalMessage(messageOriginal, false);
		setPrintedTimestamp(timestampPrinted, false);
		setTimestamp(timestamp, false);
		setModifiedTimestamp(timestampModified, false);
		setType(type, false);
	}

	@Override
	public void onLoad(KahluaTable table) {
		// Set the ID if it exists.
		UUID uniqueId = UUID.fromString(table.rawget("id").toString());
		if (uniqueId != null) {
			setUniqueId(uniqueId, false);
		}
		setChannelId(UUID.fromString(table.rawget("channel_id").toString()), false);
		setPlayerId(UUID.fromString(table.rawget("player_id").toString()), false);
		setEditorId(UUID.fromString(table.rawget("editor_id").toString()), false);
		setDeleterId(UUID.fromString(table.rawget("deleter_id").toString()), false);
		setOrigin(table.rawget("origin").toString(), false);
		setCachedPlayerName(table.rawget("player_name").toString(), false);
		setMessage(table.rawget("message").toString(), false);
		setOriginalMessage(table.rawget("message_original").toString(), false);
		// Check to see if the printed timestamp is given. If not, the method
		// with a null String passed will generate one instead.
		String timestampPrinted = null;
		Object oTimestampPrinted = table.rawget("timestamp_printed");
		if (oTimestampPrinted != null) {
			timestampPrinted = oTimestampPrinted.toString();
		}
		setPrintedTimestamp(timestampPrinted, false);
		// Check to see if a timestamp is given. If not, create one.
		Object oTimestamp = table.rawget("timestamp");
		if (oTimestamp != null) {
			long timestamp = ((Double) Double.parseDouble(oTimestamp.toString())).longValue();
			setTimestamp(timestamp, false);
		} else {
			createTimestamp(false);
		}
		// Check to see if a timestamp is given. If the timestamp is 0, assign one.
		Object oTimestampModified = table.rawget("timestamp_modified");
		if (oTimestampModified != null) {
			long timestampModified = ((Double) Double.parseDouble(oTimestamp.toString())).longValue();
			if (timestampModified > 0) {
				setModifiedTimestamp(timestampModified, false);
			} else if (timestampModified == 0) {
				createModifiedTimestamp(false);
			}
		}
		setType((Integer) table.rawget("message_type"), false);
	}

	@Override
	public void onExport() {
		// @formatter:off
		set("id"                , getUniqueId().toString() );
		set("channel_id"        , getChannelId().toString());
		set("player_id"         , getPlayerId().toString() );
		set("editor_id"         , getEditorId().toString() );
		set("deleter_id"        , getDeleterId().toString());
		set("origin"            , getOrigin()              );
		set("player_name"       , getCachedPlayerName()    );
		set("message"           , getMessage()             );
		set("message_original"  , getOriginalMessage()     );
		set("timestamp"         , getTimestamp()           );
		set("timestamp_modified", getModifiedTimestamp()   );
		set("timestamp_printed" , getPrintedTimestamp()    );
		set("message_type"      , getType()                );
		set("edited"            , isEdited()               );
		set("deleted"           , isDeleted()              );
		// @formatter:on
	}

	@Override
	public boolean equals(Object other) {
		boolean returned = false;
		if (other instanceof ChatMessage) {
			returned = ((ChatMessage) other).getUniqueId().equals(getUniqueId());
		}
		return returned;
	}

	/**
	 * Deep-Clones a Chat-Message, creating a new <MongoChatMessage> document,
	 * however it is not saved to the MongoDB database.
	 */
	public ChatMessage clone() {
		MongoCollection collectionChatMessages = getMongoDocument().getCollection();
		MongoChatMessage mongoChatMessage = new MongoChatMessage(collectionChatMessages);
		UUID channelId = getChannelId();
		UUID playerId = getPlayerId();
		UUID editorId = getEditorId();
		UUID deleterId = getDeleterId();
		String origin = getOrigin();
		String playerName = getCachedPlayerName();
		String message = getMessage();
		String messageOriginal = getOriginalMessage();
		String timestampPrinted = getPrintedTimestamp();
		long timestamp = getTimestamp();
		long timestampModified = getModifiedTimestamp();
		int type = getType();
		ChatMessage chatMessageClone = new ChatMessage(mongoChatMessage, channelId, playerId, editorId, deleterId,
				origin, playerName, message, messageOriginal, timestampPrinted, timestamp, timestampModified, type);
		return chatMessageClone;
	}

	public Player getPlayer() {
		Player returned = null;
		if (player == null) {
			UUID playerId = getPlayerId();
			if (playerId != null) {
				player = SledgeHammer.instance.getPlayer(playerId);
			}
		}
		returned = player;
		return returned;
	}

	public UUID getPlayerId() {
		return getMongoDocument().getPlayerId();
	}

	public void setPlayerId(UUID playerId, boolean save) {
		getMongoDocument().setPlayerId(playerId, save);
		if (playerId != null) {
			player = SledgeHammer.instance.getPlayer(playerId);
		}
	}

	public boolean isDeleted() {
		return getMongoDocument().isDeleted();
	}

	public void setDeleted(boolean deleted, boolean save) {
		getMongoDocument().setDeleted(deleted, save);
	}

	public boolean isEdited() {
		return getMongoDocument().isEdited();
	}

	public void setEdited(boolean edited, boolean save) {
		getMongoDocument().setEdited(edited, save);
	}

	public int getType() {
		return getMongoDocument().getType();
	}

	public void setType(int type, boolean save) {
		getMongoDocument().setType(type, save);
	}

	public long getModifiedTimestamp() {
		return getMongoDocument().getModifiedTimestamp();
	}

	public void setModifiedTimestamp(long timestampModified, boolean save) {
		getMongoDocument().setModifiedTimestamp(timestampModified, save);
	}

	public void createModifiedTimestamp(boolean save) {
		getMongoDocument().setModifiedTimestamp(System.currentTimeMillis(), save);
	}

	public long getTimestamp() {
		return getMongoDocument().getTimestamp();
	}

	public void setTimestamp(long timestamp, boolean save) {
		getMongoDocument().setTimestamp(timestamp, save);
	}

	public void createTimestamp(boolean save) {
		getMongoDocument().setTimestamp(System.currentTimeMillis(), save);
	}

	public String getPrintedTimestamp() {
		return getMongoDocument().getPrintedTimestamp();
	}

	public void setPrintedTimestamp(String timestampPrinted, boolean save) {
		if (timestampPrinted == null) {
			setPrintedTimestamp(save);
		}
		getMongoDocument().setPrintedTimestamp(timestampPrinted, save);
	}

	public void setPrintedTimestamp(boolean save) {
		getMongoDocument().setPrintedTimestamp(save);
	}

	public String getOriginalMessage() {
		return getMongoDocument().getOriginalMessage();
	}

	public void setOriginalMessage(String messageOriginal, boolean save) {
		getMongoDocument().setOriginalMessage(messageOriginal, save);
	}

	public String getMessage() {
		return getMongoDocument().getMessage();
	}

	public void setMessage(String message, boolean save) {
		getMongoDocument().setMessage(message, save);
	}

	public String getCachedPlayerName() {
		return getMongoDocument().getCachedPlayerName();
	}

	private void setCachedPlayerName(String playerName, boolean save) {
		getMongoDocument().setCachedPlayerName(playerName, save);
	}

	public String getOrigin() {
		return getMongoDocument().getOrigin();
	}

	public void setOrigin(String origin, boolean save) {
		getMongoDocument().setOrigin(origin, save);
	}

	public UUID getDeleterId() {
		return getMongoDocument().getDeleterId();
	}

	private void setDeleterId(UUID deleterId, boolean save) {
		getMongoDocument().setDeleterId(deleterId, save);
	}

	public UUID getEditorId() {
		return getMongoDocument().getEditorId();
	}

	private void setEditorId(UUID editorId, boolean save) {
		getMongoDocument().setEditorId(editorId, save);
	}

	public UUID getChannelId() {
		return getMongoDocument().getChannelId();
	}

	public void setChannelId(UUID channelId, boolean save) {
		getMongoDocument().setChannelId(channelId, save);
	}

	public UUID getUniqueId() {
		return getMongoDocument().getUniqueId();
	}

	private void setUniqueId(UUID uniqueId, boolean save) {
		getMongoDocument().setUniqueId(uniqueId, save);
	}

	public void save() {
		getMongoDocument().save();
	}

	public void delete() {
		getMongoDocument().delete();
	}
}