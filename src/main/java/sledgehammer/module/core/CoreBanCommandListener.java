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

package sledgehammer.module.core;

import sledgehammer.SledgeHammer;
import sledgehammer.annotations.CommandHandler;
import sledgehammer.enums.LogType;
import sledgehammer.enums.Result;
import sledgehammer.command.CommandListener;
import sledgehammer.language.Language;
import sledgehammer.language.LanguagePackage;
import sledgehammer.lua.chat.ChatMessage;
import sledgehammer.lua.core.Player;
import sledgehammer.command.Command;
import sledgehammer.util.Response;
import zombie.core.raknet.UdpConnection;
import zombie.core.znet.SteamUtils;
import zombie.network.ServerWorldDatabase;
import zombie.sledgehammer.PacketHelper;

public class CoreBanCommandListener extends CommandListener {

  private ModuleCore module;

  CoreBanCommandListener(ModuleCore module) {
    super(module.getLanguagePackage());
    setModule(module);
  }

  @CommandHandler(command = "ban", permission = "core.command.ban")
  private void onCommandBan(Command c, Response r) {
    Player commanderP = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commanderP.getLanguage();
    String[] args = c.getArguments();
    if (args.length == 0) {
      r.set(Result.FAILURE, lang.getString("tooltip_command_ban"));
      return;
    }
    String response;
    String commander = commanderP.getUsername();
    if (args.length > 1) {
      String username = null;
      String IP = null;
      String SteamID = null;
      String reason = null;
      boolean bUsername = false;
      boolean bIP = false;
      boolean bSteamID = false;
      for (int x = 0; x < args.length; x++) {
        String arg = args[x];
        String argN = ((x + 1) < args.length) ? args[x + 1] : null;
        if ((arg.startsWith("-U") || arg.startsWith("-u"))
            && argN != null
            && !argN.startsWith("-")) {
          bUsername = true;
          username = argN;
          x++;
        } else if ((arg.startsWith("-R") || arg.startsWith("-r"))
            && argN != null
            && !argN.startsWith("-")) {
          reason = argN;
          x++;
        } else if (arg.startsWith("-i")) {
          if (!SteamUtils.isSteamModeEnabled()) {
            bIP = true;
          } else {
            response = "Cannot infer IP-Ban in Steam mode.";
            r.set(Result.FAILURE, response);
            return;
          }
        } else if (arg.startsWith("-s")) {
          if (SteamUtils.isSteamModeEnabled()) {
            bSteamID = true;
          } else {
            response = "Cannot infer SteamID Ban in Non-Steam mode.";
            r.set(Result.FAILURE, response);
            return;
          }
        } else if (arg.startsWith("-I") && argN != null && !argN.startsWith("-")) {
          if (!SteamUtils.isSteamModeEnabled()) {
            bIP = true;
            IP = argN;
            x++;
          } else {
            response = "Cannot IP-Ban in Steam mode.";
            r.set(Result.FAILURE, response);
            return;
          }
        } else if (arg.startsWith("-S") && argN != null && !argN.startsWith("-")) {
          if (SteamUtils.isSteamModeEnabled()) {
            bSteamID = true;
            SteamID = argN;
            x++;
          } else {
            response = "Cannot SteamID Ban in Non-Steam mode.";
            r.set(Result.FAILURE, response);
            return;
          }
        } else if ((arg.startsWith("-S")
                || arg.startsWith("-s")
                || arg.startsWith("-I")
                || arg.startsWith("-i")
                || arg.startsWith("-U")
                || arg.startsWith("-u")
                || arg.startsWith("-R")
                || arg.startsWith("-r"))
            && (argN == null || argN.startsWith("-"))) {
          r.set(Result.FAILURE, lang.getString("tooltip_command_ban", language));
          return;
        }
      }
      if (!bIP && !bSteamID && !bUsername) {
        r.set(Result.FAILURE, lang.getString("tooltip_command_ban", language));
        return;
      }
      Player playerBanned = SledgeHammer.instance.getPlayer(username);
      UdpConnection connectionBanned = playerBanned.getConnection();
      if (bIP && IP != null && !IP.isEmpty()) {
        if (SteamUtils.isSteamModeEnabled()) {
          response = "Cannot IP ban when the server is in Steam mode.";
          r.set(Result.FAILURE, response);
          return;
        }
        if (reason == null) {
          reason = "Banned. (IP)";
        }
        ServerWorldDatabase.instance.banIp(
            IP, username == null || username.isEmpty() ? "NULL" : username, reason, true);
        response = "Banned. (IP).";
        kickUser(connectionBanned, reason);
        r.set(Result.SUCCESS, response);
        r.setLoggedImportant(true);
        r.log(LogType.STAFF, commander + " banned " + username + ". IP=(" + IP + ")");
        return;
      }

      if (bSteamID && SteamID != null && !SteamID.isEmpty()) {
        if (!SteamUtils.isSteamModeEnabled()) {
          response = "Cannot Steam-Ban a user while NOT in Steam mode.";
          r.set(Result.FAILURE, response);
          return;
        }
        if (!SteamUtils.isValidSteamID(SteamID)) {
          response = "Invalid SteamID: \"" + SteamID + "\".";
          r.set(Result.FAILURE, response);
          return;
        }
        if (reason == null) {
          reason = "Banned. (Steam)";
        }
        ServerWorldDatabase.instance.banSteamID(
            SteamID, username == null || username.isEmpty() ? "NULL" : username, reason, true);
        response = "Steam-Banned Player.";
        kickUser(connectionBanned, reason);
        r.set(Result.SUCCESS, response);
        r.setLoggedImportant(true);
        r.log(LogType.STAFF, commander + " banned " + username + ". SteamID=(" + SteamID + ")");
        return;
      }
      if (!bUsername) {
        response = "Must have -u \"username\" to use this command!";
        r.set(Result.FAILURE, response);
        return;
      }
      // Implied. Requires -U
      if (bIP) {
        if (SteamUtils.isSteamModeEnabled()) {
          response = "Cannot IP ban when the server is in Steam mode.";
          r.set(Result.FAILURE, response);
          return;
        }
        if (connectionBanned == null || !connectionBanned.connected) {
          response = "User must be online in order to imply IP ban.";
          r.set(Result.FAILURE, response);
          return;
        }
        IP = connectionBanned.ip;
        if (reason == null) {
          reason = "Banned. (IP)";
        }
        ServerWorldDatabase.instance.banIp(IP, username, reason, true);
        kickUser(connectionBanned, reason);
        response = "IP-Banned Player.";
        r.set(Result.SUCCESS, response);
        r.setLoggedImportant(true);
        r.log(LogType.STAFF, commander + " banned " + username + ". IP=(" + IP + ")");
      } else if (bSteamID) {
        if (!SteamUtils.isSteamModeEnabled()) {
          response = "Cannot Steam-Ban a user while NOT in Steam mode.";
          r.set(Result.FAILURE, response);
          return;
        }
        if (connectionBanned == null || !connectionBanned.connected) {
          response = "User must be online in order to imply Steam-ban.";
          r.set(Result.FAILURE, response);
          return;
        }
        SteamID = "" + connectionBanned.steamID;
        if (!SteamUtils.isValidSteamID(SteamID)) {
          response = "Invalid SteamID: \"" + SteamID + "\".";
          r.set(Result.FAILURE, response);
          return;
        }
        if (reason == null) {
          reason = "Banned. (Steam)";
        }
        response = ServerWorldDatabase.instance.banSteamID(SteamID, username, reason, true);
        kickUser(connectionBanned, reason);
        r.set(Result.SUCCESS, response);
        r.setLoggedImportant(true);
        r.log(LogType.STAFF, commander + " banned " + username + ". SteamID=(" + SteamID + ")");
      } else {
        if (reason == null) {
          reason = "Banned.";
        }
        response = ServerWorldDatabase.instance.banUser(username, true);
        kickUser(connectionBanned, reason);
        r.set(Result.SUCCESS, response);
        r.setLoggedImportant(true);
        r.log(LogType.STAFF, commander + " banned " + username + ".");
      }
    } else {
      r.set(Result.FAILURE, lang.getString("tooltip_command_ban", language));
    }
  }

