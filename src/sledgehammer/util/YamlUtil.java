package sledgehammer.util;

import org.yaml.snakeyaml.Yaml;

/**
 * Utility class for Yaml operations.
 * 
 * @author Jab
 */
public class YamlUtil {

	/** The <Yaml> instance for <SledgeHammer> to use. */
	private static Yaml yaml = new Yaml();

	/**
	 * @return Returns the global instance of <Yaml>.
	 */
	public static Yaml getYaml() {
		return yaml;
	}
}
