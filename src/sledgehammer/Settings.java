package sledgehammer;

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
	 * SledgeHammer instance using the Settings instance.
	 */
	private SledgeHammer sledgeHammer;

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
					sledgeHammer.getModuleManager().setPluginList(new String[0]);
				} else {
					// The plug-in entries are comma-delimited.
					sledgeHammer.getModuleManager().setPluginList(listPluginsRaw.split(","));
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
		sledgeHammer.getPermissionsManager().setPermissionDeniedMessage(ini.getVariableAsString("GENERAL", "permissiondeniedmessage"));
	}

	/**
	 * Creates the INI file from scratch with default values.
	 * 
	 * @param ini
	 */
	private void createSettings(INI ini) {
		ini.createSection("GENERAL");
		ini.setVariable("GENERAL", "debug", "false");
		ini.setVariable("GENERAL", "plugins", "");
		ini.setVariable("GENERAL", "permissiondeniedmessage", "You do not have access to that command.");
	}

	/**
	 * Returns the INI instance of the 'SledgeHammer.ini' file.
	 * 
	 * @return
	 */
	public INI getINI() {
		return ini;
	}

	public String getName() { return "Settings"; }

	public void save() {
		try {
			ini.save();
		} catch (Exception e) {
			stackTrace("Failed to save default Sledgehammer settings.", e);
		}
	}
}