  @CommandHandler(command = "unban", permission = "core.command.unban")
  private void unban(Command c, Response r) {
    Player commanderP = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commanderP.getLanguage();
    String[] args = c.getArguments();
    if (args.length == 0) {
      r.set(Result.FAILURE, lang.getString("tooltip_command_unban", language));
      return;
    }
    boolean bUsername = false;
    boolean bIP = false;
    boolean bSteamID = false;
    String response = null;
    String commander = commanderP.getUsername();
    String username = null;
    String IP = null;
    String SteamID = null;
    for (int x = 0; x < args.length; x++) {
      String arg = args[x];
      String argN = (x + 1) < args.length ? args[x + 1] : null;
      if ((arg.startsWith("-U") || arg.startsWith("-u")) && argN != null && !argN.startsWith("-")) {
        bUsername = true;
        username = argN;
      } else if (arg.startsWith("-I") && argN != null && !argN.startsWith("-")) {
        bIP = true;
        IP = argN;
        x++;
      } else if (arg.startsWith("-S") && argN != null && !argN.startsWith("-")) {
        bSteamID = true;
        SteamID = argN;
        x++;
      } else if ((arg.startsWith("-I")
              || arg.startsWith("-S")
              || arg.startsWith("-U")
              || arg.startsWith("-u"))
          && (argN == null || argN.startsWith("-"))) {
        r.set(Result.FAILURE, lang.getString("tooltip_command_unban", language));
        return;
      }
    }
    if (!bIP && !bSteamID && !bUsername) {
      r.set(Result.FAILURE, lang.getString("tooltip_command_unban", language));
      return;
    }
    if (bSteamID) {
      ServerWorldDatabase.instance.banSteamID(
          SteamID, username == null || username.isEmpty() ? null : username, false);
      response = "SteamID unbanned.";
    }
    if (bIP) {
      ServerWorldDatabase.instance.banIp(
          IP, username == null || username.isEmpty() ? null : username, null, false);
      response = "IP unbanned.";
    }
    if (bUsername) {
      ServerWorldDatabase.instance.banUser(username, false);
      response = "Player unbanned.";
    }
    r.set(Result.SUCCESS, response);
    r.setLoggedImportant(true);
    r.log(LogType.STAFF, commander + " unbanned " + username + ".");
  }

