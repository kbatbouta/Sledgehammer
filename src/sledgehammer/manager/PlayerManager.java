package sledgehammer.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.event.ConnectEvent;
import sledgehammer.event.DisconnectEvent;
import sledgehammer.event.Event;
import sledgehammer.interfaces.EventListener;
import sledgehammer.wrapper.Player;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.GameServer;
import zombie.network.ServerWorldDatabase;

public class PlayerManager extends Manager {

	public static final String NAME = "PlayerManager";
	
	/**
	 * List of SledgeHammer's Player instances.
	 */
	private List<Player> listPlayers;
	
	private Map<String, Player> mapPlayersByUserName;
	
	private Map<Integer, Player> mapPlayersByDatabaseID;
	
	private Player admin = new Player();
	
	/**
	 * Instance of SledgeHammer. While this is statically accessible through the
	 * singleton, maintaining an OOP hierarchy is a good practice.
	 */
	private SledgeHammer sledgeHammer;
	
	private DisconnectionHandler disconnectionHandler;
	
	/**
	 * Main constructor.
	 * 
	 * @param sledgeHammer
	 */
	public PlayerManager(SledgeHammer sledgeHammer) {
		this.sledgeHammer = sledgeHammer;
		
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
	 * Returns a Player, based on a user-name.
	 * 
	 * If the wild-card parameter is flag as true, this method will search to
	 * see if the user-name given is contained in a player's user-name.
	 * 
	 * @param username
	 *
	 * @param wildcard
	 *
	 * @return
	 */
	private static IsoPlayer getIsoPlayerByUsername(String username, boolean wildcard) {

		if (wildcard) username = username.toLowerCase().trim();
		
		for (UdpConnection connection : SledgeHammer.instance.getConnections()) {
			for (int playerIndex = 0; playerIndex < 4; ++playerIndex) {
				IsoPlayer player = connection.players[playerIndex];
				if (player != null) {
					String usernameNext = player.getUsername().toLowerCase();
					if (wildcard) {
						if (usernameNext.contains(username)) return player;
					} else {
						if (usernameNext.equals(username)) return player;
					}
				}
			}
		}

		return null;
	}
	
	/**
	 * Returns a Player, based on a nick-name.
	 * 
	 * If the wild-card parameter is flag as true, this method will search to
	 * see if the nick-name given is contained in a player's nick-name.
	 * 
	 * @param nickname
	 *
	 * @param wildcard
	 *
	 * @return
	 */
	private static IsoPlayer getIsoPlayerByNickname(String nickname, boolean wildcard) {

		if (wildcard) nickname = nickname.toLowerCase().trim();
		
		for (UdpConnection connection : SledgeHammer.instance.getConnections()) {
			for (int playerIndex = 0; playerIndex < 4; ++playerIndex) {
				IsoPlayer player = connection.players[playerIndex];
				if (player != null) {
					
					String usernameNext = player.getPublicUsername();
					if(usernameNext == null) continue;
					usernameNext = usernameNext.toLowerCase();
					
					if (wildcard) {
						if (usernameNext.contains(nickname)) return player;
					} else {
						if (usernameNext.equals(nickname)) return player;
					}
				}
			}
		}

		return null;
	}
	
	/**
	 * Returns a Player, based on a user-name. If the name given doesn't match any
	 * connected user-names, a second search will occur, attempting to match
	 * nicknames with the name.
	 * 
	 * @param username
	 * @return
	 */
	public static IsoPlayer getIsoPlayer(String name) {
		IsoPlayer player = null;
		
		player = getIsoPlayerByUsername(name, false);
		
		if(player == null) {
			player = getIsoPlayerByNickname(name, false);
		}
		
		return player;
	}
	
	/**
	 * Returns a Player, based on a user-name. If the name given doesn't match any
	 * connected user-names, a second search will occur, attempting to match
	 * nicknames with the name.
	 * 
	 * If the wild-card parameter is flag as true, a 2nd iteration of the search
	 * will be made if the player is not located, searching for the name given,
	 * to see if the name is contained in a user-name, or nickname of a player.
	 * 
	 * @param name
	 * 
	 * @param wildcard
	 * 
	 * @return
	 */
	public static IsoPlayer getIsoPlayerDirty(String name, boolean wildcard) {
		IsoPlayer player = null;
		
		player = getIsoPlayerByUsername(name, false);
		
		if(player == null) {
			player = getIsoPlayerByNickname(name, false);
		}
		
		if(player == null && wildcard) {
			
			name = name.toLowerCase().trim();
			
			player = getIsoPlayerByUsername(name, true);
			
			if(player == null) {
				player = getIsoPlayerByNickname(name, true);
			}
		}
		
		return player;
	}
	
	/**
	 * Returns a Player, based on the UdpConnection given.
	 * 
	 * @param connection
	 * 
	 * @return
	 */
	public static IsoPlayer getIsoPlayer(UdpConnection connection) {
		long guid = connection.getConnectedGUID();
		
		for(IsoPlayer player : GameServer.PlayerToAddressMap.keySet()) {
			if(player != null) {
				Long value = (Long) GameServer.PlayerToAddressMap.get(player);
				if(value.longValue() == guid) {
					return (IsoPlayer) player;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Returns a UdpConnection instance tied with the IsoPlayer instance given.
	 * 
	 * @param player
	 * 
	 * @return
	 */
	public static UdpConnection getConnection(IsoPlayer player) {
		Long guid = GameServer.PlayerToAddressMap.get(player);

		if (guid == null)
			return null;

		return SledgeHammer.instance.getUdpEngine().getActiveConnection(guid.longValue());
	}
	
	/**
	 * 
	 * @param username
	 * 
	 * @return
	 */
	public static IsoPlayer getIsoPlayerByUsername(String username) {
		return getIsoPlayerByUsername(username, false);
	}

	/**
	 * 
	 * @param nickname
	 * 
	 * @return
	 */
	public static IsoPlayer getIsoPlayerByNickname(String nickname) {
		return getIsoPlayerByNickname(nickname, false);
	}
	
	/**
	 * 
	 * @param nickname
	 * 
	 * @return
	 */
	public static IsoPlayer getIsoPlayerByNicknameDirty(String nickname) {
		return getIsoPlayerByNickname(nickname, true);
	}

	/**
	 * 
	 * @param username
	 * 
	 * @return
	 */
	public static IsoPlayer getIsoPlayerByUsernameDirty(String username) {
		return getIsoPlayerByUsername(username, true);
	}
	
	/**
	 * Returns a Player, based on a username.
	 * 
	 * @param username
	 * 
	 * @return
	 */
	public static IsoPlayer getIsoPlayerDirty(String username) {
		return getIsoPlayerDirty(username, true);
	}
	
	public SledgeHammer getSledgeHammer() {
		return sledgeHammer;
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
		player = new Player(username);
		
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
	
	private class ConnectionHandler implements EventListener {

		private PlayerManager manager;
		
		ConnectionHandler(PlayerManager manager) {
			this.manager = manager;
		}
		
		@Override
		public String[] getTypes() { return new String[] {ConnectEvent.ID}; }

		@Override
		public void handleEvent(Event event) {
			if(event.getID() == ConnectEvent.ID) {				
				manager.onConnect(((ConnectEvent)event).getPlayer());
			}
		}

		@Override
		public boolean runSecondary() {
			return false;
		}	
	}

	@Override
	public void onStart() {
		sledgeHammer.register(disconnectionHandler);
	}

	@Override
	public void onShutDown() {
		sledgeHammer.unregister(disconnectionHandler);
	}

	@Override
	public void onLoad() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpdate() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Returns player properties based on a given player ID.
	 * 
	 * @param id
	 * 
	 * @return
	 */
	public Map<String, String> getProperties(int id) {
		return sledgeHammer.getModuleManager().getCoreModule().getProperties(id);
	}
	
	public void saveProperties(int id, Map<String, String> mapProperties) {
		sledgeHammer.getModuleManager().getCoreModule().saveProperties(id, mapProperties);
	}
	
	public Player getAdmin() {
		return admin;
	}
	
}
