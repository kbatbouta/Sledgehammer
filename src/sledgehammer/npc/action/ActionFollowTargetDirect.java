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

import sledgehammer.module.npc.ModuleNPC;
import zombie.characters.IsoPlayer;
import zombie.iso.IsoObject;
import zombie.network.GameServer;
import zombie.sledgehammer.npc.NPC;

/**
 * Action that executes <NPC>'s following a targeted location directly.
 * 
 * @author Jab
 */
public class ActionFollowTargetDirect extends Action {

	/** The <String> name of the <Action>. */
	public static final String NAME = "Action->FollowTargetDirect";

	@Override
	public boolean act(NPC npc) {
		IsoObject primaryFollowTarget = npc.getTarget();
		IsoObject secondaryFollowTarget = npc.getDefaultTarget();
		if (primaryFollowTarget != null && primaryFollowTarget instanceof IsoPlayer) {
			if (!GameServer.Players.contains(((IsoPlayer) primaryFollowTarget))) {
				if (ModuleNPC.DEBUG) {
					println("NPC: Following target disconnected.");
				}
				npc.setTarget(null);
				if (secondaryFollowTarget == null) {
					npc.setFollow(false);
					// The default target is null too. No need to follow. Returning true to stop
					// loop.
					return true;
				}
			}
		}
		if (secondaryFollowTarget != null && secondaryFollowTarget instanceof IsoPlayer) {
			if (!GameServer.Players.contains(((IsoPlayer) secondaryFollowTarget))) {
				if (ModuleNPC.DEBUG) {
					println("NPC: Default following target disconnected.");
				}
				npc.setFollow(false);
				npc.setDefaultTarget(null);
				// The default target is null too. No need to follow. Returning true to stop
				// loop.
				return true;
			}
		}
		IsoObject focusTarget = primaryFollowTarget;
		if (focusTarget == null)
			focusTarget = secondaryFollowTarget;
		if (focusTarget == null) {
			npc.setFollow(false);
			// The default target is null too. No need to follow. Returning true to stop
			// loop.
			return true;
		}
		long timeNow = System.currentTimeMillis();
		long delta = timeNow - npc.getLastActionTime();
		if (delta >= 500L) {
			npc.faceDirection(focusTarget);
			npc.setActionTime(timeNow);
		}
		npc.setDestination(focusTarget);
		float distanceFromTarget = npc.DistTo(focusTarget);
		float speed = npc.getPathSpeed();
		boolean foundSpeed = false;
		if (!foundSpeed && npc.canRun() && distanceFromTarget > npc.getDistanceToRun()) {
			foundSpeed = true;
			npc.playAnimation(npc.getRunAnimation());
			npc.setRunning(true);
		} else if (!foundSpeed && npc.canWalk() && distanceFromTarget > npc.getDistanceToWalk()) {
			foundSpeed = true;
			npc.setRunning(false);
			npc.playAnimation(npc.getWalkAnimation());
		} else if (distanceFromTarget <= npc.getArrivalRadius()) {
			speed = 0.0F;
			npc.setRunning(false);
			npc.playAnimation(npc.getIdleAnimation());
			npc.setArrived(true);
			// The NPC has arrived. Returning true to stop loop.
			return true;
		}
		npc.moveForward(speed);
		return false;
	}

	@Override
	public String getName() {
		return NAME;
	}
}