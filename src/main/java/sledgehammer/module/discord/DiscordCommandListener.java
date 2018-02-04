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

import sledgehammer.annotations.CommandHandler;
import sledgehammer.enums.Result;
import sledgehammer.command.CommandListener;
import sledgehammer.language.Language;
import sledgehammer.language.LanguagePackage;
import sledgehammer.lua.core.Player;
import sledgehammer.util.Command;
import sledgehammer.util.Response;

public class DiscordCommandListener extends CommandListener {

  private ModuleDiscord module;

  DiscordCommandListener(ModuleDiscord module) {
    super(module.getLanguagePackage());
    setModule(module);
  }

  @CommandHandler(command = "discord", permission = "core.discord.command.discord")
  public void onCommandDiscord(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    String command;
    String[] args = c.getArguments();
    if (args.length != 1) {
      r.set(Result.FAILURE, lang.getString("tooltip_command_discord", language));
      return;
    }
    command = args[0];
    if (command.equalsIgnoreCase("start")) {
      module.onStart();
      r.set(Result.SUCCESS, "Starting the Discord bot.");
    } else if (command.equalsIgnoreCase("stop")) {
      module.onStop();
      r.set(Result.SUCCESS, "Stopping the Discord bot.");
    } else {
      r.set(Result.FAILURE, lang.getString("tooltip_command_discord", language));
    }
  }

  public ModuleDiscord getModule() {
    return this.module;
  }

  private void setModule(ModuleDiscord module) {
    this.module = module;
  }
}
