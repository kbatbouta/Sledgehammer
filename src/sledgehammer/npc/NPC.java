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
import zombie.network.ServerMap;

// TODO: Work on transferring items to the deadBody onDeath.
public class NPC extends IsoPlayer {

	private static final long serialVersionUID = 8799144318873059045L;
	private List<Behavior> listBehaviors;
	private Vector3f destination = new Vector3f();
	private float speed = 0f;
	private String runAnim;
	
	public NPC(IsoCell cell, SurvivorDesc desc, String username, int x, int y, int z) {
		super(cell, desc, x, y, z);
		
		// Update position in world.
		updateSquare();

		listBehaviors = new ArrayList<>();
		
		// Generates an index.
		int playerIndex = 0;
		for (int index = Byte.MIN_VALUE; index < 0; index++) {
			Object objectPlayer = GameServer.IDToPlayerMap.get(index);
			if (objectPlayer == null) {
				playerIndex = index;
				break;
			}
		}

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
		super.update();
		for(Behavior behavior: listBehaviors) {
			behavior.updateBehavior();
		}
		
		updateSquare();
	}
	
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
	
	public float getNPCSpeed() {
		return this.speed;
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

	public float getSpeed() {
		return this.speed;
	}
	
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
	public void setAnimations() {
		
		String weaponType = "bat";
		
		InventoryItem itemPrimary = getPrimaryHandItem();
		if(itemPrimary != null && itemPrimary instanceof HandWeapon) {
			HandWeapon handPrimary = (HandWeapon) itemPrimary;
			weaponType = handPrimary.getSwingAnim();
			if(!weaponType.equals("Bat") && !weaponType.equals("Handgun") && !weaponType.equals("Rifle")) {
				weaponType = "Bat";
            }
			this.strafeRAnim = "Strafe_Aim_" + weaponType + "_R";
            this.strafeAnim  = "Strafe_Aim_" + weaponType       ;
            this.walkAnim    = "Walk_Aim_"   + weaponType       ;
            this.walkRAnim   = "Walk_Aim_"   + weaponType + "_R";
            this.runAnim     = handPrimary.RunAnim              ; 
            this.lastWeapon  = handPrimary                      ;
		} else {
			this.strafeRAnim = "Strafe_R";
            this.strafeAnim  = "Strafe"  ;
            this.walkAnim    = "Walk"    ;
            this.walkRAnim   = "Walk_R"  ;
            this.runAnim     = "Run"     ;
            this.lastWeapon  = null      ;
		}
		
	}
	
	public String getWalkAnimation() {
		return this.walkAnim;
	}
	
	public String getRunAnimation() {
		return this.runAnim;
	}

}