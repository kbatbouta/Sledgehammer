package sledgehammer.module.npc;

import sledgehammer.event.ConnectEvent;
import sledgehammer.event.DisconnectEvent;
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
    NPCManager npcManager = null;

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
        if (event.getID() == ConnectEvent.ID) {
            ConnectEvent connectEvent = (ConnectEvent) event;
            Player player = connectEvent.getPlayer();
            UdpConnection connection = player.getConnection();
            for (NPC npc : npcManager.getNPCS()) {
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