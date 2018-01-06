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
 * Abstract Event that associates a Player with Player-oriented Events in
 * the Sledgehammer engine.
 *
 * @author Jab
 */
public abstract class PlayerEvent extends Event {

    /**
     * The String ID of the Event.
     */
    public static final String ID = "PlayerEvent";

    /**
     * The Player associated with the PlayerEvent.
     */
    private Player player;

    /**
     * Main constructor.
     *
     * @param player The Player associated with the PlayerEvent.
     */
    public PlayerEvent(Player player) {
        super();
        if (player == null) {
            throw new IllegalArgumentException("Player is null!");
        }
        setPlayer(player);
    }

    /**
     * @return Returns the Player associated with the PlayerEvent.
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the Player associated with the PlayerEvent.
     *
     * @param player The Player to set.
     */
    private void setPlayer(Player player) {
        this.player = player;
    }
}