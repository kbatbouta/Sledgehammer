/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

package sledgehammer.lua.core;

import java.util.Map;
import java.util.UUID;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.database.module.core.MongoPlayer;
import sledgehammer.event.player.AliveEvent;
import sledgehammer.event.player.DeathEvent;
import sledgehammer.event.player.PlayerCreatedEvent;
import sledgehammer.language.Language;
import sledgehammer.lua.MongoLuaObject;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.chat.ChatMessage;
import sledgehammer.lua.permissions.PermissionGroup;
import sledgehammer.lua.permissions.PermissionUser;
import sledgehammer.module.chat.ModuleChat;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.ServerOptions;
import zombie.sledgehammer.SledgeHelper;

/**
 * MongoLuaObject to store player data and handle player operations and utilities for the
 * Sledgehammer engine.
 *
 * @author Jab
 */
public class Player extends MongoLuaObject<MongoPlayer> {

  /** The Player instance of the Administrator account. */
  public static final Player admin;

  /** The Map of meta-data properties, stored using String keys and String values. */
  private Map<String, String> mapProperties;
  /** The native UdpConnection Object of the Player. */
  private UdpConnection connection;
  /** The native IsoPlayer Object of the Player. */
  private IsoPlayer iso;
  /** The Language for messages sent. */
  private Language language = Language.English;
  /** The String user-name of the Player. */
  private String username;
  /** The String nickname of the Player. */
  private String nickname;
  /** The Color LuaTable representing the Player. */
  private Color color = Color.WHITE;
  /** The Vector3f position of the Player. */
  private Vector3f position;
  /** The Vector2f meta-position of the Player. */
  private Vector2f positionMeta;
  /** The Long time-stamp in milliseconds since the Player's last in-game character dies. */
  private long sinceDeath = 0L;
  /** The Boolean flag to signify if the Player account is new. */
  private boolean isNewAccount = false;
  /** The Boolean flag to signify if the Player's in-game character is a new character. */
  private boolean isNewCharacter = false;
  /** The Boolean flag to signify if the Player is currently alive in-game. */
  private boolean isAlive = true;
  /** The Boolean flag to signify if the Player Object has initialized. */
  private boolean initialized = false;
  /**
   * The Boolean flag to signify if the PlayerCreateEvent has dispatched after the Player Object is
   * initialized.
   */
  private boolean created = false;

  /**
   * UdpConnection constructor.
   *
   * @param connection The UdpConnection of the Player.
   */
  public Player(UdpConnection connection) {
    super(null, "Player");
    if (connection == null) {
      throw new IllegalArgumentException("UdpConnection instance given is null!");
    }
    setConnection(connection);
    setIso(SledgeHelper.getIsoPlayer(connection));
    setPosition(new Vector3f(0, 0, 0));
    setMetaPosition(new Vector2f(0, 0));
    setColor(Color.WHITE);
  }

  /**
   * Constructor for 'Console' connections. This includes 3rd-Party console access.
   *
   * @param password The String password of the Player.
   * @param isNotActuallyAParameter (This is used to differentiate between constructors that use the
   *     same Class-type parameter)
   */
  private Player(String password, boolean isNotActuallyAParameter) {
    super(null, "Player");
    setUsername("admin");
    MongoPlayer mongoDocument = SledgeHammer.instance.getDatabase().getMongoPlayer("admin");
    if (mongoDocument == null) {
      mongoDocument = SledgeHammer.instance.getDatabase().createPlayer("admin", password);
    }
    mongoDocument.setAdministrator(true, false);
    mongoDocument.setEncryptedPassword(password);
    mongoDocument.save();
    setMongoDocument(mongoDocument);
  }

  /**
   * Constructor for arbitrarily defining with only a player name. The constructor attempts to
   * locate the UdpConnection instance, and the IsoPlayer instance, using the user-name given.
   *
   * @param username The String username of the Player.
   */
  public Player(String username) {
    super(null, "Player");
    if (username == null || username.isEmpty()) {
      throw new IllegalArgumentException("Username given is null or empty!");
    }
    // Set the user-name of the Player instance to the parameter given.
    setUsername(username);
    // Tries to get a Player instance. Returns null if invalid.
    setIso(SledgeHammer.instance.getIsoPlayerDirty(username));
    // Go through each connection.
    for (UdpConnection conn : SledgeHammer.instance.getConnections()) {
      // If the user-name on the UdpConnection instance matches,
      if (conn.username != null && conn.username.equalsIgnoreCase(username)) {
        // Set this connection as the instance of the Player.
        setConnection(conn);
        // Break out of the loop to save computation time.
        break;
      }
    }
    setPosition(new Vector3f(0, 0, 0));
    setMetaPosition(new Vector2f(0, 0));
  }

