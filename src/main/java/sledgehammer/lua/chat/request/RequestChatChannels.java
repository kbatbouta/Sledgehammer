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

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.lua.LuaArray;
import sledgehammer.lua.LuaTable;
import sledgehammer.lua.chat.ChatChannel;

/**
 * LuaTable designed to handle packaging of requests for ChatChannel LuaTables.
 *
 * @author Jab
 */
public class RequestChatChannels extends LuaTable {

  /** The LuaArray to store the channels. */
  private LuaArray<ChatChannel> channels;

  /** New constructor. */
  public RequestChatChannels() {
    super("RequestChatChannels");
    channels = new LuaArray<>();
  }

  /**
   * Lua load constructor.
   *
   * @param table The KahluaTable storing the data.
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
    set("length", channels.size());
    set("channels", channels);
    // @formatter:on
  }

  /**
   * Adds a given ChatChannel to the list.
   *
   * @param channel The ChatChannel to add.
   */
  public void addChannel(ChatChannel channel) {
    channels.add(channel);
  }
}
