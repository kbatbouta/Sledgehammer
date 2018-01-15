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
 * Event to store and manage logging data for other Events, as well as any
 * additional logging pushed from registered Modules in the Sledgehammer
 * engine.
 *
 * @author Jab
 */
public class LogEvent extends Event {

    /**
     * The String ID of the Event.
     */
    public static final String ID = "LogEvent";

    /**
     * The Player being logged. (Optional)
     */
    private Player player;
    /**
     * The Event being logged.
     */
    private Event event;
    /**
     * The String message being logged.
     */
    private String message;
    /**
     * Flag to signify the importance of the LogEvent.
     */
    private boolean importance;

    /**
     * Main constructor.
     *
     * @param event The Event being logged.
     */
    public LogEvent(Event event) {
        super();
        setEvent(event);
        this.message = event.getLogMessage();
        this.importance = false;
    }

    @Override
    public String getLogMessage() {
        return this.message;
    }

    @Override
    public String getID() {
        return ID;
    }

    /**
     * @return Returns the Player of the LogEvent, if set. Otherwise returns
     * null.
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Sets the Player of the LogEvent.
     *
     * @param player The Player to set.
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * @return Returns the Event being logged.
     */
    public Event getEvent() {
        return this.event;
    }

    /**
     * Sets the Event being logged.
     *
     * @param event The Event to set.
     */
    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * @return Returns true if the LogEvent is important.
     */
    public boolean isImportant() {
        return this.importance;
    }

    /**
     * Sets the importance of the LogEvent
     *
     * @param flag The flag to set.
     */
    public void setImportant(boolean flag) {
        this.importance = flag;
    }
}