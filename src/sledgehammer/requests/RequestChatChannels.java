package sledgehammer.requests;

/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
*/

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
