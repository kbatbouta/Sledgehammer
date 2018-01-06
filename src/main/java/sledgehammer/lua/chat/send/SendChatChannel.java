package sledgehammer.lua.chat.send;

import sledgehammer.lua.Send;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.chat.ChatHistory;

/**
 * Send Class for ChatChannels.
 *
 * @author Jab
 */
public class SendChatChannel extends Send {

    /**
     * The ChatChannel being sent.
     */
    private ChatChannel chatChannel;

    /**
     * Main constructor.
     *
     * @param chatChannel The ChatChannel to send.
     */
    public SendChatChannel(ChatChannel chatChannel) {
        super("core.chat", "sendChatChannel");
        setChatChannel(chatChannel);
    }

    @Override
    public void onExport() {
        set("channel", getChatChannel());
        set("history", getChatHistory());
    }

    /**
     * @return Returns the ChatChannel being sent.
     */
    public ChatChannel getChatChannel() {
        return this.chatChannel;
    }

    /**
     * @return Returns the ChatHistory assigned to the set ChatChannel.
     */
    public ChatHistory getChatHistory() {
        return getChatChannel().getHistory();
    }

    /**
     * Sets the ChatChannel to be sent.
     *
     * @param chatChannel The ChatChannel being sent.
     */
    private void setChatChannel(ChatChannel chatChannel) {
        this.chatChannel = chatChannel;
    }
}
