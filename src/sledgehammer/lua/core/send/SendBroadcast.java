package sledgehammer.lua.core.send;

import sledgehammer.lua.Send;
import sledgehammer.lua.core.Broadcast;

// @formatter:off
/**
 * Send Class for sending Broadcasts.
 *
 * Exports a LuaTable:
 * {
 *   - "broadcast": (LuaTable) The Broadcast being sent.
 * }
 * 
 * @author Jab
 */
// @formatter:on
public class SendBroadcast extends Send {

	/** The <Broadcast> LuaObject being packaged. */
	private Broadcast broadcast;

	/**
	 * Main constructor.
	 */
	public SendBroadcast() {
		super("core", "sendBroadcast");
	}

	@Override
	public void onExport() {
		set("broadcast", getBroadcast());
	}

	/**
	 * @return Returns the <Broadcast> being sent.
	 */
	public Broadcast getBroadcast() {
		return this.broadcast;
	}

	/**
	 * Sets the <Broadcast> being sent.
	 * 
	 * @param broadcast
	 *            The <Broadcast> to set.
	 */
	public void setBroadcast(Broadcast broadcast) {
		this.broadcast = broadcast;
	}
}