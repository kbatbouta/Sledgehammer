package sledgehammer.npc.action;

import sledgehammer.SledgeHammer;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.IsoZombie;
import zombie.characters.Moodles.MoodleType;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.inventory.types.HandWeapon;
import zombie.iso.Vector2;
import zombie.network.PacketTypes;
import zombie.network.ServerOptions;
import zombie.sledgehammer.npc.NPC;

public class ActionAttackCharacter extends Action {

	public static final String NAME = "Action->AttackCharacter";

	@Override
	public boolean act(NPC npc) {

		IsoGameCharacter target = npc.getAttackTarget();

		HandWeapon weapon = npc.getPrimaryWeapon();
		
		npc.faceDirection(target);

		if (weapon != null && npc.CanAttack()) {
			
			target.Hit(weapon, npc, 1F, false, 0F);

			float damageSplit = 5F;

			if (npc.isAttackWasSuperAttack()) {
				damageSplit *= 10.0F;
			}

			switch (npc.getMoodles().getMoodleLevel(MoodleType.Endurance)) {
			case 0:
			default:
				break;
			case 1:
				damageSplit *= 0.5F;
				break;
			case 2:
				damageSplit *= 0.2F;
				break;
			case 3:
				damageSplit *= 0.1F;
				break;
			case 4:
				damageSplit *= 0.05F;
			}
			
			Vector2 oPos = new Vector2(npc.getX(), npc.getY());
			Vector2 tPos = new Vector2(target.getX(), target.getY());
			tPos.x -= oPos.x;
			tPos.y -= oPos.y;
			float dist2 = tPos.getLength();
			float rangeDel = 1.0F;
			if (!weapon.isRangeFalloff()) {
				rangeDel = dist2 / weapon.getMaxRange(npc);
			}

			if (rangeDel < 0.3F) {
				rangeDel = 1.0F;
			}

			for (UdpConnection connection : SledgeHammer.instance.getConnections()) {
				ByteBufferWriter bb = connection.startPacket();
				PacketTypes.doPacket((byte) 26, bb);
				bb.putByte((byte) ((IsoPlayer) npc).PlayerIndex);
				if (target instanceof IsoZombie) {
					bb.putByte((byte) 1);
					bb.putShort(((IsoZombie) target).OnlineID);
				} else {
					bb.putByte((byte) 0);
					bb.putShort((short) ((IsoPlayer) target).OnlineID);
					npc.setSafetyCooldown(npc.getSafetyCooldown() + (float) ServerOptions.instance.SafetyCooldownTimer.getValue());
				}

				bb.putUTF(weapon.getFullType());
				bb.putFloat(damageSplit);
				bb.putBoolean(weapon == null);
				bb.putFloat(rangeDel);
				bb.putFloat(target.getX());
				bb.putFloat(target.getY());
				bb.putFloat(target.getZ());
				bb.putFloat(npc.getX());
				bb.putFloat(npc.getY());
				bb.putFloat(npc.getZ());
				bb.putFloat(target.getHitForce());
				bb.putFloat(target.getHitDir().x);
				bb.putFloat(target.getHitDir().y);
				bb.putFloat(((IsoPlayer) npc).useChargeDelta);
				connection.endPacket();
			}

		}

		// Make sure target is valid.
		if (target != null) {

		}

		return false;
	}

	@Override
	public String getName() {
		return NAME;
	}

}
