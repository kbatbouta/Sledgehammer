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
package sledgehammer.module.core;

import java.util.HashMap;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.event.DeathEvent;
import sledgehammer.event.Event;
import sledgehammer.event.PVPKillEvent;
import sledgehammer.interfaces.EventListener;
import sledgehammer.lua.core.Player;
import sledgehammer.util.ChatTags;
import zombie.core.raknet.UdpConnection;
import zombie.sledgehammer.npc.NPC;

//Imports chat colors for short-hand.
import static sledgehammer.util.ChatTags.*;

/**
 * TODO: Document.
 * 
 * @author Jab
 */
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
		return new String[] { DeathEvent.ID, PVPKillEvent.ID };
	}

	@Override
	public void onEvent(Event event) {
		event.setIgnoreCore(true);
		String text = event.getLogMessage();

		if (event.getID() == DeathEvent.ID) {
			if (!event.shouldAnnounce() || ((DeathEvent) event).getPlayer().getIso() instanceof NPC)
				return;
			String username = ((DeathEvent) event).getPlayer().getUsername();
			if (username != null) {
				Long timeStamp = mapPlayerTimeStamps.get(username.toLowerCase());
				if (timeStamp != null) {
					event.setHandled(true);
					event.setCanceled(true);
					return;
				}
				mapPlayerTimeStamps.put(username.toLowerCase(), System.currentTimeMillis());
				module.sendGlobalMessage(ChatTags.COLOR_RED + " " + text);
				SledgeHammer.instance.handleCommand("/thunder start", false);
			}
		} else if (event.getID() == PVPKillEvent.ID) {
			if (!event.shouldAnnounce()) {
				return;
			}
			Player killed = ((PVPKillEvent) event).getKilled();
			if (killed.getIso() instanceof NPC) {
				return;
			}
			String username = killed.getUsername();
			Long timeStamp = mapPlayerTimeStamps.get(username.toLowerCase());
			if (timeStamp != null) {
				event.setHandled(true);
				event.setCanceled(true);
				return;
			}
			mapPlayerTimeStamps.put(username.toLowerCase(), System.currentTimeMillis());
			module.sendGlobalMessage(COLOR_RED + " " + text);
			SledgeHammer.instance.handleCommand((UdpConnection) null, "/thunder start", false);
		}
	}

    @Override
    public boolean runSecondary() {
        return true;
    }

	public void update() {
		mapPlayerTimeStamps.clear();
	}
}
