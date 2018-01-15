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

package sledgehammer.module.faction;

import java.util.*;

import sledgehammer.enums.Result;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.language.Language;
import sledgehammer.language.LanguagePackage;
import sledgehammer.lua.core.Player;
import sledgehammer.util.Command;
import sledgehammer.util.Printable;
import sledgehammer.util.Response;

/**
 * Class designed to handle commands from players for the Factions Module.
 * <p>
 *
 * @author Jab
 */
public class FactionsCommandListener extends Printable implements CommandListener {

    /**
     * Module instance using this Command Handler.
     */
    private ModuleFactions module;

    private Map<String, String> mapContexts;

    public FactionsCommandListener(ModuleFactions module) {
        setModule(module);
        LanguagePackage lang = module.getLanguagePackage();
        // @formatter:off
		mapContexts = new HashMap<>();
		mapContexts.put("faction"             , "sledgehammer.factions"             );
		mapContexts.put("faction create"      , "sledgehammer.factions.create"      );
		mapContexts.put("faction disband"     , "sledgehammer.factions.disband"     );
		mapContexts.put("faction join"        , "sledgehammer.factions.join"        );
		mapContexts.put("faction leave"       , "sledgehammer.factions.leave"       );
		mapContexts.put("faction invite"      , "sledgehammer.factions.invite"      );
		mapContexts.put("faction accept"      , "sledgehammer.factions.accept"      );
		mapContexts.put("faction reject"      , "sledgehammer.factions.reject"      );
		mapContexts.put("faction kick"        , "sledgehammer.factions.kick"        );
		mapContexts.put("faction set"         , "sledgehammer.factions.set"         );
		mapContexts.put("faction set color"   , "sledgehammer.factions.set.color"   );
		mapContexts.put("faction set name"    , "sledgehammer.factions.set.name"    );
		mapContexts.put("faction set password", "sledgehammer.factions.set.password");
		mapContexts.put("faction set tag"     , "sledgehammer.factions.set.tag"     );
		// Add all commands to the default permissions list.
		module.addDefaultPermission(mapContexts.get("faction"             ));
		module.addDefaultPermission(mapContexts.get("faction create"      ));
		module.addDefaultPermission(mapContexts.get("faction disband"     ));
		module.addDefaultPermission(mapContexts.get("faction join"        ));
		module.addDefaultPermission(mapContexts.get("faction leave"       ));
		module.addDefaultPermission(mapContexts.get("faction invite"      ));
		module.addDefaultPermission(mapContexts.get("faction accept"      ));
		module.addDefaultPermission(mapContexts.get("faction reject"      ));
		module.addDefaultPermission(mapContexts.get("faction kick"        ));
		module.addDefaultPermission(mapContexts.get("faction set"         ));
		module.addDefaultPermission(mapContexts.get("faction set tag"     ));
		module.addDefaultPermission(mapContexts.get("faction set name"    ));
		module.addDefaultPermission(mapContexts.get("faction set color"   ));
		module.addDefaultPermission(mapContexts.get("faction set password"));
		// @formatter:on
    }

    public String[] getCommands() {
        return new String[]{"faction"};
    }

    public String getPermissionNode(String command) {
        if (command == null || command.isEmpty()) {
            return null;
        }
        command = command.toLowerCase().trim();
        return mapContexts.get(command);
    }

    @Override
    public String onTooltip(Player commander, Command c) {
        String command = c.getCommand().toLowerCase();
        if (command.equals("faction") && commander.hasPermission(getPermissionNode("faction") + ".*")) {
            Response r = new Response();
            processHelpMessage(commander, r);
            return r.getResponse();
        }
        return null;
    }

