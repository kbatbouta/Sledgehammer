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
	
	public Player() {
		username = "admin";
	}
	
	public Player(String username) {
		this.iso = ZUtil.getPlayer(username);
		for(UdpConnection conn : SledgeHammer.instance.getUdpEngine().connections) {
			if(conn.username.equalsIgnoreCase(username)) {
				this.connection = conn;
				break;
			}
		}
		this.username = username;
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
		return get() == null? username.equalsIgnoreCase("admin") : get().admin;
	}
}
