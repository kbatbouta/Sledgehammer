package sledgehammer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import sledgehammer.lua.core.ModuleProperties;
import sledgehammer.lua.core.PluginProperties;
import sledgehammer.module.Module;
import sledgehammer.util.Printable;

public class Plugin extends Printable {

	private static final boolean DEBUG = false;

	private boolean loadClasses = true;
	
	private File file;

	private PluginProperties properties;

	List<Module> listModulesToLoad;
	List<Module> listModulesLoaded;
	List<Module> listModulesToStart;
	List<Module> listModulesStarted;
	List<Module> listModulesStoppped;

	private Map<String, Module> mapModules;

	List<Module> listModulesStopped = new ArrayList<>();
	List<Module> listModulesUnloaded = new ArrayList<>();

	private ClassLoader classLoader;

	public Plugin(File file) {
		setJarFile(file);
		mapModules = new HashMap<>();
		listModulesToLoad = new ArrayList<>();
		listModulesLoaded = new ArrayList<>();
		listModulesToStart = new ArrayList<>();
		listModulesStarted = new ArrayList<>();
		listModulesStopped = new ArrayList<>();
	}

	public Plugin(File file, PluginProperties properties) {
		setJarFile(file);
		setProperties(properties);
		mapModules = new HashMap<>();
		listModulesToLoad = new ArrayList<>();
		listModulesLoaded = new ArrayList<>();
		listModulesToStart = new ArrayList<>();
		listModulesStarted = new ArrayList<>();
		listModulesStopped = new ArrayList<>();
	}

	@Override
	public String getName() {
		return "Plugin (" + getPluginName() + ")";
	}

	public void load() {
		loadProperties();
		instantiateModules();
	}

	public void reload() {
		if (mapModules.size() > 0) {
			if (listModulesStarted.size() > 0) {
				stopModules();
			}
			if (listModulesLoaded.size() > 0) {
				unloadModules();
			}
		}
		load();
	}

