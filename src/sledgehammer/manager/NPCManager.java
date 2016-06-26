package sledgehammer.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.event.ConnectEvent;
import sledgehammer.event.DisconnectEvent;
import sledgehammer.event.Event;
import sledgehammer.interfaces.EventListener;
import sledgehammer.modules.ModuleNPC;
import sledgehammer.npc.Action;
import sledgehammer.npc.ActionAttackTarget;
import sledgehammer.npc.ActionGrabItemOnGround;
import sledgehammer.npc.ActionMoveToLocation;
import sledgehammer.npc.ActionMoveToLocationAStar;
import sledgehammer.npc.NPC;
import sledgehammer.wrapper.Player;
import zombie.ZombiePopulationManager;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.core.raknet.UdpEngine;
import zombie.inventory.types.HandWeapon;
import zombie.iso.Vector2;
import zombie.network.GameServer;
import zombie.network.PacketTypes;
import zombie.network.ServerLOS;

/**
 * Manager class designed to handle NPC components, as well as update them.
 * 
 * @author Jab
 */
public class NPCManager {
	
	/**
	 * Instance of SledgeHammer. While this is statically accessible through the
	 * singleton, maintaining an OOP hierarchy is a good practice.
	 */
	private SledgeHammer sledgeHammer;
	
	/**
	 * Map containing all actions to influence NPCs.
	 */
	private Map<String, Action> mapActions;
	
	/**
	 * Long variable to measure update-tick deltas.
	 */
	private long timeThen;
	
	/**
	 * List of live NPC instances on the server.
	 */
	private List<NPC> listNPCs;
	
	private ModuleNPC moduleNPC;
	
	/**
	 * EventListener to handle sending NPC data to connecting players.
	 */
	ConnectionListener connectionListener = null;
	
	/**
	 * Main constructor.
	 * 
	 * @param sledgeHammer
	 */
	public NPCManager(SledgeHammer sledgeHammer) {
		this.sledgeHammer = sledgeHammer;
		
		// Initialize Lists.
		listNPCs = new ArrayList<>();
		
		// Initializes the NPC Core Actions.
		initializeActions();
		
		// Event Listener for joining.
		connectionListener = new ConnectionListener(this);
		sledgeHammer.register(connectionListener);
		
		moduleNPC = new ModuleNPC();
		sledgeHammer.getModuleManager().registerModule(moduleNPC);
	}
	
	/**
	 * Initializes all default Actions for Behavior classes to use for NPCs.
	 */
	private void initializeActions() {
		
		// Initialize the Map.
		mapActions = new HashMap<>();
		
		// Register all Actions by the static 'NAME' field.
		addAction(ActionAttackTarget.NAME       , new ActionAttackTarget()       );
		addAction(ActionMoveToLocation.NAME     , new ActionMoveToLocation()     );
		addAction(ActionGrabItemOnGround.NAME   , new ActionGrabItemOnGround()   );
		addAction(ActionMoveToLocationAStar.NAME, new ActionMoveToLocationAStar());

	}
	
	/**
	 * Registers a NPC instance to the NPCManager.
	 * 
	 * @param npc
	 * 
	 * @return
	 */
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
	
	/**
	 * Destroys a NPC, unregistering it, and killing it in-game.
	 * 
	 * @param npc
	 */
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
	
	/**
	 * Updates all NPCs registered.
	 */
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
		
		long timeNow = System.currentTimeMillis();
		
		// Update the NPCs every 200ms.
		if (timeNow - timeThen > 200) {
			
			// Set the last time updated to now.
			timeThen = timeNow;
			
			// Go through each NPC in the list, and send the players the information.
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
				}
				
			}
		}
	}
	
	/**
	 * Adds an Action instance to the list of Actions that a NPC can call to Act on.
	 * 
	 * @param name
	 * 
	 * @param action
	 */
	public void addAction(String name, Action action) {
		mapActions.put(name, action);
	}
	
	/**
	 * Returns an Action instance, based on the name given.
	 * 
	 * @param name
	 * 
	 * @return
	 */
	public Action getAction(String name) {
		return mapActions.get(name);
	}

	/**
	 * Returns the List of all the NPCs registered.
	 * 
	 * @return
	 */
	public List<NPC> getNPCS() {
		return this.listNPCs;
	}

	/**
	 * Destroys all active NPCs registered.
	 */
	public void destroyNPCs() {
		
		for(NPC npc : listNPCs) {
			destroyNPC(npc);
		}
		
	}
	
	public ModuleNPC getModule() {
		return moduleNPC;
	}
	
	/**
	 * Implemented EventListener to assist the NPCManager to send NPC player info to connecting Players, since NPCs do not have a UDPConnection instance.
	 * 
	 * @author Jab
	 */
	private class ConnectionListener implements EventListener {

		NPCManager npcManager = null;
		
		public ConnectionListener(NPCManager engine) {
			this.npcManager = engine;
		}
		
		@Override
		public String[] getTypes() {
			return new String[] {ConnectEvent.ID , DisconnectEvent.ID};
		}

		@Override
		public void handleEvent(Event event) {
			if(event.getID() == ConnectEvent.ID) {
				
				ConnectEvent connectEvent = (ConnectEvent) event;
				
				Player player = connectEvent.getPlayer();
				UdpConnection connection = player.getConnection();
				
				for(NPC npc : npcManager.getNPCS()) {
					GameServer.sendPlayerConnect(npc, connection);
				}
			}
		}
	}	
}
