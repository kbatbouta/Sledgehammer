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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.lua.LuaTable;
import sledgehammer.util.YamlUtil;

/**
 * LuaTable to handle the assignment and definition of plug-ins and the module
 * definitions registered with it.
 *
 * @author Jab
 */
@SuppressWarnings("rawtypes")
public class PluginProperties extends LuaTable {

    /**
     * The String name definition of the plug-in.
     */
    private String name;
    /**
     * The String version definition of the plug-in.
     */
    private String version;
    /**
     * The String description definition of the plug-in.
     */
    private String description;
    /**
     * The List of String Module names registered in the plug-in.
     */
    private List<String> listModuleNames;
    /**
     * The Map of loaded Module definitions.
     */
    private Map<String, ModuleProperties> mapModuleProperties;
    /**
     * The Map of the YAML contents of the plugin.yml File.
     */
    private Map mapProperties;

    /**
     * Main constructor.
     *
     * @param inputStream The InputStream of the YAML contents of the plugin.yml File.
     */
    public PluginProperties(InputStream inputStream) {
        super("PluginProperties");
        mapProperties = YamlUtil.getYaml().load(inputStream);
        listModuleNames = new ArrayList<>();
        mapModuleProperties = new HashMap<>();
        loadProperties();
    }

    /**
     * Manual constructor.
     *
     * @param name        The String name definition of the plug-in.
     * @param version     The String version definition of the plug-in.
     * @param description The String description definition of the plug-in.
     */
    public PluginProperties(String name, String version, String description) {
        super("PluginProperties");
        setPluginName(name);
        setPluginVersion(version);
        setPluginDescription(description);
        listModuleNames = new ArrayList<>();
        mapModuleProperties = new HashMap<>();
    }

    /**
     * (Private Method)
     * <p>
     * Loads the properties from the YAML definition.
     */
    private void loadProperties() {
        Object oName = mapProperties.get("name");
        if (oName != null) {
            setPluginName(oName.toString());
        }
        Object oVersion = mapProperties.get("version");
        if (oVersion != null) {
            setPluginVersion(oVersion.toString());
        }
        Object oDescription = mapProperties.get("description");
        if (oDescription != null) {
            setPluginDescription(oDescription.toString());
        }

        Map map = (Map) mapProperties.get("modules");
        if (map == null) {
            return;
        }
        mapModuleProperties = new HashMap<>();
        for (Object key : map.keySet()) {
            ModuleProperties moduleProperties = new ModuleProperties((String) key, (Map) map.get(key));
            moduleProperties.setPluginProperties(this);
            addProperties(moduleProperties);
        }
    }

    /**
     * Adds a Module's property definition to the plug-in definition.
     *
     * @param moduleProperties The ModuleProperties to add.
     */
    public void addProperties(ModuleProperties moduleProperties) {
        String moduleName = moduleProperties.getModuleName();
        if (!listModuleNames.contains(moduleName)) {
            listModuleNames.add(moduleName);
        }
        mapModuleProperties.put(moduleName, moduleProperties);
    }

    /**
     * @return Returns the String name definition of the plug-in.
     */
    public String getPluginName() {
        return this.name;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the String name definition of the plug-in.
     *
     * @param name The String name to set.
     */
    private void setPluginName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the String version definition of the plug-in.
     */
    public String getPluginVersion() {
        return this.version;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the String version definition of the plug-in.
     *
     * @param version The String version to set.
     */
    private void setPluginVersion(String version) {
        this.version = version;
    }

    /**
     * @return Returns the String description definition of the plug-in.
     */
    public String getPluginDescription() {
        return this.description;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the String description definition of the plug-in.
     *
     * @param description The String definition to set.
     */
    private void setPluginDescription(String description) {
        this.description = description;
    }

    /**
     * @return Returns a List of String name definitions of all modules
     * registered in the plug-in's definitions.
     */
    public List<String> getModuleNames() {
        return this.listModuleNames;
    }

    /**
     * @param moduleName The String name of the Module.
     * @return Returns a ModuleProperty definition of the Module with the given
     * String name. If no Module defined in the plug-in uses the name
     * given, null is returned.
     */
    public ModuleProperties getModuleProperties(String moduleName) {
        return mapModuleProperties.get(moduleName);
    }
}