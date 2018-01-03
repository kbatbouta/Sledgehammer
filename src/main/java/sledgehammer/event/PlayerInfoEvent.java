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

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import sledgehammer.lua.core.Player;

/**
 * PlayerEvent that handles the transmission of a <Player>'s position, and
 * meta-position.
 * 
 * @author Jab
 */
public class PlayerInfoEvent extends PlayerEvent {

	/** The String ID of the Event. */
	public static final String ID = "PlayerInfoEvent";

	/** The <String> logged message for the <PlayerInfoEvent>. */
	private String logMessage = null;

	/**
	 * Main constructor.
	 * 
	 * @param player
	 *            The <Player> being updated.
	 */
	public PlayerInfoEvent(Player player) {
		super(player);
	}

	@Override
	public String getLogMessage() {
		return this.logMessage;
	}

	@Override
	public String getID() {
		return ID;
	}

	/**
	 * Sets the <String> logged message for the <PlayerInfoEvent>.
	 * 
	 * @param logMessage
	 *            The <String logged message to set.
	 */
	public void setLogMessage(String logMessage) {
		this.logMessage = logMessage;
	}

	/**
	 * @return Returns the <Vector3f> position of the <Player>.
	 */
	public Vector3f getPosition() {
		return getPlayer().getPosition();
	}

	/**
	 * @return Returns the <Vector2f> meta-position of the <Player>.
	 */
	public Vector2f getMetaPosition() {
		return getPlayer().getMetaPosition();
	}
}