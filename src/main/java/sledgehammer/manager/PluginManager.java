/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

package sledgehammer.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.event.core.player.ClientEvent;
import sledgehammer.lua.core.Player;
import sledgehammer.lua.core.send.SendLua;
import sledgehammer.module.chat.ModuleChat;
import sledgehammer.module.core.ModuleCore;
import sledgehammer.module.faction.ModuleFactions;
import sledgehammer.module.permissions.ModulePermissions;
import sledgehammer.module.vanilla.ModuleVanilla;
import sledgehammer.plugin.Module;
import sledgehammer.plugin.Plugin;

/**
 * Manager to handle Plug-in data and operations for the Sledgehammer engine.
 *
 * @author Jab
 */
public class PluginManager extends Manager {

    /**
     * Debug boolean, used for verbose output.
     */
    public static boolean DEBUG = true;

    /**
     * The Map of loaded Plug-ins identified by their String name.
     */
    private Map<String, Plugin> mapPlugins;
    /**
     * The Core Plug-in.
     */
    private Plugin pluginSledgehammer;
    /**
     * The <List> of Plug-ins to load.
     */
    private List<Plugin> listPluginsToLoad;
    /**
     * The <List> of Plug-ins to start.
     */
    private List<Plugin> listPluginsToStart;
    /**
     * The <List> of Plug-ins that have started.
     */
    private List<Plugin> listPluginsStarted;
    /**
     * The <List> of Plug-ins to unload.
     */
    private List<Plugin> listPluginsToUnload;
    /**
     * The <ModulePermissions> instance in the Core plug-in.
     */
    private ModulePermissions modulePermissions;
    /**
     * The <ModuleCore> instance in the Core plug-in.
     */
    private ModuleCore moduleCore;
    /**
     * The <ModuleVanilla> instance in the Core plug-in.
     */
    private ModuleVanilla moduleVanilla;
    /**
     * The <ModuleChat> instance in the Core plug-in.
     */
    private ModuleChat moduleChat;
    /**
     * The <ModuleFactions> instance in the Core plug-in.
     */
    private ModuleFactions moduleFactions;
    /**
     * The <File> Object of the directory for Plug-in's to load.
     */
    private File directory;
    /**
     * A <Long> value to store the last time the Plug-in's were updated.
     */
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
     * <p>
     * Loads the Core Plug-in with all the Modules for the core.
     */
    private void loadCorePlugin() {
        pluginSledgehammer = new Plugin(SledgeHammer.getJarFile());
        pluginSledgehammer.setLoadClasses(false);
        pluginSledgehammer.load();
        registerPlugin(pluginSledgehammer);
    }

    /**
     * (Private Method)
     * <p>
     * Loads Plug-ins installed in the plugin directory.
     */
    private void loadInstalledPlugins() {
        println("Loading plug-in module(s).");
        File[] plugins = getPluginDirectory().listFiles();
        if (plugins != null && plugins.length > 0) {
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
     * @param event The ClientEvent to handle.
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
     * @param jar The File Object of the Jar File. If the File does not exist, an
     *            IllegalArgumentException is thrown.
     * @return Returns a loaded Plug-in with the given Jar File.
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
     * Returns a registered. Module with the given Class.
     *
     * @param clazz The Class of the Module.
     * @param <T>   The Class of the Module returned.
     * @return Returns a Module with the given Class.
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
     * Sends a SendLua Object storing the Lua code compiled.
     *
     * @return Returns a SendLua containing the Lua code in a String format for all started Plug-ins.
     */
    public SendLua getLua(Player player) {
        SendLua sendLua = new SendLua(player);
        Plugin pluginSledgehammer = getSledgehammerPlugin();
        pluginSledgehammer.getLua(sendLua);
        for (Plugin plugin : getPlugins()) {
            if (plugin.equals(pluginSledgehammer)) {
                continue;
            }
            plugin.getLua(sendLua);
        }
        return sendLua;
    }

    /**
     * @return Returns the File Object for the directory that Plug-in's are
     * installed.
     */
    public File getPluginDirectory() {
        if (directory == null) {
            directory = new File("plugins" + File.separator);
        }
        return directory;
    }

    /**
     * Registers a given Plug-in by adding it to the List of Plug-ins to load.
     *
     * @param plugin The Plug-in to register.
     */
    public void registerPlugin(Plugin plugin) {
        this.listPluginsToLoad.add(plugin);
    }

    /**
     * @return Returns a Collection of the loaded Plug-in's.
     */
    private Collection<Plugin> getPlugins() {
        return mapPlugins.values();
    }

    /**
     * @return Returns the ModulePermissions instance in the Core Plug-in.
     */
    public ModulePermissions getPermissionsModule() {
        return this.modulePermissions;
    }

    /**
     * @return Returns the ModuleCore instance in the Core Plug-in.
     */
    public ModuleCore getCoreModule() {
        return this.moduleCore;
    }

    /**
     * @return Returns the ModuleVanilla instance in the Core Plug-in.
     */
    public ModuleVanilla getVanillaModule() {
        return this.moduleVanilla;
    }

    /**
     * @return Returns the ModuleChat instance in the Core Plug-in.
     */
    public ModuleChat getChatModule() {
        return this.moduleChat;
    }

    /**
     * @return Returns the ModuleFactions instance in the Core Plug-in.
     */
    public ModuleFactions getFactionsModule() {
        return this.moduleFactions;
    }

    /**
     * @return Returns the Plug-in instance of the Core Plug-in.
     */
    public Plugin getSledgehammerPlugin() {
        return this.pluginSledgehammer;
    }
}