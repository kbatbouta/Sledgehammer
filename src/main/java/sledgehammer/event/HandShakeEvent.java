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

package sledgehammer.event;

import sledgehammer.lua.core.Player;

/**
 * PlayerEvent to handle dispatching the SledgehammerLua handshake with the
 * Sledgehammer engine and the registered Modules listening for the
 * HandshakeEvent.
 *
 * @author Jab
 */
public class HandShakeEvent extends PlayerEvent {

    /**
     * The String ID of the Event.
     */
    public static final String ID = "HandShakeEvent";

    /**
     * Main constructor.
     *
     * @param player The Player hand-shaking with the server.
     */
    public HandShakeEvent(Player player) {
        super(player);
    }

    @Override
    public String getLogMessage() {
        return null;
    }

    @Override
    public String getID() {
        return ID;
    }
}