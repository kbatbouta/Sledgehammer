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
package sledgehammer;

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

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.database.module.core.MongoPlayer;
import sledgehammer.database.module.core.SledgehammerDatabase;
import sledgehammer.event.CommandEvent;
import sledgehammer.event.Event;
import sledgehammer.event.PlayerCreatedEvent;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.interfaces.EventListener;
import sledgehammer.interfaces.ThrowableListener;
import sledgehammer.interfaces.LogEventListener;
import sledgehammer.interfaces.PermissionListener;
import sledgehammer.lua.LuaTable;
import sledgehammer.lua.Send;
import sledgehammer.lua.core.Player;
import sledgehammer.manager.EventManager;
import sledgehammer.manager.NPCManager;
import sledgehammer.manager.PlayerManager;
import sledgehammer.manager.PluginManager;
import sledgehammer.module.chat.ModuleChat;
import sledgehammer.module.core.ModuleCore;
import sledgehammer.module.faction.ModuleFactions;
import sledgehammer.module.permissions.ModulePermissions;
import sledgehammer.module.vanilla.ModuleVanilla;
import sledgehammer.util.Command;
import sledgehammer.util.Printable;
import zombie.GameWindow;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.core.raknet.UdpEngine;
import zombie.network.GameServer;
import zombie.network.ServerOptions;
import zombie.sledgehammer.SledgeHelper;

/**
 * Main class of operations for SledgeHammer.
 * 
 * This class is used to initialize all components of SledgeHammer, and act as a
 * approximation to access most components in SledgeHammer.
 * 
 * @author Jab
 */
public class SledgeHammer extends Printable {

	/** Singleton instance of the SledgeHammer engine. */
	public static SledgeHammer instance;
	/** The version of SledgeHammer release. */
	public static final String VERSION = "4.0";
	/** Debug boolean for the SledgeHammer engine. Used for verbose output. */
	public static boolean DEBUG = false;
	/**
	 * Boolean to load SledgeHammer for testing a module, without ProjectZomboid
	 * code being invoked directly. Used for Module test classes.
	 */
	public static boolean TESTMODULE = false;

	/** The MongoDB Database instance for the SledgeHammer instance. */
	private SledgehammerDatabase database;
	/** Manager instance to handle NPC operations. */
	private NPCManager managerNPC;
	/** Manager instance to handle Plug-in operations. */
	private PluginManager managerPlugin;
	/** Manager to handle Events. */
	private EventManager managerEvent;
	/** Manager to handle logging of Players and Player data. */
	private PlayerManager managerPlayer;
	/**
	 * UdpEngine pointer for the ProjectZomboid GameServer UdpEngine instance, to
	 * communicate with connections.
	 */
	private UdpEngine udpEngine;
	/** The <PermissionListener> used to query permission requests. */
	private PermissionListener permissionListener;
	/** The name of the server running SledgeHammer. */
	private String publicServerName;
	/** Flag for whether or not the SledgeHammer instance has started. */
	private boolean started = false;

	/**
	 * Test-Case constructor. Use this constructor for testing a Module.
	 * 
	 * @param debug
	 */
	public SledgeHammer(boolean debug) {
		// Sets verbose debug mode.
		DEBUG = debug;
		// Sets TESTMODULE to true, in order to properly load SledgeHammer without
		// ProjectZomboid.
		TESTMODULE = true;
		Settings.getInstance();
	}

	/**
	 * Main constructor.
	 */
	public SledgeHammer() {
		new File("plugins" + File.separator).mkdirs();
		Settings.getInstance();
	}

	@Override
	public String getName() {
		return "SledgeHammer";
	}

	/**
	 * Initializes the SledgeHammer engine.
	 */
	public void init() {
		try {
			publicServerName = ServerOptions.instance.getOption("PublicName");
			// Initialize the Chat Engine.
			managerEvent = new EventManager();
			managerPlugin = new PluginManager();
			managerPlayer = new PlayerManager();
			// Initialize the NPC Engine.
			managerNPC = new NPCManager();
			// Then, load the core modules, and start the Modules.
			if (!TESTMODULE) {
				managerPlugin.onLoad(false);
			}
		} catch (Exception e) {
			stackTrace("An Error occured while initializing Sledgehammer.", e);
		}
	}

