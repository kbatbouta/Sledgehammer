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

package sledgehammer.module.discord;

import sledgehammer.enums.Result;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.lua.core.Player;
import sledgehammer.util.Command;
import sledgehammer.util.Response;

public class DiscordCommandListener implements CommandListener {

    private ModuleDiscord module;

    DiscordCommandListener(ModuleDiscord instance) {
        module = instance;
    }

    public String[] getCommands() {
        return new String[]{"discord"};
    }

    public void onCommand(Command c, Response r) {
        String command = c.getCommand();
        String[] args = c.getArguments();
        Player player = c.getPlayer();

        if (command.equalsIgnoreCase("discord")) {
            if (player.hasPermission(getPermissionNode("discord"))) {
                if (args.length == 1) {
                    command = args[0];
                    if (command.equalsIgnoreCase("start")) {
                        module.start();
                        r.set(Result.SUCCESS, "Starting the Discord bot.");
                    } else if (command.equalsIgnoreCase("stop")) {
                        module.stop();
                        r.set(Result.SUCCESS, "Stopping the Discord bot.");
                    } else {
                        r.set(Result.FAILURE, onTooltip(player, c));
                    }
                } else {
                    r.set(Result.FAILURE, onTooltip(player, c));
                }
            } else {
                r.set(Result.FAILURE, module.getPermissionDeniedMessage());
            }
        }
    }

    public String onTooltip(Player player, Command c) {
        String command = c.getCommand();
        if (command.equalsIgnoreCase("discord")) {
            if (player.hasPermission(getPermissionNode("discord"))) {
                return "Manages the discord bot. EX: '/discord start' '/discord stop'";
            } else {
                return module.getPermissionDeniedMessage();
            }
        }
        return null;
    }

    public String getPermissionNode(String command) {
        if (command.equalsIgnoreCase("discord")) {
            return "sledgehammer.discord";
        }
        return null;
    }
}