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

import java.util.Map;

import sledgehammer.lua.LuaTable;

/**
 * LuaTable to store and define Module properties.
 *
 * @author Jab
 */
@SuppressWarnings("rawtypes")
public class ModuleProperties extends LuaTable {

    /**
     * The defined properties for the plug-in.
     */
    private PluginProperties pluginProperties;
    /**
     * The String name of the Module.
     */
    private String name;
    /**
     * The String version of the Module.
     */
    private String version;
    /**
     * The String module location of the Module class.
     */
    private String moduleLocation;
    /**
     * The String description of the Module.
     */
    private String description;
    /**
     * The String client ID of the Module for Lua communication.
     */
    private String clientModuleId;

    /**
     * Used for core modules, or test-cases.
     *
     * @param name           The String name of the Module.
     * @param version        The String version of the Module.
     * @param moduleLocation The String location of the Module (package.ModuleName)
     * @param description    The String description of the Module.
     */
    public ModuleProperties(String name, String version, String moduleLocation, String description) {
        super("ModuleProperties");
        // Set the properties.
        setModuleName(name);
        setModuleVersion(version);
        setModuleLocation(moduleLocation);
        setModuleDescription(description);
    }

    /**
     * Main constructor.
     *
     * @param name The String name of the Module.
     * @param map  The Map definitions of the Module.
     */
    public ModuleProperties(String name, Map map) {
        super("ModuleProperties");
        loadProperties(name, map);
    }

    /**
     * Default constructor. Sets default values.
     */
    public ModuleProperties() {
        super("ModuleProperties");
        // Set default variables
        setModuleName("Untitled Module");
        setModuleVersion("1.0");
        setModuleDescription("No description.");
        setModuleLocation("unknown");
    }

    /**
     * Loads the properties for the Module.
     *
     * @param name The String name of the Module.
     * @param map  The Map definitions of the Module.
     */
    private void loadProperties(String name, Map map) {
        setModuleName(name);
        // Default variables
        String version = "1.0";
        String description = "No description.";
        String moduleLocation = "unknown";
        String clientModuleId = name.toLowerCase().trim();
        // Grab the version.
        Object oVersion = map.get("version");
        // If it exists, set it.
        if (oVersion != null) {
            version = oVersion.toString();
        }
        // Grab the description.
        Object oDescription = map.get("description");
        // If it exists, set it.
        if (oDescription != null) {
            description = oDescription.toString();
        }
        // Grab the module-location.
        Object oModuleLocation = map.get("class");
        // If it exists, set it.
        if (oModuleLocation != null) {
            moduleLocation = oModuleLocation.toString();
        }
        Object oClientModuleId = map.get("client-module-id");
        if (oClientModuleId != null) {
            clientModuleId = oClientModuleId.toString().toLowerCase().trim();
        }
        if (DEBUG) {
            System.out.println("Name: " + name);
            System.out.println("Version: " + version);
            System.out.println("Location: " + moduleLocation);
            System.out.println("Description: " + description);
            System.out.println("ClientModuleId: " + clientModuleId);
        }
        // Set the result properties.
        setModuleVersion(version);
        setModuleLocation(moduleLocation);
        setModuleDescription(description);
        setClientModuleId(clientModuleId);
    }

    /**
     * @return Returns the client ID of the Module for Lua communication.
     */
    public String getClientModuleId() {
        return this.clientModuleId;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the client ID of the Module for Lua communication.
     *
     * @param clientModuleId The String client ID to set.
     */
    private void setClientModuleId(String clientModuleId) {
        this.clientModuleId = clientModuleId;
    }

    /**
     * @return Returns the String name of the Module.
     */
    public String getModuleName() {
        return this.name;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the name of the Module.
     *
     * @param name The name to set.
     */
    private void setModuleName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the String version of the Module.
     */
    public String getModuleVersion() {
        return this.version;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the Module's version.
     *
     * @param version The version to set.
     */
    private void setModuleVersion(String version) {
        this.version = version;
    }

    /**
     * @return Returns the location of the Module's Class in Java..
     */
    public String getModuleLocation() {
        return this.moduleLocation;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the location of the Module's Class in Java.
     *
     * @param moduleLocation The location to set.
     */
    private void setModuleLocation(String moduleLocation) {
        this.moduleLocation = moduleLocation;
    }

    /**
     * @return Returns the description of the Module.
     */
    public String getModuleDescription() {
        return this.description;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the description of the Module.
     *
     * @param description The description to set.
     */
    private void setModuleDescription(String description) {
        this.description = description;
    }

    /**
     * @return Returns the properties of the Plug-in containing the Module.
     */
    public PluginProperties getPluginProperties() {
        return this.pluginProperties;
    }

    /**
     * Sets the properties of the Plug-in containing the Module.
     *
     * @param pluginProperties The PluginProperties to set.
     */
    public void setPluginProperties(PluginProperties pluginProperties) {
        this.pluginProperties = pluginProperties;
    }
}