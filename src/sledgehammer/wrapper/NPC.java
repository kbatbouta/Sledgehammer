package sledgehammer.wrapper;

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
import zombie.inventory.types.HandWeapon;
import zombie.iso.IsoCell;
import zombie.iso.IsoMovingObject;
import zombie.iso.IsoObject;
import zombie.iso.IsoUtils;
import zombie.network.GameServer;

public class NPC extends IsoPlayer {

	private static final long serialVersionUID = 8799144318873059045L;

	private boolean followObject = false;
	private IsoObject followTarget = null;

	private Vector3f destination = new Vector3f();
	
	public NPC(IsoCell cell, SurvivorDesc desc, String username, int x, int y, int z) {
		super(cell, desc, x, y, z);
		
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

	public void follow(IsoMovingObject target) {
		followTarget = target;
		followObject = followTarget == null ? false : true;
	}

	private long timeThen = System.currentTimeMillis();
	
	private float speed = 0f;
	
	public void update() {
		
		super.update();
		if (followObject) {
			setDestination(followTarget.getX(), followTarget.getY(), followTarget.getZ());
			
			long timeNow = System.currentTimeMillis();
			long delta = timeNow - timeThen;
			
			if(delta >= 500L) {				
				faceDirection(followTarget);
				timeThen = timeNow;
			}
			
			float distanceFromTarget = DistTo(followTarget);
			
			if (distanceFromTarget > 5) {
				this.speed = this.getMoveSpeed();
				this.PlayAnim("Run");
				this.bRunning = true;
			} else if(distanceFromTarget > 2){
				this.speed = getPathSpeed();
				this.bRunning = false;
				this.PlayAnim("Walk");				
			} else {
				this.speed = 0.0F;
				this.bRunning = false;
				this.PlayAnim("Idle");
			}
			
			//Move(playerMoveDir);
			MoveForward(speed);
		}
		
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

	public float DistTo(IsoObject other) {
		return IsoUtils.DistanceManhatten(getX(), getY(), other.getX(), other.getY());
	}

	public void faceDirection(IsoObject other) {
		tempo.x  = other.getX();
		tempo.y  = other.getY();
		tempo.x -=       getX();
		tempo.y -=       getY();
		tempo.normalize();
		this.DirectionFromVector(tempo);
	}
	
	@Override
	public void hitConsequences(HandWeapon weapon, IsoGameCharacter wielder, boolean bIgnoreDamage, float damage,
			boolean bKnockdown) {
		System.out.println("NPC hit");

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

}
