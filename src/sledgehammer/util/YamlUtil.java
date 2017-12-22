package sledgehammer.util;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;

/**
 * Utility class for Yaml operations.
 * 
 * @author Jab
 */
public class YamlUtil {

	/** The <Yaml> instance for <SledgeHammer> to use. */
	private static Yaml yaml;

	static {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setDefaultScalarStyle(ScalarStyle.LITERAL);
		yaml = new Yaml(options);
	}

	/**
	 * @return Returns the global instance of <Yaml>.
	 */
	public static Yaml getYaml() {
		return yaml;
	}
}
