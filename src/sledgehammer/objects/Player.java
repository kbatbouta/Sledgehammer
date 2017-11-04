package sledgehammer.objects;


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
import java.util.UUID;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.database.MongoPlayer;
import sledgehammer.event.AliveEvent;
import sledgehammer.event.DeathEvent;
import sledgehammer.event.PlayerCreatedEvent;
import sledgehammer.manager.ChatManager;
import sledgehammer.object.LuaTable;
import sledgehammer.objects.chat.ChatChannel;
import sledgehammer.objects.chat.ChatMessage;
import sledgehammer.objects.chat.ChatMessagePlayer;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.ServerOptions;
import zombie.sledgehammer.SledgeHelper;

public class Player extends LuaTable {
	
	public static final Player admin = new Player();
	
	private Map<String, String> mapProperties;
	private MongoPlayer mongoPlayer;
	private UdpConnection connection;
	private IsoPlayer iso;
	private String username;
	private String nickname;
	private Color color;
	private Vector3f position;
	private Vector2f metaPosition;
	private long sinceDeath = 0L;
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
	
	public MongoPlayer getMongoPlayer() {
		return this.mongoPlayer;
	}
	
	public void setMongoPlayer(MongoPlayer mongoPlayer) {
		this.mongoPlayer = mongoPlayer;
	}

	/**
	 * Constructor for 'Console' connections. This includes 3rd-Party console access.
	 */
	private Player() {
		super("Player");
		username = "admin";
		mongoPlayer = SledgeHammer.instance.getDatabase().getMongoPlayer("admin");
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
		if(username == null || username.isEmpty()) {
			throw new IllegalArgumentException("Username given is null or empty!");
		}
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
			initProperties();
			hasInit = true;
		}
	}
	
	public void initProperties() {
		if(getProperty("muteglobal") == null) setProperty("muteglobal", "0");
		if(getProperty("alive") == null || getProperty("alive").equalsIgnoreCase("0")) {
			System.out.println("NewCharacter: " + getUsername());
			this.isNewCharacter = true;
			this.isAlive = false;
		}		
	}
	
	/**
	 * FIXME: Possible condition bug with not setting alive property.
	 * @param flag
	 */
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
		getMongoPlayer().setMetaData(property, content, save);
	}
	
	public void saveProperties() {
		saveProperties();
	}
	
	public String getProperty(String property) {
		return getMongoPlayer().getMetaData(property);
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
		set("id", getUniqueId().toString());
		set("username", getUsername());
		set("nickname", getNickname());
		set("color", getColor());
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public void sendMessage(ChatMessage message) {
		ChatChannel channel = SledgeHammer.instance.getChatManager().getChannel(message.getChannel());
		if(message instanceof ChatMessagePlayer) {
			channel.sendMessagePlayer((ChatMessagePlayer)message, this);
		} else {			
			channel.sendMessage(message, this);
		}
	}

	public void sendMessageAllChannels(ChatMessagePlayer message) {
		String oldChannel = message.getChannel();
		ChatChannel channel = ChatManager.chatChannelAll;
		message.setChannel("*");
		if(message instanceof ChatMessagePlayer) {
			channel.sendMessagePlayer((ChatMessagePlayer)message, this);
		} else {			
			channel.sendMessage(message, this);
		}
		message.setChannel(oldChannel);
	}

	public void sendMessage(String string) {
		ChatMessage message = new ChatMessage(string);
		sendMessage(message);
	}

	public void update() {
		SledgeHammer.instance.updatePlayer(this);
	}

	public boolean isWithinLocalRange(Player other) {
		if(isConnected() && other.isConnected()) {
			IsoPlayer isoOther = other.getIso();
			return getConnection().ReleventTo(isoOther.x, isoOther.y);
		}
		return false;
	}

	public void setPermission(String username, String context, boolean b) {
		SledgeHammer.instance.getPermissionsManager().setPermission(username, context, b);
	}

	public boolean hasRawPermission(String context) {
		return SledgeHammer.instance.getPermissionsManager().hasRawPermission(username, context);
	}
	
	public UUID getUniqueId() {
		return getMongoPlayer().getUniqueId();
	}
	
}
