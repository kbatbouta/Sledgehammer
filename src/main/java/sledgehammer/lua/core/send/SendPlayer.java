/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 */
package sledgehammer.lua.core.send;

import sledgehammer.lua.Send;
import sledgehammer.lua.core.Player;

// @formatter:off
/**
 * Send Class for sending Player Objects.
 * 
 * Exports a LuaTable:
 * { 
 *   - "player": (LuaTable) The Player being sent.
 * }
 * 
 * @author Jab
 */
// @formatter:on
public class SendPlayer extends Send {

    /**
     * The Player Object to send.
     */
    private Player player;

    /**
     * Main constructor.
     */
    public SendPlayer() {
        super("core", "sendPlayer");
    }

    @Override
    public void onExport() {
        set("player", getPlayer());
    }

    /**
     * @return Returns the Player Object to send.
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Sets the Player Object to send.
     *
     * @param player The Player Object to set.
     */
    public void setPlayer(Player player) {
        this.player = player;
    }
}