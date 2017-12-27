package sledgehammer.lua.chat.send;

import sledgehammer.lua.Send;
import sledgehammer.lua.chat.ChatMessage;

// @formatter:off
/**
 * Send Class for ChatMessages.
 * 
 * Exports a LuaTable:
 * {
 *   - "channel": (String) UUID of the Channel.
 *   - "message": (LuaTable) ChatMessage Object.
 * }
 * 
 * @author Jab
 */
// @formatter:on
public class SendChatMessage extends Send {

	/** The <ChatMessage> being sent. */
	private ChatMessage message;

	/**
	 * Main constructor.
	 */
	public SendChatMessage() {
		super("core.chat", "sendChatMessage");
	}

	@Override
	public void onExport() {
		ChatMessage message = getChatMessage();
		set("channel", message.getChannelId().toString());
		set("message", message);
	}

	/**
	 * Sets the <ChatMessage> being sent.
	 * 
	 * @param message
	 *            The <ChatMessage> being sent.
	 */
	public void setChatMessage(ChatMessage message) {
		this.message = message;
	}

	/**
	 * @return Returns the <ChatMessage> being sent.
	 */
	public ChatMessage getChatMessage() {
		return this.message;
	}
}