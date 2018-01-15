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

package sledgehammer.module.permissions;

import java.util.*;

import sledgehammer.enums.Result;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.language.Language;
import sledgehammer.language.LanguagePackage;
import sledgehammer.lua.core.Player;
import sledgehammer.util.Command;
import sledgehammer.util.Response;

/**
 * TODO: Document.
 *
 * @author Jab
 */
public class PermissionsCommandListener implements CommandListener {

    private ModulePermissions module;

    private Map<String, String> mapPermissionNodes;

    PermissionsCommandListener(ModulePermissions module) {
        setModule(module);
        mapPermissionNodes = new HashMap<>();
        // @formatter:off
        mapPermissionNodes.put("permissions"                 , "sledgehammer.permissions"                  );
        mapPermissionNodes.put("permissions group"           , "sledgehammer.permissions.group"            );
        mapPermissionNodes.put("permissions group create"    , "sledgehammer.permissions.group.create"     );
        mapPermissionNodes.put("permissions group delete"    , "sledgehammer.permissions.group.delete"     );
        mapPermissionNodes.put("permissions group list"      , "sledgehammer.permissions.group.list"       );
        mapPermissionNodes.put("permissions group rename"    , "sledgehammer.permissions.group.rename"     );
        mapPermissionNodes.put("permissions group set"       , "sledgehammer.permissions.group.set"        );
        mapPermissionNodes.put("permissions group set node"  , "sledgehammer.permissions.group.set.context");
        mapPermissionNodes.put("permissions group set parent", "sledgehammer.permissions.group.set.parent" );
        mapPermissionNodes.put("permissions user"            , "sledgehammer.permissions.user"             );
        mapPermissionNodes.put("permissions user create"     , "sledgehammer.permissions.user.create"      );
        mapPermissionNodes.put("permissions user delete"     , "sledgehammer.permissions.user.delete"      );
        mapPermissionNodes.put("permissions user set"        , "sledgehammer.permissions.user.set"         );
        mapPermissionNodes.put("permissions user set node"   , "sledgehammer.permissions.user.set.context" );
        mapPermissionNodes.put("permissions user set group"  , "sledgehammer.permissions.user.set.group"   );
        mapPermissionNodes.put("permissions user set list"   , "sledgehammer.permissions.user.list"        );
        // @formatter:on
    }

