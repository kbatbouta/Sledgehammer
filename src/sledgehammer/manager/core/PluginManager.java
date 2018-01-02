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
package sledgehammer.manager.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.Plugin;
import sledgehammer.SledgeHammer;
import sledgehammer.event.ClientEvent;
import sledgehammer.manager.Manager;
import sledgehammer.module.Module;
import sledgehammer.module.chat.ModuleChat;
import sledgehammer.module.core.ModuleCore;
import sledgehammer.module.faction.ModuleFactions;
import sledgehammer.module.permissions.ModulePermissions;
import sledgehammer.module.vanilla.ModuleVanilla;

/**
 * TODO: Document
 * 
 * @author Jab
 */
public class PluginManager extends Manager {

	/**
	 * Debug boolean, used for verbose output.
	 */
	public static boolean DEBUG = true;

	private Map<String, Plugin> mapPlugins;

	private Plugin pluginSledgehammer;
	private List<Plugin> listPluginsToLoad;
	private List<Plugin> listPluginsToStart;
	private List<Plugin> listPluginsStarted;
	private List<Plugin> listPluginsToUnload;

	private ModulePermissions modulePermissions;
	private ModuleCore moduleCore;
	private ModuleVanilla moduleVanilla;
	private ModuleChat moduleChat;
	private ModuleFactions moduleFactions;

	private File directory;
	private long timeThen;

	public PluginManager() {

	}

	// @formatter:off
	@Override
	public void onLoad(boolean debug) {
		listPluginsToLoad = new ArrayList<>();
		listPluginsToStart = new ArrayList<>();
		listPluginsStarted = new ArrayList<>();
		listPluginsToUnload = new ArrayList<>();
		// Create the Map to store all the Plugins.
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
	 * Loads the Core Plugin with all the Modules for the core.
	 */
	private void loadCorePlugin() {
		pluginSledgehammer = new Plugin(SledgeHammer.getJarFile());
		pluginSledgehammer.setLoadClasses(false);
		pluginSledgehammer.load();
		registerPlugin(pluginSledgehammer);
	}

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

	public void handleClientCommand(ClientEvent event) {
		String moduleId = event.getModuleName();
		println("handleClientCommand(" + moduleId + ", " + event.getCommand() + ");");
		boolean foundModule = false;
		for (Plugin plugin : getPlugins()) {

			for (Module module : plugin.getModules()) {
				String clientModuleId = module.getClientModuleId();
				// println("\t-> Next Module: " + module.getModuleName() + " ClientModuleId: " +
				// clientModuleId);
				if (clientModuleId.equalsIgnoreCase(event.getModuleName())) {
					foundModule = true;
					// println("\t\tFound! Executing ClientCommand.");
					module.onClientCommand(event);
					break;
				}
			}
			if (foundModule) {
				break;
			}
		}
	}

	private Collection<Plugin> getPlugins() {
		return mapPlugins.values();
	}

	public Plugin loadPlugin(File jar) {
		if (!jar.exists()) {
			throw new IllegalArgumentException("Jar file not found: " + jar.getAbsolutePath());
		}
		Plugin plugin = new Plugin(jar);
		plugin.load();
		return plugin;
	}

	public void registerPlugin(Plugin plugin) {
		this.listPluginsToLoad.add(plugin);
	}

	private File getPluginDirectory() {
		if (directory == null) {
			directory = new File("plugins" + File.separator);
		}
		return directory;
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

	public ModulePermissions getPermissionsModule() {
		return this.modulePermissions;
	}

	public ModuleCore getCoreModule() {
		return this.moduleCore;
	}

	public ModuleVanilla getVanillaModule() {
		return this.moduleVanilla;
	}

	public ModuleChat getChatModule() {
		return this.moduleChat;
	}

	public ModuleFactions getFactionsModule() {
		return this.moduleFactions;
	}

	public Plugin getSledgehammerPlugin() {
		return this.pluginSledgehammer;
	}

	public File getDirectory() {
		return this.directory;
	}
}