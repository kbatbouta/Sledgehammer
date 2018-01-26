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

import sledgehammer.annotations.CommandHandler;
import sledgehammer.enums.Result;
import sledgehammer.interfaces.Listener;
import sledgehammer.language.Language;
import sledgehammer.language.LanguagePackage;
import sledgehammer.lua.core.Player;
import sledgehammer.util.Command;
import sledgehammer.util.Printable;
import sledgehammer.util.Response;

/**
 * Class designed to handle commands from players for the Factions Module.
 *
 * <p>
 *
 * @author Jab
 */
class FactionsCommandListener extends Printable implements Listener {

  private ModuleFactions module;

  /**
   * Main constructor.
   *
   * @param module The faction module instance registering the listener.
   */
  FactionsCommandListener(ModuleFactions module) {
    setModule(module);
  }

  @CommandHandler(
    command = "faction",
    permission = "core.faction.command.faction.*",
    defaultPermission = true
  )
  private void onCommandFaction(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    r.set(Result.SUCCESS, lang.getString("tooltip_command_faction", language));
  }

  @CommandHandler(
    command = "faction create",
    permission = "core.faction.command.faction.create",
    defaultPermission = true
  )
  private void onCommandFactionCreate(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = c.getArguments();
    if (args.length == 3) {
      // @formatter:off
      String name = args[0];
      String tag = args[1];
      String password = args[2];
      // @formatter:on
      r.set(module.commandCreateFaction(commander, name, tag, password));
    } else {
      r.set(Result.FAILURE, lang.getString("command_tooltip_faction_create", language));
    }
  }

  @CommandHandler(
    command = "faction disband",
    permission = "core.faction.command.faction.disband",
    defaultPermission = true
  )
  private void onCommandFactionDisband(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = c.getArguments();
    if (args.length != 0) {
      r.set(Result.FAILURE, lang.getString("command_tooltip_faction_disband", language));
      return;
    }
    r.set(module.commandDisbandFaction(commander));
  }

  @CommandHandler(
    command = "faction join",
    permission = "core.faction.command.faction.join",
    defaultPermission = true
  )
  private void onCommandFactionJoin(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = c.getArguments();
    if (args.length != 2) {
      r.set(Result.FAILURE, lang.getString("command_tooltip_faction_join", language));
      return;
    }
    String factionName = args[0];
    String password = args[1];
    r.set(module.commandJoinFaction(commander, factionName, password));
  }

  @CommandHandler(
    command = "faction leave",
    permission = "core.faction.command.faction.leave",
    defaultPermission = true
  )
  private void onCommandFactionLeave(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = c.getArguments();
    if (args.length != 0) {
      r.set(Result.FAILURE, lang.getString("command_tooltip_faction_leave", language));
      return;
    }
    r.set(module.commandLeaveFaction(commander));
  }

  @CommandHandler(
    command = "faction invite",
    permission = "core.faction.command.faction.invite",
    defaultPermission = true
  )
  private void onCommandFactionInvite(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = c.getArguments();
    if (args.length != 1) {
      r.set(Result.FAILURE, lang.getString("command_tooltip_faction_invite", language));
      return;
    }
    // Grab the username argument.
    String usernameInvited = args[0];
    // Attempt to invite the Player.
    r.set(module.commandInviteToFaction(commander, usernameInvited));
  }

  @CommandHandler(
    command = "faction accept",
    permission = "core.faction.command.faction.accept",
    defaultPermission = true
  )
  private void onCommandFactionAccept(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = c.getArguments();
    if (args.length != 1) {
      r.set(Result.FAILURE, lang.getString("command_tooltip_faction_accept", language));
      return;
    }
    String factionName = args[0];
    r.set(module.commandAcceptInvite(commander, factionName));
  }

  @CommandHandler(
    command = "faction reject",
    permission = "core.faction.command.faction.reject",
    defaultPermission = true
  )
  private void onCommandFactionReject(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = c.getArguments();
    if (args.length != 1) {
      r.set(Result.FAILURE, lang.getString("command_tooltip_faction_reject", language));
      return;
    }
    String factionName = args[0];
    r.set(module.commandRejectInvites(commander, factionName));
  }

  @CommandHandler(
    command = "faction kick",
    permission = "core.faction.command.faction.kick",
    defaultPermission = true
  )
  private void onCommandFactionKick(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = c.getArguments();
    if (args.length == 0) {
      r.set(Result.FAILURE, lang.getString("command_tooltip_faction_kick", language));
      return;
    }
    String usernameKicked = args[0];
    String reason = args.length > 1 ? Command.combineArguments(args, 1) : "No" + " reason.";
    r.set(module.commandKickFromFaction(commander, usernameKicked, reason));
  }

  @CommandHandler(
    command = "faction set",
    permission = "core.faction.command.faction.set.*",
    defaultPermission = true
  )
  private void onCommandFactionSet(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    r.set(Result.SUCCESS, lang.getString("tooltip_command_faction_set", language));
  }

  @CommandHandler(
    command = "faction set color",
    permission = "core.faction.command.faction.set.color",
    defaultPermission = true
  )
  private void onCommandFactionSetColor(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = Command.getSubArgs(c.getArguments(), 2);
    if (args.length != 1) {
      r.set(Result.FAILURE, lang.getString("command_tooltip_faction_set_color", language));
      return;
    }
    String color = args[0];
    r.set(module.commandSetFactionColor(commander, color));
  }

  @CommandHandler(
    command = "faction set name",
    permission = "core.faction.command.faction.set.name",
    defaultPermission = true
  )
  private void onCommandFactionSetName(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = Command.getSubArgs(c.getArguments(), 2);
    if (args.length == 0) {
      r.set(Result.FAILURE, lang.getString("command_tooltip_faction_set_name", language));
      return;
    }
    String factionName = Command.combineArguments(args, 0);
    r.set(module.commandSetFactionName(commander, factionName));
  }

  @CommandHandler(
    command = "faction set owner",
    permission = "core.faction.command.faction.set.owner",
    defaultPermission = true
  )
  private void onCommandFactionSetOwner(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = Command.getSubArgs(c.getArguments(), 2);
    r.set(Result.SUCCESS, "Command not implemented yet!");
  }

  @CommandHandler(
    command = "faction set password",
    permission = "core.faction.command.faction.set.password",
    defaultPermission = true
  )
  private void onCommandFactionSetPassword(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = Command.getSubArgs(c.getArguments(), 2);
    if (args.length != 2) {
      r.set(Result.FAILURE, lang.getString("command_tooltip_faction_set_password", language));
      return;
    }
    // @formatter:off
    String password = args[0];
    String passwordNew = args[1];
    // @formatter:on
    r.set(module.commandSetFactionPassword(commander, password, passwordNew));
  }

  @CommandHandler(
    command = "faction set tag",
    permission = "core.faction.command.faction.set.tag",
    defaultPermission = true
  )
  private void onCommandFactionSetTag(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = Command.getSubArgs(c.getArguments(), 2);
    if (args.length != 1) {
      r.set(Result.FAILURE, lang.getString("command_tooltip_faction_set_tag", language));
      return;
    }
    String tag = args[0];
    r.set(module.commandSetFactionTag(commander, tag));
  }

  private LanguagePackage getLanguagePackage() {
    return getModule().getLanguagePackage();
  }

  private ModuleFactions getModule() {
    return this.module;
  }

  private void setModule(ModuleFactions module) {
    this.module = module;
  }

  @Override
  public String getName() {
    return "FactionsCommandListener";
  }
}
