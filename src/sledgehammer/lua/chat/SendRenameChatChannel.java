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
package sledgehammer.lua.chat;

import sledgehammer.lua.Send;

/**
 * TODO: Document.
 * 
 * @author Jab
 */
public class SendRenameChatChannel extends Send {

	private ChatChannel channel;
	private String nameOld;
	private String nameNew;

	public SendRenameChatChannel() {
		super("core.chat", "sendRenameChatChannel");
	}

	public void set(ChatChannel channel, String nameOld, String nameNew) {
		this.channel = channel;
		this.nameOld = nameOld;
		this.nameNew = nameNew;
	}

	public ChatChannel getChatChannel() {
		return this.channel;
	}

	public String getOldName() {
		return this.nameOld;
	}

	public String getNewName() {
		return this.nameNew;
	}

	@Override
	public void onExport() {
		set("channel", getChatChannel());
		set("nameNew", getNewName());
		set("nameOld", getOldName());
	}
}