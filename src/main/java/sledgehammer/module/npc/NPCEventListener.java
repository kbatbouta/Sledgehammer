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

import sledgehammer.annotations.EventHandler;
import sledgehammer.event.player.ConnectEvent;
import sledgehammer.interfaces.Listener;
import sledgehammer.lua.core.Player;
import zombie.core.raknet.UdpConnection;
import zombie.network.GameServer;
import zombie.sledgehammer.npc.NPC;

/**
 * EventListener to assist the NPCManager to send NPC player info to connecting Players, since NPCs
 * do not have a UDPConnection instance.
 *
 * <p>TODO: Rewrite NPCs and remove the NPCManager.
 *
 * @author Jab
 */
class NPCEventListener implements Listener {

  private ModuleNPC module;

  /**
   * Main constructor.
   *
   * @param module The npc module instance registering the listener.
   */
  public NPCEventListener(ModuleNPC module) {
    setModule(module);
  }

  @EventHandler(id = "core.npc.event.connect")
  public void on(ConnectEvent event) {
    Player commander = event.getPlayer();
    UdpConnection connection = commander.getConnection();
    for (NPC npc : getModule().getNPCs()) {
      GameServer.sendPlayerConnect(npc, connection);
    }
  }

  private ModuleNPC getModule() {
    return this.module;
  }

  private void setModule(ModuleNPC module) {
    this.module = module;
  }
}