    public void onCommand(Command c, Response r) {
        ModuleFactions module = getModule();
        LanguagePackage lang = module.getLanguagePackage();
        Result result = Result.FAILURE;
        Player commander = c.getPlayer();
        UUID playerId = commander.getUniqueId();
        Language language = commander.getLanguage();
        String response = null;
        String command = c.getCommand().toLowerCase();
        String[] args = c.getArguments();
        // /faction
        if (command.equals("faction")) {
            if (args.length > 0) {
                command = args[0].toLowerCase();
                args = Command.getSubArgs(args, 1);
                // /faction create
                if (command.equals("create")) {
                    if (!commander.hasPermission(getPermissionNode("faction create"))) {
                        r.deny();
                        return;
                    }
                    if (args.length == 3) {
                        // @formatter:off
                        String name     = args[0];
                        String tag      = args[1];
                        String password = args[2];
                        // @formatter:on
                        r.set(module.commandCreateFaction(commander, name, tag, password));
                    } else {
                        r.set(Result.FAILURE, lang.getString("command_tooltip_faction_create", language));
                    }
                }
                // /faction disband
                else if (command.equals("disband")) {
                    if (!commander.hasPermission(getPermissionNode("faction disband"))) {
                        r.deny();
                        return;
                    }
                    if (args.length == 0) {
                        if (!commander.hasPermission(getPermissionNode("faction disband"))) {
                            r.deny();
                            return;
                        }
                        r.set(module.commandDisbandFaction(commander));
                    } else {
                        r.set(Result.FAILURE, lang.getString("command_tooltip_faction_disband", language));
                    }
                }
                // /faction join
                else if (command.equals("join")) {
                    if (!commander.hasPermission(getPermissionNode("faction join"))) {
                        r.deny();
                        return;
                    }
                    if (args.length == 2) {
                        String factionName = args[0];
                        String password = args[1];
                        r.set(module.commandJoinFaction(commander, factionName, password));
                    } else {
                        r.set(Result.FAILURE, lang.getString("command_tooltip_faction_join", language));
                    }
                }
                // /faction leave
                else if (command.equals("leave")) {
                    if (!commander.hasPermission(getPermissionNode("faction leave"))) {
                        r.deny();
                        return;
                    }
                    if (args.length == 0) {
                        r.set(module.commandLeaveFaction(commander));
                    } else {
                        r.set(Result.FAILURE, lang.getString("command_tooltip_faction_leave", language));
                    }
                }
                // /faction invite
                else if (command.equals("invite")) {
                    if (!commander.hasPermission(getPermissionNode("faction invite"))) {
                        r.deny();
                        return;
                    }
                    if (args.length == 1) {
                        // Grab the username argument.
                        String usernameInvited = args[0];
                        // Attempt to invite the Player.
                        r.set(module.commandInviteToFaction(commander, usernameInvited));
                    } else {
                        r.set(Result.FAILURE, lang.getString("command_tooltip_faction_invite", language));
                    }
                }
                // /faction accept
                else if (command.equals("accept")) {
                    if (!commander.hasPermission(getPermissionNode("faction accept"))) {
                        r.deny();
                        return;
                    }
                    if (args.length == 1) {
                        String factionName = args[0];
                        r.set(module.commandAcceptInvite(commander, factionName));
                    } else {
                        r.set(Result.FAILURE, lang.getString("command_tooltip_faction_accept", language));
                    }
                }
                // /faction reject
                else if (command.equals("reject")) {
                    if (!commander.hasPermission(getPermissionNode("faction reject"))) {
                        r.deny();
                        return;
                    }
                    if (args.length == 1) {
                        String factionName = args[0];
                        r.set(module.commandRejectInvites(commander, factionName));
                    } else {
                        r.set(Result.FAILURE, lang.getString("command_tooltip_faction_reject", language));
                    }
                }
                // /faction kick
                else if (command.equals("kick")) {
                    if (!commander.hasPermission(getPermissionNode("faction kick"))) {
                        r.deny();
                        return;
                    }
                    if (args.length >= 1) {
                        String usernameKicked = args[0];
                        String reason = args.length > 1 ? Command.combineArguments(args, 1) : "No reason.";
                        r.set(module.commandKickFromFaction(commander, usernameKicked, reason));
                    } else {
                        r.set(Result.FAILURE, lang.getString("command_tooltip_faction_kick", language));
                    }
                }
                // /faction set
                else if (command.equals("set")) {
                    if (!commander.hasPermission(getPermissionNode("faction set") + ".*")) {
                        r.deny();
                        return;
                    }
                    if (args.length > 0) {
                        command = args[0].toLowerCase();
                        args = Command.getSubArgs(args, 1);
                        // /faction set color
                        if (command.equals("color")) {
                            if (!commander.hasPermission(getPermissionNode("faction set color"))) {
                                r.deny();
                                return;
                            }
                            if (args.length == 1) {
                                String color = args[0];
                                r.set(module.commandSetFactionColor(commander, color));
                            } else {
                                r.set(Result.FAILURE, lang.getString("command_tooltip_faction_set_color", language));
                            }
                        }
                        // /faction set name
                        else if (command.equals("name")) {
                            if (!commander.hasPermission(getPermissionNode("faction set name"))) {
                                r.deny();
                                return;
                            }
                            if (args.length >= 1) {
                                String factionName = Command.combineArguments(args, 0);
                                r.set(module.commandSetFactionName(commander, factionName));
                            } else {
                                r.set(Result.FAILURE, lang.getString("command_tooltip_faction_set_name", language));
                            }
                        }
                        // /faction set password
                        else if (command.equals("password")) {
                            if (!commander.hasPermission(getPermissionNode("faction set password"))) {
                                r.deny();
                                return;
                            }
                            if (args.length == 2) {
                                // @formatter:off
                                String password    = args[0];
                                String passwordNew = args[1];
                                // @formatter:on
                                r.set(module.commandSetFactionPassword(commander, password, passwordNew));
                            } else {
                                r.set(Result.FAILURE, lang.getString("command_tooltip_faction_set_password", language));
                            }
                        }
                        // /faction set tag
                        else if (command.equals("tag")) {
                            if (!commander.hasPermission(getPermissionNode("faction set tag"))) {
                                r.deny();
                                return;
                            }
                            if (args.length == 1) {
                                String tag = args[0];
                                r.set(module.commandSetFactionTag(commander, tag));
                            } else {
                                r.set(Result.FAILURE, lang.getString("command_tooltip_faction_set_tag", language));
                            }
                        } else {
                            r.set(Result.FAILURE, lang.getString("command_tooltip_faction_set", language));
                        }
                    } else {
                        r.set(Result.FAILURE, lang.getString("command_tooltip_faction_set", language));
                    }
                } else {
                    r.set(Result.FAILURE, lang.getString("command_tooltip_faction", language));
                }
            } else {
                // No sub-commands exist.
                processHelpMessage(commander, r);
            }
        }
    }

