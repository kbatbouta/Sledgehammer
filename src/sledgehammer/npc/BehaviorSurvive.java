package sledgehammer.npc;

import java.util.List;

import zombie.inventory.InventoryItem;
import zombie.iso.objects.IsoWorldInventoryObject;

public class BehaviorSurvive extends Behavior {

	private boolean seesItemWorth = false;
	private IsoWorldInventoryObject worldItemWorth = null;
	private long timeThenItemSearch = 0L;
	
	public BehaviorSurvive(NPC npc) {
		super(npc);
	}

	@Override
	public void update() {
		
		long timeNow = System.currentTimeMillis();
	
		if(seesItemWorth) {
			// Set the item to follow.
	
			if(hasArrived()) {
				boolean added = addItemToInventory(worldItemWorth);
				
				if(added) {
					InventoryItem itemRetrieved = worldItemWorth.getItem();
					
					if(itemRetrieved.IsWeapon()) {
						// TODO: SetWeapon
						
						InventoryItem primaryWeapon = getPrimaryWeapon();
						
						// If the weapon is null, set the retrieved weapon as primary.
						if(primaryWeapon == null || primaryWeapon.getType().equals("BareHands")) {
							setPrimaryWeapon(itemRetrieved);
						}
					}
				}

				// Reset Item checks.
				seesItemWorth = false;
				worldItemWorth = null;
				
				// Set primary follow target to null.
				follow(null);
				
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

	public void scanForValuableItems() {
		List<IsoWorldInventoryObject> listItemsOnTheGround = getNearbyItemsOnGround(4);
		
		for(IsoWorldInventoryObject worldItem : listItemsOnTheGround) {
			InventoryItem item = worldItem.getItem();
			if(item.IsWeapon()) {
				seesItemWorth = true;
				worldItemWorth = worldItem;
				follow(worldItemWorth);
				break;
			}
		}

	}

}
