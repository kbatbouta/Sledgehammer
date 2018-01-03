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
 * Manager to handle <Player> data and operations for the Sledgehammer engine.
 * 
 * @author Jab
 */
public class PlayerManager extends Manager {

	/** The <String> name of the <Manager>. */
	public static final String NAME = "PlayerManager";

	/** The <Map> of <Player>'s identified by their <UUID> unique Id's. */
	public Map<UUID, Player> mapPlayersByID = new HashMap<>();
	/** The <Map> of <Player>'s identified by their <String> user-names. */
	public Map<String, Player> mapPlayersByUsername = new HashMap<>();
	/** A <List> of <Player>'s online. */
	public List<Player> listPlayers = new ArrayList<>();

	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * @return Returns a <List> of <Player>'s registered in the <PlayerManager>.
	 */
	public List<Player> getPlayers() {
		return listPlayers;
	}

	/**
	 * @param username
	 *            The <String> user-name of the <Player>.
	 * @return Returns a <Player> with the given <String> user-name. If no <Player>
	 *         identifies with the <String> user-name, then null is returned.
	 */
	public Player getPlayer(String username) {
		return mapPlayersByUsername.get(username.toLowerCase());
	}

	/**
	 * @param uniqueId
	 *            The <UUID> unique Id of the <Player>.
	 * @return Returns a <Player> with the given <UUID> unique Id. If no <Player>
	 *         identifies with the UUID unique Id, then null is retrned.
	 */
	public Player getPlayer(UUID uniqueId) {
		return mapPlayersByID.get(uniqueId);
	}

	/**
	 * Adds a <Player> to the <PlayerManager>.
	 * 
	 * @param player
	 *            The <Player> to add.
	 */
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