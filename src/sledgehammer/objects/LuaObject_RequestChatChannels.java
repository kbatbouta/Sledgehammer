package sledgehammer.objects;

import java.util.Map;

import se.krka.kahlua.vm.KahluaTable;

public class LuaObject_RequestChatChannels extends LuaObject {

	private KahluaTable channels;
	
	private int size = 0;
	
	public LuaObject_RequestChatChannels() {
		super("RequestChatChannels");
		channels = newTable();
	}
	
	public void addChannel(String channel) {
		channels.rawset(size++, channel);
		set("channels", channels);
	}

	@Override
	public void construct(Map<String, Object> definitions) {
		definitions.put("channels", channels);
	}

	@Override
	public void load(KahluaTable table) {
		channels = (KahluaTable) table.rawget("channels");
	}

}
