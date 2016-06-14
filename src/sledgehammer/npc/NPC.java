package sledgehammer.npc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import fmod.fmod.DummySoundEmitter;
import fmod.fmod.DummySoundListener;
import sledgehammer.SledgeHammer;
import sledgehammer.util.ZUtil;
import zombie.ai.states.StaggerBackState;
import zombie.characters.DummyCharacterSoundEmitter;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.SurvivorDesc;
import zombie.characters.skills.PerkFactory;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.inventory.InventoryItem;
import zombie.inventory.ItemContainer;
import zombie.inventory.types.HandWeapon;
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
import zombie.network.ServerLOS;
import zombie.network.ServerMap;

// TODO: Work on transferring items to the deadBody onDeath.
public class NPC extends IsoPlayer {

	private static final long serialVersionUID = 8799144318873059045L;
	private List<Behavior> listBehaviors;
	private Vector3f destination = new Vector3f();
	private String walkAnim = "Walk";
	private String runAnim  = "Run";
	private String idleAnim = "Idle";
	private IsoObject followTarget = null;
	private boolean followObject = false;
	private IsoMovingObject followTargetDefault = null;
	
	/**
	 * An item to be set for the NPC to go to, and pick up.
	 */
	private IsoWorldInventoryObject worldItemTarget = null;
	
	private HandWeapon weapon = null;
	
	public NPC(IsoCell cell, SurvivorDesc desc, String username, int x, int y, int z) {
		super(cell, desc, x, y, z);
		
		ServerLOS.instance.addPlayer(this);
		
		initializePerks();

		updateHands();
		
		// Update position in world.
		updateSquare();

		
		listBehaviors = new ArrayList<>();
		
		// Generates an index.
		int playerIndex = 1;

		this.PlayerIndex          =                          playerIndex;
		this.username             =                             username;
		this.OnlineChunkGridWidth =                                    3;
		this.OnlineID             =  (short) ZUtil.random.nextInt(28000);
		this.bRemote              =                                 true;
		this.invisible            =                                false;
		this.emitter              = new DummyCharacterSoundEmitter(this);
		this.soundListener        =  new DummySoundListener(PlayerIndex);
		this.testemitter          =              new DummySoundEmitter();
		
		setHealth(1.0F);
		PlayAnim("Idle");
	}
	
	@Override
	public void hitConsequences(HandWeapon weapon, IsoGameCharacter wielder, boolean bIgnoreDamage, float damage,
			boolean bKnockdown) {
		if (bIgnoreDamage) {
			this.sendObjectChange("Shove", new Object[] { "hitDirX", Float.valueOf(this.getHitDir().getX()), "hitDirY",
					Float.valueOf(this.getHitDir().getY()), "force", Float.valueOf(this.getHitForce()) });
			return;
		}

		this.BodyDamage.DamageFromWeapon(weapon);

		if (wielder instanceof IsoPlayer) {
			if (!bIgnoreDamage) {
				if (weapon.isAimedFirearm()) {
					this.Health -= damage * 0.7F;
				} else {
					this.Health -= damage * 0.15F;
				}
			}
		} else if (!bIgnoreDamage) {
			if (weapon.isAimedFirearm()) {
				this.Health -= damage * 0.7F;
			} else {
				this.Health -= damage * 0.15F;
			}
		}

		if (this.Health > 0.0F && this.BodyDamage.getHealth() > 0.0F && (!weapon.isAlwaysKnockdown() && !bKnockdown)) {
			if (weapon.isSplatBloodOnNoDeath()) {
				this.splatBlood(3, 0.3F);
			}

			if (weapon.isKnockBackOnNoDeath()) {
				if (wielder.xp != null) {
					wielder.xp.AddXP(PerkFactory.Perks.Strength, 2.0F);
				}

				this.stateMachine.changeState(StaggerBackState.instance());
			}
			
			SledgeHammer.instance.getNPCEngine().updateNPCToPlayers(this);
		
		} else {
			this.DoDeath(weapon, wielder);
		}
		
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
			inventory.addItemOnServer(worldItem.getItem());
			
			// Network remove the item from the ground.
			GameServer.RemoveItemFromMap(worldItem);
			
			return true;
		}
		
