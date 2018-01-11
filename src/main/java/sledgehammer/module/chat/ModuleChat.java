package sledgehammer.module.chat;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.database.MongoCollection;
import sledgehammer.database.module.chat.MongoChatChannel;
import sledgehammer.database.module.chat.MongoChatMessage;
import sledgehammer.database.module.core.SledgehammerDatabase;
import sledgehammer.enums.Result;
import sledgehammer.event.*;
import sledgehammer.event.chat.RequestChannelsEvent;
import sledgehammer.interfaces.CommandListener;
import sledgehammer.interfaces.EventListener;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.chat.ChatHistory;
import sledgehammer.lua.chat.ChatMessage;
import sledgehammer.lua.chat.request.RequestChatChannels;
import sledgehammer.lua.chat.request.RequestChatHistory;
import sledgehammer.lua.core.Player;
import sledgehammer.lua.core.send.SendLua;
import sledgehammer.plugin.MongoModule;
import sledgehammer.util.Command;
import sledgehammer.util.Response;

/**
 * TODO: Document.
 *
 * @author Jab
 */
public class ModuleChat extends MongoModule implements EventListener, CommandListener {

    private Map<UUID, ChatChannel> mapChatChannels;
    private LinkedList<ChatChannel> listOrderedChatChannels;
    private MongoCollection collectionChannels;
    private MongoCollection collectionMessages;
    private ChatChannel all;
    private ChatChannel global;
    private ChatChannel local;
    private ChatChannel pms;
    private ChatChannel espanol;

    private SendLua sendLua;

    /**
     * Main constructor.
     */
    public ModuleChat() {
        super(getDefaultDatabase());
    }

    @Override
    public void onLoad() {
        loadLua();
        mapChatChannels = new LinkedHashMap<>();
        listOrderedChatChannels = new LinkedList<>();
        // Grab the MongoCollections storing the data for this Module.
        SledgehammerDatabase database = getSledgehammerDatabase();
        collectionChannels = database.createMongoCollection("sledgehammer_chat_channels");
        collectionMessages = database.createMongoCollection("sledgehammer_chat_messages");
        // Handle chat command initializations.
        addDefaultPermission("sledgehammer.chat.global");
        addDefaultPermission("sledgehammer.chat.local");
        addDefaultPermission("sledgehammer.chat.pm");
        addDefaultPermission(getPermissionNode("espanol"));
        // @formatter:on
    }

    @Override
    public void onStart() {
        loadMongoDocuments();
        verifyCoreChannels();
        register((EventListener) this);
        register((CommandListener) this);
    }

    @Override
    public void onUpdate(long delta) {

    }

    @Override
    public void onStop() {
        unregister((EventListener) this);
        unregister((CommandListener) this);
    }

    @Override
    public void onUnload() {
        for (ChatChannel chatChannel : getChatChannels()) {
            chatChannel.removePlayers();
        }
        // Nullify all fields. @formatter:off
		this.collectionChannels      = null;
		this.collectionMessages      = null;
		this.mapChatChannels         = null;
		this.listOrderedChatChannels = null;
		this.all                     = null;
		this.global                  = null;
		this.local                   = null;
		this.pms                     = null;
		// @formatter:on
    }

    @Override
    public void onBuildLua(SendLua send) {
        send.append(sendLua);
    }

    @Override
    public void onClientCommand(ClientEvent event) {
        String command = event.getCommand();
        Player player = event.getPlayer();
        if (command.equalsIgnoreCase("requestChatChannels")) {
            RequestChannelsEvent requestEvent = new RequestChannelsEvent(player);
            onEvent(requestEvent);
            RequestChatChannels request = new RequestChatChannels();
            // Add the main ChatChannels first in order.
            if (global.hasAccess(player)) {
                global.addPlayer(player, false);
                request.addChannel(global);
            }
            if (local.hasAccess(player)) {
                local.addPlayer(player, false);
                request.addChannel(local);
            }
            if (pms.hasAccess(player)) {
                pms.addPlayer(player, false);
                request.addChannel(pms);
            }
            if (espanol.hasAccess(player)) {
                espanol.addPlayer(player, false);
                request.addChannel(espanol);
            }
            for (ChatChannel channel : getChatChannels()) {
                if (channel.equals(global) || channel.equals(local) || channel.equals(pms) || channel.equals(espanol)) {
                    continue;
                }
                if (channel.hasAccess(player)) {
                    channel.addPlayer(player, false);
                    request.addChannel(channel);
                }
            }
            event.respond(request);
        } else if (command.equalsIgnoreCase("requestChatHistories")) {
            RequestChatHistory request = new RequestChatHistory();
            for (ChatChannel chatChannel : getChatChannels()) {
                if (chatChannel.hasAccess(player)) {
                    request.addChatHistory(chatChannel.getHistory());
                }
            }
            event.respond(request);
        } else if (command.equalsIgnoreCase("sendChatMessage")) {
            MongoChatMessage mongoChatMessage = new MongoChatMessage(collectionMessages);
            KahluaTable table = (KahluaTable) event.getTable().rawget("message");
            ChatMessage chatMessage = new ChatMessage(mongoChatMessage, table);
            UUID channelId = chatMessage.getChannelId();
            ChatChannel chatChannel = getChatChannel(channelId);
            if (chatChannel == null) {
                errln("ChatMessage provided null Channel ID: " + channelId);
                return;
            }
            chatChannel.getHistory().addChatMessage(chatMessage, true);
            chatMessage.save();
        }
    }

