package sledgehammer.module;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.event.ClientEvent;
import sledgehammer.event.Event;
import sledgehammer.event.EventManager;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.interfaces.EventListener;
import sledgehammer.interfaces.ExceptionListener;
import sledgehammer.interfaces.LogListener;
import sledgehammer.interfaces.ModuleSettingsHandler;
import sledgehammer.interfaces.PermissionListener;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.core.Player;
import sledgehammer.manager.core.ChatManager;
import sledgehammer.manager.core.ModuleManager;
import sledgehammer.manager.core.PermissionsManager;
import sledgehammer.module.core.ModuleChat;
import sledgehammer.util.INI;
import sledgehammer.util.Printable;

/**
 * TODO: Document
 * @author Jab
 */
public abstract class Module extends Printable {

	private INI ini;

	private File iniFile;

	public boolean loadedSettings = false;

	private boolean loaded = false;

	private boolean started = false;

	private String jarName = null;

	private Map<String, String> pluginSettings = new HashMap<>();

	public void loadSettings(ModuleSettingsHandler handler) {

		if (handler == null)
			throw new IllegalArgumentException("Settings Handler given is null!");

		loadedSettings = false;

		if (ini == null)
			getINI();

		if (iniFile.exists()) {
			handler.createSettings(getINI());
			try {
				ini.read();
				loadedSettings = true;
			} catch (IOException e) {
				println("Failed to read settings.");
				e.printStackTrace();
			}
		} else {
			println("WARNING: No settings file found. Creating one.");
			println("WARNING: " + getName() + " may require modified settings to run properly.");
			println("Settings file is located at: " + ini.getFile().getAbsolutePath());
			handler.createSettings(ini);
			loadedSettings = true;
			try {
				ini.save();
			} catch (IOException e) {
				println("Failed to save settings.");
				e.printStackTrace();
			}
		}
	}

	public void register(CommandListener listener) {
			SledgeHammer.instance.register(listener);
	}

	public boolean stopModule() {
		if (loaded) {
			try {
				if (started) {
					this.onStop();
				} else {
					println("Module is already stopped.");
				}
			} catch (Exception e) {
				println("Failed to safely stop module.");
				e.printStackTrace();
			}
		}
		loaded = false;
		return true;
	}

	public boolean loadModule() {
		try {
			onLoad();
			loaded = true;
			return true;
		} catch (Exception e) {
			println("Failed to load module.");
			loaded = false;
			e.printStackTrace();

		}
		return false;
	}

	public boolean unloadModule() {
		try {
			if (loaded) {
				this.onUnload();
			} else {
			}
		} catch (Exception e) {
			println("Failed to safely unload module.");
			e.printStackTrace();
		}
		return true;
	}

	public void register(EventListener listener) {
		String[] types = listener.getTypes();
		if (types == null) {
			throw new IllegalArgumentException("EventListener getTypes() array is null!");
		}
		for (String type : types) {
			SledgeHammer.instance.register(type, listener);
		}
	}
	
	public void unregister(EventListener listener) {
		getEventManager().unregister(listener);
	}
	
	public void unregister(CommandListener listener) {
		getEventManager().unregister(listener);
	}
	
	public void unregister(LogListener listener) {
		getEventManager().unregister(listener);
	}
	
	public void unregister(ExceptionListener listener) {
		getEventManager().unregister(listener);
	}
	
	public void setPermissionListener(PermissionListener permissionListener) {
		getPermissionsManager().setPermissionListener(permissionListener);
	}
	
	public void startModule() {
		if (!started) {
			started = true;
			onStart();
		} else {
			println("Module is already started.");
		}
	}

	public INI getINI() {
		if (ini == null) {
			iniFile = new File("plugins" + File.separator
					+ getJarName() + ".ini");
			ini = new INI(iniFile);
		}

		return this.ini;
	}

	public void unload() {
		started = false;
		getModuleManager().unloadModule(this, true);
	}

	public EventManager getEventManager() {
		return SledgeHammer.instance.getEventManager();
	}
	
