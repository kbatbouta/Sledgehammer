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

public class PVPEvent extends PlayerEvent {

	public static final String ID = "PVPEvent";

	private boolean enabled;

	public PVPEvent(Player player, boolean enabled) {
		super(player);
		this.enabled = enabled;
	}

	public boolean isPVPEnabled() {
		return this.enabled;
	}

	public String getLogMessage() {
		return getPlayer().getUsername() + " " + (isPVPEnabled() ? "enabled" : "disabled") + " PVP.";
	}

	public String getID() {
		return ID;
	}
}
