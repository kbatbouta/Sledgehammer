package sledgehammer.modules.core;
import java.util.List;

import sledgehammer.SledgeHammer;
import sledgehammer.event.ClientEvent;
import sledgehammer.event.Event;
import sledgehammer.interfaces.EventListener;
import sledgehammer.objects.Player;
import sledgehammer.objects.chat.ChatChannel;
import sledgehammer.requests.RequestChatChannels;
import sledgehammer.requests.RequestInfo;

public class CoreClientListener implements EventListener {

	ModuleCore module;
	
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
		Player player     = event.getPlayer();
		
		if (module.equalsIgnoreCase("core")) {
			
			if (command.equalsIgnoreCase("handshake")) {
				// We just want to ping back to the client saying we received the request.
				event.respond();
			}
			
			if(command.equalsIgnoreCase("requestInfo")) {
				
				RequestInfo info = new RequestInfo();
				info.setSelf(player);

				event.respond(info);
			}
			
			if(command.equalsIgnoreCase("getChatChannels")) {
				
				List<ChatChannel> channels = SledgeHammer.instance.getChatManager().getChannelsForPlayer(player);
				RequestChatChannels request = new RequestChatChannels();
				
				for(ChatChannel channel : channels) {
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