	/**
	 * Starts the SledgeHammer framework.
	 */
	public void start() {
		getPluginManager().onStart();
		getPlayerManager().onStart();
		for (Player player : getPlayers()) {
			PlayerCreatedEvent event = new PlayerCreatedEvent(player);
			SledgeHammer.instance.handle(event);
		}
		started = true;
	}

	/**
	 * Main update method for SledgeHammer components.
	 */
	public void update() {
		try {
			synchronized (this) {
				managerPlugin.onUpdate();
				managerNPC.onUpdate();
			}
		} catch (Exception e) {
			stackTrace("An Error occured in Sledgehammer's update method.", e);
		}
	}

	/**
	 * Stops all SledgeHammer components.
	 */
	public void stop() {
		try {
			synchronized (this) {
				managerPlugin.onShutDown();
				managerPlayer.onShutDown();
				getDatabase().shutDown();
			}
		} catch (Exception e) {
			stackTrace("An Error occured while stopping Sledgehammer.", e);
		}
		started = false;
	}

	/**
	 * Checks if a <Player> has a <String> permission-node.
	 * 
	 * @param player
	 *            The <Player> to test.
	 * @param node
	 *            The <String> permission-node to test.
	 * @return Returns true if the <Player> is granted the given <String>
	 *         permission-node.
	 */
	public boolean hasPermission(Player player, String node) {
		// Validate the Player argument.
		if (player == null) {
			throw new IllegalArgumentException("Player given is null");
		}
		// Validate the node argument.
		if (node == null || node.isEmpty()) {
			throw new IllegalArgumentException("Node given is null or empty.");
		}
		// Format the node.
		node = node.toLowerCase();
		// The flag to return.
		boolean returned = false;
		if (hasPermissionListener()) {
			PermissionListener permissionListener = getPermissionListener();
			try {
				returned = permissionListener.hasPermission(player, node);
				if (!returned) {
					returned = permissionListener.hasDefaultPermission(node);
				}
			} catch (Exception e) {
				errorln("The assigned PermissionListener failed to execute properly.");
				if (DEBUG) {
					e.printStackTrace();
				}
			}
		} else {
			throw new IllegalStateException(
					"No PermissionHandlers are registered for SledgeHammer, so permissions cannot be tested.");
		}
		// If no permissions handler identified as true, return false.
		return returned;
	}

	/**
	 * @return Returns the <String> message to send when a permission is denied.
	 */
	public String getPermissionDeniedMessage() {
		return Settings.getInstance().getPermissionDeniedMessage();
	}

	/**
	 * Adds a <String> permission-node to the default <PermissionGroup>, allowing
	 * all <Player>'s to be granted the permission-node.
	 * 
	 * @param node
	 *            The <String> node to add.
	 */
	public void addDefaultPermission(String node) {
		addDefaultPermission(node, true);
	}

	/**
	 * Adds a <String> permission-node to the default <PermissionGroup> with a given
	 * <Boolean> flag. All <PermissionUser>'s with a specific definition will
	 * override this.
	 * 
	 * @param node
	 *            The <String> node to add.
	 * @param flag
	 *            The <Boolean> flag to set.
	 */
	public void addDefaultPermission(String node, boolean flag) {
		getPermissionListener().addDefaultPermission(node, flag);
	}

	private PermissionListener getPermissionListener() {
		return this.permissionListener;
	}

	private boolean hasPermissionListener() {
		return this.permissionListener != null;
	}

	public void setPermission(Player player, String node, boolean flag) {
		// Validate the Player argument.
		if (player == null) {
			throw new IllegalArgumentException("Player given is null");
		}
		// Validate the node argument.
		if (node == null || node.isEmpty()) {
			throw new IllegalArgumentException("Node given is null or empty.");
		}
		// Format the node.
		node = node.toLowerCase();
		if (hasPermissionListener()) {
			getPermissionListener().setPermission(player, node, flag);
		} else {
			throw new IllegalStateException(
					"No PermissionHandlers are registered for SledgeHammer, so permissions cannot be set.");
		}
	}

