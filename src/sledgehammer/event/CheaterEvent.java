package sledgehammer.event;

import sledgehammer.wrapper.Player;

public class CheaterEvent extends PlayerEvent {
	
	public static final String ID = "CheaterEvent";
	private String logMessage = null;
	
	public CheaterEvent(Player player, String logMessage) {
		super(player);
		this.logMessage = logMessage;
	}

	@Override
	public String getLogMessage() {
		return logMessage;
	}

	@Override
	public String getID() {return ID;}
}
