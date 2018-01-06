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

/**
 * Class designed to handle common event operations and utilities for
 * Sledgehammer's EventManager.
 *
 * @author Jab
 */
public abstract class Event {

    /**
     * The String ID of the Event.
     */
    public static final String ID = "Event";

    /**
     * The <Long> time-stamp for when the Event is created.
     */
    private long timeStamp = 0L;
    /**
     * Flag for if the Event has been handled by an EventListener.
     */
    private boolean handled = false;
    /**
     * Flag for whether or not to announce the Event.
     */
    private boolean announce = false;
    /**
     * Flag for whether or not to ignore the Core plug-in's EventListeners.
     */
    private boolean ignoreCore = false;
    /**
     * Flag for if the Event is cancelled.
     */
    private boolean canceled = false;

    /**
     * Main constructor.
     */
    public Event() {
        timeStamp = System.currentTimeMillis();
    }

    /**
     * Sets the handled flag for the Event.
     *
     * @param handled The flag to set.
     */
    public void setHandled(boolean handled) {
        this.handled = handled;
    }

    /**
     * @return Returns true if the Event is handled.
     */
    public boolean handled() {
        return this.handled;
    }

    /**
     * Sets the announcement flag for the Event.
     *
     * @param announce The flag to set.
     */
    public void announce(boolean announce) {
        this.announce = true;
    }

    /**
     * @return Returns true if the Event should be announced.
     */
    public boolean shouldAnnounce() {
        return this.announce;
    }

    /**
     * @return Returns the Long time-stamp when the Event was created.
     */
    public long getTimeStamp() {
        return this.timeStamp;
    }

    /**
     * @return Returns true if the Event has been cancelled.
     */
    public boolean canceled() {
        return this.canceled;
    }

    /**
     * Sets the Event cancelled.
     *
     * @param canceled The flag to set.
     */
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    /**
     * @return Returns true if the Core plug-in's EventListeners are ignored by
     * this Event.
     */
    public boolean ignoreCore() {
        return ignoreCore;
    }

    /**
     * Sets the Event to ignore the Core plug-in's EventListeners.
     *
     * @param flag The flag to set.
     */
    public void setIgnoreCore(boolean flag) {
        ignoreCore = flag;
    }

    /**
     * @return Returns the String logged message for the Event. If null is
     * returned, the Event is not logged.
     */
    public abstract String getLogMessage();

    /**
     * @return Returns the static String ID of the Event for identity purposes.
     */
    public abstract String getID();
}