  /**
   * Offline Player constructor.
   *
   * @param mongoDocument The MongoPlayer database object.
   */
  public Player(MongoPlayer mongoDocument) {
    super(mongoDocument, "Player");
    setUsername(mongoDocument.getUsername());
  }

  @Override
  public void onLoad(KahluaTable table) {
    // (Note: Players will only be authored by the server.)
    throw new IllegalStateException("Player objects cannot be loaded from Lua.");
  }

  @Override
  public void onExport() {
    int nativeId = -1;

    IsoPlayer iso = getIso();
    if (iso != null) {
      nativeId = iso.getOnlineID();
    }

    // @formatter:off
    set("id", getUniqueId().toString());
    set("native_id", nativeId);
    set("username", getUsername());
    set("nickname", getNickname());
    set("color", getColor());
    // @formatter:on
  }

  @Override
  public void setMongoDocument(MongoPlayer mongoPlayer) {
    if (mongoPlayer == null) return;
    super.setMongoDocument(mongoPlayer);
    if (!isCreated()) {
      if (SledgeHammer.instance.isStarted()) {
        PlayerCreatedEvent event = new PlayerCreatedEvent(this);
        SledgeHammer.instance.handle(event);
        setCreated(true);
      }
    }
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
    return other instanceof Player && ((Player) other).getUniqueId().equals(getUniqueId());
  }

  @Override
  public String toString() {
    return getName();
  }

  /** Initializes the Player. */
  public void init() {
    setPosition(new Vector3f(0, 0, 0));
    setMetaPosition(new Vector2f(0, 0));
    if (!hasInitialized()) {
      IsoPlayer iso = getIso();
      if (iso != null) {
        setUsername(iso.getUsername());
      }
      if (getUsername() == null) {
        setUsername(connection.username);
      }
      initProperties();
      setInitialized(true);
    }
  }

  /** Initializes the Player's meta-data properties. */
  public void initProperties() {
    if (getProperty("muteglobal") == null) {
      setProperty("muteglobal", "0");
    }
    if (getProperty("alive") == null || getProperty("alive").equalsIgnoreCase("0")) {
      this.isNewCharacter = true;
      this.isAlive = false;
    }
  }

  /** Updates the Player in core Module for the Core plug-in. */
  public void update() {
    SledgeHammer.instance.updatePlayer(this);
  }

  /**
   * Sends a given ChatMessage to the Player on the ChatChannel defined.
   *
   * <p>If a ChatChannel's Id is not defined in the ChatMessage, an IllegalArgumentException is
   * thrown.
   *
   * <p>If a ChatChanel is not found with the defined Id in the ChatMessage, an
   * IllegalArgumentException is thrown.
   *
   * @param message The ChatMessage being sent to the Player.
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
      throw new IllegalArgumentException(
          "ChatMessage given does not have a defined ChatChannel Id.");
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
    channel.sendChatMessageDirect(message, this);
  }

  /**
   * Sends a given ChatMessage to the PLayer in all available ChatChannels.
   *
   * @param chatMessage The ChatMessage to send.
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
   * Sends a ChatMessage with provided String content to the Player.
   *
   * @param message The String message content to send.
   */
  public void sendChatMessage(String message) {
    // Create the ChatMessage using the message content.
    ChatMessage chatMessage =
        getChatModule().createChatMessage(message.replaceAll("\n", " <LINE> "));
    ChatChannel chatChannel = getChatModule().getGlobalChatChannel();
    chatMessage.setChannelId(chatChannel.getUniqueId(), false);
    sendChatMessage(chatMessage);
  }

  /**
   * Sends ChatMessages to a Player, with a given String Array of lines.
   *
   * @param lines The String Array of messages to send.
   */
  public void sendChatMessages(String[] lines) {
    for (String line : lines) {
      sendChatMessage(line);
    }
  }

  /**
   * @param other The Player to measure.
   * @return Returns true if the Player is less than or equal to the defined range in the PZ server
   *     for local chat.
   */
  public boolean isWithinLocalRange(Player other) {
    if (other.equals(this)) {
      return true;
    }
    if (isConnected() && other.isConnected()) {
      IsoPlayer isoOther = other.getIso();
      return getConnection().ReleventTo(isoOther.x, isoOther.y);
    }
    return false;
  }

  /** @return Returns true if the Player is currently on the server. */
  public boolean isOnline() {
    return connection != null && connection.connected;
  }

