package sledgehammer.module;

import java.io.InputStream;
import java.util.Map;

import sledgehammer.util.YamlUtil;

/**
 * Class to store and define <Module> properties.
 * 
 * @author Jab
 */
public class ModuleProperties {

	/** The <Module> using the properties. */
	private Module module;
	/** The <String> name of the <Module>. */
	private String name;
	/** The <String> version of the <Module>. */
	private String version;
	/** The <String> module location of the <Module> class. */
	private String moduleLocation;
	/** The <String> description of the <Module>. */
	private String description;

	/**
	 * Load constructor.
	 * 
	 * @param yamlStream
	 *            the <InputStream> from the YML source.
	 */
	public ModuleProperties(InputStream yamlStream) {
		// Default variables
		String name = "Untitled Module";
		String version = "1.0";
		String description = "No description.";
		String moduleLocation = "unknown";
		// Load the YAML into a map.
		@SuppressWarnings("rawtypes")
		Map map = YamlUtil.getYaml().load(yamlStream);
		// Grab the name.
		Object oName = map.get("name");
		// If it exists, set it.
		if (oName != null) {
			name = oName.toString();
		}
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
		Object oModuleLocation = map.get("module-location");
		// If it exists, set it.
		if (oModuleLocation != null) {
			moduleLocation = oModuleLocation.toString();
		}
		// Set the result properties.
		setModuleName(name);
		setModuleVersion(version);
		setModuleLocation(moduleLocation);
		setModuleDescription(description);
	}

	/**
	 * (Internal constructor)
	 * 
	 * Used for core modules, or test-cases.
	 * 
	 * @param module
	 *            The <Module> using the properties.
	 * @param name
	 *            The <String> name of the <Module>.
	 * @param version
	 *            The <String> version of the <Module>.
	 * @param moduleLocation
	 *            The <String> location of the <Module> (package.ModuleName)
	 * @param description
	 *            The <String> description of the <Module>.
	 */
	public ModuleProperties(Module module, String name, String version, String moduleLocation, String description) {
		// Set the Module using the properties.
		setModule(module);
		// Set the properties.
		setModuleName(name);
		setModuleVersion(version);
		setModuleLocation(moduleLocation);
		setModuleDescription(description);
	}

	/**
	 * Debug constructor.
	 * @param name
	 */
	public ModuleProperties(String name) {
		// Set default variables
		setModuleName(name);
		setModuleVersion("DEBUG-TEST");
		setModuleDescription("No description.");
		setModuleLocation("unknown");
	}

	/**
	 * Default constructor. Sets default values.
	 */
	public ModuleProperties() {
		// Set default variables
		setModuleName("Untitled Module");
		setModuleVersion("1.0");
		setModuleDescription("No description.");
		setModuleLocation("unknown");
	}

	/**
	 * @return Returns the <String> name of the <Module>.
	 */
	public String getModuleName() {
		return this.name;
	}

	/**
	 * (Internal Method)
	 * 
	 * Sets the <String> name of the <Module>.
	 * 
	 * @param name
	 *            The <String> name to set.
	 */
	private void setModuleName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the <String> version of the <Module>.
	 */
	public String getModuleVersion() {
		return this.version;
	}

	/**
	 * (Internal Method)
	 * 
	 * 
	 * @param version
	 */
	private void setModuleVersion(String version) {
		this.version = version;
	}

	/**
	 * @return Returns the <String> module location of the <Module> class.
	 */
	public String getModuleLocation() {
		return this.moduleLocation;
	}

	/**
	 * (Internal Method)
	 * 
	 * 
	 * @param moduleLocation
	 */
	private void setModuleLocation(String moduleLocation) {
		this.moduleLocation = moduleLocation;
	}

	/**
	 * @return Returns the <String> description of the <Module>.
	 */
	public String getModuleDescription() {
		return this.description;
	}

	/**
	 * (Internal Method)
	 * 
	 * @param description
	 */
	private void setModuleDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the <Module> using the properties.
	 */
	public Module getModule() {
		return this.module;
	}

	/**
	 * Sets the <Module> using the properties.
	 * 
	 * @param module
	 *            The <Module> to set.
	 */
	public void setModule(Module module) {
		this.module = module;
	}
}