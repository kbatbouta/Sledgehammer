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

package sledgehammer.interfaces;

import sledgehammer.event.Event;

/**
 * Interface for listening to and handling Events in the Sledgehammer engine.
 * 
 * TODO: Rewrite to support Annotation assignment.
 * 
 * @author Jab
 */
public interface EventListener {

	/**
	 * @return Returns the String IDs of the Events to pass to the Listener.
	 */
	String[] getTypes();

	/**
	 * Handles an Event passed to the Listener.
	 * 
	 * @param event
	 *            The Event passed.
	 */
	void onEvent(Event event);

	/**
	 * @return Returns true if the Listener runs after all Listeners that are
	 *         primary have processed the Event.
	 */
	boolean runSecondary();
}
