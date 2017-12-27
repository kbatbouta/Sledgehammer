package sledgehammer.lua.chat.send;

import java.util.UUID;

import sledgehammer.lua.Send;

// @formatter:off
/**
 * Send Class for ChatMessages being renamed.
 * 
 * Exports a LuaTable:
 * {
 * 	 - "channel_id": (String) The UUID String of the ChatChannel.
 *   -   "name_old": (String) The old ChatChannel name.
 *   -   "name_new": (String) The new ChatChannel name.
 * }
 * 
 * @author Jab
 */
// @formatter:on
public class SendChatChannelRename extends Send {

	/** The <UUID> channelId of the ChatChannel. */
	private UUID channelId;
	/** The <String> old name of the ChatChannel. */
	private String nameOld;
	/** The <String> new name of the ChatChannel. */
	private String nameNew;

	/**
	 * Public constructor.
	 */
	public SendChatChannelRename() {
		super("core.chat", "sendChatChannelRename");
	}

	@Override
	public void onExport() {
		// @formatter:off
		set("channel_id", getChannelId().toString());
		set("name_old"  , getOldName());
		set("name_new"  , getNewName());
		// @formatter:on
	}

	/**
	 * @return Returns the <UUID> of the ChatChannel to send.
	 */
	public UUID getChannelId() {
		return this.channelId;
	}

	/**
	 * Sets the <UUID> of the ChatChannel to send.
	 * 
	 * @param channelId
	 *            The <UUID> of the ChatChannel.
	 */
	public void setChannelId(UUID channelId) {
		this.channelId = channelId;
	}

	/**
	 * @return Returns the <String> new name of the ChatChannel.
	 */
	public String getNewName() {
		return this.nameNew;
	}

	/**
	 * Sets the <String> new name of the ChatChannel being sent.
	 * 
	 * @param nameNew
	 *            The <String> new name of the ChatChannel.
	 */
	public void setNewName(String nameNew) {
		this.nameNew = nameNew;
	}

	/**
	 * @return Returns the <String> old name of the ChatChannel.
	 */
	public String getOldName() {
		return this.nameOld;
	}

	/**
	 * Sets the <String> old name of the ChatChannel being sent.
	 * 
	 * @param nameOld
	 *            The <String> old name of the ChatChannel.
	 */
	public void setOldName(String nameOld) {
		this.nameOld = nameOld;
	}
}