/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
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
 * Class to handle NPCs with a passive, long-term behavior pattern. This
 * class provides utilities and executes Actions for the NPC to act on.
 *
 * @author Jab
 */
public abstract class Behavior extends Printable {

    /**
     * The NPC using the Behavior.
     */
    private NPC npc;

    /**
     * Flag for the Behavior being active.
     */
    private boolean active = false;

    /**
     * Main constructor.
     *
     * @param npc The NPC using the Behavior.
     */
    public Behavior(NPC npc) {
        this.npc = npc;
    }

    @Override
    public String getName() {
        return getNPC().username;
    }

    /**
     * Updates the NPCs Behavior if it is active.
     */
    public void updateBehavior() {
        if (isActive()) {
            update();
        }
    }

    /**
     * @param radius The Integer radius in IsoGridSquares around the NPC.
     * @return Returns a List of IsoWorldInventoryObjects around the NPC on
     * the ground.
     */
    @SuppressWarnings("unchecked")
    public List<IsoWorldInventoryObject> getNearbyItemsOnGround(int radius) {
        return getNPC().getNearbyItemsOnGround(radius);
    }

    /**
     * Sets the NPC to follow a target.
     *
     * @param flag The flag to set.
     */
    public void setFollow(boolean flag) {
        getNPC().setFollow(flag);
    }

    /**
     * @return Returns the set IsoObject target for the NPC to follow.
     */
    public IsoObject getTarget() {
        return getNPC().getTarget();
    }

    /**
     * Sets a IsoObject target for the NPC to follow.
     *
     * @param target The IsoObject target to set.
     */
    public void setTarget(IsoObject target) {
        getNPC().setTarget(target);
    }

    /**
     * @return Returns the default IsoMovingObject target for the NPC to follow.
     * (Note: This is the passive, or secondary target to follow)
     */
    public IsoObject getDefaultTarget() {
        return getNPC().getDefaultTarget();
    }

    /**
     * Sets the default IsoMovingObject target for the NPC to follow.
     * <p>
     * (Note: This is a passive, or secondary target to follow)
     *
     * @param target The IsoMovingObject target to set.
     */
    public void setDefaultTarget(IsoMovingObject target) {
        getNPC().setDefaultTarget(target);
    }

    /**
     * @return Returns true if the NPC is following an IsoObject.
     */
    public boolean isFollowingObject() {
        return getNPC().isFollowingObject();
    }

    /**
     * Sets the NPC to face the direction of a IsoObject target.
     *
     * @param target The IsoObject target to face.
     */
    public void faceDirection(IsoObject target) {
        getNPC().faceDirection(target);
    }

    /**
     * @return Returns the String id of the idle animation.
     */
    public String getIdleAnimation() {
        return getNPC().getIdleAnimation();
    }

    /**
     * @return Returns the String id of the walking animation.
     */
    public String getWalkAnimation() {
        return getNPC().getWalkAnimation();
    }

    /**
     * @return Returns the String id of the running animation.
     */
    public String getRunAnimation() {
        return getNPC().getRunAnimation();
    }

    /**
     * @return Returns the String id of the walking and aiming animation.
     */
    public String getWalkAndAimAnimation() {
        return getNPC().getWalkAndAimAnimation();
    }

    /**
     * @return Returns the String id of the attacking on the floor animation.
     */
    public String getAttackOnFloorAnimation() {
        return getNPC().getAttackOnFloorAnimation();
    }

    /**
     * @return Returns the String id of the attacking animation.
     */
    public String getAttackAnimation() {
        return getNPC().getAttackAnimation();
    }

    /**
     * @return Returns the ItemContainer inventory for the NPC.
     */
    public ItemContainer getInventory() {
        return getNPC().getInventory();
    }

    /**
     * @return Returns the Float weight value of the NPCs inventory.
     */
    public float getInventoryWeight() {
        return getNPC().getInventoryWeight();
    }

    /**
     * @return Returns the Float x-coordinate of the NPCs location.
     */
    public float getX() {
        return getNPC().getX();
    }

    /**
     * Sets the Float x-coordinate of the NPCs location.
     *
     * @param x The Float coordinate to set.
     */
    public void setX(float x) {
        getNPC().setX(x);
    }

    /**
     * @return Returns the Float y-coordinate of the NPCs location.
     */
    public float getY() {
        return getNPC().getY();
    }

    /**
     * Sets the Float y-coordinate of the NPCs location.
     *
     * @param y The Float coordinate to set.
     */
    public void setY(float y) {
        getNPC().setY(y);
    }

    /**
     * @return Returns the Float z-coordinate of the NPCs location.
     */
    public float getZ() {
        return getNPC().getZ();
    }

    /**
     * Sets the Float z-coordinate of the NPCs location.
     *
     * @param z The Float coordinate to set.
     */
    public void setZ(float z) {
        getNPC().setZ(z);
    }

    /**
     * Approximate method for 'getNPC().getDestination()'.
     *
     * @return Returns a Vector3f of the set destination for the NPC.
     */
    public Vector3f getDestination() {
        return getNPC().getDestination();
    }

    /**
     * Approximate method for 'getNPC().setDestination()'.
     * <p>
     * Sets the location for a NPC to attempt traveling towards.
     *
     * @param x The Float x-coordinate of the location.
     * @param y The Float y-coordinate of the location.
     * @param z The Float z-coordinate of the location.
     */
    public void setDestination(float x, float y, float z) {
        getNPC().setDestination(x, y, z);
    }

