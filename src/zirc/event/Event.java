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
	
	public abstract String getID();
	

}
