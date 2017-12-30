package sledgehammer.language;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.lua.core.Color;
import sledgehammer.lua.core.Player;

public class LanguagePackage {

	private static Map<Character, String> mapColors = new HashMap<>();
	
	public static final String NEW_LINE = "\n";
	
	private Map<Language, LanguageFile> mapLanguageFiles;

	private File directory;

	private String name;

	public LanguagePackage(File directory, String name) {
		mapLanguageFiles = new HashMap<>();
		setDirectory(directory);
		setPackageName(name);
	}
	
	public void load() {
		String packageName = getPackageName();
		File[] files = directory.listFiles();
		for (File file : files) {
			String name = file.getName().toLowerCase();
			System.out.println("Checking file: " + name);
			if (name.startsWith(packageName) && name.endsWith(".yml")) {
				System.out.println("Loading file: " + name);
				Language language = Language.getLanguageWithAbbreviation(name.split("_")[1].split("\\.")[0]);
				LanguageFile languageFile = new LanguageFile(file, language);
				languageFile.load();
				mapLanguageFiles.put(language, languageFile);
			}
		}
	}

	public String getAnyString(String key) {
		Language language = Language.English;
		String value = getString(key, language);
		if (value == null) {
			for (Language languageNext : Language.values()) {
				value = getString(key, languageNext);
				if (value != null) {
					break;
				}
			}
		}
		return value;
	}

	public String getString(String entry) {
		return getString(entry, Language.English, new EntryField[] {});
	}

	public String getString(String key, Language language, EntryField... entries) {
		String value = getString(key, language);
		value = processString(value, this, language, entries);
		return value;
	}

	public String getString(String key, EntryField... entries) {
		return getString(key, Language.English, entries);
	}

	public String getString(String key, Language language) {
		String value = null;
		LanguageFile file = mapLanguageFiles.get(language);
		if (file != null) {
			value = file.get(key);
		}
		return value;
	}

	public List<String> getAnyStringList(String key) {
		return toList(getAnyString(key));
	}

	public List<String> getStringList(String key) {
		return toList(getString(key));
	}

	public List<String> getStringList(String key, Language language, EntryField... entries) {
		return toList(getString(key, language, entries));
	}

	public List<String> getStringList(String key, EntryField... entries) {
		return toList(getString(key, entries));
	}

	public List<String> getStringList(String key, Language language) {
		return toList(getString(key, language));
	}

	public File getDirectory() {
		return this.directory;
	}

	private void setDirectory(File directory) {
		this.directory = directory;
	}

	public String getPackageName() {
		return this.name;
	}

	private void setPackageName(String name) {
		this.name = name;
	}

