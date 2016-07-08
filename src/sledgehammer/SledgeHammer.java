package sledgehammer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import sledgehammer.event.CommandEvent;
import sledgehammer.event.Event;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.interfaces.EventListener;
import sledgehammer.interfaces.ExceptionListener;
import sledgehammer.interfaces.LogListener;
import sledgehammer.manager.ChatManager;
import sledgehammer.manager.EventManager;
import sledgehammer.manager.ModuleManager;
import sledgehammer.manager.NPCManager;
import sledgehammer.manager.PermissionsManager;
import sledgehammer.manager.PlayerManager;
import sledgehammer.util.Printable;
import sledgehammer.wrapper.Player;
import zombie.GameWindow;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.core.raknet.UdpEngine;
import zombie.network.GameServer;
import zombie.network.ServerOptions;

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
	
	public static final String VERSION = "2.05_04";
	
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
	 * Settings instance to handle loading, and reading SledgeHammer's settings.
	 */
	private Settings settings = null;

	/**
	 * The name of the server running SledgeHammer.
	 */
	private String publicServerName;
	
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
		new File(SledgeHammer.getCacheFolder() + File.separator + "plugins" + File.separator).mkdirs();
		// Load the settings for SledgeHammer.
		loadSettings();
	}

	/**
	 * Initializes the SledgeHammer engine.
	 */
	public void init() {

		// Displays version.
		println(VERSION);
		
		try {
			
			publicServerName = ServerOptions.instance.getOption("PublicName");
			
			// Initialize the Chat Engine.
			chat = new ChatManager();
			
			managerEvent = new EventManager(this);
			
			managerPermissions = new PermissionsManager(this);
			
			// Initialize the ModuleManager.
			managerModule = new ModuleManager(this);

			managerPlayer = new PlayerManager(this);
			
			// Initialize the NPC Engine.
			managerNPC = new NPCManager(this);
			
			// Then, load the core modules, and start the Modules.
			if(!TESTMODULE) {
				managerModule.load();
			}
			
		} catch(Exception e) {
			stackTrace("An Error occured while initializing Sledgehammer.", e);
		}
		
	}

	/**
	 * Loads the SledgeHammer.ini settings in the cache folder.
	 */
	private void loadSettings() {
		println("Loading settings..");
		
		try {			
			settings = new Settings(this);
			settings.readSettings();
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
			
				managerModule.update();
				managerNPC.update();
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
			
				managerModule.shutdown();
			}
			
		} catch(Exception e) {
			
			stackTrace("An Error occured while stopping Sledgehammer.", e);
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
	public List<UdpConnection> getConnections() {
		return getUdpEngine().getConnections();
	}
	
	/**
	 * Returns the Settings instance for SledgeHammer.
	 * 
	 * @return
	 */
	public Settings getSettings() {
		return settings;
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
		getChatManager().setUdpEngine(udpEngine);
	}

	
	public IsoPlayer getIsoPlayer(String name) {
		return PlayerManager.getIsoPlayer(name);
	}
	
	public IsoPlayer getIsoPlayerDirty(String name) {
		return PlayerManager.getIsoPlayerDirty(name);
	}
	
	public IsoPlayer getIsoPlayerByUsername(String username) {
		return PlayerManager.getIsoPlayerByUsername(username);
	}
	
	public IsoPlayer getIsoPlayerByUsernameDirty(String username) {
		return PlayerManager.getIsoPlayerByUsernameDirty(username);
	}
	
	public IsoPlayer getIsoPlayerByNickname(String nickname) {
		return PlayerManager.getIsoPlayerByNickname(nickname);
	}
	
	public IsoPlayer getIsoPlayerByNicknameDirty(String nickname) {
		return PlayerManager.getIsoPlayerByNicknameDirty(nickname);
	}
	
	public Player getPlayer(int id) {
		return getPlayerManager().resolve(id);
	}
	
	public Player getPlayer(String username) {
		return getPlayerManager().getPlayerByUsername(username);
	}
	
	
	
	/**
	 * Updates the scoreboard for every player that is online.
	 */
	public void updateScoreboard() {
		for(UdpConnection connection : getConnections()) {
			GameServer.scoreboard(connection);
		}
	}
	
	
	@Override
	public String getName() { return "SledgeHammer"; }
	
	public static void main(String[] args) throws IOException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {
		instance = new SledgeHammer();
		
		
		System.setProperty("java.library.path", System.getProperty("user.dir") + File.separator + "natives");
		Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
		fieldSysPath.setAccessible(true);
		fieldSysPath.set(null, null);
		GameServer.main(args);
	}

	public void start() {
		managerModule.start();
		
	}
}