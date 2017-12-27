package sledgehammer.lua.chat;

import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

import sledgehammer.lua.LuaArray;
import sledgehammer.lua.LuaTable;

/**
 * Chat LuaTable designed to store <ChatMessage> entries for a <ChatChannel>.
 * 
 * @author Jab
 */
public class ChatHistory extends LuaTable {

	/** The Maximum amount of messages to store in the <ChatChannel>'s history. */
	public static final int MAX_SIZE = 1024;
	/** The <UUID> of the <ChatChannel>. */
	private UUID channelId;
	/** The <LinkedList> to store the <ChatMessage>s. */
	private LinkedList<ChatMessage> listMessages;

	/**
	 * Main constructor.
	 * 
	 * @param channelId
	 *            The <UUID> of the <ChatChannel> using the history.
	 */
	public ChatHistory(UUID channelId) {
		super("ChatHistory");
		setChannelId(channelId);
		listMessages = new LinkedList<>();
	}
	
	@Override
	public void onExport() {
		LuaArray<ChatMessage> listChatMessages = new LuaArray<>();
		for(ChatMessage chatMessage : listMessages) {
			listChatMessages.add(chatMessage);
		}
	}

	/**
	 * Adds a <Collection> of <ChatMessage> to the history.
	 * 
	 * @param collectionChatMessages
	 *            The <Collection> of <ChatMessage>s to add to the history.
	 */
	public void addMessages(Collection<ChatMessage> collectionChatMessages) {
		for (ChatMessage chatMessage : collectionChatMessages) {
			addMessage(chatMessage);
		}
	}

	/**
	 * Adds a <ChatMessage> to the history.
	 * 
	 * @param chatMessage
	 *            The <ChatMessage> being added to the history.
	 */
	public void addMessage(ChatMessage chatMessage) {
		// Make sure the history doesn't already contain the ChatMessage.
		if (!listMessages.contains(chatMessage)) {
			// Add the ChatMessage to the history.
			listMessages.add(chatMessage);
			// Check if the history is at message capacity.
			if (listMessages.size() > MAX_SIZE) {
				// If it is, grab the oldest ChatMessage to the list and delete it.
				ChatMessage chatMessageRemoved = listMessages.removeFirst();
				chatMessageRemoved.delete();
			}
		}
	}

	/**
	 * Clears the history, removing and deleting all the <ChatMessages> from the
	 * database.
	 */
	public void clear() {
		for (ChatMessage chatMessage : listMessages) {
			chatMessage.delete();
		}
		listMessages.clear();
	}

	/**
	 * @return Returns the <UUID>
	 */
	public UUID getChannelId() {
		return this.channelId;
	}

	private void setChannelId(UUID channelId) {
		this.channelId = channelId;
	}
}