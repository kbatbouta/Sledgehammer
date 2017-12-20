package sledgehammer.manager.core;

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
import java.io.InputStream;
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

import sledgehammer.SledgeHammer;
import sledgehammer.event.ClientEvent;
import sledgehammer.manager.Manager;
import sledgehammer.module.Module;
import sledgehammer.module.ModuleProperties;
import sledgehammer.module.core.ModuleChat;
import sledgehammer.module.core.ModuleCore;
import sledgehammer.module.faction.ModuleFactions;
import sledgehammer.module.permissions.ModulePermissions;
import sledgehammer.module.vanilla.ModuleVanilla;

/**
 * Manager class designed to manage Loading, Unloading, and updating modules.
 * 
 * TODO: Document
 * 
 * @author Jab
 */
public final class ModuleManager extends Manager {

	public static final String NAME = "ModuleManager";

	/**
	 * Debug boolean, used for verbose output.
	 */
	public static boolean DEBUG = false;

	/** Stores the list of plug-ins from SledgeHammer.ini Settings. */
	private String[] listPluginsRaw;
	/** Map for modules, organized by their associated IDs. */
	private Map<String, Module> mapModules;
	/** List for modules. */
	private List<Module> listModules;
	/** List of Modules, ready to be unloaded in the next update tick. */
	private List<Module> listUnloadNext;
	/** Handles core-level components of SledgeHammer. */
	private ModuleCore moduleCore;
	/**
	 * ModuleVanilla instance to communicate with vanilla commands, and handlers
	 * from the original game code. NOTE: This module's code is not accessible in
	 * respect to the proprietary nature of the game.
	 */
	private ModuleVanilla moduleVanilla;
	/** ModuleChat instance to handle chat operations for SledgeHammer. */
	private ModulePermissions modulePermissions;
	private ModuleChat moduleChat;
	private ModuleFactions moduleFactions;

	private ModuleProperties modulePropertiesCore;
	private ModuleProperties modulePropertiesVanilla;
	private ModuleProperties modulePropertiesPermissions;
	private ModuleProperties modulePropertiesChat;
	private ModuleProperties modulePropertiesFactions;

	private File directoryModules;
	
	/** Delta calculation variable for the update process. */
	private long timeThen = 0L;

	/**
	 * Main Constructor.
	 */
	public ModuleManager() {
		// Create the directory object, then make sure the folder is available to use.
		directoryModules = new File("plugins" + File.separator);
		if (!directoryModules.exists()) {
			directoryModules.mkdirs();
		}
		// Initialize the Lists.
		listModules = new ArrayList<>();
		listUnloadNext = new ArrayList<>();
		// Initialize the Maps.
		mapModules = new HashMap<>();
	}

	@Override
	public void onLoad(boolean debug) {
		loadDefaultModules();
		// Load the modules first.
		loadModules(debug);
	}

