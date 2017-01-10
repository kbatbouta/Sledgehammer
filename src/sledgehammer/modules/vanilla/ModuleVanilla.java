package sledgehammer.modules.vanilla;

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

import java.util.HashMap;
import java.util.Map;

import sledgehammer.interfaces.CommandListener;
import sledgehammer.interfaces.LogListener;
import sledgehammer.module.Module;
import zombie.sledgehammer.modules.vanilla.VanillaCommandListener;

public class ModuleVanilla extends Module {

	public static final String ID      = "sledgehammer_vanilla_core";
	public static final String NAME    = "Basic"                    ;
	public static final String VERSION = "1.02"                     ;
	
	CommandListener commandListener;
	
	LogListener logListener;
	
	private Map<String, String> mapContexts;
	
	public ModuleVanilla() {
		
		// Adding contexts to this class for exposure & modification purposes. (This is not in vanilla).
		// Otherwise, this would be defined in the CommandHandler.
		mapContexts = new HashMap<>();
		mapContexts.put("roll"                   , "base.command.player.rolldice"              );
		mapContexts.put("changepwd"              , "base.command.player.changepwd"             );
		mapContexts.put("card"                   , "base.command.player.card"                  );
		mapContexts.put("addalltowhitelist"      , "base.command.admin.addalltowhitelist"      );
		mapContexts.put("additem"                , "base.command.admin.additem"                );
		mapContexts.put("adduser"                , "base.command.admin.adduser"                );
		mapContexts.put("addusertowhitelist"     , "base.command.admin.addusertowhitelist"     );
		mapContexts.put("addxp"                  , "base.command.admin.addxp"                  );
		mapContexts.put("alarm"                  , "base.command.admin.alarm"                  );
		mapContexts.put("changeoption"           , "base.command.admin.changeoption"           );
		mapContexts.put("chopper"                , "base.command.admin.chopper"                );
		mapContexts.put("createhorde"            , "base.command.admin.createhorde"            );
		mapContexts.put("disconnect"             , "base.command.admin.disconnect"             );
		mapContexts.put("godmod"                 , "base.command.admin.godmod"                 );
		mapContexts.put("grantadmin"             , "base.command.admin.grantadmin"             );
		mapContexts.put("gunshot"                , "base.command.admin.gunshot"                );
		mapContexts.put("invisible"              , "base.command.admin.invisible"              );
		mapContexts.put("kickuser"               , "base.command.admin.kickuser"               );
		mapContexts.put("noclip"                 , "base.command.admin.noclip"                 );
		mapContexts.put("players"                , "base.command.admin.players"                );
		mapContexts.put("quit"                   , "base.command.admin.quit"                   );
		mapContexts.put("reloadlua"              , "base.command.admin.reloadlua"              );
		mapContexts.put("reload"                 , "base.command.admin.reload"                 );
		mapContexts.put("reloadoptions"          , "base.command.admin.reloadoptions"          );
		mapContexts.put("removeadmin"            , "base.command.admin.removeadmin"            );
		mapContexts.put("removeuserfromwhitelist", "base.command.admin.removeuserfromwhitelist");
		mapContexts.put("save"                   , "base.command.admin.save"                   );
		mapContexts.put("sendpulse"              , "base.command.admin.sendpulse"              );
		mapContexts.put("showoptions"            , "base.command.admin.showoptions"            );
		mapContexts.put("startrain"              , "base.command.admin.startrain"              );
		mapContexts.put("stoprain"               , "base.command.admin.stoprain"               );
		mapContexts.put("teleport"               , "base.command.admin.teleport"               );
		mapContexts.put("thunder"                , "base.command.admin.thunder"                );
		mapContexts.put("banuser"                , "base.command.admin.banuser"                );
		mapContexts.put("unbanuser"              , "base.command.admin.unbanuser"              );
		mapContexts.put("banid"                  , "base.command.admin.banid"                  );
		mapContexts.put("unbanid"                , "base.command.admin.unbanid"                );
	}
	
	public void onLoad() {
		commandListener = new VanillaCommandListener(this);
		logListener = new VanillaLogListener();
		
		register(logListener);
	}
	
	public void onUnload() {
		unregister(logListener);
	}
	
	public CommandListener getCommandListener() {
		return commandListener;
	}
	
	public void setCommandListener(CommandListener listener) {
		this.commandListener = listener;
	}
		
	public Map<String, String> getContexts() {
		return this.mapContexts;
	}

	public void onStart() {}
	public void onUpdate(long delta) {}
	public void onStop() {}
	public String getID()      { return ID      ; }
	public String getName()    { return NAME    ; }
	public String getVersion() { return VERSION ; }
}
