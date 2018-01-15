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

package sledgehammer.lua.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.database.module.chat.MongoChatChannel;
import sledgehammer.lua.MongoLuaObject;
import sledgehammer.lua.chat.send.SendChatChannel;
import sledgehammer.lua.chat.send.SendChatChannelRemove;
import sledgehammer.lua.chat.send.SendChatChannelRename;
import sledgehammer.lua.chat.send.SendChatMessages;
import sledgehammer.lua.core.Player;

/**
 * TODO: Document.
 *
 * @author Jab
 */
public class ChatChannel extends MongoLuaObject<MongoChatChannel> {

    private ChatHistory chatHistory;

    private List<Player> listPlayersSent;

    private SendChatMessages sendChatMessages;
    private SendChatChannel sendChatChannel;
    private SendChatChannelRemove sendChatChannelRemove;
    private SendChatChannelRename sendChatChannelRename;

    /**
     * MongoDB load constructor.
     *
     * @param mongoDocument The MongoDocument storing the data for the ChatChannel.
     */
    public ChatChannel(MongoChatChannel mongoDocument) {
        super(mongoDocument, "ChatChannel");
        initialize();
    }

    /**
     * Lua load constructor.
     *
     * @param mongoDocument The MongoDocument storing the data for the ChatChannel.
     * @param table         The KahluaTable to import from the client.
     */
    public ChatChannel(MongoChatChannel mongoDocument, KahluaTable table) {
        super(mongoDocument, "ChatChannel");
        initialize();
        onLoad(table);
    }

    @Override
    public void onLoad(KahluaTable table) {
        setChannelName(table.rawget("name").toString(), false);
        setPermissionNode(table.rawget("permission_node").toString(), false);
        importFlags((KahluaTable) table.rawget("flags"));
    }

    @Override
    public void onExport() {
        // @formatter:off
		set("id"             , getUniqueId().toString());
		set("name"           , getChannelName()        );
		set("permission_node", getPermissionNode()     );
		set("flags"          , exportFlags()           );
		// @formatter:on
    }

    @Override
    public boolean equals(Object other) {
        boolean returned = false;
        if (other instanceof ChatChannel) {
            returned = ((ChatChannel) other).getUniqueId().equals(getUniqueId());
        }
        return returned;
    }

    /**
     * (Private Method)
     * <p>
     * Initializes the ChatChannel instance fields.
     */
    private void initialize() {
        chatHistory = new ChatHistory(this);
        listPlayersSent = new ArrayList<>();
        sendChatChannel = new SendChatChannel(this);
        sendChatChannelRename = new SendChatChannelRename();
        sendChatChannelRename.setChannelId(getUniqueId());
        sendChatChannelRemove = new SendChatChannelRemove();
        sendChatChannelRemove.setChannelId(getUniqueId());
        sendChatMessages = new SendChatMessages(getUniqueId());
    }

    private void importFlags(KahluaTable table) {
        setGlobalChannel((boolean) table.rawget("global"), false);
        setPublicChannel((boolean) table.rawget("public"), false);
        setCustomChannel((boolean) table.rawget("custom"), false);
        setCanSpeak((boolean) table.rawget("speak"), false);
        setSaveHistory((boolean) table.rawget("history"), false);
        setExplicit((boolean) table.rawget("explicit"), false);
    }

    private KahluaTable exportFlags() {
        KahluaTable table = newTable();
        // @formatter:off
		table.rawset("global"   , isGlobalChannel());
		table.rawset("public"   , isPublicChannel());
		table.rawset("custom"   , isCustomChannel());
		table.rawset("can_speak", canSpeak()       );
		table.rawset("history"  , saveHistory()    );
		table.rawset("explicit" , isExplicit()     );
		// @formatter:on
        return table;
    }

    /**
     * Adds a Player to the ChatChannel.
     *
     * @param player The Player to add.
     * @param send   Set to true to send the addition to the Player.
     */
    public void addPlayer(Player player, boolean send) {
        if (!listPlayersSent.contains(player)) {
            if (send) {
                SledgeHammer.instance.send(sendChatChannel, player);
            }
            listPlayersSent.add(player);
        }
    }

