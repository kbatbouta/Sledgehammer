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
import sledgehammer.annotations.EventHandler;
import sledgehammer.event.player.DeathEvent;
import sledgehammer.event.player.DisconnectEvent;
import sledgehammer.event.player.PlayerJoinEvent;
import sledgehammer.event.player.PlayerQuitEvent;
import sledgehammer.event.player.pvp.PVPKillEvent;
import sledgehammer.interfaces.Listener;
import sledgehammer.lua.core.Player;
import zombie.GameTime;

// Imports chat colors for short-hand.
import static sledgehammer.util.ChatTags.*;

/**
 * TODO: Document.
 *
 * @author Jab
 */
public class CoreEventListener implements Listener {

  private ModuleCore module;

  public CoreEventListener(ModuleCore module) {
    this.module = module;
  }

  @EventHandler(id = "core.event.join", priority = 1)
  private void on(PlayerJoinEvent event) {}

  @EventHandler(id = "core.event.quit", priority = 1)
  private void on(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    SledgeHammer.instance.getPlayerManager().removePlayer(player);
  }

  @EventHandler(id = "core.event.disconnect", priority = 1)
  private void on(DisconnectEvent event) {
    Player player = event.getPlayer();
    if (player != null) {
      SledgeHammer.instance.getPlayerManager().removePlayer(player);
    }
  }

  @EventHandler(id = "core.event.death", priority = 1)
  private void on(DeathEvent event) {
    module.sendGlobalMessage(COLOR_RED + " " + event.getLogMessage());
    GameTime.getInstance().thunderStart(true);
  }

  @EventHandler(id = "core.event.pvp.kill", priority = 1)
  private void on(PVPKillEvent event) {
    module.sendGlobalMessage(COLOR_RED + " " + event.getLogMessage());
    GameTime.getInstance().thunderStart(true);
  }
}
