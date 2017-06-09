package sledgehammer.event;

import sledgehammer.objects.Player;

public class RequestChannelsEvent extends PlayerEvent {

	public static final String ID = "RequestChannelsEvent";
	
	public RequestChannelsEvent(Player player) {
		super(player);
	}

	public String getLogMessage() { return null; }
	public String getID() { return ID; }
}
