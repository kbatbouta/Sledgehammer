package sledgehammer.objects.chat;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.modules.core.ModuleChat;
import sledgehammer.object.LuaTable;
import zombie.Lua.LuaManager;

/**
 * TODO: Document.
 * @author Jab
 *
 */
public class ChatMessage extends LuaTable {

	public static final String ORIGIN_CLIENT = "client";
	public static final String ORIGIN_SERVER = "server";
	public static final String ORIGIN_MODULE = "module";
	public static final String ORIGIN_CORE   = "core";
	
	private String origin;
	private String channel;
	private String message;
	private String messageOriginal;
	private long messageID = -1L;
	private long modifiedTimestamp = -1L;
	private int editorID = -1;
	private int deleterID = -1;
	private boolean edited = false;
	private boolean deleted = false;
	private String time;
	
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
	}
	
	public ChatMessage(long id, String channel, String message, String messageOriginal,
			boolean edited, int editorID, boolean deleted, int deleterID, long modifiedTimestamp, String time) {
		super("ChatMessage");
		this.messageID         = id;
		this.channel           = channel;
		this.message           = message;
		this.messageOriginal   = messageOriginal;
		this.modifiedTimestamp = modifiedTimestamp;
		this.editorID          = editorID;
		this.deleterID         = deleterID;
		this.edited            = edited;
		this.deleted           = deleted;
		this.time              = time;
	}
	
	/**
	 * Constructor for loading an existing ChatMessage Object.
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
		setEditorID((int) (Double.parseDouble(table.rawget("editorID").toString())));
	
		setDeleted(Boolean.parseBoolean(table.rawget("deleted").toString()));
		setDeleterID((int) (Double.parseDouble(table.rawget("deleterID").toString())));
		Double d = Double.parseDouble(table.rawget("modifiedTimestamp").toString());
		setModifiedTimestamp(d.longValue());
		
		// If origin is set.
		Object o = table.rawget("origin");
		if(o != null) {			
			setOrigin(o.toString());
		}
	}

	private void setDeleted(boolean flag) {
		this.deleted = flag;
	}

	private void setDeleterID(int id) {
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

	private void setEditorID(int id) {
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
		channel = channel.toLowerCase().trim();
		
		if(this.channel == null || !this.channel.equals(channel)) {			
			this.channel = channel;
		}
	}
	
	public long getMessageID() {
		return this.messageID;
	}
	
	public void setID(long id) {
		if(this.messageID != id) {
			this.messageID = id;
		}
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public void setMessage(String message) {
		if(this.message == null || !this.message.equals(message)) {
			this.message = message;
		}
	}

	public long getModifiedTimestamp() {
		return this.modifiedTimestamp;
	}

	public int getDeleterID() {
		return this.deleterID;
	}

	public boolean isDeleted() {
		return this.deleted;
	}

	public int getEditorID() {
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
}
