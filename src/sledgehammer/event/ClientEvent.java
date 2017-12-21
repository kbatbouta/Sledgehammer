/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 */
package sledgehammer.event;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.lua.LuaTable;
import sledgehammer.lua.core.Player;
import zombie.network.GameServer;

public class ClientEvent extends PlayerEvent {

	public static final String ID = "ClientCommandEvent";

	private String moduleName;
	private String moduleRaw;
	private String command;
	private boolean request = false;
	private KahluaTable table;

	public ClientEvent(Player player, String module, String command, KahluaTable table) {
		super(player);

		this.moduleRaw = module;

		if (moduleRaw.startsWith("request:")) {
			request = true;
		}

		if (moduleRaw.contains("sledgehammer.module.") || moduleRaw.startsWith("request:sledgehammer.module.")) {
			String[] split = module.split("sledgehammer.module.");
			setModuleName(split[1]);
		} else if (moduleRaw.startsWith("request:")) {
			setModuleName(moduleRaw.split("request:")[1]);
		} else {
			setModuleName(module);
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

	public String getModuleName() {
		return this.moduleName;
	}

	private String getModuleRaw() {
		return moduleRaw;
	}

	private void setModuleName(String moduleName) {
		this.moduleName = moduleName;
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

	@Override
	public String getID() {
		return ID;
	}
}