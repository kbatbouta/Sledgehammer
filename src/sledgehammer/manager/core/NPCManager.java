package sledgehammer.manager.core;

/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.event.ConnectEvent;
import sledgehammer.event.DisconnectEvent;
import sledgehammer.event.Event;
import sledgehammer.interfaces.EventListener;
import sledgehammer.manager.Manager;
import sledgehammer.module.npc.ModuleNPC;
import sledgehammer.npc.action.Action;
import sledgehammer.npc.action.ActionAttackCharacter;
import sledgehammer.npc.action.ActionFollowTargetDirect;
import sledgehammer.npc.action.ActionGrabItemOnGround;
import sledgehammer.objects.Player;
import zombie.core.raknet.UdpConnection;
import zombie.network.GameServer;
import zombie.sledgehammer.PacketHelper;
import zombie.sledgehammer.npc.NPC;
import zombie.sledgehammer.npc.action.ActionFollowTargetPath;

/**
 * Manager class designed to handle NPC components, as well as update them.
 * 
 * @author Jab
 */
public class NPCManager extends Manager {
	
	public static final String NAME = "NPCManager";
	
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
	 */
	public NPCManager() {

		// Initialize Lists.
		listNPCs = new ArrayList<>();
		
		// Initializes the NPC Core Actions.
		initializeActions();
		
		// Event Listener for joining.
		connectionListener = new ConnectionListener(this);
		getSledgeHammer().register(connectionListener);
		
		moduleNPC = new ModuleNPC();
		getSledgeHammer().getModuleManager().registerModule(moduleNPC);
	}
	
	/**
	 * Initializes all default Actions for Behavior classes to use for NPCs.
	 */
	private void initializeActions() {
		
		// Initialize the Map.
		mapActions = new HashMap<>();
		
		// Register all Actions by the static 'NAME' field.
		addAction(ActionAttackCharacter.NAME   , new ActionAttackCharacter()   );
		addAction(ActionGrabItemOnGround.NAME  , new ActionGrabItemOnGround()  );
		addAction(ActionFollowTargetPath.NAME  , new ActionFollowTargetPath()  );
		addAction(ActionFollowTargetDirect.NAME, new ActionFollowTargetDirect());

	}
	
	/**
	 * Registers a NPC instance to the NPCManager.
	 * 
	 * @param npc
	 * 
	 * @return
	 */
	public NPC addNPC(NPC npc) {
		npc = PacketHelper.addNPC(npc);
		listNPCs.add(npc);
		return npc;
	}
	
	/**
	 * Destroys a NPC, unregistering it, and killing it in-game.
	 * 
	 * @param npc
	 */
	public void destroyNPC(NPC npc) {
		PacketHelper.destroyNPC(npc);
		listNPCs.remove(npc);
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

		@Override
		public boolean runSecondary() {
			return false;
		}
	}

	@Override
	public String getName() { return NAME; }

	@Override
	public void onLoad(boolean debug) {}

	@Override
	public void onStart() {}

	/**
	 * Updates all NPCs registered.
	 */
	@Override
	public void onUpdate() {
		
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
			
			PacketHelper.updateNPCs(listNPCs);
		}
	}

	@Override
	public void onShutDown() {}
}
