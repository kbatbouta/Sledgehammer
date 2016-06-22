package sledgehammer.npc;

import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import sledgehammer.SledgeHammer;
import sledgehammer.modules.ModuleNPC;
import sledgehammer.util.Printable;
import zombie.characters.IsoPlayer;
import zombie.inventory.ItemContainer;
import zombie.iso.IsoMovingObject;
import zombie.iso.IsoObject;
import zombie.iso.IsoUtils;
import zombie.iso.objects.IsoWorldInventoryObject;
import zombie.network.GameServer;

public abstract class Behavior extends Printable {
	
	NPC npc = null;

	private boolean active = false;

	public Behavior(NPC npc) {
		this.npc = npc;
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
		getNPC().setRunning(true);
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
		// if(ModuleNPC.DEBUG) println("Behavior: Update");
		if(isActive()) update();
	}
	
	public boolean hasArrived() {
		return getNPC().hasArrived();
	}

	public int getPlayerIndex() {
		return getNPC().PlayerIndex;
	}
	
	public Action getCurrentAction() {
		return getNPC().getCurrentAction();
	}
	
	public Action getNextAction() {
		return getNPC().getNextAction();
	}
	
	public void actNext(String name) {
		getNPC().actNext(name);
	}
	
	public void actImmediately(String name) {
		getNPC().actImmediately(name);
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