    @Override
    public void onEvent(Event event) {
        String Id = event.getID();
        if (Id.equals(DisconnectEvent.ID)) {
            handleDisconnectEvent((DisconnectEvent) event);
        }
    }

    @Override
    public String[] getTypes() {
        // @formatter:off
		return new String[] {
		        DisconnectEvent.ID
		};
		// @formatter:on
    }

    @Override
    public boolean runSecondary() {
        return false;
    }

    @Override
    public String getName() {
        return "ModuleChat";
    }

    @Override
    public ChatChannel getChatChannel(String name) {
        name = name.toLowerCase();
        println("name: " + name);
        ChatChannel returned = null;
        for (ChatChannel chatChannel : getChatChannels()) {
            String chatChannelName = chatChannel.getChannelName().toLowerCase();
            println("\tNext chatChannel: " + chatChannelName);
            if (chatChannelName.equalsIgnoreCase(name)) {
                returned = chatChannel;
                break;
            }
        }
        return returned;
    }

    @Override
    public ChatChannel createChatChannel(String channelName, String channelDescription, String permissionNode,
                                         boolean isGlobalChannel, boolean isPublicChannel, boolean isCustomChannel, boolean saveHistory,
                                         boolean canSpeak) {
        MongoChatChannel mongoChatChannel = new MongoChatChannel(collectionChannels, channelName, channelDescription,
                permissionNode, isGlobalChannel, isPublicChannel, isCustomChannel, saveHistory, canSpeak);
        ChatChannel chatChannel = new ChatChannel(mongoChatChannel);
        mapChatChannels.put(chatChannel.getUniqueId(), chatChannel);
        listOrderedChatChannels.add(chatChannel);
        mongoChatChannel.save();
        return chatChannel;
    }

    @Override
    public void unregisterChatChannel(ChatChannel chatChannel) {
        if (chatChannel == null) {
            throw new IllegalArgumentException("ChatChannel given is null.");
        }
        chatChannel.removePlayers();
        chatChannel.delete();
        mapChatChannels.remove(chatChannel.getUniqueId());
        listOrderedChatChannels.remove(chatChannel);
    }

    @Override
    public ChatMessage createChatMessage(String message) {
        MongoChatMessage mongoChatMessage = new MongoChatMessage(collectionMessages);
        ChatMessage chatMessage = new ChatMessage(mongoChatMessage);
        chatMessage.setMessage(message, false);
        chatMessage.setOriginalMessage(message, false);
        chatMessage.setPrintedTimestamp(false);
        chatMessage.setOrigin(ChatMessage.ORIGIN_SERVER, false);
        chatMessage.setTimestamp(System.currentTimeMillis(), false);
        chatMessage.setModifiedTimestamp(-1L, false);
        return chatMessage;
    }

    @Override
    public void onCommand(Command com, Response r) {
        Player commander = com.getPlayer();
        String command = com.getCommand().toLowerCase();
        if (command.equals("espanol")) {
            String permissionNode = "sledgehammer.chat.espanol";
            ChatChannel channel = getChatChannel("Espanol");
            if (commander.hasPermission(permissionNode, true)) {
                commander.setPermission(permissionNode, false);
                channel.removePlayer(commander);
                r.set(Result.SUCCESS, "You have been removed from the Espanol channel.");
            } else {
                commander.setPermission(permissionNode, true);
                channel.addPlayer(commander, true);
                r.set(Result.SUCCESS, "You are now added to the Espanol channel.");
            }
        }
    }

