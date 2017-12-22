import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import sledgehammer.util.YamlUtil;

@SuppressWarnings("rawtypes")
public class ConfigTest {

	Map data;

	public void run() {
		try {
			FileInputStream fis;
			fis = new FileInputStream(new File("config.yml"));
			data = YamlUtil.getYaml().load(fis);
			System.out.println(data);
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new ConfigTest().run();
	}
}
