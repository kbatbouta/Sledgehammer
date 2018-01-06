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

import sledgehammer.lua.core.Player;
import sledgehammer.util.Command;
import sledgehammer.util.Response;

/**
 * Interface that handles the registration and execution of Commands in the
 * Sledgehammer engine.
 *
 * @author Jab
 */
public interface CommandListener {

    /**
     * @return Returns a String Array of commands that are interpreted in the
     * CommandListener.
     */
    String[] getCommands();

    /**
     * Handles a Command.
     *
     * @param command  The Command to handle.
     * @param response The Response to apply the Result and any additional
     *                 information to pass back to the author of the Command.
     */
    void onCommand(Command command, Response response);

    /**
     * @param player  The Player requesting the Commands tool-tip description.
     * @param command The Command issued.
     * @return Returns a String tool-tip description. If the Player is not
     * permitted to use the Command issued, then null should be returned.
     */
    String onTooltip(Player player, Command command);

    /**
     * @param command The String command to resolve.
     * @return Returns a resolved String permission-node for the String command
     * given.
     */
    String getPermissionNode(String command);
}