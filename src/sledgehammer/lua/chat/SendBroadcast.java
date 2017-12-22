package sledgehammer.lua.chat;

import sledgehammer.lua.Send;

/**
 * Class designed to package Broadcast LuaObjects.
 * 
 * @author Jab
 * 
 */
public class SendBroadcast extends Send {

	/**
	 * The Broadcast LuaObject being packaged.
	 */
	private Broadcast broadcast;

	/**
	 * Main constructor.
	 * 
	 * @param broadcast
	 */
	public SendBroadcast(Broadcast broadcast) {
		super("core", "sendBroadcast");

		// Set variable(s).
		setBroadcast(broadcast);
	}

	@Override
	public void onExport() {
		set("broadcast", getBroadcast());
	}

	/**
	 * Returns the Broadcast LuaObject packaged.
	 * 
	 * @return
	 */
	public Broadcast getBroadcast() {
		return this.broadcast;
	}

	/**
	 * Sets the Broadcast LuaObject to be packaged.
	 * 
	 * @param broadcast
	 */
	public void setBroadcast(Broadcast broadcast) {
		this.broadcast = broadcast;
	}
}
