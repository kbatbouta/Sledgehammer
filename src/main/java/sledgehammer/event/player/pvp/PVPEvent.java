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

package sledgehammer.event.player.pvp;

import sledgehammer.event.player.PlayerEvent;
import sledgehammer.lua.core.Player;

/**
 * PlayerEvent to dispatch When a Player is affected or Authors a PVPEvent.
 *
 * @author Jab
 */
public class PVPEvent extends PlayerEvent {

  /** Flag to note if PVP Mode is enabled. */
  private boolean pvpModeEnabled;

  /**
   * Main constructor.
   *
   * @param player The Player affected or authoring the PVPEvent.
   * @param pvpModeEnabled Flag to note if PVP Mode is enabled.
   */
  public PVPEvent(Player player, boolean pvpModeEnabled) {
    super(player);
    setPVPModeEnabled(pvpModeEnabled);
  }

  @Override
  public String getLogMessage() {
    return getPlayer().getUsername()
        + " "
        + (isPVPModeEnabled() ? "enabled" : "disabled")
        + " PVP.";
  }

  /** @return Returns true if PVP Mode is enabled. */
  public boolean isPVPModeEnabled() {
    return this.pvpModeEnabled;
  }

  /**
   * (Private Method)
   *
   * <p>Sets the flag for PVP Mode.
   *
   * @param pvpModeEnabled The flag to set.
   */
  private void setPVPModeEnabled(boolean pvpModeEnabled) {
    this.pvpModeEnabled = pvpModeEnabled;
  }
}
