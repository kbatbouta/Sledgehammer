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
package sledgehammer.module.npc;

import java.util.HashMap;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.plugin.Module;
import zombie.characters.IsoGameCharacter;
import zombie.characters.SurvivorDesc;
import zombie.characters.SurvivorFactory;
import zombie.iso.IsoCell;
import zombie.sledgehammer.npc.NPC;

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
    private NPCCommandListener commandListener = null;

    @Override
    public void onLoad() {
        mapSpawns = new HashMap<>();
        commandListener = new NPCCommandListener(this);
        register(commandListener);
    }

    @Override
    public void onUnload() {
        unregister(commandListener);
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
        return SledgeHammer.instance.getNPCManager().addNPC(npc);
    }
}