/*
 * This file is part of Sledgehammer.
 *
 *    Sledgehammer is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Sledgehammer is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 *
 *    Sledgehammer is free to use and modify, ONLY for non-official third-party servers
 *    not affiliated with TheIndieStone, or it's immediate affiliates, or contractors.
 */

package sledgehammer.module.faction;

import java.util.ArrayList;
import java.util.List;

import sledgehammer.SledgeHammer;
import sledgehammer.event.*;
import sledgehammer.event.chat.RequestChannelsEvent;
import sledgehammer.event.core.player.*;
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

    /**
     * The Module using the listener.
     */
    private ModuleFactions module;

    /**
     * Main constructor.
     *
     * @param module The ModuleFactions instance using the listener.
     */
    public FactionsEventHandler(ModuleFactions module) {
        setModule(module);
    }

    @Override
    public void onEvent(Event event) {
        String ID = event.getID();
        switch (ID) {
            case ConnectEvent.ID:
                handleConnectEvent((ConnectEvent) event);
                break;
            case PlayerCreatedEvent.ID:
                handlePlayerCreatedEvent((PlayerCreatedEvent) event);
                break;
            case RequestChannelsEvent.ID:
                handleRequestChannelsEvent((RequestChannelsEvent) event);
                break;
            case PlayerChatReadyEvent.ID:
                handlePlayerChatReadyEvent((PlayerChatReadyEvent) event);
                break;
            case DisconnectEvent.ID:
                handleDisconnectEvent((DisconnectEvent) event);
                break;
        }
    }

    @Override
    public String[] getTypes() {
        // @formatter:off
        return new String[]{
                PlayerCreatedEvent.ID  ,
                ConnectEvent.ID        ,
                RequestChannelsEvent.ID,
                PlayerChatReadyEvent.ID,
                DisconnectEvent.ID
        };
        // @formatter:on
    }

    @Override
    public boolean runSecondary() {
        return false;
    }

    private void handlePlayerCreatedEvent(PlayerCreatedEvent event) {
        Player player = event.getPlayer();
        FactionMember factionMember = module.getFactionMember(player);
        if (factionMember != null) {
            Faction faction = factionMember.getFaction();
            player.setColor(Color.getColor(faction.getFactionRawColor()));
            player.setNickname("[" + faction.getFactionTag() + "] " + player.getUsername());
        }
    }

    private void handleConnectEvent(ConnectEvent event) {
        Player player = event.getPlayer();
        FactionMember factionMember = module.getFactionMember(player);
        if (factionMember != null) {
            factionMember.setTag(player);
        }
    }

    private void handleRequestChannelsEvent(RequestChannelsEvent event) {
        Player player = event.getPlayer();
        FactionMember factionMember = module.getFactionMember(player);
        if (factionMember != null) {
            Faction faction = factionMember.getFaction();
            ChatChannel chatChannel = faction.getChatChannel();
            chatChannel.addPlayer(player, false);
            event.addChatChannel(chatChannel);
        }
    }

    private void handlePlayerChatReadyEvent(PlayerChatReadyEvent event) {
        Player player = event.getPlayer();
        List<FactionInvite> invites = module.getInvitesForPlayer(player);
        if (invites != null && invites.size() > 0) {
            List<FactionInvite> invitesToDelete = new ArrayList<>();
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
                Player playerInvitee = SledgeHammer.instance.getPlayer(factionInvite.getUniqueId());
                if (playerInvitee == null) {
                    SledgeHammer.instance.getOfflinePlayer(factionInvite.getUniqueId());
                }
                if (playerInvitee != null) {
                    player.sendChatMessage(playerInvitee.getUsername() + " has invited you to join the faction "
                            + faction.getFactionName() + ".");
                } else {
                    player.sendChatMessage("You have been invited to join the faction "
                            + faction.getFactionName() + ".");
                }
            }
            for (FactionInvite invite : invitesToDelete) {
                module.deleteInvite(invite);
            }
            player.sendChatMessage("Type \'/faction accept \"faction\"\' to accept.");
            player.sendChatMessage("To reject an invitation, type \'/faction reject \"faction\'.");
            player.sendChatMessage("To reject all invitations, type \'/faction reject all\'.");
        }
    }

    private void handleDisconnectEvent(DisconnectEvent event) {
        Player player = event.getPlayer();
        FactionMember factionMember = module.getFactionMember(player);
        if (factionMember != null) {
            Faction faction = factionMember.getFaction();
            faction.getChatChannel().removePlayer(player, false);
        }
    }

    /**
     * @return Returns the ModuleFactions instance using the listener.
     */
    public ModuleFactions getModule() {
        return this.module;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the ModuleFactions instance using the listener.
     *
     * @param module The ModuleFactions instance.
     */
    private void setModule(ModuleFactions module) {
        this.module = module;
    }
}