    @Override
    public void onCommand(Command c, Response r) {
        ModulePermissions module = getModule();
        LanguagePackage lang = module.getLanguagePackage();
        Result result = Result.FAILURE;
        Player commander = c.getPlayer();
        Language language = commander.getLanguage();
        String commanderName = commander.getUsername();
        String response = null;
        String command = c.getCommand().toLowerCase();
        String[] args = c.getArguments();
        Response moduleResponse = null;
        if (command.equals("permissions")) {
            if (args.length > 0) {
                command = args[0];
                args = Command.getSubArgs(args, 1);
                if (command.equals("help")) {
                    processHelpMessage(commander, r);
                } else if (command.equalsIgnoreCase("group")) {
                    if (args.length > 0) {
                        command = args[0];
                        args = Command.getSubArgs(args, 1);
                        // /permissions group create
                        if(command.equalsIgnoreCase("create")) {
                            if(!commander.hasPermission(getPermissionNode("permissions group create"))) {
                                r.deny();
                                return;
                            }
                            if(args.length == 1) {
                                String permissionGroupName = args[0];
                                r.set(module.commandCreatePermissionGroup(commander, permissionGroupName));
                            }
                            // tool-tip
                            else {
                                r.set(Result.SUCCESS, lang.getString("command_tooltip_permissions_group_create", language));
                            }
                        }
                        // /permissions group delete
                        else if(command.equalsIgnoreCase("delete")) {
                            if(!commander.hasPermission(getPermissionNode("permissions group delete"))) {
                                r.deny();
                                return;
                            }
                            if(args.length == 1) {
                                String permissionGroupName = args[0];
                                r.set(module.commandDeletePermissionGroup(commander, permissionGroupName));
                            } else {
                                r.set(Result.SUCCESS, lang.getString("command_tooltip_permissions_group_delete", language));
                            }
                        }
                        // /permissions group list
                        else if(command.equalsIgnoreCase("list")) {
                            if(!commander.hasPermission(getPermissionNode("permissions group list"))) {
                                r.deny();
                                return;
                            }
                            if(args.length == 1) {
                                String permissionGroupName = args[0];
                                r.set(module.commandListPermissionGroup(commander, permissionGroupName));
                            } else {
                                r.set(Result.SUCCESS, lang.getString("command_tooltip_permissions_group_list", language));
                            }
                        }
                        // /permissions group rename
                        else if(command.equalsIgnoreCase("rename")) {
                            if(!commander.hasPermission(getPermissionNode("permissions group rename"))) {
                                r.deny();
                                return;
                            }
                            if(args.length == 2) {
                                String permissionGroupName = args[0];
                                String permissionGroupNameNew = args[1];
                                r.set(module.commandRenamePermissionGroup(commander, permissionGroupName, permissionGroupNameNew));
                            } else {
                                r.set(Result.SUCCESS, lang.getString("command_tooltip_permissions_group_rename", language));
                            }
                        }
                        // /permissions group set
                        else if(command.equalsIgnoreCase("set")) {
                            if(args.length > 0) {
                                command = args[0];
                                args = Command.getSubArgs(args, 1);
                                // /permissions group set node
                                if(command.equalsIgnoreCase("node")) {
                                    if(!commander.hasPermission(getPermissionNode("permissions group set node"))) {
                                        r.deny();
                                        return;
                                    }
                                    if(args.length == 3) {
                                        String permissionGroupName = args[0];
                                        String node = args[1];
                                        String flag = args[2];
                                        r.set(module.commandSetPermissionGroupNode(commander, permissionGroupName, node, flag));
                                    } else {
                                        r.set(Result.SUCCESS, lang.getString("command_tooltip_permissions_group_set_node", language));
                                    }
                                }
                                // /permissions group set parent
                                else if(command.equalsIgnoreCase("parent")) {
                                    if(!commander.hasPermission(getPermissionNode("permissions group set parent"))) {
                                        r.deny();
                                        return;
                                    }
                                    if(args.length == 2) {
                                        String permissionGroupName = args[0];
                                        String permissionGroupNameParent = args[1];
                                        r.set(module.commandSetPermissionGroupParent(commander, permissionGroupName, permissionGroupNameParent));
                                    } else {
                                        r.set(Result.SUCCESS, lang.getString("command_tooltip_permissions_group_set_parent", language));
                                    }
                                }
                            } else {
                                r.set(Result.FAILURE, lang.getString("command_tooltip_permissions_group_set", language));
                            }
                        } else {
                            r.set(Result.FAILURE, lang.getString("command_tooltip_permissions_group", language));
                        }
                    } else {
                        r.set(Result.FAILURE, lang.getString("command_tooltip_permissions_group", language));
                    }
                }
                // /permission user
                else if(command.equalsIgnoreCase("user")) {
                    if (args.length > 0) {
                        command = args[0];
                        args = Command.getSubArgs(args, 1);
                        // /permission user create
                        if(command.equalsIgnoreCase("create")) {
                            if(!commander.hasPermission(getPermissionNode("permissions user create"))) {
                                r.deny();
                                return;
                            }
                            if(args.length > 0) {
                                String username = args[0];
                                r.set(module.commandCreatePermissionUser(commander, username));
                            } else {
                                r.set(Result.FAILURE, lang.getString("command_tooltip_permissions_user_create", language));
                            }
                        }
                        // /permission user delete
                        else if(command.equalsIgnoreCase("delete")) {
                            if(!commander.hasPermission(getPermissionNode("permissions user delete"))) {
                                r.deny();
                                return;
                            }
                            if(args.length > 0) {
                                String username = args[0];
                                r.set(module.commandDeletePermissionUser(commander, username));
                            } else {
                                r.set(Result.FAILURE, lang.getString("command_tooltip_permissions_user_delete", language));
                            }
                        }
                        // /permission user list
                        else if(command.equalsIgnoreCase("list")) {
                            if(!commander.hasPermission(getPermissionNode("permissions user list"))) {
                                r.deny();
                                return;
                            }
                            if(args.length == 1) {
                                String username = args[0];
                                r.set(module.commandListPermissionUser(commander, username));
                            } else {
                                r.set(Result.FAILURE, lang.getString("command_tooltip_permissions_user_list", language));
                            }
                        }
                        // /permission user set
                        else if(command.equals("set")) {
                            if(args.length > 0) {
                                command = args[0];
                                args = Command.getSubArgs(args, 1);
                                // /permission user set group
                                if(command.equalsIgnoreCase("group")) {
                                    if(!commander.hasPermission(getPermissionNode("permissions user set group"))) {
                                        r.deny();
                                        return;
                                    }
                                    if(args.length == 2) {
                                        String username = args[0];
                                        String permissionGroupName = args[1];
                                        r.set(module.commandSetPermissionUserGroup(commander, username, permissionGroupName));
                                    } else {
                                        r.set(Result.FAILURE, lang.getString("command_tooltip_permissions_user_set_group", language));
                                    }
                                }
                                // /permission user set node
                                else if(command.equalsIgnoreCase("node")) {
                                    if(!commander.hasPermission(getPermissionNode("permissions user set node"))) {
                                        r.deny();
                                        return;
                                    }
                                    if(args.length == 3) {
                                        String username = args[0];
                                        String node = args[1];
                                        String flag = args[2];
                                        r.set(module.commandSetPermissionUserNode(commander, username, node, flag));
                                    } else {
                                        r.set(Result.FAILURE, lang.getString("command_tooltip_permissions_user_set_node", language));
                                    }
                                }
                            } else {
                                r.set(Result.FAILURE, lang.getString("command_tooltip_permissions_user_set", language));
                            }
                        }
                        else {
                            r.set(Result.FAILURE, lang.getString("command_tooltip_permissions_user", language));
                        }
                    } else {
                        r.set(Result.FAILURE, lang.getString("command_tooltip_permissions_user", language));
                    }
                } else {
                    r.set(Result.FAILURE, lang.getString("command_tooltip_permissions", language));
                }
            } else {
                // No sub-commands exist.
                processHelpMessage(commander, r);
            }
        }
    }

