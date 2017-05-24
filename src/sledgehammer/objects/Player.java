package sledgehammer.objects;

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

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.event.AliveEvent;
import sledgehammer.event.DeathEvent;
import sledgehammer.event.PlayerCreatedEvent;
import sledgehammer.modules.core.ModuleChat;
import sledgehammer.object.LuaTable;
import sledgehammer.objects.chat.ChatChannel;
import sledgehammer.objects.chat.ChatMessage;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.ServerOptions;
import zombie.network.ServerWorldDatabase;
import zombie.sledgehammer.SledgeHelper;

public class Player extends LuaTable {
	
	public static final Player admin = new Player();
	
	private IsoPlayer iso;
	private UdpConnection connection;
	private String username;
	private String nickname;
	
	private Color color;
	
	private Vector3f position;
	private Vector2f metaPosition;
	
	private Map<String, String> mapProperties;
	private long sinceDeath = 0L;
	private int id = -1;
	private boolean isNewAccount = false;
	private boolean isNewCharacter = false;
	private boolean isAlive = true;	
	private boolean hasInit = false;
		
	public Player(UdpConnection connection) {
		super("Player");
		if(connection == null) throw new IllegalArgumentException("UdpConnection instance given is null!");
		this.connection = connection;
		this.iso = SledgeHelper.getIsoPlayer(connection);
		position = new Vector3f(0,0,0);
		metaPosition = new Vector2f(0,0);
		color = Color.WHITE;
		
		if(SledgeHammer.instance.isStarted()) {			
			PlayerCreatedEvent event = new PlayerCreatedEvent(this);
			SledgeHammer.instance.handle(event);
		}
	}
	
	/**
	 * Constructor for 'Console' connections. This includes 3rd-Party console access.
	 */
	private Player() {
		super("Player");
		username = "admin";	
		
		if(SledgeHammer.instance.isStarted()) {			
			PlayerCreatedEvent event = new PlayerCreatedEvent(this);
			SledgeHammer.instance.handle(event);
		}
	}
	
	/**
	 * Constructor for arbitrarily defining with only a player name. The
	 * constructor attempts to locate the UdpConnection instance, and the
	 * IsoPlayer instance, using the username given.
	 * 
	 * @param username
	 */
	public Player(String username) {
		super("Player");
		// Set the username of the Player instance to the parameter given.
		this.username = username;

		// Tries to get a Player instance. Returns null if invalid.
		this.iso = SledgeHammer.instance.getIsoPlayerDirty(username);
		
		// Go through each connection.
		for(UdpConnection conn : SledgeHammer.instance.getConnections()) {
			
			// If the username on the UdpConnection instance matches,
			if(conn.username != null && conn.username.equalsIgnoreCase(username)) {
				
				// Set this connection as the instance of the Player.
				this.connection = conn;
				
				// Break out of the loop to save computation time.
				break;
			}
		}
		
		initProperties();
		position = new Vector3f(0,0,0);
		metaPosition = new Vector2f(0,0);
		
		if(SledgeHammer.instance.isStarted()) {			
			PlayerCreatedEvent event = new PlayerCreatedEvent(this);
			SledgeHammer.instance.handle(event);
		}
	}
	
	public void init() {
		
		position = new Vector3f(0,0,0);
		metaPosition = new Vector2f(0,0);
		
		if(!hasInit) {
			IsoPlayer player = getIso();
			if(player   != null) username = getIso().getUsername();
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
	
	public IsoPlayer getIso() {
		return iso;
	}
	
	public UdpConnection getConnection() {
		if(connection == null) {
			for(UdpConnection next : SledgeHammer.instance.getConnections()) {
				if(next.username.equalsIgnoreCase(getUsername())) {
					setConnection(connection);
					break;
				}
			}
		}
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
		return getIso() == null ? username.equalsIgnoreCase("admin") : getIso().accessLevel.equals("admin");
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
		if(nickname == null) return username;
		return nickname;
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
	
	/**
	 * Proxy method for asking if the Player has a permission.
	 * @param context
	 * @return
	 */
	public boolean hasPermission(String context) {
		return SledgeHammer.instance.getPermissionsManager().hasPermission(getUsername(), context);
	}

	@Override
	public void onLoad(KahluaTable table) {
		// Players will only be authored by the server.
	}
	
	public Color getColor() {
		return this.color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public void onExport() {
		set("id", getID());
		set("username", getUsername());
		set("nickname", getNickname());
		set("color", getColor());
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public void sendMessage(ChatMessage message) {
		ChatChannel channel = SledgeHammer.instance.getChatManager().getChannel(message.getChannel());
		channel.sendMessage(message, this);
	}
	
}