	/**
	 * Returns a <Player> with a given nickname or user-name, or a fragment of that
	 * name. User-names are checked before Nicknames are checked.
	 * 
	 * @param nameFragment
	 *            The <String> name or fragment of the of the user-name or nickname
	 *            of the Player.
	 * @return Returns a <Player>. If a <Player> is not identified with the given
	 *         <String> fragment, null is returned.
	 */
	public Player getPlayerDirty(String nameFragment) {
		// Search by user-name.
		Player player = getPlayerByUsername(nameFragment);
		// Search by nickname.
		if (player == null) {
			player = getPlayerByNickname(nameFragment);
		}
		// Search dirty for user-name.
		if (player == null) {
			for (Player nextPlayer : getPlayers()) {
				if (nextPlayer.getUsername().toLowerCase().contains(nameFragment.toLowerCase())) {
					player = nextPlayer;
					break;
				}
			}
		}
		// Search dirty for nickname.
		if (player == null) {
			// Go through each Player.
			for (Player playerNext : getPlayers()) {
				// Grab and format the next Player's nickname.
				String playerNextNickname = playerNext.getNickname().toLowerCase();
				// If the next Player in the list contains part of the
				if (playerNextNickname.contains(nameFragment.toLowerCase())) {
					player = playerNext;
					break;
				}
			}
		}
		// Return the result Player.
		return player;
	}

	/**
	 * Sends a Send LuaTable Object to a given Player.
	 * 
	 * @param send
	 *            The sent <LuaObject>.
	 * @param player
	 *            The <Player> the <Send> is sent to.
	 */
	public void send(Send send, Player player) {
		// Make sure the Player is online before attempting to send to the Player.
		if (player.isConnected()) {
			if (DEBUG) {
				println("Sending to player: " + player + ", send=" + send);
			}
			// Send the packet using the native packet code.
			GameServer.sendServerCommand("sledgehammer.module." + send.getModule(), send.getCommand(), send.export(),
					player.getConnection());
		}
	}

	/**
	 * @param nickname
	 *            The <String> nickname of the <Player>.
	 * @return Returns a <Player> with a given <String> nickname. If no online
	 *         Players have this nickname, then null is returned.
	 */
	public Player getPlayerByNickname(String nickname) {
		// The Player to returned.
		Player returned = null;
		// Go through each online Player.
		for (Player player : getPlayers()) {
			if (player.getNickname().equalsIgnoreCase(nickname)) {
				// Set the returned Player and break out of the loop to save computation.
				returned = player;
				break;
			}
		}
		// Return the result.
		return returned;
	}

	/**
	 * @param username
	 *            The <String> user-name of the <Player>.
	 * @return Returns a <Player> with a given <String> user-name. If no online
	 *         Players have this nickname, then null is returned.
	 */
	public Player getPlayerByUsername(String username) {
		// The Player to return.
		Player returned = null;
		// Go through each online Player.
		for (Player player : getPlayers()) {
			// If the Player's registered username matches the one given,
			if (player.getUsername().equalsIgnoreCase(username)) {
				// Set the returned Player and break out of the loop to save computation.
				returned = player;
				break;
			}
		}
		// Return the result.
		return returned;
	}

	/**
	 * Returns a non-cached <Player> object to represent an Offline player.
	 * 
	 * @param playerId
	 *            The <UUID> identifier of the Player.
	 * @return Returns a <Player> object if the player exists in the database.
	 *         Returns null if the Player does not exist.
	 */
	public Player getOfflinePlayer(UUID playerId) {
		Player player = null;
		MongoPlayer mongoPlayer = getDatabase().getMongoPlayer(playerId);
		if (mongoPlayer != null) {
			player = new Player(mongoPlayer);
		}
		return player;
	}

	/**
	 * Returns a non-cached <Player> object to represent an Offline player.
	 * 
	 * @param username
	 *            The <String> user-name of the Player.
	 * @return Returns a <Player> object if the player exists in the database.
	 *         Returns null if the Player does not exist.
	 */
	public Player getOfflinePlayer(String username) {
		Player player = null;
		MongoPlayer mongoPlayer = getDatabase().getMongoPlayer(username);
		if (mongoPlayer != null) {
			player = new Player(mongoPlayer);
		}
		return player;
	}

	public SledgehammerDatabase getDatabase() {
		if (this.database == null) {
			database = new SledgehammerDatabase();
			database.connect(SledgehammerDatabase.getConnectionURL());
		}
		return this.database;
	}

	public void register(CommandListener listener) {
		for (String command : listener.getCommands()) {
			getEventManager().register(command, listener);
		}
	}

	/**
	 * Updates the score-board for every player that is online.
	 */
	public void updateScoreboard() {
		for (UdpConnection connection : getConnections()) {
			GameServer.scoreboard(connection);
		}
	}

