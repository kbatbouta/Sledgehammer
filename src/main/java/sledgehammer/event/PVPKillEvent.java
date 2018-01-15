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
 * Event to dispatch when a Player kills another Player.
 *
 * @author Jab
 */
public class PVPKillEvent extends Event {

    /**
     * The String ID of the Event.
     */
    public static final String ID = "PVPKillEvent";

    /**
     * The Player killed by the other Player.
     */
    private Player playerKiller;
    /**
     * The Player killing the other Player.
     */
    private Player playerKilled;

    /**
     * Main constructor.
     *
     * @param playerKiller The Player that killed the other Player.
     * @param playerKilled The Player killed by the other Player.
     */
    public PVPKillEvent(Player playerKiller, Player playerKilled) {
        super();
        setKiller(playerKiller);
        setKilled(playerKilled);
    }

    @Override
    public String getLogMessage() {
        String playerKillerName = "Unknown Player (Null)";
        String playerKilledName = "Unknown Player (Null)";
        if (playerKiller != null)
            playerKillerName = playerKiller.getUsername();
        if (playerKilled != null)
            playerKilledName = playerKilled.getUsername();
        return playerKillerName + " killed " + playerKilledName + '.';
    }

    @Override
    public String getID() {
        return ID;
    }

    /**
     * @return Returns the Player killing the other Player.
     */
    public Player getKiller() {
        return this.playerKiller;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the Player killing the other Player.
     *
     * @param playerKiller The Player to set.
     */
    private void setKiller(Player playerKiller) {
        this.playerKiller = playerKiller;
    }

    /**
     * @return Returns the Player killed by the other Player.
     */
    public Player getKilled() {
        return this.playerKilled;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the Player killed by the other Player.
     *
     * @param playerKilled The Player to set.
     */
    private void setKilled(Player playerKilled) {
        this.playerKilled = playerKilled;
    }

}
