package sledgehammer.npc;

import java.util.ArrayList;
import java.util.List;


import sledgehammer.SledgeHammer;
import zombie.ZombiePopulationManager;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.core.raknet.UdpEngine;
import zombie.inventory.types.HandWeapon;
import zombie.iso.Vector2;
import zombie.network.GameServer;
import zombie.network.PacketTypes;
import zombie.network.ServerLOS;

public class NPCManager {
	
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
	public NPCManager(SledgeHammer sledgeHammer) {
		this.sledgeHammer = sledgeHammer;
		
		// Initialize Lists.
		listNPCs = new ArrayList<>();
		
		// Event Listener for joining.
		eventListener = new NPCEventListener(this);
		SledgeHammer.instance.register(eventListener);
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
		
		npc.DoDeath((HandWeapon) null, npc);
		
		npc.removeFromWorld();
		npc.removeFromSquare();
		GameServer.PlayerToAddressMap.remove(npc);
		GameServer.IDToAddressMap.remove(Integer.valueOf(npc.OnlineID));
		GameServer.IDToPlayerMap.remove(Integer.valueOf(npc.OnlineID));
		GameServer.Players.remove(npc);
		
		for (UdpConnection connection : SledgeHammer.instance.getConnections()) {
			ByteBufferWriter b = connection.startPacket();
			PacketTypes.doPacket(PacketTypes.PlayerTimeout, b);
			b.putInt(npc.OnlineID);
			connection.endPacketImmediate();
		}

		ServerLOS.instance.removePlayer(npc);
		ZombiePopulationManager.instance.updateLoadedAreas();
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

				for (UdpConnection c : sledgeHammer.getConnections()) {
					ByteBufferWriter byteBufferWriter = c.startPacket();
					PacketTypes.doPacket((byte) 7, byteBufferWriter);
					byteBufferWriter.putShort((short) npc.OnlineID);
					byteBufferWriter.putByte((byte) npc.dir.index());
					
					byteBufferWriter.putFloat(npc.getX()             );
					byteBufferWriter.putFloat(npc.getY()             );
					byteBufferWriter.putFloat(npc.getZ()             );
					byteBufferWriter.putFloat(npc.playerMoveDir.x    );
					byteBufferWriter.putFloat(npc.playerMoveDir.y    );
					
					byteBufferWriter.putByte(npc.NetRemoteState);
					
					// Send the current animation state.
					if (npc.sprite != null) {
						byteBufferWriter.putByte((byte) npc.sprite.AnimStack.indexOf(npc.sprite.CurrentAnim));
					} else {
						byteBufferWriter.putByte((byte) 0);
					}
					
					byteBufferWriter.putByte((byte) ((int) npc.def.Frame));
					
					// Send the Animation frame delta and lighting data.
					byteBufferWriter.putFloat(npc.def.AnimFrameIncrease);
					byteBufferWriter.putFloat(npc.mpTorchDist          );
					byteBufferWriter.putFloat(npc.mpTorchStrength      );
					
					boolean legAnimation = npc.legsSprite != null && npc.legsSprite.CurrentAnim != null && npc.legsSprite.CurrentAnim.FinishUnloopedOnFrame == 0;
					
					if (npc.def.Finished) flags = (byte) (flags |  1);
					if (npc.def.Looped)   flags = (byte) (flags |  2);
					if (legAnimation)     flags = (byte) (flags |  4);
					if (npc.bSneaking)    flags = (byte) (flags |  8);
					if (torchCone)        flags = (byte) (flags | 16);
					if (onFire)           flags = (byte) (flags | 32);
					
					byteBufferWriter.putByte(flags);
					c.endPacketUnreliable();
					//c.endPacketSuperHighUnreliable();
				}
			
				
			}
		}
	}

	public List<NPC> getNPCS() {
		return this.listNPCs;
	}

	public void destroyNPCs() {
		for(NPC npc : listNPCs) {
			destroyNPC(npc);
		}
	}
	
	
}
