package sledgehammer.npc;

import zombie.PathfindManager;
import zombie.ai.astar.AStarPathFinder;
import zombie.ai.astar.IPathfinder;
import zombie.ai.astar.Mover;
import zombie.ai.astar.Path;

public class ActionMoveToLocationAStar extends Action implements IPathfinder {
	
	public static final String NAME = "Action->MoveToLocationAStar";
	
	@Override
	public boolean act(NPC npc) {

		//TODO
		
		//PathfindManager.instance.AddJob(this, npc, sx, sy, sz, tx, ty, tz);
		
		return false;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void Succeeded(Path arg0, Mover arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Failed(Mover arg0) {
		// TODO Auto-generated method stub
		
	}


}
