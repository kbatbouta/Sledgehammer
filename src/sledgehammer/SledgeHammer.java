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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.database.MongoPlayer;
import sledgehammer.database.SledgehammerDatabase;
import sledgehammer.event.CommandEvent;
import sledgehammer.event.Event;
import sledgehammer.event.PlayerCreatedEvent;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.interfaces.EventListener;
import sledgehammer.interfaces.ExceptionListener;
import sledgehammer.interfaces.LogListener;
import sledgehammer.interfaces.ContextListener;
import sledgehammer.manager.ChatManager;
import sledgehammer.manager.EventManager;
import sledgehammer.manager.ModuleManager;
import sledgehammer.manager.NPCManager;
import sledgehammer.manager.PermissionsManager;
import sledgehammer.manager.PlayerManager;
import sledgehammer.modules.core.CoreContextListener;
import sledgehammer.object.LuaTable;
import sledgehammer.objects.Player;
import sledgehammer.objects.chat.Command;
import sledgehammer.objects.send.Send;
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
 * TODO: Add unregister proxy methods.
 * 
 * @author Jab
 */
public class SledgeHammer extends Printable {
	
	public static final String VERSION = "3.0";
	
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
	
	
	private SledgehammerDatabase database;

	/**
	 * Manager instance to handle NPC operations.
	 */
	private NPCManager managerNPC;
	
	/**
	 * Manager instance to handle Module operations.
	 */
	private ModuleManager managerModule;
	
	/**
	 * Manager instance to handle Permissions operations.
	 */
	private PermissionsManager managerPermissions;
	
	/**
	 * Manager to handle Events.
	 */
	private EventManager managerEvent;

	/**
	 * Chat instance for working with chat packets and chat filtering.
	 */
	private ChatManager chat;
	
	private PlayerManager managerPlayer;
	
	/**
	 * UdpEngine pointer for the Project Zomboid GameServer UdpEngine instance, to communicate with connections.
	 */
	private UdpEngine udpEngine;
	
	/**
	 * The name of the server running SledgeHammer.
	 */
	private String publicServerName;
	
	private ContextListener translator;
	
	private boolean firstLoad = true;
	
	private boolean started = false;
	
	/**
	 * Test-Case constructor. Use this constructor for testing a Module.
	 * 
	 * @param debug
	 */
	public SledgeHammer(boolean debug) {
		
		// Sets verbose debug mode.
		DEBUG = debug;
		
		// Sets TESTMODULE to true, in order to properly load SledgeHammer without ProjectZomboid.
		TESTMODULE = true;
	}
	
	public SledgeHammer() {
		translator = new CoreContextListener();
		
		new File("plugins" + File.separator).mkdirs();
		loadSettings();
	}

	/**
	 * Initializes the SledgeHammer engine.
	 */
	public void init() {
		
		try {
			if(!firstLoad) {				
				loadSettings();
			}
			firstLoad = false;
			
			translator = new CoreContextListener();
			
			publicServerName = ServerOptions.instance.getOption("PublicName");
			
			// Initialize the Chat Engine.
			chat = new ChatManager(this);
			
			managerEvent = new EventManager();
			
			managerPermissions = new PermissionsManager();
			
			// Initialize the ModuleManager.
			managerModule = new ModuleManager();

			managerPlayer = new PlayerManager();
			
			// Initialize the NPC Engine.
			managerNPC = new NPCManager();
			
			// Then, load the core modules, and start the Modules.
			if(!TESTMODULE) {
				managerModule.onLoad();
			}
		} catch(Exception e) {
			stackTrace("An Error occured while initializing Sledgehammer.", e);
		}
		
	}
	
	public void start() {
		getModuleManager().onStart();
		getPlayerManager().onStart();
		getChatManager().startChat();
		
		for(Player player : getPlayers()) {
			PlayerCreatedEvent event = new PlayerCreatedEvent(player);
			SledgeHammer.instance.handle(event);
		}
		
		started = true;
	}

	/**
	 * Loads the SledgeHammer.ini settings in the cache folder.
	 */
	private void loadSettings() {
		println("Loading settings..");
		
		try {			
			Settings.getInstance().readSettings();
		} catch(Exception e) {
			stackTrace("An Error occured while loading Sledgehammer's settings.", e);
		}
		
	}

	/**
	 * Main update method for SledgeHammer components.
	 */
	public void update() {
		try {
			synchronized (this) {
				managerModule.onUpdate();
				managerNPC.onUpdate();
			}
		} catch(Exception e) {
			stackTrace("An Error occured in Sledgehammer's update method.", e);
		}
	}

