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

/**
 * Class designed to handle common event operations and utilities for Sledgehammer's
 * EventManagerOld.
 *
 * @author Jab
 */
public abstract class Event {

  /** The <Long> time-stamp for when the Event is created. */
  private long timeStamp;
  /** Flag for if the Event has been handled by an EventListener. */
  private boolean handled = false;
  /** Flag for whether or not to announce the Event. */
  private boolean announce = false;
  /** Flag for whether or not to ignore the Core plug-in's EventListeners. */
  private boolean ignoreCore = false;

  /** Main constructor. */
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

  /** @return Returns true if the Event is handled. */
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

  /** @return Returns true if the Event should be announced. */
  public boolean shouldAnnounce() {
    return this.announce;
  }

  /** @return Returns the Long time-stamp when the Event was created. */
  public long getTimeStamp() {
    return this.timeStamp;
  }

  /**
   * @return Returns the String logged message for the Event. If null is returned, the Event is not
   *     logged.
   */
  public String getLogMessage() {
    return null;
  }
}
