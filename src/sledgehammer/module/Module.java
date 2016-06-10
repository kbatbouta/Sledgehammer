package sledgehammer.module;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.ModuleManager;
import sledgehammer.event.Event;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.interfaces.EventListener;
import sledgehammer.interfaces.LogListener;
import sledgehammer.interfaces.ModuleSettingsHandler;
import sledgehammer.interfaces.PermissionHandler;
import sledgehammer.util.INI;
import sledgehammer.util.Printable;
import sledgehammer.PermissionsManager;

public abstract class Module extends Printable {
	
	private INI ini;
	
	private File iniFile;
	
	public boolean loadedSettings = false;
	
	private boolean loaded = false;
	
	private boolean started = false;
	
	private String jarName = null;
	
	private Map<String, String> pluginSettings = new HashMap<>();
	
	public void loadSettings(ModuleSettingsHandler handler) {

		if (handler == null)
			throw new IllegalArgumentException("Settings Handler given is null!");

		loadedSettings = false;

		if (ini == null)
			getINI();

		if (iniFile.exists()) {
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
			println("WARNING: " + getName() + " may require modified settings to run properly.");
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
	
	public void register(CommandListener listener) {
		String[] commands = listener.getCommands();
		if(commands == null) {
			throw new IllegalArgumentException("CommandListener commands array is null!");
		} else
		if(commands.length == 0) {
			throw new IllegalArgumentException("CommandListener commands array is empty!");
		}
		for(String command : commands) {
			SledgeHammer.instance.register(command, listener);			
		}
	}
	
	public boolean stopModule() {
		if(loaded) {
			try {
				if(started) {					
					this.onStop();
				} else {
					println("Module is already stopped.");
				}
			} catch(Exception e) {
				println("Failed to safely stop module.");
				e.printStackTrace();
			}
		}
		loaded = false;
		return true;
	}
	
	public boolean loadModule() {
		try {	
			onLoad();
			loaded = true;
			return true;
		} catch(Exception e) {
			println("Failed to load module.");
			loaded = false;
			e.printStackTrace();
			
		}
		return false;
	}
	
	public boolean unloadModule() {
		try {
			if(loaded) {				
				this.onUnload();
			} else {
			}
		} catch(Exception e) {
			println("Failed to safely unload module.");
			e.printStackTrace();
		}
		return true;
	}
	
	public void register(EventListener listener) {
		String[] types = listener.getTypes();
		if(types == null) {
			throw new IllegalArgumentException("EventListener getTypes() array is null!");
		}
		for(String type : types) {			
			SledgeHammer.instance.register(type, listener);
		}
	}

	public void startModule() {
		if(!started) {			
			started = true;
			onStart();
		} else {
			println("Module is already started.");
		}
	}
	
	public INI getINI() {
		if (ini == null) {
			iniFile = new File(SledgeHammer.getCacheFolder() + File.separator + "plugins" + File.separator + getJarName() + ".ini");
			ini = new INI(iniFile);
		}

		return this.ini;
	}
	
	public void unload() {
		started = false;
		getModuleManager().unloadModule(this);
	}
	
	public ModuleManager getModuleManager() {
		return SledgeHammer.instance.getModuleManager();
	}
	
	public String getPermissionDeniedMessage() {
		return SledgeHammer.instance.getPermissionsManager().getPermissionDeniedMessage();
	}
	
	public Map<String, String> getPluginSettings() {
		return this.pluginSettings;
	}
	
	public void setPluginSettings(Map<String, String> map) {
		this.pluginSettings = map;
	}
	
	public String getJarName() {
		return this.jarName;
	}
	
	public void setJarName(String jarName) {
		this.jarName = jarName;
	}
	
	public void handleEvent(Event event, boolean shouldLog) {
		SledgeHammer.instance.handle(event, shouldLog);
	}
	
	public void handleEvent(Event event) {
		SledgeHammer.instance.handle(event);
	}
	
	public Module getModuleByID(String ID) {
		return getModuleManager().getModuleByID(ID);
	}
	
	public boolean hasPermission(String username, String context) {
		return getPermissionsManager().hasPermission(username, context);
	}
	
	public PermissionsManager getPermissionsManager() {
		return SledgeHammer.instance.getPermissionsManager();
	}
	
	public void register(PermissionHandler handler) {
		getPermissionsManager().registerPermissionHandler(handler);
	}
	
	public void updateModule(long delta) {
		if(started) onUpdate(delta);
	}
	
	public boolean loadedSettings() {
		return this.loadedSettings;
	}
	
	public void register(LogListener listener) {
		SledgeHammer.instance.register(listener);
	}
	
	public void register(String type, EventListener listener) {
		SledgeHammer.instance.register(type, listener);
	}
	
	public void register(String command, CommandListener listener) {
		SledgeHammer.instance.register(command, listener);
	}
	
	public abstract void onLoad();
	public abstract void onStart();
	public abstract void onUpdate(long delta);
	public abstract void onStop();
	public abstract void onUnload();
	public abstract String getID();
	public abstract String getVersion();
}