	/**
	 * Sends a Send LuaTable Object to online players.
	 * 
	 * @param send
	 *            The <Send> LuaTable Object being sent.
	 */
	public void send(Send send) {
		for (Player player : getPlayers()) {
			send(send, player);
		}
	}

	/**
	 * Sends a <Send> LuaTable Object to the given <Collection> of <Player>'s.
	 * 
	 * @param send
	 *            The <Send> LuaTable Object being sent.
	 * @param players
	 *            The <Collection> of <Player>'s being sent the <Send> Object.
	 */
	public void send(Send send, Collection<Player> players) {
		for (Player player : players) {
			send(send, player);
		}
	}

	/**
	 * Reloads SledgeHammer entirely.
	 */
	public void reload() {
		stop();
		init();
	}

	/**
	 * @return Returns the <PluginManager> instance for SledgeHammer.
	 */
	public PluginManager getPluginManager() {
		return this.managerPlugin;
	}

	/**
	 * @return Returns the <NPCManager> instance.
	 */
	public NPCManager getNPCManager() {
		return this.managerNPC;
	}

	/**
	 * @return Returns the <EventManager> instance.
	 */
	public EventManager getEventManager() {
		return managerEvent;
	}

	/**
	 * @return Returns the <PlayerManager> instance.
	 */
	public PlayerManager getPlayerManager() {
		return managerPlayer;
	}

	/**
	 * @return Returns the <ModuleChat> instance loaded in the Core plug-in.
	 */
	public ModuleFactions getFactionModule() {
		return getPluginManager().getFactionsModule();
	}

	/**
	 * @return Returns the <ModuleChat> instance loaded in the Core plug-in.
	 */
	public ModuleChat getChatModule() {
		return getPluginManager().getChatModule();
	}

	/**
	 * @return Returns the <ModuleCore> instance loaded in the Core plug-in.
	 */
	public ModuleVanilla getVanillaModule() {
		return getPluginManager().getVanillaModule();
	}

	/**
	 * @return Returns the <ModuleCore> instance loaded in the Core plug-in.
	 */
	public ModuleCore getCoreModule() {
		return getPluginManager().getCoreModule();
	}

	/**
	 * @return Returns the <ModulePermissions> instance loaded in the Core plug-in.
	 */
	public ModulePermissions getPermissionsModule() {
		return getPluginManager().getPermissionsModule();
	}

	/**
	 * @return Returns the cache folder for SledgeHammer data, settings, and
	 *         plug-ins.
	 */
	public static String getCacheFolder() {
		return GameWindow.getCacheDir() + File.separator + "Server" + File.separator + "SledgeHammer";
	}

	/**
	 * @return Returns the Public Server's name that is using SledgeHammer.
	 */
	public String getPublicServerName() {
		return publicServerName;
	}

	/**
	 * @return Returns the list of UdpConnections on the Server.
	 */
	@SuppressWarnings("unchecked")
	public List<UdpConnection> getConnections() {
		return getUdpEngine().getConnections();
	}

	/**
	 * @return Returns Project Zomboid's UdpEngine instance.
	 */
	public UdpEngine getUdpEngine() {
		return this.udpEngine;
	}

	/**
	 * @return Returns the Settings instance for SledgeHammer.
	 */
	public Settings getSettings() {
		return Settings.getInstance();
	}

	/**
	 * @return Returns whether or not the SledgeHammer framework has started
	 *         operations.
	 */
	public boolean isStarted() {
		return this.started;
	}

	/**
	 * Registers an EventListener interface, with a Event ID, given as a String.
	 * 
	 * @param type
	 * @param listener
	 */
	public void register(String type, EventListener listener) {
		getEventManager().register(type, listener);
	}

	/**
	 * Registers an EventListener interface, with all Event IDs listed in the
	 * interface as String[] getTypes().
	 * 
	 * @param listener
	 */
	public void register(EventListener listener) {
		getEventManager().register(listener);
	}

	/**
	 * Registers a CommandListener interface, with a command, given as a String.
	 * 
	 * @param command
	 * @param listener
	 */
	public void register(String command, CommandListener listener) {
		getEventManager().register(command, listener);
	}

	/**
	 * Registers a CommandListener interface, with a command, given as a String.
	 * 
	 * @param command
	 * @param listener
	 */
	public void register(LogEventListener listener) {
		getEventManager().register(listener);
	}

