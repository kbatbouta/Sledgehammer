package sledgehammer.objects.chat;

import se.krka.kahlua.vm.KahluaTable;

public class ChatMessagePlayer extends ChatMessage {
	
	private int playerID;
	
	public ChatMessagePlayer(KahluaTable table) {
		super(table);
		setName("ChatMessagePlayer");
	}
	
	public int getPlayerID() {
		return this.playerID;
	}
	
	public void setPlayerID(int playerID) {
		if(this.playerID != playerID) {
			this.playerID = playerID;
			set("playerID", playerID);			
		}
	}
	
	@Override
	public void onLoad(KahluaTable table) {
		
		// Load superclass table-data first.
		super.onLoad(table);
		
		setPlayerID(Integer.parseInt(table.rawget("playerID").toString()));
	}
}
