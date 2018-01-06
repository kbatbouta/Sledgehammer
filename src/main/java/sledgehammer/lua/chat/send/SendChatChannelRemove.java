package sledgehammer.lua.chat.send;

import java.util.UUID;

import sledgehammer.lua.Send;

// @formatter:off
/**
 * Send Class for ChatChannels being removed.
 * 
 * Exports a LuaTable:
 * {
 *   - "channel_id": (String) ChatChannel Unique ID.
 * }
 * 
 * @author Jab
 */
// @formatter:on
public class SendChatChannelRemove extends Send {

    /**
     * The Unique ID of the ChatChannel being removed.
     */
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
     * @return Returns the Unique ID of the ChatChannel being removed.
     */
    public UUID getChannelId() {
        return this.channelId;
    }

    /**
     * Sets the Unique ID of the ChatChannel being removed.
     *
     * @param channelId The Unique ID of the ChatChannel.
     */
    public void setChannelId(UUID channelId) {
        this.channelId = channelId;
    }
}