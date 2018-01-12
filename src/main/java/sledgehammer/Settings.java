/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 */
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.yaml.snakeyaml.error.YAMLException;

import sledgehammer.util.Printable;
import sledgehammer.util.YamlUtil;
import zombie.sledgehammer.util.MD5;

/**
 * Class to load and manage the settings for the Sledgehammer engine.
 *
 * @author Jab
 */
@SuppressWarnings("rawtypes")
public class Settings extends Printable {

    /**
     * The Java OS definition for the new-line operator.
     */
    private static final String NEW_LINE = System.getProperty("line.separator");
    /**
     * The singleton instance of the Settings class.
     */
    private static Settings instance;

    /**
     * The raw YAML Map data for the config.yml File.
     */
    private Map map;
    /**
     * The String names of the Player accounts to exempt from the check to
     * remove inactive accounts.
     */
    private List<String> listAccountsExcluded;
    /**
     * The File Object of the config.yml File.
     */
    private final File fileConfig;
    /**
     * The set directory to the vanilla distribution installation of the
     * ProjectZomboid Dedicated Server.
     */
    private String pzServerDirectory;
    /**
     * The set String message when a permission is denied.
     */
    private String permissionDeniedMessage;
    /**
     * The String password for the Administrator account.
     */
    private String administratorPassword;
    /**
     * The String URL pointing at the MongoDB storing data for Sledgehammer.
     */
    private String databaseURL;
    /**
     * The String PORT the MongoDB server listens on.
     */
    private int databasePORT;
    /**
     * The String username of the Sledgehammer account for MongoDB.
     */
    private String databaseUsername;
    /**
     * The String password of the Sledgehammer account for MongoDB.
     */
    private String databasePassword;
    /**
     * The String database the Sledgehammer account is defined in.
     */
    private String databaseDatabase;
    /**
     * The Integer amount of days an account has until it is considered expired.
     * Set to 0 to disable the utility.
     */
    private int accountIdleExpireTime;
    /**
     * The maximum Integer radius an explosion is allowed for a server. Anything
     * above this limit is registered is cancelled, and invokes a CheaterEvent.
     */
    private int explosionRadiusMaximum;
    /**
     * The Debug flag for the Sledgehammer engine.
     */
    private boolean debug = false;
    /**
     * Flag to enable the native RCON utility for the PZ server.
     */
    private boolean allowRCON = false;
    /**
     * Flag to enable Helicopter events on the PZ server.
     */
    private boolean allowHelicopters;

    private boolean overrideLua;
    private boolean overrideLang;

    /**
     * Main constructor.
     */
    private Settings() {
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

    /**
     * (Private Method)
     * <p>
     * Reads and interprets the YAML from the config.yml File.
     */
    private void loadConfig() {
        try {
            FileInputStream fis = new FileInputStream(fileConfig);
            map = YamlUtil.getYaml().load(fis);
            fis.close();
            parseConfig();
        } catch (YAMLException e) {
            errln("Failed to parse YAML.");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            errln("Config file does not exist.");
        } catch (IOException e) {
            errln("Failed to read config.yml");
        }
    }

    /**
     * (Private Method)
     * <p>
     * Parses and interprets the YAML setting sections.
     */
    private void parseConfig() {
        parseGeneralConfig((Map) map.get("general"));
        parseSecurityConfig((Map) map.get("security"));
        parseDatabaseConfig((Map) map.get("mongo_db"));
    }

    /**
     * (Private Method)
     * <p>
     * Parses and interprets the general section.
     *
     * @param general The Map definition.
     */
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
        // (String) general.permission_message_denied
        Object oPermissionMessageDenied = general.get("permission_message_denied");
        if (oPermissionMessageDenied != null) {
            setPermissionMessageDenied(oPermissionMessageDenied.toString(), false);
        }
        // (String) general.account_idle_expire_time
        Object oAccountIdleExpireTime = general.get("account_idle_expire_time");
        if (oAccountIdleExpireTime != null) {
            String s = oAccountIdleExpireTime.toString();
            try {
                int value = Integer.parseInt(s);
                if (value < 0) {
                    errln("account_idle_expire_time not valid: " + s);
                    errln("Number is supposed to be a non-zero, non-negative integer.");
                    errln("Setting value to 0. (disabled)");
                    setAccountIdleExpireTime(0, false);
                } else {
                    setAccountIdleExpireTime(value, false);
                }

            } catch (NumberFormatException e) {
                errln("account_idle_expire_time not valid: " + s);
                errln("Number is supposed to be a non-zero, non-negative integer.");
                errln("Setting value to 0. (disabled)");
                setAccountIdleExpireTime(0, false);
            }
        }
        listAccountsExcluded = new LinkedList<>();
        // (List) genera.account_idle_exclusions
        Object oAccountIdleExclusions = general.get("account_idle_exclusions");
        if (oAccountIdleExclusions != null && oAccountIdleExclusions instanceof List) {
            List list = (List) oAccountIdleExclusions;
            for (Object o : list) {
                listAccountsExcluded.add(o.toString());
            }
        }
        // (Boolean) general.allow_helicopters
        Object oAllowHelicopters = general.get("allow_helicopters");
        if (oAllowHelicopters != null) {
            setAllowHelicopters(getBoolean(oAllowHelicopters.toString()), false);
        } else {
            setAllowHelicopters(true, false);
        }
        // (Boolean) general.overrideLua
        Object oOverrideLua = general.get("override_lua");
        if (oOverrideLua != null) {
            setOverrideLua(getBoolean(oOverrideLua.toString()), false);
        } else {
            setOverrideLua(false, false);
        }
        // (Boolean) general.overrideLang
        Object oOverrideLang = general.get("override_lang");
        if (oOverrideLang != null) {
            setOverrideLang(getBoolean(oOverrideLang.toString()), false);
        } else {
            setOverrideLang(false, false);
        }
    }

