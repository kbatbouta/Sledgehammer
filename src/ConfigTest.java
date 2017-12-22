import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import sledgehammer.util.YamlUtil;

public class ConfigTest {
	
	public void run() {
		try {
			FileInputStream fis;
			fis = new FileInputStream(new File("config.yml"));
			Map data = YamlUtil.getYaml().load(fis);
			System.out.println(YamlUtil.getYaml().dump(data));
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new ConfigTest().run();
	}
}
