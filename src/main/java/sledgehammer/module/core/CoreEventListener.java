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

package sledgehammer.module.core;

import java.util.HashMap;
import java.util.Map;

import sledgehammer.SledgeHammer;
import sledgehammer.event.core.player.DeathEvent;
import sledgehammer.event.Event;
import sledgehammer.event.core.player.DisconnectEvent;
import sledgehammer.event.core.player.PlayerJoinEvent;
import sledgehammer.event.core.player.PlayerQuitEvent;
import sledgehammer.event.core.player.pvp.PVPKillEvent;
import sledgehammer.interfaces.EventListener;
import sledgehammer.lua.core.Player;
import sledgehammer.util.ChatTags;
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
        return new String[]{
                PlayerJoinEvent.ID,
                PlayerQuitEvent.ID,
                DisconnectEvent.ID,
                DeathEvent.ID,
                PVPKillEvent.ID
        };
    }

    @Override
    public void onEvent(Event event) {
        event.setIgnoreCore(true);
        String id = event.getID();
        if (id.equals(PlayerJoinEvent.ID)) {
            onPlayerJoinEvent((PlayerJoinEvent) event);
        } else if (id.equals(PlayerQuitEvent.ID)) {
            onPlayerQuitEvent((PlayerQuitEvent) event);
        } else if (event.getID().equals(DisconnectEvent.ID)) {
            onDisconnectEvent((DisconnectEvent) event);
        } else if (id.equals(DeathEvent.ID)) {
            onPlayerDeathEvent((DeathEvent) event);
        } else if (event.getID().equals(PVPKillEvent.ID)) {
            onPVPKillEvent((PVPKillEvent) event);
        }
    }

    @Override
    public boolean runSecondary() {
        return true;
    }

    private void onPlayerJoinEvent(PlayerJoinEvent event) {
    }

    private void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        SledgeHammer.instance.getPlayerManager().removePlayer(player);
    }

    private void onDisconnectEvent(DisconnectEvent event) {
        Player player = event.getPlayer();
        if (player != null) {
            SledgeHammer.instance.getPlayerManager().removePlayer(player);
        }
    }

    private void onPlayerDeathEvent(DeathEvent event) {
        if (!event.shouldAnnounce() || ((DeathEvent) event).getPlayer().getIso() instanceof NPC) {
            return;
        }
        String username = ((DeathEvent) event).getPlayer().getUsername();
        if (username != null) {
            Long timeStamp = mapPlayerTimeStamps.get(username.toLowerCase());
            if (timeStamp != null) {
                event.setHandled(true);
                event.setCanceled(true);
                return;
            }
            mapPlayerTimeStamps.put(username.toLowerCase(), System.currentTimeMillis());
            String text = event.getLogMessage();
            module.sendGlobalMessage(ChatTags.COLOR_RED + " " + text);
            SledgeHammer.instance.handleCommand("/thunder start", false);
        }
    }

    private void onPVPKillEvent(PVPKillEvent event) {
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
        String text = event.getLogMessage();
        module.sendGlobalMessage(COLOR_RED + " " + text);
        SledgeHammer.instance.handleCommand(null, "/thunder start", false);
    }

    public void update() {
        mapPlayerTimeStamps.clear();
    }
}