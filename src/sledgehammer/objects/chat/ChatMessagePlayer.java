package sledgehammer.objects.chat;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.modules.core.ModuleChat;
import sledgehammer.objects.Player;

public class ChatMessagePlayer extends ChatMessage {

	private Player player;
	private String origin = "in-game";
	
	public ChatMessagePlayer(KahluaTable table) {
		super(table);
		setName("ChatMessagePlayer");
	}
	
	public ChatMessagePlayer(KahluaTable table, long id) {
		super(table);
		setID(id);
	}

	public ChatMessagePlayer(long messageID, String channel, String message, String messageOriginal,
			boolean edited, int editorID, boolean deleted, int deleterID, long modifiedTimestamp, String time, int playerID, String playerName) {
		super(messageID, channel, message, messageOriginal, edited, editorID, deleted, deleterID, modifiedTimestamp, time);
		setPlayer(SledgeHammer.instance.getPlayer(playerID));
		
		// Player expired or unregistered.
		if(getPlayer() == null) {
			Player player = new Player(playerName);
			setPlayer(player);
		}
	}

	public Player getPlayer() {
		return this.player;
	}
	
	public void setPlayer(Player player) {
		if(this.player != player) {
			this.player = player;
		}
	}
	
	@Override
	public void onExport() {
		super.onExport();
		set("player", player);
	}
	
	@Override
	public void onLoad(KahluaTable table) {
		
		// Load superclass table-data first.
		super.onLoad(table);
		
		int playerID = (new Double(table.rawget("playerID").toString()).intValue());
		Player player = SledgeHammer.instance.getPlayer(playerID);
		setPlayer(player);
	}
}
