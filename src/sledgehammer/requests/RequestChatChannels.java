package sledgehammer.requests;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.object.LuaArray;
import sledgehammer.object.LuaTable;
import sledgehammer.objects.chat.ChatChannel;

/**
 * TODO: Documentation.
 * @author Jab
 */
public class RequestChatChannels extends LuaTable {

	private LuaArray<ChatChannel> channels;
	
	public RequestChatChannels() {
		super("RequestChatChannels");
		channels = new LuaArray<>();
	}
	
	public void addChannel(ChatChannel channel) {
		channels.add(channel);
	}
	
	public void onLoad(KahluaTable table) {
		channels = new LuaArray<>((KahluaTable) table.rawget("channels"));
	}

	public void onExport() {
		set("length", channels.size());
		set("channels", channels);
	}

}
