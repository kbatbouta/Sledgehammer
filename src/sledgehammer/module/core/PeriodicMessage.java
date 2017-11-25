package sledgehammer.module.core;

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

import sledgehammer.util.ChatTags;

/**
 * Class designed to handle periodic messages on the server.
 * 
 * @author Jab
 *
 */
public class PeriodicMessage {

	/**
	 * The name of the message. (Used for identification)
	 */
	private String name;

	/**
	 * The message content.
	 */
	private String content;

	/**
	 * The color of the message broadcasted.
	 */
	private String color = ChatTags.COLOR_WHITE;

	/**
	 * Whether or not to broadcast the message.
	 */
	private boolean enabled = false;

	/**
	 * Whether or not to broadcast the message on the screen.
	 */
	private boolean broadcast = false;

	/**
	 * The time setting. (In minutes. E.G: 15 = 15 minutes)
	 */
	private int time = 15;

	/**
	 * Main constructor.
	 * 
	 * @param name
	 * @param content
	 */
	public PeriodicMessage(String name, String content) {
		this.name = name;
		this.content = content;
	}

	/**
	 * Returns whether or not this message is enabled.
	 * 
	 * @return
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the enabled flag for this message.
	 * 
	 * @param flag
	 */
	public void setEnabled(boolean flag) {
		enabled = flag;
	}

	/**
	 * Returns this message's content.
	 * 
	 * @return
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Returns the name associated with this message. (the ID of the message)
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns how long (in minutes), this message waits until execution.
	 *
	 * @return
	 */
	public int getTime() {
		return time;
	}

	/**
	 * Sets the time (in minutes), this message waits until execution.
	 * 
	 * @param time
	 */
	public void setTime(int time) {
		this.time = time;
	}

	/**
	 * Returns whether or not this message is broadcasted on the screen.
	 * 
	 * @return
	 */
	public boolean isBroadcasted() {
		return broadcast;
	}

	/**
	 * Sets whether or not this message is broadcasted on the screen.
	 * 
	 * @param flag
	 */
	public void setBroadcasted(boolean flag) {
		broadcast = flag;
	}

	public String getColor() {
		return color;
	}
	
	public void setColor(String color) {
		this.color = color;
	}
}