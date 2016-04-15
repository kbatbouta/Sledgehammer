package zirc.wrapper;

import zombie.characters.SurvivorDesc;
import zombie.iso.IsoCell;
import zombie.iso.IsoMovingObject;
import zombie.iso.IsoNPCPlayer;
import zombie.iso.IsoObject;
import zombie.iso.IsoUtils;
import zombie.network.GameServer;

public class NPC extends IsoNPCPlayer {

	private static final long serialVersionUID = 8799144318873059045L;

	private boolean followObject = false;
	private IsoObject followTarget = null;
	
	public NPC(IsoCell cell, SurvivorDesc desc, String username, int x, int y, int z) {
		super(cell, desc, x, y, z);
		
		// Generates an index.
		int playerIndex = 0;
		for (int index = 0; index < 1024; index++) {
			Object objectPlayer = GameServer.IDToPlayerMap.get(index);
			if (objectPlayer == null) {
				playerIndex = index;
				break;
			}
		}

		this.PlayerIndex          = playerIndex;
		this.username             =    username;
		this.OnlineChunkGridWidth =           1;
		this.OnlineID             =          -1;
		this.bRemote              =        true;
	}

	public void follow(IsoMovingObject target) {
		followTarget = target;
		followObject = followTarget == null ? false : true;
	}

	public void update() {
		super.update();
		if (followObject) {
			setDestination(followTarget.getX(), followTarget.getY(), followTarget.getZ());
			faceDirection(followTarget);
			float dist = DistTo(followTarget);
			if (dist > 1) MoveForward(dist);
		}
	}

	public float DistTo(IsoObject other) {
		return IsoUtils.DistanceManhatten(getX(), getY(), other.getX(), other.getY());
	}

	public void faceDirection(IsoObject other) {
		tempo.x  = other.getX();
		tempo.y  = other.getY();
		tempo.x -=       getX();
		tempo.y -=       getY();
		tempo.normalize();
		this.DirectionFromVector(tempo);
	}
}
