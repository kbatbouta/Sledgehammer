package sledgehammer.modules.core;

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

import java.util.HashMap;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.event.ChatEvent;
import sledgehammer.event.ConnectEvent;
import sledgehammer.event.DeathEvent;
import sledgehammer.event.DisconnectEvent;
import sledgehammer.event.Event;
import sledgehammer.event.PVPKillEvent;
import sledgehammer.interfaces.EventListener;
import sledgehammer.manager.ChatManager;
import sledgehammer.util.ChatTags;
import sledgehammer.wrapper.Player;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.ServerOptions;
import zombie.sledgehammer.npc.NPC;

//Imports chat colors for short-hand.
import static sledgehammer.util.ChatTags.*;

public class CoreEventListener implements EventListener {

	private ModuleCore module;
	private Map<String, Long> mapPlayerTimeStamps;

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
		event.setIgnoreCore(true);
		ChatManager chat = SledgeHammer.instance.getChatManager();
		String text = event.getLogMessage();
		
		if(event.getID() == ChatEvent.ID) {
			handleChatEvent((ChatEvent) event);
		} else
		if(event.getID() == ConnectEvent.ID) {
			Player player = ((ConnectEvent)event).getPlayer();
			if(player.getProperty("muteglobal").equals("1")) {
				chat.messagePlayer(player, "[NOTICE]: ", COLOR_LIGHT_GREEN, "Global chat is currently muted for you. To unmute global chat, type \"/globalmute\".", COLOR_LIGHT_GREEN, true, true);
			}
		} else
		if(event.getID() == DisconnectEvent.ID) {
			
		} else
		if(event.getID() == DeathEvent.ID) {
			if(!event.shouldAnnounce() || ((DeathEvent)event).getPlayer().get() instanceof NPC) return;
			String username = ((DeathEvent)event).getPlayer().getUsername();
			if(username != null) {				
				Long timeStamp = mapPlayerTimeStamps.get(username.toLowerCase());
				if(timeStamp != null) {
					event.setHandled(true);
					event.setCanceled(true);
					return;
				}
				mapPlayerTimeStamps.put(username.toLowerCase(), System.currentTimeMillis());
				module.messageGlobal(null, null, text, COLOR_RED);
				SledgeHammer.instance.handleCommand("/thunder start", false);
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
			module.messageGlobal(null, null, text, COLOR_RED);
			SledgeHammer.instance.handleCommand((UdpConnection)null, "/thunder start", false);
		}
	}
	
	private void handleChatEvent(ChatEvent event) {
		
		Player player = event.getPlayer();
		String text = event.getText();

		UdpConnection connectionCommander = player.getConnection();

		text = ChatTags.stripTags(text, false);
		text = text.replaceAll("<", "&lt;");
		text = text.replaceAll(">", "&gt;");						
		if(event.isGlobal()) {
			if(player.getProperty("muteglobal").equals("1")) {
				module.messagePlayer(player, "[NOTICE]: ", COLOR_LIGHT_GREEN, "Global chat is currently muted. to unmute global chat, type \"/globalmute\".", COLOR_LIGHT_GREEN, true, true);
				return;
			}
			
			for (Player nextPlayer : module.getPlayers()) {
				module.messagePlayer(nextPlayer, event.getHeader(), event.getHeaderColor(), text, event.getTextColor(), true, false);
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
			
			for (Player nextPlayer : module.getPlayers()) {
				try {
					
					UdpConnection connection = nextPlayer.getConnection();
					
					if (connectionCommander == null 
					|| (connectionCommander != null && isoPlayer != null && connection.ReleventTo(isoPlayer.x, isoPlayer.y))
						) {
						
						if(connection.getConnectedGUID() != connectionCommander.getConnectedGUID()) {
							module.localMessage(connection, playerID, text, chatType, sayIt);							
						}
						
						if(ServerOptions.instance.getBoolean("LogLocalChat")) {							
							module.messagePlayer(nextPlayer, "[Local] " + event.getHeader(), event.getHeaderColor(), text, event.getTextColor(), true, true);
						}
						
					}
				} catch(NullPointerException e) {
					module.stackTrace(e);
					// This is when a player is checked, but disconnects asynchronously.
				}
			}
			event.setHandled(true);
		}
	}

	public void update() {
		mapPlayerTimeStamps.clear();
	}

	@Override
	public boolean runSecondary() {
		return true;
	}

}
