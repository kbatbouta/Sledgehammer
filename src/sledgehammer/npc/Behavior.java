package sledgehammer.npc;

import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import sledgehammer.SledgeHammer;
import zombie.characters.IsoPlayer;
import zombie.inventory.ItemContainer;
import zombie.iso.IsoMovingObject;
import zombie.iso.IsoObject;
import zombie.iso.IsoUtils;
import zombie.iso.objects.IsoWorldInventoryObject;
import zombie.network.GameServer;

public abstract class Behavior {
	
	NPC npc = null;

	long timeThenFollow = 0L;
	
	private float distanceToRun = 5;
	private float distanceToWalk = 1;
	private boolean arrived = false;
	
	private boolean active = false;

	private Action actionCurrent = null;
	
	public Behavior(NPC npc) {
		this.npc = npc;
	}
	
	// TODO: weapon walking & running.
	private void checkFollowing() {
		
		if (npc.isFollowingObject()) {
			
			IsoObject primaryFollowTarget = getTarget();
			IsoObject secondaryFollowTarget = getDefaultTarget();
			
			if(primaryFollowTarget instanceof IsoPlayer) {
				if(!GameServer.Players.contains(((IsoPlayer)primaryFollowTarget))) {
					System.out.println("NPC: Following target disconnected.");
					setTarget(null);
					if(secondaryFollowTarget == null) {
						setFollow(false);
						return;
					}
				}
			}
			
			if(secondaryFollowTarget instanceof IsoPlayer) {
				if(!GameServer.Players.contains(((IsoPlayer)secondaryFollowTarget))) {
					System.out.println("NPC: Default following target disconnected.");
					setFollow(false);
					setDefaultTarget(null);
					return;
				}
			}
			
			IsoObject focusTarget = primaryFollowTarget;
			
			if(focusTarget == null) focusTarget = secondaryFollowTarget;			
			if(focusTarget == null) {
				setFollow(false);
				return;
			}
			
			long timeNow = System.currentTimeMillis();
			long delta = timeNow - timeThenFollow ;
			
			if(delta >= 500L) {
				faceDirection(focusTarget);
				timeThenFollow = timeNow;
			}
			
			setDestination(focusTarget);
			
			float distanceFromTarget = DistTo(focusTarget);
			
			float speed = getPathSpeed();
			if (distanceFromTarget > distanceToRun) {
				playAnimation(getRunAnimation());
				setRunning(true);
			} else if(distanceFromTarget > distanceToWalk){
				setRunning(false);
				playAnimation(getWalkAnimation());				
			} else {
				speed = 0.0F;
				setRunning(false);
				playAnimation(getIdleAnimation());
				arrived = true;
			}
			
			moveForward(speed);
		}		
	}
	
	public List<IsoWorldInventoryObject> getNearbyItemsOnGround(int radius) {
		return getNPC().getNearbyItemsOnGround(radius);
	}

	public void setFollow(boolean flag) {
		getNPC().setFollow(flag);
	}
	
	public IsoObject getTarget() {
		return getNPC().getTarget();
	}
	
	public void setTarget(IsoObject target) {
		getNPC().setTarget(target);
	}
	
	public IsoObject getDefaultTarget() {
		return getNPC().getDefaultTarget();
	}
	
	public void setDefaultTarget(IsoMovingObject target) {
		getNPC().setDefaultTarget(target);
	}

	public boolean isFollowingObject() {
		return getNPC().isFollowingObject();
	}
	
	public void faceDirection(IsoObject target) {
		getNPC().faceDirection(target);
	}

	public String getIdleAnimation() {
		return getNPC().getIdleAnimation();
	}

	public String getWalkAnimation() {
		return getNPC().getWalkAnimation();
	}

	public String getRunAnimation() {
		return getNPC().getRunAnimation();
	}
	
	public ItemContainer getInventory() {
		return getNPC().getInventory();
	}
	
	public float getInventoryWeight() {
		return getNPC().getInventoryWeight();
	}
	
	public float DistTo(IsoObject other) {
		return IsoUtils.DistanceManhatten(getX(), getY(), other.getX(), other.getY());
	}
	
	/**
	 * Returns the NPC using this BehaviorState instance.
	 * @return
	 */
	public NPC getNPC() {
		return this.npc;
	}
	
	public float getX() {
		return getNPC().getX();
	}
	
	public void setX(float x) {
		getNPC().setX(x);
	}
	
	public float getY() {
		return getNPC().getY();
	}
	
	public void setY(float y) {
		getNPC().setY(y);
	}
	
	/**
	 * Proxy method for 'getNPC().getDestination()'.
	 * @return
	 */
	public Vector3f getDestination() {
		return getNPC().getDestination();
	}

	/**
	 * Proxy method for 'getNPC().setDestination()'.
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setDestination(float x, float y, float z) {
		getNPC().setDestination(x, y, z);
	}
	
	public void setDestination(IsoObject o) {
		getNPC().setDestination(o);
	}
	
	/**
	 * Proxy method for 'getNPC().MoveForward()'.
	 * @param distance
	 */
	public void moveForward(float distance) {
		getNPC().MoveForward(distance);
	}
	
	/**
	 * Returns whether or not this BehaviorState is active.
	 * @return
	 */
	public boolean isActive() {
		return active;
	}
	
	public boolean isRunning() {
		return getNPC().IsRunning();
	}
	
	public void setRunning(boolean flag) {
		getNPC().bRunning = true;
	}
	
	public void playAnimation(String animation) {
		getNPC().PlayAnim(animation);
	}
	
	public float getMovementSpeed() {
		return getNPC().getMoveSpeed();
	}
	
	public float getPathSpeed() {
		return getNPC().getPathSpeed();
	}
	
	public void setWorldItemTarget(IsoWorldInventoryObject worldItemTarget) {
		getNPC().setWorldItemTarget(worldItemTarget);
	}
	
	public IsoWorldInventoryObject getWorldItemTarget() {
		return getNPC().getWorldItemTarget();
	}
	
	public float getZ() {
		return getNPC().getZ();
	}
	
	/**
	 * Sets whether or not this BehaviorState is active.
	 * @param flag
	 */
	public void setActive(boolean flag) {
		active = flag;
	}
	
	public String getName() {
		return getNPC().username;
	}
	
	public void updateBehavior() {
		if(isActive()) update();
		checkFollowing();
	}
	
	public boolean hasArrived() {
		return arrived;
	}
	
	/**
	 * Sets the following distance threshold to run.
	 * @param distance
	 */
	public void setDistanceToRun(float distance) {
		this.distanceToRun = distance;
	}
	
	/**
	 * Sets the following distance threshold to walk.
	 * @param distance
	 */
	public void setDistanceToWalk(float distance) {
		this.distanceToWalk = distance;
	}

	public int getPlayerIndex() {
		return getNPC().PlayerIndex;
	}
	
	public Action getCurrentAction() {
		return actionCurrent;
	}
	
	public void setCurrentAction(Action action) {
		actionCurrent = action;
	}
	
	public Action getAction(String name) {
		return getNPC().getAction(name);
	}
	
	public void act(String name) {
		getNPC().act(name);
	}
	
	public void actIndefinitely(String name) {
		getNPC().actIndefinitely(name);
	}
	
	public void stopAction() {
		getNPC().stopAction();
	}
	
	public boolean isCurrentActionLooped() {
		return getNPC().isCurrentActionLooped();
	}
	
	public abstract void update();
}