    /**
     * (Private Method)
     * <p>
     * Parses and interprets the security section.
     *
     * @param security The Map definition.
     */
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
                    setMaximumExplosionRadius(12);
                    errln("Number not valid: " + oMaximumExplosionRadius.toString());
                    errln("Number is supposed to be a non-zero, non-negative integer.");
                    errln("Setting value to 12.");
                } else {
                    setMaximumExplosionRadius(value);
                }
            } catch (NumberFormatException e) {
                errln("Failed to set security.maximum_explosion_radius");
                errln("Number not valid: " + oMaximumExplosionRadius.toString());
                errln("Number is supposed to be a non-zero, non-negative integer.");
                errln("Setting value to 12.");
            }
        }
        // (Boolean) security.allow_rcon
        Object oAllowRCON = security.get("allow_rcon");
        if (oAllowRCON != null) {
            setAllowRCON(getBoolean(oAllowRCON));
        }
    }

    /**
     * (Private Method)
     * <p>
     * Parses and interprets the MongoDB database section.
     *
     * @param mongoDB The Map definition.
     */
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
                    errln("Failed to set database.port");
                    errln("Number not valid: " + oDatabasePort.toString());
                    errln("Number is supposed to be a non-zero, non-negative signed short (1-32767).");
                    errln("Setting value to 27017.");
                    setDatabasePort(27017, true);
                } else {
                    setDatabasePort(value, false);
                }
            } catch (NumberFormatException e) {
                errln("Failed to set database.port");
                errln("Number not valid: " + oDatabasePort.toString());
                errln("Number is supposed to be a non-zero, non-negative signed short (1-32767).");
                errln("Setting value to 27017.");
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

    /**
     * @param overrideLua The flag to set.
     * @param save        The flag to save the Settings.
     */
    public void setOverrideLua(boolean overrideLua, boolean save) {
        this.overrideLua = overrideLua;
        if (save) {
            List<String> lines = readConfigFile();
            if (lines != null) {
                for (int index = 0; index < lines.size(); index++) {
                    String line = lines.get(index);
                    String interpreted = line.trim();
                    if (interpreted.startsWith("#")) {
                        continue;
                    }
                    int spaces = getLeadingSpaceCount(line);
                    if (interpreted.startsWith("override_lua:")) {
                        String newLine = getSpaces(spaces) + "override_lua: \"" + overrideLua + "\"";
                        lines.set(index, newLine);
                    }
                }
                writeConfigFile(lines);
            }
        }
    }

    /**
     * @param overrideLang The flag to set.
     * @param save         The flag to save the Settings.
     */
    public void setOverrideLang(boolean overrideLang, boolean save) {
        this.overrideLang = overrideLang;
        if (save) {
            List<String> lines = readConfigFile();
            if (lines != null) {
                for (int index = 0; index < lines.size(); index++) {
                    String line = lines.get(index);
                    String interpreted = line.trim();
                    if (interpreted.startsWith("#")) {
                        continue;
                    }
                    int spaces = getLeadingSpaceCount(line);
                    if (interpreted.startsWith("override_lang:")) {
                        String newLine = getSpaces(spaces) + "override_lang: \"" + overrideLang + "\"";
                        lines.set(index, newLine);
                    }
                }
                writeConfigFile(lines);
            }
        }
    }

    /**
     * Sets the flag for allowing Helicopters.
     *
     * @param save Flag to save the setting.
     */
    public void setAllowHelicopters(boolean allowHelicopters, boolean save) {
        this.allowHelicopters = allowHelicopters;
        if (save) {
            List<String> lines = readConfigFile();
            if (lines != null) {
                for (int index = 0; index < lines.size(); index++) {
                    String line = lines.get(index);
                    String interpreted = line.trim();
                    if (interpreted.startsWith("#")) {
                        continue;
                    }
                    int spaces = getLeadingSpaceCount(line);
                    if (interpreted.startsWith("allow_helicopters:")) {
                        String newLine = getSpaces(spaces) + "allow_helicopters: \"" + allowHelicopters + "\"";
                        lines.set(index, newLine);
                    }
                }
                writeConfigFile(lines);
            }
        }
    }

    /**
     * Sets the message to send when a permission request is denied.
     *
     * @param permissionMessageDenied The message to set.
     * @param save                    Flag to save the setting.
     */
    public void setPermissionMessageDenied(String permissionMessageDenied, boolean save) {
        this.permissionDeniedMessage = permissionMessageDenied;
        if (save) {
            List<String> lines = readConfigFile();
            if (lines != null) {
                for (int index = 0; index < lines.size(); index++) {
                    String line = lines.get(index);
                    String interpreted = line.trim();
                    if (interpreted.startsWith("#")) {
                        continue;
                    }
                    int spaces = getLeadingSpaceCount(line);
                    if (interpreted.startsWith("permission_message_denied:")) {
                        String newLine = getSpaces(spaces) + "permission_message_denied: \"" + permissionMessageDenied
                                + "\"";
                        lines.set(index, newLine);
                    }
                }
                writeConfigFile(lines);
            }
        }
    }

    /**
     * Sets the directory path to the vanilla distribution of the
     * Project Zomboid Dedicated Server.
     *
     * @param pzServerDirectory The directory path to set.
     * @param save              Flag to save the setting.
     */
    public void setPZServerDirectory(String pzServerDirectory, boolean save) {
        this.pzServerDirectory = pzServerDirectory;
        if (save) {
            List<String> lines = readConfigFile();
            if (lines != null) {
                for (int index = 0; index < lines.size(); index++) {
                    String line = lines.get(index);
                    String interpreted = line.trim();
                    if (interpreted.startsWith("#")) {
                        continue;
                    }
                    int spaces = getLeadingSpaceCount(line);
                    if (interpreted.startsWith("pz_server_directory:")) {
                        String newLine = getSpaces(spaces) + "pz_server_directory: \"" + pzServerDirectory + "\"";
                        lines.set(index, newLine);
                    }
                }
                writeConfigFile(lines);
            }
        }
    }

    /**
     * Sets the password of the Administrator Player account for the
     * Sledgehammer engine.
     *
     * @param administratorPassword The password to set.
     * @param save                  Flag to save the setting.
     */
    public void setAdministratorPassword(String administratorPassword, boolean save) {
        this.administratorPassword = administratorPassword;
        if (save) {
            List<String> lines = readConfigFile();
            if (lines != null) {
                for (int index = 0; index < lines.size(); index++) {
                    String line = lines.get(index);
                    String interpreted = line.trim();
                    if (interpreted.startsWith("#")) {
                        continue;
                    }
                    int spaces = getLeadingSpaceCount(line);
                    if (interpreted.startsWith("administrator_password:")) {
                        String newLine = getSpaces(spaces) + "administrator_password: \"" + administratorPassword + "\"";
                        lines.set(index, newLine);
                    }
                }
                writeConfigFile(lines);
            }
        }
    }

    /**
     * Sets the URL to the MongoDB server.
     *
     * @param databaseURL The URL to set.
     * @param save        Flag to save the setting.
     */
    public void setDatabaseURL(String databaseURL, boolean save) {
        this.databaseURL = databaseURL;
        if (save) {
            List<String> lines = readConfigFile();
            if (lines != null) {
                for (int index = 0; index < lines.size(); index++) {
                    String line = lines.get(index);
                    String interpreted = line.trim();
                    if (interpreted.startsWith("#")) {
                        continue;
                    }
                    int spaces = getLeadingSpaceCount(line);
                    if (interpreted.startsWith("url:")) {
                        String newLine = getSpaces(spaces) + "url: \"" + databaseURL + "\"";
                        lines.set(index, newLine);
                    }
                }
                writeConfigFile(lines);
            }
        }
    }

    /**
     * Sets the PORT to the MongoDB server.
     *
     * @param databasePORT The PORT to set.
     * @param save         Flag to save the setting.
     */
    public void setDatabasePort(int databasePORT, boolean save) {
        this.databasePORT = databasePORT;
        if (save) {
            List<String> lines = readConfigFile();
            if (lines != null) {
                for (int index = 0; index < lines.size(); index++) {
                    String line = lines.get(index);
                    String interpreted = line.trim();
                    if (interpreted.startsWith("#")) {
                        continue;
                    }
                    int spaces = getLeadingSpaceCount(line);
                    if (interpreted.startsWith("port:")) {
                        String newLine = getSpaces(spaces) + "port: " + databasePORT;
                        lines.set(index, newLine);
                    }
                }
                writeConfigFile(lines);
            }
        }
    }

    /**
     * Sets the user-name for the Sledgehammer account in the MongoDB
     * server.
     *
     * @param databaseUsername The user-name to set.
     * @param save             Flag to save the setting.
     */
    public void setDatabaseUsername(String databaseUsername, boolean save) {
        this.databaseUsername = databaseUsername;
        if (save) {
            List<String> lines = readConfigFile();
            if (lines != null) {
                for (int index = 0; index < lines.size(); index++) {
                    String line = lines.get(index);
                    String interpreted = line.trim();
                    if (interpreted.startsWith("#")) {
                        continue;
                    }
                    int spaces = getLeadingSpaceCount(line);
                    if (interpreted.startsWith("username:")) {
                        String newLine = getSpaces(spaces) + "username: \"" + databaseUsername + "\"";
                        lines.set(index, newLine);
                    }
                }
                writeConfigFile(lines);
            }
        }
    }

    /**
     * Sets the password for the Sledgehammer account in the MongoDB
     * server.
     *
     * @param databasePassword The password to set.
     * @param save             Flag to save the setting.
     */
    public void setDatabasePassword(String databasePassword, boolean save) {
        this.databasePassword = databasePassword;
        if (save) {
            List<String> lines = readConfigFile();
            if (lines != null) {
                for (int index = 0; index < lines.size(); index++) {
                    String line = lines.get(index);
                    String interpreted = line.trim();
                    if (interpreted.startsWith("#")) {
                        continue;
                    }
                    int spaces = getLeadingSpaceCount(line);
                    if (interpreted.startsWith("password:")) {
                        String newLine = getSpaces(spaces) + "password: \"" + databasePassword + "\"";
                        lines.set(index, newLine);
                    }
                }
                writeConfigFile(lines);
            }
        }
    }

    /**
     * Sets the database for the Sledgehammer account in the MongoDB
     * server.
     *
     * @param databaseDatabase The database to set.
     * @param save             Flag to save the setting.
     */
    public void setDatabaseDatabase(String databaseDatabase, boolean save) {
        this.databaseDatabase = databaseDatabase;
        if (save) {
            List<String> lines = readConfigFile();
            if (lines != null) {
                for (int index = 0; index < lines.size(); index++) {
                    String line = lines.get(index);
                    String interpreted = line.trim();
                    if (interpreted.startsWith("#")) {
                        continue;
                    }
                    int spaces = getLeadingSpaceCount(line);
                    if (interpreted.startsWith("database:")) {
                        String newLine = getSpaces(spaces) + "database: \"" + databaseDatabase + "\"";
                        lines.set(index, newLine);
                    }
                }
                writeConfigFile(lines);
            }
        }
    }

    /**
     * Sets the message sent to a Player when a permission is denied.
     *
     * @param permissionDeniedMessage The message to set.
     * @param save                    Flag to save the setting.
     */
    public void setPermissionDeniedMessage(String permissionDeniedMessage, boolean save) {
        this.permissionDeniedMessage = permissionDeniedMessage;
        if (save) {
            List<String> lines = readConfigFile();
            if (lines != null) {
                for (int index = 0; index < lines.size(); index++) {
                    String line = lines.get(index);
                    String interpreted = line.trim();
                    if (interpreted.startsWith("#")) {
                        continue;
                    }
                    int spaces = getLeadingSpaceCount(line);
                    if (interpreted.startsWith("permission_message_denied:")) {
                        String newLine = getSpaces(spaces) + "permission_message_denied: \"" + permissionDeniedMessage
                                + "\"";
                        lines.set(index, newLine);
                    }
                }
                writeConfigFile(lines);
            }
        }
    }

    /**
     * @return Returns the directory path to the vanilla Project Zomboid
     * Dedicated Server installation.
     */
    public String getPZServerDirectory() {
        return this.pzServerDirectory;
    }

    /**
     * @return Returns the database in the MongoDB server that defines the
     * Sledgehammer account.
     */
    public String getDatabaseDatabase() {
        return this.databaseDatabase;
    }

    /**
     * @return Returns the password for the Sledgehammer MongoDB account.
     */
    public String getDatabasePassword() {
        return this.databasePassword;
    }

    /**
     * @return Returns the user-name for the Sledgehammer MongoDB account.
     */
    public String getDatabaseUsername() {
        return this.databaseUsername;
    }

    /**
     * @return Returns the PORT that the MongoDB service listens on.
     */
    public int getDatabasePort() {
        return this.databasePORT;
    }

    /**
     * @return Returns the URL that points to the MongoDB service.
     */
    public String getDatabaseURL() {
        return this.databaseURL;
    }

    /**
     * @return Returns true if the native RCON utility is allowed to run on the PZ
     * server.
     */
    public boolean allowRCON() {
        return this.allowRCON;
    }

    /**
     * (Private Method)
     *
     * @param flag The flag to set.
     */
    private void setAllowRCON(boolean flag) {
        this.allowRCON = flag;
    }

    /**
     * @return Returns the maximum radius an explosion is allowed for a
     * server. Anything above this limit is registered is cancelled, and
     * invokes a CheaterEvent.
     */
    public int getMaximumExplosionRadius() {
        return this.explosionRadiusMaximum;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the maximum radius an explosion is allowed for a server.
     * Anything above this limit is registered is cancelled, and invokes a
     * CheaterEvent.
     *
     * @param explosionRadiusMaximum The radius to set.
     */
    private void setMaximumExplosionRadius(int explosionRadiusMaximum) {
        this.explosionRadiusMaximum = explosionRadiusMaximum;
    }

    /**
     * (Private Method)
     * <p>
     * Saves the template of the config.yml in the Sledgehammer.jar to the server
     * folder.
     */
    private void saveDefaultConfig() {
        File file = SledgeHammer.getJarFile();
        String fileConfigName = "config.yml";
        write(file, fileConfigName, new File(fileConfigName));
    }

    /**
     * @return Returns true if Sledgehammer is set in debug-mode.
     */
    public boolean isDebug() {
        return this.debug;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the debug flag for Sledgehammer.
     *
     * @param debug The Boolean flag to set.
     */
    private void setDebug(boolean debug) {
        this.debug = debug;
        SledgeHammer.DEBUG = debug;
    }

    /**
     * @return Returns the String password for the Administrator account.
     */
    public String getAdministratorPassword() {
        return this.administratorPassword;
    }

    /**
     * Generates a new String password for the Administrator Player account.
     */
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

    /**
     * @return Returns the String message when a permission is denied.
     */
    public String getPermissionDeniedMessage() {
        return this.permissionDeniedMessage;
    }

    /**
     * Sets the expiration time in days for inactive accounts to be removed.
     *
     * @param accountIdleExpireTime The time in days to set.
     * @param save                  Flag to save the Setting.
     */
    public void setAccountIdleExpireTime(int accountIdleExpireTime, boolean save) {
        this.accountIdleExpireTime = accountIdleExpireTime;
        if (save) {
            // TODO: Implement save.
        }
    }

    /**
     * @return Returns the time in days for inactive accounts to be
     * removed.
     */
    public int getAccountIdleExpireTime() {
        return this.accountIdleExpireTime;
    }

    /**
     * @return Returns a List of account names that are excluded from the
     * inactive-account-removal utility.
     */
    public List<String> getExcludedIdleAccounts() {
        return this.listAccountsExcluded;
    }

    /**
     * @return Returns true if Helicopter events are allows on the PZ server.
     */
    public boolean allowHelicopters() {
        return this.allowHelicopters;
    }

    /**
     * @return Returns true if Lua code is allowed to override from the original code from the Modules.
     */
    public boolean overrideLua() {
        return this.overrideLua;
    }


    /**
     * (Private Method)
     * <p>
     * Reads the config.yml file as a List of lines.
     *
     * @return Returns a List of lines.
     */
    private static List<String> readConfigFile() {
        try {
            FileReader fr = new FileReader("config.yml");
            BufferedReader br = new BufferedReader(fr);
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            br.close();
            fr.close();
            return lines;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * (Private Method)
     * <p>
     * Writes the config.yml File with a List of lines.
     *
     * @param lines The List of lines to save.
     */
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

    /**
     * (Private Method)
     *
     * @param object The Object being interpreted.
     * @return Returns true if the Object identifies as a literal boolean primitive,
     * a packaged Boolean Object, or a String that matches "1", "true",
     * "yes", or "on".
     */
    private static boolean getBoolean(Object object) {
        if (object instanceof Boolean) {
            return ((Boolean) object);
        } else {
            String s = object.toString();
            return s.equals("1") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("on");
        }
    }

    /**
     * @return Returns the singleton instance of the Settings class. If the
     * singleton is not loaded, it is instantiated and loaded before
     * returning the instance.
     */
    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    /**
     * (Private Method)
     *
     * @param length The count of spaces to add to the returned String.
     * @return Returns a valid YAML character sequence of spaces as a String.
     */
    private static String getSpaces(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length given is less than 0.");
        }
        StringBuilder string = new StringBuilder();
        for (int index = 0; index < length; index++) {
            string.append(" ");
        }
        return string.toString();
    }

    /**
     * (Private Method)
     *
     * @param string The String being interpreted.
     * @return Returns the count of spaces in front of any text in the
     * given line.
     */
    private static int getLeadingSpaceCount(String string) {
        if (string == null) {
            throw new IllegalArgumentException("String given is null.");
        }
        if (string.isEmpty()) {
            return 0;
        }
        int spaces = 0;
        char[] chars = string.toCharArray();
        for (char c : chars) {
            if (c != ' ') {
                break;
            }
            spaces++;
        }
        return spaces;
    }

    /**
     * (Private Method)
     *
     * @return Returns input from the console.
     */
    private static String requestDatabaseURL() {
        String databaseURL = null;
        String input;
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

    /**
     * (Private Method)
     *
     * @return Returns input from the console.
     */
    private static int requestDatabasePORT() {
        Integer databasePORT = null;
        String input;
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

    /**
     * (Private Method)
     *
     * @return Returns input from the console.
     */
    private static String requestDatabaseUsername() {
        String databaseUsername = null;
        String input;
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

    /**
     * (Private Method)
     *
     * @return Returns input from the console.
     */
    private static String requestDatabasePassword() {
        String databasePassword = null;
        String input;
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

    /**
     * (Private Method)
     *
     * @return Returns input from the console.
     */
    private static String requestDatabaseDatabase() {
        String databaseDatabase = null;
        String input;
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

    /**
     * (Private Method)
     *
     * @return Returns input from the console.
     */
    private static String requestPZDedicatedServerDirectory() {
        String pzDirectory = null;
        String input;
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

    /**
     * (Private Method)
     *
     * @param jar    The File Object of the Jar File.
     * @param source The source path inside the Jar File.
     * @return Returns an InputStream of the Jar File Entry.
     * @throws IOException Thrown with File Exceptions.
     */
    private static InputStream getStream(File jar, String source) throws IOException {
        return new URL("jar:file:" + jar.getAbsolutePath() + "!/" + source).openStream();
    }

    /**
     * (Private Method)
     * <p>
     * Writes a Jar File Entry to the given destination File.
     *
     * @param jar         The File Object of the Jar File.
     * @param source      The source path inside the Jar File to the Entry.
     * @param destination The File destination to write the Jar File Entry.
     */
    private static void write(File jar, String source, File destination) {
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

    public boolean overrideLang() {
        return this.overrideLang;
    }
}