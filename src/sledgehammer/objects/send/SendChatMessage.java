package sledgehammer.objects.send;

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

/**
 * TODO: Document.
 * @author Jab
 *
 */
public class SendChatMessage extends Send {

	private ChatMessage message;
	
	public SendChatMessage() {
		super("core.chat", "sendChatMessage");
	}
	
	public void setChatMessage(ChatMessage message) {
		this.message = message;
	}
	
	public ChatMessage getChatMessage() {
		return this.message;
	}

	public void onExport() {
		ChatMessage message = getChatMessage();
		set("message", message);
		set("channel", message.getChannel());
	}

}
