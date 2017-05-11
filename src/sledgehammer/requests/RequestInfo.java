package sledgehammer.requests;

import java.util.Map;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.object.LuaTable;
import sledgehammer.objects.Player;

public class RequestInfo extends LuaTable {

	private int playerID = -1;
	private Player self;
	
	public RequestInfo() {
		super("requestInfo");
	}
	
	public void setPlayerID(int id) {
		if (this.playerID != id) {
			this.playerID = id;
			set("playerID", this.playerID);
		}
	}

	public void construct(Map<Object, Object> definitions) {
		definitions.put("self", getSelf());
	}

	public void onLoad(KahluaTable table) {
		// Server authored only.
	}

	public Player getSelf() {
		return this.self;
	}
	
	public void setSelf(Player player) {
		this.self = player;
		set("self", getSelf());
	}

	@Override
	public void onExport() {
		set("self", getSelf());
	}

}
