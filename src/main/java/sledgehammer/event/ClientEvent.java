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

/**
 * PlayerEvent that handles Lua-Client communication with LuaNet.
 *
 * @author Jab
 */
public class ClientEvent extends PlayerEvent {

    /**
     * The String ID of the Event.
     */
    public static final String ID = "ClientCommandEvent";

    /**
     * The String client ID of the Module.
     */
    private String moduleName;
    /**
     * The String raw client ID of the Module.
     */
    private String moduleRaw;
    /**
     * The String command of the ClientEvent.
     */
    private String command;
    /**
     * Flag for whether or not the ClientEvent is a Sledgehammer 'Request'.
     */
    private boolean request = false;
    /**
     * The LuaTable Object passed as arguments.
     */
    private KahluaTable table;

    /**
     * Main constructor.
     *
     * @param player  The Player authoring the Event.
     * @param module  The String client ID of the Module.
     * @param command The String command of the ClientEvent.
     * @param table   The LuaTable Object passed as arguments.
     */
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

    @Override
    public String getLogMessage() {
        return null;
    }

    @Override
    public String getID() {
        return ID;
    }

    /**
     * Sends a LuaNet ServerCommand with the KahluaTable in the ClientEvent.
     */
    public void respond() {
        GameServer.sendServerCommand(getModuleRaw(), getCommand(), getTable(), getPlayer().getConnection());
    }

    /**
     * Sends a LuaNet ServerCommand with a given KahluaTable.
     *
     * @param table The KahluaTable to send.
     */
    public void respond(KahluaTable table) {
        GameServer.sendServerCommand(getModuleRaw(), getCommand(), table, getPlayer().getConnection());
    }

    /**
     * Sends a LuaNet ServerCommand with a given String command and a
     * KahluaTable.
     *
     * @param command The String command.
     * @param table   The KahluaTable to send.
     */
    public void respond(String command, KahluaTable table) {
        GameServer.sendServerCommand(getModuleRaw(), command, table, getPlayer().getConnection());
    }

    /**
     * Sends a LuaNet ServerCommand with a given LuaObject to export.
     *
     * @param luaTable The LuaTable to export as a KahluaTable.
     */
    public void respond(LuaTable luaTable) {
        GameServer.sendServerCommand(getModuleRaw(), getCommand(), luaTable.export(), getPlayer().getConnection());
    }

    /**
     * Sends a LuaNet ServerCommand with a given String command and a LuaObject
     * to export.
     *
     * @param command  The String command.
     * @param luaTable The LuaTable to export as a KahluaTable.
     */
    public void respond(String command, LuaTable luaTable) {
        GameServer.sendServerCommand(getModuleRaw(), command, luaTable.export(), getPlayer().getConnection());
    }

    /**
     * @return Returns true if the ClientEvent is a request and that the ClientEvent
     * needs to respond to the client.
     */
    public boolean isRequest() {
        return this.request;
    }

    /**
     * @return Returns the String client ID of the Module.
     */
    public String getModuleName() {
        return this.moduleName;
    }

    /**
     * @return Returns the raw String client ID of the Module.
     */
    private String getModuleRaw() {
        return moduleRaw;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the String client ID of the Module.
     *
     * @param moduleName The String client ID to set.
     */
    private void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    /**
     * @return Returns the String command of the ClientEvent.
     */
    public String getCommand() {
        return this.command;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the String command of the ClientEvent.
     *
     * @param command The String command to set.
     */
    private void setCommand(String command) {
        this.command = command;
    }

    /**
     * @return Returns the KahluaTable arguments passed with the ClientEvent.
     */
    public KahluaTable getTable() {
        return this.table;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the KahluaTable arguments passed with the ClientEvent.
     *
     * @param table The KahluaTable arguments to set.
     */
    private void setTable(KahluaTable table) {
        this.table = table;
    }
}