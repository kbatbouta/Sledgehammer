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

package sledgehammer.module.chat;

import sledgehammer.annotations.CommandHandler;
import sledgehammer.enums.Result;
import sledgehammer.interfaces.Listener;
import sledgehammer.language.Language;
import sledgehammer.language.LanguagePackage;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.core.Player;
import sledgehammer.util.Command;
import sledgehammer.util.Response;

public class ChatCommandListener implements Listener {

    private static final String permissionNodeEspanol = "core.chat.channel.espanol";

    private ModuleChat module;

    ChatCommandListener(ModuleChat module) {
        setModule(module);
    }

    @CommandHandler(command = "espanol", permission = permissionNodeEspanol,
            defaultPermission = true)
    public void onCommandEspanol(Command c, Response r) {
        Player commander = c.getPlayer();
        ChatChannel channel = module.getChatChannel("Espanol");
        if (commander.hasPermission(permissionNodeEspanol, true)) {
            commander.setPermission(permissionNodeEspanol, null);
            channel.removePlayer(commander, true);
            r.set(Result.SUCCESS, "You have been removed from the Espanol channel.");
        } else {
            commander.setPermission(permissionNodeEspanol, true);
            channel.addPlayer(commander, true);
            r.set(Result.SUCCESS, "You are now added to the Espanol channel.");
        }
    }

    public LanguagePackage getLanguagePackage() {
        return getModule().getLanguagePackage();
    }

    public ModuleChat getModule() {
        return this.module;
    }

    private void setModule(ModuleChat module) {
        this.module = module;
    }
}
