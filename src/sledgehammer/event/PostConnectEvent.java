package sledgehammer.event;

import sledgehammer.objects.Player;

public class PostConnectEvent extends PlayerEvent {

	public static final String ID = "PostConnectionEvent";
	
	public PostConnectEvent(Player player) {
		super(player);
	}

	@Override
	public String getLogMessage() {
		// TODO: LogEvent Message.
		return null;
	}

	@Override
	public String getID() {return ID; }

}
