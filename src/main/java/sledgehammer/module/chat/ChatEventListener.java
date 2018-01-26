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
import sledgehammer.annotations.EventHandler;
import sledgehammer.database.module.core.SledgehammerDatabase;
import sledgehammer.event.core.player.DisconnectEvent;
import sledgehammer.interfaces.Listener;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.core.Player;

public class ChatEventListener implements Listener {

  private ModuleChat module;

  ChatEventListener(ModuleChat module) {
    setModule(module);
  }

  @EventHandler(id = "core.chat.event.disconnect")
  private void on(DisconnectEvent event) {
    Player player = event.getPlayer();
    if (player != null) {
      for (ChatChannel channel : getModule().getChatChannels()) {
        channel.removePlayer(player, false);
      }
      SledgehammerDatabase database = SledgeHammer.instance.getDatabase();
      database.removeMongoPlayer(player.getMongoDocument());
    }
  }

  public ModuleChat getModule() {
    return this.module;
  }

  private void setModule(ModuleChat module) {
    this.module = module;
  }
}
