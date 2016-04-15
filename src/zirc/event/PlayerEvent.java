package zirc.event;

import zirc.wrapper.Player;

public abstract class PlayerEvent extends Event {

	public static final String ID = "PlayerEvent";
	
	private Player player;
	
	public PlayerEvent(Player player) {
		super();
		if(player == null) throw new IllegalArgumentException("Player is null!");
		this.player = player;
	}
	
	public Player getPlayer() {
		return this.player;
	}

}
