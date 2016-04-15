package zirc.module;

import java.util.HashMap;
import java.util.Map;

import zirc.ZIRC;
import zirc.event.Event;
import zirc.interfaces.CommandListener;
import zirc.interfaces.EventListener;
import zirc.interfaces.LogListener;

public abstract class Module {
	
	private boolean loaded = false;
	private boolean started = false;
	
	private String jarName = null;
	private Map<String, String> pluginSettings = new HashMap<>();
	
	public Module() {
		
	}
	
	public void println(Object... messages) {
		for (Object message : messages) System.out.println("MODULE (" + getModuleName() + "): " + message.toString());
		if(messages.length == 0) System.out.println("MODULE (" + getModuleName() + "): \n");
	}
	
	public void register(String type, EventListener listener) {
		ZIRC.instance.registerEventListener(type, listener);
	}
	
	public void register(String command, CommandListener listener) {
		ZIRC.instance.registerCommandListener(command, listener);
	}
	
	public void register(EventListener listener) {
		String[] types = listener.getTypes();
		if(types == null) {
			throw new IllegalArgumentException("EventListener getTypes() array is null!");
		}
		for(String type : types) {			
			ZIRC.instance.registerEventListener(type, listener);
		}
	}
	
	public void unload() {
		started = false;
		ZIRC.instance.unloadModule(this);
	}
	
	public void register(LogListener listener) {
		ZIRC.instance.registerLogListener(listener);
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
			ZIRC.instance.registerCommandListener(command, listener);			
		}
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
	
	public void updateModule(long delta) {
		if(started) onUpdate(delta);
	}

	public void startModule() {
		if(!started) {			
			started = true;
			onStart();
		} else {
			println("Module is already started.");
		}
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
		ZIRC.instance.handleEvent(event, shouldLog);
	}
	
	public void handleEvent(Event event) {
		ZIRC.instance.handleEvent(event);
	}
	
	public abstract void onLoad();
	public abstract void onStart();
	public abstract void onUpdate(long delta);
	public abstract void onStop();
	public abstract void onUnload();
	public abstract String getModuleName();
	public abstract String getVersion();
}
