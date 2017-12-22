package sledgehammer.lua.core;

import sledgehammer.lua.Send;

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
