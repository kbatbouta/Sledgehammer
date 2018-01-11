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