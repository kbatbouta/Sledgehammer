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

import sledgehammer.SledgeHammer;
import sledgehammer.annotations.CommandHandler;
import sledgehammer.enums.LogType;
import sledgehammer.enums.Result;
import sledgehammer.command.CommandListener;
import sledgehammer.language.EntryField;
import sledgehammer.language.Language;
import sledgehammer.language.LanguagePackage;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.chat.send.Broadcast;
import sledgehammer.lua.core.Color;
import sledgehammer.lua.core.Player;
import sledgehammer.command.Command;
import sledgehammer.lua.chat.send.SendBroadcast;
import sledgehammer.util.Response;
import zombie.ui.UIFont;

public class ChatCommandListener extends CommandListener {

  private static final String permissionNodeEspanol = "core.chat.channel.espanol";

  private ModuleChat module;

  private SendBroadcast sendBroadcast;

  ChatCommandListener(ModuleChat module) {
    super(module.getLanguagePackage());
    setModule(module);
    sendBroadcast = new SendBroadcast();
  }

  @CommandHandler(command = "broadcast", permission = "core.chat.command.broadcast")
  private void onCommandBroadcast(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = c.getArguments();
    if (args.length <= 2) {
      r.set(Result.FAILURE, lang.getString("tooltip_command_broadcast", language));
      return;
    }
    String message = Command.combineArguments(args, 2, " ").trim();
    Color color = Color.getColor(args[1]);
    String fontName = args[0].trim().toLowerCase();
    UIFont font = null;
    for(UIFont fontNext : UIFont.values()) {
      if(fontNext.name().equalsIgnoreCase(fontName)) {
        font = fontNext;
        break;
      }
    }
    if(font == null) {
      font = UIFont.Massive;
    }
    if (color == null) {
      color = Color.LIGHT_RED;
    }
    Broadcast broadcast = new Broadcast(message, font, color);
    sendBroadcast.setBroadcast(broadcast);
    SledgeHammer.instance.send(sendBroadcast);
    r.set(Result.SUCCESS, "Broadcast sent.");
    r.log(LogType.STAFF, commander.getUsername() + " broadcasted message: \"" + args[1] + "\".");
  }

  @CommandHandler(command = "joinchannel", permission = "core.chat.command.joinchannel")
  public void onCommandJoin(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = c.getArguments();
    if (args.length == 0 || args.length > 2) {
      r.set(Result.FAILURE, lang.getString("tooltip_command_joinchannel", language));
      return;
    }
    String channelName = args[0];
    EntryField fieldChannel = new EntryField("channel", channelName);
    ChatChannel chatChannel = module.getDefinedChatChannel(channelName);
    if (chatChannel == null) {
      r.set(Result.FAILURE, lang.getString("channel_not_found", language, fieldChannel));
      return;
    }
    if (chatChannel.hasPlayer(commander)) {
      r.set(Result.FAILURE, lang.getString("already_in_channel", language, fieldChannel));
      return;
    }
    commander.setPermission(chatChannel.getPermissionNode(), true);
    chatChannel.addPlayer(commander, true);
    r.set(Result.SUCCESS, lang.getString("command_joinchannel_success", language, fieldChannel));
  }

  @CommandHandler(command = "leavechannel", permission = "core.chat.command.leavechannel")
  public void onCommandLeave(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String[] args = c.getArguments();
    if (args.length != 1) {
      r.set(Result.FAILURE, lang.getString("tooltip_command_leavechannel", language));
      return;
    }
    String channelName = args[0];
    EntryField fieldChannel = new EntryField("channel", channelName);
    ChatChannel chatChannel = module.getDefinedChatChannel(channelName);
    if (chatChannel == null) {
      r.set(Result.FAILURE, lang.getString("channel_not_found", language, fieldChannel));
      return;
    }
    if (!chatChannel.hasPlayer(commander)) {
      r.set(Result.FAILURE, lang.getString("not_in_channel", language, fieldChannel));
      return;
    }
    commander.setPermission(chatChannel.getPermissionNode(), false);
    chatChannel.removePlayer(commander, true);
    r.set(Result.SUCCESS, lang.getString("command_leavechannel_success", language, fieldChannel));
  }

  public ModuleChat getModule() {
    return this.module;
  }

  private void setModule(ModuleChat module) {
    this.module = module;
  }
}
