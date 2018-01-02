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

import java.util.List;

import sledgehammer.lua.core.Player;
import sledgehammer.util.ChatTags;

//Imports chat colors for short-hand.
import static sledgehammer.util.ChatTags.*;

/**
 * PlayerEvent that handles a chat-related events.
 * 
 * TODO: Removed unused flags from this PlayerEvent.
 * 
 * @author Jab
 */
public class ChatEvent extends PlayerEvent {

	/** The String ID of the Event. */
	public static final String ID = "ChatEvent";
	
	/** The <String> message content. */
	private String input;
	/** The <String> header of the ChatMessage. */
	private String header;
	/** The <String> encoded color of the ChatMessage header. */
	private String headerColor;
	/** The <String> encoded color of the ChatMessage content. */
	private String textColor;
	/** Flag for whether or not the ChatMessage is global. */
	private boolean global = false;
	/** Flag for whether or not the ChatMessage should be displayed to others. */
	private boolean say = false;
	/** <Byte> type flag for the ChatMessage. */
	private byte chatType = -1;
	/** The <List> of <String> user-names of <Players> that muted their chat. */
	private List<String> listMutedUsers;

	/**
	 * Main constructor.
	 * 
	 * @param player
	 *            The <Player> authoring the <PlayerEvent>.
	 * @param input
	 *            The <String> raw input from the <Player>.
	 */
	public ChatEvent(Player player, String input) {
		super(player);
		if (player == null) {
			throw new IllegalArgumentException("Player given is null!");
		}
		if (input == null || input.isEmpty()) {
			// FIXME: Potentially throws off chat.
			throw new IllegalArgumentException("Input given is null or empty!");
		}
		setText(input);
		setHeaderColor(COLOR_WHITE);
		setTextColor(COLOR_WHITE);
		setHeader(player.getUsername() + ": ");
	}

	@Override
	public String getLogMessage() {
		if (isGlobal()) {
			return "(Global) " + ChatTags.stripTags(getHeader() + getText(), false);
		} else {
			return "(Local) " + ChatTags.stripTags(getHeader() + getText(), false);
		}
	}

	@Override
	public String getID() {
		return ID;
	}

	/**
	 * @return Returns the <String> input from the ChatMessage.
	 */
	public String getText() {
		return this.input;
	}

	/**
	 * Sets the <String> input from the ChatMessage.
	 * 
	 * @param input
	 *            The <String> input to set.
	 */
	public void setText(String input) {
		this.input = input;
	}

	/**
	 * @return Returns the <String> header in-front of the ChatMessage.
	 */
	public String getHeader() {
		return this.header;
	}

	/**
	 * Sets the <String> header in-front of the ChatMessage.
	 * 
	 * @param header
	 *            The <String> header to set.
	 */
	public void setHeader(String header) {
		this.header = header;
	}

	/**
	 * @return Returns the <String> encoded color of the ChatMessage header.
	 */
	public String getHeaderColor() {
		return this.headerColor;
	}

	/**
	 * Sets the <String> encoded color of the ChatMessage header.
	 * 
	 * @param color
	 *            The <String> encoded color to set.
	 */
	public void setHeaderColor(String color) {
		this.headerColor = color;
	}

	/**
	 * @return Returns the <String> encoded color of the ChatMessage content.
	 */
	public String getTextColor() {
		return this.textColor;
	}

	/**
	 * Sets the <String> encoded color of the ChatMessage content.
	 * 
	 * @param color
	 *            The <String> encoded color to set.
	 */
	public void setTextColor(String color) {
		this.textColor = color;
	}

	/**
	 * Sets whether or not the ChatMessage is global.
	 * 
	 * @param global
	 *            Flag to set.
	 */
	public void setGlobal(boolean global) {
		this.global = global;
	}

	/**
	 * @return Returns true if the ChatMessage is global.
	 */
	public boolean isGlobal() {
		return this.global;
	}

	/**
	 * @return Returns the <Byte> type flag of the ChatMessage.
	 */
	public byte getChatType() {
		return this.chatType;
	}

	/**
	 * Sets the <Byte> type flag of the ChatMessage.
	 * 
	 * @param chatType
	 *            The <Byte> type flag to set.
	 */
	public void setChatType(byte chatType) {
		this.chatType = chatType;
	}

	/**
	 * @return Returns a <List> of <String> user-names that muted the chat.
	 */
	public List<String> getMutedUsers() {
		return this.listMutedUsers;
	}

	/**
	 * Sets the <List> of <String> user-names that muted the chat.
	 * 
	 * @param listMutedUsers
	 *            The <List> of <String> user-names to set.
	 */
	public void setMutedUsers(List<String> listMutedUsers) {
		this.listMutedUsers = listMutedUsers;
	}

	/**
	 * @return Returns true if the ChatMessage should be displayed to other users.
	 */
	public boolean sayIt() {
		return this.say;
	}

	/**
	 * Sets whether or not the ChatMessage should be displayed to other users.
	 * 
	 * @param say
	 *            The Flag to set.
	 */
	public void setSayIt(boolean say) {
		this.say = say;
	}
}