package sledgehammer.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import sledgehammer.module.Module;
import sledgehammer.modules.ModuleMonitor;
import sledgehammer.modules.core.ModuleCore;
import sledgehammer.modules.vanilla.ModuleVanilla;
import sledgehammer.util.Printable;
import sledgehammer.util.ZUtil;

/**
 * Manager class designed to manage Loading, Unloading, and updating modules.
 * 
 * @author Jab
 *
 */
public final class ModuleManager extends Printable {

	public static final String NAME = "ModuleManager";
	
	/**
	 * Debug boolean, used for verbose output.
	 */
	public static boolean DEBUG = false;

	/**
	 * Instance of SledgeHammer. While this is statically accessible through the
	 * singleton, maintaining an OOP hierarchy is a good practice.
	 */
	private SledgeHammer sledgeHammer = null;

	/**
	 * Map for modules, organized by their associated IDs.
	 */
	private Map<String, Module> mapModules;

	/**
	 * List for modules.
	 */
	private List<Module> listModules;

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
	 * ModuleCore instance to handle core-level components of SledgeHammer.
	 */
	private ModuleCore moduleCore;

	/**
	 * String Array to store the list of the plugins from SledgeHammer.ini
	 * Settings.
	 */
	private String[] listPluginsRaw;

	/**
	 * Delta calculation variable for the update process.
	 */
	private long timeThen = 0L;

	/**
	 * Main Constructor.
	 * 
	 * @param instance
	 */
	public ModuleManager(SledgeHammer instance) {
		
		// Set the SledgeHammer instance using this manager.
		sledgeHammer   = instance         ;
		// Initialize the Lists.
		listModules    = new ArrayList<>();
		listUnloadNext = new ArrayList<>();
		// Initialize the Maps.
		mapModules     = new HashMap<>()  ;
	}
	
	/**
	 * Loads the Core Module services already included in SledgeHammer.
	 */
	private void loadDefaultModules() {
		try {
			
			moduleVanilla = new ModuleVanilla();
			moduleCore    = new ModuleCore()   ;
			
			if (DEBUG) registerModule(new ModuleMonitor());
	
			registerModule(moduleVanilla);
			registerModule(moduleCore   );
			
		} catch(Exception e) {
			
			stackTrace("An Error occured while initializing Sledgehammer's core modules.", e);
		}
		
	}
	
	public void load() {
		loadDefaultModules();
		
		// Load the modules first.
		loadModules();
	}
	
	/**
	 * Starts all plug-in modules loaded through SledgeHammer.
	 */
	public void start() {
		
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
	public void update() {
		
		// Grab the current time.
		long timeNow = System.currentTimeMillis();
		
		// Compare it with the last time the method has been called, calculating the delta.
		long delta = timeNow - timeThen;
		
		// Grab the List of modules as a Iterator to prevent ConcurrentModificationExceptions.
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
				if(listUnloadNext.contains(module)) {
				
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
			if(module != null) {
				
				// If the Module instance is loaded, unload it.
				if(module.isLoaded()) {
					unloadModule(module, false);
				}
				
			}

			// Remove the Module instance from the list.
			modules.remove();
		}

	}

	/**
	 * Loads the modules given from the plug-ins folder.
	 */
	void loadModules() {

		// Ensures a plug-in folder exists.
		if (!ZUtil.pluginFolder.exists()) ZUtil.pluginFolder.mkdirs();
		
		listPluginsRaw = sledgeHammer.getSettings().getPluginList();

		println("Loading modules.");

		
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

		Iterator<Module> modules = listModules.iterator();
		
		while (modules.hasNext()) {
		
			Module module = modules.next();
			
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
			if (module != null) unloadModule(module, false);
			
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
		
		synchronized (sledgeHammer) {
			if (module == null) throw new IllegalArgumentException("Module is null!");

			if (!listModules.contains(module)) {
				listModules.add(module);
			}
			Module mappedModule = mapModules.get(module.getID());
			if (mappedModule != null) {
				throw new IllegalArgumentException("Module ID for class "
						+ (module.getClass().getPackage() + "." + module.getClass().getName())
						+ " conflicts with the module already registered: "
						+ (mappedModule.getClass().getPackage() + "." + mappedModule.getClass().getName())
						+ ". If you are the author of this mod, you will need to change the ID to be unique. Otherwise, report this to the mod author.");
			} else {
				mapModules.put(module.getID(), module);
			}

		}
	}
	
	/**
	 * Attempts to update a module. If the module stack-traces, the module is unloaded.
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
		if (module == null) throw new IllegalArgumentException("Module is null!");
		
		try {
			
			// Attempt to safely unload the module.
			module.unloadModule();
			
			// Remove the module if requested by the 'remove' parameter.
			if(remove) mapModules.remove(module.getID());
		
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
		
		if (SledgeHammer.DEBUG) println("Reading plugin: " + name + ".");
		
		String pluginName = ZUtil.pluginLocation + name + ".jar";
		
		File pluginFile = new File(pluginName);
		
		if (!pluginFile.exists()) throw new IllegalArgumentException("Jar file not found: " + pluginName);

		Map<String, String> pluginSettings = getPluginSettings(pluginName);
		
		String module = pluginSettings.get("module");
		
		if (module == null) throw new IllegalArgumentException("plugin.txt is not valid: " + pluginName);

		URL url = pluginFile.toURI().toURL();
		
		URL[] urls = { url };

		ClassLoader loader = new URLClassLoader(urls);

		List<String> listClasses = new ArrayList<>();

		JarFile jarFile = new JarFile(pluginName);

		Enumeration<?> e = jarFile.entries();
		
		while (e.hasMoreElements()) {
		
			JarEntry entry = (JarEntry) e.nextElement();
			
			if (entry.isDirectory() || !entry.getName().endsWith(".class")) continue;
			
			String className = entry.getName().substring(0, entry.getName().length() - 6);
			
			className = className.replace('/', '.');
			
			listClasses.add(className);
		}
		
		jarFile.close();

		try {
			
			// Loads all classes in the JAR file.
			for (String clazz : listClasses) loader.loadClass(clazz);
			
			if (SledgeHammer.DEBUG) println("Class.forName(" + module + ", true, " + loader + ");");
			
			Class<?> classToLoad = Class.forName(module, true, loader);
			
			instance = (Module) classToLoad.newInstance();
			
			instance.setPluginSettings(pluginSettings);
			
			instance.setJarName(name);
		
		} catch (Exception exception) {
			SledgeHammer.instance.stackTrace(exception);
		}
		
		return instance;
	}

	/**
	 * Reads the 'plugin.txt' file from the plug-in JAR file.
	 * 
	 * TODO: Move with ClassLoader methods to a utility class.
	 * 
	 * @param fileName
	 *
	 * @return
	 */
	private static Map<String, String> getPluginSettings(String fileName) {
		
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
			
			reader.close();
			is.close();
		} catch (Exception e) {
			SledgeHammer.instance.stackTrace(e);
		}
		
		return listSettings;
	}

	/**
	 * Stops, and unloads all active, and registered Module instances.
	 */
	public void shutdown() {
		stopModules();
		unloadModules();
	}
	
	/**
	 * Returns the Core SledgeHammer Module instance.
	 * 
	 * @return
	 */
	public ModuleCore getCoreModule() {
		return moduleCore;
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
	public Module getModuleByID(String ID) {
		return mapModules.get(ID);
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
	 * Returns the SledgeHammer instance using this manager.
	 * @return
	 */
	SledgeHammer getSledgeHammer() {
		return sledgeHammer;
	}
	
	@Override
	public String getName() { return NAME; }

	
	
}
