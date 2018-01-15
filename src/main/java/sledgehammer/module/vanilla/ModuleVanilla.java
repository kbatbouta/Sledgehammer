/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

package sledgehammer.module.vanilla;

import java.util.HashMap;
import java.util.Map;

import sledgehammer.interfaces.LogEventListener;
import sledgehammer.plugin.Module;
import zombie.sledgehammer.modules.vanilla.VanillaCommandListener;

/**
 * Module to handle Vanilla data and operations for the Core Plug-in.
 *
 * @author Jab
 */
public class ModuleVanilla extends Module {

    /**
     * The Map of String permission-nodes identified by the String commands
     * they represent.
     */
    private Map<String, String> mapPermissionNodes;
    /** The CommandListener instance for the Module. */
    private VanillaCommandListener commandListener;
    private LogEventListener logListener;

    public ModuleVanilla() {
        // Adding contexts to this class for exposure & modification purposes. (This is
        // not in vanilla) Otherwise, this would be defined in the CommandHandler.
        // @formatter:off
		mapPermissionNodes = new HashMap<>();
		mapPermissionNodes.put("roll"                   , "base.command.player.rolldice"              );
		mapPermissionNodes.put("changepwd"              , "base.command.player.changepwd"             );
		mapPermissionNodes.put("card"                   , "base.command.player.card"                  );
		mapPermissionNodes.put("addalltowhitelist"      , "base.command.admin.addalltowhitelist"      );
		mapPermissionNodes.put("additem"                , "base.command.admin.additem"                );
		mapPermissionNodes.put("adduser"                , "base.command.admin.adduser"                );
		mapPermissionNodes.put("addusertowhitelist"     , "base.command.admin.addusertowhitelist"     );
		mapPermissionNodes.put("addxp"                  , "base.command.admin.addxp"                  );
		mapPermissionNodes.put("alarm"                  , "base.command.admin.alarm"                  );
		mapPermissionNodes.put("changeoption"           , "base.command.admin.changeoption"           );
		mapPermissionNodes.put("chopper"                , "base.command.admin.chopper"                );
		mapPermissionNodes.put("createhorde"            , "base.command.admin.createhorde"            );
		mapPermissionNodes.put("disconnect"             , "base.command.admin.disconnect"             );
		mapPermissionNodes.put("godmod"                 , "base.command.admin.godmod"                 );
		mapPermissionNodes.put("grantadmin"             , "base.command.admin.grantadmin"             );
		mapPermissionNodes.put("gunshot"                , "base.command.admin.gunshot"                );
		mapPermissionNodes.put("invisible"              , "base.command.admin.invisible"              );
		mapPermissionNodes.put("kickuser"               , "base.command.admin.kickuser"               );
		mapPermissionNodes.put("noclip"                 , "base.command.admin.noclip"                 );
		mapPermissionNodes.put("players"                , "base.command.admin.players"                );
		mapPermissionNodes.put("quit"                   , "base.command.admin.quit"                   );
		mapPermissionNodes.put("reloadlua"              , "base.command.admin.reloadlua"              );
		mapPermissionNodes.put("reload"                 , "base.command.admin.reload"                 );
		mapPermissionNodes.put("reloadoptions"          , "base.command.admin.reloadoptions"          );
		mapPermissionNodes.put("removeadmin"            , "base.command.admin.removeadmin"            );
		mapPermissionNodes.put("removeuserfromwhitelist", "base.command.admin.removeuserfromwhitelist");
		mapPermissionNodes.put("save"                   , "base.command.admin.save"                   );
		mapPermissionNodes.put("sendpulse"              , "base.command.admin.sendpulse"              );
		mapPermissionNodes.put("showoptions"            , "base.command.admin.showoptions"            );
		mapPermissionNodes.put("startrain"              , "base.command.admin.startrain"              );
		mapPermissionNodes.put("stoprain"               , "base.command.admin.stoprain"               );
		mapPermissionNodes.put("teleport"               , "base.command.admin.teleport"               );
		mapPermissionNodes.put("thunder"                , "base.command.admin.thunder"                );
		mapPermissionNodes.put("banuser"                , "base.command.admin.banuser"                );
		mapPermissionNodes.put("unbanuser"              , "base.command.admin.unbanuser"              );
		mapPermissionNodes.put("banid"                  , "base.command.admin.banid"                  );
		mapPermissionNodes.put("unbanid"                , "base.command.admin.unbanid"                );
		// @formatter:on
    }

    @Override
    public void onLoad() {
        commandListener = new VanillaCommandListener(this);
        logListener = new VanillaLogListener();
        register(logListener);
    }

    @Override
    public void onUnload() {
        unregister(logListener);
    }

    /**
     * @return Returns the VanillaCommandListener for the Vanilla Module.
     */
    public VanillaCommandListener getCommandListener() {
        return commandListener;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the VanillaCommandListener for the Vanilla Module.
     *
     * @param listener The VanillaCommandListener to set.
     */
    public void setCommandListener(VanillaCommandListener listener) {
        this.commandListener = listener;
    }

    /**
     * @return Returns the Map of String permission-nodes identified by the
     * String commands they represent.
     */
    public Map<String, String> getCommandPermissionNodes() {
        return this.mapPermissionNodes;
    }
}