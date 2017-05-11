package sledgehammer.objects.chat;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.object.LuaTable;

public class ChatMessage extends LuaTable {

	private long messageID;
	private String channel;
	private String message;
	
	/**
	 * Constructor for creating a new ChatMessage Object.
	 */
	public ChatMessage(String message) {
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
	public ChatMessage(KahluaTable table) {
		super("ChatMessage", table);
	}
	
	public String getChannel() {
		return this.channel;
	}
	
	public void setChannel(String channel) {
		channel = channel.toLowerCase().trim();
		
		if(!this.channel.equals(channel)) {			
			this.channel = channel;
		}
	}
	
	public long getID() {
		return this.messageID;
	}
	
	private void setID(long id) {
		if(this.messageID != id) {
			this.messageID = id;
		}
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public void setMessage(String message) {
		if(!message.equals(this.message)) {
			this.message = message;
		}
	}
	
	public static long generateMessageID() {
		return System.currentTimeMillis();
	}

	@Override
	public void onLoad(KahluaTable table) {
		setID(Long.parseLong(table.rawget("id").toString()));
		setChannel(table.rawget("channel").toString());
		setMessage(table.rawget("message").toString());
	}

	@Override
	public void onExport() {
		set("id", getID());
		set("channel", getChannel());
		set("message", getMessage());
	}

}
