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
package sledgehammer.plugin;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import sledgehammer.SledgeHammer;
import sledgehammer.util.Printable;

/**
 * Class to handle plug-in operations and utilities.
 *
 * @author Jab
 */
public class Plugin extends Printable {

    /**
     * Debug flag for debugging the Plugin class.
     */
    private static final boolean DEBUG = false;

    /**
     * The Map storing the Modules by name.
     */
    private Map<String, Module> mapModules;
    /**
     * The List of Modules to load.
     */
    private List<Module> listModulesToLoad;
    /**
     * The List of Modules already loaded.
     */
    private List<Module> listModulesLoaded;
    /**
     * The List of Modules to start.
     */
    private List<Module> listModulesToStart;
    /**
     * The List of Modules already started.
     */
    private List<Module> listModulesStarted;
    /**
     * The List of Modules that are stopped.
     */
    private List<Module> listModulesStopped;
    /**
     * The List of Modules that are unloaded.
     */
    private List<Module> listModulesUnloaded;
    /**
     * The File Object of the Jar File containing the plug-in.
     */
    private File jarFile;
    /**
     * The ClassLoader for the plug-in. This is the System ClassLoader for the
     * core plug-in.
     */
    private ClassLoader classLoader;
    /**
     * The plug-in properties and definitions.
     */
    private PluginProperties properties;
    /**
     * Flag to load the classes in the plug-in. This is false for the core plug-in.
     */
    private boolean loadClasses = true;

    /**
     * Main constructor.
     *
     * @param file The File Object of the Jar File containing the plug-in.
     */
    public Plugin(File file) {
        setJarFile(file);
        mapModules = new HashMap<>();
        // @formatter:off
		listModulesToLoad   = new LinkedList<>();
		listModulesLoaded   = new LinkedList<>();
		listModulesToStart  = new LinkedList<>();
		listModulesStarted  = new LinkedList<>();
		listModulesStopped  = new LinkedList<>();
		listModulesUnloaded = new LinkedList<>();
		// @formatter:on
    }

    /**
     * Manual constructor.
     *
     * @param file       The File Object of the Jar File containing the plug-in.
     * @param properties The plug-in properties to set manually.
     */
    public Plugin(File file, PluginProperties properties) {
        setJarFile(file);
        setProperties(properties);
        mapModules = new HashMap<>();
        // @formatter:off
		listModulesToLoad   = new LinkedList<>();
		listModulesLoaded   = new LinkedList<>();
		listModulesToStart  = new LinkedList<>();
		listModulesStarted  = new LinkedList<>();
		listModulesStopped  = new LinkedList<>();
		listModulesUnloaded = new LinkedList<>();
		// @formatter:on
    }

    @Override
    public String getName() {
        return "Plugin (" + getPluginName() + ")";
    }

    /**
     * Loads the plug-in.
     */
    public void load() {
        loadProperties();
        instantiateModules();
    }

    /**
     * Reloads the plug-in.
     */
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

