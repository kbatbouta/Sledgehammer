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

import sledgehammer.lua.chat.Command;
import sledgehammer.lua.core.Player;
import sledgehammer.util.Response;

/**
 * TODO: Document.
 * 
 * @author Jab
 *
 */
public interface CommandListener {

	String[] getCommands();

	public void onCommand(Command command, Response response);

	public String onTooltip(Player player, Command command);

	public String getPermissionNode(String command);
}
