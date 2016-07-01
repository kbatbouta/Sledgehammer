package zombie.sledgehammer.npc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import fmod.fmod.DummySoundListener;
import fmod.fmod.SoundEmitter;
import sledgehammer.SledgeHammer;
import sledgehammer.modules.ModuleNPC;
import sledgehammer.npc.NPCBodyDamage;
import sledgehammer.npc.NPCBodyDamageSyncUpdater;
import sledgehammer.npc.action.Action;
import sledgehammer.npc.action.PathAction;
import sledgehammer.npc.behavior.Behavior;
import sledgehammer.util.ZUtil;
import zombie.PathfindManager;
import zombie.ai.astar.AStarPathFinder;
import zombie.ai.astar.IPathfinder;
import zombie.ai.astar.Mover;
import zombie.ai.astar.Path;
import zombie.ai.astar.AStarPathFinder.PathFindProgress;
import zombie.ai.states.StaggerBackState;
import zombie.characters.CharacterSoundEmitter;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.IsoZombie;
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
import zombie.iso.objects.IsoWorldInventoryObject;
import zombie.network.BodyDamageSync;
import zombie.network.GameServer;
import zombie.network.PacketTypes;
import zombie.network.ServerLOS;
import zombie.network.ServerMap;

// TODO: Work on transferring items to the deadBody onDeath.
public class NPC extends IsoPlayer implements IPathfinder {

	private static final long serialVersionUID = 8799144318873059045L;
	private List<Behavior> listBehaviors;
	private Vector3f destination = new Vector3f();
	private String walkAnim = "Walk";
	private String runAnim  = "Run";
	private String idleAnim = "Idle";
	private IsoObject followTarget = null;
	private boolean followObject = false;
	private int[] pathPosition = new int[] {0,0,0};
	private int[] pathDestination = new int[] {0,0,0};
	private int[] previousPathDestination = null;
	String actionNameLast = "";
	private int destinationRadius = 2;
	private String currentAnimation = "";
	private String currentAnimationUnlooped = "";
	private int pIndex;
	
	private Vector2 pathVector = new Vector2(0.0F, 0.0F);
	
	private IsoGameCharacter attackTarget = null;
	
	private BodyDamageSync.Updater damageUpdater = null;
	
	private IsoMovingObject followTargetDefault = null;
	
	private long timeActionLast = 0L;
	
	private boolean canRun = true;
	
	private boolean canWalk = true;
	
	/**
	 * An item to be set for the NPC to go to, and pick up.
	 */
	private IsoWorldInventoryObject worldItemTarget = null;
	
	private HandWeapon weapon = null;
	
	private Action nextAction;
	
	private float distanceToRun = 5;
	
	private float distanceToWalk = 1;
	
	private float arrivalRadius = 1;

	private boolean actionLooped = false;
	
	private boolean arrived = false;
	private Action currentAction;
	private String walkAimAnim;
	private String attackFloorAnim;
	private String attackAnim;
	
	public NPC(IsoCell cell, SurvivorDesc desc, String username, int x, int y, int z) {
		super(cell, desc, x, y, z);
		
		// Initialize the Lists.
		listBehaviors = new ArrayList<>();
		
		initializePerks();

		setBodyDamage(new NPCBodyDamage(this));
		
		damageUpdater = new NPCBodyDamageSyncUpdater(this);
		
		updateHands();
		
		// Update position in world.
		updateSquare();

		this.PlayerIndex          =                                    1;
		this.username             =                             username;
		this.OnlineChunkGridWidth =                                    1;
		this.OnlineID             =  (short) ZUtil.random.nextInt(30000);
		this.bRemote              =                                 true;
		this.invisible            =                                false;
		this.emitter              =      new CharacterSoundEmitter(this);
		this.soundListener        =  new DummySoundListener(PlayerIndex);
		this.testemitter          =                   new SoundEmitter();
		
		// Sets the health to 100%.
		setHealth(1.0F);
		
		// Adds the NPC to the server's 'Line-Of-Sight' engine, so Zombies can see it.
		ServerLOS.instance.addPlayer(this);
	}
	