    /**
     * (Private Method)
     * <p>
     * Loads the plug-in's properties and definitions.
     */
    private void loadProperties() {
        try {
            InputStream inputStream = getStream(getJarFile(), "plugin.yml");
            setProperties(new PluginProperties(inputStream));
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructs the Java Object instances of the Modules assigned to the
     * plug-in.
     */
    public void instantiateModules() {
        try {
            File fileJar = getJarFile();
            if (loadClasses()) {
                setClassLoader(loadJarClasses(fileJar));
            }
            List<String> listModuleNames = getProperties().getModuleNames();
            for (String moduleName : listModuleNames) {
                Module module = instantiateModule(moduleName);
                if (module != null) {
                    mapModules.put(module.getModuleName(), module);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructs the Java Object instance of the given Module.
     *
     * @param moduleName The String name of the Module to load.
     * @return Returns the constructed instance of the Module.
     */
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return module;
    }

    /**
     * Processes 'onLoad()' for the plug-in's Modules.
     */
    public void loadModules() {
        synchronized (this) {
            for (Module module : listModulesToLoad) {
                if (loadModule(module)) {
                    listModulesToStart.add(module);
                    listModulesLoaded.add(module);
                }
            }
            listModulesToLoad.clear();
        }
    }

    /**
     * Starts the plug-in's Modules.
     */
    public void startModules() {
        synchronized (this) {
            for (Module module : listModulesToStart) {
                if (startModule(module)) {
                    listModulesStarted.add(module);
                }
            }
            listModulesToStart.clear();
        }
    }

    /**
     * Updates the plug-in's Modules.
     *
     * @param delta The time in milliseconds since the last tick.
     */
    public void updateModules(long delta) {
        synchronized (this) {
            if (listModulesToLoad.size() > 0) {
                loadModules();
            }
            if (listModulesToStart.size() > 0) {
                startModules();
            }
            List<Module> listModulesToUpdate = new ArrayList<>(listModulesStarted);
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

    /**
     * Stops the plug-in's Modules.
     */
    public void stopModules() {
        synchronized (this) {
            for (Module module : listModulesStarted) {
                if (module.isLoaded() && module.isStarted()) {
                    stopModule(module);
                }
            }
            listModulesStarted.clear();
        }
    }

    /**
     * Unloads the plug-in's Modules.
     */
    public void unloadModules() {
        synchronized (this) {
            for (Module module : listModulesLoaded) {
                if (module.isLoaded()) {
                    unloadModule(module);
                }
            }
            listModulesLoaded.clear();
            mapModules.clear();
        }
    }

    /**
     * (Private Method)
     * <p>
     * Attempts to load a given Module.
     *
     * @param module The Module to load.
     * @return Returns true if the Module given loads successfully.
     */
    private boolean loadModule(Module module) {
        if (module == null) {
            throw new IllegalArgumentException("Module given is null.");
        }
        if (module.isStarted()) {
            errln("Module has already loaded and has started, and cannot be loaded.");
            return true;
        }
        if (module.isLoaded()) {
            errln("Module has already loaded and cannot be loaded.");
            return true;
        }
        try {
            println("Loading module " + module.getModuleName() + ".");
            module.loadModule();
            return true;
        } catch (Exception e) {
            errln("Failed to load Module: " + module.getModuleName());
            stackTrace(e);
        }
        return false;
    }

    /**
     * (Private Method)
     * <p>
     * Attempts to start a given Module.
     *
     * @param module The Module to start.
     * @return Returns true if the Module given starts successfully.
     */
    private boolean startModule(Module module) {
        if (module == null) {
            throw new IllegalArgumentException("Module given is null.");
        }
        if (!module.isLoaded()) {
            errln("Module " + module.getModuleName() + " is not loaded and cannot be started.");
            return false;
        }
        if (module.isStarted()) {
            errln("Module " + module.getModuleName() + " has already started.");
            return true;
        }
        try {
            println("Starting module " + module.getModuleName() + ".");
            module.startModule();
            return true;
        } catch (Exception e) {
            errln("Failed to start Module: " + module.getModuleName());
            stackTrace(e);
            if (module.isLoaded()) {
                unloadModule(module);
            }
        }
        return false;
    }

    /**
     * Updates a Module.
     *
     * @param module The Module to update.
     * @param delta  The time in milliseconds since the last update tick.
     * @return Returns true if the Module given updates successfully.
     */
    public boolean updateModule(Module module, long delta) {
        try {
            module.updateModule(delta);
            return true;
        } catch (Exception e) {
            errln("Failed to update Module: " + module.getModuleName());
            stackTrace(e);
            if (module.isLoaded()) {
                unloadModule(module);
            }
        }
        return false;
    }

    /**
     * Stops a Module.
     *
     * @param module The Module to stop.
     */
    private void stopModule(Module module) {
        if (module == null) {
            throw new IllegalArgumentException("Module given is null.");
        }
        if (!module.isLoaded()) {
            errln("Module " + module.getModuleName() + " is not loaded and cannot be stopped.");
            return;
        }
        if (!module.isStarted()) {
            errln("Module " + module.getModuleName() + " has not started and cannot be stopped.");
            return;
        }
        try {
            println("Stopping module " + module.getModuleName() + ".");
            module.stopModule();
        } catch (Exception e) {
            errln("Failed to stop Module: " + module.getModuleName());
            stackTrace(e);
            if (module.isLoaded()) {
                unloadModule(module);
            }
        }
    }

    /**
     * Unloads a Module.
     *
     * @param module The Module to unload.
     */
    public void unloadModule(Module module) {
        if (module == null) {
            throw new IllegalArgumentException("Module given is null.");
        }
        if (!module.isLoaded()) {
            errln("Module " + module.getModuleName() + " is not loaded and cannot be unloaded.");
            return;
        }
        try {
            if (module.isStarted()) {
                stopModule(module);
            }
            println("Unloading module " + module.getModuleName() + ".");
            module.unloadModule();
        } catch (Exception e) {
            errln("Failed to unload Module: " + module.getModuleName());
            stackTrace(e);
        }
    }

    /**
     * Adds a Module to the plug-in. If the Module is already loaded and added in,
     * IllegalArgumentException is thrown.
     *
     * @param module The Module to add to the plug-in.
     */
    public void addModule(Module module) {
        String classLiteral = getClassLiteral(module.getClass());
        for (Module moduleNext : listModulesToLoad) {
            String classLiteralNext = getClassLiteral(moduleNext.getClass());
            if (classLiteral.equalsIgnoreCase(classLiteralNext)) {
                throw new IllegalArgumentException(
                        "Module " + classLiteral + " is already loaded in the plug-in " + getPluginName() + ".");
            }
        }
        listModulesToLoad.add(module);
    }

    /**
     * Removes a Module from the plug-in.
     *
     * @param module The Module to remove.
     */
    public void removeModule(Module module) {
        mapModules.remove(module.getModuleName());
    }

    /**
     * @param name The String name of the Module.
     * @return Returns the Module with the given String name. If no Module in
     * the plug-in has the name given, null is returned.
     */
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

    /**
     * @param clazz The Java Class of the Module.
     * @return Returns the Module instance of the Java Class given. If no Module
     * loaded in the plug-in is of the Class given, null is returned.
     */
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

    /**
     * Set the plug-in to load the classes in the Jar File. This is an option for
     * embedded plug-ins.
     *
     * @param flag The Flag to set.
     */
    public void setLoadClasses(boolean flag) {
        loadClasses = flag;
        if (!loadClasses) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
    }

    /**
     * @return Returns a Collection of the Modules registered and loaded in the
     * plug-in.
     */
    public Collection<Module> getModules() {
        return this.mapModules.values();
    }

    /**
     * Saves a resource from the Jar File to the destination path.
     *
     * @param jarPath  The String path in the Jar File.
     * @param destPath The String path to save to.
     */
    public void saveResourceAs(String jarPath, String destPath) {
        saveResourceAs(jarPath, new File(destPath));
    }

    /**
     * Saves a resource from the Jar File to the destination path.
     *
     * @param jarPath The String path in the Jar File.
     * @param dest    The File destination to save to.
     */
    public void saveResourceAs(String jarPath, File dest) {
        write(getJarFile(), jarPath, dest);
    }

    /**
     * @return Returns the properties and definitions of the plug-in.
     */
    public PluginProperties getProperties() {
        return this.properties;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the properties and definitions of the plug-in.
     *
     * @param properties The PluginProperties to set.
     */
    private void setProperties(PluginProperties properties) {
        this.properties = properties;
    }

    /**
     * @return Returns the File Object of the Jar File.
     */
    public File getJarFile() {
        return this.jarFile;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the File Object of the Jar File.
     *
     * @param file The File to set.
     */
    private void setJarFile(File file) {
        this.jarFile = file;
    }

    /**
     * @return Returns the defined Java ClassLoader used by the plug-in.
     */
    private ClassLoader getClassLoader() {
        return this.classLoader;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the defined Java ClassLoader used by the plug-in.
     *
     * @param classLoader The ClassLoader to load the plug-in's classes.
     */
    private void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * @return Returns the String name of the plug-in.
     */
    public String getPluginName() {
        return getProperties().getPluginName();
    }

    /**
     * @return Returns the String version of the plug-in.
     */
    public String getPluginVersion() {
        return getProperties().getPluginVersion();
    }

    /**
     * @return Returns the String description of the plug-in.
     */
    public String getPluginDescription() {
        return getProperties().getPluginDescription();
    }

    /**
     * @return Returns a List of the loaded Modules in the plug-in.
     */
    private List<Module> getLoadedModules() {
        return listModulesLoaded;
    }

    /**
     * @return Returns a List of started Modules in the plug-in.
     */
    private List<Module> getStartedModules() {
        return listModulesStarted;
    }

    /**
     * @return Returns whether or not the plug-in uses its own ClassLoader.
     */
    public boolean loadClasses() {
        return this.loadClasses;
    }

    /**
     * Returns the literal String form of a Java Class.
     *
     * @param clazz The Class to translate.
     * @return Returns a String notation of the given Class's location.
     */
    public static String getClassLiteral(Class<?> clazz) {
        return clazz.getPackage().getName() + "." + clazz.getSimpleName();
    }

    /**
     * @param jar    The File Object of the Jar File.
     * @param source The String source path of the File to stream.
     * @return Returns a InputStream for a registered File in the given Jar
     * File.
     * @throws IOException Thrown when the InputStream fails to establish.
     */
    private static InputStream getStream(File jar, String source) throws IOException {
        return new URL("jar:file:" + jar.getAbsolutePath() + "!/" + source).openStream();
    }

    /**
     * Writes a Jar File entry to a File.
     *
     * @param jar         The File Object of the Jar File.
     * @param source      The String source path to the File entry in the Jar File.
     * @param destination The File destination to write the stream to.
     */
    public static void write(File jar, String source, File destination) {
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

    /**
     * Loads the ClassLoader of the given Jar File.
     *
     * @param fileJar The File Object of the Jar File.
     * @return Returns the ClassLoader for the Jar File.
     * @throws IOException Throws a IOException depending on the validity of the Jar File's contents.
     */
    private static ClassLoader loadJarClasses(File fileJar) throws IOException {
        URL url = fileJar.toURI().toURL();
        URL[] urls = {url};
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
            } catch (ClassNotFoundException | NoClassDefFoundError error) {
                if (DEBUG) {
                    System.err.println("Jar->Class not found: " + clazz);
                }
                try {
                    ClassLoader.getSystemClassLoader().loadClass(clazz);
                } catch (Exception error2) {
                    if (DEBUG) {
                        System.err.println("System->Class not found: " + clazz);
                    }
                }
            }
        }
        return loader;
    }
}