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

package sledgehammer.plugin;

import java.io.File;
import java.util.List;

import sledgehammer.Settings;
import sledgehammer.SledgeHammer;
import sledgehammer.event.ClientEvent;
import sledgehammer.event.Event;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.interfaces.EventListener;
import sledgehammer.interfaces.ThrowableListener;
import sledgehammer.interfaces.LogEventListener;
import sledgehammer.interfaces.PermissionListener;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.chat.ChatMessage;
import sledgehammer.lua.core.Player;
import sledgehammer.lua.core.send.SendLua;
import sledgehammer.manager.EventManager;
import sledgehammer.manager.PluginManager;
import sledgehammer.module.chat.ModuleChat;
import sledgehammer.util.Printable;

/**
 * Class to handle module operations and utilities.
 *
 * @author Jab
 */
public abstract class Module extends Printable {

    /**
     * The Plug-in instance the Module is packaged with.
     */
    private Plugin plugin;
    /**
     * The properties of the Module.
     */
    private ModuleProperties properties = new ModuleProperties();
    /**
     * The File directory for data to be placed.
     */
    private File directory = null;
    /**
     * Flag to note if the Module has passed into the loaded state.
     */
    private boolean loaded = false;
    /**
     * Flag to note if the Module has passed into the started state.
     */
    private boolean started = false;

    @Override
    public String getName() {
        return getProperties().getModuleName();
    }

    /**
     * (Note: Modules should not use this method)
     * <p>
     * Loads the Module.
     *
     * @return Returns true if the Module loads successfully.
     */
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

    /**
     * (Note: Modules should not use this method)
     * <p>
     * Starts the module.
     */
    public void startModule() {
        if (!started) {
            started = true;
            onStart();
        } else {
            println("Module is already started.");
        }
    }

    /**
     * (Note: Modules should not use this method)
     * <p>
     * Updates the Module every tick.
     *
     * @param delta The latency in milliseconds since the last tick.
     */
    public void updateModule(long delta) {
        if (started) {
            onUpdate(delta);
        }
    }

    /**
     * (Note: Modules should not use this method)
     * <p>
     * Stops the Module.
     *
     * @return Returns true if the Module stopped successfully.
     */
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

    /**
     * (Note: Modules should not use this method)
     * <p>
     * Unloads the Module.
     *
     * @return Returns true if the Module unloads successfully.
     */
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

    /**
     * Unloads the module.
     */
    public void unload() {
        started = false;
        getPlugin().unloadModule(this);
    }

