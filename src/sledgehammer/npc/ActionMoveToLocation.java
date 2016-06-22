package sledgehammer.npc;

import sledgehammer.modules.ModuleNPC;
import zombie.characters.IsoPlayer;
import zombie.iso.IsoObject;
import zombie.network.GameServer;

public class ActionMoveToLocation extends Action {

	public static final String NAME = "Action->MoveToLocation";
	
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
					
					npc.setFollow(false);

					// The default target is null too. No need to follow. Returning true to stop loop.
					return true;
				}
			}
		}
		
		if(secondaryFollowTarget != null && secondaryFollowTarget instanceof IsoPlayer) {
			if(!GameServer.Players.contains(((IsoPlayer)secondaryFollowTarget))) {
				
				if(ModuleNPC.DEBUG) {					
					println("NPC: Default following target disconnected.");
				}
				
				npc.setFollow(false);
				
				npc.setDefaultTarget(null);
				
				// The default target is null too. No need to follow. Returning true to stop loop.
				return true;
			}
		}
		
		IsoObject focusTarget = primaryFollowTarget;
		
		if(focusTarget == null) focusTarget = secondaryFollowTarget;			
		
		if(focusTarget == null) {
			
			npc.setFollow(false);
			
			// The default target is null too. No need to follow. Returning true to stop loop.
			return true;
		}
		

		
		long timeNow = System.currentTimeMillis();

		long delta = timeNow - npc.getLastActionTime();
		
		if(delta >= 500L) {
			npc.faceDirection(focusTarget);
			npc.setActionTime(timeNow);
		}
		
		npc.setDestination(focusTarget);
		
		float distanceFromTarget = npc.DistTo(focusTarget);
		
		float speed = npc.getPathSpeed();
		
		if (distanceFromTarget > npc.getDistanceToRun()) {
			
			npc.playAnimation(npc.getRunAnimation());
			
			npc.setRunning(true);
		
		} else if(distanceFromTarget > npc.getDistanceToWalk()){
			
			npc.setRunning(false);
			
			npc.playAnimation(npc.getWalkAnimation());				
		
		} else if(distanceFromTarget <= npc.getArrivalRadius()){
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
	public String getName() { return NAME; }

}