	/**
	 * Starts all plug-in modules loaded through SledgeHammer.
	 */
	@Override
	public void onStart() {
		Iterator<Module> modules = listModules.iterator();
		while (modules.hasNext()) {
			Module module = modules.next();
			if (module != null) {
				try {
					println("Starting module " + module.getName() + " Version: " + module.getVersion() + ".");
					module.startModule();
				} catch (Exception e) {
					println("Error starting module " + module.getName() + ": " + e.getMessage());
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

	/**
	 * Updates all active Module instances registered.
	 */
	@Override
	public void onUpdate() {
		// Grab the current time.
		long timeNow = System.currentTimeMillis();
		// Compare it with the last time the method has been called, calculating
		// the delta.
		long delta = timeNow - timeThen;
		// Grab the List of modules as a Iterator to prevent
		// ConcurrentModificationExceptions.
		Iterator<Module> modules = listModules.iterator();
		// Loop until all registered Modules have been accessed.
		while (modules.hasNext()) {
			// Grab the next Module.
			Module module = modules.next();
			// Make sure the Module instance is valid.
			if (module != null) {
				// If the list of Module instances to unload and remove contains
				// this Module instance, unload it, remove it, and continue to
				// the next Module instance in the list.
				if (listUnloadNext.contains(module)) {
					// Attempt to unload the module.
					unloadModule(module, false);
					modules.remove();
					continue;
				}
				// If the module is valid, and not to be removed, update it.
				updateModule(module, delta);
			}
		}
		// Go through the list of modules to be unloaded.
		modules = listUnloadNext.iterator();
		while (modules.hasNext()) {
			// Grab the next Module instance.
			Module module = modules.next();
			// If the Module instance is valid.
			if (module != null) {
				// If the Module instance is loaded, unload it.
				if (module.isLoaded()) {
					unloadModule(module, false);
				}
			}
			// Remove the Module instance from the list.
			modules.remove();
		}
	}

	/**
	 * Stops, and unloads all active, and registered Module instances.
	 */
	@Override
	public void onShutDown() {
		stopModules();
		unloadModules();
	}

	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * Returns the Core SledgeHammer Module instance.
	 * 
	 * @return
	 */
	@Override
	public ModuleCore getCoreModule() {
		return moduleCore;
	}

	/**
	 * Loads the Core Module services already included in SledgeHammer.
	 */
	private void loadDefaultModules() {
		try {
			// Instantiate the default Modules. @formatter:off
			modulePermissions = new ModulePermissions();
			moduleCore        = new ModuleCore();
			moduleVanilla     = new ModuleVanilla();
			moduleChat        = new ModuleChat();
			moduleFactions    = new ModuleFactions();
			// Create the ModuleProperties objects for each default module.
			modulePropertiesPermissions = new ModuleProperties(
					modulePermissions,
					"Permissions",
					"2.0_0",
					modulePermissions.getClass().getCanonicalName(),
					"Default permissions plug-in for Sledgehammer."
			);
			modulePropertiesCore = new ModuleProperties(
					moduleCore,
					"Core",
					"2.0_0",
					moduleCore.getClass().getCanonicalName(),
					"Core plug-in for Sledgehammer."
			);
			modulePropertiesVanilla = new ModuleProperties(
					moduleVanilla,
					"Vanilla",
					"1.3_0",
					moduleVanilla.getClass().getCanonicalName(),
					"Vanilla core plug-in for Sledgehammer."
			);
			modulePropertiesChat = new ModuleProperties(
					moduleChat,
					"Chat",
					"2.0_0",
					moduleChat.getClass().getCanonicalName(),
					"Chat plug-in for Sledgehammer."
			);
			modulePropertiesFactions = new ModuleProperties(
					moduleFactions,
					"Factions",
					"3.0_0",
					moduleFactions.getClass().getCanonicalName(),
					"Faction plug-in for Sledgehammer."
			);
			// Assign properties to each default module.
			modulePermissions.setProperties(modulePropertiesPermissions);
			moduleCore.setProperties(modulePropertiesCore);
			moduleVanilla.setProperties(modulePropertiesVanilla);
			moduleChat.setProperties(modulePropertiesChat);
			moduleFactions.setProperties(modulePropertiesFactions);
			// Register The default module.
			registerModule(modulePermissions);
			registerModule(moduleCore       );
			registerModule(moduleVanilla    );
			registerModule(moduleChat       );
			registerModule(moduleFactions   );
			// @formatter:on
		} catch (Exception e) {
			stackTrace("An Error occured while initializing Sledgehammer's core modules.", e);
		}
	}

	/**
	 * Loads the modules given from the plug-ins folder.
	 */
	void loadModules(boolean debug) {
		if (!debug) {
			SledgeHammer sledgeHammer = SledgeHammer.instance;
			listPluginsRaw = sledgeHammer.getSettings().getPluginList();
			println("Loading module(s).");
			if (listPluginsRaw != null && listPluginsRaw.length > 0) {
				for (String plugin : listPluginsRaw) {
					if (plugin != null && !plugin.isEmpty()) {
						try {
							Module module = loadPlugin(plugin);
							registerModule(module);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} else {
				println("No module(s) to load.");
			}
		}
		Iterator<Module> modules = listModules.iterator();
		while (modules.hasNext()) {
			Module module = modules.next();
			println("Loading module \"" + module.getName() + "\" Version: " + module.getVersion() + ".");
			if (module != null) {
				try {
					module.loadModule();
				} catch (Exception e) {
					stackTrace("Error loading module " + module.getName() + ": " + e.getMessage(), e);
					unloadModule(module, false);
					modules.remove();
				}
			}
		}
	}

	/**
	 * Attempts to stop all modules currently active.
	 */
	private void stopModules() {
		Iterator<Module> modules = listModules.iterator();
		while (modules.hasNext()) {
			Module module = modules.next();
			stopModule(module);
		}
	}

	/**
	 * Attempts to unload all modules.
	 */
	private void unloadModules() {
		Iterator<Module> modules = listModules.iterator();
		while (modules.hasNext()) {
			// Grab the next module in the list.
			Module module = modules.next();
			// If the module instance is valid, attempt to unload it.
			if (module != null) {
				unloadModule(module, false);
			}
			// Removes the Module instance from the List.
			modules.remove();
		}
		// Unload the core module last.
		// unloadModule(moduleCore, false);
	}

	/**
	 * Registers a SledgeHammer Module.
	 * 
	 * @param module
	 */
	public void registerModule(Module module) {
		SledgeHammer sledgeHammer = SledgeHammer.instance;
		synchronized (sledgeHammer) {
			if (module == null) {
				throw new IllegalArgumentException("Module is null!");
			}
			println("Registering Module: \"" + module.getName() + "\".");
			if (!listModules.contains(module)) {
				listModules.add(module);
			}
			Module mappedModule = mapModules.get(module.getClass().getName());
			if (mappedModule != null) {
				throw new IllegalArgumentException("Module ID for class "
						+ (module.getClass().getPackage() + "." + module.getClass().getName())
						+ " conflicts with the module already registered: "
						+ (mappedModule.getClass().getPackage() + "." + mappedModule.getClass().getName())
						+ ". If you are the author of this mod, you will need to change the ID to be unique. Otherwise, report this to the mod author.");
			} else {
				mapModules.put(module.getClass().getName(), module);
			}
		}
	}

	/**
	 * Attempts to update a module. If the module stack-traces, the module is
	 * unloaded.
	 * 
	 * @param module
	 * 
	 * @param delta
	 */
	private void updateModule(Module module, long delta) {
		if (module == null)
			throw new IllegalArgumentException("module is null!");
		if (delta < 0)
			return;
		try {
			module.updateModule(delta);
		} catch (Exception e) {
			println("Error updating module " + module.getName() + ": " + e.getMessage());
			for (StackTraceElement o : e.getStackTrace()) {
				println(o);
			}
			stopModule(module);
			unloadModule(module, false);
		}
	}

	/**
	 * Attempts to stop a given module, if active.
	 * 
	 * @param module
	 */
	private void stopModule(Module module) {
		try {
			println("Stopping module " + module.getName() + "...");
			module.stopModule();
		} catch (Exception e) {
			stackTrace("Failed to stop module " + module.getName() + ": " + e.getMessage(), e);
		}
	}

	/**
	 * Attempts to unload a module.
	 * 
	 * @param module
	 * 
	 * @param remove
	 */
	public void unloadModule(Module module, boolean remove) {
		// Make sure the Module instance is valid.
		if (module == null)
			throw new IllegalArgumentException("Module is null!");
		try {
			// Attempt to safely unload the module.
			module.unloadModule();
			// Remove the module if requested by the 'remove' parameter.
			if (remove) {
				mapModules.remove(module.getClass().getName());
			}
		} catch (Exception e) {
			stackTrace("Failed to unload module " + module.getName() + ": " + e.getMessage(), e);
		}
	}

	/**
	 * Loads a Jar plug-in for Sledgehammer.
	 * 
	 * @param name
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	private Module loadPlugin(String name) throws Exception {
		// The Module instance to return.
		Module instance = null;
		if (SledgeHammer.DEBUG) {
			println("Reading plugin: " + name + ".");
		}
		String pluginName = directoryModules.getAbsolutePath() + File.separator + name + ".jar";
		File pluginFile = new File(pluginName);
		if (!pluginFile.exists()) {
			throw new IllegalArgumentException("Jar file not found: " + pluginName);
		}
		ModuleProperties moduleProperties = getPluginProperties(pluginName);
		String moduleLocation = moduleProperties.getModuleLocation();
		if (moduleLocation.equals("unknown")) {
			throw new IllegalArgumentException("plugin.txt is not valid: " + pluginName);
		}
		URL url = pluginFile.toURI().toURL();
		URL[] urls = { url };
		ClassLoader loader = new URLClassLoader(urls);
		List<String> listClasses = new ArrayList<>();
		JarFile jarFile = new JarFile(pluginName);
		Enumeration<?> e = jarFile.entries();
		while (e.hasMoreElements()) {
			JarEntry entry = (JarEntry) e.nextElement();
			if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
				continue;
			}
			String className = entry.getName().substring(0, entry.getName().length() - 6);
			className = className.replace('/', '.');
			listClasses.add(className);
		}
		jarFile.close();
		try {
			// Loads all classes in the JAR file.
			for (String clazz : listClasses) {
				loader.loadClass(clazz);
			}
			Class<?> classToLoad = Class.forName(moduleLocation, true, loader);
			instance = (Module) classToLoad.newInstance();
			instance.setProperties(moduleProperties);
		} catch (Exception exception) {
			SledgeHammer.instance.stackTrace(exception);
		}
		return instance;
	}

	private static ModuleProperties getPluginProperties(String pluginName) {
		ModuleProperties returned = null;
		URL url;
		try {
			url = new URL("jar:file:" + pluginName + "!/plugin.yml");
			InputStream is = url.openStream();
			returned = new ModuleProperties(is);
			is.close();
		} catch (Exception e) {
			SledgeHammer.instance.stackTrace(e);
		}
		return returned;
	}

	public void handleClientCommand(ClientEvent e) {
		for (Module module : this.getLoadedModules()) {
			if (module.getModuleName().equalsIgnoreCase(e.getModule())) {
				module.onClientCommand(e);
			}
		}
	}

	/**
	 * Returns the Vanilla Module instance.
	 * 
	 * @return
	 */
	public ModuleVanilla getVanillaModule() {
		return moduleVanilla;
	}

	/**
	 * Sets the plug-ins to be loaded from the plug-ins folder.
	 * 
	 * @param list
	 */
	public void setPluginList(String[] list) {
		listPluginsRaw = list;
	}

	/**
	 * Unloads a module.
	 * 
	 * @param module
	 */
	public void queueUnloadModule(Module module) {
		this.listUnloadNext.add(module);
	}

	/**
	 * Returns a Module with a given ID.
	 * 
	 * @param ID
	 * @return
	 */
	public Module getModule(@SuppressWarnings("rawtypes") Class clazz) {
		return mapModules.get(clazz.getName());
	}

	/**
	 * Returns the list of all loaded modules.
	 * 
	 * @return
	 */
	public List<Module> getLoadedModules() {
		return this.listModules;
	}

	/**
	 * @return Returns the <File> directory for Modules..
	 */
	public File getModulesDirectory() {
		return directoryModules;
	}
}