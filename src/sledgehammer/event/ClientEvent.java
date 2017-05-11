package sledgehammer.event;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.object.LuaTable;
import sledgehammer.objects.Player;
import zombie.network.GameServer;

public class ClientEvent extends PlayerEvent {

	public static final String ID = "ClientCommandEvent";
	
	private String module;
	private String command;
	private KahluaTable table;
	
	public ClientEvent(Player player, String module, String command, KahluaTable table) {
		super(player);
		setModule(module);
		setCommand(command);
		setTable(table);
	}
	
	public void respond() {
		GameServer.sendServerCommand(getModule(), getCommand(), getTable(), getPlayer().getConnection());
	}
	
	
	public void respond(KahluaTable table) {
		GameServer.sendServerCommand(getModule(), getCommand(), table, getPlayer().getConnection());
	}
	
	public void respond(String command, KahluaTable table) {
		GameServer.sendServerCommand(getModule(), command, table, getPlayer().getConnection());
	}

	public void respond(LuaTable obj) {
		GameServer.sendServerCommand(getModule(), getCommand(), obj.export(), getPlayer().getConnection());
	}
	
	public void respond(String command, LuaTable obj) {
		GameServer.sendServerCommand(getModule(), command, obj.export(), getPlayer().getConnection());
	}
	
	public String getModule() {
		return this.module;
	}
	
	private void setModule(String module) {
		this.module = module;
	}
	
	public String getCommand() {
		return this.command;
	}

	private void setCommand(String command) {
		this.command = command;
	}
	
	public KahluaTable getTable() {
		return this.table;
	}
	
	private void setTable(KahluaTable table) {
		this.table = table;
	}

	@Override
	public String getLogMessage() {
		return null;
	}

	@Override public String getID() { return ID; }

}