    /**
     * Removes a Player from the ChatChannel.
     *
     * @param player The Player to remove.
     * @param send   Set to true to send the removal to the Player. (False if disconnecting)
     */
    public void removePlayer(Player player, boolean send) {
        if (listPlayersSent.contains(player)) {
            listPlayersSent.remove(player);
            if (send) {
                SledgeHammer.instance.send(sendChatChannelRemove, player);
            }
        }
    }

    public void removePlayers(boolean send) {
        if(send) {
            SledgeHammer.instance.send(sendChatChannelRemove, getPlayers());
        }
        listPlayersSent.clear();
    }

    public boolean hasAccess(Player player) {
        boolean returned;
        String permissionNode = getPermissionNode();
        returned = listPlayersSent.contains(player) || permissionNode == null || player.hasPermission(permissionNode, true);
        return returned;
    }

    public void rename(String nameNew, boolean save) {
        sendChatChannelRename.setOldName(getChannelName());
        setChannelName(nameNew, save);
        sendChatChannelRename.setNewName(nameNew);
        SledgeHammer.instance.send(sendChatChannelRename, getPlayers());
    }

    /**
     * Sends a ChatMessage directly to a Player in the ChatChannel.
     * <p>
     * (Note: This ChatChannel does not save the ChatMessage passed in this method)
     *
     * @param message The ChatMessage being sent to the Player.
     * @param player  The Player being sent the ChatMessage.
     */
    public void sendChatMessageDirect(ChatMessage message, Player player) {
        sendChatMessages.clearChatMessages();
        sendChatMessages.addChatMessage(message);
        SledgeHammer.instance.send(sendChatMessages, player);
    }

    public void addChatMessage(ChatMessage chatMessage) {
        getHistory().addChatMessage(chatMessage, true);
    }

    public boolean canSpeak() {
        return getMongoDocument().canSpeak();
    }

    public UUID getUniqueId() {
        return getMongoDocument().getUniqueId();
    }

    public String getChannelName() {
        return getMongoDocument().getChannelName();
    }

    public void setChannelName(String channelName, boolean save) {
        getMongoDocument().setChannelName(channelName, save);
    }

    public String getChannelDescription() {
        return getMongoDocument().getChannelDescription();
    }

    public void setChannelDescription(String channelDescription, boolean save) {
        getMongoDocument().setChannelDescription(channelDescription, save);
    }

    public String getPermissionNode() {
        return getMongoDocument().getPermissionNode();
    }

    public void setPermissionNode(String permissionNode, boolean save) {
        getMongoDocument().setPermissionNode(permissionNode, save);
    }

    public boolean isGlobalChannel() {
        return getMongoDocument().isGlobalChannel();
    }

    public void setGlobalChannel(boolean isGlobalChannel, boolean save) {
        getMongoDocument().setGlobalChannel(isGlobalChannel, save);
    }

    public boolean isPublicChannel() {
        return getMongoDocument().isPublicChannel();
    }

    public void setPublicChannel(boolean isPublicChannel, boolean save) {
        getMongoDocument().setPublicChannel(isPublicChannel, save);
    }

    public boolean isCustomChannel() {
        return getMongoDocument().isCustomChannel();
    }

    public void setCustomChannel(boolean isCustomChannel, boolean save) {
        getMongoDocument().setCustomChannel(isCustomChannel, save);
    }

    public void setCanSpeak(boolean canSpeak, boolean save) {
        getMongoDocument().setCanSpeak(canSpeak, save);
    }

    public boolean saveHistory() {
        return getMongoDocument().saveHistory();
    }

    public void setSaveHistory(boolean saveHistory, boolean save) {
        getMongoDocument().setSaveHistory(saveHistory, save);
    }

    public ChatHistory getHistory() {
        return this.chatHistory;
    }

    public void setHistory(ChatHistory chatHistory) {
        this.chatHistory = chatHistory;
    }

    public void delete() {
        getMongoDocument().delete();
    }

    public List<Player> getPlayers() {
        return this.listPlayersSent;
    }

    public boolean isExplicit() {
        return getMongoDocument().isExplicit();
    }

    public void setExplicit(boolean explicit, boolean save) {
        getMongoDocument().setExplicit(explicit, save);
    }
}