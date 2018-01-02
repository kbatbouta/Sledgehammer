package sledgehammer.lua.chat.request;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.lua.LuaArray;
import sledgehammer.lua.LuaTable;
import sledgehammer.lua.chat.ChatChannel;

/**
 * LuaTable designed to handle packaging of requests for <ChatChannel>
 * <LuaTable>s.
 * 
 * @author Jab
 */
public class RequestChatChannels extends LuaTable {

	/** The <LuaArray> to store the channels. */
	private LuaArray<ChatChannel> channels;

	/**
	 * New constructor.
	 */
	public RequestChatChannels() {
		super("RequestChatChannels");
		channels = new LuaArray<>();
	}

	/**
	 * Lua load constructor.
	 * 
	 * @param table
	 *            The <KahluaTable> storing the data.
	 */
	public RequestChatChannels(KahluaTable table) {
		super("RequestChatChannels");
		onLoad(table);
	}

	@Override
	public void onLoad(KahluaTable table) {
		channels = new LuaArray<>((KahluaTable) table.rawget("channels"));
	}

	@Override
	public void onExport() {
		// @formatter:off
		set("length"  , channels.size());
		set("channels", channels       );
		// @formatter:on
	}

	/**
	 * Adds a given <ChatChannel> to the list.
	 * 
	 * @param channel
	 *            The <ChatChannel> to add.
	 */
	public void addChannel(ChatChannel channel) {
		channels.add(channel);
	}
}