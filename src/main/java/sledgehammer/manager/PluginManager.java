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
package sledgehammer.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.event.ClientEvent;
import sledgehammer.module.chat.ModuleChat;
import sledgehammer.module.core.ModuleCore;
import sledgehammer.module.faction.ModuleFactions;
import sledgehammer.module.permissions.ModulePermissions;
import sledgehammer.module.vanilla.ModuleVanilla;
import sledgehammer.plugin.Module;
import sledgehammer.plugin.Plugin;

/**
 * Manager to handle <Plugin> data and operations for the Sledgehammer engine.
 * 
 * @author Jab
 */
public class PluginManager extends Manager {

	/** Debug boolean, used for verbose output. */
	public static boolean DEBUG = true;

	/** The <Map> of loaded <Plugin>'s identified by their <String> name. */
	private Map<String, Plugin> mapPlugins;
	/** The Core <Plugin>. */
	private Plugin pluginSledgehammer;

	/** The <List> of <Plugin>'s to load. */
	private List<Plugin> listPluginsToLoad;
	/** The <List> of <Plugin>'s to start. */
	private List<Plugin> listPluginsToStart;
	/** The <List> of <Plugin>'s that have started. */
	private List<Plugin> listPluginsStarted;
	/** The <List> of <Plugin>'s to unload. */
	private List<Plugin> listPluginsToUnload;
	/** The <ModulePermissions> instance in the Core plug-in. */
	private ModulePermissions modulePermissions;
	/** The <ModuleCore> instance in the Core plug-in. */
	private ModuleCore moduleCore;
	/** The <ModuleVanilla> instance in the Core plug-in. */
	private ModuleVanilla moduleVanilla;
	/** The <ModuleChat> instance in the Core plug-in. */
	private ModuleChat moduleChat;
	/** The <ModuleFactions> instance in the Core plug-in. */
	private ModuleFactions moduleFactions;
	/** The <File> Object of the directory for <Plugin>'s to load. */
	private File directory;
	/** A <Long> value to store the last time the <Plugin>'s were updated. */
	private long timeThen;

	// @formatter:off
	@Override
	public void onLoad(boolean debug) {
		// Construct the Lists @formatter:off
		listPluginsToLoad   = new ArrayList<>();
		listPluginsToStart  = new ArrayList<>();
		listPluginsStarted  = new ArrayList<>();
		listPluginsToUnload = new ArrayList<>();
		// Create the Map to store all the Plugins. @formatter:on
		mapPlugins = new HashMap<>();
		// Load the core Plugin first.
		loadCorePlugin();
		// Store the core modules for reference after unloading. @formatter:off
		modulePermissions = pluginSledgehammer.getModule(ModulePermissions.class);
		moduleCore        = pluginSledgehammer.getModule(ModuleCore.class       );
		moduleVanilla     = pluginSledgehammer.getModule(ModuleVanilla.class    );
		moduleChat        = pluginSledgehammer.getModule(ModuleChat.class       );
		moduleFactions    = pluginSledgehammer.getModule(ModuleFactions.class   );
		// @formatter:on
		if (!debug) {
			loadInstalledPlugins();
		}
		for (Plugin plugin : listPluginsToLoad) {
			println("Loading plug-in " + plugin.getPluginName() + "'s module(s):");
			plugin.loadModules();
			listPluginsToStart.add(plugin);
		}
		listPluginsToLoad.clear();
	}

	@Override
	public void onStart() {
		for (Plugin plugin : listPluginsToStart) {
			println("Starting plug-in " + plugin.getPluginName() + "'s module(s):");
			plugin.startModules();
			listPluginsStarted.add(plugin);
			mapPlugins.put(plugin.getPluginName(), plugin);
		}
		listPluginsToStart.clear();
	}

	@Override
	public void onUpdate() {
		// Grab the current time.
		long timeNow = System.currentTimeMillis();
		// Compare it with the last time the method has been called, calculating
		// the delta.
		long delta = timeNow - timeThen;
		for (Plugin plugin : getPlugins()) {
			plugin.updateModules(delta);
		}
		// Store the current time to compare for the next cycle.
		timeThen = timeNow;
	}

	@Override
	public void onShutDown() {
		for (Plugin plugin : listPluginsStarted) {
			println("Stopping plug-in " + plugin.getPluginName() + "'s module(s):");
			plugin.stopModules();
			listPluginsToUnload.add(plugin);
		}
		listPluginsStarted.clear();
		mapPlugins.clear();
		for (Plugin plugin : listPluginsToUnload) {
			println("Unloading plug-in " + plugin.getPluginName() + "'s module(s):");
			plugin.unloadModules();
		}
		listPluginsToUnload.clear();
	}

	@Override
	public String getName() {
		return "PluginManager";
	}

