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
import sledgehammer.event.core.command.CommandListener;
import sledgehammer.language.EntryField;
import sledgehammer.language.Language;
import sledgehammer.language.LanguagePackage;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.core.Player;
import sledgehammer.util.Command;
import sledgehammer.util.Response;

public class ChatCommandListener extends CommandListener {

  private static final String permissionNodeEspanol = "core.chat.channel.espanol";

  private ModuleChat module;

  ChatCommandListener(ModuleChat module) {
    super(module.getLanguagePackage());
    setModule(module);
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