	private void loadProperties() {
		try {
			InputStream inputStream = getStream(getJarFile(), "plugin.yml");
			setProperties(new PluginProperties(inputStream));
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void instantiateModules() {
		try {
			File fileJar = getJarFile();
			if(loadClasses()) {				
				setClassLoader(loadJarClasses(fileJar));
			}
			List<String> listModuleNames = getProperties().getModuleNames();
			for (String moduleName : listModuleNames) {
				instantiateModule(moduleName);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public Module instantiateModule(String moduleName) {
		Module module = null;
		ModuleProperties moduleProperties = getProperties().getModuleProperties(moduleName);
		Class<?> classToLoad;
		try {
			classToLoad = Class.forName(moduleProperties.getModuleLocation(), true, getClassLoader());
			module = (Module) classToLoad.newInstance();
			module.setProperties(moduleProperties);
			module.setPlugin(this);
			addModule(module);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		mapModules.put(module.getModuleName(), module);
		return module;
	}

	public void loadModules() {
		synchronized(this) {			
			for (Module module : listModulesToLoad) {
				if (loadModule(module)) {
					listModulesToStart.add(module);
					listModulesLoaded.add(module);
				}
			}
			listModulesToLoad.clear();
		}
	}

	public void startModules() {
		synchronized(this) {
			for (Module module : listModulesToStart) {
				if (startModule(module)) {
					listModulesStarted.add(module);
				}
			}
			listModulesToStart.clear();
		}
	}

	public void updateModules(long delta) {
		synchronized(this) {
			if(listModulesToLoad.size() > 0) {			
				loadModules();
			}
			if(listModulesToStart.size() > 0) {
				startModules();
			}
			List<Module> listModulesToUpdate = new ArrayList<>();
			listModulesToUpdate.addAll(listModulesStarted);
			for (Module module : listModulesToUpdate) {
				if (!module.isStarted()) {
					listModulesStopped.add(module);
					continue;
				}
				updateModule(module, delta);
			}
			for (Module module : getStartedModules()) {
				if (!module.isStarted()) {
					listModulesStopped.add(module);
				}
			}
			for (Module module : getLoadedModules()) {
				if (!module.isLoaded()) {
					listModulesUnloaded.add(module);
				}
			}
			if (listModulesStopped.size() > 0) {
				for (Module module : listModulesStopped) {
					listModulesStarted.remove(module);
				}
				listModulesStopped.clear();
			}
			if (listModulesUnloaded.size() > 0) {
				for (Module module : listModulesUnloaded) {
					listModulesLoaded.remove(module);
				}
				listModulesUnloaded.clear();
			}
		}
	}

	public void stopModules() {
		synchronized(this) {
			for (Module module : listModulesStarted) {
				if (module.isLoaded() && module.isStarted()) {
					stopModule(module);
				}
			}
			listModulesStarted.clear();
		}
	}

	public void unloadModules() {
		synchronized(this) {			
			for (Module module : listModulesLoaded) {
				if (module.isLoaded()) {
					unloadModule(module);
				}
			}
			listModulesLoaded.clear();
			mapModules.clear();
		}
	}

	private boolean loadModule(Module module) {
		if (module == null) {
			throw new IllegalArgumentException("Module given is null.");
		}
		if (module.isStarted()) {
			errorln("Module has already loaded and has started, and cannot be loaded.");
			return true;
		}
		if (module.isLoaded()) {
			errorln("Module has already loaded and cannot be loaded.");
			return true;
		}
		try {
			println("Loading module " + module.getModuleName() + ".");
			module.loadModule();
			return true;
		} catch (Exception e) {
			errorln("Failed to load Module: " + module.getModuleName());
			stackTrace(e);
		}
		return false;
	}

	private boolean startModule(Module module) {
		if (module == null) {
			throw new IllegalArgumentException("Module given is null.");
		}
		if (!module.isLoaded()) {
			errorln("Module " + module.getModuleName() + " is not loaded and cannot be started.");
			return false;
		}
		if (module.isStarted()) {
			errorln("Module " + module.getModuleName() + " has already started.");
			return true;
		}
		try {
			println("Starting module " + module.getModuleName() + ".");
			module.startModule();
			return true;
		} catch (Exception e) {
			errorln("Failed to start Module: " + module.getModuleName());
			stackTrace(e);
			if (module.isLoaded()) {
				unloadModule(module);
			}
		}
		return false;
	}

	public boolean updateModule(Module module, long delta) {
		try {
			module.updateModule(delta);
			return true;
		} catch (Exception e) {
			errorln("Failed to update Module: " + module.getModuleName());
			stackTrace(e);
			if (module.isLoaded()) {
				unloadModule(module);
			}
		}
		return false;
	}

	private void stopModule(Module module) {
		if (module == null) {
			throw new IllegalArgumentException("Module given is null.");
		}
		if (!module.isLoaded()) {
			errorln("Module " + module.getModuleName() + " is not loaded and cannot be stopped.");
			return;
		}
		if (!module.isStarted()) {
			errorln("Module " + module.getModuleName() + " has not started and cannot be stopped.");
			return;
		}
		try {
			println("Stopping module " + module.getModuleName() + ".");
			module.stopModule();
		} catch (Exception e) {
			errorln("Failed to stop Module: " + module.getModuleName());
			stackTrace(e);
			if (module.isLoaded()) {
				unloadModule(module);
			}
		}
	}

	public void unloadModule(Module module) {
		if (module == null) {
			throw new IllegalArgumentException("Module given is null.");
		}
		if (!module.isLoaded()) {
			errorln("Module " + module.getModuleName() + " is not loaded and cannot be unloaded.");
			return;
		}
		try {
			if (module.isStarted()) {
				stopModule(module);
			}
			println("Unloading module " + module.getModuleName() + ".");
			module.unloadModule();
		} catch (Exception e) {
			errorln("Failed to unload Module: " + module.getModuleName());
			stackTrace(e);
		}
	}

	public void addModule(Module module) {
		String classLiteral = getClassLiteral(module.getClass());
		for(Module moduleNext : listModulesToLoad) {
			String classLiteralNext = getClassLiteral(moduleNext.getClass());
			if(classLiteral.equalsIgnoreCase(classLiteralNext)) {
				throw new IllegalArgumentException("Module " + classLiteral + " is already loaded in the plug-in " + getPluginName() + ".");
			}
		}
		listModulesToLoad.add(module);
	}
	
	public static String getClassLiteral(Class<?> clazz) {
		return clazz.getPackage().getName() + "." + clazz.getSimpleName();
	}

	public boolean removeModule(Module module) {
		return mapModules.remove(module.getModuleName(), module);
	}

	public Module getModule(String name) {
		Module returned = null;
		for (String moduleName : mapModules.keySet()) {
			if (moduleName.equalsIgnoreCase(name)) {
				returned = mapModules.get(moduleName);
				break;
			}
		}
		return returned;
	}

	@SuppressWarnings("unchecked")
	public <T extends Module> T getModule(Class<? extends Module> clazz) {
		Module returned = null;
		for (Module module : mapModules.values()) {
			if (module.getClass().equals(clazz)) {
				returned = module;
				break;
			}
		}
		return (T) returned;
	}

	public Collection<Module> getModules() {
		return this.mapModules.values();
	}

	public void saveResourceAs(String jarPath, String destPath) {
		saveResourceAs(jarPath, new File(destPath));
	}

	public void saveResourceAs(String jarPath, File dest) {
		write(getJarFile(), jarPath, dest);
	}

	public PluginProperties getProperties() {
		return this.properties;
	}

	private void setProperties(PluginProperties properties) {
		this.properties = properties;
	}

	public File getJarFile() {
		return this.file;
	}

	private void setJarFile(File file) {
		this.file = file;
	}

	private ClassLoader getClassLoader() {
		return this.classLoader;
	}

	private void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public String getPluginName() {
		return getProperties().getPluginName();
	}

	public String getPluginVersion() {
		return getProperties().getPluginVersion();
	}

	public String getPluginDescription() {
		return getProperties().getPluginDescription();
	}

	private List<Module> getLoadedModules() {
		return listModulesLoaded;
	}

	private List<Module> getStartedModules() {
		return listModulesStarted;
	}
	
	public void setLoadClasses(boolean flag) {
		loadClasses = flag;
		if(!loadClasses) {
			classLoader = ClassLoader.getSystemClassLoader();
		}
	}
	
	public boolean loadClasses() {
		return this.loadClasses;
	}

	private static InputStream getStream(File jar, String source) throws IOException {
		return new URL("jar:file:" + jar.getAbsolutePath() + "!/" + source).openStream();
	}

	private static void write(File jar, String source, File destination) {
		try {
			InputStream is = getStream(jar, source);
			OutputStream os = new FileOutputStream(destination);
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

	private static ClassLoader loadJarClasses(File fileJar) throws IOException {
		URL url = fileJar.toURI().toURL();
		URL[] urls = { url };
		ClassLoader loader = new URLClassLoader(urls);
		List<String> listClasses = new ArrayList<>();
		JarFile jarFile = new JarFile(fileJar);
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
		// Loads all classes in the JAR file.
		for (String clazz : listClasses) {
			try {
				loader.loadClass(clazz);
			} catch (NoClassDefFoundError error) {
				if (DEBUG) {
					System.err.println("Jar->Class not found: " + clazz);
				}
				try {
					ClassLoader.getSystemClassLoader().loadClass(clazz);
				} catch (NoClassDefFoundError error2) {
					if (DEBUG) {
						System.err.println("System->Class not found: " + clazz);
					}
				} catch (ClassNotFoundException exception2) {
					if (DEBUG) {
						System.err.println("System->Class not found: " + clazz);
					}
				}
			} catch (ClassNotFoundException exception) {
				if (DEBUG) {
					System.err.println("Jar->Class not found: " + clazz);
				}
				try {
					ClassLoader.getSystemClassLoader().loadClass(clazz);
				} catch (NoClassDefFoundError error2) {
					if (DEBUG) {
						System.err.println("System->Class not found: " + clazz);
					}
				} catch (ClassNotFoundException exception2) {
					if (DEBUG) {
						System.err.println("System->Class not found: " + clazz);
					}
				}
			}
		}
		return loader;
	}
}