  @CommandHandler(command = "warn", permission = "core.command.warn")
  private void onCommandWarn(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = c.getArguments();
    if (args.length < 2) {
      r.set(Result.FAILURE, lang.getString("tooltip_command_warn", language));
      return;
    }
    String playerName = args[0];
    StringBuilder msg = new StringBuilder();
    for (int x = 1; x < args.length; x++) {
      msg.append(args[x]).append(" ");
    }
    msg = new StringBuilder(msg.substring(0, msg.length() - 1));
    Player playerDirty = SledgeHammer.instance.getPlayerDirty(playerName);
    if (playerDirty == null) {
      r.set(Result.FAILURE, "Player not found: " + playerName);
      return;
    }
    if (!playerDirty.isConnected()) {
      r.set(Result.FAILURE, "player is not Online: \"" + playerDirty.getName() + "\"");
      return;
    }
    ChatMessage chatMessage = module.createChatMessage("You have been warned. Reason: " + msg);
    chatMessage.setPlayerId(commander.getUniqueId(), false);
    playerDirty.sendChatMessageToAllChatChannels(chatMessage);
    r.set(Result.SUCCESS, "Player warned.");
    r.log(LogType.STAFF, "WARNED " + playerDirty.getName() + " with message: \"" + msg + "\".");
  }

  private void kickUser(UdpConnection connection, String reason) {
    PacketHelper.kickUser(connection, reason);
  }

  public ModuleCore getModule() {
    return this.module;
  }

  public void setModule(ModuleCore module) {
    this.module = module;
  }
}
