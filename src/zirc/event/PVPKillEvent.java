package zirc.event;

import zirc.wrapper.Player;

public class PVPKillEvent extends Event {

	public static final String ID = "PVPKillEvent";
	
	private Player playerKiller;
	private Player playerKilled;
	
	public PVPKillEvent(Player killer, Player killed) {
		super();
		this.playerKiller = killer;
		this.playerKilled = killed;
	}
	
	public Player getKiller() {
		return this.playerKiller;
	}
	
	public Player getKilled() {
		return this.playerKilled;
	}

	@Override
	public String getLogMessage() {
		return playerKiller.getUsername() + " killed " + playerKilled.getUsername() + '.';
	}

	@Override
	public String getID() {
		return ID;
	}
}
