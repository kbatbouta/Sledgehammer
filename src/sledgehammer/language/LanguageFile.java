package sledgehammer.language;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.util.YamlUtil;

@SuppressWarnings("rawtypes")
public class LanguageFile {

	private Map<String, String> mapEntries;
	private Map map;
	private File file;
	private Language language;

	public LanguageFile(File file, Language language) {
		mapEntries = new HashMap<>();
		setFile(file);
		setLanguage(language);
	}

	public void load() {
		Map map = getYamlMap();
		for (Object oKey : map.keySet()) {
			String key = (String) oKey;
			Object oValue = map.get(oKey);
			String value = null;
			if (oValue instanceof List) {
				for (Object entry : (List) oValue) {
					String line = entry.toString();
					if (value == null) {
						value = line;
					} else {
						value += LanguagePackage.NEW_LINE + line;
					}
				}
			} else {
				value = oValue.toString();
			}
			add(key, value);
		}
	}

	public String get(String key) {
		key = key.toLowerCase();
		return mapEntries.get(key);
	}

	public void add(String key, String entry) {
		key = key.toLowerCase();
		mapEntries.put(key, entry);
	}

	public File getFile() {
		return this.file;
	}

	private void setFile(File file) {
		this.file = file;
	}

	private Map getYamlMap() {
		if (map == null) {
			FileInputStream fis;
			try {
				fis = new FileInputStream(getFile());
				map = YamlUtil.getYaml().load(fis);
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return map;
	}

	public Language getLanguage() {
		return this.language;
	}

	private void setLanguage(Language language) {
		this.language = language;
	}
}