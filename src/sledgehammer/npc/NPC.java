package sledgehammer.npc;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import fmod.fmod.DummySoundEmitter;
import fmod.fmod.DummySoundListener;
import sledgehammer.util.ZUtil;
import zombie.ai.states.StaggerBackState;
import zombie.characters.DummyCharacterSoundEmitter;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.SurvivorDesc;
import zombie.characters.skills.PerkFactory;
import zombie.inventory.InventoryItem;
import zombie.inventory.types.HandWeapon;
import zombie.iso.IsoCell;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.network.GameServer;
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
	
	/**
	 * Updates Hand-related data.
	 */
	private void updateHands() {
		InventoryItem itemPrimary = getPrimaryHandItem();
		if(itemPrimary instanceof HandWeapon) {
//			HandWeapon oldWeapon = weapon;
			weapon = (HandWeapon) itemPrimary;
//			if(oldWeapon != null) {
//				// If the weapon has changed.
//				if(!weapon.getName().equals(oldWeapon.getName())) {
//					// Update the current animations.
//					updateAnimations();
//				}				
//			}
		} else {
			weapon = null;
		}
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
	
	private IsoGameCharacter.PerkInfo createPerkInfo(PerkFactory.Perks perks) {
		IsoGameCharacter.PerkInfo info = new IsoGameCharacter.PerkInfo();

		info.perkType = perks;
		info.perk = PerkFactory.getPerk(perks);
		info.level = 1;

		return info;
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
		} else {
			this.DoDeath(weapon, wielder);
		}
		
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
	
	public Vector3f getDestination() {
		return this.destination;
	}

	public void setDestination(float x, float y, float z) {
		this.destination.x = x;
		this.destination.y = y;
		this.destination.z = z;
	}
	
	public void setDestination(IsoObject o) {
		this.destination.set(o.getX(), o.getY(), o.getZ());
	}

	
	public void updateAnimations() {
		
		String weaponType = "bat";
		
		if(weapon != null) {
//			weaponType = weapon.getSwingAnim();
//			if(!weaponType.equals("Bat") && !weaponType.equals("Handgun") && !weaponType.equals("Rifle")) {
//				weaponType = "Bat";
//          }
//			this.strafeRAnim = "Strafe_Aim_" + weaponType + "_R";
//          this.strafeAnim  = "Strafe_Aim_" + weaponType       ;
//          this.walkRAnim   = "Walk_Aim_"   + weaponType + "_R";
            this.runAnim     = weapon.RunAnim                   ; 
            this.idleAnim    = weapon.IdleAnim                  ;
            this.lastWeapon  = weapon                           ;
		} else {
//			this.strafeRAnim = "Strafe_R";
//          this.strafeAnim  = "Strafe"  ;
//          this.walkRAnim   = "Walk_R"  ;
            this.idleAnim    = "Idle"    ;
            this.runAnim     = "Run"     ;
            this.lastWeapon  = null      ;
		}
		
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

}