		return false;
	}
	
	private void initializePerks() {
		ArrayList<IsoGameCharacter.PerkInfo> perks = new ArrayList<>();
		perks.add(createPerkInfo(PerkFactory.Perks.Agility));
		perks.add(createPerkInfo(PerkFactory.Perks.Cooking));
		perks.add(createPerkInfo(PerkFactory.Perks.Melee));
		perks.add(createPerkInfo(PerkFactory.Perks.Crafting));
		perks.add(createPerkInfo(PerkFactory.Perks.Fitness));
		perks.add(createPerkInfo(PerkFactory.Perks.Strength));
		perks.add(createPerkInfo(PerkFactory.Perks.Blunt));
		perks.add(createPerkInfo(PerkFactory.Perks.Axe));
		perks.add(createPerkInfo(PerkFactory.Perks.Sprinting));
		perks.add(createPerkInfo(PerkFactory.Perks.Lightfoot));
		perks.add(createPerkInfo(PerkFactory.Perks.Nimble));
		perks.add(createPerkInfo(PerkFactory.Perks.Woodwork));
		perks.add(createPerkInfo(PerkFactory.Perks.Aiming));
		perks.add(createPerkInfo(PerkFactory.Perks.Reloading));
		perks.add(createPerkInfo(PerkFactory.Perks.Farming));
		perks.add(createPerkInfo(PerkFactory.Perks.Survivalist));
		perks.add(createPerkInfo(PerkFactory.Perks.Trapping));
		perks.add(createPerkInfo(PerkFactory.Perks.Passiv));
		perks.add(createPerkInfo(PerkFactory.Perks.Firearm));
		perks.add(createPerkInfo(PerkFactory.Perks.PlantScavenging));
		perks.add(createPerkInfo(PerkFactory.Perks.BluntParent));
		perks.add(createPerkInfo(PerkFactory.Perks.BladeParent));
		perks.add(createPerkInfo(PerkFactory.Perks.BluntGuard));
		perks.add(createPerkInfo(PerkFactory.Perks.BladeGuard));
		perks.add(createPerkInfo(PerkFactory.Perks.BluntMaintenance));
		perks.add(createPerkInfo(PerkFactory.Perks.BladeMaintenance));
		perks.add(createPerkInfo(PerkFactory.Perks.Doctor));
		perks.add(createPerkInfo(PerkFactory.Perks.Electricity));
		this.setPerkList(perks);
	}
	
	public void update() {
		if(!isDead()) {			
			super.update();
			updateHands();
			updateAnimations();
			for(Behavior behavior: listBehaviors) {
				behavior.updateBehavior();
			}
			updateSquare();
		}
	}
	
	private IsoGameCharacter.PerkInfo createPerkInfo(PerkFactory.Perks perks) {
		IsoGameCharacter.PerkInfo info = new IsoGameCharacter.PerkInfo();

		info.perkType = perks;
		info.perk = PerkFactory.getPerk(perks);
		info.level = 1;

		return info;
	}
	
	public InventoryItem getPrimaryWeapon() {
		return getPrimaryHandItem();
	}
	
	public void setPrimaryWeapon(InventoryItem item) {
		setPrimaryHandItem(item);
		
		byte hand = 0;
		byte has = 1;
		
		for(UdpConnection c : SledgeHammer.instance.getUdpEngine().getConnections()) {
           IsoPlayer p2 = GameServer.getAnyPlayerFromConnection(c);
           if(p2 != null) {
              ByteBufferWriter byteBufferWriter = c.startPacket();
              PacketTypes.doPacket(PacketTypes.Equip, byteBufferWriter);
              byteBufferWriter.putByte(hand);
              byteBufferWriter.putByte(has);
              byteBufferWriter.putInt(OnlineID);

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
	
	/**
	 * Updates the square the NPC is on.
	 */
	private void updateSquare() {
		int ix = (int) Math.floor(getX());
		int iy = (int) Math.floor(getY());
		int iz = (int) Math.floor(getZ());
		IsoGridSquare square = ServerMap.instance.getGridSquare(ix, iy, iz);
		this.setCurrent(square);
		this.setSquare(square);		
	}
	
	/**
	 * Updates Hand-related data.
	 */
	private void updateHands() {
		InventoryItem itemPrimary = getPrimaryHandItem();
		if(itemPrimary instanceof HandWeapon) {
			weapon = (HandWeapon) itemPrimary;
		} else {
			weapon = null;
		}
	}
	
	public IsoBuilding getCurrentBuilding() {
		return getCurrentBuilding();
	}
	
	public IsoGridSquare getCurrentSquare() {
		IsoGridSquare square = getCurrentSquare();
		if(square == null) {
			square = ServerMap.instance.getGridSquare((int)Math.floor(getX()), (int)Math.floor(getY()), (int)Math.floor(getZ()));
			setSquare(square);
			setCurrent(square);
		}
		return square;
	}
	
	public IsoChunk getCurrentChunk() {
		IsoGridSquare square = getCurrentSquare();
		if(square != null) {
			return square.getChunk();
		}
		return null;
	}
	
	public IsoCell getCurrentCell() {
		IsoGridSquare square = getCurrentSquare();
		if(square != null) {
			return square.getCell();
		}
		return null;
	}
	
	public LotHeader getCurrentLotHeader() {
		IsoCell cell = getCurrentCell();
		if(cell != null) {
			return cell.getCurrentLotHeader();
		}
		return null;
	}
	
	public Vector3f getDestination() {
		return this.destination;
	}

	public void updateAnimations() {
		
		if(weapon != null) {
			String weaponType = getWeaponType();
			this.strafeRAnim = "Strafe_Aim_" + weaponType + "_R";
            this.strafeAnim  = "Strafe_Aim_" + weaponType       ;
            this.walkRAnim   = "Walk_Aim_"   + weaponType + "_R";
            this.runAnim     = weapon.RunAnim                   ; 
            this.idleAnim    = weapon.IdleAnim                  ;
            this.lastWeapon  = weapon                           ;
		} else {
			this.strafeRAnim = "Strafe_R";
            this.strafeAnim  = "Strafe"  ;
            this.walkRAnim   = "Walk_R"  ;
            this.idleAnim    = "Idle"    ;
            this.runAnim     = "Run"     ;
            this.lastWeapon  = null      ;
		}
		
	}
	
	/**
	 * Returns the weapon type as a String. This is useful for animations.
	 * @return
	 */
	public String getWeaponType() {
		if(weapon != null) {			
			String weaponType = weapon.getSwingAnim();
			
			if(weaponType != null) {
				if(!weaponType.equals("Bat") && !weaponType.equals("Handgun") && !weaponType.equals("Rifle")) {
					weaponType = "Bat";
				}			
			}
			
			return weaponType;
		}
		
		return null;
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
	
	public void setDestination(float x, float y, float z) {
		this.destination.x = x;
		this.destination.y = y;
		this.destination.z = z;
	}
	
	public void setDestination(IsoObject o) {
		this.destination.set(o.getX(), o.getY(), o.getZ());
	}
	
	public void setTarget(IsoObject target) {
		followTarget = target;
		followObject = followTarget == null && followTargetDefault == null ? false : true;
	}
	
	public void setDefaultTarget(IsoMovingObject target) {
		followTargetDefault = target;
		if(target != null) followObject = true;
	}
	
	public List<Behavior> getBehaviorStates() {
		return listBehaviors;
	}
	
	public void addBehavior(Behavior behaviorState) {
		if(!listBehaviors.contains(behaviorState)) {
			listBehaviors.add(behaviorState);
		}
	}
	
	public void removeBehavior(Behavior behaviorState) {
		listBehaviors.remove(behaviorState);
	}
	
	
	public void setDirection(Vector2 vector) {
		DirectionFromVector(vector);
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
	 * Returns the item on the ground that the NPC will pick up.
	 * @return
	 */
	public IsoWorldInventoryObject getWorldItemTarget() {
		return worldItemTarget;
	}
	
	/**
	 * Sets the item on the ground that the NPC will pick up.
	 * @param worldItemTarget
	 */
	public void setWorldItemTarget(IsoWorldInventoryObject worldItemTarget) {
		this.worldItemTarget = worldItemTarget;
	}
	
	public String getIdleAnimation() {
		return this.idleAnim;
	}
	
	public String getWalkAnimation() {
		return this.walkAnim;
	}
	
	public String getRunAnimation() {
		return this.runAnim;
	}

	public boolean isFollowingObject() {
		return this.followObject;
	}
	
	public IsoObject getTarget() {
		return followTarget;
	}
	
	public IsoMovingObject getDefaultTarget() {
		return followTargetDefault;
	}
	
	public void setFollow(boolean flag) {
		this.followObject = flag;
	}

}