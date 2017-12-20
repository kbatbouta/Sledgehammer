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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

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
 * 
 * @author Jab
 */
public abstract class Module extends Printable {

	private ModuleProperties properties = new ModuleProperties();
	private INI ini;
	private File iniFile;
	public boolean loadedSettings = false;
	private boolean loaded = false;
	private boolean started = false;

	/** The <File> directory for data to be placed. */
	private File directory = null;

	@Override
	public String getName() {
		return getProperties().getModuleName();
	}

	public void loadSettings(ModuleSettingsHandler handler) {
		if (handler == null) {
			throw new IllegalArgumentException("Settings Handler given is null!");
		}
		loadedSettings = false;
		if (ini == null) {
			getINI();
		}
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

	/**
	 * Registers a <ChatChannel> properly.
	 * 
	 * @param chatChannel
	 *            The <ChatChannel> being registered.
	 */
	public void registerChatChannel(ChatChannel chatChannel) {
		if (chatChannel == null) {
			throw new IllegalArgumentException("ChatChannel given is null!");
		}
		// Grab the ChatManager.
		ChatManager chatManager = getChatManager();
		// Add the ChatChannel to the ChatManager.
		chatManager.addChatChannel(chatChannel);
	}

	/**
	 * Unregisters a given <ChatChannel>.
	 * 
	 * @param chatChannel
	 *            The <ChatChannel> being unregistered.
	 */
	public void unregisterChatChannel(ChatChannel chatChannel) {
		if (chatChannel == null) {
			throw new IllegalArgumentException("ChatChannel given is null!");
		}
		ModuleChat moduleChat = getChatModule();
		moduleChat.deleteChannel(chatChannel);
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
			iniFile = new File("plugins" + File.separator + getProperties().getModuleName() + ".ini");
			ini = new INI(iniFile);
		}
		return this.ini;
	}

	public void updateModule(long delta) {
		if (started) {
			onUpdate(delta);
		}
	}

	public void unload() {
		started = false;
		getModuleManager().unloadModule(this, true);
	}

	public void sendGlobalMessage(String message) {
		ChatChannel global = getChatManager().getChannel("global");
		global.addMessage(message);
	}

	public void register(CommandListener listener) {
		SledgeHammer.instance.register(listener);
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

	public EventManager getEventManager() {
		return SledgeHammer.instance.getEventManager();
	}

	public ModuleManager getModuleManager() {
		return SledgeHammer.instance.getModuleManager();
	}

	public String getPermissionDeniedMessage() {
		return SledgeHammer.instance.getPermissionsManager().getPermissionDeniedMessage();
	}

	public void handleEvent(Event event, boolean shouldLog) {
		SledgeHammer.instance.handle(event, shouldLog);
	}

	public void handleEvent(Event event) {
		SledgeHammer.instance.handle(event);
	}

	public Module getModule(@SuppressWarnings("rawtypes") Class clazz) {
		return getModuleManager().getModule(clazz);
	}

	public PermissionsManager getPermissionsManager() {
		return SledgeHammer.instance.getPermissionsManager();
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
		return (ModuleChat) getModule(ModuleChat.class);
	}

	public ChatManager getChatManager() {
		return SledgeHammer.instance.getChatManager();
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

	public List<Player> getPlayers() {
		return SledgeHammer.instance.getPlayers();
	}

	public boolean isLoaded() {
		return loaded;
	}

	/**
	 * Used to execute GenericEvent commands. This will be picked up by modules
	 * that @override this this.
	 * 
	 * @param type
	 * 
	 * @param context
	 */
	public void executeCommand(String type, String context) {
	}

	public String getVersion() {
		return getProperties().getModuleVersion();
	}

	public ModuleProperties getProperties() {
		return this.properties;
	}

	public void setProperties(ModuleProperties properties) {
		this.properties = properties;
	}

	/**
	 * FIXME: A Module's name needs to be defined in the YML.
	 * 
	 * @return
	 */
	public String getModuleName() {
		return getProperties().getModuleName();
	}

	/**
	 * Fired when the <Module> is loaded.
	 * 
	 * (Override this method if you want to execute code in your module when it
	 * loads)
	 */
	public void onLoad() {
	}

	/**
	 * Fired when the <Module> is started.
	 * 
	 * (Override this method if you want to execute code in your module when it
	 * starts)
	 */
	public void onStart() {
	}

	/**
	 * Fired when the <Module> is updated.
	 * 
	 * (Override this method if you want to execute code in your module when the
	 * server updates)
	 * 
	 * @param delta
	 *            The delta in milliseconds since the last update.
	 */
	public void onUpdate(long delta) {
	}

	/**
	 * Fired when the <Module> is stopped.
	 * 
	 * (Override this method if you want to execute code in your module when it
	 * stops)
	 * 
	 */
	public void onStop() {
	}

	/**
	 * Fired when the <Module is unloaded.
	 * 
	 * (Override this method if you want to execute code in your module when it
	 * unloads)
	 */
	public void onUnload() {
	}

	/**
	 * Fired when a ClientCommand is sent from a Player's client.
	 * 
	 * @param event
	 *            the <ClientEvent> container for the event.
	 */
	public void onClientCommand(ClientEvent event) {
	}

	/**
	 * @return Returns the <File> directory for the <Module> to store data.
	 */
	public File getModuleDirectory() {
		ModuleManager managerModule = getModuleManager();
		if (directory == null) {
			directory = new File(managerModule.getModulesDirectory(), getProperties().getModuleName());
			if (!directory.exists()) {
				directory.mkdirs();
			}
		}
		return directory;
	}

	public void saveResource(String path) {
		saveResource(path, false);
	}

	public void saveResource(String path, boolean overwrite) {
		File file = new File(getModuleDirectory(), path);
		if (!overwrite && file.exists()) {
			return;
		}
		write("jar:file:" + properties.getJarLocation() + "!/" + path, file);
	}

	public void saveResourceAs(String jarPath, String destPath) {
		saveResourceAs(jarPath, destPath, false);
	}

	public void saveResourceAs(String jarPath, String destPath, boolean overwrite) {
		File file = new File(getModuleDirectory(), destPath);
		if (!overwrite && file.exists()) {
			return;
		}
		write("jar:file:" + properties.getJarLocation() + "!/" + jarPath, file);
	}

	private static void write(String urlString, File file) {
		try {
			URL url = new URL(urlString);
			InputStream is = url.openStream();
			OutputStream os = new FileOutputStream(file);
			byte[] buffer = new byte[102400];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			is.close();
			os.flush();
			os.close();
		} catch (Exception e) {
			SledgeHammer.instance.stackTrace(e);
		}
	}
}