  /**
   * @param permissionNodes The command's permission nodes.
   * @return Returns true if the Player is granted the given String node permission. If the Player
   *     is an administrator, this method will always return true. To grab the raw permission, use
   *     'hasRawPermission(String node)...'.
   */
  public boolean hasPermission(String... permissionNodes) {
    return hasPermission(false, permissionNodes);
  }

  /**
   * Validates if a commanding player is granted the permission node for the command. Only one
   * permission node has to grant permission for this method to return true.
   *
   * @param ignoreAdmin Boolean flag for ignoring Administrator check.
   * @param permissionNodes The command's permission nodes.
   * @return Returns true if the commanding player is granted the command's permission node.
   */
  public boolean hasPermission(boolean ignoreAdmin, String... permissionNodes) {
    boolean returned = false;
    boolean isDefault = false;
    for (String permissionNode : permissionNodes) {
      boolean inverted = permissionNode.startsWith("!");
      if (inverted) {
        permissionNode = permissionNode.substring(1, permissionNode.length());
      }
      // If the permission node given is the default wildcard, this will always return true.
      if (permissionNode.equals("*")) {
        returned = true;
      }
      // Check if the permission node defines a specific group.
      else if (permissionNode.startsWith("group:")) {
        PermissionGroup permissionGroup;
        String groupName = permissionNode.split(":")[1].trim();
        // If the permission group defined is the default permission group, set the permission
        // group to be the default permission group. This will still be evaluated as players can be
        // assigned to other groups than default.
        if (groupName.equals("default")) {
          isDefault = true;
          permissionGroup = SledgeHammer.instance.getDefaultPermissionGroup();
        }
        // Attempt to grab the defined permission group.
        else {
          permissionGroup = SledgeHammer.instance.getPermissionGroup(groupName);
        }
        // Check to see if the permission group is defined.
        if (permissionGroup != null) {
          // Check to see if the permission user is also defined.
          PermissionUser permissionUser = getPermissionUser();
          if (permissionUser != null) {
            PermissionGroup groupUser = permissionUser.getPermissionGroup();
            if (groupUser.equals(permissionGroup)) {
              returned = true;
            }
          }
          // If the permission user is not defined and the group defined is default, then this will
          // automatically return true.
          else if (isDefault) {
            returned = true;
          }
        }
      }
      // If no permission group syntax is provided, check the given string as true.
      else {
        returned =
            !ignoreAdmin && isAdministrator()
                || SledgeHammer.instance.hasPermission(this, permissionNode);
      }
      // If the permission node is inverted, then flip the result.
      if (inverted) {
        returned = !returned;
      }
      // If the permission node is granted to the commanding player, then break. We only need one
      // to return true.
      if (returned) {
        break;
      }
    }
    return returned;
  }

  /**
   * Sets a String permission-node for a Player with the explicitly-defined Boolean flag.
   *
   * @param node The String permission-node to set.
   * @param flag The Boolean flag to set. If set to null, the node is removed from the
   *     PermissionUser.
   */
  public void setPermission(String node, Boolean flag) {
    SledgeHammer.instance.setPermission(this, node, flag);
  }

  /**
   * @param username The String user-name to test. (Non-Case-Sensitive)
   * @return Returns true if the given String user-name matches the one for the Player.
   */
  public boolean isUsername(String username) {
    return getUsername().equalsIgnoreCase(username);
  }

  /**
   * @param usernameFragment The String user-name to test. (Non-Case-Sensitive)
   * @return Returns true if the String user-name of the Player contains the String user-name
   *     fragment given.
   */
  public boolean isUsernameDirty(String usernameFragment) {
    return getUsername().toLowerCase().contains(usernameFragment.toLowerCase());
  }

  /**
   * @param nickname The String nickname to test. (Non-Case-Sensitive)
   * @return Returns true if the given String nickname matches the one for the Player.
   */
  public boolean isNickname(String nickname) {
    return getNickname().equalsIgnoreCase(nickname);
  }

  /**
   * @param nicknameFragment The String nickname fragment to test. (Non-Case-Sensitive)
   * @return Returns true if the String nickname of the Player contains the String nickname fragment
   *     given.
   */
  public boolean isNicknameDirty(String nicknameFragment) {
    return getNickname().toLowerCase().contains(nicknameFragment);
  }

  /**
   * @param name The String user-name or nickname to test. (Non-Case Sensitive)
   * @return Returns true if the given String user-name or nickname matches the one for the Player.
   *     The String user-name is checked first. The String nickname is checked second.
   */
  public boolean isName(String name) {
    return isUsername(name) || isNickname(name);
  }

