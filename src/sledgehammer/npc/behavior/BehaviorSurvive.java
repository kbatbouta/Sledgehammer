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

import sledgehammer.npc.action.ActionAttackCharacter;
import sledgehammer.npc.action.ActionGrabItemOnGround;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoZombie;
import zombie.inventory.InventoryItem;
import zombie.inventory.ItemContainer;
import zombie.inventory.types.HandWeapon;
import zombie.iso.IsoObject;
import zombie.iso.objects.IsoWorldInventoryObject;
import zombie.sledgehammer.npc.NPC;
import zombie.sledgehammer.npc.action.ActionFollowTargetPath;

/**
 * TODO: Document.
 * 
 * @author Jab
 */
public class BehaviorSurvive extends Behavior {

	private List<IsoZombie> listNearbyZombies = null;
	private long timeThenItemSearch = 0L;
	private long timeThenNearbyZombieLookup = 0L;
	private long deltaItemSearch = 1000L;
	private long deltaNearbyZombieLookup = 1000L;
	/**
	 * Hold a maximum of 2 of a certain item.
	 */
	private int maximumIdenticalItemsToCarry = 2;
	private boolean seesItemWorth = false;
	private boolean isAttackingZombie = false;

	public BehaviorSurvive(NPC npc) {
		super(npc);
	}

	@Override
	public void update() {
		boolean foundJob = false;
		long timeNow = System.currentTimeMillis();
		if (isAttackingZombie) {
			IsoGameCharacter target = getAttackTarget();
			if (target == null) {
				setTarget(null);
				setCanRun(true);
				isAttackingZombie = false;
			} else {
				if (target.isDead()) {
					setAttackTarget(null);
					setTarget(null);
					isAttackingZombie = false;
					setCanRun(true);
				}
			}
		}
		if (timeNow - timeThenNearbyZombieLookup >= deltaNearbyZombieLookup) {
			listNearbyZombies = getNearestZombies(12);
			// Set the time field to check
			timeThenNearbyZombieLookup = System.currentTimeMillis();
		}
		if (isAttackingZombie) {
			IsoZombie target = getNearestZombie(listNearbyZombies);
			setAttackTarget(target);
			setTarget(target);
			if (target == null) {
				isAttackingZombie = false;
				setCanRun(true);
				return;
			}
			float distToHit = 2F;
			HandWeapon weapon = npc.getPrimaryWeapon();
			if (weapon != null) {
				distToHit = weapon.getMinRange() * 2;
			}
			if (getDistance(target) <= distToHit) {
				if (target == null || target.isDead()) {
					setAttackTarget(null);
					setTarget(null);
					setCanRun(true);
					isAttackingZombie = false;
					playAnimation(getIdleAnimation());
				} else {
					foundJob = true;
					setTarget(target);
					setCanRun(true);
					playAnimation(getAttackAnimation());
					actImmediately(ActionAttackCharacter.NAME);
				}
			} else {
				foundJob = true;
				setTarget(target);
				playAnimation(getWalkAndAimAnimation());
				setArrived(false);
				actIndefinitely(ActionFollowTargetPath.NAME);
			}
		} else {
			// If we have nearby zombies.
			if (!listNearbyZombies.isEmpty()) {
				int amountZombies = listNearbyZombies.size();
				if (amountZombies > 5) {
					// TODO: run away from spot.
				} else {
					IsoZombie targetZombie = getNearestZombie(listNearbyZombies);
					setAttackTarget(targetZombie);
					setTarget(targetZombie);
					setCanWalk(true);
					setCanRun(false);
					isAttackingZombie = true;
				}
			}
		}
		if (seesItemWorth) {
			// Set the item to follow.
			if (hasArrived()) {
				foundJob = true;
				actImmediately(ActionGrabItemOnGround.NAME);
				// Set primary follow target to null.
				// If the defaultFollow is set, the NPC will follow that.
				setTarget(null);
				seesItemWorth = false;
			} else {
				foundJob = true;
				setArrived(false);
				actIndefinitely(ActionFollowTargetPath.NAME);
			}
		} else {
			// Wait for one second, then search for items nearby.
			if ((timeNow - timeThenItemSearch) > deltaItemSearch) {
				scanForValuableItems();
				if (seesItemWorth) {
					foundJob = true;
					setArrived(false);
					actIndefinitely(ActionFollowTargetPath.NAME);
				}
				// Reset the timer for delta.
				timeThenItemSearch = timeNow;
			}
		}
		if (!foundJob) {
			IsoObject target = npc.getPrimaryTarget();
			if (target != null) {
				setFollow(true);
				setArrived(false);
				npc.actIndefinitely(ActionFollowTargetPath.NAME);
			}
		}
	}

	public void scanForValuableItems() {
		// If the NPC is already going to pick up another item, then there's no
		// need to scan, until the item is picked up.
		if (getWorldItemTarget() != null) {			
			return;
		}
		// Scan for nearby items on the ground.
		List<IsoWorldInventoryObject> listItemsOnTheGround = getNearbyItemsOnGround(4);
		for (IsoWorldInventoryObject worldItem : listItemsOnTheGround) {
			InventoryItem item = worldItem.getItem();
			ItemContainer inventory = getInventory();
			// TODO: If we have enough of this item.
			if (inventory.getItemCount(item.getType()) < maximumIdenticalItemsToCarry) {
			}
			if (item.IsWeapon()) {
				seesItemWorth = true;
				setWorldItemTarget(worldItem);
				setTarget(worldItem);
				break;
			}
		}
	}
}