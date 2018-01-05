package sledgehammer.language;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.lua.core.Color;
import sledgehammer.lua.core.Player;

/**
 * LanguagePackage is a utility that provides the ability to substitute sections
 * of a string recursively. This allows for Strings to be dynamically edited,
 * and defined anywhere within the String to be injected with <EntryField>'s.
 * Adding to this is the ability to select what <Language> to choose from,
 * falling back to English if not defined.
 * 
 * @author Jab
 *
 */
public class LanguagePackage {

	/**
	 * The <Map> of Sledgehammer <Color> constants, assigned to hexadecimal
	 * characters.
	 */
	private static Map<Character, String> mapColors = new HashMap<>();

	/** The standard 'line.separator' for most Java Strings. */
	public static final String NEW_LINE = "\n";

	/**
	 * The <Map> for <LanguageFiles>, assigned with their <Language>'s.
	 */
	private Map<Language, LanguageFile> mapLanguageFiles;

	/**
	 * The <File> Object for the directory where the <LanguageFile>'s are stored.
	 */
	private File directory;

	/**
	 * The <String> name of the <LanguagePackage>. This is noted in the
	 * <LanguageFile>'s as "{{name}}_{{language_abbreviation}}.yml"
	 */
	private String name;

	/**
	 * Main constructor.
	 * 
	 * @param directory
	 *            The <File> Object for the directory where the <LanguageFile>'s are
	 *            stored.
	 * @param name
	 *            The <String> name of the <LanguagePackage>. This is noted in the
	 *            <LanguageFile>'s as "{{name}}_{{language_abbreviation}}.yml"
	 */
	public LanguagePackage(File directory, String name) {
		mapLanguageFiles = new HashMap<>();
		setDirectory(directory);
		setPackageName(name);
	}

	/**
	 * Loads the <LanguagePackage>.
	 */
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

	/**
	 * Sends a processed <String> Message to a <Player> with a given <Language>, and
	 * additionally defined <EntryField>'s.
	 * 
	 * @param player
	 *            The <Player> to send the <String> message.
	 * @param key
	 *            The <String> identity of the entry to process.
	 * @param language
	 *            The <Language> to use for the <Player>.
	 * @param entries
	 *            The <EntryList> Array of any additional entries to process with
	 *            the <String> message.
	 */
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

	/**
	 * Sends a processed <String> Message to a <Player> with a English <Language>,
	 * and additionally defined <EntryField>'s.
	 * 
	 * @param player
	 *            The <Player> to send the <String> message.
	 * @param key
	 *            The <String> identity of the entry to process.
	 * @param entries
	 *            The <EntryList> Array of any additional entries to process with
	 *            the <String> message.
	 */
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

	/**
	 * @param key
	 *            The <String> identity of the entry to process.
	 * @return Returns a processed <String>. If the English <LanguageFile> does not
	 *         contain an entry for the given String identity, other LanguageFiles
	 *         are checked. If none of the remaining LanguageFiles contains an
	 *         entry, null is returned.
	 */
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

	/**
	 * @param key
	 *            The <String> identity of the entry to process.
	 * @return Returns a processed <String> in the English <LanguageFile>. If the
	 *         LanguageFile does not contain an entry, null is returned.
	 */
	public String getString(String key) {
		return getString(key, Language.English, new EntryField[] {});
	}

	/**
	 * @param key
	 *            The <String> identity of the entry to process.
	 * @param language
	 *            The <Language> to search for the <String> entry primarily.
	 * @param entries
	 *            Any additional <EntryField>'s that add to or override the
	 *            <LanguagePackage> library.
	 * @return Returns a processed <String> in the given <Language>. If the
	 *         <LanguageFile> does not contain an entry, null is returned.
	 */
	public String getString(String key, Language language, EntryField... entries) {
		String value = getString(key, language);
		value = processString(value, this, language, entries);
		return value;
	}