  /**
   * @param nameFragment The String user-name or nickname fragment to test. (Non-Case-Sensitive)
   * @return Returns true if the String user-name or String nick-name of the Player contains the
   *     String name fragment given. The user-name is checked first. The nickname is checked second.
   *     strict comparisons are checked before dirty comparisons.
   */
  public boolean isNameDirty(String nameFragment) {
    return isUsername(nameFragment)
        || isNickname(nameFragment)
        || isUsernameDirty(nameFragment)
        || isNicknameDirty(nameFragment);
  }

  /** @return Returns true if the Player is flagged as an Administrator with full privileges. */
  public boolean isAdministrator() {
    return getIso() == null
        ? username.equalsIgnoreCase("admin")
        : getIso().accessLevel.equals("admin");
  }

  /**
   * Sets a given meta-data String property value with a String key to identify. This change is
   * automatically saved to the MongoDB database.
   *
   * @param property The String key to identify.
   * @param content The String value to set.
   * @return Returns the old String property value if this operation replaces a previous value.
   */
  public String setProperty(String property, String content) {
    return setProperty(property, content, true);
  }

  /**
   * Sets a given meta-data String property value with a String key to identify.
   *
   * @param property The String key to identify.
   * @param content The String value to set.
   * @param save Flag to save the MongoDocument after applying the changes.
   * @return Returns the old String property value if this operation replaces a previous value.
   */
  public String setProperty(String property, String content, boolean save) {
    String propertyOld = getProperty(property);
    getMongoDocument().setMetaData(property, content, save);
    return propertyOld;
  }

  /**
   * @param property The String key to identify the meta-data property definition.
   * @return Returns the String definition of a meta-data property. If no property is defined under
   *     the String key given, null is returned.
   */
  public String getProperty(String property) {
    return getMongoDocument().getMetaData(property);
  }

  /**
   * Sets the native IsoPlayer Object for the Player.
   *
   * @param iso The native IsoPlayer Object to set.
   */
  public void set(IsoPlayer iso) {
    this.iso = iso;
  }

  /** @return Returns true if the Player is a new account. */
  public boolean isNewAccount() {
    return isNewAccount;
  }

  /**
   * Sets the Boolean flag for the Player account being new.
   *
   * @param flag The flag to set.
   */
  public void setNewAccount(boolean flag) {
    this.isNewAccount = flag;
  }

  /** @return Returns true if the Player's in-game character is a new character. */
  public boolean isNewCharacter() {
    return isNewCharacter;
  }

  /**
   * Sets the Boolean flag for the Player's in-game character being a new character.
   *
   * @param flag The flag to set.
   */
  public void setNewCharacter(boolean flag) {
    this.isNewCharacter = flag;
  }

  /**
   * (Private Method)
   *
   * <p>Approximate method for 'SledgeHammer.instance.getChatModule()'.
   *
   * @return Returns the chat Module instance.
   */
  private ModuleChat getChatModule() {
    return SledgeHammer.instance.getChatModule();
  }

  /** @return Returns true if the Player is connected to the PZ server. */
  public boolean isConnected() {
    return connection != null && connection.connected;
  }

  /**
   * @return Returns true if the Player has successfully logged into the PZ server and is still
   *     connected.
   */
  public boolean isInGame() {
    return isConnected() && connection.isFullyConnected();
  }

  /** @return Returns true if the Player's in-game character is alive. */
  public boolean isAlive() {
    return this.isAlive;
  }

  /**
   * Sets the Boolean Alive flag for the Player Object.
   *
   * <p>FIXME: Possible condition bug with not setting alive property.
   *
   * @param flag The flag to set.
   */
  public void setAlive(boolean flag) {
    if (isAlive() && !flag) {
      this.isAlive = false;
      setProperty("alive", "0");
      DeathEvent event = new DeathEvent(this);
      Boolean announce = ServerOptions.instance.getBoolean("AnnounceDeath");
      if (announce != null) {
        event.announce(announce);
      }
      SledgeHammer.instance.handle(event);
      sinceDeath = System.currentTimeMillis();
    }
    // Asynchronous protection against flips between alive and death states.
    if (!isAlive() && flag && (System.currentTimeMillis() - sinceDeath) > 5000L) {
      this.isAlive = true;
      setProperty("alive", "1");
      AliveEvent event = new AliveEvent(this);
      SledgeHammer.instance.handle(event);
    }
  }

