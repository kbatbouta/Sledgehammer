package zirc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import zirc.event.ChatEvent;
import zirc.event.CommandEvent;
import zirc.event.Event;
import zirc.event.LogEvent;
import zirc.interfaces.CommandListener;
import zirc.interfaces.EventListener;
import zirc.interfaces.LogListener;
import zirc.interfaces.PermissionHandler;
import zirc.module.Module;
import zirc.module.ModuleMonitor;
import zirc.modules.core.CoreCommandListener;
import zirc.modules.core.ModuleCore;
import zirc.modules.vanilla.ModuleVanilla;
import zirc.util.Chat;
import zirc.util.INI;
import zirc.util.Result;
import zirc.util.ZUtil;
import zirc.wrapper.NPC;
import zirc.wrapper.Player;
import zombie.GameWindow;
import zombie.core.logger.LoggerManager;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.core.raknet.UdpEngine;
import zombie.iso.Vector2;
import zombie.network.GameServer;
import zombie.network.PacketTypes;

public class ZIRC {

	/**
	 * Singleton instance of the ZIRC engine.
	 */
	public static ZIRC instance;
	
	/**
	 * The concurrent Object for synchronization.
	 */
	private static Object concurrentLock = new Object();
	
	/**
	 * Chat instance for working with chat packets and chat filtering.
	 */
	private Chat chat;

	/**
	 * List for modules.
	 */
	private List<Module> listModules;
	
	/**
	 * Map for modules, organized by their associated IDs.
	 */
	private Map<String, Module> mapModules;
	
	/**
	 * Map for registered EventListener interfaces.
	 */
	private Map<String, List<EventListener>> mapEventListeners;
	
	/**
	 * Map for registered CommandListener interfaces.
	 */
	private Map<String, List<CommandListener>> mapCommandListeners;
	
	/**
	 * List for registered LogListener interfaces.
	 */
	private List<LogListener> listLogListeners;
	
	/**
	 * List of Modules, ready to be unloaded in the next update tick.
	 */
	private List<Module> listUnloadNext;
	
	/**
	 * ModuleVanilla instance to communicate with vanilla commands, and handlers from the original game code.
	 * NOTE: This module's code is not accessible in respect to the proprietary nature of the game.
	 */
	private ModuleVanilla moduleVanilla;
	
	/**
	 * ModuleCore instance to handle core-level components of ZIRC.
	 */
	private ModuleCore moduleCore;
	
	/**
	 * UdpEngine pointer for the Project Zomboid GameServer UdpEngine instance, to communicate with connections.
	 */
	private UdpEngine udpEngine;
	
	/**
	 * Long variable to measure update-tick deltas.
	 */
	private long timeThen;
	
	/**
	 * String Array to store the list of the plugins from ZIRC.ini Settings.
	 */
	private String[] listPluginsRaw;

	/**
	 * List of registered PermissionHandler interfaces.
	 */
	private List<PermissionHandler> listPermissionHandlers;

	/**
	 * List of live NPC instances on the server.
	 */
	private List<NPC> listNPCs;

	/**
	 * INI file for ZIRC Settings.
	 */
	private INI ini;

	/**
	 * Permission Denied message to send to players.
	 */
	private String permissionDeniedMessage = "Permission denied.";

	/**
	 * Main constructor. Requires UdpEngine instance from GameServer to initialize.
	 * @param udpEngine
	 */
	public ZIRC(UdpEngine udpEngine) {
		setUdpEngine(udpEngine);
	}
	
	/**
	 * Initializes the ZIRC engine.
	 */
	public void init() {
		// Initialize Maps.
		mapEventListeners      = new HashMap<>();
		mapCommandListeners    = new HashMap<>();
		mapModules             = new HashMap<>();
		
		// Initialize Lists.
		listNPCs               = new ArrayList<>();
		listModules            = new ArrayList<>();
		listLogListeners       = new ArrayList<>();
		listUnloadNext         = new ArrayList<>();
		listPermissionHandlers = new ArrayList<>();

		// Put a wild-card List for the CommandListener interface Map.
		mapCommandListeners.put("*", new ArrayList<CommandListener>());

		// Initialize the Chat Engine.
		chat = new Chat(udpEngine);
		
		// Load the settings for ZIRC.
		loadSettings();
		
		// Load and start the Modules.
		loadModules();
		startModules();
	}

