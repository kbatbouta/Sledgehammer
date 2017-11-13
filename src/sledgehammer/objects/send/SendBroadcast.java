package sledgehammer.objects.send;

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

import sledgehammer.objects.chat.Broadcast;

/**
 * Class designed to package Broadcast LuaObjects.
 * @author Jab
 * 
 */
public class SendBroadcast extends Send {

	/**
	 * The Broadcast LuaObject being packaged.
	 */
	private Broadcast broadcast;
	
	/**
	 * Main constructor.
	 * @param broadcast
	 */
	public SendBroadcast(Broadcast broadcast) {
		super("core", "sendBroadcast");
		
		// Set variable(s).
		setBroadcast(broadcast);
	}

	@Override
	public void onExport() {
		set("broadcast", getBroadcast());
	}

	/**
	 * Returns the Broadcast LuaObject packaged.
	 * @return
	 */
	public Broadcast getBroadcast() {
		return this.broadcast;
	}

	/**
	 * Sets the Broadcast LuaObject to be packaged.
	 * @param broadcast
	 */
	public void setBroadcast(Broadcast broadcast) {
		this.broadcast = broadcast;
	}
}
