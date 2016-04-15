package zirc.event;

import zirc.wrapper.Player;

public class PVPEvent extends PlayerEvent {
	
	public static final String ID = "PVPEvent";
	
	private boolean enabled;
	public PVPEvent(Player player, boolean enabled) {
		super(player);
		this.enabled = enabled;
	}

	public boolean isPVPEnabled() {
		return this.enabled;
	}
	
	public String getLogMessage() {
		return getPlayer().getUsername() + " " + (isPVPEnabled()?"enabled":"disabled") + " PVP.";
	}

	public String getName() {
		return ID;
	}
}
