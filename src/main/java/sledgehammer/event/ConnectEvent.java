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
package sledgehammer.event;

import sledgehammer.lua.core.Player;

/**
 * PlayerEvent that is passed when a Player connected to the PZ server.
 *
 * @author Jab
 */
public class ConnectEvent extends PlayerEvent {

    /**
     * The String ID of the Event.
     */
    public static final String ID = "ConnectEvent";

    /**
     * Main constructor.
     *
     * @param player The Player connecting to the PZ server.
     */
    public ConnectEvent(Player player) {
        super(player);
    }

    @Override
    public String getLogMessage() {
        return getPlayer().getUsername() + " connected.";
    }

    @Override
    public String getID() {
        return ID;
    }
}