	/**
	 * Stops all SledgeHammer components.
	 */
	public void stop() {
		try {			
			
			synchronized (this) {
				managerModule.onShutDown();
				managerPlayer.onShutDown();
				getChatManager().stopChat();
			}
			
		} catch(Exception e) {
			
			stackTrace("An Error occured while stopping Sledgehammer.", e);
		}
		
		started = false;
	}

	/**
	 * Reloads SledgeHammer entirely.
	 */
	public void reload() {
		stop();
		init();
	}

	/**
	 * Returns the ChatManager instance.
	 * 
	 * @return
	 */
	public ChatManager getChatManager() {
		return chat;
	}
	
	/**
	 * Returns the NPCManager instance.
	 * 
	 * @return
	 */
	public NPCManager getNPCManager() {
		return this.managerNPC;
	}
	
	/**
	 * Returns the ModuleManager instance.
	 * 
	 * @return
	 */
	public ModuleManager getModuleManager() {
		return managerModule;
	}
	
	/**
	 * Returns the PermissionsManager instance.
	 * 
	 * @return
	 */
	public PermissionsManager getPermissionsManager() {
		return managerPermissions;
	}
	
	/**
	 * Returns the EventManager instance.
	 * 
	 * @return
	 */
	public EventManager getEventManager() {
		return managerEvent;
	}
	
	/**
	 * Returns the PlayerManager instance.
	 * 
	 * @return
	 */
	public PlayerManager getPlayerManager() {
		return managerPlayer;
	}
	
	/**
	 * Returns the cache folder for SledgeHammer data, settings, and plug-ins.
	 * 
	 * @return
	 */
	public static String getCacheFolder() {
		return GameWindow.getCacheDir() + File.separator + "Server" + File.separator + "SledgeHammer";
	}
	
	/**
	 * Returns the Public Server's name that is using SledgeHammer.
	 * 
	 * @return
	 */
	public String getPublicServerName() {
		return publicServerName;
	}

	/**
	 * Returns the list of UdpConnections on the Server.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UdpConnection> getConnections() {
		return getUdpEngine().getConnections();
	}
	
	/**
	 * Returns the Settings instance for SledgeHammer.
	 * 
	 * @return
	 */
	public Settings getSettings() {
		return Settings.getInstance();
	}
	
	public boolean isStarted() {
		return this.started;
	}
	
	/**
	 * Registers an EventListener interface, with a Event ID, given as a String.
	 * 
	 * @param type
	 * 
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
	
	public void register(CommandListener listener) {
		for(String command : listener.getCommands()) {
			getEventManager().registerCommandListener(command, listener);
		}
	}
	
	/**
	 * Registers a CommandListener interface, with a command, given as a String.
	 * 
	 * @param command
	 * 
	 * @param listener
	 */
	public void register(String command, CommandListener listener) {
		getEventManager().registerCommandListener(command, listener);
	}
	
	/**
	 * Registers a CommandListener interface, with a command, given as a String.
	 * 
	 * @param command
	 * 
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
	 * 
	 * @return
	 */
	public Event handle(Event event) {
		return getEventManager().handleEvent(event);
	}
	
	/**
	 * Executes EventListeners from a given Event instance. Logging is optional.
	 * 
	 * @param event
	 * 
	 * @param logEvent
	 * 
	 * @return
	 */
	public Event handle(Event event, boolean logEvent) {
		return getEventManager().handleEvent(event, logEvent);
	}
	
	/**
	 * Handles a given CommandEvent, by giving the UdpConnection associated with
	 * the raw input String. Logging is optional.
	 * 
	 * @param connection
	 * 
	 * @param input
	 * 
	 * @return
	 */
	public CommandEvent handleCommand(UdpConnection connection, String input, boolean logEvent) {
		return getEventManager().handleCommand(connection, input, logEvent);
	}
	
	/**
	 * Handles a given CommandEvent, with the raw input String. Logging is
	 * optional.
	 * 
	 * @param connection
	 * 
	 * @param input
	 * 
	 * @return
	 */
	public CommandEvent handleCommand(String input, boolean logEvent) {
		return getEventManager().handleCommand((UdpConnection) null, input, logEvent);
	}
	
	/**
	 * Handles a given CommandEvent, by giving the UdpConnection associated with
	 * the raw input String. The Event is logged.
	 * 
	 * @param connection
	 * 
	 * @param input
	 * 
	 * @return
	 */
	public CommandEvent handleCommand(UdpConnection connection, String input) {
		return getEventManager().handleCommand(connection, input, true);
	}
	
	/**
	 * TODO: Document.
	 * @param command
	 * @return 
	 */
	public CommandEvent handleCommand(Command command) {
		return getEventManager().handleCommand(new CommandEvent(command), true);
	}
	
