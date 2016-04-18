package zirc.module;

import java.io.File;
import java.io.IOException;

import zirc.INI;
import zirc.interfaces.ModuleSettingsHandler;
import zombie.GameWindow;

public abstract class SettingsModule extends Module {
	private INI ini;
	private File iniFile;
	public boolean loadedSettings = false;
	
	public SettingsModule() {}
	
	public void loadSettings(ModuleSettingsHandler handler) {
		
		if(handler == null) throw new IllegalArgumentException("Settings Handler given is null!");
		
		loadedSettings = false;
		
		if(ini == null) getINI();
		
		if(iniFile.exists()) {
			handler.createSettings(getINI());
			try {
				ini.read();
				loadedSettings = true;
			} catch (IOException e) {
				println("Failed to read settings.");
				e.printStackTrace();
			}
		} else {
			println("WARNING: No settings file found. Creating one.");
			println("WARNING: " + getModuleName() + " may require modified settings to run properly.");
			println("Settings file is located at: " + ini.getFile().getAbsolutePath());
			handler.createSettings(ini);
			loadedSettings = true;
			try {
				ini.save();
			} catch (IOException e) {
				println("Failed to save settings.");
				e.printStackTrace();
			}
		}
	}
	
	public boolean loadedSettings() {
		return this.loadedSettings;
	}
	
	public INI getINI() {
		if(ini == null) {			
			iniFile = new File(GameWindow.getCacheDir() + File.separator + "Server" + File.separator + "ZIRC" + File.separator + "plugins" + File.separator + getJarName() + ".ini");
			ini = new INI(iniFile);
		}
		
		return this.ini;
	}

}
