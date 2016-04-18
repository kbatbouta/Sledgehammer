package zirc.modules;

import zirc.Chat;
import zirc.ZIRC;
import zirc.event.ConnectEvent;
import zirc.event.DisconnectEvent;
import zirc.event.Event;
import zirc.interfaces.EventListener;
import zirc.wrapper.Player;

public class CoreEventListener implements EventListener {

	private ModuleCore module;

	public CoreEventListener(ModuleCore module) {
		this.module = module;
	}
	
	@Override
	public String[] getTypes() {
		return new String[] {ConnectEvent.ID, DisconnectEvent.ID};
	}

	@Override
	public void handleEvent(Event event) {
		if(event.getName() == ConnectEvent.ID) {
			Chat chat = ZIRC.instance.getChat();
			Player player = ((ConnectEvent)event).getPlayer();
			String username = player.getUsername().toLowerCase();
			boolean isGlobalMuted = module.getGlobalMuted(username);
			if(isGlobalMuted) {
				if(!chat.listGlobalMuters.contains(username)) {
					chat.listGlobalMuters.add(username);
				}
				chat.messagePlayer(player.getConnection(), "[NOTICE]: ", Chat.CHAT_COLOR_LIGHT_GREEN, "Global chat is currently muted for you. To unmute global chat, type \"/globalmute\".", Chat.CHAT_COLOR_LIGHT_GREEN, true, true);
			}
		} else
		if(event.getName() == DisconnectEvent.ID) {
			Chat chat = ZIRC.instance.getChat();
			String username = ((DisconnectEvent)event).getPlayer().getUsername();
			chat.listGlobalMuters.remove(username);
		}
	}

}
