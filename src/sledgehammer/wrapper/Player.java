package sledgehammer.wrapper;

import sledgehammer.SledgeHammer;
import sledgehammer.util.ZUtil;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;

public class Player {
	
	private IsoPlayer iso;
	private UdpConnection connection;
	private String username;
	
	public Player(IsoPlayer iso) {
		if(iso == null) throw new IllegalArgumentException("IsoPlayer instance given is null!");
		this.iso = iso;
		connection = ZUtil.getConnection(iso);
		init();
	}
	
	public Player(UdpConnection connection) {
		if(connection == null) throw new IllegalArgumentException("UdpConnection instance given is null!");
		this.connection = connection;
		this.iso = ZUtil.getPlayer(connection);
		init();
	}
	
	public Player(UdpConnection connection, IsoPlayer iso) {
		if(connection == null) throw new IllegalArgumentException("UdpConnection instance given is null!");
		if(iso        == null) throw new IllegalArgumentException("IsoPlayer instance given is null!"    );

		this.connection = connection;
		this.iso = iso;
		init();
	}
	
	/**
	 * Constructor for 'Console' connections. This includes 3rd-Party console access.
	 */
	public Player() {
		username = "admin";
	}
	
	/**
	 * Constructor for arbitrarily defining with only a player name. The
	 * constructor attempts to locate the UdpConnection instance, and the
	 * IsoPlayer instance, using the username given.
	 * 
	 * @param username
	 */
	public Player(String username) {
		
		// Set the username of the Player instance to the parameter given.
		this.username = username;

		// Tries to get a Player instance. Returns null if invalid.
		this.iso = ZUtil.getPlayer(username);
		
		// Go through each connection.
		for(UdpConnection conn : SledgeHammer.instance.getUdpEngine().connections) {
			
			// If the username on the UdpConnection instance matches,
			if(conn.username.equalsIgnoreCase(username)) {
				
				// Set this connection as the instance of the Player.
				this.connection = conn;
				
				// Break out of the loop to save computation time.
				break;
			}
		}
	}

	private void init() {
		IsoPlayer player = get();
		if(player != null) {
			username = get().getUsername();			
		}
		if(username == null) {
			username = connection.username;
		}
	}
	
	public IsoPlayer get() {
		return iso;
	}
	
	public UdpConnection getConnection() {
		return connection;
	}
	
	public boolean isConnected() {
		return connection != null && connection.connected;
	}
	
	public boolean isInGame() {
		return isConnected() && connection.isFullyConnected();
	}
	
	public String getUsername() {
		return username;
	}
	
	public boolean isOnline() {
		if(connection == null) {
			return false;
		} else {
			return connection.connected;
		}
	}
	
	public boolean isAdmin() {
		return get() == null ? username.equalsIgnoreCase("admin") : get().admin;
	}
}
