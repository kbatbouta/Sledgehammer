package zombie.sledgehammer.npc.action;

import zombie.ai.astar.Path;
import zombie.ai.astar.AStarPathFinder.PathFindProgress;
import zombie.characters.IsoPlayer;
import zombie.iso.IsoObject;
import zombie.iso.Vector2;
import zombie.network.GameServer;
import zombie.sledgehammer.npc.NPC;
import sledgehammer.modules.ModuleNPC;
import sledgehammer.npc.action.PathAction;

public class ActionFollowTargetPath extends PathAction {

	public static final String NAME = "PathAction->FollowTargetPath";

	@Override
	public boolean act(NPC npc) {

		IsoObject primaryFollowTarget = npc.getTarget();
		IsoObject secondaryFollowTarget = npc.getDefaultTarget();
		
		if(primaryFollowTarget != null && primaryFollowTarget instanceof IsoPlayer) {
			if(!GameServer.Players.contains(((IsoPlayer)primaryFollowTarget))) {
				
				if(ModuleNPC.DEBUG) {					
					println("NPC: Following target disconnected.");
				}
				
				npc.setTarget(null);
				
				if(secondaryFollowTarget == null) {
					npc.setArrived(true);
					npc.faceTarget();
					npc.setFollow(false);
					npc.playAnimation(npc.getIdleAnimation());
					// The default target is null too. No need to follow. Returning true to stop loop.
					return true;
				}
			}
		}
		
		IsoObject target = primaryFollowTarget;
		
		if(target == null) target = secondaryFollowTarget;			
		
		if(target == null) {
			npc.setArrived(true);
			npc.faceTarget();
			npc.setFollow(false);
			npc.playAnimation(npc.getIdleAnimation());
			// The default target is null too. No need to follow. Returning true to stop loop.
			return true;
		}
		
		if(npc.isAdjacentTo(target)) {
			npc.setArrived(true);
			npc.faceTarget();
			npc.playAnimation(npc.getIdleAnimation());
			return true;
		}
		
		if(secondaryFollowTarget != null && secondaryFollowTarget instanceof IsoPlayer) {
			if(!GameServer.Players.contains(((IsoPlayer)secondaryFollowTarget))) {
				
				if(ModuleNPC.DEBUG) {					
					println("NPC: Default following target disconnected.");
				}
				npc.setArrived(true);
				npc.faceTarget();
				npc.setFollow(false);
				npc.setDefaultTarget(null);
				npc.playAnimation(npc.getIdleAnimation());
				
				// The default target is null too. No need to follow. Returning true to stop loop.
				return true;
			}
		}

		PathFindProgress pathFlag = npc.getFinder().progress;

		// If the path failed, try again.
		if (pathFlag == PathFindProgress.failed || pathFlag == PathFindProgress.notrunning) {
			npc.faceTarget();
			return true;
		} else if (pathFlag == PathFindProgress.notyetfound) {
			return false;
		} else if (pathFlag == PathFindProgress.found) {
			Path path = npc.getPath();

			if (path != null) {

				//println("Path-Length: " + path.getLength());
				
				int pathIndex = npc.getPathIndex();
				
				// Reset path if finished.
				if (npc.getPathIndex() >= path.getLength()) {
					npc.setArrived(true);
					npc.faceTarget();
					npc.playAnimation(npc.getIdleAnimation());
					return true;
				}
				
				float speed = npc.getPathSpeed();
				
				int npcX = (int) npc.getX();
				int npcY = (int) npc.getY();
				int npcZ = (int) npc.getZ();
				
				int[] next = npc.getPathPosition();
				
				int nextX = next[0];
				int nextY = next[1];
				int nextZ = next[2];
				
				if(npcX == nextX && npcY == nextY && npcZ == nextZ) {
					npc.setPathIndex(pathIndex + 1);
					
					// Reset path if finished.
					if (npc.getPathIndex() >= path.getLength()) {
						npc.setArrived(true);
						npc.faceTarget();
						npc.playAnimation(npc.getIdleAnimation());
						return true;
					}
					
					nextX = path.getX(pathIndex);
					nextY = path.getY(pathIndex);
					nextZ = path.getZ(pathIndex);
					next[0] = nextX;
					next[1] = nextY;
					next[2] = nextZ;
				}
				
				
				//println("Path-" + pathIndex + ": (" + nextX + "," + nextY + "," + nextZ + ")");

				int targetX = (int) target.getX();
				int targetY = (int) target.getY();
				int targetZ = (int) target.getZ();
				
				if(npcX == targetX && npcY == targetY && npcZ == targetZ) {
					npc.setArrived(true);
					npc.faceTarget();
					return true;
				} else {
					Vector2 pathTarget = new Vector2();
					
					pathTarget.x = nextX;
					pathTarget.y = nextY;
					pathTarget.x -= npcX;
					pathTarget.y -= npcY;
					if (pathTarget.getLength() > 0.0F) {
						pathTarget.normalize();
					}
					
					float distanceFromTarget = npc.DistTo(target);
					
					boolean foundSpeed = false;
					
					if (!foundSpeed && npc.canRun() && distanceFromTarget > npc.getDistanceToRun()) {
						
						foundSpeed = true;
						
						npc.playAnimation(npc.getRunAnimation());
						
						npc.setRunning(true);
					
					} else if(!foundSpeed && npc.canWalk() && distanceFromTarget > npc.getDistanceToWalk()){
						
						foundSpeed = true;
						
						npc.setRunning(false);
						
						npc.playAnimation(npc.getWalkAnimation());				
					
					} else if(distanceFromTarget <= npc.getArrivalRadius()){
						println("Finish");
						npc.setRunning(false);
						npc.playAnimation(npc.getIdleAnimation());
						npc.setArrived(true);
						npc.faceTarget();
						// The NPC has arrived. Returning true to stop loop.
						return true;
					}
					
					npc.setDirection(pathTarget);
					npc.MoveForward(speed);
				}
				
			} else {
				if(ModuleNPC.DEBUG) {					
					println("Path is null. Returning");
				}
				return true;
			}
		}
		
		return false;
	}

	@Override
	public String getName() { return NAME; }
}
