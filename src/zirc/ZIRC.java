package zirc;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import zirc.event.ChatEvent;
import zirc.event.CommandEvent;
import zirc.event.Event;
import zirc.event.LogEvent;
import zirc.event.CommandEvent.Result;
import zirc.interfaces.CommandListener;
import zirc.interfaces.EventListener;
import zirc.interfaces.LogListener;
import zirc.module.Module;
import zirc.module.ModuleMonitor;
import zirc.module.ModuleVanilla;
import zirc.modules.CoreCommandListener;
import zirc.modules.ModuleCore;
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
	
	public static ZIRC instance;
	private static Object concurrentLock = new Object();
	private Chat chat;
	
	private List<Module> listModules;
	private Map<String, List<EventListener>> mapEventListeners;
	private Map<String, List<CommandListener>> mapCommandListeners;
	private List<LogListener> listLogListeners;
	private List<Module> listUnloadNext;
	private ModuleVanilla moduleVanilla;
	private ModuleCore moduleCore;
	private UdpEngine udpEngine;
	private long timeThen;
	private String[] listPluginsRaw;
	
	private List<NPC> listNPCs;
	
	private INI ini;
	
	public ZIRC(UdpEngine udpEngine) {
		setUdpEngine(udpEngine);
	}
	
	public void init() {
		listNPCs            = new ArrayList<>();
		listModules         = new ArrayList<>();
		mapEventListeners   = new HashMap<>();
		mapCommandListeners = new HashMap<>();
		listLogListeners    = new ArrayList<>();
		listUnloadNext      = new ArrayList<>();
		mapCommandListeners.put("*", new ArrayList<CommandListener>());
		chat = new Chat(udpEngine);
		loadSettings();
		loadModules();
		startModules();
	}
	
	private void loadSettings() {
		println("Loading settings..");
		String iniFileLocation = GameWindow.getCacheDir() + File.separator + "Server" + File.separator + "ZIRC.ini";
		File iniFile = new File(iniFileLocation);
		ini = new INI(iniFile);
		if(iniFile.exists()) {			
			try {
				ini.read();
				String listPluginsRaw = ini.getVariableAsString("GENERAL", "plugins");
				this.listPluginsRaw = listPluginsRaw.split(",");
			} catch (IOException e) {
				println("Failed to read settings.");
				e.printStackTrace();
			}
		} else {
			ini.createSection("GENERAL");
			ini.setVariable("GENERAL", "plugins", "");
			try {
				ini.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void loadModules() {
		registerModule(new ModuleMonitor());
		// registerModule(new ModuleNPC());
		ZIRCUtil.verifyPluginFolder();
		
		for(String plugin : listPluginsRaw) {
			if(plugin != null && !plugin.isEmpty()) {
				try {
					Module module = ZIRCUtil.loadPlugin(plugin);
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
			if(module != null) {				
				try {
				println("Loading module " + module.getModuleName() + "...");
				module.loadModule();
				} catch(Exception e) {
					println("Error loading module " + module.getModuleName() + ": " + e.getMessage());
					for(StackTraceElement o : e.getStackTrace()) {
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
			if(module != null) {
				try {					
					println("Starting module " + module.getModuleName() + "...");
					module.startModule();
				} catch(Exception e) {
					println("Error starting module " + module.getModuleName() + ": " + e.getMessage());
					for(StackTraceElement o : e.getStackTrace()) {
						println(o);
					}
					stopModule(module);
					unloadModule(module, false);
					modules.remove();
				}
			}
		}
	}
	
	public void update() {
		synchronized(concurrentLock) {			
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
		for(NPC npc : listNPCs) {
			npc.preupdate();
			npc.update();
			npc.postupdate();
		}
		
		if(System.currentTimeMillis() - timeThen > 200) {
			for(NPC npc : listNPCs) {
				byte flags = 0;
				if(npc.def.Finished) flags = (byte)(flags | 1);
				if(npc.def.Looped) flags = (byte)(flags | 2);
				if(npc.legsSprite != null && npc.legsSprite.CurrentAnim != null && npc.legsSprite.CurrentAnim.FinishUnloopedOnFrame == 0) flags = (byte)(flags | 4);
				if(npc.bSneaking) flags = (byte)(flags | 8);
				if(npc.isTorchCone()) flags = (byte)(flags | 16);
				if(npc.isOnFire()) flags = (byte)(flags | 32);
				boolean torchCone = (flags & 16) != 0;
				boolean onFire = (flags & 32) != 0;
				
				for(UdpConnection c : udpEngine.connections) {
					ByteBufferWriter byteBufferWriter = c.startPacket();
					PacketTypes.doPacket((byte)7, byteBufferWriter);
					byteBufferWriter.putShort((short)npc.OnlineID);
					byteBufferWriter.putByte((byte)npc.dir.index());
					byteBufferWriter.putFloat(npc.getX());
					byteBufferWriter.putFloat(npc.getY());
					byteBufferWriter.putFloat(npc.getZ());
					byteBufferWriter.putFloat(npc.playerMoveDir.x * 2);
					byteBufferWriter.putFloat(npc.playerMoveDir.y * 2);
					byteBufferWriter.putByte(npc.NetRemoteState);
					if(npc.sprite != null) {
						byteBufferWriter.putByte((byte)npc.sprite.AnimStack.indexOf(npc.sprite.CurrentAnim));
					} else {
						byteBufferWriter.putByte((byte)0);
					}
					byteBufferWriter.putByte((byte)((int)npc.def.Frame));
					byteBufferWriter.putFloat(npc.def.AnimFrameIncrease);
					byteBufferWriter.putFloat(npc.mpTorchDist);
					byteBufferWriter.putFloat(npc.mpTorchStrength);
					if(npc.def.Finished) flags = (byte)(flags | 1);
					if(npc.def.Looped) flags = (byte)(flags | 2);
					if(npc.legsSprite != null && npc.legsSprite.CurrentAnim != null && npc.legsSprite.CurrentAnim.FinishUnloopedOnFrame == 0) flags = (byte)(flags | 4);
					if(npc.bSneaking) flags = (byte)(flags | 8);
					if(torchCone) flags = (byte)(flags | 16);
					if(onFire) flags = (byte)(flags | 32);
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
			if(module != null) {
				boolean remove = false;
				for(Module un : listUnloadNext) {
					if(un.equals(module)) {
						remove = true;
						break;
					}
				}
				if(remove) {
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
		
		if(module == null) throw new IllegalArgumentException("module is null!");
		if(delta < 0) return; //throw new IllegalArgumentException("Delta must be 0 or greater!");
		try {
			module.updateModule(delta);					
		} catch(Exception e) {
			println("Error updating module " + module.getModuleName() + ": " + e.getMessage());
			for(StackTraceElement o : e.getStackTrace()) {
				println(o);
			}
			stopModule(module);
			unloadModule(module, false);
		}
	}
	
	public void stop() {
		synchronized(concurrentLock) {			
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
		} catch(Exception e) {
			println("Failed to stop module " + module.getModuleName() + ": " + e.getMessage());
			for(StackTraceElement o : e.getStackTrace()) {
				println(o);
			}
		}
	}
	
	private void unloadModules() {
		// onUnload();
		Iterator<Module> modules = listModules.iterator();
		while (modules.hasNext()) {
			Module module = modules.next();
			if(module != null) unloadModule(module, false);
			modules.remove();
		}
		
		// Unload the core module last.
		unloadModule(moduleCore, false);
	}
	
	private void unloadModule(Module module, boolean remove) {
		if(module == null) throw new IllegalArgumentException("Module is null!");
		println("Unloading module " + module.getModuleName() + "...");
		try {						
			module.unloadModule();
		} catch(Exception e) {
			println("Failed to unload module " + module.getModuleName() + ": " + e.getMessage());
			for(StackTraceElement o : e.getStackTrace()) {
				println(o);
			}
		}
	}
	
	public void reload() {
		stop();
		init();
	}
	
	public Event handleEvent(Event event) {
		return handleEvent(event, true);
	}
	
	public Event handleEvent(Event event, boolean logEvent) {
		try {
			if(event == null) throw new IllegalArgumentException("Event is null!");
			
			if(event.getName() == CommandEvent.ID) {
				return(handleCommand((CommandEvent)event, logEvent));
			}
			
			List<EventListener> listEventListeners = mapEventListeners.get(event.getName());
			if(listEventListeners != null) {
				for(EventListener listener : listEventListeners) {
					listener.handleEvent(event);
					if(event.canceled()) return event;
					if(event.handled()) break;
				}
			}
			
			if(event.canceled()) return event;
			
			// Force Core Event-handling to be last, for modification potential.
			if(!event.handled()) {
				moduleCore.getEventListener().handleEvent(event);					
			}
			
			if(event.canceled()) return event;			
			if(logEvent) logEvent(event);
			
		} catch(Exception e) {
			println("Error handling event " + event + ": " + e.getMessage());
			for(StackTraceElement o : e.getStackTrace()) {
				println(o);
			}
		}
		return event;
	}
	
	public void logEvent(Event event) {
		try {
			LogEvent logEvent = new LogEvent(event);
			for(LogListener listener : listLogListeners) {
				if(listener != null) listener.onLogEntry(logEvent);
			}
			
			String log = "ZIRC";
			String eName = event.getName();
			if(eName.equalsIgnoreCase(ChatEvent.ID)) {
				log += "-CHAT";
			} else
				if(eName.equalsIgnoreCase(CommandEvent.ID)) {
					log += "-COMMAND";
				}
			if(!logEvent.isImportant()) {
				LoggerManager.getLogger(log).write(logEvent.getLogMessage());
			} else {				
				LoggerManager.getLogger(log).write(logEvent.getLogMessage(), "IMPORTANT");
			}
		} catch(Exception e) {
			println("Error logging event " + event + ": " + e.getMessage());
			for(StackTraceElement o : e.getStackTrace()) {
				println(o);
			}
		}
		
	}
	
	public CommandEvent handleCommand(UdpConnection connection, String input) {
		return handleCommand(connection, input, true);
	}
	
	public CommandEvent handleCommand(UdpConnection connection, String input, boolean logEvent) {
		Player player = null;
		if(connection == null) player = new Player();
		else player = new Player(connection);
		CommandEvent c = new CommandEvent(player, input);
		return handleCommand(c, logEvent);
	}
	
	private CommandEvent handleCommand(CommandEvent c, boolean logEvent) {
		synchronized(concurrentLock) {
			try {
				String command = c.getCommand();
				
				if(command.equalsIgnoreCase("help")) {
					help(c);
					return c;
				}
	
				// Run through selected command listeners first (Optimization).
				List<CommandListener> listListeners = mapCommandListeners.get(c.getCommand());
				if(listListeners != null) {
					for(CommandListener listener : listListeners) {
						if(listener != null) listener.onCommand(c);
						if(c.handled()) break;
					}
				}
				
				// Force Vanilla CommandListener to last for modification potential.
				CommandListener vanillaListener = moduleVanilla.getCommandListener();
				if(!c.handled() && vanillaListener != null) {
					vanillaListener.onCommand(c);
				}
				
				CoreCommandListener coreCommandListener = moduleCore.getCommandListener();
				
				if(!c.handled() && coreCommandListener != null) {
					coreCommandListener.onCommand(c);
				}
				
				if(logEvent) {				
					// Iterate the log listeners after the command.
					if(c.getLoggedMessage() != null) {
						LogEvent entry = new LogEvent(c);
						for(LogListener listener : listLogListeners) {
							if(listener != null) listener.onLogEntry(entry);
						}
					}
				}
				
				// For console commands, or other methods outside of the game,
				// this strips the color codes, and replaces '<LINE>' with \n.
				if(c.getPlayer().getConnection() == null) {
					c.setResponse(c.getResult(), Chat.getStripped(c.getResponse(), true));
				}
			} catch(Exception e) {
				println("Error handling command " + c + ": " + e.getMessage());
				for(StackTraceElement o : e.getStackTrace()) {
					println(o);
				}
			}
		}
		return c;
	}
	
	private void help(CommandEvent command) {
		Player player = command.getPlayer();
		String response = "Commands: " + Chat.CHAT_LINE + " " + Chat.CHAT_COLOR_WHITE + " ";
		
		for(List<CommandListener> listListeners : mapCommandListeners.values()) {
			if(listListeners != null) {					
				for(CommandListener listener : listListeners) {
					if(listener != null) {
						String[] commands = listener.getCommands();
						if(commands != null) {
							for(String com : listener.getCommands()) {
								if(com != null) {
									String tip = listener.onTooltip(player, com.toLowerCase());
									if(tip != null) {											
										response += Chat.CHAT_COLOR_LIGHT_GREEN + " " + com + ": " + Chat.CHAT_COLOR_WHITE + " " + listener.onTooltip(player, com.toLowerCase()) + Chat.CHAT_COLOR_WHITE + " " + Chat.CHAT_LINE + " " + Chat.CHAT_LINE + " ";
									}
								}
							}						
						}						
					}
				}
			}
		}
		
		CoreCommandListener coreCommandListener = moduleCore.getCommandListener();
		
		if(coreCommandListener != null) {
			String[] commands = coreCommandListener.getCommands();
			if(commands != null) {					
				for(String com : coreCommandListener.getCommands()) {
					if(com != null) {
						String tip = coreCommandListener.onTooltip(player, com.toLowerCase());
						if(tip != null) {								
							response += Chat.CHAT_COLOR_LIGHT_GREEN + " " + com + ": " + Chat.CHAT_COLOR_WHITE + " " + coreCommandListener.onTooltip(player, com.toLowerCase()) + Chat.CHAT_COLOR_WHITE + " " + Chat.CHAT_LINE + " " + Chat.CHAT_LINE + " ";
						}
					}
				}
			}
		}
		
		CommandListener vanillaListener = moduleVanilla.getCommandListener();
		if(vanillaListener != null) {
			String[] commands = vanillaListener.getCommands();
			if(commands != null) {					
				for(String com : vanillaListener.getCommands()) {
					if(com != null) {
						String tip = vanillaListener.onTooltip(player, com.toLowerCase());
						if(tip != null) {								
							response += Chat.CHAT_COLOR_LIGHT_GREEN + " " + com + ": " + Chat.CHAT_COLOR_WHITE + " " + vanillaListener.onTooltip(player, com.toLowerCase()) + Chat.CHAT_COLOR_WHITE + " " + Chat.CHAT_LINE + " " + Chat.CHAT_LINE + " ";
						}
					}
				}
			}
		}
		
		command.setResponse(Result.SUCCESS, response);
	}
	
	public void registerModule(Module module) {
		synchronized(concurrentLock) {
			if(module == null) throw new IllegalArgumentException("Module is null!");
			
			if(!listModules.contains(module)) {
				listModules.add(module);
			}
		}
	}

	public void registerEventListener(String event, EventListener listener) {
		if(listener == null) throw new IllegalArgumentException("Listener is null!");
		List<EventListener> listListeners = mapEventListeners.get(event);
		if(listListeners == null) {
			listListeners = new ArrayList<>();
			mapEventListeners.put(event, listListeners);
			listListeners.add(listener);
		} else {
			listListeners.add(listener);
		}
	}
	
	public void registerEventListener(EventListener listener) {
		if(listener == null) throw new IllegalArgumentException("Listener is null!");
		String[] types = listener.getTypes();
		if(types == null) throw new IllegalArgumentException("listener.getTypes() array is null!");
		for(String type : types) {
			registerEventListener(type, listener);
		}
		
	}

	public void registerCommandListener(String command, CommandListener listener) {
		if(listener == null) throw new IllegalArgumentException("Listener is null!");
		
		command = command.toLowerCase();
		
		List<CommandListener> listListeners = mapCommandListeners.get(command);
		if(listListeners == null) {
			listListeners = new ArrayList<>();
			mapCommandListeners.put(command, listListeners);
			listListeners.add(listener);
		} else {
			listListeners.add(listener);
		}
	}
	
	public void registerLogListener(LogListener listener) {
		if(listener == null) throw new IllegalArgumentException("Listener is null!");
		listLogListeners.add(listener);
	}
	
	public UdpEngine getUdpEngine() {
		return this.udpEngine;
	}
	
	public void setUdpEngine(UdpEngine udpEngine) {
		this.udpEngine = udpEngine;
	}
	
	public static void println(Object... messages) {
		for (Object message : messages) System.out.println("ZIRC: " + message);
	}

	public void unloadModule(Module module) {
		this.listUnloadNext.add(module);
	}
	
	public List<Module> getLoadedModules() {
		return this.listModules;
	}
	
	public Map<String, List<EventListener>> getEventListeners() {
		return this.mapEventListeners;
	}
	
	public List<LogListener> getLogListeners() {
		return this.listLogListeners;
	}
	
	public Map<String, List<CommandListener>> getCommandListeners() {
		return this.mapCommandListeners;
	}
	
	public Chat getChat() {
		return chat;
	}
	
	@SuppressWarnings("unchecked")
	public NPC addNPC(NPC npc) {
		GameServer.PlayerToAddressMap.put(npc, -1L);
		GameServer.playerToCoordsMap.put(Integer.valueOf(npc.PlayerIndex), new Vector2());
		GameServer.IDToPlayerMap.put(Integer.valueOf(npc.PlayerIndex), npc);
		GameServer.Players.add(npc);
		
		UdpEngine udpEngine = ZIRC.instance.getUdpEngine();
		for(UdpConnection c : udpEngine.connections) {
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
	
}
