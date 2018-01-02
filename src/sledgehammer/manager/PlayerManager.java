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
package sledgehammer.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import sledgehammer.SledgeHammer;
import sledgehammer.lua.core.Player;

/**
 * TODO: Re-implement.
 * 
 * TODO: Document
 * 
 * @author Jab
 */
public class PlayerManager extends Manager {

	public static final String NAME = "PlayerManager";

	public Map<UUID, Player> mapPlayersByID = new HashMap<>();
	public Map<String, Player> mapPlayersByUsername = new HashMap<>();
	public List<Player> listPlayers = new ArrayList<>();

	/**
	 * Main constructor.
	 */
	public PlayerManager() {

	}

	@Override
	public String getName() {
		return NAME;
	}

	public List<Player> getPlayers() {
		return listPlayers;
	}

	public Player getPlayer(String username) {
		return mapPlayersByUsername.get(username.toLowerCase());
	}

	public Player getPlayer(UUID uniqueId) {
		return mapPlayersByID.get(uniqueId);
	}

	public void addPlayer(Player player) {
		if (!listPlayers.contains(player)) {
			listPlayers.add(player);
		}
		if (!mapPlayersByID.containsKey(player.getUniqueId())) {
			mapPlayersByID.put(player.getUniqueId(), player);
		}
		if (!mapPlayersByUsername.containsKey(player.getUsername().toLowerCase())) {
			mapPlayersByUsername.put(player.getUsername().toLowerCase(), player);
		}
		if (SledgeHammer.DEBUG) {
			println("Adding player: " + player + ", " + player.getUsername() + ", " + player.getUniqueId().toString()
					+ ", " + player.getConnection());
		}
	}
}
