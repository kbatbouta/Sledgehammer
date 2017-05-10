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

import java.util.List;

import sledgehammer.util.ChatTags;
import sledgehammer.wrapper.Player;

//Imports chat colors for short-hand.
import static sledgehammer.util.ChatTags.*;

public class ChatEvent extends PlayerEvent {

	public static final String ID = "ChatEvent";

	private String input;
	private String header;
	private String headerColor;
	private String textColor;
	private boolean global = false;
	private boolean say = false;
	private byte chatType = -1;
	private List<String> listMutedUsers;
	

	public ChatEvent(Player player, String input) {
		super(player);
		if(player == null) {
			throw new IllegalArgumentException("Player given is null!");
		}
		if(input == null || input.isEmpty()) {
			//FIXME: Potentially throws off chat.
			throw new IllegalArgumentException("Input given is null or empty!");
		}

		this.input = input;
		headerColor = COLOR_WHITE;
		textColor   = COLOR_WHITE;
		
		setHeader(player.getUsername() + ": ");
	}

	public String getText() {
		return input;
	}
	
	public String getHeader() {
		return this.header;
	}
	
	public void setHeader(String header) {
		this.header = header;
	}
	
	public void setText(String input) {
		this.input = input;
	}
	
	public String getHeaderColor() {
		return this.headerColor;
	}
	
	public void setHeaderColor(String color) {
		this.headerColor = color;
	}
	
	public String getTextColor() {
		return this.textColor;
	}
	
	public void setTextColor(String color) {
		this.textColor = color;
	}

	public void setGlobal(boolean global) {
		this.global  = global;
	}
	
	public boolean isGlobal() {
		return this.global;
	}

	public byte getChatType() {
		return this.chatType;
	}
	
	public void setChatType(byte chatType) {
		this.chatType  = chatType;
	}
	
	public List<String> getMutedUsers() {
		return this.listMutedUsers;
	}

	public void setMutedUsers(List<String> listMutedUsers) {
		this.listMutedUsers = listMutedUsers;
	}
	
	
	public boolean sayIt() {
		return this.say;
	}
	
	public void setSayIt(boolean say) {
		this.say = say;
	}

	@Override
	public String getLogMessage() {
		if(isGlobal()) {
			return "(Global) " + ChatTags.stripTags(getHeader() + getText(), false);
		} else {
			return "(Local) " + ChatTags.stripTags(getHeader() + getText(), false);
		}
	}

	@Override
	public String getID() {
		return ID;
	}
	
}
