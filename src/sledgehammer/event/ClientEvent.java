package sledgehammer.event;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.wrapper.Player;

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