	/**
	 * (Private Method)
	 * 
	 * Loads the Core <Plugin> with all the Modules for the core.
	 */
	private void loadCorePlugin() {
		pluginSledgehammer = new Plugin(SledgeHammer.getJarFile());
		pluginSledgehammer.setLoadClasses(false);
		pluginSledgehammer.load();
		registerPlugin(pluginSledgehammer);
	}

	/**
	 * (Private Method)
	 * 
	 * Loads <Plugin>'s installed in the plugin directory.
	 */
	private void loadInstalledPlugins() {
		println("Loading plug-in module(s).");
		File[] plugins = getPluginDirectory().listFiles();
		if (plugins.length > 0) {
			int loadedModules = 0;
			int loadedPlugins = 0;
			for (File jar : plugins) {
				if (jar.isFile()) {
					String fileName = jar.getName();
					String fileExtension = fileName.split("\\.")[1].toLowerCase();
					if (fileExtension.endsWith("jar")) {
						try {
							if (SledgeHammer.DEBUG) {
								println("Reading plugin: " + jar.getName().split("\\.")[0] + ".");
							}
							Plugin plugin = loadPlugin(jar);
							registerPlugin(plugin);
							loadedModules += plugin.getModules().size();
							loadedPlugins++;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			println("Loaded " + loadedPlugins + " plugin(s).");
			println("Registered " + loadedModules + " plug-in module(s).");
		} else {
			println("Loaded no plug-in(s).");
			println("Registered no plug-in module(s).");
		}
	}

	/**
	 * Passes a ClientEvent to the Module identifying with the Client ID given, and
	 * handles the ClientEvent.
	 * 
	 * @param event
	 *            The <ClientEvent> to handle.
	 */
	public void handleClientCommand(ClientEvent event) {
		boolean foundModule = false;
		for (Plugin plugin : getPlugins()) {
			for (Module module : plugin.getModules()) {
				String clientModuleId = module.getClientModuleId();
				if (clientModuleId.equalsIgnoreCase(event.getModuleName())) {
					foundModule = true;
					module.onClientCommand(event);
					break;
				}
			}
			if (foundModule) {
				break;
			}
		}
	}

	/**
	 * @param jar
	 *            The <File> Object of the Jar File. If the File does not exist, an
	 *            IllegalArgumentException is thrown.
	 * @return Returns a loaded <Plugin> with the given Jar File.
	 */
	public Plugin loadPlugin(File jar) {
		if (!jar.exists()) {
			throw new IllegalArgumentException("Jar file not found: " + jar.getAbsolutePath());
		}
		Plugin plugin = new Plugin(jar);
		plugin.load();
		return plugin;
	}

	/**
	 * Returns a registered. <Module> with the given <Class>.
	 * 
	 * @param clazz
	 *            The Class of the Module.
	 * @return Returns a <Module> with the given <Class>.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Module> T getModule(Class<? extends Module> clazz) {
		Module returned = null;
		for (Plugin plugin : getPlugins()) {
			for (Module module : plugin.getModules()) {
				if (module.getClass().equals(clazz)) {
					returned = module;
					break;
				}
			}
			if (returned != null) {
				break;
			}
		}
		return (T) returned;
	}

	/**
	 * @return Returns the <File> Object for the directory that <Plugin>'s are
	 *         installed.
	 */
	public File getPluginDirectory() {
		if (directory == null) {
			directory = new File("plugins" + File.separator);
		}
		return directory;
	}

	/**
	 * Registers a given <Plugin> by adding it to the <List> of Plug-ins to load.
	 * 
	 * @param plugin
	 *            The <Plugin> to register.
	 */
	public void registerPlugin(Plugin plugin) {
		this.listPluginsToLoad.add(plugin);
	}

	/**
	 * @return Returns a <Collection> of the loaded <Plugin>'s.
	 */
	private Collection<Plugin> getPlugins() {
		return mapPlugins.values();
	}

	/**
	 * @return Returns the <ModulePermissions> instance in the Core Plug-in.
	 */
	public ModulePermissions getPermissionsModule() {
		return this.modulePermissions;
	}

	/**
	 * @return Returns the <ModuleCore> instance in the Core Plug-in.
	 */
	public ModuleCore getCoreModule() {
		return this.moduleCore;
	}

	/**
	 * @return Returns the <ModuleVanilla> instance in the Core Plug-in.
	 */
	public ModuleVanilla getVanillaModule() {
		return this.moduleVanilla;
	}

	/**
	 * @return Returns the <ModuleChat> instance in the Core Plug-in.
	 */
	public ModuleChat getChatModule() {
		return this.moduleChat;
	}

	/**
	 * @return Returns the <ModuleFactions> instance in the Core Plug-in.
	 */
	public ModuleFactions getFactionsModule() {
		return this.moduleFactions;
	}

	/**
	 * @return Returns the <Plugin> instance of the Core Plug-in.
	 */
	public Plugin getSledgehammerPlugin() {
		return this.pluginSledgehammer;
	}
}