	/**
	 * Registers a ExceptionListener interface.
	 * 
	 * @param listener
	 */
	public void register(ThrowableListener listener) {
		getEventManager().register(listener);
	}

	/**
	 * Executes EventListeners from a given Event instance.
	 * 
	 * This method is a simplified version of:
	 * <code> handleEvent(event, true); </code>,
	 * 
	 * The Event is logged.
	 * 
	 * @param event
	 * @return
	 */
	public Event handle(Event event) {
		return getEventManager().handleEvent(event);
	}

	/**
	 * Executes EventListeners from a given Event instance. Logging is optional.
	 * 
	 * @param event
	 * @param logEvent
	 * @return
	 */
	public Event handle(Event event, boolean logEvent) {
		return getEventManager().handleEvent(event, logEvent);
	}

	/**
	 * Handles a given CommandEvent, by giving the UdpConnection associated with the
	 * raw input String. Logging is optional.
	 * 
	 * @param connection
	 * @param input
	 * @return
	 */
	public CommandEvent handleCommand(UdpConnection connection, String input, boolean logEvent) {
		return getEventManager().handleCommand(connection, input, logEvent);
	}

	/**
	 * Handles a given CommandEvent, with the raw input String. Logging is optional.
	 * 
	 * @param connection
	 * @param input
	 * @return
	 */
	public CommandEvent handleCommand(String input, boolean logEvent) {
		return getEventManager().handleCommand((UdpConnection) null, input, logEvent);
	}

	/**
	 * Handles a given CommandEvent, by giving the UdpConnection associated with the
	 * raw input String. The Event is logged.
	 * 
	 * @param connection
	 * @param input
	 * @return
	 */
	public CommandEvent handleCommand(UdpConnection connection, String input) {
		return getEventManager().handleCommand(connection, input, true);
	}

	/**
	 * Passes a <Command> to be handled by registered <CommandListener>'s with the
	 * Command's.
	 * 
	 * @param command
	 *            The <Command> executed.
	 * @return Returns the result <CommandEvent>.
	 */
	public CommandEvent handleCommand(Command command) {
		return getEventManager().handleCommand(new CommandEvent(command), true);
	}

	/**
	 * Sets SledgeHammer's reference to Project Zomboid's UdpEngine instance.
	 * 
	 * @param udpEngine
	 */
	public void setUdpEngine(UdpEngine udpEngine) {
		this.udpEngine = udpEngine;
	}

	/**
	 * @param name
	 *            The name of the <Player>'s native Object.
	 * @return Returns the native <IsoPlayer> Object with the <String> name of the
	 *         Player.
	 */
	public IsoPlayer getIsoPlayer(String name) {
		return SledgeHelper.getIsoPlayer(name);
	}

	/**
	 * @param nameFragment
	 *            The <String> name-fragment of the <Player>.
	 * @return Returns the native <IsoPlayer> Object of the <Player>.
	 */
	public IsoPlayer getIsoPlayerDirty(String nameFragment) {
		return SledgeHelper.getIsoPlayerDirty(nameFragment);
	}

	/**
	 * @param username
	 *            The <String> user-name of the <Player>.
	 * @return Returns the native <IsoPlayer> Object with the <String> user-name of
	 *         the <Player>.
	 */
	public IsoPlayer getIsoPlayerByUsername(String username) {
		return SledgeHelper.getIsoPlayerByUsername(username);
	}

	/**
	 * @param usernameFragment
	 *            The
	 * @return Returns the native <IsoPlayer> with a given <String>
	 *         user-name-fragment. If no Player's use-rname contains the fragment,
	 *         null is returned.
	 */
	public IsoPlayer getIsoPlayerByUsernameDirty(String usernameFragment) {
		return SledgeHelper.getIsoPlayerByUsernameDirty(usernameFragment);
	}

	/**
	 * @param nickname
	 *            The <String> nickname of the <Player>.
	 * @return Returns the native <IsoPlayer> Object for a <Player> with the given
	 *         nickname. If no Player has this nickname, null is returned.
	 */
	public IsoPlayer getIsoPlayerByNickname(String nickname) {
		return SledgeHelper.getIsoPlayerByNickname(nickname);
	}

	/**
	 * @param nicknameFragment
	 *            The <String> nickname fragment of a <Player>.
	 * @return Returns the native <IsoPlayer> Object for a <Player> with the
	 *         <String> nickname-fragment. If no Player's nickname contains the
	 *         fragment, null is returned. returned.
	 */
	public IsoPlayer getIsoPlayerByNicknameDirty(String nicknameFragment) {
		return SledgeHelper.getIsoPlayerByNicknameDirty(nicknameFragment);
	}

