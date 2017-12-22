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
package sledgehammer.module.core;

import sledgehammer.event.ClientEvent;
import sledgehammer.event.Event;
import sledgehammer.interfaces.EventListener;

/**
 * TODO: Document.
 * 
 * @author Jab
 */
public class CoreClientListener implements EventListener {

	ModuleCore module;

	public CoreClientListener(ModuleCore module) {
		this.module = module;
	}

	public String[] getTypes() {
		return new String[] { ClientEvent.ID };
	}

	public void handleEvent(Event e) {
		// // Cast to proper Event sub-class.
		// ClientEvent event = (ClientEvent) e;
		//
		// // Get event content.
		// String module = event.getModule();
		// String command = event.getCommand();
		// Player player = event.getPlayer();
		//
		// if (module.equalsIgnoreCase("sledgehammer")) {
		//
		// if (command.equalsIgnoreCase("handshake")) {
		//
		// // We just want to ping back to the client saying we received the request.
		// event.respond();
		//
		// // Create a HandShakeEvent.
		// HandShakeEvent handshakeEvent = new HandShakeEvent(player);
		//
		// // Handle the event.
		// SledgeHammer.instance.handle(handshakeEvent);
		// }
		//
		//
		// } else if(module.equalsIgnoreCase("core")) {
		//
		// if(command.equalsIgnoreCase("requestInfo")) {
		//
		// RequestInfo info = new RequestInfo();
		// info.setSelf(player);
		//
		// event.respond(info);
		// }
		// }
	}

	@Override
	public boolean runSecondary() {
		// TODO Auto-generated method stub
		return false;
	}
}
