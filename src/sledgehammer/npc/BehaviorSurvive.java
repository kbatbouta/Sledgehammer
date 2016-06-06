package sledgehammer.npc;

import java.util.List;

import zombie.inventory.InventoryItem;
import zombie.inventory.ItemContainer;
import zombie.inventory.types.HandWeapon;
import zombie.iso.objects.IsoWorldInventoryObject;

public class BehaviorSurvive extends Behavior {

	private boolean seesItemWorth = false;
	private IsoWorldInventoryObject worldItemWorth = null;
	private long timeThenItemSearch = 0L;
	
	/**
	 * Hold a maximum of 2 of a certain item.
	 */
	private int maximumIdenticalItemsToCarry = 2;
	
	public BehaviorSurvive(NPC npc) {
		super(npc);
	}

	@Override
	public void update() {
		
		long timeNow = System.currentTimeMillis();
	
		if(seesItemWorth) {
			// Set the item to follow.
			if(hasArrived()) {
				retrieveItem();
			}
		} else {
			// Wait for one second, then search for items nearby.
			if ( (timeNow - timeThenItemSearch) > 1000L ) {
				scanForValuableItems();

				// Reset the timer for delta.
				timeThenItemSearch = timeNow;
			}
		}
		
	}
	
	/**
	 * Retrieves the item on the ground, and analyzes the quality of the item.
	 * 
	 * If it is a weapon, then analysis determines whether or not to use it.
	 * The weapon most preferred is an Axe.
	 */
	public void retrieveItem() {
		
		// Try to add the item in the inventory.
		boolean added = addItemToInventory(worldItemWorth);
		
		// If the item was successfully added.
		if(added) {
			InventoryItem itemRetrieved = worldItemWorth.getItem();
			
			if(itemRetrieved.IsWeapon()) {
				// Grab weapon currently used.
				HandWeapon primaryWeapon   = (HandWeapon) getPrimaryWeapon();

				// Cast the weapon to HandWeapon.
				HandWeapon weaponRetrieved = (HandWeapon) itemRetrieved;
				
				
				// If the weapon is null, set the retrieved weapon as primary.
				if(primaryWeapon == null || primaryWeapon.getType().equals("BareHands")) {
					
					// Set the picked-up weapon as primary.					
					setPrimaryWeapon(weaponRetrieved);
				
				} else {						
					
					// If the item is the same as currently weilding.
					if(primaryWeapon.getName().equals(weaponRetrieved.getName())) {
						
						// If the item picked up has less damage than the one currently wielded.
						if(weaponRetrieved.getConditionPercent() < primaryWeapon.getConditionPercent()) {
							
							// Set the picked-up weapon as primary.
							setPrimaryWeapon(weaponRetrieved);							
						}
						
					// If the weapon is different.
					} else {
						
						// Avoid sledge-hammer.
						if(!weaponRetrieved.isCantAttackWithLowestEndurance()) {							
							
							// If the weapon is more damaging.
							if(weaponRetrieved.getDamagePercent() > primaryWeapon.getDamagePercent()) {
								
								// TODO: Check weapon speed.

								// Set the picked-up weapon as primary.
								setPrimaryWeapon(weaponRetrieved);
							}
						}
					}
				}
			}
			
		// If the item wasn't added to the inventory (Full).
		} else {
			
			// TODO: Evaluate the need of this item.
			
		}

		// Reset Item checks.
		seesItemWorth = false;
		worldItemWorth = null;
		
		// Set primary follow target to null.
		// If the defaultFollow is set, the NPC will follow that.
		follow(null);
	}

	public void scanForValuableItems() {
		List<IsoWorldInventoryObject> listItemsOnTheGround = getNearbyItemsOnGround(4);
		
		for(IsoWorldInventoryObject worldItem : listItemsOnTheGround) {
			InventoryItem item = worldItem.getItem();
			ItemContainer inventory = getInventory();
			
			// If we have enough of this item.
			if(inventory.getItemCount(item.getType()) < this.maximumIdenticalItemsToCarry) {
				
			}
			
			if(item.IsWeapon()) {
				seesItemWorth = true;
				worldItemWorth = worldItem;
				follow(worldItemWorth);
				break;
			}
		}
	}

}