	@Override
	public void hitConsequences(HandWeapon weapon, IsoGameCharacter wielder, boolean bIgnoreDamage, float damage, boolean bKnockdown) {
		if (bIgnoreDamage) {
			this.sendObjectChange("Shove", new Object[] { "hitDirX", Float.valueOf(this.getHitDir().getX()), "hitDirY", Float.valueOf(this.getHitDir().getY()), "force", Float.valueOf(this.getHitForce()) });
			return;
		}

		BodyDamage.DamageFromWeapon(weapon);

		if (wielder instanceof IsoPlayer) {
			if (!bIgnoreDamage) {
				if (weapon.isAimedFirearm()) {
					Health -= damage * 0.7F;
				} else {
					Health -= damage * 0.15F;
				}
			}
		} else if (!bIgnoreDamage) {
			if (weapon.isAimedFirearm()) {
				Health -= damage * 0.7F;
			} else {
				Health -= damage * 0.15F;
			}
		}

		if (Health > 0.0F && BodyDamage.getHealth() > 0.0F && (!weapon.isAlwaysKnockdown() && !bKnockdown)) {
			if (weapon.isSplatBloodOnNoDeath()) {
				splatBlood(3, 0.3F);
			}

			if (weapon.isKnockBackOnNoDeath()) {
				if (wielder.xp != null) {
					wielder.xp.AddXP(PerkFactory.Perks.Strength, 2.0F);
				}

				stateMachine.changeState(StaggerBackState.instance());
			}
			
		} else {
			DoDeath(weapon, wielder);
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
			
			// Boolean to store whether or not the action is completed.
			boolean finishedActionLoop = false;
			
			if(nextAction != null) {				
				currentAction = nextAction;
				nextAction = null;
			}
			
			if (currentAction != null) {

				// Execute this action. Store the state of the action.
				finishedActionLoop = currentAction.act(this);

			}

			// If the current action is to be ran only once, or the looping
			// animation has finished,
			if (!isCurrentActionLooped() || finishedActionLoop) {
				currentAction = null;
			}
			
			super.update();
			
			updateHands();
			updateAnimations();
			
			for(Behavior behavior: listBehaviors) {
				behavior.updateBehavior();
			}
			
			updateSquare();
			
			BodyDamage.Update();
			
			// Update the body damage synchronizer.
			damageUpdater.update();
		}
	}
	
	/**
	 * Updates the square the NPC is on.
	 */
	private void updateSquare() {
		
		// Grab the 'X', 'Y', and 'Z' coordinates, floor them, and cast to an Integer.
		int ix = (int) Math.floor(getX());
		int iy = (int) Math.floor(getY());
		int iz = (int) Math.floor(getZ());
		
		// Use the integer values to locate the IsoGridSquare the NPC is current on.
		IsoGridSquare square = ServerMap.instance.getGridSquare(ix, iy, iz);
		
		// Set the current square for the IsoMovingObject API.
		setCurrent(square);

		// Set the current square for the IsoObject API.
		setSquare(square);		
	}
	
	/**
	 * Updates the local HandWeapon field for NPC's.
	 */
	private void updateHands() {
		
		// Grab the Primary InventoryItem from the primary hand (first weapon on the top of the screen).
		InventoryItem itemPrimary = getPrimaryHandItem();
		
		// If the item is in-fact a weapon,
		if(itemPrimary instanceof HandWeapon) {
			
			// Set it as the casted object to HandWeapon.
			weapon = (HandWeapon) itemPrimary;
		} else {
			
			// Set the weapon to null. This helps the code know quicker that the item in primary is not a weapon.
			weapon = null;
		}
	}
	
	private IsoGameCharacter.PerkInfo createPerkInfo(PerkFactory.Perks perks) {
		IsoGameCharacter.PerkInfo info = new IsoGameCharacter.PerkInfo();

		info.perkType = perks;
		info.perk = PerkFactory.getPerk(perks);
		info.level = 1;

		return info;
	}
	
	public HandWeapon getPrimaryWeapon() {
		InventoryItem primaryHandItem = getPrimaryHandItem();
		if(primaryHandItem instanceof HandWeapon) {
			return (HandWeapon) primaryHandItem;
		}
		return null;
	}
	
