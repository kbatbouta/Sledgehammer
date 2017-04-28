package sledgehammer.objects;

import java.util.Map;

import se.krka.kahlua.vm.KahluaTable;

public class LuaObject_ChatMessagePlayer extends LuaObject_ChatMessage {
	
	private int playerID;
	
	public LuaObject_ChatMessagePlayer(KahluaTable table) {
		super(table);
		setObjectName("ChatMessagePlayer");
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
	public void construct(Map<String, Object> definitions) {
		// Load superclass definitions first.
		super.construct(definitions);
		
		definitions.put("playerID", getPlayerID());
	}

	@Override
	public void load(KahluaTable table) {
		
		// Load superclass table-data first.
		super.load(table);
		
		setPlayerID(Integer.parseInt(table.rawget("playerID").toString()));
	}
}
