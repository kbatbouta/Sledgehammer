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
package sledgehammer.lua;

import java.util.Map;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.lua.core.Player;

/**
 * TODO: Document.
 * 
 * @author Jab
 */
public class RequestInfo extends LuaTable {

	private int playerID = -1;
	private Player self;

	public RequestInfo() {
		super("requestInfo");
	}

	public void setPlayerID(int id) {
		if (this.playerID != id) {
			this.playerID = id;
			set("playerID", this.playerID);
		}
	}

	public void construct(Map<Object, Object> definitions) {
		definitions.put("self", getSelf());
	}

	public void onLoad(KahluaTable table) {
		// Server authored only.
	}

	public Player getSelf() {
		return this.self;
	}

	public void setSelf(Player player) {
		this.self = player;
		set("self", getSelf());
	}

	@Override
	public void onExport() {
		set("self", getSelf());
	}
}