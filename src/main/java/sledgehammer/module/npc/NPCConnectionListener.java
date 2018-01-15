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

import sledgehammer.event.core.player.ConnectEvent;
import sledgehammer.event.core.player.DisconnectEvent;
import sledgehammer.event.Event;
import sledgehammer.interfaces.EventListener;
import sledgehammer.lua.core.Player;
import sledgehammer.manager.NPCManager;
import zombie.core.raknet.UdpConnection;
import zombie.network.GameServer;
import zombie.sledgehammer.npc.NPC;

/**
 * EventListener to assist the NPCManager to send NPC player info to connecting
 * Players, since NPCs do not have a UDPConnection instance.
 * <p>
 * TODO: Rewrite NPCs and remove the NPCManager.
 *
 * @author Jab
 */
public class NPCConnectionListener implements EventListener {

    /**
     * The NPCManager instance.
     */
    private NPCManager npcManager;

    /**
     * Main constructor.
     *
     * @param npcManager The NPCManager instance.
     */
    public NPCConnectionListener(NPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @Override
    public void onEvent(Event event) {
        if (event.getID().equals(ConnectEvent.ID)) {
            ConnectEvent connectEvent = (ConnectEvent) event;
            Player player = connectEvent.getPlayer();
            UdpConnection connection = player.getConnection();
            for (NPC npc : npcManager.getNPCs()) {
                GameServer.sendPlayerConnect(npc, connection);
            }
        }
    }

    @Override
    public String[] getTypes() {
        return new String[]{ConnectEvent.ID, DisconnectEvent.ID};
    }

    @Override
    public boolean runSecondary() {
        return false;
    }
}