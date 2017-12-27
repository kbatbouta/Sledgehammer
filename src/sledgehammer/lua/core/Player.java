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
package sledgehammer.lua.core;

import java.util.Map;
import java.util.UUID;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.database.module.core.MongoPlayer;
import sledgehammer.event.AliveEvent;
import sledgehammer.event.DeathEvent;
import sledgehammer.event.PlayerCreatedEvent;
import sledgehammer.lua.LuaTable;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.chat.ChatMessage;
import sledgehammer.module.core.ModuleChat;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.ServerOptions;
import zombie.sledgehammer.SledgeHelper;

/**
 * TODO: Document.
 * 
 * @author Jab
 */
public class Player extends LuaTable {

	public static final Player admin = new Player(SledgeHammer.instance.getSettings().getAdministratorPassword(),
			false);

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
	private boolean created = false;

	/**
	 * UdpConnection constructor.
	 * 
	 * @param connection
	 *            The <UdpConnection> of the Player.
	 */
	public Player(UdpConnection connection) {
		super("Player");
		if (connection == null) {
			throw new IllegalArgumentException("UdpConnection instance given is null!");
		}
		this.connection = connection;
		this.iso = SledgeHelper.getIsoPlayer(connection);
		position = new Vector3f(0, 0, 0);
		metaPosition = new Vector2f(0, 0);
		color = Color.WHITE;
	}

	/**
	 * Constructor for 'Console' connections. This includes 3rd-Party console
	 * access.
	 * 
	 * @param password
	 * @param isNotActuallyAParameter
	 */
	private Player(String password, boolean isNotActuallyAParameter) {
		super("Player");
		username = "admin";
		mongoPlayer = SledgeHammer.instance.getDatabase().getMongoPlayer("admin");
		if (mongoPlayer == null) {
			mongoPlayer = SledgeHammer.instance.getDatabase().createPlayer("admin", password);
		}
		mongoPlayer.setAdministrator(true);
		mongoPlayer.setEncryptedPassword(password);
		mongoPlayer.save();
	}

	/**
	 * Constructor for arbitrarily defining with only a player name. The constructor
	 * attempts to locate the UdpConnection instance, and the IsoPlayer instance,
	 * using the username given.
	 * 
	 * @param username
	 */
	public Player(String username) {
		super("Player");
		if (username == null || username.isEmpty()) {
			throw new IllegalArgumentException("Username given is null or empty!");
		}
		// Set the username of the Player instance to the parameter given.
		this.username = username;
		// Tries to get a Player instance. Returns null if invalid.
		this.iso = SledgeHammer.instance.getIsoPlayerDirty(username);
		// Go through each connection.
		for (UdpConnection conn : SledgeHammer.instance.getConnections()) {
			// If the username on the UdpConnection instance matches,
			if (conn.username != null && conn.username.equalsIgnoreCase(username)) {
				// Set this connection as the instance of the Player.
				this.connection = conn;
				// Break out of the loop to save computation time.
				break;
			}
		}
		position = new Vector3f(0, 0, 0);
		metaPosition = new Vector2f(0, 0);
	}

	/**
	 * Offline Player constructor.
	 * 
	 * @param mongoPlayer
	 *            The <MongoPlayer> database object.
	 */
	public Player(MongoPlayer mongoPlayer) {
		super("Player");
		setMongoPlayer(mongoPlayer);
		username = mongoPlayer.getUsername();
	}

	@Override
	public void onLoad(KahluaTable table) {
		// Players will only be authored by the server.
	}

	@Override
	public void onExport() {
		set("id", getUniqueId().toString());
		set("username", getUsername());
		set("nickname", getNickname());
		set("color", getColor());
	}

