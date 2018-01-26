/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

package sledgehammer.npc.action;

import zombie.ai.astar.Mover;
import zombie.ai.astar.Path;
import zombie.sledgehammer.npc.NPC;

/**
 * Action that handles native Path and Mover operations for NPCs.
 *
 * @author Jab
 */
public abstract class PathAction extends Action {

  /**
   * Fired when the NPC has successfully reached the destination of the Path.
   *
   * @param npc The NPC to act.
   * @param mover The Mover that is executing the path-finding process.
   * @param path The Path that is projected for the NPC to move.
   */
  public void onPathSuccess(NPC npc, Mover mover, Path path) {}

  /**
   * Fired when the NPC fails to reach the destination of the Path.
   *
   * @param npc The NPC to act.
   * @param mover The Mover that is executing the path-finding process.
   */
  public void onPathFailure(NPC npc, Mover mover) {}
}
