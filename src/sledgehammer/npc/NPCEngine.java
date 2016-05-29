package sledgehammer.npc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


import sledgehammer.SledgeHammer;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.core.raknet.UdpEngine;
import zombie.iso.Vector2;
import zombie.network.GameServer;
import zombie.network.PacketTypes;

public class NPCEngine {
	
	/**
	 * A SledgeHammer instance to follow proper OOP code methods.
	 */
	private SledgeHammer sledgeHammer;
	
	/**
	 * Long variable to measure update-tick deltas.
	 */
	private long timeThen;
	
	/**
	 * List of live NPC instances on the server.
	 */
	private List<NPC> listNPCs;
	
	NPCEventListener eventListener = null;
	
	/**
	 * Main constructor.
	 * @param sledgeHammer
	 */
	public NPCEngine(SledgeHammer sledgeHammer) {
		this.sledgeHammer = sledgeHammer;
		
		// Initialize Lists.
		listNPCs = new ArrayList<>();
		
		// Event Listener for joining.
		eventListener = new NPCEventListener(this);
		SledgeHammer.instance.registerEventListener(eventListener);
	}
	
	public NPC addNPC(NPC npc) {
		
		//long guid = random.nextLong();
		GameServer.PlayerToAddressMap.put(npc, (long) npc.PlayerIndex);
		GameServer.playerToCoordsMap.put(Integer.valueOf(npc.PlayerIndex), new Vector2());
		GameServer.IDToPlayerMap.put(npc.PlayerIndex, npc);
		GameServer.Players.add(npc);

		UdpEngine udpEngine = SledgeHammer.instance.getUdpEngine();
		for (UdpConnection c : udpEngine.connections) {
			GameServer.sendPlayerConnect(npc, c);
		}

		listNPCs.add(npc);
		return npc;
	}
	
	public void destroyNPC(NPC npc) {
		GameServer.PlayerToAddressMap.remove(npc);
		GameServer.playerToCoordsMap.remove(npc.PlayerIndex);
		GameServer.IDToPlayerMap.remove(npc.PlayerIndex);
		GameServer.Players.remove(npc);
		
		// TODO: Send out disconnection of NPC Player.
//		UdpEngine udpEngine = SledgeHammer.instance.getUdpEngine();
//		for (UdpConnection c : udpEngine.connections) {
//			
//		}
		
	}
	
	public void update() {
		
		List<NPC> listDead = new ArrayList<>();
		
		for (int index = 0; index < listNPCs.size(); index++) {
			NPC npc = listNPCs.get(index);
			if(npc.isDead()) {
				listDead.add(npc);
			} else {
				npc.preupdate();
				npc.update();
				npc.postupdate();				
			}
		}
		
		// Remove the dead NPCs from the list.
		for (NPC npc : listDead) {
			listNPCs.remove(npc);
		}
		
		if (System.currentTimeMillis() - timeThen > 200) {
			for (NPC npc : listNPCs) {
				
				ByteBuffer bb = ByteBuffer.allocate(65535);
				ByteBufferWriter bbw = new ByteBufferWriter(bb);
				
				byte flags = 0;
				
				boolean animFlag = npc.legsSprite != null && npc.legsSprite.CurrentAnim != null && npc.legsSprite.CurrentAnim.FinishUnloopedOnFrame == 0;
				
				if (npc.def.Finished ) flags = (byte) (flags |  1);
				if (npc.def.Looped   ) flags = (byte) (flags |  2);
				if (animFlag         ) flags = (byte) (flags |  4);
				if (npc.bSneaking    ) flags = (byte) (flags |  8);
				if (npc.isTorchCone()) flags = (byte) (flags | 16);
				if (npc.isOnFire()   ) flags = (byte) (flags | 32);
				
				boolean torchCone = (flags & 16) != 0;
				boolean onFire    = (flags & 32) != 0;
				byte animationByte = npc.sprite != null ? (byte) npc.sprite.AnimStack.indexOf(npc.sprite.CurrentAnim): (byte) 0; 

				bb.clear();
				
				PacketTypes.doPacket(PacketTypes.PlayerUpdateInfo, bbw);
				
				bbw.putShort((short) npc.OnlineID);
				bbw.putByte((byte) npc.dir.index());
				
				bbw.putFloat(npc.getX()             );
				bbw.putFloat(npc.getY()             );
				bbw.putFloat(npc.getZ()             );
				bbw.putFloat(npc.playerMoveDir.x    );
				bbw.putFloat(npc.playerMoveDir.y    );
				bbw.putByte (npc.NetRemoteState     );
				
				// Send the current animation state.
				bbw.putByte(animationByte);
				
				bbw.putByte((byte) ((int) npc.def.Frame));
				
				// Send the Animation frame delta and lighting data.
				bbw.putFloat(npc.def.AnimFrameIncrease);
				bbw.putFloat(npc.mpTorchDist          );
				bbw.putFloat(npc.mpTorchStrength      );
				
				if (npc.def.Finished) flags = (byte) (flags |  1);
				if (npc.def.Looped  ) flags = (byte) (flags |  2);
				if (animFlag        ) flags = (byte) (flags |  4);
				if (npc.bSneaking   ) flags = (byte) (flags |  8);
				if (torchCone       ) flags = (byte) (flags | 16);
				if (onFire          ) flags = (byte) (flags | 32);
				
				bbw.putByte(flags);
				
				UdpEngine udpEngine = sledgeHammer.getUdpEngine();
				for (UdpConnection cconnection : udpEngine.connections) {
					cconnection.setPacket(bb);					
					cconnection.endPacketSuperHighUnreliable();
				}
			
				
			}
		}
	}

	public List<NPC> getNPCS() {
		return this.listNPCs;
	}
	
	
}
