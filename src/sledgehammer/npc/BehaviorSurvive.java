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

		// Reset Item checks.
		seesItemWorth = false;
		setWorldItemTarget(null);
		
		// Set primary follow target to null.
		// If the defaultFollow is set, the NPC will follow that.
		setTarget(null);
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
				setWorldItemTarget(worldItem);
				setTarget(worldItem);
				break;
			}
		}
	}

}
