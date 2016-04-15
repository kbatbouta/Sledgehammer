package zirc.event;

public abstract class Event {
	
	public static final String ID = "Event";
	
	private boolean handled = false;
	private boolean announce = false;
	long timeStamp = 0L;
	private boolean canceled = false;
	
	public Event() {
		timeStamp = System.currentTimeMillis();
	}
	
//	public Type getType() {
//		return this.type;
//	}
//
//	public enum Type {
//		EVENT_PLAYER_CONNECT,
//		EVENT_PLAYER_DISCONNECT,
//		EVENT_PLAYER_DEATH,
//		EVENT_PLAYER_GRANTED_ADMIN,
//		EVENT_PLAYER_REMOVED_ADMIN,
//		EVENT_PLAYER_BANNED,
//		EVENT_PLAYER_KICKED,
//		EVENT_PLAYER_PVP_TOGGLE,
//		EVENT_PLAYER_PVP_ATTACK,
//		EVENT_PLAYER_PVP_KILL,
//		
//		EVENT_CHAT,
//		EVENT_COMMAND,
//		
//		EVENT_LOG,
//		EVENT_SERVER,
//		EVENT_MISC
//	}
	
	public abstract String getLogMessage();
	
	public void setHandled(boolean handled) {
		this.handled = handled;
	}
	
	public boolean handled() {
		return this.handled ;
	}
	
	public void announce(boolean announce) {
		this.announce = true;
	}
	
	public boolean shouldAnnounce() {
		return this.announce;
	}
	
	public long getTimeStamp() {
		return this.timeStamp;
	}
	
	public boolean canceled() {
		return this.canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled  = canceled;
	}
	
	public abstract String getName();
	

}
