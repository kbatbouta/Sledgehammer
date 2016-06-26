package sledgehammer.npc;

import zombie.ai.astar.Mover;
import zombie.ai.astar.Path;
import zombie.iso.IsoObject;
import zombie.iso.IsoUtils;
import zombie.iso.IsoWorld;
import zombie.iso.Vector2;

import static zombie.ai.astar.AStarPathFinder.*;

import zombie.GameTime;
import zombie.SandboxOptions;

public class ActionMoveToLocationAStar extends PathAction {

	public static final String NAME = "PathAction->MoveToLocationAStar";

	@Override
	public boolean act(NPC npc) {

		Vector2 pathTarget = npc.getPathVector();

		// TODO
		PathFindProgress pathFlag = npc.getFinder().progress;

		// If the path failed, try again.
		if (pathFlag == PathFindProgress.failed || pathFlag == PathFindProgress.notrunning) {
			setPath(npc);
			return false;
		} else if (pathFlag == PathFindProgress.notyetfound) {
			return false;
		} else if (pathFlag == PathFindProgress.found) {

			Path path = npc.getPath();

			IsoObject target = getTarget(npc);

			int pathIndex = npc.getPathIndex();

			// Reset path if finished.
			if (npc.getPathIndex() >= path.getLength()) {
				setPath(npc);
				return false;
			}

			int npcX = (int) npc.getX();
			int npcY = (int) npc.getY();
			int npcZ = (int) npc.getZ();

			int lx = path.getX(pathIndex);
			int ly = path.getY(pathIndex);
			int lz = path.getZ(pathIndex);

			int targetX = (int) target.getX();
			int targetY = (int) target.getY();
			int targetZ = (int) target.getZ();

			float delta = 1.0F;

			float speed = npc.getPathSpeed();

			if (path != null) {
				boolean bOnPath = false;
				if (npcX == lx && npcY == ly && npcZ == lz) {
					float tx = (float) path.getX(pathIndex) + 0.5F;
					float ty = (float) path.getY(pathIndex) + 0.5F;
					pathTarget.x = tx;
					pathTarget.y = ty;
					npc.angle.x = pathTarget.x;
					npc.angle.y = pathTarget.y;
					npc.angle.normalize();
					if (pathIndex < path.getLength() - 1) {
						int nx = path.getX(pathIndex + 1);
						int ny = path.getY(pathIndex + 1);
						IsoWorld.instance.CurrentCell.getGridSquare(nx, ny, (int) npc.getZ());
					}

					if (targetX == npcX && targetY == npcY && targetZ == npcZ) {
						// owner.getStateMachine().changeState(IdleState.instance());
						return false;
					}

					if (IsoUtils.DistanceManhatten(npcX, npcY, tx, ty) < speed * 6.2F) {
						npc.setPathIndex(npc.getPathIndex() + 1);

						if (npc.getPathIndex() >= path.getLength()) {
							return false;
						}

						if (GameTime.instance.getMultiplier() >= 10.0F) {
							npc.setX((float) path.getX(npc.getPathIndex()) + 0.5F);
							npc.setY((float) path.getY(npc.getPathIndex()) + 0.5F);
						} else {
							npc.setX((float) lx + 0.5F);
							npc.setY((float) ly + 0.5F);
						}
					}

					pathTarget.x -= npcX;
					pathTarget.y -= npcY;
					if (pathTarget.getLength() > 0.0F) {
						pathTarget.normalize();
						npc.DirectionFromVector(pathTarget);
					}

					npc.MoveForward(speed, pathTarget.x, pathTarget.y, delta);
					npc.angle.x = pathTarget.x;
					npc.angle.y = pathTarget.y;
					npc.angle.normalize();

					// Possibly needed. If so, grab from IsoZombie.
					// npc.updateFrameSpeed();

				} else {
					float tx = (float) lx + 0.5F;
					float ty = (float) ly + 0.5F;
					pathTarget.x = tx;
					pathTarget.y = ty;
					pathTarget.x -= npcX;
					pathTarget.y -= npcY;
					if (pathTarget.getLength() > 0.0F) {
						pathTarget.normalize();
					}

					npc.DirectionFromVector(pathTarget);
					npc.bRunning = false;

					// TODO radius checks for running.

					npc.MoveForward(speed, pathTarget.x, pathTarget.y, delta);
					npc.angle.x = pathTarget.x;
					npc.angle.y = pathTarget.y;
					npc.angle.normalize();
					// npc.reqMovement.x = npc.angle.x;
					// npc.reqMovement.y = npc.angle.y;
				}
			}
		}

		return false;
	}
	

	public void setPath(NPC npc) {

		npc.pathFinished();

		IsoObject target = getTarget(npc);
		
		if(target != null) {			
			npc.addPath((int) target.getX(), (int) target.getY(), (int) target.getZ());
		}
	}
	
	public IsoObject getTarget(NPC npc) {
		
		IsoObject primaryFollowTarget = npc.getTarget();
		IsoObject secondaryFollowTarget = npc.getDefaultTarget();
		
		IsoObject focusTarget = primaryFollowTarget;
		
		if(focusTarget == null) {
			focusTarget = secondaryFollowTarget;	
		}
		
		return focusTarget;
	}

	@Override
	public String getName() {
		return NAME;
	}


}
