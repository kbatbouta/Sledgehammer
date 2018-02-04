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

import sledgehammer.annotations.CommandHandler;
import sledgehammer.enums.Result;
import sledgehammer.command.CommandListener;
import sledgehammer.language.Language;
import sledgehammer.language.LanguagePackage;
import sledgehammer.lua.core.Player;
import sledgehammer.lua.permissions.PermissionGroup;
import sledgehammer.util.Command;
import sledgehammer.util.Response;

/**
 * TODO: Document.
 *
 * @author Jab
 */
public class PermissionsCommandListener extends CommandListener {

  private ModulePermissions module;

  PermissionsCommandListener(ModulePermissions module) {
    super(module.getLanguagePackage());
    setModule(module);
  }

  @CommandHandler(
    command = {"permissions", "permission"},
    permission = "core.permissions.command.permissions"
  )
  public void onCommandPermissions(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    r.set(Result.SUCCESS, lang.getString("tooltip_command_permissions", language));
  }

  @CommandHandler(
    command = {"permissions group", "permissions g", "permission group", "permission g"},
    permission = "core.permissions.command.permissions.group"
  )
  public void onCommandPermissionsGroup(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    r.set(Result.SUCCESS, lang.getString("tooltip_command_permissions_group", language));
  }

  @CommandHandler(
    command = {
      "permissions group create",
      "permissions g create",
      "permission group create",
      "permission g create",
    },
    permission = "core.permissions.command.permissions.group.create"
  )
  public void onCommandPermissionsGroupCreate(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = Command.getSubArgs(c.getArguments(), 2);
    if (args.length != 1) {
      r.set(Result.SUCCESS, lang.getString("tooltip_command_permissions_group_create", language));
      return;
    }
    String permissionGroupName = args[0];
    r.set(module.commandCreatePermissionGroup(commander, permissionGroupName));
  }

  @CommandHandler(
    command = {
      "permissions group delete",
      "permissions g delete",
      "permission group delete",
      "permission g delete"
    },
    permission = "core.permissions.command.permissions.group.delete"
  )
  public void onCommandPermissionsGroupDelete(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = Command.getSubArgs(c.getArguments(), 2);
    if (args.length != 1) {
      r.set(Result.SUCCESS, lang.getString("tooltip_command_permissions_group_delete", language));
      return;
    }
    String permissionGroupName = args[0];
    r.set(module.commandDeletePermissionGroup(commander, permissionGroupName));
  }

  @CommandHandler(
    command = {
      "permissions group list",
      "permissions g list",
      "permission group list",
      "permission g list"
    },
    permission = "core.permissions.command.permissions.group.list"
  )
  public void onCommandPermissionsGroupList(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = Command.getSubArgs(c.getArguments(), 2);
    if (args.length != 1) {
      r.set(Result.SUCCESS, lang.getString("tooltip_command_permissions_group_list", language));
      return;
    }
    String permissionGroupName = args[0];
    r.set(module.commandListPermissionGroup(commander, permissionGroupName));
  }

  @CommandHandler(
    command = {
      "permissions group rename",
      "permissions g rename",
      "permission group rename",
      "permission g rename"
    },
    permission = "core.permissions.command.permissions.group.rename"
  )
  public void onCommandPermissionsGroupRename(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = Command.getSubArgs(c.getArguments(), 2);
    if (args.length != 2) {
      r.set(Result.SUCCESS, lang.getString("tooltip_command_permissions_group_rename", language));
      return;
    }
    String permissionGroupName = args[0];
    String permissionGroupNameNew = args[1];
    r.set(
        module.commandRenamePermissionGroup(
            commander, permissionGroupName, permissionGroupNameNew));
  }

  @CommandHandler(
    command = {
      "permissions group set",
      "permissions g set",
      "permission group set",
      "permission g set"
    },
    permission = "core.permissions.command.permissions.group.set"
  )
  public void onCommandPermissionsGroupSet(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    r.set(Result.SUCCESS, lang.getString("tooltip_command_permissions_group_set", language));
  }

  @CommandHandler(
    command = {
      "permissions group set node",
      "permissions g set node",
      "permission group set node",
      "permission g set node"
    },
    permission = "core.permissions.command.permissions.group.set.node"
  )
  public void onCommandPermissionsGroupSetNode(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = Command.getSubArgs(c.getArguments(), 3);
    if (args.length != 3) {
      r.set(Result.SUCCESS, lang.getString("tooltip_command_permissions_group_set_node", language));
      return;
    }
    String permissionGroupName = args[0];
    String node = args[1];
    String flag = args[2];
    r.set(module.commandSetPermissionGroupNode(commander, permissionGroupName, node, flag));
  }

