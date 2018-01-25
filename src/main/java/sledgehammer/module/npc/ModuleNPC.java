/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

package sledgehammer.module.npc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.language.LanguagePackage;
import sledgehammer.npc.action.Action;
import sledgehammer.npc.action.ActionAttackCharacter;
import sledgehammer.npc.action.ActionFollowTargetDirect;
import sledgehammer.npc.action.ActionGrabItemOnGround;
import sledgehammer.plugin.Module;
import zombie.characters.IsoGameCharacter;
import zombie.characters.SurvivorDesc;
import zombie.characters.SurvivorFactory;
import zombie.sledgehammer.PacketHelper;
import zombie.sledgehammer.npc.NPC;
import zombie.sledgehammer.npc.action.ActionFollowTargetPath;

/**
 * Module to handle NPC data and operations for the Core Plug-in of
 * Sledgehammer.
 * <p>
 * TODO: Rewrite NPCs and remove the NPCManager.
 *
 * @author Jab
 */
public class ModuleNPC extends Module {

    /**
     * Map storing Actions for NPCs.
     */
    private Map<String, Action> mapActions;
    /**
     * Long variable to measure update-tick deltas.
     */
    private long timeThen;
    /**
     * List of NPCs alive on the server.
     */
    private List<NPC> listNPCs;
    /**
     * Debug flag for the Module.
     */
    public static final boolean DEBUG = true;

    /**
     * The Map of native IsoGameCharacter Objects, identified by the
     * Sledgehammer NPC wrapper.
     */
    protected Map<NPC, IsoGameCharacter> mapSpawns;

    /**
     * The CommandListener implementation for the Module.
     */
    private NPCCommandListener commandListener;
    private NPCEventListener eventListener;
    private LanguagePackage languagePackage;

    @Override
    public void onLoad() {
        mapSpawns = new HashMap<>();
        listNPCs = new ArrayList<>();
        loadLanguagePackage();
        initializeActions();
        eventListener = new NPCEventListener(this);
        commandListener = new NPCCommandListener(this);
    }

    @Override
    public void onStart() {
        register(commandListener);
        register(eventListener);
    }

    @Override
    public void onUpdate(long delta) {
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
    public void onStop() {
        unregister(commandListener);
        unregister(eventListener);
    }

    @Override
    public void onUnload() {
        commandListener = null;
        eventListener = null;
    }

    private void loadLanguagePackage() {
        File langDir = getLanguageDirectory();
        boolean override = !isLangOverriden();
        saveResourceAs("lang/npc_en.yml", new File(langDir, "npc_en.yml"), override);
        languagePackage = new LanguagePackage(getLanguageDirectory(), "npc");
    }

    /**
     * Initializes all default Actions for Behavior classes to use for NPCs.
     */
    private void initializeActions() {
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
     * @param name The String name of the NPC.
     * @param x    The Float x-coordinate to spawn the NPC.
     * @param y    The Float y-coordinate to spawn the NPC.
     * @param z    The Float z-coordinate to spawn the NPC.
     * @return Returns a NPC instance with the String name, spawned at the given
     * Float coordinates.
     */
    public NPC createFakePlayer(String name, float x, float y, float z) {
        SurvivorDesc desc = SurvivorFactory.CreateSurvivor();
        System.out.println("SurvivorDesc ID: " + desc.getID());
        NPC npc = new NPC(null, desc, name, (int) x, (int) y, (int) z);
        return addNPC(npc);
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
    public List<NPC> getNPCs() {
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

    public LanguagePackage getLanguagePackage() {
        return this.languagePackage;
    }
}