package sledgehammer.event;

import sledgehammer.objects.Player;

public class AliveEvent extends PlayerEvent {

	public static final String ID = "AliveEvent";
	
	public AliveEvent(Player player) {
		super(player);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getLogMessage() {
		return null;
	}

	@Override public String getID() { return ID; }
}