	@Override
	public String getName() {
		String name = getNickname();
		if (name == null) {
			name = getUsername();
		}
		return name;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Player) {
			return ((Player) other).getUniqueId().equals(getUniqueId());
		}
		return false;
	}

	@Override
	public String toString() {
		return getName();
	}

	public void init() {
		position = new Vector3f(0, 0, 0);
		metaPosition = new Vector2f(0, 0);
		if (!hasInit) {
			IsoPlayer player = getIso();
			if (player != null)
				username = getIso().getUsername();
			if (username == null)
				username = connection.username;
			initProperties();
			hasInit = true;
		}
	}

	public void initProperties() {
		if (getProperty("muteglobal") == null)
			setProperty("muteglobal", "0");
		if (getProperty("alive") == null || getProperty("alive").equalsIgnoreCase("0")) {
			this.isNewCharacter = true;
			this.isAlive = false;
		}
	}

	/**
	 * FIXME: Possible condition bug with not setting alive property.
	 * 
	 * @param flag
	 */
	public void setAlive(boolean flag) {
		if (isAlive && !flag) {
			isAlive = false;
			setProperty("alive", "0");
			DeathEvent event = new DeathEvent(this);
			boolean announce = ServerOptions.instance.getBoolean("AnnounceDeath").booleanValue();
			event.announce(announce);
			SledgeHammer.instance.handle(event);
			sinceDeath = System.currentTimeMillis();
		}
		// Async protection against flipping between alive and death states.
		if (!isAlive && flag && (System.currentTimeMillis() - sinceDeath) > 5000L) {
			isAlive = true;
			setProperty("alive", "1");
			AliveEvent event = new AliveEvent(this);
			SledgeHammer.instance.handle(event);
		}
	}

	/**
	 * Sends a given <ChatMessage> to the Player on the ChatChannel defined.
	 * 
	 * If a ChatChannel's Id is not defined in the ChatMessage, an
	 * IllegalArgumentException is thrown.
	 * 
	 * If a ChatChanel is not found with the defined Id in the ChatMessage, an
	 * IllegalArgumentException is thrown.
	 * 
	 * @param message
	 *            The <ChatMessage> being sent to the Player.
	 */
	public void sendChatMessage(ChatMessage message) {
		// Check and make sure the Message provided is not null.
		if (message == null) {
			throw new IllegalArgumentException("ChatMessage given is null.");
		}
		// Grab the ChannelId defined in the message.
		UUID channelId = message.getChannelId();
		// Make sure that the ChatChannel is defined. It must be defined to send to the
		// Player.
		if (channelId == null) {
			throw new IllegalArgumentException("ChatMessage given does not have a defined ChatChannel Id.");
		}
		// Grab the ModuleChat to grab the ChatChannel with the given Id.
		ModuleChat moduleChat = getChatModule();
		// Grab the ChatChannel with the given Id.
		ChatChannel channel = moduleChat.getChatChannel(channelId);
		// If the channelId is invalid, we cannot send this message.
		if (channel == null) {
			throw new IllegalArgumentException("ChatMessage given does not have a valid ChatChannel.");
		}
		// Send the ChatMessage to the Player to the ChatChannel.
		channel.sendMessage(message, this);
	}

	/**
	 * Sends a given <ChatMessage> to the PLayer in all available <ChatChannel>'s.
	 * 
	 * @param chatMessage
	 *            The <ChatMessage> to send.
	 */
	public void sendChatMessageToAllChatChannels(ChatMessage chatMessage) {
		// Check and make sure the Message provided is not null.
		if (chatMessage == null) {
			throw new IllegalArgumentException("ChatMessage given is null.");
		}
		// Grab the ChatChannel for relaying to all ChatChannels.
		ChatChannel chatChannelAll = getChatModule().getAllChatChannel();
		// Clone the ChatMessage to set the ChatChannel exclusively for this action
		// only.
		chatMessage = chatMessage.clone();
		// Set the 'All' ChatChannel so that it may be distributed on the client-side.
		chatMessage.setChannelId(chatChannelAll.getUniqueId(), false);
		// Send the ChatMessage clone formally.
		sendChatMessage(chatMessage);
	}

	/**
	 * Sends a <ChatMessage> with provided <String> content to the Player.
	 * 
	 * @param message
	 *            The <String> message content to send.
	 */
	public void sendChatMessage(String message) {
		// Create the ChatMessage using the message content.
		ChatMessage chatMessage = getChatModule().createChatMessage(message);
		// Send the ChatMessage to All available ChatChannels.
		sendChatMessageToAllChatChannels(chatMessage);
	}

	public UdpConnection getConnection() {
		if (connection == null) {
			for (UdpConnection next : SledgeHammer.instance.getConnections()) {
				if (next.username.equalsIgnoreCase(getUsername())) {
					setConnection(connection);
					break;
				}
			}
		}
		return connection;
	}

	public MongoPlayer getMongoPlayer() {
		return this.mongoPlayer;
	}

	public void setMongoPlayer(MongoPlayer mongoPlayer) {
		this.mongoPlayer = mongoPlayer;
		if (!created) {
			if (SledgeHammer.instance.isStarted()) {
				PlayerCreatedEvent event = new PlayerCreatedEvent(this);
				SledgeHammer.instance.handle(event);
				created = true;
			}
		}
	}

	public boolean isWithinLocalRange(Player other) {
		if (isConnected() && other.isConnected()) {
			IsoPlayer isoOther = other.getIso();
			return getConnection().ReleventTo(isoOther.x, isoOther.y);
		}
		return false;
	}

	public boolean isOnline() {
		if (connection == null) {
			return false;
		} else {
			return connection.connected;
		}
	}

	/**
	 * @param node
	 *            The <String> node that is being tested.
	 * @return Returns true if the <Player> is granted the given <String> node
	 *         permission. If the <Player> is an administrator, this method will
	 *         always return true. To grab the raw permission, use
	 *         'hasRawPermission(String node)...'.
	 */
	public boolean hasPermission(String node) {
		if (isAdmin()) {
			return true;
		}
		return hasRawPermission(node);
	}

	public IsoPlayer getIso() {
		return iso;
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

	public String getNickname() {
		if (nickname == null)
			return username;
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

	public boolean hasRawPermission(String node) {
		return SledgeHammer.instance.getPermissionsManager().hasRawPermission(this, node);
	}

	public Color getColor() {
		return this.color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	private ModuleChat getChatModule() {
		return SledgeHammer.instance.getPluginManager().getChatModule();
	}

	public void update() {
		SledgeHammer.instance.updatePlayer(this);
	}

	public void setPermission(String username, String node, boolean flag) {
		SledgeHammer.instance.getPermissionsManager().setRawPermission(this, node, flag);
	}

	public UUID getUniqueId() {
		return getMongoPlayer().getUniqueId();
	}
}