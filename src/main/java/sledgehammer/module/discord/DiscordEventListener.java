package sledgehammer.module.discord;

import sledgehammer.event.ChatMessageEvent;
import sledgehammer.event.Event;
import sledgehammer.interfaces.EventListener;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.chat.ChatMessage;
import sledgehammer.lua.core.Player;
import sledgehammer.util.ChatTags;

public class DiscordEventListener implements EventListener {

	private ModuleDiscord module;
	private DiscordBot bot;
	
	public DiscordEventListener(ModuleDiscord module) {
		this.module = module;
		this.bot = module.getBot();
	}
	
	@Override
	public String[] getTypes() {
		return new String[] {ChatMessageEvent.ID};
	}

	@Override
	public void onEvent(Event event) {
		if(event.getID().equals(ChatMessageEvent.ID)) {
			handleChatEvent((ChatMessageEvent) event);
		}
	}
	
	public void handleChatEvent(ChatMessageEvent event) {
		ChatMessage message = event.getMessage();
		ChatChannel chatChannel = event.getChatChannel();
		String chatChannelName = chatChannel.getChannelName().toLowerCase();
		if(chatChannelName.equalsIgnoreCase("global")) {
			String compiled = message.getMessage();
            Player player = message.getPlayer();
            if(player != null) {
                compiled = player.getName() + ": " + message.getMessage();
            }
			module.getBot().say(module.getPublicChannelName(), false, ChatTags.stripTags(compiled, false));
		} else {
			String compiled = message.getMessage();
			Player player = message.getPlayer();
			if(player != null) {
				compiled = player.getName() + ": " + message.getMessage();
			}
			compiled = ChatTags.stripTags(compiled, false);
			if(chatChannelName.equalsIgnoreCase("local")) {
				if(!message.getMessage().equalsIgnoreCase("ZzzZZZzzzz")) {					
					module.getBot().say("console", false, "[" + chatChannel.getChannelName() + "] : " + compiled);
					return;
				}
			} else if(chatChannel.isPublicChannel()) {
				module.getBot().say("channel_" + DiscordBot.toAsciiString(chatChannel.getChannelName()), false, compiled);
				return;
			}
			module.getBot().say("console", false, "[" + chatChannel.getChannelName() + "] : " + compiled);
		}
	}

	@Override
	public boolean runSecondary() {
		return false;
	}

}
