package sledgehammer.lua.chat;

import sledgehammer.lua.Send;

/**
 * TODO: Document.
 * 
 * @author Jab
 */
public class SendRemoveChatChannel extends Send {
	
	private ChatChannel channel;
	
	public SendRemoveChatChannel(ChatChannel channel) {
		super("core.chat", "removeChatChannel");
		this.channel = channel;
	}
	
	@Override
	public void onExport() {
		set("channel", getChatChannel());
	}

	public ChatChannel getChatChannel() {
		return this.channel;
	}
}