	public ModuleManager getModuleManager() {
		return SledgeHammer.instance.getModuleManager();
	}

	public String getPermissionDeniedMessage() {
		return SledgeHammer.instance.getPermissionsManager().getPermissionDeniedMessage();
	}

	public Map<String, String> getPluginSettings() {
		return this.pluginSettings;
	}

	public void setPluginSettings(Map<String, String> map) {
		this.pluginSettings = map;
	}

	public String getJarName() {
		return this.jarName;
	}

	public void setJarName(String jarName) {
		this.jarName = jarName;
	}

	public void handleEvent(Event event, boolean shouldLog) {
		SledgeHammer.instance.handle(event, shouldLog);
	}

	public void handleEvent(Event event) {
		SledgeHammer.instance.handle(event);
	}

	public Module getModuleByID(String ID) {
		return getModuleManager().getModuleByID(ID);
	}

	public PermissionsManager getPermissionsManager() {
		return SledgeHammer.instance.getPermissionsManager();
	}

	public void updateModule(long delta) {
		if (started)
			onUpdate(delta);
	}

	public boolean loadedSettings() {
		return this.loadedSettings;
	}

	public String getPublicServerName() {
		return SledgeHammer.instance.getPublicServerName();
	}

	public void register(LogListener listener) {
		SledgeHammer.instance.register(listener);
	}
	
	public void register(ExceptionListener listener) {
		SledgeHammer.instance.register(listener);
	}

	public void register(String type, EventListener listener) {
		SledgeHammer.instance.register(type, listener);
	}

	public void register(String command, CommandListener listener) {
		SledgeHammer.instance.register(command, listener);
	}
	
	public ModuleChat getChatModule() {
		return (ModuleChat) SledgeHammer.instance.getModuleManager().getModuleByID(ModuleChat.ID);
	}

	public ChatManager getChatManager() {
		return SledgeHammer.instance.getChatManager();
	}
	
	/**
	 * Registers a <ChatChannel> properly.
	 * 
	 * @param chatChannel
	 *            The <ChatChannel> being registered.
	 */
	public void registerChatChannel(ChatChannel chatChannel) {
		if(chatChannel == null) {
			throw new IllegalArgumentException("ChatChannel given is null!");
		}
		// Grab the ChatManager.
		ChatManager chatManager = getChatManager();
		// Add the ChatChannel to the ChatManager.
		chatManager.addChatChannel(chatChannel);
	}
	
	/**
	 * Unregisters a given <ChatChannel>.
	 * @param chatChannel The <ChatChannel> being unregistered.
	 */
	public void unregisterChatChannel(ChatChannel chatChannel) {
		if(chatChannel == null) {
			throw new IllegalArgumentException("ChatChannel given is null!");
		}
		ModuleChat moduleChat = getChatModule();
		moduleChat.deleteChannel(chatChannel);
	}
	
	/**
	 * @param chatChannelName
	 *            The <String> name used to retrieve the <ChatChannel>
	 * @return Returns a <ChatChannel> if one exists with the given <String>
	 *         chatChannelName. Returns null if no <ChatChannel> exists without the
	 *         name provided.
	 */
	public ChatChannel getChatChannel(String chatChannelName) {
		return getChatManager().getChannel(chatChannelName);
	}

	public void sendGlobalMessage(String message) {
		ChatChannel global = getChatManager().getChannel("global");
		global.addMessage(message);
	}
	
	public List<Player> getPlayers() {
		return SledgeHammer.instance.getPlayers();
	}

	public boolean isLoaded() {
		return loaded;
	}

	/**
	 * Used to execute GenericEvent commands. This will be picked up by modules that @override this this.
	 * 
	 * @param type
	 * 
	 * @param context
	 */
	public void executeCommand(String type, String context) {
	}

	public abstract void onLoad();

	public abstract void onStart();

	public abstract void onUpdate(long delta);

	public abstract void onStop();

	public abstract void onUnload();

	public abstract String getID();

	public abstract String getVersion();
	
	public abstract String getModuleName();

	public abstract void onClientCommand(ClientEvent e);
}