	public void setPrimaryWeapon(InventoryItem item) {
		setPrimaryHandItem(item);
		
		byte hand = 0;
		byte has = 1;
		
		for(UdpConnection connection : SledgeHammer.instance.getUdpEngine().getConnections()) {
           IsoPlayer p2 = GameServer.getAnyPlayerFromConnection(connection);
           if(p2 != null) {
              ByteBufferWriter byteBufferWriter = connection.startPacket();
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
              
              connection.endPacketImmediate();
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

				// TODO Ignore Z checks for now. Possible to-do
				IsoGridSquare lSquare = cell.getGridSquare(x, y, sz);

				if (lSquare != null) {

					@SuppressWarnings("unchecked")
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
	
	@Override
	public IsoGridSquare getCurrentSquare() {
		IsoGridSquare square = super.getCurrentSquare();
		if(square == null) {
			square = ServerMap.instance.getGridSquare((int)Math.floor(getX()), (int)Math.floor(getY()), (int)Math.floor(getZ()));
			setSquare(square);
			setCurrent(square);
		}
		return square;
	}
	
	public IsoChunk getCurrentChunk() {
		IsoGridSquare square = getCurrentSquare();
		if(square != null) return square.getChunk();
		return null;
	}
	
	public IsoCell getCurrentCell() {
		IsoGridSquare square = getCurrentSquare();
		if(square != null) return square.getCell();
		return null;
	}
	
	public LotHeader getCurrentLotHeader() {
		IsoCell cell = getCurrentCell();
		if(cell != null) return cell.getCurrentLotHeader();
		return null;
	}
	
	public Vector3f getDestination() {
		return this.destination;
	}

	public void updateAnimations() {
		
		if(weapon != null) {
			String weaponType = getWeaponType();
			
			this.strafeRAnim     = "Strafe_Aim_" + weaponType + "_R";
            this.strafeAnim      = "Strafe_Aim_" + weaponType       ;
            this.walkRAnim       = "Walk_Aim_"   + weaponType + "_R";
            this.runAnim         = weapon.RunAnim                   ; 
            this.idleAnim        = weapon.IdleAnim                  ;
            this.walkAimAnim     = "Walk_Aim_" + weaponType         ;
            this.attackAnim      = "Attack_" + weaponType           ;
            this.attackFloorAnim = "Attack_Floor_" + weaponType     ;
            this.lastWeapon      = weapon                           ;
		} else {
			this.strafeRAnim = "Strafe_R";
            this.strafeAnim  = "Strafe"  ;
            this.walkRAnim   = "Walk_R"  ;
            this.idleAnim    = "Idle"    ;
            this.runAnim     = "Run"     ;
            this.walkAimAnim = "Walk"    ;
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
	
	public List<IsoZombie> getNearestZombies(int radius) {
		List<IsoZombie> listZombies = new ArrayList<>();
		
		IsoGridSquare currentSquare = getSquare();
		IsoCell cell = getCurrentCell();
		
		int sx = currentSquare.getX();
		int sy = currentSquare.getY();
		int sz = currentSquare.getZ();

		// Go through each square
		for (int y = sy - radius; y < sy + radius; y++) {
			for (int x = sx - radius; x < sx + radius; x++) {

				// TODO Ignore Z checks for now. Possible to-do.
				IsoGridSquare lSquare = cell.getGridSquare(x, y, sz);

				if (lSquare != null) {
					for(Object o : lSquare.getMovingObjects()) {
						if(o != null && o instanceof IsoZombie) {
							IsoZombie zombie = (IsoZombie) o;
							listZombies.add(zombie);
						}
					}
				}
			}
		}
		
//		if(ModuleNPC.DEBUG) {
//			System.out.println("Npc->getNearestZombies(" + radius + "): Returned " + listZombies.size() + " zombies.");
//		}
		
		return listZombies;
	}
	
	/**
	 * Returns the nearest zombie in a list.
	 * @param listZombies
	 * @return
	 */
	public IsoZombie getNearestZombie(List<IsoZombie> listZombies) {
		
		// The initial zombie will be null.
		IsoZombie nearestZombie = null;
		
		// Set the initial minimums to maximum value, so any zombie will initially be closer.
		float distanceMinimum = Float.MAX_VALUE;
		float nextDistance = Float.MAX_VALUE;
		
		// Go through each zombie in the list.
		for(IsoZombie nextZombie : listZombies) {
			
			// Make sure the zombie in the list is valid.
			if(nextZombie != null) {

				// Grab the distance from the zombie to the NPC.
				nextDistance = this.getDistance(nextZombie);
				
				// If this distance is less than the current minimum distance,
				if(nextDistance < distanceMinimum) {
					// Set the next zombie as the closest.
					nearestZombie = nextZombie;
					
					// Make sure to note the next minimum distance.
					distanceMinimum = nextDistance;
				}
				
			}
			
		}
		
		// Finally return the nearest zombie. If there's none, then return null.
		return nearestZombie;
	}
	
	public void addPath(int x, int y, int z) {
		
		// Grab the NPC's rounded coordinates.
		int nx = (int)getX();
		int ny = (int)getY();
		int nz = (int)getZ();

		if(nx == x && ny == y && nz == z || isAdjacentTo(x, y, z)) {
			return;
		}
		
		setPath((Path)null);
		
		// Add the job to the manager.
		PathfindManager.instance.AddJob(this, this, nx, ny, nz, x, y, z);
		
		// Set the initial state of the finder.
		getFinder().progress = AStarPathFinder.PathFindProgress.notyetfound;
	}
	
	public int[] getPathPosition() {
		return pathPosition;
	}
	
	public int[] getPreviousPathDestination() {
		return previousPathDestination;
	}
	
	public int[] getPathDestination() {
		return pathDestination;
	}
	
	public void faceDirection(IsoObject other) {
		if(other == null) return;
		Vector2 vector = new Vector2();
		vector.x  = other.getX();
		vector.y  = other.getY();
		vector.x -=       getX();
		vector.y -=       getY();
		vector.normalize();
		setDirection(vector);
	}
	
	public IsoGameCharacter getAttackTarget() {
		return attackTarget;
	}
	
	public void setAttackTarget(IsoGameCharacter target) {
		attackTarget = target;
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
	
	public Action getAction(String name) {
		return SledgeHammer.instance.getNPCManager().getAction(name);
	}
	
	public void actNext(String name) {
		this.nextAction = getAction(name);
		this.actionLooped = false;
	}
	
	public void actImmediately(String name) {
		Action action = getAction(name);
		if(action != null) {			
			action.act(this);
		} else if(ModuleNPC.DEBUG) {
			System.out.println("NPC: action is null: " + name);
		}
	}
	
	public void actIndefinitely(String name) {
		
		if(currentAction != null && actionNameLast.equals(name)) {
			return;
		}
		
		actionNameLast = name;
		
		this.nextAction = getAction(name);
		this.actionLooped = true;
		
		if(nextAction != null && nextAction instanceof PathAction) {
			
			setPathTarget();
			setArrived(false);
		}
	}
	
	public void stopAction() {
		this.actionLooped = false;
	}
	
	public boolean isCurrentActionLooped() {
		return this.actionLooped;
	}
	
	public long getLastActionTime() {
		return timeActionLast;
	}
	
	public void setActionTime(long time) {
		timeActionLast = time;
	}
	
	public float DistTo(IsoObject other) {
		return IsoUtils.DistanceManhatten(getX(), getY(), other.getX(), other.getY());
	}
	
	/**
	 * Sets the following distance threshold to run.
	 * @param distance
	 */
	public void setDistanceToRun(float distance) {
		this.distanceToRun = distance;
	}
	
	/**
	 * Proxy method for 'getNPC().MoveForward()'.
	 * @param distance
	 */
	public void moveForward(float distance) {
		MoveForward(distance);
	}
	
	/**
	 * Sets the following distance threshold to walk.
	 * @param distance
	 */
	public void setDistanceToWalk(float distance) {
		this.distanceToWalk = distance;
	}
	
	public float getArrivalRadius() {
		return arrivalRadius;
	}
	
	public void setArrivalRadius(float radius) {
		arrivalRadius = radius;
	}

	public float getDistanceToWalk() {
		return distanceToWalk;
	}

	public float getDistanceToRun() {
		return distanceToRun;
	}

	public void setRunning(boolean b) {
		bRunning = true;
	}

	public void setArrived(boolean flag) {
		arrived = true;
	}
	
	public boolean hasArrived() {
		return arrived;
	}

	public Action getCurrentAction() {
		return currentAction;
	}
	
	public Action getNextAction() {
		return nextAction;
	}
	
	public boolean canWalk() {
		return canWalk;
	}
	
	public boolean canRun() {
		return canRun;
	}
	
	public void setCanWalk(boolean flag) {
		canWalk = flag;
	}

	public void setCanRun(boolean flag) {
		canRun = flag;
	}
	
	public String getWalkAndAimAnimation() {
		return walkAimAnim;
	}
	
	public String getAttackOnFloorAnimation() {
		return attackFloorAnim;
	}
	
	public String getAttackAnimation() {
		return attackAnim;
	}
	
	public Vector2 getPathVector() {
		return pathVector;
	}

	@Override
	public void Succeeded(Path path, Mover mover) {

		setPathIndex(0);

		Path p = getPath();
		if (p != null) {
			for (int n = 0; n < p.getLength(); ++n) {
				Path.stepstore.push(p.getStep(n));
			}
		}

		setPath(path);
		getFinder().progress = AStarPathFinder.PathFindProgress.found;

		if (currentAction instanceof PathAction) {
			((PathAction) currentAction).onPathSuccess(this, mover, path);
		} else {
			// TODO: Manual path handling.
		}
	}

	@Override
	public void Failed(Mover mover) {
		
		getFinder().progress = AStarPathFinder.PathFindProgress.failed;
		
		if(currentAction instanceof PathAction) {
			((PathAction)currentAction).onPathFailure(this, mover);
		} else {
			//TODO: Manual path handle failure.
		}
	}
	
	public void setPathTarget() {
		IsoObject target = getPrimaryTarget();
		
		if(target != null) {			
			int x = (int) target.getX();
			int y = (int) target.getY();
			int z = (int) target.getZ();

			int[] previousDestination = getPreviousPathDestination();
			
			int[] destination = getPathDestination();
			
			if(getFinder().progress == PathFindProgress.notrunning) {
				pathPosition            = new int[] {(int)getX(), (int)getY(), (int)getZ()};
				pathDestination         = new int[] {x, y, z};
				previousPathDestination = new int[] {x, y, z};
				
				setPathIndex(0);
				addPath(x, y, z);
				
			} else {
				
				// If the target is within the destination radius, don't try to path to the target.
				if( (  Math.abs(previousDestination[0] - x) < destinationRadius 
						&& Math.abs(previousDestination[1] - y) < destinationRadius 
						&& previousDestination[1] == z)) {
					
					// Attack targets need to be accurately distanced.
					if(target != getAttackTarget()) {						
						return;
					}
				}

				// Going nowhere.
				if(isAdjacentTo(target)) {
					return;
				}
				
				previousDestination = getPathDestination();
				
				destination[0] = x;
				destination[1] = y;
				destination[2] = z;
				
				// Set the original position to be the NPC's position.
				pathPosition[0] = (int) getX();
				pathPosition[1] = (int) getY();
				pathPosition[2] = (int) getZ();
				
				setPathTargetX(x);
				setPathTargetY(y);
				setPathTargetZ(z);
				
				setPathIndex(0);
				addPath(x, y, z);
			}
			
			

		} else {
			System.out.println("NPC: Target is null");
		}
	}
	
	public IsoObject getPrimaryTarget() {
		
		IsoObject primaryFollowTarget = getTarget();
		IsoObject secondaryFollowTarget = getDefaultTarget();
		
		IsoObject focusTarget = primaryFollowTarget;
		
		if(focusTarget == null) {
			focusTarget = secondaryFollowTarget;	
		}
		
		return focusTarget;
	}
	
	/**
	 * Proxy method for 'IsoGameCharacter.PlayAnim(String animation)'.
	 * 
	 * Plays a looped animation.
	 * 
	 * E.X: playAnimation("Idle");
	 * 
	 * @param string
	 */
	public void playAnimation(String animation) {
		if(animation.equals(currentAnimation)) {
			return;
		}
		currentAnimation = animation;
		PlayAnim(animation);
	}
	
	/**
	 * Proxy method for 'IsoGameCharacter.PlayAnimUnlooped(String animation)'.
	 * 
	 * Plays a animation once.
	 * 
	 * E.X: playAnimation("SitDown");
	 * 
	 * @param animation
	 */
	public void playAnimationUnlooped(String animation) {
		if(animation.equals(currentAnimationUnlooped)) {
			return;
		}
		currentAnimationUnlooped = animation;
		PlayAnimUnlooped(animation);
	}
	
	public int getPathIndex() {
		return pIndex;
	}

	public void setPathIndex(int pathIndex) {
		pIndex = pathIndex;
	}

	public void faceTarget() {
		faceDirection(getPrimaryTarget());
	}
	
	public boolean isAdjacentTo(IsoObject other) {
		int ox = (int)other.getX();
		int oy = (int)other.getY();
		int oz = (int)other.getZ();
		
		return isAdjacentTo(ox, oy, oz);
		
	}
	
	public boolean isAdjacentTo(int ox, int oy, int oz) {
		
		int nx = (int)getX();
		int ny = (int)getY();
		int nz = (int)getZ();
		
		int ax = Math.abs(ox - nx);
		int ay = Math.abs(oy - ny);
		int az = Math.abs(oz - nz);
		
		boolean x = ax < 2;
		boolean y = ay < 2;
		boolean z = az < 2;
		
		return (x && y && z) && !(ax == 0 && ay == 0 && az == 0);
		
	}

	
}