	/**
	 * @param key
	 *            The <String> identity of the entry to process.
	 * @param entries
	 *            Any additional <EntryField>'s that add to or override the
	 *            <LanguagePackage> library.
	 * @return Returns a processed <String> in the English <LanguageFile>. If the
	 *         LanguageFile does not contain an entry, null is returned.
	 */
	public String getString(String key, EntryField... entries) {
		return getString(key, Language.English, entries);
	}

	/**
	 * @param key
	 *            The <String> identity of the entry to process.
	 * @param language
	 *            The <Language> to search for the <String> entry primarily.
	 * @return Returns a processed <String> in the given <Language>. If the
	 *         <LanguageFile> does not contain an entry, null is returned.
	 */
	public String getString(String key, Language language) {
		String value = null;
		LanguageFile file = mapLanguageFiles.get(language);
		if (file != null) {
			value = file.get(key);
			value = processString(value, this, language);
		}
		return value;
	}

	/**
	 * @param key
	 *            The <String> identity of the entry to process.
	 * @return Returns a <List> of processed <String>'s.
	 */
	public List<String> getAnyStringList(String key) {
		return toList(getAnyString(key));
	}

	/**
	 * @param key
	 *            The <String> identity of the entry to process.
	 * @return Returns a <List> of processed <String>'s.
	 */
	public List<String> getStringList(String key) {
		return toList(getString(key));
	}

	/**
	 * @param key
	 *            The <String> identity of the entry to process.
	 * @param language
	 *            The <Language> to search for the <String> entry primarily.
	 * @param entries
	 *            Any additional <EntryField>'s that add to or override the
	 *            <LanguagePackage> library.
	 * @return Returns a <List> of processed <String>'s.
	 */
	public List<String> getStringList(String key, Language language, EntryField... entries) {
		return toList(getString(key, language, entries));
	}

	/**
	 * @param key
	 *            The <String> identity of the entry to process.
	 * @param entries
	 *            Any additional <EntryField>'s that add to or override the
	 *            <LanguagePackage> library.
	 * @return Returns a <List> of processed <String>'s.
	 */
	public List<String> getStringList(String key, EntryField... entries) {
		return toList(getString(key, entries));
	}

	/**
	 * @param key
	 *            The <String> identity of the entry to process.
	 * @param language
	 *            The <Language> to search for the <String> entry primarily.
	 * @return Returns a <List> of processed <String>'s.
	 */
	public List<String> getStringList(String key, Language language) {
		return toList(getString(key, language));
	}

	/**
	 * @return Returns the <File> Object of the directory where the <LanguageFile>'s
	 *         are located.
	 */
	public File getDirectory() {
		return this.directory;
	}

	/**
	 * (Private Method)
	 * 
	 * Sets the <File> Object of the directory where the <LanguageFile>'s are
	 * located.
	 * 
	 * @param directory
	 */
	private void setDirectory(File directory) {
		this.directory = directory;
	}

	/**
	 * @return Returns the <String> name of the <LibraryPackage>. This is noted in
	 *         the <LanguageFile>'s as "{{name}}_{{language_abbreviation}}.yml"
	 */
	public String getPackageName() {
		return this.name;
	}

	/**
	 * (Private Method)
	 * 
	 * Sets the <String> name of the <LibraryPackage>. This is noted in the
	 * <LanguageFile>'s as "{{name}}_{{language_abbreviation}}.yml"
	 * 
	 * @param name
	 *            The <String> name to set.
	 */
	private void setPackageName(String name) {
		this.name = name;
	}

	/**
	 * Processes a <String> with the <LanguagePackage> library, using the given
	 * <Language> as an option. Any <EntryField>'s passed to this method override
	 * entries already defined in the LanguagePackage provided.
	 * 
	 * @param value
	 *            The <String> to be processed.
	 * @param languagePackage
	 *            The <LanguagePackage> library to reference for any <EntryField>'s
	 *            not defined that are requested.
	 * @param language
	 *            The <Language> to use primarily. If <EntryField>'s are not
	 *            defined, and the <LanguageFile> referenced using the <Language>
	 *            provided does not have a definition, then the LanguagePackage's
	 *            English LanguageFile is checked for that definition.
	 * @param entries
	 *            The <EntryField> Array to add to or override the <LanguagePackage>
	 *            library if passed.
	 * @return Returns the processed <String> value.
	 */
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
					System.out.println(" Next Key: " + key);
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

