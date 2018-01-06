package sledgehammer.lua.chat.send;

import sledgehammer.lua.Send;
import sledgehammer.lua.chat.ChatHistory;

public class SendChatHistory extends Send {

    private ChatHistory chatHistory;

    public SendChatHistory(ChatHistory chatHistory) {
        super("core.chat", "sendChatHistory");
        setChatHistory(chatHistory);
    }

    @Override
    public void onExport() {
        set("history", getChatHistory());
    }

    public ChatHistory getChatHistory() {
        return this.chatHistory;
    }

    public void setChatHistory(ChatHistory chatHistory) {
        this.chatHistory = chatHistory;
    }
}
