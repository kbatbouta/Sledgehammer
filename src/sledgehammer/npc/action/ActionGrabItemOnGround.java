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
package sledgehammer.npc.action;

import zombie.inventory.InventoryItem;
import zombie.inventory.types.HandWeapon;
import zombie.iso.objects.IsoWorldInventoryObject;
import zombie.sledgehammer.npc.NPC;

/**
 * Action that executes <NPC>'s grabbing an item that is on the ground.
 * 
 * @author Jab
 */
public class ActionGrabItemOnGround extends Action {

	/** The <String> name of the <Action>. */
	public static final String NAME = "Action->GrabItemOnGround";

	@Override
	public boolean act(NPC npc) {
		IsoWorldInventoryObject worldItemWorth = npc.getWorldItemTarget();
		// Try to add the item in the inventory.
		boolean added = npc.addItemToInventory(worldItemWorth);
		// If the item was successfully added.
		if (added) {
			InventoryItem itemRetrieved = worldItemWorth.getItem();
			if (itemRetrieved.IsWeapon()) {
				// Grab weapon currently used.
				HandWeapon primaryWeapon = (HandWeapon) npc.getPrimaryWeapon();
				// Cast the weapon to HandWeapon.
				HandWeapon weaponRetrieved = (HandWeapon) itemRetrieved;
				// If the weapon is null, set the retrieved weapon as primary.
				if (primaryWeapon == null || primaryWeapon.getType().equals("BareHands")) {
					// Set the picked-up weapon as primary.
					npc.setPrimaryWeapon(weaponRetrieved);
				} else {
					// If the item is the same as currently weilding.
					if (primaryWeapon.getName().equals(weaponRetrieved.getName())) {
						// If the item picked up has less damage than the one currently wielded.
						if (weaponRetrieved.getConditionPercent() < primaryWeapon.getConditionPercent()) {
							// Set the picked-up weapon as primary.
							npc.setPrimaryWeapon(weaponRetrieved);
						}
					}
					// If the weapon is different.
					else {
						// Avoid sledge-hammer.
						if (!weaponRetrieved.isCantAttackWithLowestEndurance()) {
							// If the weapon is more damaging.
							if (weaponRetrieved.getDamagePercent() > primaryWeapon.getDamagePercent()) {
								// TODO: Check weapon speed.
								// Set the picked-up weapon as primary.
								npc.setPrimaryWeapon(weaponRetrieved);
							}
						}
					}
				}
			}
			// Reset Item checks.
			npc.setWorldItemTarget(null);
			// Set primary follow target to null.
			// If the defaultFollow is set, the NPC will follow that.
			npc.setTarget(null);
		}
		// If the item wasn't added to the inventory (Full).
		else {
			// TODO: Evaluate the need of this item.
			// Reset Item checks.
			npc.setWorldItemTarget(null);
			// Set primary follow target to null.
			// If the defaultFollow is set, the NPC will follow that.
			npc.setTarget(null);
		}
		return true;
	}

	@Override
	public String getName() {
		return NAME;
	}
}