    @Override
    public String[] getCommands() {
        // @formatter:off
		return new String[] {
				"espanol"
		};
		// @formatter:on
    }

    @Override
    public String onTooltip(Player player, Command com) {
        String command = com.getCommand().toLowerCase();
        if (command.equals("espanol")) {
            return "Adds you to the Spanish chat channel.";
        }
        return null;
    }

    @Override
    public String getPermissionNode(String command) {
        if (command.equalsIgnoreCase("espanol")) {
            return "sledgehammer.chat.command.espanol";
        }
        return null;
    }

    private void loadLua() {
        File lua = getLuaDirectory();
        boolean overwrite = !isLuaOverriden();
        // @formatter:off
        File fileChatChannel = new File(lua, "ChatChannel.lua");
        File fileChatHistory = new File(lua, "ChatHistory.lua");
        File fileChatMessage = new File(lua, "ChatMessage.lua");
        File fileChatWindow  = new File(lua, "ChatWindow.lua" );
        File fileChatModule  = new File(lua, "ModuleChat.lua" );
        saveResourceAs("lua/module/core.chat/ChatChannel.lua", fileChatChannel, overwrite);
        saveResourceAs("lua/module/core.chat/ChatHistory.lua", fileChatHistory, overwrite);
        saveResourceAs("lua/module/core.chat/ChatMessage.lua", fileChatMessage, overwrite);
        saveResourceAs("lua/module/core.chat/ChatWindow.lua" , fileChatWindow , overwrite);
        saveResourceAs("lua/module/core.chat/ModuleChat.lua" , fileChatModule , overwrite);
        // @formatter:on
        sendLua = new SendLua(fileChatChannel, fileChatHistory, fileChatMessage, fileChatWindow, fileChatModule);
    }

    private void handleDisconnectEvent(DisconnectEvent event) {
        Player player = event.getPlayer();
        for (ChatChannel channel : mapChatChannels.values()) {
            channel.removePlayer(player);
        }
    }

    private void loadMongoDocuments() {
        loadMongoChatChannels();
        loadMongoChatHistories();
        loadMongoChatBroadcasts();
    }

    private void loadMongoChatChannels() {
        DBCursor cursor = collectionChannels.find();
        while (cursor.hasNext()) {
            MongoChatChannel mongoChatChannel = new MongoChatChannel(collectionChannels, cursor.next());
            ChatChannel chatChannel = new ChatChannel(mongoChatChannel);
            mapChatChannels.put(chatChannel.getUniqueId(), chatChannel);
            listOrderedChatChannels.add(chatChannel);
        }
        cursor.close();
    }

    private void loadMongoChatHistories() {
        for (ChatChannel chatChannel : mapChatChannels.values()) {
            // Create the History container.
            ChatHistory chatHistory = new ChatHistory(chatChannel);
            chatChannel.setHistory(chatHistory);
            // Grab the chat messages from the message collection for this history.
            List<ChatMessage> listChatMessages = getChatMessages(chatChannel.getUniqueId(), ChatHistory.MAX_SIZE);
            chatHistory.addChatMessages(listChatMessages, false);
        }
    }

    /**
     * Returns the ChatChannels that a Player can see.
     *
     * @param player The Player being checked.
     * @return Returns a List of ChatChannels.
     */
    public List<ChatChannel> getChatChannels(Player player) {
        List<ChatChannel> listChatChannels = new LinkedList<>();
        for (ChatChannel chatChannel : getChatChannels()) {
            if (chatChannel.hasAccess(player)) {
                listChatChannels.add(chatChannel);
            }
        }
        return listChatChannels;
    }

    /**
     * @param channelId The Unique ID of the channel.
     * @param limit     The Integer limit of ChatMessages to load.
     * @return Returns a List of ChatMessages for the ChatChannel.
     */
    private List<ChatMessage> getChatMessages(UUID channelId, int limit) {
        List<ChatMessage> listChatMessages = new LinkedList<>();
        // Grab all the messages with the channel_id set to the one provided.
        DBObject query = new BasicDBObject("channel_id", channelId);
        DBCursor cursor = collectionMessages.find(query);
        // Sort the list by timestamp so that the last messages appear first.
        cursor.sort(new BasicDBObject("timestamp", 1));
        cursor.limit(limit);
        // Go through all entries.
        while (cursor.hasNext()) {
            // Create the MongoDocument.
            MongoChatMessage mongoChatMessage = new MongoChatMessage(collectionMessages, cursor.next());
            // Create the container for the document.
            ChatMessage chatMessage = new ChatMessage(mongoChatMessage);
            // Add this to the list to return.
            listChatMessages.add(chatMessage);
        }
        // Close the cursor to release resources.
        cursor.close();
        // Return the result list of messages for the channel.
        return listChatMessages;
    }

