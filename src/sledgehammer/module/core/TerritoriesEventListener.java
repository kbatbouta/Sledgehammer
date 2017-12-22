package sledgehammer.module.core;

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

import sledgehammer.event.Event;
import sledgehammer.event.PlayerInfoEvent;
import sledgehammer.interfaces.EventListener;

public class TerritoriesEventListener implements EventListener {

	@SuppressWarnings("unused")
	private ModuleTerritories module;

	TerritoriesEventListener(ModuleTerritories module) {
		this.module = module;
	}

	@Override
	public String[] getTypes() {
		return new String[] { PlayerInfoEvent.ID };
	}

	@Override
	public void handleEvent(Event event) {
		if (event.getID() == PlayerInfoEvent.ID) {
			handlePlayerInfoEvent((PlayerInfoEvent) event);
		}
	}

	private void handlePlayerInfoEvent(PlayerInfoEvent event) {
	}

	@Override
	public boolean runSecondary() {
		return false;
	}
}