  @CommandHandler(
    command = {
      "permissions group set parent",
      "permissions g set parent",
      "permission group set parent",
      "permission g set parent"
    },
    permission = "core.permissions.command.permissions.group.set.parent"
  )
  public void onCommandPermissionsGroupSetParent(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = Command.getSubArgs(c.getArguments(), 3);
    if (args.length != 2) {
      r.set(
          Result.SUCCESS, lang.getString("tooltip_command_permissions_group_set_parent", language));
      return;
    }
    String permissionGroupName = args[0];
    String permissionGroupNameParent = args[1];
    r.set(
        module.commandSetPermissionGroupParent(
            commander, permissionGroupName, permissionGroupNameParent));
  }

  @CommandHandler(
    command = {"permissions test", "permission test"},
    permission = "core.permissions.command.permissions.test"
  )
  public void onCommandPermissionsTest(Command c, Response r) {
    String[] args = Command.getSubArgs(c.getArguments(), 1);
    String permission = args[0];
    PermissionGroup groupDefault = module.getDefaultPermissionGroup();
    boolean flag = groupDefault.hasPermission(permission);
    r.set(
        Result.SUCCESS,
        "[" + groupDefault.getGroupName() + "] Node: " + permission + " = " + flag + ".");
  }

  @CommandHandler(
    command = {"permissions user", "permissions u", "permission user", "permission u"},
    permission = "core.permissions.command.permissions.user"
  )
  public void onCommandPermissionsUser(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    r.set(Result.SUCCESS, lang.getString("tooltip_command_permissions_user", language));
  }

  @CommandHandler(
    command = {
      "permissions user create",
      "permissions u create",
      "permission user create",
      "permission u create"
    },
    permission = "core.permissions.command.permissions.user.create"
  )
  public void onCommandPermissionsUserCreate(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = Command.getSubArgs(c.getArguments(), 2);
    if (args.length != 1) {
      r.set(Result.FAILURE, lang.getString("tooltip_command_permissions_user_create", language));
      return;
    }
    String username = args[0];
    r.set(module.commandCreatePermissionUser(commander, username));
  }

  @CommandHandler(
    command = {
      "permissions user delete",
      "permissions u delete",
      "permission user delete",
      "permission u delete"
    },
    permission = "core.permissions.command.permissions.user.delete"
  )
  public void onCommandPermissionsUserDelete(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = Command.getSubArgs(c.getArguments(), 2);
    if (args.length != 1) {
      r.set(Result.FAILURE, lang.getString("tooltip_command_permissions_user_delete", language));
      return;
    }
    String username = args[0];
    r.set(module.commandDeletePermissionUser(commander, username));
  }

  @CommandHandler(
    command = {
      "permissions user list",
      "permissions u list",
      "permission user list",
      "permission u list"
    },
    permission = "core.permissions.command.permissions.user.list"
  )
  public void onCommandPermissionsUserList(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = Command.getSubArgs(c.getArguments(), 2);
    if (args.length != 1) {
      r.set(Result.FAILURE, lang.getString("tooltip_command_permissions_user_list", language));
      return;
    }
    String username = args[0];
    r.set(module.commandListPermissionUser(commander, username));
  }

  @CommandHandler(
    command = {
      "permissions user set",
      "permissions u set",
      "permission user set",
      "permission u set"
    },
    permission = "core.permissions.command.permissions.user.set"
  )
  public void onCommandPermissionsUserSet(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    r.set(Result.SUCCESS, lang.getString("tooltip_command_permissions_user_set", language));
  }

  @CommandHandler(
    command = {
      "permissions user set node",
      "permissions u set node",
      "permission user set node",
      "permission u set node"
    },
    permission = "core.permissions.command.permissions.user.set.node"
  )
  public void onCommandPermissionsUserSetNode(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = Command.getSubArgs(c.getArguments(), 3);
    if (args.length != 3) {
      r.set(Result.FAILURE, lang.getString("tooltip_command_permissions_user_set_node", language));
      return;
    }
    String username = args[0];
    String node = args[1];
    String flag = args[2];
    r.set(module.commandSetPermissionUserNode(commander, username, node, flag));
  }

  @CommandHandler(
    command = {
      "permissions user set group",
      "permissions u set group",
      "permission user set group",
      "permission u set group"
    },
    permission = "core.permissions.command.permissions.user.set.group"
  )
  public void onCommandPermissionsUserSetGroup(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = Command.getSubArgs(c.getArguments(), 3);
    if (args.length != 2) {
      r.set(Result.FAILURE, lang.getString("tooltip_command_permissions_user_set_group", language));
      return;
    }
    String username = args[0];
    String permissionGroupName = args[1];
    r.set(module.commandSetPermissionUserGroup(commander, username, permissionGroupName));
  }

  public ModulePermissions getModule() {
    return module;
  }

  public void setModule(ModulePermissions module) {
    this.module = module;
  }
}
