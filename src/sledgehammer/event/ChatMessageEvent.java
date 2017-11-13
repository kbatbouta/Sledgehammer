package sledgehammer.event;

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

import sledgehammer.objects.chat.ChatMessage;

public class ChatMessageEvent extends Event {

	public static final String ID = "ChatMessageEvent";

	private ChatMessage message;
	
	public ChatMessageEvent(ChatMessage message) {
		this.message = message;
	}
	
	public ChatMessage getMessage() {
		return this.message;
	}
	
	public String getChannelName() {
		return getMessage().getChannel();
	}
	
	public String getLogMessage() { return null; }
	public String getID() { return ID; }

}
