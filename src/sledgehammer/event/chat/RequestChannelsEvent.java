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
package sledgehammer.event.chat;

import java.util.ArrayList;
import java.util.List;

import sledgehammer.event.PlayerEvent;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.core.Player;

public class RequestChannelsEvent extends PlayerEvent {

	public static final String ID = "RequestChannelsEvent";

	private List<ChatChannel> listChatChannels;

	public RequestChannelsEvent(Player player) {
		super(player);
		listChatChannels = new ArrayList<>();
	}

	public void addChatChannel(ChatChannel chatChannel) {
		if (!listChatChannels.contains(chatChannel)) {
			listChatChannels.add(chatChannel);
		}
	}

	public void removeChatChannel(ChatChannel chatChannel) {
		if (listChatChannels.contains(chatChannel)) {
			listChatChannels.remove(chatChannel);
		}
	}

	public List<ChatChannel> getChatChannels() {
		return this.listChatChannels;
	}

	public String getLogMessage() {
		return null;
	}

	public String getID() {
		return ID;
	}
}