	/**
	 * Unregisters a <CommandListener>.
	 * 
	 * @param listener
	 *            the <CommandListener> to unregister.
	 */
	public void unregister(CommandListener listener) {
		getEventManager().unregister(listener);
	}

	/**
	 * Unregisters a <LogListener>.
	 * 
	 * @param listener
	 *            The <LogListener> to unregister.
	 */
	public void unregister(LogEventListener listener) {
		getEventManager().unregister(listener);
	}

	/**
	 * Unregisters a EventListener.
	 * 
	 * @param listener
	 *            The <EventListener> to unregister.
	 */
	public void unregister(EventListener listener) {
		getEventManager().unregister(listener);
	}

	/**
	 * Sends a Lua ServerCommand to a given Player.
	 * 
	 * @param player
	 * @param module
	 * @param command
	 * @param luaObject
	 */
	public void sendServerCommand(Player player, String module, String command, LuaTable luaObject) {
		sendServerCommand(player, module, command, luaObject.export());
	}

	/**
	 * Sends a Lua ServerCommand to a given Player.
	 * 
	 * @param player
	 * @param module
	 * @param command
	 * @param kahluaTable
	 */
	public void sendServerCommand(Player player, String module, String command, KahluaTable kahluaTable) {
		GameServer.sendServerCommand(module, command, kahluaTable, player.getConnection());
	}

	/**
	 * Sets the <PermissionListener> that handles Permission checks and assignments.
	 * 
	 * @param permissionListener
	 *            The <PermissionListener> to set.
	 */
	public void setPermissionListener(PermissionListener permissionListener) {
		this.permissionListener = permissionListener;
	}

	/**
	 * Updates a Player's Lua data.
	 * 
	 * @param player
	 *            The <Player> to update.
	 */
	public void updatePlayer(Player player) {
		getPluginManager().getCoreModule().updatePlayer(player);
	}

	/**
	 * Checks if a Player exists with a given <UUID>.
	 * 
	 * @param playerId
	 *            The <UUID> of the <Player>.
	 * @return Returns true if a <Player> exists with the given <UUID>.
	 */
	public boolean playerExists(UUID playerId) {
		return getDatabase().playerExists(playerId);
	}

	/**
	 * Adds a <Player> to the <List> of online Players.
	 * 
	 * @param player
	 *            The <Player> being added to the <PlayerManager>.
	 */
	public void addPlayer(Player player) {
		getPlayerManager().addPlayer(player);
	}

	/**
	 * @return Returns a <List> of the online <Player>'s.
	 */
	public List<Player> getPlayers() {
		return getPlayerManager().getPlayers();
	}

	/**
	 * @param username
	 *            The <String> user-name of a <Player>.
	 * @return Returns a <Player> with the given <String> user-name. If a <Player>
	 *         does not have this user-name, null is returned.
	 */
	public Player getPlayer(String username) {
		return getPlayerManager().getPlayer(username);
	}

	/**
	 * @param uniqueId
	 *            The <UUID> of a <Player>.
	 * @return Returns a <Player> with the given <UUID>. If no <Player> has this
	 *         uniqueId, null is returned.
	 */
	public Player getPlayer(UUID uniqueId) {
		return getPlayerManager().getPlayer(uniqueId);
	}

	public void broadcastMessage(String line) {
		// TODO: Implement.
	}

	/**
	 * @return Returns the path to the Sledgehammer.jar.
	 */
	public static String getJarFileLocation() {
		// The path to return.
		String path = null;
		try {
			// Grab the URI path from the Java library from the Class library.
			path = SledgeHammer.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		// Return the result path.
		return path;
	}

	/**
	 * @return Returns the Sledgehammer.Jar as a <File> Object.
	 */
	public static File getJarFile() {
		return new File(getJarFileLocation());
	}

	/**
	 * @return Returns the <Player> Object of the administrator account.
	 */
	public static Player getAdministrator() {
		return Player.admin;
	}

	/**
	 * Entry point of execution for the Sledgehammer server framework.
	 * 
	 * @param args
	 *            The Java arguments.
	 */
	public static void main(String[] args) {
		instance = new SledgeHammer();
		GameServer.main(args);
	}
}