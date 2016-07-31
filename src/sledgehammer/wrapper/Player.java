package sledgehammer.wrapper;

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

import java.util.Map;

import sledgehammer.SledgeHammer;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.ServerWorldDatabase;
import zombie.sledgehammer.SledgeHelper;

public class Player {
	
	private IsoPlayer iso;
	private UdpConnection connection;
	private String username;
	
	private Map<String, String> mapProperties;
	
	private int id = -1;
	
	public Player(IsoPlayer iso) {
		if(iso == null) throw new IllegalArgumentException("IsoPlayer instance given is null!");
		this.iso = iso;
		connection = SledgeHelper.getConnection(iso);
		init();
	}
	
	public Player(UdpConnection connection) {
		if(connection == null) throw new IllegalArgumentException("UdpConnection instance given is null!");
		this.connection = connection;
		this.iso = SledgeHelper.getIsoPlayer(connection);
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
		this.iso = SledgeHammer.instance.getIsoPlayerDirty(username);
		
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
		id = ServerWorldDatabase.instance.resolvePlayerID(username);
		setProperties(SledgeHammer.instance.getPlayerManager().getProperties(id));
		if(getProperty("muteglobal") == null) setProperty("muteglobal", "0");
	}
	
	private void init() {
		IsoPlayer player = get();
		if(player   != null) username = get().getUsername();
		if(username == null) username = connection.username;
		
		id = ServerWorldDatabase.instance.resolvePlayerID(username);
		
		setProperties(SledgeHammer.instance.getPlayerManager().getProperties(id));
		if(getProperty("muteglobal") == null) setProperty("muteglobal", "0");
		
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
	
	public String getName() {
		String name = getNickname();
		if(name == null) {
			name = getUsername();
		}
		
		return name;
	}
	
	public boolean isOnline() {
		if(connection == null) {
			return false;
		} else {
			return connection.connected;
		}
	}
	
	public boolean isUsername(String username) {
		return getUsername().equalsIgnoreCase(username);
	}
	
	public boolean isUsernameDirty(String username) {
		return getUsername().contains(username);
	}
	
	public boolean isNickname(String nickname) {
		return getNickname().equalsIgnoreCase(nickname);
	}
	
	public boolean isNicknameDirty(String nickname) {
		return getNickname().equalsIgnoreCase(nickname);
	}
	
	public boolean isName(String name) {
		return isUsername(name) || isNickname(name);
	}
	
	public boolean isNameDirty(String name) {
		return isUsername(name) || isNickname(name) || isUsernameDirty(name) || isNicknameDirty(name);
	}
	
	public boolean isAdmin() {
		return get() == null ? username.equalsIgnoreCase("admin") : get().admin;
	}

	public int getID() {
		return id;
	}

	public void setConnection(UdpConnection connection) {
		this.connection = connection;
	}
	
	public Map<String, String> getProperties() {
		return mapProperties;
	}
	
	public void setProperties(Map<String, String> mapProperties) {
		this.mapProperties = mapProperties;
	}

	public void setProperty(String property, String content) {
		setProperty(property, content, true);
	}
	
	public void setProperty(String property, String content, boolean save) {
		mapProperties.put(property.toLowerCase(), content);

		if(save) saveProperties();
	}
	
	public void saveProperties() {
		SledgeHammer.instance.getPlayerManager().saveProperties(id, mapProperties);
	}
	
	public String getProperty(String property) {
		return mapProperties.get(property.toLowerCase());
	}

	public void set(IsoPlayer iso) {
		this.iso = iso;
	}
	
	@Override
	public String toString() {
		return getName();
	}

	public String getNickname() {
		IsoPlayer player = get();
		if(player != null) {
			return player.getPublicUsername();
		}

		return null;
	}
}
