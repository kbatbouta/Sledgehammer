package sledgehammer.wrapper;

import java.util.HashMap;

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

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import sledgehammer.SledgeHammer;
import sledgehammer.event.AliveEvent;
import sledgehammer.event.DeathEvent;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.ServerOptions;
import zombie.network.ServerWorldDatabase;
import zombie.sledgehammer.SledgeHelper;

public class Player {
	
	public static final Player admin = new Player();
	
	private IsoPlayer iso;
	private UdpConnection connection;
	private String username;
	
	private Vector3f position;
	private Vector2f metaPosition;
	
	private Map<String, String> mapProperties;
	private long sinceDeath = 0L;
	private int id = -1;
	private boolean isNewAccount = false;
	private boolean isNewCharacter = false;
	private boolean isAlive = true;	
	private boolean hasInit = false;
		
//	public Player(IsoPlayer iso) {
//		if(iso == null) throw new IllegalArgumentException("IsoPlayer instance given is null!");
//		this.iso = iso;
//		connection = SledgeHelper.getConnection(iso);
//	}
	
	public Player(UdpConnection connection) {
		if(connection == null) throw new IllegalArgumentException("UdpConnection instance given is null!");
		this.connection = connection;
		this.iso = SledgeHelper.getIsoPlayer(connection);
		position = new Vector3f(0,0,0);
		metaPosition = new Vector2f(0,0);
	}
	
//	public Player(UdpConnection connection, IsoPlayer iso) {
//		if(connection == null) throw new IllegalArgumentException("UdpConnection instance given is null!");
//		if(iso        == null) throw new IllegalArgumentException("IsoPlayer instance given is null!"    );
//
//		this.connection = connection;
//		this.iso = iso;
//
//	}
	
	/**
	 * Constructor for 'Console' connections. This includes 3rd-Party console access.
	 */
	private Player() {
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
		for(UdpConnection conn : SledgeHammer.instance.getConnections()) {
			
			// If the username on the UdpConnection instance matches,
			if(conn.username.equalsIgnoreCase(username)) {
				
				// Set this connection as the instance of the Player.
				this.connection = conn;
				
				// Break out of the loop to save computation time.
				break;
			}
		}
		
		initProperties();
		position = new Vector3f(0,0,0);
		metaPosition = new Vector2f(0,0);
	}
	
	public void init() {
		
		position = new Vector3f(0,0,0);
		metaPosition = new Vector2f(0,0);
		
		if(!hasInit) {
			IsoPlayer player = get();
			if(player   != null) username = get().getUsername();
			if(username == null) username = connection.username;
			
			hasInit = true;
		}
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public void initProperties() {
		setProperties(SledgeHammer.instance.getPlayerManager().getProperties(id));
		if(getProperty("muteglobal") == null) setProperty("muteglobal", "0");
		
		if(getProperty("alive") == null || getProperty("alive").equalsIgnoreCase("0")) {
			System.out.println("NewCharacter: " + getUsername());
			this.isNewCharacter = true;
			this.isAlive = false;
		}		
	}
	
	public void setAlive(boolean flag) {
		if(isAlive && !flag) {
			isAlive = false;
			setProperty("alive", "0");
			DeathEvent event = new DeathEvent(this);
			boolean announce = ServerOptions.instance.getBoolean("AnnounceDeath").booleanValue();
			event.announce(announce);
			SledgeHammer.instance.handle(event);
			sinceDeath = System.currentTimeMillis();
		}
		// Async protection against flipping between alive and death states.
		if(!isAlive && flag && (System.currentTimeMillis() - sinceDeath) > 5000L) {
			isAlive = true;
			setProperty("alive", "1");
			AliveEvent event = new AliveEvent(this);
			SledgeHammer.instance.handle(event);
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
		return get() == null ? username.equalsIgnoreCase("admin") : get().accessLevel.equals("admin");
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
		if(mapProperties == null) {
			setProperties(SledgeHammer.instance.getPlayerManager().getProperties(getID()));
		}
		if(mapProperties == null) {
			mapProperties = new HashMap<>();
		}
		mapProperties.put(property.toLowerCase(), content);

		if(save) saveProperties();
	}
	
	public void saveProperties() {
		SledgeHammer.instance.getPlayerManager().saveProperties(id, mapProperties);
	}
	
	public String getProperty(String property) {
		if(mapProperties == null) {
			setProperties(SledgeHammer.instance.getPlayerManager().getProperties(getID()));
		}
		if(mapProperties == null) {
			setID(ServerWorldDatabase.instance.resolvePlayerID(getUsername()));
			setProperties(SledgeHammer.instance.getPlayerManager().getProperties(getID()));
		}
		if(mapProperties != null) {			
			return mapProperties.get(property.toLowerCase());
		}
		
		return null;
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
	
	public boolean isNewAccount() {
		return isNewAccount;
	}
	
	public void setNewAccount(boolean flag) {
		this.isNewAccount = flag;
	}

	public boolean isNewCharacter() {
		return isNewCharacter;
	}

	public void setNewCharacter(boolean flag) {
		this.isNewCharacter = flag;
	}
	
	public Vector3f getPosition() {
		return this.position;
	}
	
	public Vector2f getMetaPosition() {
		return this.metaPosition;
	}
	
//	public void updateInventory() {
//	
//		ByteBufferWriter bbw = connection.startPacket();
//		PacketTypes.doPacket((byte) 65, bbw);
//		
//		bbw.putShort((short) iso.OnlineID);
//		bbw.putByte((byte)iso.PlayerIndex);
//		
//		try {
//			iso.getInventory().save(bbw.bb, false);
//		} catch (Exception var6) {
//			var6.printStackTrace();
//		}
//		
//		if(iso.getClothingItem_Torso() != null) {
//			bbw.bb.putShort((short)iso.getInventory().getItems().indexOf(iso.getClothingItem_Torso()));
//		} else {
//			bbw.bb.putShort((short)-1);
//		}
//		
//		if(iso.getClothingItem_Legs() != null) {
//			bbw.bb.putShort((short)iso.getInventory().getItems().indexOf(iso.getClothingItem_Legs()));
//		} else {
//			bbw.bb.putShort((short)-1);
//		}
//		
//		if(iso.getClothingItem_Feet() != null) {
//			bbw.bb.putShort((short)iso.getInventory().getItems().indexOf(iso.getClothingItem_Feet()));
//		} else {
//			bbw.bb.putShort((short)-1);
//		}
//
//		connection.endPacketImmediate();
//	}
//	
//	public void giveItem(String name, int count) {
//		ByteBufferWriter b2 = connection.startPacket();
//		PacketTypes.doPacket((byte) 85, b2);
//		b2.putShort((short) iso.OnlineID);
//		b2.putUTF(name);
//		b2.putInt(count);
//		connection.endPacketImmediate();
//	}
}
