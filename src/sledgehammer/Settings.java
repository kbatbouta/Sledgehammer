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

import sledgehammer.util.INI;
import sledgehammer.util.Printable;

/**
 * Class designed to handle SledgeHammer's settings interface.
 * 
 * @author Jab
 *
 */
public class Settings extends Printable {

	/**
	 * INI file for SledgeHammer Settings.
	 */
	private INI ini;

	/**
	 * Instance of SledgeHammer. While this is statically accessible through the
	 * singleton, maintaining an OOP hierarchy is a good practice.
	 */
	private SledgeHammer sledgeHammer;
	
	private String[] pluginList;

	private String permissionDeniedMessage = "Permission Denied.";
	
	private short maximumExplosionRadius = 12;

	/**
	 * Main constructor.
	 * 
	 * @param sledgeHammer
	 */
	public Settings(SledgeHammer sledgeHammer) {
		this.sledgeHammer = sledgeHammer;
	}

	/**
	 * Reads the settings if the file exists, or creates a new settings file.
	 */
	public void readSettings() {
		// Location of the main configuration file for SledgeHammer.
		String iniFileLocation = SledgeHammer.getCacheFolder() + File.separator + "SledgeHammer.ini";

		File iniFile = new File(iniFileLocation);
		ini = new INI(iniFile);
		if (iniFile.exists()) {
			try {

				// Create default settings before overwriting any from the file.
				createSettings(ini);

				// Read the settings file.
				ini.read();
				
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

				// Grab the list of plugins as a string.
				String listPluginsRaw = ini.getVariableAsString("GENERAL", "plugins");

				String debugString = ini.getVariableAsString("GENERAL", "debug");
				if (debugString != null) {
					if (debugString.equalsIgnoreCase("true") || debugString.equalsIgnoreCase("1")
							|| debugString.equalsIgnoreCase("yes") || debugString.equalsIgnoreCase("y")) {
						SledgeHammer.DEBUG = true;
					}
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
		permissionDeniedMessage = ini.getVariableAsString("GENERAL", "permissiondeniedmessage");
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
			ini.setVariable("GENERAL", "debug", "false");
			ini.setVariable("GENERAL", "plugins", "");
			ini.setVariable("GENERAL", "helicopter", "true", "Whether or not to enable or disable the helicopter ambient event.");
			ini.setVariable("GENERAL", "permissiondeniedmessage", "You do not have access to that command.");
			ini.setVariable("GENERAL", "allowRCON", "false", "Whether or not to run the vanilla Remote-Console system.");
			ini.setVariable("GENERAL", "maximumExplosionRadius", "12", "This is to allow mods with large explosions, and prevent malicious players with mods to spam large-radius explosions. If you do not need to, do not adjust this value.");
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
	
	SledgeHammer getSledgeHammer() {
		return sledgeHammer;
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
	
}
