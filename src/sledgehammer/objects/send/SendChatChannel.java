package sledgehammer.objects.send;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.object.LuaTable;
import sledgehammer.objects.chat.ChatChannel;

/**
 * TODO: Document.
 * @author Jab
 *
 */
public class SendChatChannel extends Send {
	
	private ChatChannel channel;
	
	public SendChatChannel(ChatChannel channel) {
		super("core", "sendChatChannel");
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
