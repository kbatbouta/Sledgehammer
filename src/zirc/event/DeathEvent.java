package zirc.event;

import zirc.wrapper.Player;

public class DeathEvent extends PlayerEvent {

	public static final String ID = "DeathEvent";
	
	public DeathEvent(Player player) {
		super(player);
	}
	
	@Override
	public String getLogMessage() {
		return getPlayer().getUsername() + " has died.";
	}

	@Override
	public String getName() {
		return ID;
	}

}