	/**
	 * Returns Project Zomboid's UdpEngine instance.
	 * 
	 * @return
	 */
	public UdpEngine getUdpEngine() {
		return this.udpEngine;
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
	
	/**
	 * Updates the scoreboard for every player that is online.
	 */
	public void updateScoreboard() {
		for(UdpConnection connection : getConnections()) {
			GameServer.scoreboard(connection);
		}
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

	public static List<Player> listPlayers = new ArrayList<>();
	public static Map<UUID, Player> mapPlayersByID = new HashMap<>();
	public static Map<String, Player> mapPlayersByUsername = new HashMap<>();
	
	public List<Player> getPlayers() {
		return listPlayers;
	}
	
	public Player getPlayer(String username) {
		return mapPlayersByUsername.get(username.toLowerCase());
	}
	
	public Player getPlayer(UUID uniqueId) {
		return mapPlayersByID.get(uniqueId);
	}
	
	public void addPlayer(Player player) {
		if(!listPlayers.contains(player)) {			
			listPlayers.add(player);
		}
		if(!mapPlayersByID.containsKey(player.getUniqueId())) {			
			mapPlayersByID.put(player.getUniqueId(), player);
		}
		
		if(!mapPlayersByUsername.containsKey(player.getUsername().toLowerCase())) {			
			mapPlayersByUsername.put(player.getUsername().toLowerCase(), player);
		}
		
		if(DEBUG) {			
			println("Adding player: " + player + ", " + player.getUsername() + ", " + player.getUniqueId().toString() + ", " + player.getConnection());
		}
	}

	/**
	 * Sends a Lua ServerCommand to a given Player.
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
	 * @param player
	 * @param module
	 * @param command
	 * @param kahluaTable
	 */
	public void sendServerCommand(Player player, String module, String command, KahluaTable kahluaTable) {
		GameServer.sendServerCommand(module, command, kahluaTable, player.getConnection());
	}

	/**
	 * Sends a Send LuaTable Object to online players.
	 * @param send
	 * @param player
	 */
	public void send(Send send) {
		for(Player player : getPlayers()) {			
			send(send, player);
		}
	}
	
	/**
	 * Sends a Send LuaTable Object to a given Player.
	 * @param send
	 * @param player
	 */
	public void send(Send send, Player player) {
		if(player.isConnected()) {
			if(DEBUG) {				
				println("Sending to player: " + player + ", send=" + send);
			}
			GameServer.sendServerCommand("sledgehammer.module." + send.getModule(), send.getCommand(), send.export(), player.getConnection());
		}
	}

	public void updatePlayer(Player player) {
		getModuleManager().getCoreModule().updatePlayer(player);
	}
	
	public static Player getAdmin() {
		return Player.admin;
	}
	
	public Player getPlayerByNickname(String nickname) {
		for(Player player : getPlayers()) {
			if(player.getNickname().equalsIgnoreCase(nickname)) {
				return player;
			}
		}
		return null;
	}
	
	public Player getPlayerByUsername(String username) {
		for(Player player : getPlayers()) {
			if(player.getUsername().equalsIgnoreCase(username)) {
				return player;
			}
		}
		return null;
	}

	public Player getPlayerDirty(String username) {
		 //Search by username.
		Player player = getPlayerByUsername(username);
		
		// Search by nickname.
		if(player == null) {			
			player = getPlayerByNickname(username);
		}
		
		// Search dirty for username.
		if(player == null) {
			for(Player nextPlayer : getPlayers()) {
				if(nextPlayer.getUsername().toLowerCase().contains(username.toLowerCase())) {
					player = nextPlayer;
					break;
				}
			}
		}
		
		// Search dirty for nickname.
		if(player == null) {
			for(Player nextPlayer : getPlayers()) {
				if(nextPlayer.getNickname().toLowerCase().contains(username.toLowerCase())) {
					player = nextPlayer;
					break;
				}
			}
		}
		
		return player;
	}

	public SledgehammerDatabase getDatabase() {
		if(this.database == null) {
			database = new SledgehammerDatabase();
			database.connect(getSettings().getDatabaseURL());
		}
		return this.database;
	}

	public boolean playerExists(UUID playerId) {
		return getDatabase().playerExists(playerId);
	}

	/**
	 * Returns a non-cached <Player> object to represent an Offline player.
	 * @param usernameInvited The <String> username of the Player.
	 * @return Returns a <Player> object if the player exists in the database. Returns null if the Player does not exist.
	 */
	public Player getOfflinePlayer(String usernameInvited) {
		Player player = null;
		MongoPlayer mongoPlayer = getDatabase().getMongoPlayer(usernameInvited);
		if(mongoPlayer != null) {
			player = new Player(mongoPlayer);
		}
		return player;
	}
	
	@Override
	public String getName() { return "SledgeHammer"; }
	
	public static void main(String[] args) throws IOException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {
		instance = new SledgeHammer();
		GameServer.main(args);
	}

}