package sledgehammer.lua.chat.request;

import sledgehammer.lua.LuaArray;
import sledgehammer.lua.LuaTable;
import sledgehammer.lua.chat.ChatHistory;

/**
 * TODO: Document.
 * 
 * @author Jab
 */
public class RequestChatHistory extends LuaTable {

	private LuaArray<ChatHistory> histories;

	public RequestChatHistory() {
		super("RequestChatHistory");
		histories = new LuaArray<>();
	}

	@Override
	public void onExport() {
		set("histories", getChatHistories());
	}

	private LuaArray<ChatHistory> getChatHistories() {
		return this.histories;
	}

	public void addChatHistory(ChatHistory chatHistory) {
		if (!histories.contains(chatHistory)) {
			histories.add(chatHistory);
		}
	}

	public void removeChatHistory(ChatHistory chatHistory) {
		if (histories.contains(chatHistory)) {
			histories.remove(chatHistory);
		}
	}

	public void removeAll() {
		histories.clear();
	}

}
