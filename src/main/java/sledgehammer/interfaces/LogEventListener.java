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
package sledgehammer.interfaces;

import sledgehammer.event.LogEvent;

/**
 * Listener to handle Logged events passed through the Sledgehammer engine.
 * 
 * TODO: Re-write to have a more modular design. This is too rigid of a model,
 * and should be cohesive to the EventListener model.
 * 
 * @author Jab
 */
public interface LogEventListener {

	/**
	 * Handles a LogEvent.
	 * 
	 * @param logEvent
	 *            The LogEvent to handle.
	 */
	void onLogEvent(LogEvent logEvent);
}
