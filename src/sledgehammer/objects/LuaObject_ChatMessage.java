package sledgehammer.objects;

import java.util.Map;

import se.krka.kahlua.vm.KahluaTable;

public class LuaObject_ChatMessage extends LuaObject {

	private long messageID;
	private String channel;
	private String message;
	
	/**
	 * Constructor for creating a new ChatMessage Object.
	 */
	public LuaObject_ChatMessage(String message) {
		super("ChatMessage");
		
		// Sets the message.
		setMessage(message);
		
		// Create long as both timestamp and ID.
		setID(generateMessageID());
	}
	
	/**
	 * Constructor for loading an existing ChatMessage Object.
	 * @param table
	 */
	public LuaObject_ChatMessage(KahluaTable table) {
		super("ChatMessage", table);
	}
	
	public String getChannel() {
		return this.channel;
	}
	
	public void setChannel(String channel) {
		channel = channel.toLowerCase().trim();
		
		if(!this.channel.equals(channel)) {			
			this.channel = channel;
			set("channel", channel);
		}
	}
	
	public long getID() {
		return this.messageID;
	}
	
	private void setID(long id) {
		if(this.messageID != id) {
			this.messageID = id;
			set("id", id);
		}
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public void setMessage(String message) {
		if(!message.equals(this.message)) {
			this.message = message;
			set("message", message);
		}
	}
	
	public static long generateMessageID() {
		return System.currentTimeMillis();
	}

	@Override
	public void construct(Map<String, Object> definitions) {
		definitions.put("id", getID());
		definitions.put("channel", getChannel());
		definitions.put("message", getMessage());
	}

	@Override
	public void load(KahluaTable table) {
		setID(Long.parseLong(table.rawget("id").toString()));
		setChannel(table.rawget("channel").toString());
		setMessage(table.rawget("message").toString());
	}

}