    /**
     * (Private Method)
     * <p>
     * Verifies that the Core ChatChannels are defined in-case of a new server.
     */
    private void verifyCoreChannels() {
        // Create the wild-card ChatChannel.
        String channelName = "*";
        String channelDescription = "Wildcard channel for the server. Sends messages to all spoken channels.";
        String channelPermissionNode = "sledgehammer.chat";
        // Create the MongoDocument.
        MongoChatChannel mongoChatChannel = new MongoChatChannel(collectionChannels, channelName, channelDescription,
                channelPermissionNode, false, false, false, false, false);
        setAllChatChannel(new ChatChannel(mongoChatChannel));
        // Create the global ChatChannel.
        ChatChannel global = getChatChannel("Global");
        if (global == null) {
            channelName = "Global";
            channelDescription = "Global channel for the server.";
            channelPermissionNode = "sledgehammer.chat.global";
            global = createChatChannel(channelName, channelDescription, channelPermissionNode, true,
                    true, false, true, true);
        }
        // Check to make sure that Global is forced explicit.
        if (!global.isExplicit()) {
            global.setExplicit(true, true);
        }
        // Create the local ChatChannel.
        ChatChannel local = getChatChannel("Local");
        if (local == null) {
            channelName = "Local";
            channelDescription = "Local channel for the server.";
            channelPermissionNode = "sledgehammer.chat.local";
            local = createChatChannel(channelName, channelDescription, channelPermissionNode, false,
                    true, false, false, true);
        }
        // Create the PM's ChatChannel.
        ChatChannel pms = getChatChannel("PM's");
        if (pms == null) {
            channelName = "PM's";
            channelDescription = "PM channel for the server.";
            channelPermissionNode = "sledgehammer.chat.pm";
            pms = createChatChannel(channelName, channelDescription, channelPermissionNode, false,
                    true, false, false, false);
        }
        ChatChannel espanol = getChatChannel("Espanol");
        if (espanol == null) {
            channelName = "Espanol";
            channelDescription = "Spanish channel for the server.";
            channelPermissionNode = "sledgehammer.chat.espanol";
            espanol = createChatChannel(channelName, channelDescription, channelPermissionNode, true,
                    false, false, true, true);
        }
        setGlobalChatChannel(global);
        setLocalChatChannel(local);
        setPMsChatChannel(pms);
        setEspanolChannel(espanol);
    }

    public ChatChannel getEspanolChannel() {
        return this.espanol;
    }

    private void setEspanolChannel(ChatChannel espanol) {
        this.espanol = espanol;
    }

    /**
     * @return Returns a ChatChannel that is used to send ChatMessages to all
     * speakable ChatChannels.
     */
    public ChatChannel getAllChatChannel() {
        return this.all;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the ChatChannel that is used to send ChatMessages to all speakable
     * ChatChannels.
     *
     * @param all The ChatChannel to set.
     */
    private void setAllChatChannel(ChatChannel all) {
        this.all = all;
    }

    /**
     * @return Returns the global ChatChannel for general ChatMessages.
     */
    public ChatChannel getGlobalChatChannel() {
        return this.global;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the global ChatChannel for general ChatMessages.
     *
     * @param global The ChatChannel to set.
     */
    private void setGlobalChatChannel(ChatChannel global) {
        this.global = global;
    }

    /**
     * @return Returns the local ChatChannel for ChatMessages to be sent in
     * proximity to the Player sending the ChatMessage.
     */
    public ChatChannel getLocalChatChannel() {
        return this.local;
    }

    /**
     * (Private Method)
     * <p>
     * Sets the local ChatChannel for ChatMessages to be sent in proximity to
     * the Player sending the ChatMessage.
     *
     * @param local The ChatChannel to set.
     */
    private void setLocalChatChannel(ChatChannel local) {
        this.local = local;
    }

    public ChatChannel getPMsChatChannel() {
        return this.pms;
    }

    private void setPMsChatChannel(ChatChannel pms) {
        this.pms = pms;
    }

    private void loadMongoChatBroadcasts() {
        // TODO: Implement.
    }

    public ChatChannel getChatChannel(UUID channelId) {
        return mapChatChannels.get(channelId);
    }

    public Collection<ChatChannel> getChatChannels() {
        return listOrderedChatChannels;
    }
}