    private void processHelpMessage(Player commander, Response r) {
        ModuleFactions module = getModule();
        LanguagePackage lang = module.getLanguagePackage();
        Language language = commander.getLanguage();
        StringBuilder builder = new StringBuilder();
        builder.append(lang.getString("command_tooltip_faction_header", language));
        List<String> listCommands = new ArrayList<>(mapContexts.keySet());
        Collections.sort(listCommands, new Comparator<String>() {
            @Override
            public int compare(String string1, String string2) {
                return string1.compareTo(string2);
            }
        });
        for (String key : listCommands) {
            // Grab the PermissionNode.
            String permissionNode = mapContexts.get(key);
            if (commander.hasPermission(permissionNode)) {
                String langString = "command_tooltip_" + key.replaceAll(" ", "_");
                module.println("langString: " + langString);
                String langResult = lang.getString(langString, language);
                if (langResult != null) {
                    langResult = langResult.replaceAll("\n", " <LINE> ");
                    builder.append(" <LINE> ").append(langResult);
                }
            }
        }
        // If there are any help responses, return the result help string.
        String result = builder.toString();

        // If the Commander does not have any of the permission nodes for the commands, send him a denied message.
        if (result.length() == 0) {
            r.deny();
            return;
        }
        // Make sure content is provided to send a result message.
        if (!result.equals("null")) {
            r.set(Result.SUCCESS, builder.toString());
        }
    }

    public ModuleFactions getModule() {
        return this.module;
    }

    private void setModule(ModuleFactions module) {
        this.module = module;
    }

    @Override
    public String getName() {
        return "Factions";
    }
}