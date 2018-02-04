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

package sledgehammer.command;

import sledgehammer.Settings;
import sledgehammer.annotations.CommandHandler;
import sledgehammer.interfaces.Listener;
import sledgehammer.language.Language;
import sledgehammer.language.LanguagePackage;
import sledgehammer.lua.core.Player;
import sledgehammer.util.ClassUtil;
import sledgehammer.util.Command;
import sledgehammer.util.Response;

import java.lang.reflect.Method;

/**
 * This class is a utility for Listeners with command handlers that defines the command messages in
 * a language package distributed with the module, or custom defined.
 *
 * <p>The utility that comes with this class is the 'onCommandHelp' function which is itself a
 * command handler. This method walks through all declared methods for the instance and determines
 * if the commanding player given can use the command handler. Based on that, the command handler
 * sends the tooltip message defined in the language file.
 *
 * <p>tooltips should be formatted as such: "tooltip_command_[command]". the YAML definition should
 * use underlines and not spaces for commands with spaces. E.G: "tooltip_command_permissions_set"
 *
 * <p>Make sure that the command defined for the tooltip is the first one in a multi-command-defined
 * command handler.
 *
 * @author Jab
 */
public class CommandListener implements Listener {

  private LanguagePackage languagePackage;

  public CommandListener(LanguagePackage lang) {
    if (lang == null) {
      throw new IllegalArgumentException("LanguagePackage given is null.");
    }
    setLanguagePackage(lang);
  }

  /**
   * This is one way of handling help. A plug-in can define and append commands explicitly. This
   * method simply gathers all declared command handlers and checks the permissions defined so that
   * the result will be pruned to commands that the commanding player can use.
   *
   * <p>tooltips should be formatted as such: "tooltip_command_[command]". the YAML definition
   * should use underlines and not spaces for commands with spaces. E.G:
   * "tooltip_command_permissions_set"
   *
   * <p>Make sure that the command defined for the tooltip is the first one in a
   * multi-command-defined command handler.
   */
  @CommandHandler(command = "help", ignoreHandled = true)
  private void onCommandHelp(Command c, Response r) {
    Player commander = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commander.getLanguage();
    Method[] methods = ClassUtil.getAllDeclaredMethods(this.getClass());
    for (Method method : methods) {
      boolean isHelp = false;
      CommandHandler commandHandler = method.getAnnotation(CommandHandler.class);
      if (commandHandler != null) {
        String[] permissions = commandHandler.permission();
        if (commander.hasPermission(permissions)) {
          String[] commands = commandHandler.command();
          if (commands.length > 0) {
            for (String command : commands) {
              if (command.equalsIgnoreCase("help")) {
                isHelp = true;
                break;
              }
            }
            if (isHelp) {
              continue;
            }
            String command = commands[0];
            String langQuery = "tooltip_command_" + command.replaceAll(" ", "_").toLowerCase();
            if (Settings.getInstance().isDebug()) {
              System.out.println("help: looking up command: " + command + " : " + langQuery);
            }
            String langResult = lang.getString(langQuery, language);
            r.appendLine("[" + command + "]" + (langResult != null ? " : " + langResult : ""));
          }
        }
      }
    }
  }

  /** @return Returns the language package defined for the command handler. */
  public LanguagePackage getLanguagePackage() {
    return this.languagePackage;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the language package defined for the command handler.
   *
   * @param languagePackage The language package to set.
   */
  public void setLanguagePackage(LanguagePackage languagePackage) {
    this.languagePackage = languagePackage;
  }
}
