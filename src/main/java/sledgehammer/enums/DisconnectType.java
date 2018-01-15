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

package sledgehammer.enums;

public enum DisconnectType {
    // @formatter:off
	DISCONNECT_EXITED_GAME(0),
	DISCONNECT_SERVER_FULL(1),
	DISCONNECT_USERNAME_ALREADY_LOGGED_IN(2),
	DISCONNECT_USERNAME_EMPTY(3),
	DISCONNECT_USERNAME_BANNED(4),
	DISCONNECT_KICKED(5),
	DISCONNECT_IP_BANNED(6),
	DISCONNECT_STEAM_BANNED(7),
	DISCONNECT_MISC(8);
	// @formatter:on

    /**
     * The id of the DisconnectType.
     */
    private int id;

    /**
     * Main constructor.
     *
     * @param id The id of the DisconnectType.
     */
    DisconnectType(int id) {
        setId(id);
    }

    /**
     * @return Returns the id of the DisconnectType.
     */
    public int getId() {
        return this.id;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the id of the DisconnectType.
     *
     * @param id The id to set.
     */
    private void setId(int id) {
        this.id = id;
    }
}