	private void loadSettings() {
		println("Loading settings..");
		String iniFileLocation = GameWindow.getCacheDir() + File.separator + "Server" + File.separator + "ZIRC.ini";
		File iniFile = new File(iniFileLocation);
		ini = new INI(iniFile);
		if (iniFile.exists()) {
			try {
				
				// Create default settings before overwriting any from the file.
				createSettings(ini);
				
				// Read the settings file.
				ini.read();
				
				// Grab the list of plugins as a string.
				String listPluginsRaw = ini.getVariableAsString("GENERAL", "plugins");
				
				
				// If the setting is blank, handle properly.
				if(listPluginsRaw.isEmpty()) {
					this.listPluginsRaw = new String[0];
				} else {					
					// The plug-in entries are comma-delimited.
					this.listPluginsRaw = listPluginsRaw.split(",");
				}
				
			} catch (IOException e) {
				println("Failed to read settings.");
				e.printStackTrace();
			}
		} else {
			createSettings(ini);
			try {
				ini.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.permissionDeniedMessage  = ini.getVariableAsString("GENERAL", "permissiondeniedmessage");
	}
	
	private void createSettings(INI ini) {
		ini.createSection("GENERAL");
			ini.setVariable("GENERAL", "plugins", "");
			ini.setVariable("GENERAL", "permissiondeniedmessage", "You do not have access to that command.");
	}

	public void loadModules() {
		registerModule(new ModuleMonitor());
		
		// registerModule(new ModuleNPC());
		
		ZUtil.initPluginFolder();

		for (String plugin : listPluginsRaw) {
			if (plugin != null && !plugin.isEmpty()) {
				try {
					Module module = loadPlugin(plugin);
					registerModule(module);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// Core Modules.
		moduleVanilla = new ModuleVanilla();
		moduleVanilla.loadModule();

		moduleCore = new ModuleCore();
		moduleCore.loadModule();

		// onLoad()
		Iterator<Module> modules = listModules.iterator();
		while (modules.hasNext()) {
			Module module = modules.next();
			if (module != null) {
				try {
					println("Loading module " + module.getModuleName() + "...");
					module.loadModule();
				} catch (Exception e) {
					println("Error loading module " + module.getModuleName() + ": " + e.getMessage());
					for (StackTraceElement o : e.getStackTrace()) {
						println(o);
					}
					unloadModule(module, false);
					modules.remove();
				}
			}
		}
	}

	public void startModules() {
		// onStart()
		Iterator<Module> modules = listModules.iterator();
		while (modules.hasNext()) {
			Module module = modules.next();
			if (module != null) {
				try {
					println("Starting module " + module.getModuleName() + "...");
					module.startModule();
				} catch (Exception e) {
					println("Error starting module " + module.getModuleName() + ": " + e.getMessage());
					for (StackTraceElement o : e.getStackTrace()) {
						println(o);
					}
					stopModule(module);
					unloadModule(module, false);
					modules.remove();
				}
			}
		}
	}
	
	public NPC addNPC(NPC npc) {
		GameServer.PlayerToAddressMap.put(npc, -1L);
		GameServer.playerToCoordsMap.put(Integer.valueOf(npc.PlayerIndex), new Vector2());
		GameServer.IDToPlayerMap.put(Integer.valueOf(npc.PlayerIndex), npc);
		GameServer.Players.add(npc);

		UdpEngine udpEngine = ZIRC.instance.getUdpEngine();
		for (UdpConnection c : udpEngine.connections) {
			GameServer.sendPlayerConnect(npc, c);
		}

		listNPCs.add(npc);
		return npc;
	}

	public void destroyNPC(NPC npc) {
		GameServer.PlayerToAddressMap.remove(npc);
		GameServer.playerToCoordsMap.remove(npc.PlayerIndex);
		GameServer.IDToPlayerMap.remove(npc.PlayerIndex);
		GameServer.Players.remove(npc);
	}

	public void update() {
		synchronized (concurrentLock) {
			long timeNow = System.currentTimeMillis();
			updateModules(timeNow);
			timeThen = timeNow;
			Iterator<Module> modules = listUnloadNext.iterator();
			while (modules.hasNext()) {
				Module module = modules.next();
				unloadModule(module, false);
				modules.remove();
			}
			updateNPCs();
		}
	}

	public void updateNPCs() {
		for (NPC npc : listNPCs) {
			npc.preupdate();
			npc.update();
			npc.postupdate();
		}

		if (System.currentTimeMillis() - timeThen > 200) {
			for (NPC npc : listNPCs) {
				byte flags = 0;
				if (npc.def.Finished)
					flags = (byte) (flags | 1);
				if (npc.def.Looped)
					flags = (byte) (flags | 2);
				if (npc.legsSprite != null && npc.legsSprite.CurrentAnim != null
						&& npc.legsSprite.CurrentAnim.FinishUnloopedOnFrame == 0)
					flags = (byte) (flags | 4);
				if (npc.bSneaking)
					flags = (byte) (flags | 8);
				if (npc.isTorchCone()) flags = (byte) (flags | 16);
				if (npc.isOnFire()) flags = (byte) (flags | 32);
				boolean torchCone = (flags & 16) != 0;
				boolean onFire = (flags & 32) != 0;

				for (UdpConnection c : udpEngine.connections) {
					ByteBufferWriter byteBufferWriter = c.startPacket();
					PacketTypes.doPacket((byte) 7, byteBufferWriter);
					byteBufferWriter.putShort((short) npc.OnlineID);
					byteBufferWriter.putByte((byte) npc.dir.index());
					
					byteBufferWriter.putFloat(npc.getX()             );
					byteBufferWriter.putFloat(npc.getY()             );
					byteBufferWriter.putFloat(npc.getZ()             );
					byteBufferWriter.putFloat(npc.playerMoveDir.x * 2);
					byteBufferWriter.putFloat(npc.playerMoveDir.y * 2);
					
					byteBufferWriter.putByte(npc.NetRemoteState);
					
					// Send the current animation state.
					if (npc.sprite != null) {
						byteBufferWriter.putByte((byte) npc.sprite.AnimStack.indexOf(npc.sprite.CurrentAnim));
					} else {
						byteBufferWriter.putByte((byte) 0);
					}
					
					byteBufferWriter.putByte((byte) ((int) npc.def.Frame));
					
					// Send the Animation frame delta and lighting data.
					byteBufferWriter.putFloat(npc.def.AnimFrameIncrease);
					byteBufferWriter.putFloat(npc.mpTorchDist          );
					byteBufferWriter.putFloat(npc.mpTorchStrength      );
					
					boolean legAnimation = npc.legsSprite != null && npc.legsSprite.CurrentAnim != null && npc.legsSprite.CurrentAnim.FinishUnloopedOnFrame == 0;
					
					if (npc.def.Finished) flags = (byte) (flags |  1);
					if (npc.def.Looped)   flags = (byte) (flags |  2);
					if (legAnimation)     flags = (byte) (flags |  4);
					if (npc.bSneaking)    flags = (byte) (flags |  8);
					if (torchCone)        flags = (byte) (flags | 16);
					if (onFire)           flags = (byte) (flags | 32);
					
					byteBufferWriter.putByte(flags);
					c.endPacketSuperHighUnreliable();
				}
			}
		}
	}

	private void updateModules(long timeNow) {
		long delta = timeNow - timeThen;
		Iterator<Module> modules = listModules.iterator();
		while (modules.hasNext()) {
			Module module = modules.next();
			if (module != null) {
				boolean remove = false;
				for (Module un : listUnloadNext) {
					if (un.equals(module)) {
						remove = true;
						break;
					}
				}
				if (remove) {
					modules.remove();
					continue;
				}
				updateModule(module, delta);
			}
		}

		// Update the core last.
		updateModule(moduleCore, delta);
	}

	private void updateModule(Module module, long delta) {

		if (module == null)
			throw new IllegalArgumentException("module is null!");
		if (delta < 0) return;
		try {
			module.updateModule(delta);
		} catch (Exception e) {
			println("Error updating module " + module.getModuleName() + ": " + e.getMessage());
			for (StackTraceElement o : e.getStackTrace()) {
				println(o);
			}
			stopModule(module);
			unloadModule(module, false);
		}
	}

	public void stop() {
		synchronized (concurrentLock) {
			stopModules();
			unloadModules();
		}
	}

	private void stopModules() {
		// onStop();
		Iterator<Module> modules = listModules.iterator();
		while (modules.hasNext()) {
			Module module = modules.next();
			stopModule(module);
		}

		// Stop the core module last.
		stopModule(moduleCore);

	}

	private void stopModule(Module module) {
		try {
			println("Stopping module " + module.getModuleName() + "...");
			module.stopModule();
		} catch (Exception e) {
			println("Failed to stop module " + module.getModuleName() + ": " + e.getMessage());
			for (StackTraceElement o : e.getStackTrace()) {
				println(o);
			}
		}
	}

	private void unloadModules() {
		// onUnload();
		Iterator<Module> modules = listModules.iterator();
		while (modules.hasNext()) {
			Module module = modules.next();
			if (module != null)
				unloadModule(module, false);
			modules.remove();
		}

		// Unload the core module last.
		unloadModule(moduleCore, false);
	}

	private void unloadModule(Module module, boolean remove) {
		if (module == null)
			throw new IllegalArgumentException("Module is null!");
		println("Unloading module " + module.getModuleName() + "...");
		try {
			module.unloadModule();
			mapModules.remove(module.getModuleID());
		} catch (Exception e) {
			println("Failed to unload module " + module.getModuleName() + ": " + e.getMessage());
			for (StackTraceElement o : e.getStackTrace()) {
				println(o);
			}
		}
	}

	public Event handleEvent(Event event) {
		return handleEvent(event, true);
	}

	public Event handleEvent(Event event, boolean logEvent) {
		try {
			if (event == null) throw new IllegalArgumentException("Event is null!");

			if (event.getID() == CommandEvent.ID) {
				return (handleCommand((CommandEvent) event, logEvent));
			}

			List<EventListener> listEventListeners = mapEventListeners.get(event.getID());
			if (listEventListeners != null) {
				for (EventListener listener : listEventListeners) {
					listener.handleEvent(event);
					if (event.canceled())
						return event;
					if (event.handled())
						break;
				}
			}

			// If the Event is set to canceled, return.
			if (event.canceled()) return event;

			// Force Core Event-handling to be last, for modification potential.
			if (!event.handled()) {
				moduleCore.getEventListener().handleEvent(event);
			}

			// If the Event is set to canceled, return before logging it.
			if (event.canceled()) return event;
			
			// Log the Event.
			if (logEvent) logEvent(event);

		} catch (Exception e) {
			println("Error handling event " + event + ": " + e.getMessage());
			for (StackTraceElement o : e.getStackTrace()) {
				println(o);
			}
		}
		return event;
	}

	/**
	 * Logs a Event, running through each LogListener interface.
	 * @param event
	 */
	public void logEvent(Event event) {
		try {
			// Create a new LogEvent instance for the Event.
			LogEvent logEvent = new LogEvent(event);
			
			// Go through each LogListener interface and fire it.
			for (LogListener listener : listLogListeners) {
				if (listener != null) listener.onLogEntry(logEvent);
			}

			String log = "ZIRC";
			
			// Grab the ID of the event.
			String eName = event.getID();
			
			// For organization purposes.
			if (eName.equalsIgnoreCase(ChatEvent.ID)) {
				log += "-CHAT";
			} else if (eName.equalsIgnoreCase(CommandEvent.ID)) {
				log += "-COMMAND";
			}

			// If important, log as such. Else log normally.
			if (logEvent.isImportant()) {
				LoggerManager.getLogger(log).write(logEvent.getLogMessage(), "IMPORTANT");
			} else {
				LoggerManager.getLogger(log).write(logEvent.getLogMessage());
			}
			
		} catch (Exception e) {
			println("Error logging event " + event + ": " + e.getMessage());
			for (StackTraceElement o : e.getStackTrace()) {
				println(o);
			}
		}

	}

	/**
	 * Handles a command.
	 * @param connection
	 * @param input
	 * @return
	 */
	public CommandEvent handleCommand(UdpConnection connection, String input) {
		return handleCommand(connection, input, true);
	}

	/**
	 * Handles a command.
	 * @param connection
	 * @param input
	 * @param logEvent
	 * @return
	 */
	public CommandEvent handleCommand(UdpConnection connection, String input, boolean logEvent) {
		Player player = null;
		
		// Create a Player instance.
		if (connection == null) player = new Player();
		else player = new Player(connection);
		
		// Create a CommandEvent.
		CommandEvent c = new CommandEvent(player, input);
		
		// Fire the CommandEvent handle method, and return its result.
		return handleCommand(c, logEvent);
	}

	/**
	 * Handles a CommandEvent. 
	 * @param c
	 * @param logEvent
	 * @return
	 */
	private CommandEvent handleCommand(CommandEvent c, boolean logEvent) {
		synchronized (concurrentLock) {
			try {
				String command = c.getCommand();

				// If '/help' is fired.
				if (command.equalsIgnoreCase("help")) {
					help(c);
					return c;
				}

				// Run through selected command listeners first (Optimization).
				List<CommandListener> listListeners = mapCommandListeners.get(c.getCommand());
				
				if (listListeners != null) {
					for (CommandListener listener : listListeners) {
						// If the listener is not null, fire the CommandListener.
						if (listener != null) listener.onCommand(c);
						
						// If the listener set the command as handled, break the loop.
						if (c.handled()) break;
					}
				}

				// Force Vanilla CommandListener to last for modification
				// potential.
				CommandListener vanillaListener = moduleVanilla.getCommandListener();
				if (!c.handled() && vanillaListener != null) vanillaListener.onCommand(c);
	

				CoreCommandListener coreCommandListener = moduleCore.getCommandListener();

				if (!c.handled() && coreCommandListener != null) {
					coreCommandListener.onCommand(c);
				}

				if (logEvent) {
					// Iterate the log listeners after the command.
					if (c.getLoggedMessage() != null) {
						LogEvent entry = new LogEvent(c);
						for (LogListener listener : listLogListeners) {
							if (listener != null)
								listener.onLogEntry(entry);
						}
					}
				}

				// For console commands, or other methods outside of the game,
				// this strips the color codes, and replaces '<LINE>' with \n.
				if (c.getPlayer().getConnection() == null) {
					c.setResponse(c.getResult(), Chat.getStripped(c.getResponse(), true));
				}
			} catch (Exception e) {
				println("Error handling command " + c + ": " + e.getMessage());
				for (StackTraceElement o : e.getStackTrace()) {
					println(o);
				}
			}
		}
		return c;
	}

	// TODO: Permission Integration.
	/**
	 * Method executing the '/help' command.
	 * @param command
	 */
	private void help(CommandEvent command) {
		Player player = command.getPlayer();
		String response = "Commands: " + Chat.CHAT_LINE + " " + Chat.CHAT_COLOR_WHITE + " ";

		for (List<CommandListener> listListeners : mapCommandListeners.values()) {
			if (listListeners != null) {
				for (CommandListener listener : listListeners) {
					if (listener != null) {
						String[] commands = listener.getCommands();
						if (commands != null) {
							for (String com : listener.getCommands()) {
								if (com != null) {
									String tip = listener.onTooltip(player, com.toLowerCase());
									if (tip != null) {
										response += Chat.CHAT_COLOR_LIGHT_GREEN + " " + com + ": "
												+ Chat.CHAT_COLOR_WHITE + " "
												+ listener.onTooltip(player, com.toLowerCase()) + Chat.CHAT_COLOR_WHITE
												+ " " + Chat.CHAT_LINE + " " + Chat.CHAT_LINE + " ";
									}
								}
							}
						}
					}
				}
			}
		}

		CoreCommandListener coreCommandListener = moduleCore.getCommandListener();

		if (coreCommandListener != null) {
			String[] commands = coreCommandListener.getCommands();
			if (commands != null) {
				for (String com : coreCommandListener.getCommands()) {
					if (com != null) {
						String tip = coreCommandListener.onTooltip(player, com.toLowerCase());
						if (tip != null) {
							response += Chat.CHAT_COLOR_LIGHT_GREEN + " " + com + ": " + Chat.CHAT_COLOR_WHITE + " "
									+ coreCommandListener.onTooltip(player, com.toLowerCase()) + Chat.CHAT_COLOR_WHITE
									+ " " + Chat.CHAT_LINE + " " + Chat.CHAT_LINE + " ";
						}
					}
				}
			}
		}

		CommandListener vanillaListener = moduleVanilla.getCommandListener();
		if (vanillaListener != null) {
			String[] commands = vanillaListener.getCommands();
			if (commands != null) {
				for (String com : vanillaListener.getCommands()) {
					if (com != null) {
						String tip = vanillaListener.onTooltip(player, com.toLowerCase());
						if (tip != null) {
							response += Chat.CHAT_COLOR_LIGHT_GREEN + " " + com + ": " + Chat.CHAT_COLOR_WHITE + " "
									+ vanillaListener.onTooltip(player, com.toLowerCase()) + Chat.CHAT_COLOR_WHITE + " "
									+ Chat.CHAT_LINE + " " + Chat.CHAT_LINE + " ";
						}
					}
				}
			}
		}

		command.setResponse(Result.SUCCESS, response);
	}
	
	
	/**
	 * Returns whether or not a user has a allowed permissions context.
	 * @param username
	 * @param context
	 * @return
	 */
	public boolean hasPermission(String username, String context) {
		
		if(ZUtil.isUserAdmin(username)) return true;
		
		// Loop through each handler and if any returns true, return true.
		for(PermissionHandler handler : this.listPermissionHandlers) {
			try {				
				if(handler.hasPermission(username, context)) return true;
			} catch(Exception e) {
				println("Error handling permission check: " + handler.getClass().getName() + " Error: " + e.getMessage());
				ZUtil.printStackTrace(e);
			}
		}
		
		// If no permissions handler identified as true, return false.
		return false;
	}

	/**
	 * Registers a ZIRC Module.
	 * @param module
	 */
	public void registerModule(Module module) {
		synchronized (concurrentLock) {
			if (module == null)
				throw new IllegalArgumentException("Module is null!");

			if (!listModules.contains(module)) {
				listModules.add(module);
			}

			Module mappedModule = mapModules.get(module.getModuleID());
			if (mappedModule != null) {
				throw new IllegalArgumentException("Module ID for class "
						+ (module.getClass().getPackage() + "." + module.getClass().getName())
						+ " conflicts with the module already registered: "
						+ (mappedModule.getClass().getPackage() + "." + mappedModule.getClass().getName())
						+ ". If you are the author of this mod, you will need to change the ID to be unique. Otherwise, report this to the mod author.");
			} else {
				mapModules.put(module.getModuleID(), module);
			}
			
		}
	}

	/**
	 * Registers an EventListener interface, with a Event ID, given as a String.
	 * @param event
	 * @param listener
	 */
	public void registerEventListener(String event, EventListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("Listener is null!");
		List<EventListener> listListeners = mapEventListeners.get(event);
		if (listListeners == null) {
			listListeners = new ArrayList<>();
			mapEventListeners.put(event, listListeners);
			listListeners.add(listener);
		} else {
			listListeners.add(listener);
		}
	}

	/**
	 * Registers an EventListener interface, with all Event IDs listed in the interface as String[] getTypes().
	 * @param listener
	 */
	public void registerEventListener(EventListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("Listener is null!");
		String[] types = listener.getTypes();
		if (types == null)
			throw new IllegalArgumentException("listener.getTypes() array is null!");
		for (String type : types) {
			registerEventListener(type, listener);
		}

	}

	/**
	 * Registers a CommandListener interface, with a command, given as a String.
	 * @param command
	 * @param listener
	 */
	public void registerCommandListener(String command, CommandListener listener) {
		if (listener == null) throw new IllegalArgumentException("Listener is null!");

		command = command.toLowerCase();

		List<CommandListener> listListeners = mapCommandListeners.get(command);
		if (listListeners == null) {
			listListeners = new ArrayList<>();
			mapCommandListeners.put(command, listListeners);
			listListeners.add(listener);
		} else {
			listListeners.add(listener);
		}
	}

	/**
	 * Registers a LogListener interface.
	 * @param listener
	 */
	public void registerLogListener(LogListener listener) {
		if (listener == null) throw new IllegalArgumentException("Listener is null!");
		listLogListeners.add(listener);
	}
	
	/**
	 * Registers a PermissionHandler interface.
	 * @param handler
	 */
	public void registerPermissionHandler(PermissionHandler handler) {
		if(handler != null) {			
			if(!listPermissionHandlers.contains(handler)) listPermissionHandlers.add(handler);
		}
	}
	
	/**
	 * Reloads ZIRC entirely.
	 */
	public void reload() {
		stop();
		init();
	}

	/**
	 * Returns a Module with a given ID.
	 * @param ID
	 * @return
	 */
	public Module getModuleByID(String ID) {
		return mapModules.get(ID);
	}

	/**
	 * Returns Project Zomboid's UdpEngine instance.
	 * @return
	 */
	public UdpEngine getUdpEngine() {
		return this.udpEngine;
	}

	/**
	 * Sets ZIRC's reference to Project Zomboid's UdpEngine instance.
	 * @param udpEngine
	 */
	public void setUdpEngine(UdpEngine udpEngine) {
		this.udpEngine = udpEngine;
	}

	/**
	 * Prints lines with "ZIRC: [message...]".
	 * @param messages
	 */
	public static void println(Object... messages) {
		for (Object message : messages)
			System.out.println("ZIRC: " + message);
	}

	/**
	 * Unloads a module.
	 * @param module
	 */
	public void unloadModule(Module module) {
		this.listUnloadNext.add(module);
	}

	/**
	 * Returns the list of all loaded modules.
	 * @return
	 */
	public List<Module> getLoadedModules() {
		return this.listModules;
	}

	/**
	 * Returns the map of EventListener interfaces registered.
	 * @return
	 */
	public Map<String, List<EventListener>> getEventListeners() {
		return this.mapEventListeners;
	}

	public List<LogListener> getLogListeners() {
		return this.listLogListeners;
	}

	public Map<String, List<CommandListener>> getCommandListeners() {
		return this.mapCommandListeners;
	}
	
	public String getPermissionDeniedMessage() {
		return this.permissionDeniedMessage;
	}

	public Chat getChat() {
		return chat;
	}

	private static Module loadPlugin(String name)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		String pluginName = ZUtil.pluginLocation + name + ".jar";
		File pluginFile = new File(pluginName);
		if (!pluginFile.exists())
			throw new IllegalArgumentException("Jar file not found: " + pluginName);

		Map<String, String> pluginSettings = getSettings(pluginName);
		String module = pluginSettings.get("module");
		if (module == null)
			throw new IllegalArgumentException("plugin.txt is not valid: " + pluginName);

		URL url = pluginFile.toURI().toURL();
		URL[] urls = { url };
		ClassLoader loader = new URLClassLoader(urls);

		List<String> listClasses = new ArrayList<>();

		JarFile jarFile = new JarFile(pluginName);
		Enumeration<?> e = jarFile.entries();
		while (e.hasMoreElements()) {
			JarEntry entry = (JarEntry) e.nextElement();
			if (entry.isDirectory() || !entry.getName().endsWith(".class"))
				continue;
			String className = entry.getName().substring(0, entry.getName().length() - 6);
			className = className.replace('/', '.');
			listClasses.add(className);
		}
		jarFile.close();

		for (String clazz : listClasses)
			loader.loadClass(clazz);

		Class<?> classToLoad = Class.forName(module, true, loader);
		Module instance = (Module) classToLoad.newInstance();
		instance.setPluginSettings(pluginSettings);
		instance.setJarName(name);
		return instance;
	}

	private static Map<String, String> getSettings(String fileName) {
		URL url;

		Map<String, String> listSettings = new HashMap<>();
		try {
			url = new URL("jar:file:" + fileName + "!/plugin.txt");
			InputStream is = url.openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.toLowerCase().startsWith("module:")) {
					listSettings.put("module", line.split(":")[1]);
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return listSettings;
	}

}