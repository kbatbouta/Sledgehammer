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

import sledgehammer.util.Command;
import sledgehammer.util.Response;

/**
 * Event that handles Commands sent to the Sledgehammer engine.
 *
 * @author Jab
 */
public class CommandEvent extends Event {

    /**
     * The String ID of the Event.
     */
    public static final String ID = "CommandEvent";

    /**
     * The Command sent to Sledgehammer.
     */
    private Command command;
    /**
     * The Response to send back.
     */
    private Response response;

    /**
     * Main constructor.
     *
     * @param command The Command sent to Sledgehammer.
     */
    public CommandEvent(Command command) {
        this.command = command;
        this.response = new Response();
    }

    @Override
    public String getLogMessage() {
        return getResponse().getLogMessage();
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String toString() {
        return "CommandEvent: \n"
                + "Command: " + command.toString() + "\n"
                + "Response: " + response.toString() + "\n";
    }

    /**
     * @return Returns the Command sent to Sledgehammer.
     */
    public Command getCommand() {
        return this.command;
    }

    /**
     * @return Returns the Response to send back.
     */
    public Response getResponse() {
        return response;
    }

    /**
     * @return Returns true if the CommandEvent is handled by a CommandListener.
     */
    public boolean isHandled() {
        return getResponse().isHandled();
    }
}