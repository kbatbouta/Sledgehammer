package sledgehammer.lua.chat;

import sledgehammer.lua.Send;

/**
 * TODO: Document.
 * @author Jab
 *
 */
public class SendChatMessage extends Send {

	private ChatMessage message;
	
	public SendChatMessage() {
		super("core.chat", "sendChatMessage");
	}
	
	public void setChatMessage(ChatMessage message) {
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
