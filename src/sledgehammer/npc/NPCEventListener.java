package sledgehammer.npc;

import sledgehammer.event.ConnectEvent;
import sledgehammer.event.DisconnectEvent;
import sledgehammer.event.Event;
import sledgehammer.interfaces.EventListener;
import sledgehammer.wrapper.Player;
import zombie.core.raknet.UdpConnection;
import zombie.network.GameServer;

public class NPCEventListener implements EventListener {

	NPCManager engine = null;
	
	public NPCEventListener(NPCManager engine) {
		this.engine = engine;
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
			
			for(NPC npc : engine.getNPCS()) {
				GameServer.sendPlayerConnect(npc, connection);
			}
		}
	}

}
