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

package sledgehammer.event.core.player;

import sledgehammer.lua.core.Player;

public class PlayerJoinEvent extends PlayerEvent {

  public static final String ID = "PlayerJoinEvent";

  private long timestamp = -1L;

  /**
   * Main constructor.
   *
   * @param player The Player associated with the PlayerEvent.
   */
  public PlayerJoinEvent(Player player) {
    super(player);
    setTimestamp(System.currentTimeMillis());
  }

  @Override
  public String getLogMessage() {
    return "The Player " + getPlayer().getName() + " joined to the server.";
  }

  @Override
  public String getID() {
    return PlayerJoinEvent.ID;
  }

  public long getTimestamp() {
    return this.timestamp;
  }

  private void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
