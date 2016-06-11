package sledgehammer;

import java.io.File;
import java.net.ConnectException;
import java.util.List;

import sledgehammer.event.CommandEvent;
import sledgehammer.event.Event;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.interfaces.EventListener;
import sledgehammer.interfaces.LogListener;
import sledgehammer.interfaces.MapGenerator;
import sledgehammer.module.ModuleMonitor;
import sledgehammer.module.ModuleNPC;
import sledgehammer.modules.core.ModuleCore;
import sledgehammer.modules.vanilla.ModuleVanilla;
import sledgehammer.npc.NPCManager;
import sledgehammer.util.Printable;
import zombie.GameWindow;
import zombie.core.raknet.UdpConnection;
import zombie.core.raknet.UdpEngine;
import zombie.network.ServerOptions;

public class SledgeHammer extends Printable {
	
	/**
	 * Debug boolean for the SledgeHammer engine. Used for verbose output.
	 */
	public static boolean DEBUG = false;
	
	public static boolean TESTMODULE = false;

	/**
	 * Singleton instance of the SledgeHammer engine.
	 */
	public static SledgeHammer instance;

	private NPCManager managerNPC;
	
	private ModuleManager managerModule;
	
	private PermissionsManager managerPermissions;
	
	private EventManager managerEvent;

	/**
	 * Chat instance for working with chat packets and chat filtering.
	 */
	private ChatManager chat;
	
	/**
	 * ModuleVanilla instance to communicate with vanilla commands, and handlers from the original game code.
	 * NOTE: This module's code is not accessible in respect to the proprietary nature of the game.
	 */
	private ModuleVanilla moduleVanilla;
	
	/**
	 * ModuleCore instance to handle core-level components of SledgeHammer.
	 */
	private ModuleCore moduleCore;
	
	/**
	 * UdpEngine pointer for the Project Zomboid GameServer UdpEngine instance, to communicate with connections.
	 */
	private UdpEngine udpEngine;
	
	/**
	 * Settings instance to handle loading, and reading SledgeHammer's settings.
	 */
	private Settings settings = null;

	private MapGenerator mapGenerator = null;

	private ModuleNPC moduleNPC;

	private String publicServerName;
	
	/**
	 * Main constructor. Requires UdpEngine instance from GameServer to initialize.
	 * @param udpEngine
	 */
	public SledgeHammer(UdpEngine udpEngine) {
		setUdpEngine(udpEngine);
	}
	
	public SledgeHammer(boolean debug) {
		DEBUG = debug;
		TESTMODULE = true;
	}

	/**
	 * Initializes the SledgeHammer engine.
	 */
	public void init() {

		publicServerName = ServerOptions.instance.getOption("PublicName");
		
		// Initialize the Chat Engine.
		chat = new ChatManager(udpEngine);
		
		managerEvent = new EventManager(this);
		
		managerPermissions = new PermissionsManager(this);

		// Initialize the NPC Engine.
		managerNPC = new NPCManager(this);
		
		// Initialize the ModuleManager.
		managerModule = new ModuleManager(this);
		
		// Load the settings for SledgeHammer.
		loadSettings();
		
		// Then, load the core modules, and start the Modules.
		if(!TESTMODULE) {
			loadCoreModules();
			managerModule.start();
		}
		
	}

	private void loadCoreModules() {
		// Core Modules.
		moduleVanilla = new ModuleVanilla();
		moduleCore    = new ModuleCore();
		moduleNPC     = new ModuleNPC();

		if (DEBUG) managerModule.registerModule(new ModuleMonitor());

		managerModule.registerModule(moduleVanilla);
		managerModule.registerModule(moduleCore   );
		managerModule.registerModule(moduleNPC    );
		
	}

	private void loadSettings() {
		println("Loading settings..");
		
		Settings settings = new Settings(this);
		settings.readSettings();
		
	}

	public void update() {
		synchronized (this) {
			managerModule.update();
			managerNPC.update();
		}
	}

	public void stop() {
		synchronized (this) {
			managerModule.shutdown();
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
	 * Returns Project Zomboid's UdpEngine instance.
	 * @return
	 */
	public UdpEngine getUdpEngine() {
		return this.udpEngine;
	}

	/**
	 * Sets SledgeHammer's reference to Project Zomboid's UdpEngine instance.
	 * @param udpEngine
	 */
	public void setUdpEngine(UdpEngine udpEngine) {
		this.udpEngine = udpEngine;
	}

	public ChatManager getChatManager() {
		return chat;
	}
	
	public MapGenerator getMapGenerator() {
		return this.mapGenerator;
	}
	
	public void setMapGenerator(MapGenerator mapGenerator) {
		this.mapGenerator = mapGenerator;
	}
	
	public static String getCacheFolder() {
		return GameWindow.getCacheDir() + File.separator + "Server" + File.separator + "SledgeHammer";
	}
	
	public String getPublicServerName() {
		return publicServerName;
	}

	public NPCManager getNPCEngine() {
		return this.managerNPC;
	}

	public List<UdpConnection> getConnections() {
		return getUdpEngine().getConnections();
	}
	
	public Settings getSettings() {
		return settings;
	}

	public ModuleCore getCoreModule() {
		return moduleCore;
	}
	
	public ModuleVanilla getVanillaModule() {
		return moduleVanilla;
	}

	public ModuleManager getModuleManager() {
		return managerModule;
	}
	
	public PermissionsManager getPermissionsManager() {
		return managerPermissions;
	}
	
	public EventManager getEventManager() {
		return managerEvent;
	}
	
	public void register(String command, CommandListener listener) {
		getEventManager().registerCommandListener(command, listener);
	}
	
	public void register(String type, EventListener listener) {
		getEventManager().registerEventListener(type, listener);
	}
	
	public void register(LogListener listener) {
		getEventManager().registerLogListener(listener);
	}
	
	public void register(EventListener listener) {
		getEventManager().registerEventListener(listener);
	}
	
	public Event handle(Event event) {
		return getEventManager().handleEvent(event);
	}
	
	public Event handle(Event event, boolean logEvent) {
		return getEventManager().handleEvent(event, logEvent);
	}
	
	public CommandEvent handleCommand(UdpConnection connection, String input, boolean logEvent) {
		return getEventManager().handleCommand(connection, input, logEvent);
	}
	
	public CommandEvent handleCommand(String input, boolean logEvent) {
		return getEventManager().handleCommand((UdpConnection) null, input, logEvent);
	}
	
	public CommandEvent handleCommand(UdpConnection connection, String input) {
		return getEventManager().handleCommand(connection, input, true);
	}

	@Override
	public String getName() { return "SledgeHammer"; }

}