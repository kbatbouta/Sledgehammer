/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

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