  /** @return Returns true if the Player Object has initialized. */
  public boolean hasInitialized() {
    return this.initialized;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the Boolean flag for the Player Object being initialized.
   *
   * @param flag The flag to set.
   */
  private void setInitialized(boolean flag) {
    this.initialized = flag;
  }

  /**
   * @return Returns true if the Player account has passed a PlayerCreatedEvent to the Sledgehammer
   *     engine.
   */
  public boolean isCreated() {
    return this.created;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the Boolean flag for the Player Object is created and a PlayerCreatedEvent has passed
   * through the Sledgehammer engine.
   *
   * @param flag The Boolean flag to set.
   */
  private void setCreated(boolean flag) {
    this.created = flag;
  }

  /**
   * @return Returns a meta-data property Map with String keys and String values set arbitrarily by
   *     Modules.
   */
  public Map<String, String> getProperties() {
    return mapProperties;
  }

  /**
   * Sets the entire meta-data property Map with String keys and String values for the Player.
   *
   * @param mapProperties The Map to set.
   */
  public void setProperties(Map<String, String> mapProperties) {
    this.mapProperties = mapProperties;
  }

  /** @return Returns the Color LuaTable representing the Player. */
  public Color getColor() {
    return this.color;
  }

  /**
   * Sets the Color LuaTable representing the Player.
   *
   * @param color The Color LuaTable to set.
   */
  public void setColor(Color color) {
    this.color = color;
  }

  /** @return Returns the Vector3f meta-position of the Player. */
  public Vector2f getMetaPosition() {
    return this.positionMeta;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the Vector2f meta-position of the Player.
   *
   * @param positionMeta The Vector2f to set.
   */
  private void setMetaPosition(Vector2f positionMeta) {
    this.positionMeta = positionMeta;
  }

  /** @return Returns the Vector3f position of the Player. */
  public Vector3f getPosition() {
    return this.position;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the Vector3f position of the Player.
   *
   * @param position The Vector3f to set.
   */
  private void setPosition(Vector3f position) {
    this.position = position;
  }

  /**
   * @return Returns the set String nickname of the Player. If a nickname is not defined, the
   *     Player's user-name is returned instead.
   */
  public String getNickname() {
    return this.nickname != null ? this.nickname : getUsername();
  }

  /**
   * Sets the String nickname to represent the Player.
   *
   * @param nickname The String nickname to set.
   */
  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  /** @return Returns the String account user-name the Player registered for the PZ server. */
  public String getUsername() {
    return this.username;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the String user-name of the Player.
   *
   * @param username The String user-name to set.
   */
  private void setUsername(String username) {
    this.username = username;
  }

  /** @return Returns the native IsoPlayer Object of the Player in the PZ server. */
  public IsoPlayer getIso() {
    return this.iso;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the native IsoPlayer Object for the Player.
   *
   * @param iso The native IsoPlayer Object to set.
   */
  private void setIso(IsoPlayer iso) {
    this.iso = iso;
  }

  /**
   * @return Returns the native UdpConnection Object for the Player, if the Player is connected to
   *     the server.
   */
  public UdpConnection getConnection() {
    if (this.connection == null) {
      setConnection(findConnection(getUsername()));
    }
    return this.connection;
  }

  /**
   * Sets the native UdpConnection Object for the Player.
   *
   * @param connection The native UdpConnection Object to set.
   */
  public void setConnection(UdpConnection connection) {
    this.connection = connection;
  }

  /**
   * @return Returns the Unique ID of the the Player. This is the Unique ID that identifies the
   *     Player in MongoDB, as well as any additional data storage for Modules.
   */
  public UUID getUniqueId() {
    return getMongoDocument().getUniqueId();
  }

  /** @return Returns the Language set for messages. */
  public Language getLanguage() {
    return this.language;
  }

  /**
   * Sets the Language for messages.
   *
   * @param language The Language to set.
   */
  public void setLanguage(Language language) {
    this.language = language;
  }

  public void setAdministrator(boolean flag, boolean save) {
    getMongoDocument().setAdministrator(flag, save);
  }

  /**
   * @param username The String user-name to identify the UdpConnection.
   * @return Returns a UdpConnection with the given String user-name. If no UdpConnection uses the
   *     given String user-name, null is returned.
   */
  public static UdpConnection findConnection(String username) {
    UdpConnection returned = null;
    for (UdpConnection connectionNext : SledgeHammer.instance.getConnections()) {
      if (connectionNext.username.equalsIgnoreCase(username)) {
        returned = connectionNext;
        break;
      }
    }
    return returned;
  }

  public PermissionUser getPermissionUser() {
    return SledgeHammer.instance.getPermissionUser(this);
  }

  public PermissionUser createPermissionUser() {
    PermissionUser permissionUser = getPermissionUser();
    if (permissionUser != null) {
      return permissionUser;
    }

    return SledgeHammer.instance.createPermissionUser(this);
  }

  static {
    admin = new Player(SledgeHammer.instance.getSettings().getAdministratorPassword(), false);
  }
}
