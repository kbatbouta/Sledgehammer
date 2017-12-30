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
package sledgehammer.event;

import java.util.UUID;

import sledgehammer.SledgeHammer;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.chat.ChatMessage;
import sledgehammer.module.chat.ModuleChat;

public class ChatMessageEvent extends Event {

	public static final String ID = "ChatMessageEvent";

	private ChatChannel chatChannel;
	private ChatMessage chatMessage;

	public ChatMessageEvent(ChatMessage chatMessage) {
		setMessage(chatMessage);
		UUID channelId = getMessage().getChannelId();
		ModuleChat moduleChat = getChatModule();
		setChatChannel(moduleChat.getChatChannel(channelId));
	}

	public ChatMessage getMessage() {
		return this.chatMessage;
	}

	private void setMessage(ChatMessage chatMessage) {
		this.chatMessage = chatMessage;
	}

	public ChatChannel getChatChannel() {
		return this.chatChannel;
	}

	private void setChatChannel(ChatChannel chatChannel) {
		this.chatChannel = chatChannel;
	}

	public ModuleChat getChatModule() {
		return SledgeHammer.instance.getPluginManager().getChatModule();
	}

	public String getLogMessage() {
		return null;
	}

	public String getID() {
		return ID;
	}
}