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

/*

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
import sledgehammer.event.ChatMessageEvent;
import sledgehammer.event.Event;
import sledgehammer.event.HandShakeEvent;
import sledgehammer.interfaces.EventListener;

/**
 * TODO: Document.
 * 
 * @author Jab
 */
public class ChatEventListener implements EventListener {

	@Override
	public String[] getTypes() {
		return new String[] { ChatMessageEvent.ID, HandShakeEvent.ID };
	}

	@Override
	public void handleEvent(Event event) {
	}

	@Override
	public boolean runSecondary() {
		// TODO Auto-generated method stub
		return false;
	}

}
