package sledgehammer.event;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.object.LuaTable;
import sledgehammer.objects.Player;
import zombie.core.raknet.UdpConnection;
import zombie.network.GameServer;

public class ClientEvent extends PlayerEvent {

	public static final String ID = "ClientCommandEvent";
	
	private String module;
	private String moduleRaw;
	private String command;
	private boolean request = false;
	private KahluaTable table;
	
	public ClientEvent(Player player, String module, String command, KahluaTable table) {
		super(player);
		
		this.moduleRaw = module;
		
		if(moduleRaw.startsWith("request:")) {
			request = true;
		}
		
		if(moduleRaw.contains("sledgehammer.module.") || moduleRaw.startsWith("request:sledgehammer.module.")) {
			String[] split = module.split("sledgehammer.module.");
			setModule(split[1]);
		} else if (moduleRaw.startsWith("request:")) {
			setModule(moduleRaw.split("request:")[1]);
		} else {			
			setModule(module);
		}
		setCommand(command);
		setTable(table);
	}
	
	public void respond() {
		GameServer.sendServerCommand(getModuleRaw(), getCommand(), getTable(), getPlayer().getConnection());
	}

	public void respond(KahluaTable table) {
		GameServer.sendServerCommand(getModuleRaw(), getCommand(), table, getPlayer().getConnection());
	}
	
	public void respond(String command, KahluaTable table) {
		GameServer.sendServerCommand(getModuleRaw(), command, table, getPlayer().getConnection());
	}

	public void respond(LuaTable obj) {
		GameServer.sendServerCommand(getModuleRaw(), getCommand(), obj.export(), getPlayer().getConnection());
	}
	
	public void respond(String command, LuaTable obj) {
		GameServer.sendServerCommand(getModuleRaw(), command, obj.export(), getPlayer().getConnection());
	}
	
	public boolean isRequest() {
		return this.request;
	}
	
	public String getModule() {
		return this.module;
	}
	
	private String getModuleRaw() {
		return moduleRaw;
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
