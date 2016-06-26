package sledgehammer.npc;

import zombie.ai.astar.Mover;
import zombie.ai.astar.Path;

public abstract class PathAction extends Action {

	
	public void onPathSuccess(NPC npc, Mover mover, Path path) {
		
	}
	public void onPathFailure(NPC npc, Mover mover) {
		
	}
}
