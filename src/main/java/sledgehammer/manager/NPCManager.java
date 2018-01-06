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
package sledgehammer.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.module.npc.ModuleNPC;
import sledgehammer.module.npc.NPCConnectionListener;
import sledgehammer.npc.action.Action;
import sledgehammer.npc.action.ActionAttackCharacter;
import sledgehammer.npc.action.ActionFollowTargetDirect;
import sledgehammer.npc.action.ActionGrabItemOnGround;
import zombie.sledgehammer.PacketHelper;
import zombie.sledgehammer.npc.NPC;
import zombie.sledgehammer.npc.action.ActionFollowTargetPath;

/**
 * Manager class designed to handle NPC components, as well as update them.
 * <p>
 * TODO: Rewrite NPCs and remove the NPCManager.
 *
 * @author Jab
 */
public class NPCManager extends Manager {

    /** The String name of the Manager. */
    public static final String NAME = "NPCManager";

    /** Map storing Actions for NPCs. */
    private Map<String, Action> mapActions;
    /** Long variable to measure update-tick deltas. */
    private long timeThen;
    /** List of NPCs alive on the server. */
    private List<NPC> listNPCs;
    /** The ModuleNPC instance in the Core plug-in. */
    private ModuleNPC moduleNPC;

    /**
     * EventListener to handle sending NPC data to connecting players.
     */
    NPCConnectionListener connectionListener = null;

    /**
     * Main constructor.
     */
    public NPCManager() {
        // Initialize Lists.
        listNPCs = new ArrayList<>();
        // Initializes the NPC Core Actions. initializeActions();
        // Event Listener for joining.
        connectionListener = new NPCConnectionListener(this);
        SledgeHammer.instance.register(connectionListener);
    }

    /**
     * Updates all NPCs registered.
     */
    @Override
    public void onUpdate() {
        List<NPC> listDead = new ArrayList<>();
        for (NPC npc : listNPCs) {
            if (npc.isDead()) {
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
    public String getName() {
        return NAME;
    }

    /**
     * Initializes all default Actions for Behavior classes to use for NPCs.
     */
    void initializeActions() {
        // Initialize the Map.
        mapActions = new HashMap<>();
        // Register all Actions by the static 'NAME' field. @formatter:off
		addAction(ActionAttackCharacter.NAME   , new ActionAttackCharacter()   );
		addAction(ActionGrabItemOnGround.NAME  , new ActionGrabItemOnGround()  );
		addAction(ActionFollowTargetPath.NAME  , new ActionFollowTargetPath()  );
		addAction(ActionFollowTargetDirect.NAME, new ActionFollowTargetDirect());
		// @formatter:on
    }

    /**
     * Registers a NPC Object to the NPCManager.
     *
     * @param npc The NPC to add.
     * @return Returns the NPC given.
     */
    public NPC addNPC(NPC npc) {
        npc = PacketHelper.addNPC(npc);
        listNPCs.add(npc);
        return npc;
    }

    /**
     * Destroys a NPC, un-registering it and killing it in-game.
     *
     * @param npc The NPC to destroy.
     */
    public void destroyNPC(NPC npc) {
        PacketHelper.destroyNPC(npc);
        listNPCs.remove(npc);
    }

    /**
     * Adds an Action to the Map of Actions that a NPC can call to act on.
     *
     * @param name   The String name of the Action.
     * @param action The Action to add.
     */
    public void addAction(String name, Action action) {
        mapActions.put(name, action);
    }

    /**
     * @param name The String name of the Action.
     * @return Returns an Action instance, based on the name given.
     */
    public Action getAction(String name) {
        return mapActions.get(name);
    }

    /**
     * @return Returns a List of all NPCs registered in the NPCManager.
     */
    public List<NPC> getNPCS() {
        return this.listNPCs;
    }

    /**
     * Destroys all active NPCs registered in the NPCManager.
     */
    public void destroyNPCs() {
        for (NPC npc : listNPCs) {
            destroyNPC(npc);
        }
    }

    /**
     * @return Returns the ModuleNPC instance in the Core plug-in.
     */
    public ModuleNPC getModule() {
        return moduleNPC;
    }
}