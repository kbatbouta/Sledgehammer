package sledgehammer.event.lua;

import sledgehammer.event.Event;

public class ScriptEvent extends Event {

	public static final String ID = "ScriptEvent";
	
	private String context = null;
	
	public ScriptEvent(String context) {
		super();
		this.context = context;
	}

	public String getContext() {
		return context;
	}
	
	public String getLogMessage() { return null;}
	public String getID()       { return ID; }

}
