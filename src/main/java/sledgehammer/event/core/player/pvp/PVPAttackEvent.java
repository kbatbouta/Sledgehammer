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

package sledgehammer.event.core.player.pvp;

import sledgehammer.event.Event;
import sledgehammer.lua.core.Player;

/**
 * Event to dispatch when a Player attacks another Player.
 *
 * @author Jab
 */
public class PVPAttackEvent extends Event {

    /**
     * The String ID of the Event.
     */
    public static final String ID = "PVPAttackEvent";

    /**
     * The Player attacking the other Player.
     */
    private Player playerAttacking;
    /**
     * The Player being attacked by the other Player.
     */
    private Player playerAttacked;
    /**
     * The String name of the weapon used.
     */
    private String weaponName;

    /**
     * Main constructor.
     *
     * @param playerAttacking The Player attacking.
     * @param playerAttacked  The Player being attacked.
     * @param weaponName      The String name of the weapon used.
     */
    public PVPAttackEvent(Player playerAttacking, Player playerAttacked, String weaponName) {
        super();
        setPlayerAttacking(playerAttacking);
        setPlayerAttacked(playerAttacked);
        setWeaponName(weaponName);
    }

    @Override
    public String getLogMessage() {
        return playerAttacking.getUsername() + " is attacking " + playerAttacked.getUsername() + ".";
    }

    @Override
    public String getID() {
        return ID;
    }

    /**
     * @return Returns the Player attacking the other Player.
     */
    public Player getPlayerAttacking() {
        return this.playerAttacking;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the Player attacking.
     *
     * @param playerAttacking The Player to set.
     */
    private void setPlayerAttacking(Player playerAttacking) {
        this.playerAttacking = playerAttacking;
    }

    /**
     * @return Returns the Player being attacked by the other Player.
     */
    public Player getPlayerAttacked() {
        return this.playerAttacked;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the Player being attacked.
     *
     * @param playerAttacked The Player to set.
     */
    private void setPlayerAttacked(Player playerAttacked) {
        this.playerAttacked = playerAttacked;
    }

    /**
     * @return Returns the String name of the weapon used to attack the Player.
     */
    public String getWeaponName() {
        return this.weaponName;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the String weapon name used to attack the Player.
     *
     * @param weaponName The String name to set.
     */
    private void setWeaponName(String weaponName) {
        this.weaponName = weaponName;
    }
}