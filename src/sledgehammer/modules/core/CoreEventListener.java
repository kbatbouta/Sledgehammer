package sledgehammer.modules.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.event.ChatEvent;
import sledgehammer.event.ConnectEvent;
import sledgehammer.event.DeathEvent;
import sledgehammer.event.DisconnectEvent;
import sledgehammer.event.Event;
import sledgehammer.event.PVPKillEvent;
import sledgehammer.interfaces.EventListener;
import sledgehammer.util.Chat;
import sledgehammer.wrapper.NPC;
import sledgehammer.wrapper.Player;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;

public class CoreEventListener implements EventListener {

	private ModuleCore module;
	private Map<String, Long> mapPlayerTimeStamps;
	private long timeThen = 0L;

	public CoreEventListener(ModuleCore module) {
		this.module = module;
		mapPlayerTimeStamps = new HashMap<>();
	}
	
	public Map<String, Long> getPlayerTimeStamps() {
		return mapPlayerTimeStamps;
	}
	
	@Override
	public String[] getTypes() {
		return new String[] {ConnectEvent.ID, DisconnectEvent.ID, DeathEvent.ID, PVPKillEvent.ID, ChatEvent.ID};
	}

	@Override
	public void handleEvent(Event event) {
		Chat chat = SledgeHammer.instance.getChat();
		List<String> listGlobalMuters = chat.getGlobalMuters();
		String text = event.getLogMessage();
		
		if(event.getID() == ChatEvent.ID) {
			handleChatEvent((ChatEvent) event);
		} else
		if(event.getID() == ConnectEvent.ID) {
			Player player = ((ConnectEvent)event).getPlayer();
			String username = player.getUsername().toLowerCase();
			boolean isGlobalMuted = module.getGlobalMuted(username);
			if(isGlobalMuted) {
				if(!listGlobalMuters.contains(username)) {
					listGlobalMuters.add(username);
				}
				chat.messagePlayer(player.getConnection(), "[NOTICE]: ", Chat.CHAT_COLOR_LIGHT_GREEN, "Global chat is currently muted for you. To unmute global chat, type \"/globalmute\".", Chat.CHAT_COLOR_LIGHT_GREEN, true, true);
			}
		} else
		if(event.getID() == DisconnectEvent.ID) {
			String username = ((DisconnectEvent)event).getPlayer().getUsername();
			listGlobalMuters.remove(username);
		} else
		if(event.getID() == DeathEvent.ID) {
			if(!event.shouldAnnounce()) return;
			String username = ((DeathEvent)event).getPlayer().getUsername();
			if(username != null) {				
				Long timeStamp = mapPlayerTimeStamps.get(username.toLowerCase());
				if(timeStamp != null) {
					event.setHandled(true);
					event.setCanceled(true);
					return;
				}
				mapPlayerTimeStamps.put(username.toLowerCase(), System.currentTimeMillis());
				SledgeHammer.instance.getChat().globalMessage(null, null, text, Chat.CHAT_COLOR_RED);
				SledgeHammer.instance.handleCommand((UdpConnection)null, "/thunder start", false);
			}
		} else 
		if(event.getID() == PVPKillEvent.ID) {
			
			if(!event.shouldAnnounce()) return;
			
			Player killed = ((PVPKillEvent)event).getKilled();
			if(killed.get() instanceof NPC) return;
			
			String username = killed.getUsername();
			
			Long timeStamp = mapPlayerTimeStamps.get(username.toLowerCase());
			if(timeStamp != null) {
				event.setHandled(true);
				event.setCanceled(true);
				return;
			}
			mapPlayerTimeStamps.put(username.toLowerCase(), System.currentTimeMillis());
			SledgeHammer.instance.getChat().globalMessage(null, null, text, Chat.CHAT_COLOR_RED);
			SledgeHammer.instance.handleCommand((UdpConnection)null, "/thunder start", false);
		}
	}
	
	private void handleChatEvent(ChatEvent event) {
		
		Chat chat = SledgeHammer.instance.getChat();
		List<String> listGlobalMuters = chat.getGlobalMuters();
		
		Player player = event.getPlayer();
		String text = event.getText();
		UdpConnection connectionCommander = player.getConnection();

		text = Chat.getStripped(text, false);
		text = text.replaceAll("<", "&lt;");
		text = text.replaceAll(">", "&gt;");						
		if(event.isGlobal()) {
			if(listGlobalMuters.contains(player.getUsername().toLowerCase())) {
				chat.messagePlayer(player.getConnection(), "[NOTICE]: ", Chat.CHAT_COLOR_LIGHT_GREEN, "Global chat is currently muted. to unmute global chat, type \"/globalmute\".", Chat.CHAT_COLOR_LIGHT_GREEN, true, true);
				return;
			}
			
			for (UdpConnection connection : SledgeHammer.instance.getUdpEngine().connections) {
				chat.messagePlayer(connection, event.getHeader(), event.getHeaderColor(), text, event.getTextColor(), true, false);
			}			
		} else {
			IsoPlayer isoPlayer = player.get();
			int playerID = isoPlayer != null ? isoPlayer.OnlineID : -1;
			byte sayIt = (byte) (event.sayIt() ? 1 : 0);
			byte chatType = event.getChatType();

			if(isoPlayer != null && !text.startsWith("[SERVERMSG]")) {
				if(chatType == 0) {
					isoPlayer.Say(text);
				} else if(chatType == 1) {
					isoPlayer.SayWhisper(text);
				} else if(chatType == 2) {
					isoPlayer.SayShout(text);
				}
			}
			
			for (UdpConnection connection : SledgeHammer.instance.getUdpEngine().connections) {
				try {
					if (connectionCommander == null 
							|| connectionCommander != null
								&& connection.getConnectedGUID() != connectionCommander.getConnectedGUID()
								&& player != null
								&& connection.ReleventTo(isoPlayer.x, isoPlayer.y)) {
						chat.localMessage(connection, playerID, text, chatType, sayIt);
					}
				} catch(NullPointerException e) {
					// This is when a player is checked, but disconnects asynchronously.
				}
			}
		}
	}

	public void update() {
		long timeNow = System.currentTimeMillis();
		if(timeNow - timeThen  > 5000) {
			mapPlayerTimeStamps.clear();
			timeThen = timeNow;
		}
	}

}