    /**
     * Approximate method for 'getNPC().setDestination()'.
     * <p>
     * Sets the location for a NPC to attempt traveling towards.
     *
     * @param o The IsoObject to set as the location.
     */
    public void setDestination(IsoObject o) {
        getNPC().setDestination(o);
    }

    /**
     * @return Returns true if the Behavior is active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @return Returns true if the NPC is currently running.
     */
    public boolean isRunning() {
        return getNPC().IsRunning();
    }

    /**
     * Sets the NPC to run.
     *
     * @param flag The flag to set.
     */
    public void setRunning(boolean flag) {
        getNPC().setRunning(true);
    }

    /**
     * Executes a String animation for the NPC.
     *
     * @param animation The String animation to play.
     */
    public void playAnimation(String animation) {
        getNPC().PlayAnim(animation);
    }

    /**
     * @return Returns the set Float movement speed for the NPC.
     */
    public float getMovementSpeed() {
        return getNPC().getMoveSpeed();
    }

    /**
     * @return Returns the set Float path speed for the NPC.
     */
    public float getPathSpeed() {
        return getNPC().getPathSpeed();
    }

    /**
     * Sets a IsoWorldInventoryObject target for the NPC to attempt picking up.
     *
     * @param worldItemTarget The IsoWorldInventoryObject to set.
     */
    public void setWorldItemTarget(IsoWorldInventoryObject worldItemTarget) {
        getNPC().setWorldItemTarget(worldItemTarget);
    }

    /**
     * @return Returns the IsoWorldInventoryObject set for the NPC to attempt
     * picking up. If nothing is set to be picked up, null is returned.
     */
    public IsoWorldInventoryObject getWorldItemTarget() {
        return getNPC().getWorldItemTarget();
    }

    /**
     * Sets whether or not this BehaviorState is active.
     *
     * @param flag The flag to set.
     */
    public void setActive(boolean flag) {
        active = flag;
    }

    /**
     * @return Returns true if the NPC has arrived to the target.
     */
    public boolean hasArrived() {
        return getNPC().hasArrived();
    }

    /**
     * @return Returns the internal Integer player-id assigned to the NPC.
     */
    public int getPlayerIndex() {
        return getNPC().PlayerIndex;
    }

    /**
     * @return Returns the current Action the Behavior is applying to the NPC.
     */
    public Action getCurrentAction() {
        return getNPC().getCurrentAction();
    }

    /**
     * @return Returns the next Action the Behavior will apply to the NPC.
     */
    public Action getNextAction() {
        return getNPC().getNextAction();
    }

    /**
     * Sets the next Action.
     *
     * @param name The String name of the Action.
     */
    public void actNext(String name) {
        getNPC().actNext(name);
    }

    /**
     * Forces the NPC to act immediately with a given String action.
     *
     * @param name The String name of the Action.
     */
    public void actImmediately(String name) {
        getNPC().actImmediately(name);
    }

    /**
     * Sets an Action for the NPC to act on until instructed otherwise.
     *
     * @param name The String name of the Action.
     */
    public void actIndefinitely(String name) {
        getNPC().actIndefinitely(name);
    }

    /**
     * Stops the current Action being acted by the NPC.
     */
    public void stopAction() {
        getNPC().stopAction();
    }

    @SuppressWarnings("unchecked")
    public List<IsoZombie> getNearestZombies(int radius) {
        return getNPC().getNearestZombies(radius);
    }

    /**
     * @param listZombies The List of IsoZombies to test.
     * @return Returns the nearest IsoZombie in the given List of IsoZombies.
     */
    public IsoZombie getNearestZombie(List<IsoZombie> listZombies) {
        return getNPC().getNearestZombie(listZombies);
    }

    /**
     * @return Returns true if the current Action set for the NPC is looped.
     */
    public boolean isCurrentActionLooped() {
        return getNPC().isCurrentActionLooped();
    }

    /**
     * @return Returns true if the NPC is able to walk.
     */
    public boolean canWalk() {
        return getNPC().canWalk();
    }

    /**
     * Sets the NPC to be able to walk.
     *
     * @param flag The flag to set.
     */
    public void setCanWalk(boolean flag) {
        getNPC().setCanWalk(flag);
    }

    /**
     * @return Returns true if the NPC is able to run.
     */
    public boolean canRun() {
        return getNPC().canRun();
    }

    /**
     * Sets the NPC to be able to run.
     *
     * @param flag The flag to set.
     */
    public void setCanRun(boolean flag) {
        getNPC().setCanRun(flag);
    }

    /**
     * @return Returns the IsoGameCharacter target for the NPC to attack. if no
     * target is set, null is returned.
     */
    public IsoGameCharacter getAttackTarget() {
        return getNPC().getAttackTarget();
    }

    /**
     * Sets the IsoGameCharacter target for the NPC to attack.
     *
     * @param target The IsoGameCharacter target to set.
     */
    public void setAttackTarget(IsoGameCharacter target) {
        getNPC().setAttackTarget(target);
    }

    /**
     * Sets the Boolean flag for arrival for the NPC.
     *
     * @param flag The flag to set.
     */
    public void setArrived(boolean flag) {
        getNPC().setArrived(flag);
    }

    /**
     * @param object The IsoObject to measure.
     * @return Returns the Float distance between a NPC and the given
     * IsoObject.
     */
    public float getDistance(IsoObject object) {
        return getNPC().getDistance(object);
    }

    /**
     * @return Returns the NPC using this BehaviorState instance.
     */
    public NPC getNPC() {
        return this.npc;
    }

    /**
     * Fired when the Behavior is updated.
     */
    public abstract void update();
}