package sledgehammer.npc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import sledgehammer.SledgeHammer;
import zombie.characters.IsoPlayer;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.inventory.InventoryItem;
import zombie.inventory.ItemContainer;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;
import zombie.iso.IsoDirections;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoMovingObject;
import zombie.iso.IsoObject;
import zombie.iso.IsoUtils;
import zombie.iso.LotHeader;
import zombie.iso.Vector2;
import zombie.iso.areas.IsoBuilding;
import zombie.iso.objects.IsoWorldInventoryObject;
import zombie.network.GameServer;
import zombie.network.PacketTypes;

public abstract class Behavior {
	
	NPC npc = null;
	
	private IsoObject followTarget = null;
	private boolean followObject = false;
	private IsoMovingObject followTargetDefault = null;
	long timeThenFollow = 0L;
	
	private float distanceToRun = 5;
	private float distanceToWalk = 1;
	private boolean arrived = false;
	
	private boolean active = false;

	
	public Behavior(NPC npc) {
		this.npc = npc;
	}
	
	// TODO: weapon walking & running.
	private void checkFollowing() {
		if (followObject) {
			
			IsoObject followTarget = this.followTarget;
			if(followTarget == null) followTarget = followTargetDefault;
			
			long timeNow = System.currentTimeMillis();
			long delta = timeNow - timeThenFollow ;
			
			if(delta >= 500L) {
				faceDirection(followTarget);
				timeThenFollow = timeNow;
			}
			
			setDestination(followTarget);
			
			float distanceFromTarget = DistTo(followTarget);
			
			if (distanceFromTarget > distanceToRun) {
				setSpeed(getPathSpeed());
				playAnimation(getRunAnimation());
				setRunning(true);
			} else if(distanceFromTarget > distanceToWalk){
				setSpeed(getPathSpeed());
				setRunning(false);
				playAnimation(getWalkAnimation());				
			} else {
				setSpeed(0.0F);
				setRunning(false);
				playAnimation("Idle");
				arrived = true;
			}
			
			moveForward(getSpeed());
		}		
	}
	
	private String getWalkAnimation() {
		return getNPC().getWalkAnimation();
	}

	private String getRunAnimation() {
		return getNPC().getRunAnimation();
	}

	/**
	 * Returns all Objects on the ground within a radius.
	 * @param radius
	 * @return
	 */
	public List<IsoWorldInventoryObject> getNearbyItemsOnGround(int radius) {
		List<IsoWorldInventoryObject> listObjects = new ArrayList<>();
		
		IsoCell cell = getCurrentCell();
		IsoGridSquare square = getCurrentSquare();
		int sx = square.getX();
		int sy = square.getY();
		int sz = square.getZ();

		// Go through each square
		for (int y = sy - radius; y < sy + radius; y++) {
			for (int x = sx - radius; x < sx + radius; x++) {

				// Ignore Z checks for now. Possible TODO
				IsoGridSquare lSquare = cell.getGridSquare(x, y, sz);

				if (lSquare != null) {

					ArrayList<IsoWorldInventoryObject> objects = lSquare.getWorldObjects();
					if (objects != null) {
						for (IsoWorldInventoryObject worldItem : objects) {
							if(worldItem != null) {
								listObjects.add(worldItem);
							}
						}
					}
				}
			}
		}
		
		return listObjects;
	}
	
	public boolean addItemToInventory(IsoWorldInventoryObject worldItem) {
		ItemContainer inventory = getInventory();
		InventoryItem item = worldItem.getItem();
		
		// Weight Variables.
		float itemWeight         = item.getWeight();
		float inventoryWeight    = getInventoryWeight();
		float inventoryMaxWeight = inventory.getMaxWeight();
		
		if(itemWeight + inventoryWeight <= inventoryMaxWeight) {			
			inventory.addItem(worldItem.getItem());

			// Network remove the item from the ground.
			GameServer.RemoveItemFromMap(worldItem);
			
			return true;
		}
		
		return false;
	}
	
	public InventoryItem getPrimaryWeapon() {
		return getNPC().getPrimaryHandItem();
	}
	
	public void setPrimaryWeapon(InventoryItem item) {
		getNPC().setPrimaryHandItem(item);
		
		byte hand = 0;
		byte has = 1;
		int onlineID = getNPC().OnlineID;
		
		for(UdpConnection c : SledgeHammer.instance.getUdpEngine().getConnections()) {
           IsoPlayer p2 = GameServer.getAnyPlayerFromConnection(c);
           if(p2 != null) {
              ByteBufferWriter byteBufferWriter = c.startPacket();
              PacketTypes.doPacket(PacketTypes.Equip, byteBufferWriter);
              byteBufferWriter.putByte(hand);
              byteBufferWriter.putByte(has);
              byteBufferWriter.putInt(onlineID);

              // Write the item to the buffer.
              if(has == 1) {            	  
            	  try {
            		  item.save(byteBufferWriter.bb, false);
            	  } catch (IOException var12) {
            		  var12.printStackTrace();
            	  }
              }
              
              c.endPacketImmediate();
           }
		}
		
	}
	
	public ItemContainer getInventory() {
		return getNPC().getInventory();
	}
	
	public float getInventoryWeight() {
		return getNPC().getInventoryWeight();
	}

	public void faceDirection(IsoObject other) {
		Vector2 vector = new Vector2();
		vector.x  = other.getX();
		vector.y  = other.getY();
		vector.x -=       getX();
		vector.y -=       getY();
		vector.normalize();
		setDirection(vector);
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
	
	public void setDirection(Vector2 vector) {
		getNPC().DirectionFromVector(vector);
	}
	
	public IsoDirections getDirection(Vector2 vector) {
		return IsoDirections.fromAngle(vector);
	}
	
	/**
	 * Calculates the Manhatten distance from the NPC to the object given.
	 * @param other
	 * @return
	 */
	public float getDistance(IsoObject other) {
		return IsoUtils.DistanceManhatten(getX(), getY(), other.getX(), other.getY());	
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
	 * Proxy method for 'getNPC().getSpeed()'.
	 * @return
	 */
	public float getSpeed() {
		return getNPC().getSpeed();
	}
	
	/**
	 * Proxy method for 'getNPC().setSpeed()'.
	 * @param speed
	 */
	public void setSpeed(float speed) {
		getNPC().setSpeed(speed);
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
	
	public IsoBuilding getCurrentBuilding() {
		return getNPC().getCurrentBuilding();
	}
	
	public IsoGridSquare getCurrentSquare() {
		return getNPC().getCurrentSquare();
	}
	
	public IsoChunk getCurrentChunk() {
		return getCurrentSquare().getChunk();
	}
	
	public IsoCell getCurrentCell() {
		return getCurrentSquare().getCell();
	}
	
	public LotHeader getCurrentLotHeader() {
		return getCurrentCell().getCurrentLotHeader();
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
	
	public void follow(IsoObject target) {
		followTarget = target;
		followObject = followTarget == null && followTargetDefault == null ? false : true;
	}
	
	public void followDefault(IsoMovingObject target) {
		followTargetDefault = target;
		if(target != null) followObject = true;
	}

	public int getPlayerIndex() {
		return getNPC().PlayerIndex;
	}
	
	public abstract void update();
}
