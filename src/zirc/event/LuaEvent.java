package zirc.event;

public class LuaEvent extends Event {

	public static final String ID = "LuaEvent";
	
	public LuaEvent() {
		super();
	}

	
	public String getLogMessage() { return null;}
	public String getName()       { return ID; }

}
