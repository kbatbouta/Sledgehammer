package sledgehammer.lua.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.lua.LuaTable;
import sledgehammer.util.YamlUtil;

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
		if(oName != null) {
			setPluginName(oName.toString());
		}
		Object oVersion = mapProperties.get("version");
		if(oVersion != null) {
			setPluginVersion(oVersion.toString());
		}
		Object oDescription = mapProperties.get("description");
		if(oDescription != null) {
			setPluginDescription(oDescription.toString());
		}
		
		Map map = (Map) mapProperties.get("modules");
		if(map == null) {
			return;
		}
		mapModuleProperties = new HashMap<>();
		for(Object key : map.keySet()) {
			ModuleProperties moduleProperties = new ModuleProperties((String) key, (Map) map.get(key));
			moduleProperties.setPluginProperties(this);
			addProperties(moduleProperties);
		}
	}

	public void addProperties(ModuleProperties moduleProperties) {
		String moduleName = moduleProperties.getModuleName();
		if(!listModuleNames.contains(moduleName)) {
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