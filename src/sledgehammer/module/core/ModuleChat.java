package sledgehammer.module.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import sledgehammer.database.MongoCollection;
import sledgehammer.database.module.chat.MongoChatChannel;
import sledgehammer.database.module.chat.MongoChatMessage;
import sledgehammer.database.module.core.SledgehammerDatabase;
import sledgehammer.event.ClientEvent;
import sledgehammer.event.ConnectEvent;
import sledgehammer.event.DisconnectEvent;
import sledgehammer.event.Event;
import sledgehammer.event.RequestChannelsEvent;
import sledgehammer.interfaces.EventListener;
import sledgehammer.lua.chat.ChatChannel;
import sledgehammer.lua.chat.ChatHistory;
import sledgehammer.lua.chat.ChatMessage;
import sledgehammer.lua.chat.request.RequestChatChannels;
import sledgehammer.lua.chat.request.RequestChatHistory;
import sledgehammer.lua.core.Player;
import sledgehammer.module.MongoModule;

/**
 * TODO: Document.
 * 
 * @author Jab
 */
public class ModuleChat extends MongoModule implements EventListener {

	private Map<UUID, ChatChannel> mapChatChannels;
	private MongoCollection collectionChannels;
	private MongoCollection collectionMessages;
	private ChatChannel all;
	private ChatChannel global;
	private ChatChannel local;
	private ChatChannel pms;

	/**
	 * Main constructor.
	 */
	public ModuleChat() {
		super(getDefaultDatabase());
	}

	@Override
	public void onLoad() {
		mapChatChannels = new HashMap<>();
		// Grab the MongoCollections storing the data for this Module.
		SledgehammerDatabase database = getSledgehammerDatabase();
		collectionChannels = database.createMongoCollection("sledgehammer_chat_channels");
		collectionMessages = database.createMongoCollection("sledgehammer_chat_messages");
	}

	@Override
	public void onStart() {
		loadMongoDocuments();
		verifyCoreChannels();
		register(this);
	}

	@Override
	public void onUpdate(long delta) {

	}

	@Override
	public void onStop() {
		unregister(this);
	}

	@Override
	public void onUnload() {
		for (ChatChannel chatChannel : getChatChannels()) {
			chatChannel.removePlayers();
		}
		// Nullify all fields. @formatter:off
		this.collectionChannels = null;
		this.collectionMessages = null;
		this.mapChatChannels    = null;
		this.all                = null;
		this.global             = null;
		this.local              = null;
		this.pms                = null;
		// @formatter:on
	}

	@Override
	public void onClientCommand(ClientEvent event) {
		String command = event.getCommand();
		Player player = event.getPlayer();
		if (command.equalsIgnoreCase("requestChatChannels")) {
			RequestChannelsEvent requestEvent = new RequestChannelsEvent(player);
			onEvent(requestEvent);
			RequestChatChannels request = new RequestChatChannels();
			for (ChatChannel channel : getChatChannels()) {
				if (channel.hasAccess(player)) {
					request.addChannel(channel);
				}
			}
			event.respond(request);
		} 
		else if (command.equalsIgnoreCase("requestChatHistories")) {
			RequestChatHistory request = new RequestChatHistory();
			for(ChatChannel chatChannel : getChatChannels()) {
				request.addChatHistory(chatChannel.getHistory());
			}
			event.respond(request);
		}
		else if (command.equalsIgnoreCase("sendChatMessagePlayer")) {
			println("Handling ClientEvent: \"sendChatMessagePlayer\"");			
			MongoChatMessage mongoChatMessage = new MongoChatMessage(collectionMessages);
			ChatMessage chatMessage = new ChatMessage(mongoChatMessage, event.getTable());
			UUID channelId = chatMessage.getChannelId();
			ChatChannel chatChannel = getChatChannel(channelId);
			if (chatChannel == null) {
				errorln("ChatMessage provided null Channel ID: " + channelId);
				return;
			}
			chatChannel.getHistory().addMessage(chatMessage);
			chatMessage.save();
			println("Responding ClientEvent.");
		} else if (command.equalsIgnoreCase("sendChat")) {

		}
	}

	@Override
	public void onEvent(Event event) {
		String Id = event.getID();
		if (Id == ConnectEvent.ID) {
			handleConnectEvent((ConnectEvent) event);
		} else if (Id == DisconnectEvent.ID) {
			handleDisconnectEvent((DisconnectEvent) event);
		}
	}

