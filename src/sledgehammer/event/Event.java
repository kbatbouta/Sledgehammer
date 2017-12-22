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

public abstract class Event {

	public static final String ID = "Event";

	private boolean handled = false;
	private boolean announce = false;
	private boolean ignoreCore = false;
	long timeStamp = 0L;
	private boolean canceled = false;

	public Event() {
		timeStamp = System.currentTimeMillis();
	}

	public abstract String getLogMessage();

	public void setHandled(boolean handled) {
		this.handled = handled;
	}

	public boolean handled() {
		return this.handled;
	}

	public void announce(boolean announce) {
		this.announce = true;
	}

	public boolean shouldAnnounce() {
		return this.announce;
	}

	public long getTimeStamp() {
		return this.timeStamp;
	}

	public boolean canceled() {
		return this.canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public boolean ignoreCore() {
		return ignoreCore;
	}

	public void setIgnoreCore(boolean flag) {
		ignoreCore = flag;
	}

	public abstract String getID();

}
