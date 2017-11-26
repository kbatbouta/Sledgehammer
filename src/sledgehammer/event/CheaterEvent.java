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

import sledgehammer.lua.core.Player;

public class CheaterEvent extends PlayerEvent {

	public static final String ID = "CheaterEvent";
	private String logMessage = null;

	public CheaterEvent(Player player, String logMessage) {
		super(player);
		this.logMessage = logMessage;
	}

	@Override
	public String getLogMessage() {
		return "CHEATER: " + logMessage;
	}

	@Override
	public String getID() {
		return ID;
	}
}
