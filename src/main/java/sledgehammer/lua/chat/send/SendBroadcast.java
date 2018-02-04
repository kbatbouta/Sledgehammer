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

package sledgehammer.lua.core.send;

import sledgehammer.lua.Send;
import sledgehammer.lua.core.Broadcast;

// @formatter:off
/**
 * Send Class for sending Broadcasts.
 *
 * <p>Exports a LuaTable: { - "broadcast": (LuaTable) The Broadcast being sent. }
 *
 * @author Jab
 */
// @formatter:on
public class SendBroadcast extends Send {

  /** The Broadcast LuaObject being packaged. */
  private Broadcast broadcast;

  private SendBroadcast sendBroadcast;

  /** Main constructor. */
  public SendBroadcast() {
    super("core.chat", "sendBroadcast");
  }

  @Override
  public void onExport() {
    set("broadcast", getBroadcast());
  }

  /** @return Returns the Broadcast being sent. */
  public Broadcast getBroadcast() {
    return this.broadcast;
  }

  /**
   * Sets the Broadcast being sent.
   *
   * @param broadcast The Broadcast to set.
   */
  public void setBroadcast(Broadcast broadcast) {
    this.broadcast = broadcast;
  }
}
