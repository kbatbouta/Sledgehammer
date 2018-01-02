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
package sledgehammer.module.faction;

import java.util.ArrayList;
import java.util.List;

import sledgehammer.event.ConnectEvent;
import sledgehammer.event.DisconnectEvent;
import sledgehammer.event.Event;
import sledgehammer.event.PlayerCreatedEvent;
import sledgehammer.event.chat.RequestChannelsEvent;
import sledgehammer.interfaces.EventListener;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.core.Color;
import sledgehammer.lua.core.Player;
import sledgehammer.lua.faction.Faction;
import sledgehammer.lua.faction.FactionInvite;
import sledgehammer.lua.faction.FactionMember;

/**
 * TODO: Document.
 * 
 * @author Jab
 */
public class FactionsEventHandler implements EventListener {

	/** The Module using the listener. */
	private ModuleFactions module;

	/**
	 * Main constructor.
	 * 
	 * @param module
	 *            The <ModuleFactions> instance using the listener.
	 */
	public FactionsEventHandler(ModuleFactions module) {
		setModule(module);
	}

	@Override
	public String[] getTypes() {
		return new String[] { ConnectEvent.ID, DisconnectEvent.ID, PlayerCreatedEvent.ID, RequestChannelsEvent.ID };
	}

	@Override
	public void onEvent(Event event) {
		String ID = event.getID();
		if (ID == ConnectEvent.ID) {
			handleConnectEvent((ConnectEvent) event);
		} else if (ID == DisconnectEvent.ID) {
			handleDisconnectEvent((DisconnectEvent) event);
		} else if (ID == PlayerCreatedEvent.ID) {
			handlePlayerCreatedEvent((PlayerCreatedEvent) event);
		} else if (ID == RequestChannelsEvent.ID) {
			handleRequestChannelsEvent((RequestChannelsEvent) event);
		}
	}

	private void handleConnectEvent(ConnectEvent event) {
		Player player = event.getPlayer();
		List<FactionInvite> invitesToDelete = new ArrayList<>();
		List<FactionInvite> invites = module.getInvitesForPlayer(player);
		if (invites != null && invites.size() > 0) {
			for (FactionInvite factionInvite : invites) {
				Faction faction = module.getFaction(factionInvite.getFactionId());
				if (faction == null) {
					invitesToDelete.add(factionInvite);
					continue;
				}
				FactionMember member = module.getFactionMember(factionInvite.getInviteeId());
				if (member == null) {
					invitesToDelete.add(factionInvite);
					continue;
				}
				player.sendChatMessage(factionInvite.getUniqueId() + " has invited you to join the faction "
						+ faction.getFactionName() + ".");
			}
			player.sendChatMessage("Type \'/faction accept <FACTION>\' to accept.");
			player.sendChatMessage("To reject an invitation, type \'/faction reject \"faction\'.");
			player.sendChatMessage("To reject all invitations, type \'/faction reject all\'.");
		}
	}

	private void handleDisconnectEvent(DisconnectEvent event) {
		Player player = event.getPlayer();
		FactionMember factionMember = module.getFactionMember(player);
		if (factionMember != null) {
			Faction faction = factionMember.getFaction();
			faction.getChatChannel().removePlayer(player);
		}
	}

	public void handlePlayerCreatedEvent(PlayerCreatedEvent event) {
		Player player = event.getPlayer();
		FactionMember factionMember = module.getFactionMember(player);
		if (factionMember != null) {
			Faction faction = factionMember.getFaction();
			player.setColor(Color.getColor(faction.getFactionRawColor()));
			player.setNickname("[" + faction.getFactionTag() + "] " + player.getUsername());
		}
	}

	private void handleRequestChannelsEvent(RequestChannelsEvent event) {
		Player player = event.getPlayer();
		FactionMember factionMember = module.getFactionMember(player);
		if (factionMember != null) {
			Faction faction = factionMember.getFaction();
			ChatChannel chatChannel = faction.getChatChannel();
			event.addChatChannel(chatChannel);
		}
	}

	@Override
	public boolean runSecondary() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return Returns the <ModuleFactions> instance using the listener.
	 */
	public ModuleFactions getModule() {
		return this.module;
	}

	/**
	 * (Internal Method)
	 * 
	 * Sets the <ModuleFactions> instance using the listener.
	 * 
	 * @param module
	 *            The <ModuleFactions> instance.
	 */
	private void setModule(ModuleFactions module) {
		this.module = module;
	}

}
