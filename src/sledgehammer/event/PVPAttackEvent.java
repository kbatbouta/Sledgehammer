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

public class PVPAttackEvent extends Event {

	public static final String ID = "PVPAttackEvent";

	private Player playerAttacking;
	private Player playerAttacked;
	private String weapon;

	public PVPAttackEvent(Player attacking, Player attacked, String weapon) {
		super();
		this.playerAttacking = attacking;
		this.playerAttacked = attacked;
		this.weapon = weapon;
	}

	public Player getPlayerAttacking() {
		return this.playerAttacking;
	}

	public Player getPlayerAttacked() {
		return this.playerAttacked;
	}

	public String getWeapon() {
		return this.weapon;
	}

	@Override
	public String getLogMessage() {
		return playerAttacking.getUsername() + " is attacking " + playerAttacked.getUsername() + ".";
	}

	@Override
	public String getID() {
		return ID;
	}

}
