package sledgehammer.lua.chat.send;

import java.util.Collection;
import java.util.UUID;

import sledgehammer.lua.LuaArray;
import sledgehammer.lua.Send;
import sledgehammer.lua.chat.ChatMessage;

// @formatter:off
/**
 * Send Class for ChatMessages.
 * 
 * Exports a LuaTable:
 * {
 *   - "channel": (String) The Unique ID of the Channel.
 *   - "message": (LuaTable) ChatMessage Object.
 * }
 *
 * TODO: change "channel" to "channel_id"
 * 
 * TODO: Document.
 * 
 * @author Jab
 */
// @formatter:on
public class SendChatMessages extends Send {

    /**
     * The LuaArray of ChatMessages being sent.
     */
    private LuaArray<ChatMessage> listChatMessages;
    /**
     * The Unique ID of the ChatChannel using this sender.
     */
    private UUID channelId;

    /**
     * Main constructor.
     */
    public SendChatMessages(UUID channelId) {
        super("core.chat", "sendChatMessages");
        setChannelId(channelId);
        listChatMessages = new LuaArray<>();
    }

    @Override
    public void onExport() {
        set("channel_id", getChannelId().toString());
        set("messages", getChatMessages());
    }

    public UUID getChannelId() {
        return this.channelId;
    }

    private void setChannelId(UUID channelId) {
        this.channelId = channelId;
    }

    public LuaArray<ChatMessage> getChatMessages() {
        return this.listChatMessages;
    }

    public void setChatMessages(LuaArray<ChatMessage> listChatMessages) {
        if (listChatMessages == null) {
            throw new IllegalArgumentException("LuaArray<ChatMessage> provided is null.");
        }
        this.listChatMessages = listChatMessages;
    }

    public void addChatMessages(Collection<ChatMessage> collectionChatMessages) {
        for (ChatMessage chatMessage : collectionChatMessages) {
            addChatMessage(chatMessage);
        }
    }

    public void addChatMessage(ChatMessage chatMessage) {
        if (!listChatMessages.contains(chatMessage)) {
            listChatMessages.add(chatMessage);
        }
    }

    public void removeChatMessage(ChatMessage chatMessage) {
        if (listChatMessages.contains(chatMessage)) {
            listChatMessages.remove(chatMessage);
        }
    }

    public void clearChatMessages() {
        listChatMessages.clear();
    }
}