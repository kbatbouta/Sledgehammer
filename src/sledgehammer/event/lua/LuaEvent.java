package sledgehammer.event.lua;

import sledgehammer.event.Event;

public class LuaEvent extends Event {

	public static final String ID = "LuaEvent";
	
	public LuaEvent() {
		super();
	}

	
	public String getLogMessage() { return null;}
	public String getID()       { return ID; }

}
