package sledgehammer.event;

import sledgehammer.objects.Player;

public class LogEvent extends Event {
	
	public static final String ID = "LogEvent";
	
	private LogType type;
	private Player player;
	private Event event;
	private String message;
	private boolean importance;
	
	public LogEvent(Event event) {
		super();
		setEvent(event);
		this.message = event.getLogMessage();
		this.importance = false;
	}
	
	public LogType getLogType() {
		return this.type;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public Event getEvent() {
		return this.event;
	}
	
	public void setEvent(Event event) {
		this.event = event;
	}
	
//	public static enum LogType {
//		PLAYER,
//		COMMAND,
//		SERVER,
//		CHAT,
//		ITEM
//	}
	
	public static enum LogType {
		INFO,
		WARN,
		ERROR,
		CHEAT,
		STAFF
	}

	public boolean isImportant() {
		return this.importance;
	}
	
	public void setImportant(boolean flag) {
		this.importance = flag;
	}

	@Override
	public String getLogMessage() {
		return message;
	}

	@Override
	public String getID() {
		return ID;
	}
}
