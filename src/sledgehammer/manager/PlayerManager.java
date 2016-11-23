package sledgehammer.manager;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.event.DisconnectEvent;
import sledgehammer.event.Event;
import sledgehammer.interfaces.EventListener;
import sledgehammer.wrapper.Player;
import zombie.network.ServerWorldDatabase;

public class PlayerManager extends Manager {

	public static final String NAME = "PlayerManager";
	
	/**
	 * List of SledgeHammer's Player instances.
	 */
	private List<Player> listPlayers;
	
	private Map<String, Player> mapPlayersByUserName;
	
	private Map<Integer, Player> mapPlayersByDatabaseID;
	
	private DisconnectionHandler disconnectionHandler;
	
	/**
	 * Main constructor.
	 * 
	 * @param sledgeHammer
	 */
	public PlayerManager(SledgeHammer sledgeHammer) {
		super(sledgeHammer);
		
		// Initialize Player list.
		listPlayers = new ArrayList<>();
		
		// Initialize the maps.
		mapPlayersByUserName   = new HashMap<>();
		mapPlayersByDatabaseID = new HashMap<>();
		
		disconnectionHandler = new DisconnectionHandler(this);
	}
	
	public Player addPlayer(Player player) {
	
		// Check to make sure the player passed is valid.
		if(player == null) throw new IllegalArgumentException("Player given is null!");
	
		int id = player.getID();
		String username = player.getUsername();
		
		Player playerCheck = getPlayerByUsername(username);
		if(playerCheck != null) return playerCheck;
		
		if(!mapPlayersByUserName.containsKey(username)) {			
			mapPlayersByUserName.put(username, player);
		}
		
		if(!listPlayers.contains(player)) {			
			listPlayers.add(player);
		}
		
		if(!mapPlayersByDatabaseID.containsKey(id)) {
			mapPlayersByDatabaseID.put(id, player);
		}
		
		return player;

	}
	
	public List<Player> getPlayers() {
		return listPlayers;
	}
	
	/**
	 * Resolves a Player instance of the database ID.
	 * 
	 * Returns null if ID does not exist.
	 * 
	 * @param id
	 * 
	 * @return
	 */
	public Player resolve(int id) {
		
		// The player to return.
		Player player;
		
		// Check and see if the Player already exists in the cache.
		player = this.mapPlayersByDatabaseID.get(id);
		if(player != null) return player;
		
		// If not, Grab the player's username.
		String username = ServerWorldDatabase.instance.resolvePlayerName(id);
		
		// There no user that exists. Return null.
		if(username == null) return null;
		
		// Find if the player is already online.
		player = getPlayerByUsername(username);
		
		// If the player has not online, create an offline version.
		if(player == null) player = createOfflinePlayer(username);
		
		// Return the result.
		return player;
	}
	
	/**
	 * Creates an Offline version of a player.
	 * 
	 * @param username
	 * 
	 * @return
	 */
	public Player createOfflinePlayer(String username) {
		
		// Check if username given is valid.
		if(username == null) throw new IllegalArgumentException("Username given is null!");
		
		Player player = getPlayerByUsername(username);
		
		// If the player is online, then simply return that copy instead.
		if(player != null) return player;
		
		// Create an offline player.
		// player = new Player(username);
		
		// Add this player to the cache.
		// addPlayer(player);
		
		// Return the result.
		return player;
	}
	
	public Player getPlayerByUsername(String username) {
		return mapPlayersByUserName.get(username);
	}
	
	public Player getPlayerByID(int id) {
		return mapPlayersByDatabaseID.get(id);
	}
	
	public Player getPlayerDirty(String name) {
		
		// Search by username.
		Player player = getPlayerByUsername(name);
		
		// Search by nickname.
		if(player == null) {			
			player = getPlayerByNickname(name);
		}
		
		// Search dirty for username.
		if(player == null) {
			for(Player nextPlayer : getPlayers()) {
				if(nextPlayer.getUsername().contains(name)) {
					player = nextPlayer;
					break;
				}
			}
		}
		
		// Search dirty for nickname.
		if(player == null) {
			for(Player nextPlayer : getPlayers()) {
				if(nextPlayer.getNickname().contains(name)) {
					player = nextPlayer;
					break;
				}
			}
		}
		
		return player;
	}
	
	private Player getPlayerByNickname(String name) {
		
		Player player = null;
		
		for(Player nextPlayer : getPlayers()) {
			if(nextPlayer.getNickname().equals(name)) {
				player = nextPlayer;
				break;
			}
		}
		
		return player;
	}

	public void onConnect(Player player) {
		
	}

	/**
	 * Handles disconnection of players with the cache.
	 * @param player
	 */
	protected void onDisconnect(Player player) {
		int id = player.getID();
		String username = player.getUsername();
		
		listPlayers.remove(player);
		mapPlayersByDatabaseID.remove(id);
		mapPlayersByUserName.remove(username);
		
		// Save the Player's properties.
		player.saveProperties();
	}
	
	@Override
	public String getName() { return NAME; }

	private class DisconnectionHandler implements EventListener {

		private PlayerManager manager;
		
		DisconnectionHandler(PlayerManager manager) {
			this.manager = manager;
		}
		
		@Override
		public String[] getTypes() { return new String[] {DisconnectEvent.ID}; }

		@Override
		public void handleEvent(Event event) {
			if(event.getID() == DisconnectEvent.ID) {				
				manager.onDisconnect(((DisconnectEvent)event).getPlayer());
			}
		}

		@Override
		public boolean runSecondary() {
			return false;
		}	
	}

	@Override
	public void onStart() {
		getSledgeHammer().register(disconnectionHandler);
	}

	@Override
	public void onShutDown() {
		getSledgeHammer().unregister(disconnectionHandler);
	}

	@Override
	public void onLoad() {
	}

	@Override
	public void onUpdate() {
	}

	/**
	 * Returns player properties based on a given player ID.
	 * 
	 * @param id
	 * 
	 * @return
	 */
	public Map<String, String> getProperties(int id) {
		return getSledgeHammer().getModuleManager().getCoreModule().getProperties(id);
	}
	
	public void saveProperties(int id, Map<String, String> mapProperties) {
		getSledgeHammer().getModuleManager().getCoreModule().saveProperties(id, mapProperties);
	}
	
	public Player getAdmin() {
		return Player.admin;
	}

	public void registerPlayer(Player player, String username) {
		int id = player.getID();
		mapPlayersByUserName.put(username, player);
		if(!listPlayers.contains(player)) {	
			listPlayers.add(player);
		}
		if(!mapPlayersByDatabaseID.containsKey(id)) {
			mapPlayersByDatabaseID.put(id, player);
		}
	}
	
}
