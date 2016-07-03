package sledgehammer.manager;

import java.util.ArrayList;
import java.util.List;

import sledgehammer.SledgeHammer;
import sledgehammer.util.Printable;
import sledgehammer.wrapper.Player;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.GameServer;

public class PlayerManager extends Printable {

	public static final String NAME = "PlayerManager";
	
	/**
	 * List of SledgeHammer's Player instances.
	 */
	private List<Player> listPlayers;
	
	/**
	 * Instance of SledgeHammer. While this is statically accessible through the
	 * singleton, maintaining an OOP hierarchy is a good practice.
	 */
	private SledgeHammer sledgeHammer;
	
	/**
	 * Main constructor.
	 * 
	 * @param sledgeHammer
	 */
	public PlayerManager(SledgeHammer sledgeHammer) {
		this.sledgeHammer = sledgeHammer;
		
		// Initialize Player list.
		listPlayers = new ArrayList<>();
	}
	
	public void addPlayer(Player player) {
		
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
	
	@Override
	public String getName() { return NAME; }

}