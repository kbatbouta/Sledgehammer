package sledgehammer.objects.chat;

import java.util.UUID;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.object.LuaTable;
import sledgehammer.objects.Player;

/**
 * TODO: Document.
 * @author Jab
 *
 */
public class Broadcast extends LuaTable {

	/**
	 * Author of the Broadcast. Set to admin by default.
	 */
	private Player author = SledgeHammer.getAdmin();
	
	/**
	 * The time of the broadcast.
	 */
	private String time;
	
	/**
	 * The message included in the Broadcast.
	 */
	private String message;
	
	/**
	 * Main constructor.
	 * @param message
	 */
	public Broadcast(String message) {
		super("Broadcast");
		setMessage(message);
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public Broadcast(KahluaTable table) {
		super("Broadcast", table);
	}
	
	public Player getAuthor() {
		return this.author;
	}
	
	public void setAuthor(Player player) {
		this.author = player;
	}

	public String getMessage() {
		return this.message;
	}
	
	public String getTime() {
		return this.time;
	}
	
	public void setTime(String time) {
		this.time = time;
	}
	
	public void onLoad(KahluaTable table) {
		Object _message = table.rawget("message");
		if(_message != null) {
			this.message = _message.toString();
		}
		
		Object _author = table.rawget("author");
		if(author != null) {
			if(author instanceof KahluaTable) {
				KahluaTable author = (KahluaTable) _author;
				String id = author.rawget("id").toString();
				setAuthor(SledgeHammer.instance.getPlayer(UUID.fromString(id)));
			}
		}
	}

	public void onExport() {
		set("message", getMessage());
		set("time", getTime());
		set("author", getAuthor());
	}


}
