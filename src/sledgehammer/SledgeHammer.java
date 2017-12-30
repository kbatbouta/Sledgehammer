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
import java.io.IOException;
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
import sledgehammer.interfaces.ExceptionListener;
import sledgehammer.interfaces.LogListener;
import sledgehammer.lua.LuaTable;
import sledgehammer.lua.Send;
import sledgehammer.lua.core.Player;
import sledgehammer.manager.core.EventManager;
import sledgehammer.manager.core.NPCManager;
import sledgehammer.manager.core.PermissionsManager;
import sledgehammer.manager.core.PlayerManager;
import sledgehammer.manager.core.PluginManager;
import sledgehammer.interfaces.ContextListener;
import sledgehammer.module.core.CoreContextListener;
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
 * proxy to access most components in SledgeHammer.
 * 
 * @author Jab
 */
public class SledgeHammer extends Printable {

	/**
	 * The version of SledgeHammer release.
	 */
	public static final String VERSION = "4.0";
	/**
	 * Debug boolean for the SledgeHammer engine. Used for verbose output.
	 */
	public static boolean DEBUG = false;
	/**
	 * Boolean to load SledgeHammer for testing a module, without ProjectZomboid
	 * code being invoked directly. Used for Module test classes.
	 */
	public static boolean TESTMODULE = false;
	/**
	 * Singleton instance of the SledgeHammer engine.
	 */
	public static SledgeHammer instance;
	/**
	 * The MongoDB Database instance for the SledgeHammer instance.
	 */
	private SledgehammerDatabase database;
	/**
	 * Manager instance to handle NPC operations.
	 */
	private NPCManager managerNPC;
	/**
	 * Manager instance to handle Plugin operations.
	 */
	private PluginManager managerPlugin;
	/**
	 * Manager instance to handle Permissions operations.
	 */
	private PermissionsManager managerPermissions;
	/**
	 * Manager to handle Events.
	 */
	private EventManager managerEvent;
	/**
	 * Manager to handle logging of Players and Player data.
	 */
	private PlayerManager managerPlayer;
	/**
	 * UdpEngine pointer for the Project Zomboid GameServer UdpEngine instance, to
	 * communicate with connections.
	 */
	private UdpEngine udpEngine;
	/**
	 * The name of the server running SledgeHammer.
	 */
	private String publicServerName;
	/**
	 * TODO: Document.
	 */
	private ContextListener translator;
	/**
	 * Flag for whether or not the SledgeHammer instance has started.
	 */
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
		translator = new CoreContextListener();
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
			translator = new CoreContextListener();
			publicServerName = ServerOptions.instance.getOption("PublicName");
			// Initialize the Chat Engine.
			managerEvent = new EventManager();
			managerPermissions = new PermissionsManager();
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

	public Player getPlayerDirty(String username) {
		// Search by username.
		Player player = getPlayerByUsername(username);
		// Search by nickname.
		if (player == null) {
			player = getPlayerByNickname(username);
		}
		// Search dirty for username.
		if (player == null) {
			for (Player nextPlayer : getPlayers()) {
				if (nextPlayer.getUsername().toLowerCase().contains(username.toLowerCase())) {
					player = nextPlayer;
					break;
				}
			}
		}
		// Search dirty for nickname.
		if (player == null) {
			for (Player nextPlayer : getPlayers()) {
				if (nextPlayer.getNickname().toLowerCase().contains(username.toLowerCase())) {
					player = nextPlayer;
					break;
				}
			}
		}
		return player;
	}

	/**
	 * Sends a Send LuaTable Object to a given Player.
	 * 
	 * @param send
	 * @param player
	 */
	public void send(Send send, Player player) {
		if (player.isConnected()) {
			if (DEBUG) {
				println("Sending to player: " + player + ", send=" + send);
			}
			GameServer.sendServerCommand("sledgehammer.module." + send.getModule(), send.getCommand(), send.export(),
					player.getConnection());
		}
	}

