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
package sledgehammer.lua.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.lua.LuaTable;
import sledgehammer.util.YamlUtil;

/**
 * TODO: Document
 * 
 * @author Jab
 */
@SuppressWarnings("rawtypes")
public class PluginProperties extends LuaTable {

	private String name;
	private String version;
	private String description;

	private List<String> listModuleNames;
	private Map<String, ModuleProperties> mapModuleProperties;

	private Map mapProperties;

	public PluginProperties(InputStream inputStream) {
		super("PluginProperties");
		mapProperties = YamlUtil.getYaml().load(inputStream);
		listModuleNames = new ArrayList<>();
		mapModuleProperties = new HashMap<>();
		loadProperties();
	}

	public PluginProperties(String name, String version, String description) {
		super("PluginProperties");
		setPluginName(name);
		setPluginVersion(version);
		setPluginDescription(description);
		listModuleNames = new ArrayList<>();
		mapModuleProperties = new HashMap<>();
	}

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

	public void addProperties(ModuleProperties moduleProperties) {
		String moduleName = moduleProperties.getModuleName();
		if (!listModuleNames.contains(moduleName)) {
			listModuleNames.add(moduleName);
		}
		mapModuleProperties.put(moduleName, moduleProperties);
	}

	public String getPluginName() {
		return this.name;
	}

	private void setPluginName(String name) {
		this.name = name;
	}

	public String getPluginVersion() {
		return this.version;
	}

	private void setPluginVersion(String version) {
		this.version = version;
	}

	public String getPluginDescription() {
		return this.description;
	}

	private void setPluginDescription(String description) {
		this.description = description;
	}

	public List<String> getModuleNames() {
		return this.listModuleNames;
	}

	public ModuleProperties getModuleProperties(String moduleName) {
		return mapModuleProperties.get(moduleName);
	}
}