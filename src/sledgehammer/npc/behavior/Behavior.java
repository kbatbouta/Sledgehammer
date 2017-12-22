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
package sledgehammer.npc.behavior;

import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import sledgehammer.npc.action.Action;
import sledgehammer.util.Printable;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoZombie;
import zombie.inventory.ItemContainer;
import zombie.iso.IsoObject;
import zombie.iso.objects.IsoWorldInventoryObject;
import zombie.sledgehammer.npc.NPC;
import zombie.iso.IsoMovingObject;

/**
 * TODO: Document.
 * 
 * @author Jab
 */
public abstract class Behavior extends Printable {

	NPC npc = null;

	private boolean active = false;

	public Behavior(NPC npc) {
		this.npc = npc;
	}

	@SuppressWarnings("unchecked")
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
	 * 
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
	 * 
	 * @return
	 */
	public Vector3f getDestination() {
		return getNPC().getDestination();
	}

	/**
	 * Proxy method for 'getNPC().setDestination()'.
	 * 
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
	 * 
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
	 * 
	 * @param flag
	 */
	public void setActive(boolean flag) {
		active = flag;
	}

	public String getName() {
		return getNPC().username;
	}

	public void updateBehavior() {
		if (isActive()) {
			update();
		}
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

	@SuppressWarnings("unchecked")
	public List<IsoZombie> getNearestZombies(int radius) {
		return getNPC().getNearestZombies(radius);
	}

	public IsoZombie getNearestZombie(List<IsoZombie> listZombies) {
		return getNPC().getNearestZombie(listZombies);
	}

	public boolean isCurrentActionLooped() {
		return getNPC().isCurrentActionLooped();
	}

	public boolean canWalk() {
		return getNPC().canWalk();
	}

	public void setCanWalk(boolean flag) {
		getNPC().setCanWalk(flag);
	}

	public boolean canRun() {
		return getNPC().canRun();
	}

	public void setCanRun(boolean flag) {
		getNPC().setCanRun(flag);
	}

	public IsoGameCharacter getAttackTarget() {
		return getNPC().getAttackTarget();
	}

	public void setAttackTarget(IsoGameCharacter target) {
		getNPC().setAttackTarget(target);
	}

	public String getWalkAndAimAnimation() {
		return getNPC().getWalkAndAimAnimation();
	}

	public String getAttackOnFloorAnimation() {
		return getNPC().getAttackOnFloorAnimation();
	}

	public String getAttackAnimation() {
		return getNPC().getAttackAnimation();
	}

	public void setArrived(boolean flag) {
		getNPC().setArrived(flag);
	}

	public float getDistance(IsoObject object) {
		return getNPC().getDistance(object);
	}

	public abstract void update();
}
