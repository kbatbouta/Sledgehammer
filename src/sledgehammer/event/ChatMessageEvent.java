package sledgehammer.event;

import sledgehammer.objects.chat.ChatMessage;

public class ChatMessageEvent extends Event {

	public static final String ID = "ChatMessageEvent";

	private ChatMessage message;
	
	public ChatMessageEvent(ChatMessage message) {
		this.message = message;
	}
	
	public ChatMessage getMessage() {
		return this.message;
	}
	
	public String getChannelName() {
		return getMessage().getChannel();
	}
	
	public String getLogMessage() { return null; }
	public String getID() { return ID; }

}
