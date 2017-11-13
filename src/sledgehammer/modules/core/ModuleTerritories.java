package sledgehammer.modules.core;

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

import sledgehammer.event.ClientEvent;
import sledgehammer.module.Module;

public class ModuleTerritories extends Module {

	public static final String ID      = "ModuleTerritories";
	public static final String NAME    = "Territories";
	public static final String MODULE  = "Territories";
	public static final String VERSION = "1.00";
	
	private TerritoriesEventListener eventListener;
	
	@Override
	public void onLoad() {
		eventListener = new TerritoriesEventListener(this);
		
	}

	@Override
	public void onStart() {
		register(eventListener);
	}

	@Override
	public void onUpdate(long delta) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStop() {
		unregister(eventListener);
	}

	@Override
	public void onUnload() {
		eventListener = null;
	}

	public String getID()         { return ID;      }
	public String getName()       { return NAME;    }
	public String getModuleName() { return MODULE;  }
	public String getVersion()    { return VERSION; }

	public void onClientCommand(ClientEvent e) {}
}
