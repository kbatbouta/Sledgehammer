package sledgehammer;

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

import sledgehammer.module.Module;
import sledgehammer.util.Printable;
import sledgehammer.util.ZUtil;

public final class ModuleManager extends Printable {

	/**
	 * Debug boolean, used for verbose output.
	 */
	public static boolean DEBUG = false;

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
	 * String Array to store the list of the plugins from SledgeHammer.ini
	 * Settings.
	 */
	private String[] listPluginsRaw;

	private long timeThen = 0L;

	/**
	 * Main Constructor.
	 * 
	 * @param instance
	 */
	ModuleManager(SledgeHammer instance) {
		sledgeHammer = instance;
		listModules = new ArrayList<>();
		mapModules = new HashMap<>();
		listUnloadNext = new ArrayList<>();
	}

	/**
	 * Registers a SledgeHammer Module.
	 * 
	 * @param module
	 */
	void registerModule(Module module) {
		
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

	void loadModules() {

		initPluginFolder();
		
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

		// onLoad()
		Iterator<Module> modules = listModules.iterator();
		while (modules.hasNext()) {
			Module module = modules.next();
			if (module != null) {
				try {
					module.loadModule();
				} catch (Exception e) {
					println("Error loading module " + module.getName() + ": " + e.getMessage());
					for (StackTraceElement o : e.getStackTrace()) {
						println(o);
					}
					unloadModule(module);
					modules.remove();
				}
			}
		}
	}

	public void start() {
		
		// Load the modules first.
		loadModules();
		
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
					unloadModule(module);
					modules.remove();
				}
			}
		}
	}

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
			unloadModule(module);
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
		// stopModule(moduleCore);

	}

	private void stopModule(Module module) {
		try {
			println("Stopping module " + module.getName() + "...");
			module.stopModule();
		} catch (Exception e) {
			println("Failed to stop module " + module.getName() + ": " + e.getMessage());
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
				unloadModule(module);
			modules.remove();
		}

		// Unload the core module last.
		// unloadModule(moduleCore, false);
	}

	public void unloadModule(Module module) {
		if (module == null)
			throw new IllegalArgumentException("Module is null!");
		try {
			module.unloadModule();
			mapModules.remove(module.getID());
		} catch (Exception e) {
			println("Failed to unload module " + module.getName() + ": " + e.getMessage());
			for (StackTraceElement o : e.getStackTrace()) {
				println(o);
			}
		}
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

	public String getName() {
		return "ModuleManager";
	}

	public void update() {
		long timeNow = System.currentTimeMillis();
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

		// Go through the list of modules to be unloaded.
		modules = listUnloadNext.iterator();
		while (modules.hasNext()) {
			
			// Grab the module.
			Module module = modules.next();
			
			// Unload the module.
			unloadModule(module);
			
			// Remove the module from the list.
			modules.remove();
		}

	}

	private Module loadPlugin(String name) throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		if (SledgeHammer.DEBUG)
			println("Loading plugin: " + name + ".");
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

		// ClassLoader mainLoader = ClassLoader.getSystemClassLoader();
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

		Module instance = null;

		String dClazz = null;

		try {
			for (String clazz : listClasses) {
				dClazz = clazz;
				loader.loadClass(clazz);
			}
			if (SledgeHammer.DEBUG)
				println("Class.forName(" + module + ", true, " + loader + ");");
			Class<?> classToLoad = Class.forName(module, true, loader);
			instance = (Module) classToLoad.newInstance();
			instance.setPluginSettings(pluginSettings);
			instance.setJarName(name);
		} catch (ClassNotFoundException e1) {
			if (SledgeHammer.DEBUG)
				println("loader.loadClass(" + dClazz + ");");
			e1.printStackTrace();
		}
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

	public void shutdown() {
		stopModules();
		unloadModules();
	}

	/**
	 * Initializes the plug-in folder, before using it.
	 */
	public static void initPluginFolder() {
		if (!ZUtil.pluginFolder.exists())
			ZUtil.pluginFolder.mkdirs();
	}

	void setPluginList(String[] list) {
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
	
	SledgeHammer getSledgeHammer() {
		return sledgeHammer;
	}

}