	/**
	 * Sends a String Array of messages to a <Player>.
	 * 
	 * @param player
	 *            The <Player> receiving the messages.
	 * @param lines
	 *            The <String> Array of messages to send.
	 */
	public static void sendMessage(Player player, String[] lines) {
		if (lines == null || lines.length == 0) {
			return;
		}
		if (player == null) {
			throw new IllegalArgumentException("CommandSender given is null.");
		}
		player.sendChatMessages(lines);
	}

	/**
	 * Sends a <List> of <String> messages to a <Player>.
	 * 
	 * @param player
	 *            The <Player> receiving the messages.
	 * @param lines
	 *            The <List> of <String> messages to send.
	 */
	public static void sendMessage(Player player, List<String> lines) {
		if (player == null) {
			throw new IllegalArgumentException("CommandSender given is null.");
		}
		String[] array = toStringArray(lines);
		if (array != null) {
			player.sendChatMessages(array);
		}
	}

	/**
	 * Broadcasts a <List> of <String> messages to all <Player>'s on a server.
	 * 
	 * @param lines
	 *            The <List> of <String> messages to send.
	 */
	public static void broadcastMessages(List<String> lines) {
		if (lines == null || lines.size() == 0) {
			return;
		}
		for (String line : lines) {
			SledgeHammer.instance.broadcastMessage(line);
		}
	}

	/**
	 * @param string
	 *            The <String> to partition with the '\n' operator.
	 * @return Returns a <List> of <Strings>, partitioned by the '\n' operator.
	 */
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

	/**
	 * Converts a <List> of <String>'s to a String Array.
	 * 
	 * @param list
	 *            The <List> to convert.
	 * @return Returns a <String> Array of the String Lines in the <List> provided.
	 */
	public static String[] toStringArray(List<String> list) {
		if (list == null) {
			return null;
		}
		String[] returned = new String[list.size()];
		for (int index = 0; index < list.size(); index++) {
			returned[index] = list.get(index);
		}
		return returned;
	}

	/**
	 * @param string
	 *            The <String> to partition with the '\n' operator.
	 * @return Returns a <String> Array, partitioned by the '\n' operator.
	 */
	public static String[] toStringArray(String string) {
		if (string == null) {
			return null;
		}
		return string.split(NEW_LINE);
	}

	/**
	 * @param value
	 *            The <String> to be processed.
	 * @param entries
	 *            The <EntryField> Array to add to or override the <LanguagePackage>
	 *            library if passed.
	 * @return Returns the processed <String>.
	 */
	public static String processString(String value, EntryField... entries) {
		return processString(value, null, Language.English, entries);
	}

	// @formatter:off
	/**
	 * Test-case Entry-point for <LanguagePackage>.
	 * @param args The Java String Array arguments.
	 */
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
	
	static {
		mapColors.put('0', Color.BLACK.toTag()      );
		mapColors.put('1', Color.DARK_BLUE.toTag()  );
		mapColors.put('2', Color.DARK_GREEN.toTag() );
		mapColors.put('3', Color.DARK_AQUA.toTag()  );
		mapColors.put('4', Color.DARK_RED.toTag()   );
		mapColors.put('5', Color.DARK_PURPLE.toTag());
		mapColors.put('6', Color.GOLD.toTag()       );
		mapColors.put('7', Color.LIGHT_GRAY.toTag() );
		mapColors.put('8', Color.DARK_GRAY.toTag()  );
		mapColors.put('9', Color.INDIGO.toTag()     );
		mapColors.put('a', Color.GREEN.toTag()      );
		mapColors.put('b', Color.AQUA.toTag()       );
		mapColors.put('c', Color.RED.toTag()        );
		mapColors.put('d', Color.PINK.toTag()       );
		mapColors.put('e', Color.YELLOW.toTag()     );
		mapColors.put('f', Color.WHITE.toTag()      );
	}
	// @formatter:on
}