	public void sendMessage(Player player, String key, Language language, EntryField... entries) {
		if (player == null) {
			throw new IllegalArgumentException("Player given is null.");
		}
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key given is null or empty.");
		}
		if (!player.isOnline()) {
			return;
		}
		String result = this.getString(key, language, entries);
		if (result != null) {
			sendMessage(player, toStringArray(result));
		}
	}

	public void sendMessage(Player player, String key, EntryField... entries) {
		if (player == null) {
			throw new IllegalArgumentException("Player given is null.");
		}
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key given is null or empty.");
		}
		if (!player.isOnline()) {
			return;
		}
		String result = this.getString(key, entries);
		if (result != null) {
			sendMessage(player, toStringArray(result));
		}
	}

	public static String processString(String value, LanguagePackage languagePackage, Language language,
			EntryField... entries) {
		if (value == null) {
			return null;
		}
		if (languagePackage != null && language == null) {
			language = Language.English;
		}
		String valueProcessed = "";
		boolean in = false;
		char[] chars = value.toCharArray();
		String key = "";
		String valNext = null;
		Map<String, String> mapStrings = new HashMap<>();
		for (int index = 0; index < chars.length; index++) {
			char charCurrent = chars[index];
			Character charNext = index < chars.length - 1 ? chars[index + 1] : null;
			if (in) {
				if (charCurrent == '}' && charNext == '}') {
					in = false;
					index += 1;
					key = key.toLowerCase().trim();
					valNext = mapStrings.get(key);
					if (valNext == null) {
						for (EntryField entryNext : entries) {
							if (entryNext != null && entryNext.isKey(key)) {
								valNext = entryNext.getValue().toString();
								mapStrings.put(key, valNext);
								break;
							}
						}
					}
					if (valNext == null && languagePackage != null) {
						valNext = languagePackage.getString(key, language, entries);
						if (valNext != null) {
							mapStrings.put(key, valNext);
						}
					}
					if (valNext != null) {
						valueProcessed += valNext;
					} else {
						valueProcessed += key;
					}
				} else {
					key += charCurrent;
				}
			} else {
				if (charCurrent == '[' && charNext == '&' && index + 3 <= chars.length - 1) {
					Character charChatColor = Character.toLowerCase(chars[index + 2]);
					String colorCode = mapColors.get(charChatColor);
					if (colorCode == null) {
						colorCode = Color.WHITE.toTag();
					}
					valueProcessed += colorCode;
					index += 3;
				} else if (charCurrent == '{' && charNext == '{') {
					in = true;
					index += 1;
					key = "";
					continue;
				} else {
					valueProcessed += charCurrent;
				}
			}
		}
		return valueProcessed;
	}

	public static void sendMessage(Player player, String[] lines) {
		if (lines == null || lines.length == 0) {
			return;
		}
		if (player == null) {
			throw new IllegalArgumentException("CommandSender given is null.");
		}
		player.sendChatMessage(lines);
	}

	public static void sendMessage(Player player, List<String> lines) {
		if (player == null) {
			throw new IllegalArgumentException("CommandSender given is null.");
		}
		String[] array = toStringArray(lines);
		if (array != null) {
			player.sendChatMessage(array);
		}
	}
	
	public static void broadcastMessages(List<String> list) {
		if(list == null || list.size() == 0) {
			return;
		}
		for(String line : list) {
			SledgeHammer.instance.broadcastMessage(line);
		}
	}
	
	public static List<String> toList(String string) {
		if (string == null) {
			return null;
		}
		List<String> list = new LinkedList<>();
		for (String line : string.split(NEW_LINE)) {
			list.add(line);
		}
		return list;
	}

	public static String[] toStringArray(List<String> list) {
		if(list == null) {
			return null;
		}
		String[] returned = new String[list.size()];
		for (int index = 0; index < list.size(); index++) {
			returned[index] = list.get(index);
		}
		return returned;
	}

	public static String[] toStringArray(String string) {
		if (string == null) {
			return null;
		}
		return string.split(NEW_LINE);
	}

	public static String processString(String value, EntryField... entries) {
		return processString(value, null, Language.English, entries);
	}
	
	// @formatter:off
	public static void main(String[] args) {
		String testText;
		EntryField[] fields;
		// Test 1.
		testText = "Hello {{name1}}, My name is {{name2}}.";
		fields = new EntryField[] { 
			new EntryField("Name1", "John"),
			new EntryField("naMe2", "Bob") 
		};
		String result = processString(testText, null, null, fields);
		System.out.println("Test String: " + testText);
		System.out.println("Result String: " + result);
		// Test 2.
		testText = "{{start}} is better than {{end}}";
		fields = new EntryField[] { 
			new EntryField("start", "Starting"),
			new EntryField("end", "Ending.") 
		};
		result = processString(testText, null, null, fields);
		System.out.println("Test String: " + testText);
		System.out.println("Result String: " + result);
	}
	// @formatter:on
	
	static {
		mapColors.put('0', Color.BLACK.toTag());
		mapColors.put('1', Color.DARK_BLUE.toTag());
		mapColors.put('2', Color.DARK_GREEN.toTag());
		mapColors.put('3', Color.DARK_AQUA.toTag());
		mapColors.put('4', Color.DARK_RED.toTag());
		mapColors.put('5', Color.DARK_PURPLE.toTag());
		mapColors.put('6', Color.GOLD.toTag());
		mapColors.put('7', Color.LIGHT_GRAY.toTag());
		mapColors.put('8', Color.DARK_GRAY.toTag());
		mapColors.put('9', Color.INDIGO.toTag());
		mapColors.put('a', Color.GREEN.toTag());
		mapColors.put('b', Color.AQUA.toTag());
		mapColors.put('c', Color.RED.toTag());
		mapColors.put('d', Color.PINK.toTag());
		mapColors.put('e', Color.YELLOW.toTag());
		mapColors.put('f', Color.WHITE.toTag());
	}
}