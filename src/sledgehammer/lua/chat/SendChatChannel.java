package sledgehammer.lua.chat;

import sledgehammer.lua.Send;

/**
 * TODO: Document.
 * 
 * @author Jab
 *
 */
public class SendChatChannel extends Send {

	private ChatChannel channel;

	public SendChatChannel(ChatChannel channel) {
		super("core.chat", "sendChatChannel");
		this.channel = channel;
	}

	@Override
	public void onExport() {
		set("channel", channel);
	}

	public ChatChannel getChatChannel() {
		return this.channel;
	}
}
