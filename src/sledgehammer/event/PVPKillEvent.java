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

import sledgehammer.wrapper.Player;

public class PVPKillEvent extends Event {

	public static final String ID = "PVPKillEvent";
	
	private Player playerKiller;
	private Player playerKilled;
	
	public PVPKillEvent(Player killer, Player killed) {
		super();
		this.playerKiller = killer;
		this.playerKilled = killed;
	}
	
	public Player getKiller() {
		return this.playerKiller;
	}
	
	public Player getKilled() {
		return this.playerKilled;
	}

	@Override
	public String getLogMessage() {
		String playerKillerName = "Unknown Player (Null)";
		String playerKilledName = "Unknown Player (Null)";
		if(playerKiller != null) playerKillerName = playerKiller.getUsername();
		if(playerKilled != null) playerKilledName = playerKilled.getUsername();
		return playerKillerName + " killed " + playerKilledName + '.';
	}

	@Override
	public String getID() {
		return ID;
	}
}
