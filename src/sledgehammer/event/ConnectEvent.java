package sledgehammer.event;

import sledgehammer.wrapper.Player;

public class ConnectEvent extends PlayerEvent {

	public static final String ID = "ConnectEvent";
	
	public ConnectEvent(Player player) {
		super(player);
	}
	
	@Override
	public String getLogMessage() {
		return getPlayer().getUsername() + " connected.";
	}

	@Override
	public String getID() {
		return ID;
	}

}
