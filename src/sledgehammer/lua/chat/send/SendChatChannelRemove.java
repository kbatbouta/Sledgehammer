package sledgehammer.lua.chat.send;

import java.util.UUID;

import sledgehammer.lua.Send;

// @formatter:off
/**
 * Send Class for ChatChannels being removed.
 * 
 * Exports a LuaTable:
 * {
 *   - "channel_id": (String) ChatChannel UUID.
 * }
 * 
 * @author Jab
 */
// @formatter:on
public class SendChatChannelRemove extends Send {

	/** The <UUID> of the <ChatChannel> being removed. */
	private UUID channelId;

	/**
	 * Main constructor.
	 */
	public SendChatChannelRemove() {
		super("core.chat", "sendChatChannelRemove");
	}

	@Override
	public void onExport() {
		set("channel_id", getChannelId().toString());
	}

	/**
	 * @return Returns the <UUID> of the <ChatChannel> being removed.
	 */
	public UUID getChannelId() {
		return this.channelId;
	}

	/**
	 * Sets the <UUID> of the <ChatChannel> being removed.
	 * 
	 * @param channelId
	 *            The <UUID> of the <ChatChannel>.
	 */
	public void setChannelId(UUID channelId) {
		this.channelId = channelId;
	}
}