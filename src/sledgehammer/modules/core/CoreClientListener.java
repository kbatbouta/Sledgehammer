package sledgehammer.modules.core;
import java.util.Set;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.event.ClientEvent;
import sledgehammer.event.Event;
import sledgehammer.interfaces.EventListener;
import sledgehammer.objects.LuaObject;
import sledgehammer.objects.LuaObject_RequestChatChannels;
import sledgehammer.objects.LuaObject_RequestInfo;
import sledgehammer.wrapper.Player;
import zombie.network.GameServer;

public class CoreClientListener implements EventListener {

	private ModuleCore module;
	
	public CoreClientListener(ModuleCore module) {
		this.module = module;
	}
	
	public String[] getTypes() {
		return new String[] { ClientEvent.ID };
	}
	

	public void handleEvent(Event e) {
		// Cast to proper Event sub-class.
		ClientEvent event = (ClientEvent) e;
		
		// Get event content.
		String module     = event.getModule();
		String command    = event.getCommand();
		KahluaTable table = event.getTable();
		Player player     = event.getPlayer();
		
		if (module.equalsIgnoreCase("core")) {
			if (command.equalsIgnoreCase("handshake")) {
				// We just want to ping back to the client saying we received the request.
				event.respond();
			}
			
			if(command.equalsIgnoreCase("requestInfo")) {
				
				LuaObject_RequestInfo info = new LuaObject_RequestInfo();
				info.setPlayerID(player.getID());

				event.respond(info);
			}
			
			if(command.equalsIgnoreCase("getChatChannels")) {
				
				Set<String> chatChannels = SledgeHammer.instance.getChatManager().mapChannels.keySet();
				
				LuaObject_RequestChatChannels request = new LuaObject_RequestChatChannels();
				
				for(String channel : chatChannels) {
					request.addChannel(channel);
				}
				
				event.respond(request);
			}
		}
	}

	@Override
	public boolean runSecondary() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
