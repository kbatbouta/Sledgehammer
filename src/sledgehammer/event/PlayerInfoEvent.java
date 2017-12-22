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

public class PlayerInfoEvent extends PlayerEvent {

	public static final String ID = "PlayerInfoEvent";
	private String log = null;

	public PlayerInfoEvent(Player player) {
		super(player);
	}

	@Override
	public String getLogMessage() {
		return this.log;
	}

	public void setLogMessage(String log) {
		this.log = log;
	}

	public Vector3f getPosition() {
		return getPlayer().getPosition();
	}

	public Vector2f getMetaPosition() {
		return getPlayer().getMetaPosition();
	}

	@Override
	public String getID() {
		return ID;
	}
}
