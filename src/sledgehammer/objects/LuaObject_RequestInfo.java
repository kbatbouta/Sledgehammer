package sledgehammer.objects;

import java.util.Map;

import se.krka.kahlua.vm.KahluaTable;

public class LuaObject_RequestInfo extends LuaObject {

	private int playerID = -1;
	
	public LuaObject_RequestInfo() {
		super("requestInfo");
	}
	
	public void setPlayerID(int id) {
		if (this.playerID != id) {
			this.playerID = id;
			set("playerID", this.playerID);
		}
	}

	@Override
	public void construct(Map<String, Object> definitions) {
		definitions.put("playerID", playerID);
	}

	@Override
	public void load(KahluaTable table) {
		playerID = Integer.parseInt(table.rawget("playerID").toString());
	}

}