    @Override
    public String onTooltip(Player commander, Command c) {
        String command = c.getCommand().toLowerCase();
        if (command.equals("permissions") && commander.hasPermission(getPermissionNode("permissions") + ".*")) {
            Response r = new Response();
            processHelpMessage(commander, r);
            return r.getResponse();
        }
        return null;
    }

    @Override
    public String getPermissionNode(String command) {
        return mapPermissionNodes.get(command.toLowerCase().trim());
    }

    @Override
    public String[] getCommands() {
        return new String[]{"permissions"};
    }

    private void processHelpMessage(Player commander, Response r) {
        ModulePermissions module = getModule();
        LanguagePackage lang = module.getLanguagePackage();
        Language language = commander.getLanguage();
        StringBuilder builder = new StringBuilder();
        builder.append(lang.getString("command_tooltip_permissions_header", language));
        List<String> listCommands = new ArrayList<>(mapPermissionNodes.keySet());
        Collections.sort(listCommands, new Comparator<String>() {
            @Override
            public int compare(String string1, String string2) {
                return string1.compareTo(string2);
            }
        });
        for (String key : listCommands) {
            // Grab the PermissionNode.
            String permissionNode = mapPermissionNodes.get(key);
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
        // If the Commander does not have any of the permission nodes for the commands, send him a denied message.
        if (builder.length() == 0) {
            r.set(Result.FAILURE, module.getPermissionDeniedMessage());
            return;
        }
        // If there are any help responses, return the result help string.
        r.set(Result.SUCCESS, builder.toString());
    }

    public ModulePermissions getModule() {
        return module;
    }

    public void setModule(ModulePermissions module) {
        this.module = module;
    }
}