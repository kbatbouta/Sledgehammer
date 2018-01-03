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
package sledgehammer.npc.action;

import sledgehammer.util.Printable;
import zombie.sledgehammer.npc.NPC;

/**
 * Abstract Class to execute Actioned tasks for <NPC>'s.
 * 
 * @author Jab
 */
public abstract class Action extends Printable {

	/**
	 * Executes an Action for a given <NPC>.
	 * 
	 * @param npc
	 *            The <NPC> to act.
	 * @return Returns true if the <Action> is successful.
	 */
	public abstract boolean act(NPC npc);
}