	public Player getPlayerByNickname(String nickname) {
		Player returned = null;
		for (Player player : getPlayers()) {
			if (player.getNickname().equalsIgnoreCase(nickname)) {
				returned = player;
				break;
			}
		}
		return returned;
	}

	public Player getPlayerByUsername(String username) {
		Player returned = null;
		for (Player player : getPlayers()) {
			if (player.getUsername().equalsIgnoreCase(username)) {
				returned = player;
				break;
			}
		}
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
	 *            The <String> username of the Player.
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
			getEventManager().registerCommandListener(command, listener);
		}
	}

	/**
	 * Updates the scoreboard for every player that is online.
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
	 * @return Returns the <PermissionsManager> instance.
	 */
	public PermissionsManager getPermissionsManager() {
		return managerPermissions;
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

	public void broadcastMessage(String line) {
		// TODO: Implement.
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
		getEventManager().registerEventListener(type, listener);
	}

	/**
	 * Registers an EventListener interface, with all Event IDs listed in the
	 * interface as String[] getTypes().
	 * 
	 * @param listener
	 */
	public void register(EventListener listener) {
		getEventManager().registerEventListener(listener);
	}

	/**
	 * Registers a CommandListener interface, with a command, given as a String.
	 * 
	 * @param command
	 * @param listener
	 */
	public void register(String command, CommandListener listener) {
		getEventManager().registerCommandListener(command, listener);
	}

	/**
	 * Registers a CommandListener interface, with a command, given as a String.
	 * 
	 * @param command
	 * @param listener
	 */
	public void register(LogListener listener) {
		getEventManager().registerLogListener(listener);
	}

	/**
	 * Registers a ExceptionListener interface.
	 * 
	 * @param listener
	 */
	public void register(ExceptionListener listener) {
		getEventManager().registerExceptionListener(listener);
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
	 * TODO: Document.
	 * 
	 * @param command
	 * @return
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

	public IsoPlayer getIsoPlayer(String name) {
		return SledgeHelper.getIsoPlayer(name);
	}

	public IsoPlayer getIsoPlayerDirty(String name) {
		return SledgeHelper.getIsoPlayerDirty(name);
	}

	public IsoPlayer getIsoPlayerByUsername(String username) {
		return SledgeHelper.getIsoPlayerByUsername(username);
	}

	public IsoPlayer getIsoPlayerByUsernameDirty(String username) {
		return SledgeHelper.getIsoPlayerByUsernameDirty(username);
	}

	public IsoPlayer getIsoPlayerByNickname(String nickname) {
		return SledgeHelper.getIsoPlayerByNickname(nickname);
	}

	public IsoPlayer getIsoPlayerByNicknameDirty(String nickname) {
		return SledgeHelper.getIsoPlayerByNicknameDirty(nickname);
	}

	public void unregister(CommandListener listener) {
		getEventManager().unregister(listener);
	}

	public void unregister(LogListener listener) {
		getEventManager().unregister(listener);
	}

	public void unregister(EventListener listener) {
		getEventManager().unregister(listener);
	}

	public ContextListener getStringModifier() {
		return translator;
	}

	public void setStringModifier(ContextListener stringModifier) {
		this.translator = stringModifier;
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

	public void updatePlayer(Player player) {
		getPluginManager().getCoreModule().updatePlayer(player);
	}

	public boolean playerExists(UUID playerId) {
		return getDatabase().playerExists(playerId);
	}

	public void addPlayer(Player player) {
		getPlayerManager().addPlayer(player);
	}

	public List<Player> getPlayers() {
		return getPlayerManager().getPlayers();
	}

	public Player getPlayer(String username) {
		return getPlayerManager().getPlayer(username);
	}

	public Player getPlayer(UUID uniqueId) {
		return getPlayerManager().getPlayer(uniqueId);
	}

	public static String getJarFileLocation() {
		String location = null;
		try {
			location = SledgeHammer.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return location;
	}

	public static File getJarFile() {
		return new File(getJarFileLocation());
	}

	public static Player getAdministrator() {
		return Player.admin;
	}

	public static void main(String[] args) throws IOException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {
		instance = new SledgeHammer();
		GameServer.main(args);
	}

}