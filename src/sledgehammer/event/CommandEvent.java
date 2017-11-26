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

import sledgehammer.objects.chat.Command;
import sledgehammer.util.Response;

/**
 * TODO: Document.
 * 
 * @author Jab
 *
 */
public class CommandEvent extends Event {

	public static final String ID = "CommandEvent";

	private Command command;

	private Response response;

	public CommandEvent(Command command) {
		this.command = command;
		this.response = new Response();
	}

	public Command getCommand() {
		return this.command;
	}

	public Response getResponse() {
		return response;
	}

	public boolean isHandled() {
		return getResponse().isHandled();
	}

	public String getID() {
		return ID;
	}

	public String getLogMessage() {
		return getResponse().getLogMessage();
	}

}
