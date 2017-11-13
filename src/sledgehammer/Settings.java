package sledgehammer;

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

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import sledgehammer.util.INI;
import sledgehammer.util.Printable;
import sledgehammer.util.StringUtils;

/**
 * Class designed to handle SledgeHammer's settings interface.
 * 
 * @author Jab
 *
 */
public class Settings extends Printable {

	private static Settings instance;
	
	public static Settings getInstance() {
		if(instance == null) {
			instance = new Settings();
		}
		return instance;
	}
	/**
	 * INI file for SledgeHammer Settings.
	 */
	private INI ini;

	private String[] pluginList;

	private String permissionDeniedMessage = "Permission Denied.";
	
	private short maximumExplosionRadius = 12;
	
	private short accountIdleExpireTime = 0;
	private String[] accountIdleExclusion = null;
	
	private String pzDirectory;
	
	
	private String url = "localhost";
	private String port = "27019";
	private String username = "sledgehammer";
	private String password = "";
	private String database = "sledgehammer";

	private String passwordAdmin;
	

	/**
	 * Main constructor.
	 * 
	 * @param sledgeHammer
	 */
	public Settings() {
	}

	/**
	 * Reads the settings if the file exists, or creates a new settings file.
	 */
	public void readSettings() {
		// Location of the main configuration file for SledgeHammer.
		String iniFileLocation = "Settings.ini";

		File iniFile = new File(iniFileLocation);
		ini = new INI(iniFile);
		if (iniFile.exists()) {
			try {

				// Create default settings before overwriting any from the file.
				createSettings(ini);

				// Read the settings file.
				ini.read();
				
				String s_administratorPassword = ini.getVariableAsString("GENERAL", "administratorPassword");
				if(s_administratorPassword != null) {
					this.passwordAdmin = StringUtils.md5(s_administratorPassword);
				}
				
				String s_maximumExplosionRadius = ini.getVariableAsString("GENERAL", "maximumExplosionRadius");
				if(s_maximumExplosionRadius != null) {
					try {
						this.maximumExplosionRadius = Short.parseShort(s_maximumExplosionRadius);
					} catch(NumberFormatException e){
						e.printStackTrace();
					}
				} else {
					println("MaximumExplosionRadius is not set. Setting to " + this.maximumExplosionRadius + ".");
				}
				
				String s_pzDirectory = ini.getVariableAsString("GENERAL", "pzDirectory");
				if(s_pzDirectory != null) {
					this.pzDirectory = s_pzDirectory;
				}
				
				String s_accountIdleExpireTime = ini.getVariableAsString("GENERAL", "accountIdleExpireTime");
				if(s_accountIdleExpireTime != null && !s_accountIdleExpireTime.isEmpty() && !s_accountIdleExpireTime.equalsIgnoreCase("null")) {
					try {
						accountIdleExpireTime = Short.parseShort(s_accountIdleExpireTime);
					} catch(NumberFormatException e) {
						e.printStackTrace();
					}
				} else {
					accountIdleExpireTime = 0;
				}
				
				String s_accountIdleExclusion = ini.getVariableAsString("GENERAL", "accountIdleExclusion");
				if(s_accountIdleExclusion != null && !s_accountIdleExclusion.isEmpty() && !s_accountIdleExclusion.equalsIgnoreCase("null")) {
					String[] names = s_accountIdleExclusion.trim().split(",");
					if(names.length > 0) {
						for(int index = 0; index < names.length; index++) {
							names[index] = names[index].trim();
						}						
					}
					println("Added " + names.length + " username(s) to idle expiration exclusions list.");
					this.accountIdleExclusion = names;
				}
				
				// Grab the list of plugins as a string.
				String listPluginsRaw = ini.getVariableAsString("GENERAL", "plugins");

				String debugString = ini.getVariableAsString("GENERAL", "debug");
				if (debugString != null) {
					if (debugString.equalsIgnoreCase("true") || debugString.equalsIgnoreCase("1")
							|| debugString.equalsIgnoreCase("yes") || debugString.equalsIgnoreCase("y")) {
						SledgeHammer.DEBUG = true;
					}
				}
				
				String url = ini.getVariableAsString("DATABASE", "url");
				if(url != null) {
					this.url = url;
				}
				
				String port = ini.getVariableAsString("DATABASE", "port");
				if(port != null) {
					this.port = port;
				}
				
				String username = ini.getVariableAsString("DATABASE", "username");
				if(username != null) {
					this.username = username;
				}
				
				String password = ini.getVariableAsString("DATABASE", "password");
				if(password != null) {
					this.password = password;
				}
				
				String database = ini.getVariableAsString("DATABASE", "database");
				if(database != null) {
					this.database = database;
				}

				// If the setting is blank, handle properly.
				if (listPluginsRaw.isEmpty()) {
					pluginList = new String[0];
				} else {
					// The plug-in entries are comma-delimited.
					pluginList = listPluginsRaw.split(",");
				}

				// Save the INI file, if any default setting is missing.
				try {
					ini.save();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if(this.passwordAdmin == null) {					
					String generatedPassword = ini.getVariableAsString("GENERAL", "administratorPassword");
					this.passwordAdmin = StringUtils.md5(generatedPassword);
				}

			} catch (IOException e) {
				println("Failed to read settings.");
				e.printStackTrace();
			}
		} else {
			createSettings(ini);
			try {
				ini.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String permissionDeniedMessage = ini.getVariableAsString("GENERAL", "permissiondeniedmessage");
		if(permissionDeniedMessage != null && !permissionDeniedMessage.isEmpty()) {
			this.permissionDeniedMessage = permissionDeniedMessage;
		}
	}
	
	/**
	 * Saves the SledgeHammer settings.
	 */
	public void save() {
		try {
			ini.save();
		} catch (Exception e) {
			stackTrace("Failed to save default Sledgehammer settings.", e);
		}
	}

	/**
	 * Creates the INI file from scratch with default values.
	 * 
	 * @param ini
	 */
	private void createSettings(INI ini) {
		ini.createSection("GENERAL"); {			
			ini.setVariable("GENERAL", "administratorPassword", StringUtils.md5(UUID.randomUUID().toString()), "false");
			ini.setVariable("GENERAL", "debug", "false");
			ini.setVariable("GENERAL", "pzDirectory", "", "The directory for the Project Zomboid Dedicated Server.");
			ini.setVariable("GENERAL", "plugins", "");
			ini.setVariable("GENERAL", "helicopter", "true", "Whether or not to enable or disable the helicopter ambient event.");
			ini.setVariable("GENERAL", "permissiondeniedmessage", "You do not have access to that command.");
			ini.setVariable("GENERAL", "allowRCON", "false", "Whether or not to run the vanilla Remote-Console system.");
			ini.setVariable("GENERAL", "maximumExplosionRadius", "12", "This is to allow mods with large explosions, and prevent malicious players with mods to spam large-radius explosions. If you do not need to, do not adjust this value.");
			ini.setVariable("GENERAL", "accountIdleExpireTime", "0", "Expiration time (In days), for accounts to be deleted. If set to 0, this feature is disabled.");
			ini.setVariable("GENERAL", "accountIdleExclusion", "", "Usernames are put here to exclude from the inactivity expiration.");
		}
		
		ini.createSection("DATABASE"); {
			ini.setVariable("DATABASE", "url", "localhost", "The URL or IP to connect to.");
			ini.setVariable("DATABASE", "port", "27019", "The Port to connect to.");
			ini.setVariable("DATABASE", "username", "sledgehammer", "The username to connect with.");
			ini.setVariable("DATABASE", "password", "", "The password to connect with.");
			ini.setVariable("DATABASE", "database", "database", "The database to connect to.");
		}
	
	}
	
	public String[] getPluginList() {
		return pluginList;
	}
	
	public String getPermissionDeniedMessage() {
		return permissionDeniedMessage ;
	}
	
	public void setPermissionDeniedMessage(String string) {
		permissionDeniedMessage = string;
	}
	
	public boolean allowHelicopters() {
		String setting = ini.getVariableAsString("GENERAL", "helicopter");
		return setting.equalsIgnoreCase("true") || setting.equalsIgnoreCase("yes") || setting.equalsIgnoreCase("1");
	}

	/**
	 * Returns the INI instance of the 'SledgeHammer.ini' file.
	 * 
	 * @return
	 */
	public INI getINI() {
		return ini;
	}
	
	@Override
	public String getName() { return "Settings"; }

	public boolean allowRCON() {
		String setting = ini.getVariableAsString("GENERAL", "allowRCON");
		return setting.equalsIgnoreCase("true") || setting.equalsIgnoreCase("yes") || setting.equalsIgnoreCase("1");
	}
	
	public short getMaximumExplosionRadius() {
		return maximumExplosionRadius;
	}
	
	public short getAccountIdleExpireTime() {
		return this.accountIdleExpireTime;
	}
	
	public String[] getAccountIdleExclusions() {
		return this.accountIdleExclusion;
	}
	
	public void setPZDirectory(String pzDirectory) {
		ini.setVariable("GENERAL", "pzDirectory", pzDirectory);
	}

	public String getPZDirectory() {
		return this.pzDirectory;
	}
	
	public String getDatabaseURL() {
		return "mongodb://" + username + ":" + password + "@" + url + "/" + database;
	}

	public String getDatabase() {
		return this.database;
	}
	
	public String getDatabasePort() {
		return this.port;
	}

	public String getAdministratorPassword() {
		return this.passwordAdmin;
	}
}