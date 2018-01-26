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

package sledgehammer.module.npc;

import sledgehammer.annotations.CommandHandler;
import sledgehammer.enums.Result;
import sledgehammer.interfaces.Listener;
import sledgehammer.language.Language;
import sledgehammer.language.LanguagePackage;
import sledgehammer.lua.core.Player;
import sledgehammer.npc.behavior.BehaviorSurvive;
import sledgehammer.util.Command;
import sledgehammer.util.Response;
import sledgehammer.util.ZUtil;
import zombie.characters.IsoPlayer;
import zombie.iso.IsoGridSquare;
import zombie.network.ServerMap;
import zombie.sledgehammer.npc.NPC;

/**
 * CommandListener to handle NPC Commands for the NPC Module for the Core plug-in.
 *
 * <p>TODO: Rewrite the NPC Module.
 *
 * @author Jab
 */
public class NPCCommandListener implements Listener {

  /** The ModuleNPC instance using the CommandListener. */
  private ModuleNPC module;

  /**
   * Main constructor.
   *
   * @param module The ModuleNPC instance using the CommandListener.
   */
  public NPCCommandListener(ModuleNPC module) {
    setModule(module);
  }

  @CommandHandler(command = "addnpc", permission = "core.npc.command.addnpc")
  public void onCommandAddNPC(Command c, Response r) {
    Player commader = c.getPlayer();
    LanguagePackage lang = getLanguagePackage();
    Language language = commader.getLanguage();
    String[] args = c.getArguments();
    if (args.length == 1) {
      IsoPlayer player = c.getPlayer().getIso();
      IsoGridSquare square = null;
      float x = 0, y = 0, z = 0;
      if (player != null) {
        int attempts = 0;
        int maxAttempts = 50;
        while (square == null) {
          x = player.x + ZUtil.random.nextInt(11) - 5;
          y = player.y + ZUtil.random.nextInt(11) - 5;
          z = player.z;
          square =
              ServerMap.instance.getGridSquare(
                  (int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
          if (attempts >= maxAttempts) {
            x = player.x;
            y = player.y;
            z = player.z;
            square =
                ServerMap.instance.getGridSquare(
                    (int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
            if (square == null) {
              r.set(Result.FAILURE, "Could not find solid ground to spawn NPC on.");
              return;
            }
          }
          attempts++;
        }
      }
      String name = args[0];
      NPC fakePlayer = module.createFakePlayer(name, x, y, z);
      System.out.println(
          "Adding fake player \""
              + name
              + " at ("
              + x
              + ","
              + y
              + ","
              + z
              + "). PlayerIndex: "
              + fakePlayer.PlayerIndex
              + " OnlineID: "
              + fakePlayer.OnlineID);
      BehaviorSurvive behavior = new BehaviorSurvive(fakePlayer);
      behavior.setDefaultTarget(player);
      behavior.setActive(true);
      fakePlayer.addBehavior(behavior);
      module.mapSpawns.put(fakePlayer, player);
      r.set(Result.SUCCESS, "NPC created.");
    } else {
      r.set(Result.FAILURE, lang.getString("tooltip_command_addnpc", language));
    }
  }

  @CommandHandler(command = "destroynpcs", permission = "core.npc.command.destroynpcs")
  public void onCommandDestroyNPCS(Command c, Response r) {
    getModule().destroyNPCs();
    r.set(Result.SUCCESS, "NPCs destroyed.");
  }

  public LanguagePackage getLanguagePackage() {
    return getModule().getLanguagePackage();
  }

  /** @return Returns the ModuleNPC instance using the CommandListener. */
  public ModuleNPC getModule() {
    return this.module;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the ModuleNPC instance using the CommandListener.
   *
   * @param module The ModuleNPC instance to set.
   */
  private void setModule(ModuleNPC module) {
    this.module = module;
  }
}
