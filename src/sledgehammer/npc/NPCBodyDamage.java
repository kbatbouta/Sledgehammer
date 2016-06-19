package sledgehammer.npc;

import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.IsoZombie;
import zombie.characters.Stats;
import zombie.characters.BodyDamage.BodyDamage;
import zombie.characters.BodyDamage.BodyPart;
import zombie.characters.BodyDamage.BodyPartType;
import zombie.characters.skills.PerkFactory;
import zombie.core.Rand;
import zombie.debug.DebugLog;
import zombie.debug.DebugType;
import zombie.inventory.types.HandWeapon;
import zombie.network.GameServer;

public class NPCBodyDamage extends BodyDamage {

	public NPCBodyDamage(IsoGameCharacter ParentCharacter) {
		super(ParentCharacter);
	}

	public void DamageFromWeapon(HandWeapon weapon) {

		IsoGameCharacter character = getParentChar();
		
		if (weapon != null) {
			getParentChar().sendObjectChange("DamageFromWeapon", new Object[] { "weapon", weapon.getFullType() });
		}

		byte PainType = 1;
		int PartIndex1 = Rand.Next(BodyPartType.ToIndex(BodyPartType.Hand_L), BodyPartType.ToIndex(BodyPartType.Groin) + 1);
		
		character.splatBloodFloorBig(0.4F);
		character.splatBloodFloorBig(0.4F);
		character.splatBloodFloorBig(0.4F);
		
		boolean bleed = true;
		if (weapon.getCategories().contains("Blunt")) {
			bleed = false;
			PainType = 0;
		}

		if (bleed && !weapon.isAimedFirearm()) {
			PartIndex1 = Rand.Next(BodyPartType.ToIndex(BodyPartType.Hand_L), BodyPartType.ToIndex(BodyPartType.MAX));
			this.SetScratchedFromWeapon(PartIndex1, true);
		}

		float Damage = Rand.Next(weapon.getMinDamage(), weapon.getMaxDamage()) * 15.0F;
		if (weapon.isAimedFirearm()) {
			((BodyPart) this.getBodyParts().get(PartIndex1)).damageFromFirearm(Damage * 2.0F);
		}

		if (PartIndex1 == BodyPartType.ToIndex(BodyPartType.Head)       ) Damage *= 4.0F;
		if (PartIndex1 == BodyPartType.ToIndex(BodyPartType.Neck)       ) Damage *= 4.0F;
		if (PartIndex1 == BodyPartType.ToIndex(BodyPartType.Torso_Upper)) Damage *= 4.0F;

		AddDamage(PartIndex1, Damage);
		
		Stats stats = this.ParentChar.getStats();
		switch (PainType) {
			case 0:
				stats.Pain += this.getInitialThumpPain() * BodyPartType.getPainModifyer(PartIndex1);
				break;
			case 1:
				stats.Pain += this.getInitialScratchPain() * BodyPartType.getPainModifyer(PartIndex1);
				break;
			case 2:
				stats.Pain += this.getInitialBitePain() * BodyPartType.getPainModifyer(PartIndex1);
				break;
		}

		if (character.getStats().Pain > 100.0F) {
			character.getStats().Pain = 100.0F;
		}

	}
	
	public void AddRandomDamageFromZombie(IsoZombie zombie) {

		IsoGameCharacter character = getParentChar();

		// Reset the player's last hit.
		character.setHitBy((IsoGameCharacter) null);

		int meleeCombatMod = character.getMeleeCombatMod();

		byte PainType = 0;
		int baseChance = 75 + meleeCombatMod;
		
		byte baseBiteChance = 75;
		if (this.ParentChar.HasTrait("ThickSkinned")) baseChance = 85 + meleeCombatMod;
		if (this.ParentChar.HasTrait("ThinSkinned") ) baseChance = 65 + meleeCombatMod;

		int PartIndex1;
		if (!zombie.bCrawling) {
			PartIndex1 = Rand.Next(BodyPartType.ToIndex(BodyPartType.Hand_L), BodyPartType.ToIndex(BodyPartType.Groin) + 1);
			if ((PartIndex1 == BodyPartType.ToIndex(BodyPartType.Head) || PartIndex1 == BodyPartType.ToIndex(BodyPartType.Neck)) && Rand.Next(100) > 70) {
				boolean Damage = false;
				boolean doBreak = false;

				while (!doBreak) {
					do {
						if (Damage) {
							doBreak = true;
							break;
						}
						Damage = true;
						PartIndex1 = Rand.Next(BodyPartType.ToIndex(BodyPartType.MAX));
					} while (PartIndex1 != BodyPartType.ToIndex(BodyPartType.Head) && PartIndex1 != BodyPartType.ToIndex(BodyPartType.Neck));
					Damage = false;
				}
			}
		} else {
			if (Rand.Next(2) != 0) return;
			PartIndex1 = Rand.Next(BodyPartType.ToIndex(BodyPartType.UpperLeg_L), BodyPartType.ToIndex(BodyPartType.MAX));
		}

		float Damage1 = (float) Rand.Next(1000) / 1000.0F;
		Damage1 *= (float) (Rand.Next(10) + 10);

		AddDamage(PartIndex1, Damage1);

		if (GameServer.bServer && character instanceof IsoPlayer) {
			DebugLog.log(DebugType.Combat, "zombie did " + Damage1 + " dmg to " + ((IsoPlayer) character).username);
		}

		boolean Scratch = false;
		if (Rand.Next(100) > baseChance) {
			Scratch = true;
			if (Rand.Next(100) > baseBiteChance) {
				Scratch = false;
			}

			if (Scratch) {
				SetScratched(PartIndex1, true);
				if (getHealth() > 0.0F) {
					character.getEmitter().playSound("zombiescratch");
				}

				PainType = 1;
				if (GameServer.bServer && this.ParentChar instanceof IsoPlayer) {
					DebugLog.log(DebugType.Combat, "zombie scratched " + ((IsoPlayer) this.ParentChar).username);
				}

				character.Scratched();
			} else {
				if (getHealth() > 0.0F) {
					character.getEmitter().playSound("zombiebite");
				}

				SetBitten(PartIndex1, true);
				if (GameServer.bServer && this.ParentChar instanceof IsoPlayer) {
					DebugLog.log(DebugType.Combat, "zombie bite " + ((IsoPlayer) this.ParentChar).username);
				}

				PainType = 2;
				character.Bitten();
				character.splatBloodFloorBig(0.4F);
				character.splatBloodFloorBig(0.4F);
				character.splatBloodFloorBig(0.4F);
			}
		} else if (character.getPrimaryHandItem() != null
				&& !character.getPrimaryHandItem().getName().contains("Bare Hands")) {
			if (character.haveBladeWeapon()) {
				character.getXp().AddXP(PerkFactory.Perks.BladeGuard, 4.0F);
			} else {
				character.getXp().AddXP(PerkFactory.Perks.BluntGuard, 4.0F);
			}
		}

		Stats stats = this.ParentChar.getStats();
		switch (PainType) {
			case 0:
				stats.Pain += this.getInitialThumpPain() * BodyPartType.getPainModifyer(PartIndex1);
				break;
			case 1:
				stats.Pain += this.getInitialScratchPain() * BodyPartType.getPainModifyer(PartIndex1);
				break;
			case 2:
				stats.Pain += this.getInitialBitePain() * BodyPartType.getPainModifyer(PartIndex1);
				break;
		}

		if (character.getStats().Pain > 100.0F) {
			character.getStats().Pain = 100.0F;
		}

	}

}
