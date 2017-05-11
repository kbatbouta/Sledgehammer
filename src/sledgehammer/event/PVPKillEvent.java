package sledgehammer.event;

import sledgehammer.objects.Player;

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
		String playerKillerName = "Unknown Player (Null)";
		String playerKilledName = "Unknown Player (Null)";
		if(playerKiller != null) playerKillerName = playerKiller.getUsername();
		if(playerKilled != null) playerKilledName = playerKilled.getUsername();
		return playerKillerName + " killed " + playerKilledName + '.';
	}

	@Override
	public String getID() {
		return ID;
	}
}
