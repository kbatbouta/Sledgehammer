package sledgehammer.lua.chat;

import sledgehammer.lua.Send;

/**
 * TODO: Document.
 * 
 * @author Jab
 *
 */
public class SendChatMessagePlayer extends Send {
	private ChatMessagePlayer message;

	public SendChatMessagePlayer() {
		super("core.chat", "sendChatMessagePlayer");
	}

	public void setChatMessage(ChatMessagePlayer message) {
		this.message = message;
	}

	public ChatMessage getChatMessage() {
		return this.message;
	}

	public void onExport() {
		ChatMessage message = getChatMessage();
		set("message", message);
		set("channel", message.getChannel());
	}
}
