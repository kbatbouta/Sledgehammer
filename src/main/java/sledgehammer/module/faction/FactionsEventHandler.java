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
import sledgehammer.annotations.EventHandler;
import sledgehammer.event.chat.RequestChannelsEvent;
import sledgehammer.event.core.player.*;
import sledgehammer.interfaces.Listener;
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
public class FactionsEventHandler implements Listener {

    private ModuleFactions module;

    /**
     * Main constructor.
     *
     * @param module The faction module instance using the listener.
     */
    FactionsEventHandler(ModuleFactions module) {
        setModule(module);
    }


    @EventHandler(id = "core.faction.event.playercreated")
    private void on(PlayerCreatedEvent event) {
        Player player = event.getPlayer();
        FactionMember factionMember = module.getFactionMember(player);
        if (factionMember != null) {
            Faction faction = factionMember.getFaction();
            player.setColor(Color.getColor(faction.getFactionRawColor()));
            player.setNickname("[" + faction.getFactionTag() + "] " + player.getUsername());
        }
    }

    @EventHandler(id = "core.faction.event.connect")
    private void on(ConnectEvent event) {
        Player player = event.getPlayer();
        FactionMember factionMember = module.getFactionMember(player);
        if (factionMember != null) {
            factionMember.setTag(player);
        }
    }

    @EventHandler(id = "core.faction.event.requestchannels")
    private void on(RequestChannelsEvent event) {
        Player player = event.getPlayer();
        FactionMember factionMember = module.getFactionMember(player);
        if (factionMember != null) {
            Faction faction = factionMember.getFaction();
            ChatChannel chatChannel = faction.getChatChannel();
            chatChannel.addPlayer(player, false);
            event.addChatChannel(chatChannel);
        }
    }

    @EventHandler(id = "core.faction.event.playerchatready")
    private void on(PlayerChatReadyEvent event) {
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
                    player.sendChatMessage(playerInvitee.getUsername() + " has invited you to " +
                            "join the faction "
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

    @EventHandler(id = "core.faction.event.disconnect")
    private void on(DisconnectEvent event) {
        Player player = event.getPlayer();
        FactionMember factionMember = module.getFactionMember(player);
        if (factionMember != null) {
            Faction faction = factionMember.getFaction();
            faction.getChatChannel().removePlayer(player, false);
        }
    }

    /**
     * @return Returns the faction module instance using the listener.
     */
    public ModuleFactions getModule() {
        return this.module;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the faction module instance using the listener.
     *
     * @param module The faction module instance.
     */
    private void setModule(ModuleFactions module) {
        this.module = module;
    }
}
