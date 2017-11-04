package sledgehammer.objects.chat;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.database.MongoDatabase;
import sledgehammer.modules.core.ModuleChat;
import sledgehammer.object.LuaTable;
import zombie.Lua.LuaManager;

/**
 * TODO: Document.
 * 
 * @author Jab
 *
 */
public class ChatMessage extends LuaTable {

	public static final String ORIGIN_CLIENT = "client";
	public static final String ORIGIN_SERVER = "server";
	public static final String ORIGIN_MODULE = "module";
	public static final String ORIGIN_CORE = "core";

	private String time;
	private String origin;
	private String channel = "Global";
	private String message;
	private String messageOriginal;
	private String editorID = null;
	private String deleterID = null;
	private long messageID = -1L;
	private long modifiedTimestamp = -1L;
	private int type = 0;
	private boolean edited = false;
	private boolean deleted = false;

	public ChatMessage(DBObject object) {
		super("ChatMessage");
		load(object);
	}

	/**
	 * Constructor for creating a new ChatMessage Object.
	 */
	public ChatMessage(String message) {
		super("ChatMessage");
		// Sets the message.
		setMessage(message);
		setOriginalMessage(message);
		// Create long as both timestamp and ID.
		setID(generateMessageID());
		// If the time is not set manually, create it for now.
		setTime();
		// If the origin is not set manually, set it to server.
		setOrigin(ORIGIN_SERVER);
	}

	public ChatMessage(long id, String channel, String message, String messageOriginal, boolean edited, String editorID,
			boolean deleted, String deleterID, long modifiedTimestamp, String time) {
		super("ChatMessage");
		this.messageID = id;
		this.channel = channel;
		this.message = message;
		this.messageOriginal = messageOriginal;
		this.modifiedTimestamp = modifiedTimestamp;
		this.editorID = editorID;
		this.deleterID = deleterID;
		this.edited = edited;
		this.deleted = deleted;
		this.time = time;
	}

	/**
	 * Constructor for loading an existing ChatMessage Object.
	 * 
	 * @param table
	 */
	public ChatMessage(KahluaTable table) {
		super("ChatMessage", table);
	}

	@Override
	public void onLoad(KahluaTable table) {
		setID(new Double(table.rawget("messageID").toString()).longValue());
		setChannel(table.rawget("channel").toString());
		setMessage(table.rawget("message").toString());
		setOriginalMessage(table.rawget("messageOriginal").toString());
		setEdited(Boolean.parseBoolean(table.rawget("edited").toString()));
		setEditorID(table.rawget("editorID").toString());

		setDeleted(Boolean.parseBoolean(table.rawget("deleted").toString()));
		setDeleterID(table.rawget("deleterID").toString());
		Double d = Double.parseDouble(table.rawget("modifiedTimestamp").toString());
		setModifiedTimestamp(d.longValue());

		// If origin is set.
		Object o = table.rawget("origin");
		if (o != null) {
			setOrigin(o.toString());
		}
	}

	public void load(DBObject object) {
		setID(Long.parseLong(object.get("id").toString()));
		setChannel(object.get("channel").toString());
		setMessage(object.get("message").toString());
		setOriginalMessage(object.get("messageOriginal").toString());
		setModifiedTimestamp(Long.parseLong(object.get("modifiedTimestamp").toString()));
		setTime(object.get("time").toString());
		setOrigin(object.get("origin").toString());
		setType(Integer.parseInt(object.get("type").toString()));
		setEditorID(object.get("editorID").toString());
		setEdited(object.get("edited").toString().equals("1"));
		setDeleterID(object.get("deleterID").toString());
		setDeleted(object.get("deleted").toString().equals("1"));
	}

	public void save(DBCollection collection) {
		DBObject object = new BasicDBObject();
		onSave(object);
		MongoDatabase.upsert(collection, "id", object);
	}

	public void onSave(DBObject object) {
		// @formatter:off
		object.put("id"               , getMessageID() + "");
		object.put("channel"          , getChannel());
		object.put("message"          , getMessage());
		object.put("messageOriginal"  , getOriginalMessage());
		object.put("modifiedTimestamp", getModifiedTimestamp());
		object.put("time"             , getTime());
		object.put("origin"           , getOrigin());
		object.put("type"             , getType() + "");
		object.put("editor"           , isEdited()  ? "1" : "0");
		object.put("deleted"          , isDeleted() ? "1" : "0");
		object.put("editorID"         , getEditorID());
		object.put("deleterID"        , getDeleterID());
		// @formatter:on
	}

	private void setDeleted(boolean flag) {
		this.deleted = flag;
	}

	private void setDeleterID(String id) {
		this.deleterID = id;
	}

	private void setModifiedTimestamp(long value) {
		this.modifiedTimestamp = value;
	}

	protected void setOriginalMessage(String string) {
		this.messageOriginal = string;
	}

	private void setEdited(boolean flag) {
		this.edited = flag;
	}

	private void setEditorID(String id) {
		this.editorID = id;
	}

	@Override
	public void onExport() {
		set("messageID", "" + getMessageID());
		set("channel", getChannel());
		set("message", getMessage());
		set("messageOriginal", getOriginalMessage());
		set("edited", isEdited());
		set("editorID", getEditorID());
		set("deleted", isDeleted());
		set("deleterID", getDeleterID());
		set("modifiedTimestamp", getModifiedTimestamp());
		set("time", getTime());
		set("origin", getOrigin());
	}

	public String getChannel() {
		return this.channel;
	}

	public void setChannel(String channel) {
		if (channel != null) {
			channel = channel.toLowerCase().trim();
		}

		if (this.channel == null || !this.channel.equals(channel)) {
			this.channel = channel;
		}
	}

	public long getMessageID() {
		return this.messageID;
	}

	public void setID(long id) {
		if (this.messageID != id) {
			this.messageID = id;
		}
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		if (this.message == null || !this.message.equals(message)) {
			this.message = message;
		}
	}

	public long getModifiedTimestamp() {
		return this.modifiedTimestamp;
	}

	public String getDeleterID() {
		return this.deleterID;
	}

	public boolean isDeleted() {
		return this.deleted;
	}

	public String getEditorID() {
		return this.editorID;
	}

	public boolean isEdited() {
		return this.edited;
	}

	public String getOriginalMessage() {
		return this.messageOriginal;
	}

	public static long generateMessageID() {
		return System.currentTimeMillis();
	}

	public String getOrigin() {
		return this.origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getTime() {
		return this.time;
	}

	public void save() {
		ModuleChat module = (ModuleChat) SledgeHammer.instance.getModuleManager().getModuleByID(ModuleChat.ID);
		module.saveMessage(this);
	}

	public void setTime() {
		this.setTime(LuaManager.getHourMinuteJava());
	}

	public int getType() {
		return this.type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
