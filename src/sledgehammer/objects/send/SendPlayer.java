package sledgehammer.objects.send;

import sledgehammer.lua.core.Player;

public class SendPlayer extends Send {

	private Player player;
	
	public SendPlayer() {
		super("core", "sendPlayer");
	}
	
	public Player getPlayer() {
		return this.player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	@Override
	public void onExport() {
		set("player", getPlayer());
	}

}
