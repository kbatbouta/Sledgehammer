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
package sledgehammer.lua.core.request;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.lua.LuaTable;
import sledgehammer.lua.core.Player;

/**
 * LuaTable that handles the <ModuleCore> client-request for the <Player>'s
 * information.
 * 
 * @author Jab
 */
public class RequestInfo extends LuaTable {

	/** The <Player> Object of the Player requesting information. */
	private Player self;

	/**
	 * Main constructor.
	 */
	public RequestInfo() {
		super("requestInfo");
	}

	@Override
	public void onLoad(KahluaTable table) {
		// (Note: Players will only be authored by the server.)
		throw new IllegalStateException("RequestInfo objects cannot be loaded from Lua.");
	}

	@Override
	public void onExport() {
		set("self", getSelf());
	}

	/**
	 * @return Returns the <Player> Object of the Player requesting information.
	 */
	public Player getSelf() {
		return this.self;
	}

	/**
	 * Sets the <Player> Object of the Player requesting information.
	 * 
	 * @param player
	 *            The <Player> Object to set.
	 */
	public void setSelf(Player player) {
		this.self = player;
	}
}