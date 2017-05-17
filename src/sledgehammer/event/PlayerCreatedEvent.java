package sledgehammer.event;

import sledgehammer.objects.Player;

public class PlayerCreatedEvent extends PlayerEvent {

	public static final String ID = "PlayerCreatedEvent";
	
	public PlayerCreatedEvent(Player player) {
		super(player);
	}

	public String getLogMessage() { return null; }

	public String getID() { return ID; }

}
