package sledgehammer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.yaml.snakeyaml.error.YAMLException;

import sledgehammer.util.Printable;
import sledgehammer.util.YamlUtil;
import zombie.sledgehammer.util.MD5;

@SuppressWarnings("rawtypes")
public class Settings extends Printable {

	private static final String NEW_LINE = System.getProperty("line.separator");

	private static Settings instance;

	private File fileConfig;
	private Map map;

	private List<String> listAccountsExcluded;
	private String fileConfigName = "config.yml";
	private String administratorPassword;
	private String databaseURL;
	private String databaseUsername;
	private String databasePassword;
	private String databaseDatabase;
	private int accountIdleExpireTime;
	private int explosionRadiusMaximum;
	private int databasePORT;
	private boolean debug = false;
	private boolean allowRCON = false;

	private String pzServerDirectory;

	private String permissionDeniedMessage;

	private boolean allowHelicopters;

	public Settings() {
		fileConfig = new File("config.yml");
		if (!fileConfig.exists()) {
			saveDefaultConfig();
			try {
				generateAdministratorPassword();
				setPZServerDirectory(requestPZDedicatedServerDirectory(), true);
				setDatabaseURL(requestDatabaseURL(), true);
				setDatabasePort(requestDatabasePORT(), true);
				setDatabaseUsername(requestDatabaseUsername(), true);
				setDatabasePassword(requestDatabasePassword(), true);
				setDatabaseDatabase(requestDatabaseDatabase(), true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		loadConfig();
	}

	@Override
	public String getName() {
		return "Sledgehammer->config.yml";
	}

	private void loadConfig() {
		try {
			FileInputStream fis = new FileInputStream(fileConfig);
			map = YamlUtil.getYaml().load(fis);
			fis.close();
			parseConfig();
		} catch (YAMLException e) {
			errorln("Failed to parse YAML.");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			errorln("Config file does not exist.");
		} catch (IOException e) {
			errorln("Failed to read config.yml");
		}
	}

	private void parseConfig() {
		parseGeneralConfig((Map) map.get("general"));
		parseSecurityConfig((Map) map.get("security"));
		parseDatabaseConfig((Map) map.get("mongo_db"));
	}

	private void parseGeneralConfig(Map general) {
		if (general == null) {
			return;
		}
		// (Boolean) general.debug
		Object oDebug = general.get("debug");
		if (oDebug != null) {
			setDebug(getBoolean(oDebug));
		}
		// (String) general.pz_server_directory
		Object oPZServerDirectory = general.get("pz_server_directory");
		if (oPZServerDirectory != null) {
			String s = oPZServerDirectory.toString();
			if (!s.isEmpty()) {
				setPZServerDirectory(s, false);
			} else {
				requestPZDedicatedServerDirectory();
			}
		} else {
			requestPZDedicatedServerDirectory();
		}
		// (String> general.permission_message_denied
		Object oPermissionMessageDenied = general.get("permission_message_denied");
		if (oPermissionMessageDenied != null) {
			setPermissionMessageDenied(oPermissionMessageDenied.toString(), false);
		}

		Object oAccountIdleExpireTime = general.get("account_idle_expire_time");
		if (oAccountIdleExpireTime != null) {
			String s = oAccountIdleExpireTime.toString();
			try {
				int value = Integer.parseInt(s);
				if (value < 0) {
					errorln("account_idle_expire_time not valid: " + s);
					errorln("Number is supposed to be a non-zero, non-negative integer.");
					errorln("Setting value to 0. (disabled)");
					setAccountIdleExpireTime(0, false);
				} else {
					setAccountIdleExpireTime(value, false);
				}

			} catch (NumberFormatException e) {
				errorln("account_idle_expire_time not valid: " + s);
				errorln("Number is supposed to be a non-zero, non-negative integer.");
				errorln("Setting value to 0. (disabled)");
				setAccountIdleExpireTime(0, false);
			}
		}
		listAccountsExcluded = new LinkedList<>();
		Object oAccountIdleExclusions = general.get("account_idle_exclusions");
		if (oAccountIdleExclusions != null && oAccountIdleExclusions instanceof List) {
			List list = (List) oAccountIdleExclusions;
			for (Object o : list) {
				listAccountsExcluded.add(o.toString());
			}
		}

		Object oAllowHelicopters = general.get("allow_helicopters");
		if (oAllowHelicopters != null) {
			setAllowHelicopters(getBoolean(oAllowHelicopters.toString()), false);
		} else {
			setAllowHelicopters(true, false);
		}
	}

	private void parseSecurityConfig(Map security) {
		// (String) security.administrator_password
		Object oAdministratorPassword = security.get("administrator_password");
		if (oAdministratorPassword != null) {
			String s = oAdministratorPassword.toString();
			if (!s.isEmpty()) {
				setAdministratorPassword(s, false);
			} else {
				generateAdministratorPassword();
			}
		} else {
			generateAdministratorPassword();
		}
		// (Integer) security.maximum_explosion_radius
		Object oMaximumExplosionRadius = security.get("maximum_explosion_radius");
		if (oMaximumExplosionRadius != null) {
			try {
				int value = Integer.parseInt(oMaximumExplosionRadius.toString());
				if (value <= 0) {
					value = 12;
					errorln("Number not valid: " + oMaximumExplosionRadius.toString());
					errorln("Number is supposed to be a non-zero, non-negative integer.");
					errorln("Setting value to 12.");
				} else {
					setMaximumExplosionRadius(value);
				}
			} catch (NumberFormatException e) {
				errorln("Failed to set security.maximum_explosion_radius");
				errorln("Number not valid: " + oMaximumExplosionRadius.toString());
				errorln("Number is supposed to be a non-zero, non-negative integer.");
				errorln("Setting value to 12.");
			}
		}
		// (Boolean) security.allow_rcon
		Object oAllowRCON = security.get("allow_rcon");
		if (oAllowRCON != null) {
			setAllowRCON(getBoolean(oAllowRCON));
		}
	}

	private void parseDatabaseConfig(Map mongoDB) {
		// (String) database.url
		Object oDatabaseURL = mongoDB.get("url");
		if (oDatabaseURL != null) {
			String url = oDatabaseURL.toString();
			if (!url.isEmpty()) {
				setDatabaseURL(oDatabaseURL.toString(), false);
			} else {
				setDatabaseURL(requestDatabaseURL(), true);
			}
		}
		// (Short) database.port
		Object oDatabasePort = mongoDB.get("port");
		if (oDatabasePort != null) {
			try {
				short value = Short.parseShort(oDatabasePort.toString());
				if (value == 0) {
					errorln("Failed to set database.port");
					errorln("Number not valid: " + oDatabasePort.toString());
					errorln("Number is supposed to be a non-zero, non-negative signed short (1-32767).");
					errorln("Setting value to 27017.");
					setDatabasePort(27017, true);
				} else {
					setDatabasePort(value, false);
				}
			} catch (NumberFormatException e) {
				errorln("Failed to set database.port");
				errorln("Number not valid: " + oDatabasePort.toString());
				errorln("Number is supposed to be a non-zero, non-negative signed short (1-32767).");
				errorln("Setting value to 27017.");
				setDatabasePort(27017, true);
			}
		}
		// (String) database.username
		Object oDatabaseUsername = mongoDB.get("username");
		if (oDatabaseUsername != null) {
			String username = oDatabaseUsername.toString();
			if (!username.isEmpty()) {
				setDatabaseUsername(username, false);
			} else {
				setDatabaseUsername(requestDatabaseUsername(), true);
			}
		}
		// (String) database.password
		Object oDatabasePassword = mongoDB.get("password");
		if (oDatabasePassword != null) {
			String password = oDatabasePassword.toString();
			if (!password.isEmpty()) {
				setDatabasePassword(password, false);
			} else {
				setDatabasePassword(requestDatabasePassword(), true);
			}
		}
		// (String) database.database
		Object oDatabaseDatabase = mongoDB.get("database");
		if (oDatabaseDatabase != null) {
			String database = oDatabaseDatabase.toString();
			if (!database.isEmpty()) {
				setDatabaseDatabase(oDatabaseDatabase.toString(), false);
			} else {
				setDatabaseDatabase(requestDatabaseDatabase(), true);
			}
		}
	}

	private void setAllowHelicopters(boolean allowHelicopters, boolean save) {
		this.allowHelicopters = allowHelicopters;
		if (save) {
			List<String> lines = readConfigFile();
			for (int index = 0; index < lines.size(); index++) {
				String line = lines.get(index);
				String interp = line.trim();
				if (interp.startsWith("#")) {
					continue;
				}
				int spaces = getLeadingSpaceCount(line);
				if (interp.startsWith("allow_helicopters:")) {
					String newLine = getSpaces(spaces) + "allow_helicopters: \"" + allowHelicopters + "\"";
					lines.set(index, newLine);
				}
			}
			writeConfigFile(lines);
		}
	}

	public void setPermissionMessageDenied(String permissionMessageDenied, boolean save) {
		this.permissionDeniedMessage = permissionMessageDenied;
		if (save) {
			List<String> lines = readConfigFile();
			for (int index = 0; index < lines.size(); index++) {
				String line = lines.get(index);
				String interp = line.trim();
				if (interp.startsWith("#")) {
					continue;
				}
				int spaces = getLeadingSpaceCount(line);
				if (interp.startsWith("permission_message_denied:")) {
					String newLine = getSpaces(spaces) + "permission_message_denied: \"" + permissionMessageDenied
							+ "\"";
					lines.set(index, newLine);
				}
			}
			writeConfigFile(lines);
		}
	}

	public void setPZServerDirectory(String pzServerDirectory, boolean save) {
		this.pzServerDirectory = pzServerDirectory;
		if (save) {
			List<String> lines = readConfigFile();
			for (int index = 0; index < lines.size(); index++) {
				String line = lines.get(index);
				String interp = line.trim();
				if (interp.startsWith("#")) {
					continue;
				}
				int spaces = getLeadingSpaceCount(line);
				if (interp.startsWith("pz_server_directory:")) {
					String newLine = getSpaces(spaces) + "pz_server_directory: \"" + pzServerDirectory + "\"";
					lines.set(index, newLine);
				}
			}
			writeConfigFile(lines);
		}
	}

	public void setAdministratorPassword(String administratorPassword, boolean save) {
		this.administratorPassword = administratorPassword;
		if (save) {
			List<String> lines = readConfigFile();
			for (int index = 0; index < lines.size(); index++) {
				String line = lines.get(index);
				String interp = line.trim();
				if (interp.startsWith("#")) {
					continue;
				}
				int spaces = getLeadingSpaceCount(line);
				if (interp.startsWith("administrator_password:")) {
					String newLine = getSpaces(spaces) + "administrator_password: \"" + administratorPassword + "\"";
					lines.set(index, newLine);
				}
			}
			writeConfigFile(lines);
		}
	}

	public void setDatabaseURL(String databaseURL, boolean save) {
		this.databaseURL = databaseURL;
		if (save) {
			List<String> lines = readConfigFile();
			for (int index = 0; index < lines.size(); index++) {
				String line = lines.get(index);
				String interp = line.trim();
				if (interp.startsWith("#")) {
					continue;
				}
				int spaces = getLeadingSpaceCount(line);
				if (interp.startsWith("url:")) {
					String newLine = getSpaces(spaces) + "url: \"" + databaseURL + "\"";
					lines.set(index, newLine);
				}
			}
			writeConfigFile(lines);
		}
	}

	public void setDatabasePort(int databasePORT, boolean save) {
		this.databasePORT = databasePORT;
		if (save) {
			List<String> lines = readConfigFile();
			for (int index = 0; index < lines.size(); index++) {
				String line = lines.get(index);
				String interp = line.trim();
				if (interp.startsWith("#")) {
					continue;
				}
				int spaces = getLeadingSpaceCount(line);
				if (interp.startsWith("port:")) {
					String newLine = getSpaces(spaces) + "port: " + databasePORT;
					lines.set(index, newLine);
				}
			}
			writeConfigFile(lines);
		}
	}

	public void setDatabaseUsername(String databaseUsername, boolean save) {
		this.databaseUsername = databaseUsername;
		if (save) {
			List<String> lines = readConfigFile();
			for (int index = 0; index < lines.size(); index++) {
				String line = lines.get(index);
				String interp = line.trim();
				if (interp.startsWith("#")) {
					continue;
				}
				int spaces = getLeadingSpaceCount(line);
				if (interp.startsWith("username:")) {
					String newLine = getSpaces(spaces) + "username: \"" + databaseUsername + "\"";
					lines.set(index, newLine);
				}
			}
			writeConfigFile(lines);
		}
	}

	public void setDatabasePassword(String databasePassword, boolean save) {
		this.databasePassword = databasePassword;
		if (save) {
			List<String> lines = readConfigFile();
			for (int index = 0; index < lines.size(); index++) {
				String line = lines.get(index);
				String interp = line.trim();
				if (interp.startsWith("#")) {
					continue;
				}
				int spaces = getLeadingSpaceCount(line);
				if (interp.startsWith("password:")) {
					String newLine = getSpaces(spaces) + "password: \"" + databasePassword + "\"";
					lines.set(index, newLine);
				}
			}
			writeConfigFile(lines);
		}
	}

	public void setDatabaseDatabase(String databaseDatabase, boolean save) {
		this.databaseDatabase = databaseDatabase;
		if (save) {
			List<String> lines = readConfigFile();
			for (int index = 0; index < lines.size(); index++) {
				String line = lines.get(index);
				String interp = line.trim();
				if (interp.startsWith("#")) {
					continue;
				}
				int spaces = getLeadingSpaceCount(line);
				if (interp.startsWith("database:")) {
					String newLine = getSpaces(spaces) + "database: \"" + databaseDatabase + "\"";
					lines.set(index, newLine);
				}
			}
			writeConfigFile(lines);
		}
	}

	public void setPermissionDeniedMessage(String permissionDeniedMessage, boolean save) {
		this.permissionDeniedMessage = permissionDeniedMessage;
		if (save) {
			List<String> lines = readConfigFile();
			for (int index = 0; index < lines.size(); index++) {
				String line = lines.get(index);
				String interp = line.trim();
				if (interp.startsWith("#")) {
					continue;
				}
				int spaces = getLeadingSpaceCount(line);
				if (interp.startsWith("permission_message_denied:")) {
					String newLine = getSpaces(spaces) + "permission_message_denied: \"" + permissionDeniedMessage
							+ "\"";
					lines.set(index, newLine);
				}
			}
			writeConfigFile(lines);
		}
	}

	public String getPZServerDirectory() {
		return this.pzServerDirectory;
	}

	public String getDatabaseDatabase() {
		return this.databaseDatabase;
	}

	public String getDatabasePassword() {
		return this.databasePassword;
	}

	public String getDatabaseUsername() {
		return this.databaseUsername;
	}

	public int getDatabasePort() {
		return this.databasePORT;
	}

	public String getDatabaseURL() {
		return this.databaseURL;
	}

	public boolean allowRCON() {
		return this.allowRCON;
	}

	private void setAllowRCON(boolean flag) {
		this.allowRCON = flag;
	}

	public int getMaximumExplosionRadius() {
		return this.explosionRadiusMaximum;
	}

	private void setMaximumExplosionRadius(int explosionRadiusMaximum) {
		this.explosionRadiusMaximum = explosionRadiusMaximum;
	}

	private void saveDefaultConfig() {
		File file = SledgeHammer.getJarFile();
		write(file, fileConfigName, new File(fileConfigName));
	}

	public boolean isDebug() {
		return this.debug;
	}

	private void setDebug(boolean debug) {
		this.debug = debug;
	}

	public String getAdministratorPassword() {
		return this.administratorPassword;
	}

	public void generateAdministratorPassword() {
		System.out.println("A password has been generated for the 'admin' account.");
		System.out.println("The password is located in the config.yml and can be ");
		System.out.println("changed. Keep this password safe.");
		try {
			setAdministratorPassword(MD5.getMD5Checksum("pz_admin_" + System.nanoTime()), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getPermissionDeniedMessage() {
		return this.permissionDeniedMessage;
	}

	public void setAccountIdleExpireTime(int accountIdleExpireTime, boolean save) {
		this.accountIdleExpireTime = accountIdleExpireTime;
	}

	public int getAccountIdleExpireTime() {
		return this.accountIdleExpireTime;
	}

	public List<String> getExcludedIdleAccounts() {
		return this.listAccountsExcluded;
	}

	public boolean allowHelicopters() {
		return this.allowHelicopters;
	}

	private static List<String> readConfigFile() {
		try {
			List<String> listString = new LinkedList<>();
			FileReader fr = new FileReader("config.yml");
			BufferedReader br = new BufferedReader(fr);
			for (Object oLine : br.lines().toArray()) {
				listString.add((String) oLine);
			}
			br.close();
			fr.close();
			return listString;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void writeConfigFile(List<String> lines) {
		try {
			FileWriter fw = new FileWriter("config.yml");
			BufferedWriter bw = new BufferedWriter(fw);
			for (String line : lines) {
				bw.write(line + NEW_LINE);
			}
			bw.close();
			fw.close();
		} catch (IOException e) {
			System.err.println("Failed to save config.yml");
			e.printStackTrace();
		}
	}

	private static boolean getBoolean(Object object) {
		if (object instanceof Boolean) {
			return ((Boolean) object).booleanValue();
		} else {
			String s = object.toString();
			return s.equals("1") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("on");
		}
	}

	public static Settings getInstance() {
		if (instance == null) {
			instance = new Settings();
		}
		return instance;
	}

	private static String getSpaces(int length) {
		if (length < 0) {
			throw new IllegalArgumentException("length given is less than 0.");
		}
		String string = "";
		for (int index = 0; index < length; index++) {
			string += " ";
		}
		return string;
	}

	private static int getLeadingSpaceCount(String string) {
		if (string == null) {
			throw new IllegalArgumentException("String given is null.");
		}
		if (string.isEmpty()) {
			return 0;
		}
		int spaces = 0;
		char[] chars = string.toCharArray();
		for (int index = 0; index < chars.length; index++) {
			char c = chars[index];
			if (c != ' ') {
				break;
			}
			spaces++;
		}
		return spaces;
	}

	private static String requestDatabaseURL() {
		String databaseURL = null;
		String input = null;
		// Cannot close this. It closes the System.in entirely.
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		while (databaseURL == null) {
			System.out.println("Please enter the MongoDB URL: (localhost)");
			input = scanner.nextLine();
			if (!input.isEmpty()) {
				databaseURL = input;
			} else {
				databaseURL = "localhost";
			}
		}
		return databaseURL;
	}

	private static int requestDatabasePORT() {
		Integer databasePORT = null;
		String input = null;
		// Cannot close this. It closes the System.in entirely.
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		while (databasePORT == null) {
			System.out.println("Please enter the MongoDB url: (27017)");
			input = scanner.nextLine();
			if (!input.isEmpty()) {
				try {
					Integer value = Integer.parseInt(input);
					if (value <= 0) {
						System.out.println(
								"The PORT provided is 0 or less than 0 and needs to be a non-negative integer not exceeding 65534.");
						continue;
					} else if (value > 65534) {
						System.out.println(
								"The PORT provided is greater than 65534 and needs to be a non-negative integer not exceeding 65534.");
						continue;
					}
					databasePORT = value;
				} catch (NumberFormatException e) {
					System.out.println(
							"The PORT provided is invalid and needs to be a non-negative integer not exceeding 65534.");
				}
			} else {
				databasePORT = 27017;
			}
		}
		return databasePORT;
	}

	private static String requestDatabaseUsername() {
		String databaseUsername = null;
		String input = null;
		// Cannot close this. It closes the System.in entirely.
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		while (databaseUsername == null) {
			System.out.println("Please enter the MongoDB username: (sledgehammer)");
			input = scanner.nextLine();
			if (!input.isEmpty()) {
				databaseUsername = input;
			} else {
				databaseUsername = "sledgehammer";
			}
		}
		return databaseUsername;
	}

	private static String requestDatabasePassword() {
		String databasePassword = null;
		String input = null;
		// Cannot close this. It closes the System.in entirely.
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		while (databasePassword == null) {
			System.out.println("Please enter the MongoDB password:");
			input = scanner.nextLine();
			if (!input.isEmpty()) {
				databasePassword = input;
			} else {
				System.out.println("This is not a valid password.");
			}
		}
		return databasePassword;
	}

	private static String requestDatabaseDatabase() {
		String databaseDatabase = null;
		String input = null;
		// Cannot close this. It closes the System.in entirely.
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		while (databaseDatabase == null) {
			System.out.println("Please enter the MongoDB database: (sledgehammer)");
			input = scanner.nextLine();
			if (!input.isEmpty()) {
				databaseDatabase = input;
			} else {
				databaseDatabase = "sledgehammer";
			}
		}
		return databaseDatabase;
	}

	private static String requestPZDedicatedServerDirectory() {
		String pzDirectory = null;
		String input = null;
		// Cannot close this. It closes the System.in entirely.
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		while (pzDirectory == null) {
			System.out.println("Please enter the directory for the Project Zomboid Dedicated Server:");
			input = scanner.nextLine();
			File directory = new File(input);
			if (directory.exists() && directory.isDirectory()) {
				File zombieDirectory = new File(input + File.separator + "java" + File.separator + "zombie");
				if (zombieDirectory.exists() && zombieDirectory.isDirectory()) {
					pzDirectory = input;
				} else {
					System.out.println("This is a directory, but it does not contain Project Zomboid files.");
				}
			} else {
				System.out.println("This is not a valid directory.");
			}
		}
		return pzDirectory.replace("\\", "/");
	}

	private static InputStream getStream(File jar, String source) throws IOException {
		return new URL("jar:file:" + jar.getAbsolutePath() + "!/" + source).openStream();
	}

	public static void write(File jar, String source, File destination) {
		try {
			InputStream is = getStream(jar, source);
			OutputStream os = new FileOutputStream(destination);
			byte[] buffer = new byte[102400];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			is.close();
			os.flush();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}