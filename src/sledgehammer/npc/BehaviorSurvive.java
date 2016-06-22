package sledgehammer.npc;

import java.util.List;

import zombie.inventory.InventoryItem;
import zombie.inventory.ItemContainer;
import zombie.iso.objects.IsoWorldInventoryObject;

public class BehaviorSurvive extends Behavior {

	private boolean seesItemWorth = false;
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

		boolean foundJob = false;
		
		long timeNow = System.currentTimeMillis();
	
		if(seesItemWorth) {
			
			// Set the item to follow.
			if(hasArrived()) {
				
				foundJob = true;
				actImmediately(ActionGrabItemOnGround.NAME);
				
				// Set primary follow target to null.
				// If the defaultFollow is set, the NPC will follow that.
				setTarget(null);
				
				seesItemWorth = false;
				
			} else {
				foundJob = true;
				actImmediately(ActionMoveToLocation.NAME);
			}
		} else {
			
			// Wait for one second, then search for items nearby.
			if ( (timeNow - timeThenItemSearch) > 1000L ) {
				scanForValuableItems();
				
				if(seesItemWorth) {
					foundJob = true;
					actImmediately(ActionMoveToLocation.NAME);
				}

				// Reset the timer for delta.
				timeThenItemSearch = timeNow;
			}
			
		}
		
		if(!foundJob) {				
			actImmediately(ActionMoveToLocation.NAME);
		}
	}

	public void scanForValuableItems() {
		
		// If the NPC is already going to pick up another item, then there's no
		// need to scan, until the item is picked up.
		if(getWorldItemTarget() != null) return;
		
		// Scan for nearby items on the ground.
		List<IsoWorldInventoryObject> listItemsOnTheGround = getNearbyItemsOnGround(4);
		
		for(IsoWorldInventoryObject worldItem : listItemsOnTheGround) {
			
			InventoryItem item = worldItem.getItem();
			ItemContainer inventory = getInventory();
			
			// If we have enough of this item.
			if(inventory.getItemCount(item.getType()) < maximumIdenticalItemsToCarry) {
				//TODO
			}
			
			if(item.IsWeapon()) {
				seesItemWorth = true;
				setWorldItemTarget(worldItem);
				setTarget(worldItem);
				break;
			}
		}
	}
}
