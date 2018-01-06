package sledgehammer.language;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.util.YamlUtil;

/**
 * Class designed to handle the loading and organization of Language
 * Strings.
 * <p>
 * (Note: The String id's of each entry are stored as non-case sensitive)
 *
 * @author Jab
 */
@SuppressWarnings("rawtypes")
public class LanguageFile {

    /**
     * The Map to store String entries with String values.
     */
    private Map<String, String> mapEntries;
    /**
     * The Map storing the YAML data.
     */
    private Map map;
    /**
     * The File Object of the LanguageFile.
     */
    private File file;
    /**
     * The Language that the LanguageFile represents.
     */
    private Language language;

    /**
     * Main constructor.
     *
     * @param file     The File Object of the LanguageFile.
     * @param language The Language that the LanguageFile represents.
     */
    LanguageFile(File file, Language language) {
        mapEntries = new HashMap<>();
        setFile(file);
        setLanguage(language);
    }

    /**
     * Loads the LanguageFile.
     */
    public void load() {
        Map map = getYamlMap();
        for (Object oKey : map.keySet()) {
            String key = (String) oKey;
            Object oValue = map.get(oKey);
            StringBuilder builder = new StringBuilder();
            String value;
            if (oValue instanceof List) {
                for (Object entry : (List) oValue) {
                    String line = entry.toString();
                    if (builder.length() == 0) {
                        builder.append(line);
                    } else {
                        builder.append(LanguagePackage.NEW_LINE);
                        builder.append(line);
                    }
                }
                value = builder.toString();
            } else {
                value = oValue.toString();
            }
            add(key, value);
        }
    }

    /**
     * @return Reads the File and returns the result YAML data as a Map.
     */
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

    /**
     * @param id The id of the entry.
     * @return Returns the entry with the given id. If no entry is
     * registered with the given id, null is returned.
     */
    public String get(String id) {
        return mapEntries.get(id.toLowerCase());
    }

    /**
     * Adds an entry with the given id.
     *
     * @param id    The ID to identify the entry.
     * @param entry The entry to add.
     */
    public void add(String id, String entry) {
        mapEntries.put(id.toLowerCase(), entry);
    }

    /**
     * @return Returns the File of the LanguageFile.
     */
    public File getFile() {
        return this.file;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the File Object of the LanguageFile.
     *
     * @param file The File to set.
     */
    private void setFile(File file) {
        this.file = file;
    }

    /**
     * @return Returns the Language that the LanguageFile represents.
     */
    public Language getLanguage() {
        return this.language;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the Language that the LanguageFile represents.
     *
     * @param language The Language to set.
     */
    private void setLanguage(Language language) {
        this.language = language;
    }
}