    /**
     * Registers a ChatChannel with no description or permission-node. Throws an
     * IllegalArgumentException if the name provided is null or empty.
     *
     * @param name The String name of the ChatChannel.
     * @return Returns the result ChatChannel.
     */
    public ChatChannel createChatChannel(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name provided is null or empty.");
        }
        return createChatChannel(name, null, null);
    }

    /**
     * Registers a ChatChannel with a name, description, and permission-node to
     * access it. Throws an IllegalArgumentException if the name provided is null or
     * empty.
     * <p>
     * The ChatChannel is set to be global, public, not custom, saves history, and
     * allows Players to speak.
     *
     * @param name           The String name of the ChatChannel.
     * @param description    The String description of the ChatChannel.
     * @param permissionNode The String permission-node of the ChatChannel.
     * @return Returns the result ChatChannel.
     */
    public ChatChannel createChatChannel(String name, String description, String permissionNode) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name provided is null or empty.");
        }
        if (description == null) {
            description = "No description.";
        }
        if (permissionNode == null) {
            permissionNode = "sledgehammer.chat";
        }
        return createChatChannel(name, description, permissionNode, true, true,
                false, true, true);
    }

    /**
     * Registers a ChatChannel with a name, description, permission-node, and all
     * flags associated with it.
     *
     * @param channelName        The String name of the ChatChannel.
     * @param channelDescription The String description of the ChatChannel.
     * @param permissionNode     The String permission-node of the ChatChannel.
     * @param isGlobalChannel    Flag for if the ChatChannel should send messages to all players,
     *                           or players relative to their location.
     * @param isPublicChannel    Flag for if the ChatChannel is public to all Players, or if the
     *                           ChatChannel is only accessible to the Player's with the given
     *                           permission-node.
     * @param isCustomChannel    Flag for if the ChatChannel is a custom ChatChannel. (Has no use)
     * @param saveHistory        Flag to save the history of the ChatChannel.
     * @param canSpeak           Flag for if the Players are allowed to send Messages in the
     *                           ChatChannel. (Read-Only if set to false)
     * @return Returns the result ChatChannel.
     */
    public ChatChannel createChatChannel(String channelName, String channelDescription, String permissionNode,
                                         boolean isGlobalChannel, boolean isPublicChannel, boolean isCustomChannel, boolean saveHistory,
                                         boolean canSpeak) {
        ModuleChat moduleChat = getChatModule();
        return moduleChat.createChatChannel(channelName, channelDescription, permissionNode,
                isGlobalChannel, isPublicChannel, isCustomChannel, saveHistory, canSpeak);
    }

    /**
     * Unregisters a given ChatChannel.
     *
     * @param chatChannel The ChatChannel being unregistered.
     */
    public void unregisterChatChannel(ChatChannel chatChannel) {
        if (chatChannel == null) {
            throw new IllegalArgumentException("ChatChannel given is null!");
        }
        getChatModule().unregisterChatChannel(chatChannel);
    }

    /**
     * Creates a ChatMessage with no assigned ChatChannel.
     *
     * @param message The String message content.
     * @return Returns a new ChatMessage.
     */
    public ChatMessage createChatMessage(String message) {
        return getChatModule().createChatMessage(message);
    }

    /**
     * Creates and sends a ChatMessage to the 'Global' ChatChannel.
     *
     * @param message The String message content.
     * @return Returns the created ChatMessage.
     */
    public ChatMessage sendGlobalMessage(String message) {
        ChatChannel chatChannelGlobal = getChatModule().getGlobalChatChannel();
        ChatMessage chatMessage = createChatMessage(message);
        chatChannelGlobal.addChatMessage(chatMessage);
        return chatMessage;
    }

    /**
     * @return Returns the File directory for the Module to store data.
     */
    public File getModuleDirectory() {
        if (directory == null) {
            directory = new File(getPluginDirectory(), getModuleName());
            if (!directory.exists()) {
                directory.mkdirs();
            }
        }
        return directory;
    }

    /**
     * @return Returns the File directory for the Plugin that stores Module directories.
     */
    public File getPluginDirectory() {
        return getPlugin().getPluginDirectory();
    }

    /**
     * @return Returns true if the Settings allow Lua files to be modified by anything other than the Modules that saves
     * the Lua Files. Returns false if ONLY the Modules have rights to the final versions of Lua files.
     */
    public boolean isLuaOverriden() {
        return Settings.getInstance().overrideLua();
    }

    /**
     * Saves a File resource stored in the Plug-in Jar File to the same location
     * inside the Module's data folder. If the File exists in that location, the
     * File is not overwritten.
     *
     * @param path The String path to the File inside the Jar file.
     */
    public void saveResource(String path) {
        saveResource(path, false);
    }

    /**
     * Saves a File resource stored in the Plug-in Jar File to the same location
     * inside the Module's data folder. If the file exists in that location and
     * overwrite is set to false, the File is not overwritten.
     *
     * @param path      The String path to the File inside the Jar File.
     * @param overwrite Flag to set if the File is to be overwritten, regardless of it's
     *                  state.
     */
    public void saveResource(String path, boolean overwrite) {
        File file = new File(getModuleDirectory(), path);
        if (!overwrite && file.exists()) {
            return;
        }
        Plugin plugin = getPlugin();
        plugin.saveResourceAs(path, file);
    }

    /**
     * Saves a File resource stored in the Plug-in Jar file to the given destination
     * inside the Module's data folder. If the file exists in the destination, the
     * File is not overwritten.
     *
     * @param jarPath  The String path to the File inside the Jar File.
     * @param destPath The String destination path to the File stored inside the
     *                 Module's data folder.
     */
    public void saveResourceAs(String jarPath, String destPath) {
        saveResourceAs(jarPath, destPath, false);
    }

    /**
     * Saves a File resource stored in the Plug-in Jar file to the given destination
     * inside the Module's data folder. If the file exists in that location and
     * overwrite is set to false, the File is not overwritten.
     *
     * @param jarPath   The String path to the File inside the Jar File.
     * @param destPath  The String destination path to the File stored inside the
     *                  Module's data folder.
     * @param overwrite Flag to set if the File is to be overwritten, regardless of it's
     *                  state.
     */
    public void saveResourceAs(String jarPath, String destPath, boolean overwrite) {
        File file = new File(getModuleDirectory(), destPath);
        if (!overwrite && file.exists()) {
            return;
        }
        Plugin plugin = getPlugin();
        plugin.saveResourceAs(jarPath, file);
    }

    /**
     * Saves a File resource stored in the Plug-in Jar file to the absolute File path provided.. If the File exists in the location given and
     * overwrite is set to false, the File will not be saved.
     *
     * @param jarPath      The String path to the File inside the Jar File.
     * @param absolutePath The absolute File location to save the file. (This is not a directory, but a full file path)
     * @param overwrite    Flag to set if the File is to be overwritten, regardless of it's
     *                     state.
     */
    public void saveResourceAs(String jarPath, File absolutePath, boolean overwrite) {
        if (!overwrite && absolutePath.exists()) {
            return;
        }
        Plugin plugin = getPlugin();
        plugin.saveResourceAs(jarPath, absolutePath);
    }

    /**
     * Approximate method for 'SledgeHammer.instance.register(type, listener)'.
     * <p>
     * Registers all Events defined in the EventListener.
     *
     * @param listener The EventListener being registered.
     */
    public void register(EventListener listener) {
        // Grab the types definition from the listener.
        String[] types = listener.getTypes();
        // Make sure that the types are defined.
        if (types == null) {
            throw new IllegalArgumentException(
                    "EventListener " + listener.getClass().getSimpleName() + "'s getTypes() returned null.");
        }
        // Make sure that there are types defined in the array.
        if (types.length == 0) {
            throw new IllegalArgumentException("EventListener " + listener.getClass().getSimpleName()
                    + "'s getTypes() returned an empty String Array.");
        }
        // Go through each type.
        for (String type : types) {
            // Register each type individually.
            register(type, listener);
        }
    }

    /**
     * Approximate method for 'SledgeHammer.instance.register(type, listener)'.
     * <p>
     * Registers an EventListener for a given Event type.
     *
     * @param type     The type of an Event. (Event.getID() or Event.ID)
     * @param listener The EventListener to register.
     */
    public void register(String type, EventListener listener) {
        SledgeHammer.instance.register(type, listener);
    }

    /**
     * Approximate method for 'SledgeHammer.instance.register(listener)'.
     * <p>
     * Registers a LogEventListener for LogEvents.
     *
     * @param listener The LogEventListener to register.
     */
    public void register(LogEventListener listener) {
        SledgeHammer.instance.register(listener);
    }

    /**
     * Approximate method for 'SledgeHammer.instance.register(command, listener)'.
     * <p>
     * Registers a CommandListener to a given String command.
     *
     * @param command  The String command to register under.
     * @param listener The CommandListener to register.
     */
    public void register(String command, CommandListener listener) {
        SledgeHammer.instance.register(command, listener);
    }

    /**
     * Approximate method for 'SledgeHammer.instance.register(listener)'.
     * <p>
     * Registers a CommandListener.
     *
     * @param listener The CommandListener to register.
     */
    public void register(CommandListener listener) {
        SledgeHammer.instance.register(listener);
    }

    /**
     * Approximate method for 'SledgeHammer.instance.register(listener)'.
     * <p>
     * Registers a ThrowableListener to handle thrown Exceptions in the scope of
     * the Sledgehammer engine.
     *
     * @param listener The ThrowableListener to register.
     */
    public void register(ThrowableListener listener) {
        SledgeHammer.instance.register(listener);
    }

    /**
     * Unregisters a EventListener.
     *
     * @param listener The EventListener to unregister.
     */
    public void unregister(EventListener listener) {
        getEventManager().unregister(listener);
    }

    /**
     * Unregisters a CommandListener.
     *
     * @param listener The CommandListener to unregister.
     */
    public void unregister(CommandListener listener) {
        getEventManager().unregister(listener);
    }

    /**
     * Unregisters a LogEventListener.
     *
     * @param listener The LogEventListener to unregister.
     */
    public void unregister(LogEventListener listener) {
        getEventManager().unregister(listener);
    }

    /**
     * Unregisters a ThrowableListener.
     *
     * @param listener The ThrowableListener to unregister.
     */
    public void unregister(ThrowableListener listener) {
        getEventManager().unregister(listener);
    }

    /**
     * Sets the Sledgehammer engine's PermissionListener. This method is reserved
     * for the Plug-ins implementing a permissions solution.
     *
     * @param permissionListener The PermissionListener to set.
     */
    public void setPermissionListener(PermissionListener permissionListener) {
        SledgeHammer.instance.setPermissionListener(permissionListener);
    }

    /**
     * Approximate Method to 'SledgeHammer.instance.addDefaultPermission(node)'.
     * <p>
     * Adds a String permission-node to the default PermissionGroup, allowing
     * all Players to be granted the permission-node.
     *
     * @param node The String node to add.
     */
    public void addDefaultPermission(String node) {
        SledgeHammer.instance.addDefaultPermission(node);
    }

    /**
     * Approximate Method to 'SledgeHammer.instance.addDefaultPermission(node)'.
     * <p>
     * Adds a String permission-node to the default PermissionGroup with a given
     * Boolean flag. All PermissionUsers with a specific definition will
     * override this.
     *
     * @param node The String node to add.
     * @param flag The Boolean flag to set.
     */
    public void addDefaultPermission(String node, boolean flag) {
        SledgeHammer.instance.addDefaultPermission(node, flag);
    }

    /**
     * @return Returns the EventManager instance of the Sledgehammer engine.
     */
    public EventManager getEventManager() {
        return SledgeHammer.instance.getEventManager();
    }

    /**
     * @return Returns the PluginManager instance of the Sledgehammer engine.
     */
    public PluginManager getModuleManager() {
        return SledgeHammer.instance.getPluginManager();
    }

    /**
     * @return Returns the String response to a denied permission check.
     */
    public String getPermissionDeniedMessage() {
        return SledgeHammer.instance.getPermissionDeniedMessage();
    }

    /**
     * Approximate method for 'SledgeHammer.instance.handle(event, shouldLog)'.
     * <p>
     * Handles a Event, passing it to all registered EventListeners listening
     * for the Event's type.
     *
     * @param event     The Event to handle.
     * @param shouldLog Flag for whether or not to pass the Event to the registered
     *                  LogListeners as a LogEvent, based on the data set in the Event.
     */
    public void handleEvent(Event event, boolean shouldLog) {
        SledgeHammer.instance.handle(event, shouldLog);
    }

    /**
     * Approximate method for 'SledgeHammer.instance.handle(event)'.
     * <p>
     * Handles a Event passing it to all registered EventListeners listening
     * for the Event's type.
     *
     * @param event The Event to handle.
     */
    public void handleEvent(Event event) {
        SledgeHammer.instance.handle(event);
    }

    /**
     * Returns a registered. Module with the given Class.
     *
     * @param clazz The Class of the Module.
     * @param <T>   The Module Class to return.
     * @return Returns a Module with the given Class.
     */
    @SuppressWarnings("unchecked")
    public <T extends Module> Module getModule(Class<? extends Module> clazz) {
        return getPluginManager().getModule(clazz);
    }

    /**
     * @return Returns the String name of the Project Zomboid server displayed in
     * public.
     */
    public String getPublicServerName() {
        return SledgeHammer.instance.getPublicServerName();
    }

    /**
     * @return Returns the chat Module from the Core plug-in of
     * Sledgehammer.
     */
    public ModuleChat getChatModule() {
        return getPluginManager().getChatModule();
    }

    /**
     * @return Returns the Plug-in that the Module is assigned to.
     */
    public Plugin getPlugin() {
        return this.plugin;
    }

    /**
     * (Note: Modules should not use this method)
     * <p>
     * Sets the Plug-in that the Module is assigned to.
     *
     * @param plugin The Plug-in to set.
     */
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * @return Returns a List of online Players.
     */
    public List<Player> getPlayers() {
        return SledgeHammer.instance.getPlayers();
    }

    /**
     * @return Returns the Sledgehammer PluginManager instance.
     */
    public PluginManager getPluginManager() {
        return SledgeHammer.instance.getPluginManager();
    }

    /**
     * @return Returns true if the Module has passed into the loaded state.
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * @return Returns the String version of the Module, as defined in the
     * Module's properties.
     */
    public String getVersion() {
        return getProperties().getModuleVersion();
    }

    /**
     * @return Returns the Module's properties and definitions.
     */
    public ModuleProperties getProperties() {
        return this.properties;
    }

    /**
     * (Note: Modules should not use this method)
     * <p>
     * Sets the Module's properties and definitions for integration into the
     * Sledgehammer engine.
     *
     * @param properties The ModuleProperties definitions to set.
     */
    public void setProperties(ModuleProperties properties) {
        this.properties = properties;
    }

    /**
     * @return Returns the String identity of the Module for Lua-Client
     * identification for ClientEvent communication.
     */
    public String getClientModuleId() {
        return getProperties().getClientModuleId();
    }

    /**
     * @param name The String name of the ChatChannel.
     * @return Returns a ChatChannel with the given String name. If no
     * ChatChannel uses the name, null is returned.
     */
    public ChatChannel getChatChannel(String name) {
        return getChatModule().getChatChannel(name);
    }

    /**
     * @return Return's the Module's String name defined in the properties. This
     * is used for identifying the data folder.
     */
    public String getModuleName() {
        return getProperties().getModuleName();
    }

    /**
     * @return Returns true if the Module has started and has not stopped.
     */
    public boolean isStarted() {
        return this.started;
    }

    /**
     * @return Returns the File path to the language directory.
     */
    public File getLanguageDirectory() {
        return SledgeHammer.instance.getLanguageDirectory();
    }

    public File getLuaDirectory() {
        File dir = new File(SledgeHammer.instance.getLuaDirectory(), "Module" + File.separator + getClientModuleId());
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public boolean isLangOverriden() {
        return Settings.getInstance().overrideLang();
    }

    /**
     * Fired when the Module is loaded.
     * <p>
     * (Override this method if you want to execute code in your module when it
     * loads)
     */
    public void onLoad() {
    }

    /**
     * Fired when the Module is started.
     * <p>
     * (Override this method if you want to execute code in your module when it
     * starts)
     */
    public void onStart() {
    }

    /**
     * Fired when the Module is updated.
     * <p>
     * (Override this method if you want to execute code in your module when the
     * server updates)
     *
     * @param delta The delta in milliseconds since the last update.
     */
    public void onUpdate(long delta) {
    }

    /**
     * Fired when the Module is stopped.
     * <p>
     * (Override this method if you want to execute code in your module when it
     * stops)
     */
    public void onStop() {
    }

    /**
     * Fired when the Module is unloaded.
     * <p>
     * (Override this method if you want to execute code in your module when it
     * unloads)
     */
    public void onUnload() {
    }

    /**
     * Fired when a ClientCommand is sent from a Player's client.
     *
     * @param event the ClientEvent container for the event.
     */
    public void onClientCommand(ClientEvent event) {
    }

    /**
     * Used to execute GenericEvent commands. This will be picked up by modules
     * that @override this this method.
     *
     * @param type    The type of event.
     * @param context The context of the event.
     */
    public void executeCommand(String type, String context) {
    }

    /**
     * Fired when building the Lua to send to a Player.
     * <p>
     * This allows each Module to choose what to send to the Player. This means that while regular Players receive
     * code for most functions, advanced Players may get more code to do more things.
     *
     * @param send
     */
    public void onBuildLua(SendLua send) {
    }
}