	@Override
	public String[] getTypes() {
		// @formatter:off
		return new String[] { 
			ConnectEvent.ID, 
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

	private void handleConnectEvent(ConnectEvent event) {

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
		}
		cursor.close();
	}

	private void loadMongoChatHistories() {
		for (ChatChannel chatChannel : mapChatChannels.values()) {
			UUID channelId = chatChannel.getUniqueId();
			// Create the History container.
			ChatHistory chatHistory = new ChatHistory(channelId);
			chatChannel.setHistory(chatHistory);
			// Grab the chat messages from the message collection for this history.
			List<ChatMessage> listChatMessages = getChatMessages(channelId, ChatHistory.MAX_SIZE);
			chatHistory.addMessages(listChatMessages);
		}
	}

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
	 * @param channelId
	 *            The <UUID> ID of the channel.
	 * @return
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

	private void verifyCoreChannels() {
		String channelName;
		String channelDescription;
		String channelPermissionNode;
		boolean isGlobalChannel;
		boolean isPublicChannel;
		boolean isCustomChannel;
		boolean saveHistory;
		boolean canSpeak;
		// Create the wild-card ChatChannel.
		channelName = "*";
		channelDescription = "Wildcard channel for the server. Sends messages to all spoken channels.";
		channelPermissionNode = null;
		isGlobalChannel = true;
		isPublicChannel = true;
		isCustomChannel = false;
		saveHistory = true;
		canSpeak = true;
		MongoChatChannel mongoChatChannel = new MongoChatChannel(collectionChannels, channelName, channelDescription,
				channelPermissionNode, isGlobalChannel, isPublicChannel, isCustomChannel, saveHistory, canSpeak);
		all = new ChatChannel(mongoChatChannel);
		// Create the global ChatChannel.
		ChatChannel global = getChatChannel("Global");
		if (global == null) {
			channelName = "Global";
			channelDescription = "Global channel for the server.";
			channelPermissionNode = "sledgehammer.chat.global";
			isGlobalChannel = true;
			isPublicChannel = true;
			isCustomChannel = false;
			saveHistory = true;
			canSpeak = true;
			global = createChatChannel(channelName, channelDescription, channelPermissionNode, isGlobalChannel,
					isPublicChannel, isCustomChannel, saveHistory, canSpeak);
		}
		// Create the local ChatChannel.
		ChatChannel local = getChatChannel("Local");
		if (local == null) {
			channelName = "Local";
			channelDescription = "Local channel for the server.";
			channelPermissionNode = "sledgehammer.chat.local";
			isGlobalChannel = false;
			isPublicChannel = true;
			isCustomChannel = false;
			saveHistory = false;
			canSpeak = true;
			local = createChatChannel(channelName, channelDescription, channelPermissionNode, isGlobalChannel,
					isPublicChannel, isCustomChannel, saveHistory, canSpeak);
		}
		// Create the PM's ChatChannel.
		ChatChannel pms = getChatChannel("PM's");
		if (pms == null) {
			channelName = "PM's";
			channelDescription = "PM channel for the server.";
			channelPermissionNode = "sledgehammer.chat.pm";
			isGlobalChannel = false;
			isPublicChannel = true;
			isCustomChannel = false;
			saveHistory = false;
			canSpeak = true;
			pms = createChatChannel(channelName, channelDescription, channelPermissionNode, isGlobalChannel,
					isPublicChannel, isCustomChannel, saveHistory, canSpeak);
		}
		setGlobalChatChannel(global);
		setLocalChatChannel(local);
		setPMsChatChannel(pms);
	}

	/**
	 * @return Returns a <ChatChannel> that is used to send <ChatMessage>'s to all
	 *         speakable ChatChannels.
	 */
	public ChatChannel getAllChatChannel() {
		return this.all;
	}

	/**
	 * (Private Method)
	 * 
	 * Sets the <ChatChannel> that is used to send <ChatMessage>'s to all speakable
	 * ChatChannels.
	 * 
	 * @param all
	 *            The <ChatChannel> to set.
	 */
	private void setAllChatChannel(ChatChannel all) {
		this.all = all;
	}

	/**
	 * @return Returns the global <ChatChannel> for general <ChatMessage>'s.
	 */
	public ChatChannel getGlobalChatChannel() {
		return this.global;
	}

	/**
	 * (Private Method)
	 * 
	 * Sets the global <ChatChannel> for general <ChatMessage>'s.
	 * 
	 * @param global
	 *            The <ChatChannel> to set.
	 */
	private void setGlobalChatChannel(ChatChannel global) {
		this.global = global;
	}

	/**
	 * @return Returns the local <ChatChannel> for <ChatMessage>'s to be sent in
	 *         proximity to the <Player> sending the ChatMessage.
	 */
	public ChatChannel getLocalChatChannel() {
		return this.local;
	}

	/**
	 * (Private Method)
	 * 
	 * Sets the local <ChatChannel> for <ChatMessage>'s to be sent in proximity to
	 * the <Player> sending the ChatMessage.
	 * 
	 * @param local
	 *            The <ChatChannel> to set.
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
		return mapChatChannels.values();
	}

	public ChatChannel getChatChannel(String name) {
		name = name.toLowerCase();
		ChatChannel returned = null;
		for (UUID channelId : mapChatChannels.keySet()) {
			ChatChannel chatChannel = mapChatChannels.get(channelId);
			if (chatChannel.getChannelName().equalsIgnoreCase(name)) {
				returned = chatChannel;
				break;
			}
		}
		return returned;
	}

	public ChatChannel createChatChannel(String channelName, String channelDescription, String permissionNode,
			boolean isGlobalChannel, boolean isPublicChannel, boolean isCustomChannel, boolean saveHistory,
			boolean canSpeak) {
		MongoChatChannel mongoChatChannel = new MongoChatChannel(collectionChannels, channelName, channelDescription,
				permissionNode, isGlobalChannel, isPublicChannel, isCustomChannel, saveHistory, canSpeak);
		ChatChannel chatChannel = new ChatChannel(mongoChatChannel);
		mapChatChannels.put(chatChannel.getUniqueId(), chatChannel);
		mongoChatChannel.save();
		return chatChannel;
	}

	public void deleteChatChannel(ChatChannel chatChannel) {
		if (chatChannel == null) {
			throw new IllegalArgumentException("ChatChannel given is null.");
		}
		chatChannel.removePlayers();
		chatChannel.delete();
		mapChatChannels.remove(chatChannel.getUniqueId());
	}

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

}