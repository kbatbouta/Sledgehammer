package sledgehammer.modules.core;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.event.ClientEvent;
import sledgehammer.manager.ChatManager;
import sledgehammer.module.SQLModule;
import sledgehammer.objects.Player;
import sledgehammer.objects.chat.ChatChannel;
import sledgehammer.objects.chat.ChatMessage;
import sledgehammer.objects.chat.ChatMessagePlayer;
import sledgehammer.requests.RequestChatChannels;

public class ModuleChat extends SQLModule {

	public static final String ID      = "ModuleChat";
	public static final String NAME    = "Chat";
	public static final String MODULE  = "core.chat";
	public static final String VERSION = "1.00";
	
	private static final String TABLE_CHANNELS = "sledgehammer_channels";
	private static final String TABLE_MESSAGES = "sledgehammer_messages";
	
	public ModuleChat() {
		
	}
	
	public void onLoad() {
		// Establish the SQLite database connection.
		establishConnection("sledgehammer_chat");
		validateTables();
		
		addChannel(new ChatChannel("All"));
		addChannel(new ChatChannel("Global"));
		addChannel(new ChatChannel("Local"));
		addChannel(new ChatChannel("Test"));
	}
	
	public void validateTables() {
		Statement statement;
		try {
			statement = createStatement();
			statement.executeUpdate("create table if not exists " + TABLE_CHANNELS + " (id INTEGER PRIMARY KEY ASC, name TEXT, description TEXT);");
			statement.executeUpdate("create table if not exists " + TABLE_MESSAGES 
					+ " (id BIGINT, origin TEXT, channel TEXT, message TEXT, message_original TEXT, edited BOOL, editor_id INTEGER, deleted BOOL, deleter_id INTEGER, modified_timestamp TEXT, player_id INTEGER, player_name TEXT);");
			statement.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void addChannel(ChatChannel channel) {
		loadChannel(channel);
		getManager().addChatChannel(channel);
	}
	
	
	public void loadChannel(ChatChannel channel) {
		Statement statement;
		try {
			statement = createStatement();
			
			// TODO: Settings.
			// statement.executeUpdate("create table if not exists " + "sledgehammer_channel_" + channel.getChannelName() + " (" /*Settings */+ ");");
			statement.executeUpdate("create table if not exists " + "sledgehammer_channel_" + channel.getChannelName() + "_history (message_id INTEGER, time_added BIGINT);");
			statement.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public ChatManager getManager() {
		return SledgeHammer.instance.getChatManager();
	}

	public void onStart() {}
	public void onUpdate(long delta) {}
	public void onStop() {}
	public void onUnload() {}


	public void onClientCommand(ClientEvent event) {
		String command = event.getCommand();

		if(command.equalsIgnoreCase("getChatChannels")) {
	
			Player player = event.getPlayer();
			
			List<ChatChannel> channels = SledgeHammer.instance.getChatManager().getChannelsForPlayer(player);
			RequestChatChannels request = new RequestChatChannels();
			
			for(ChatChannel channel : channels) {
				request.addChannel(channel);
			}
			
			event.respond(request);
		} else if(command.equalsIgnoreCase("sendChatMessagePlayer")) {
			// Get the arguments.
			KahluaTable table = event.getTable();
			KahluaTable tableMessage = (KahluaTable) table.rawget("message");
			ChatMessagePlayer message = new ChatMessagePlayer(tableMessage, System.nanoTime());
			
			saveMessage(message);
			
			String channelName = (String) tableMessage.rawget("channel");
			
			ChatChannel channel = SledgeHammer.instance.getChatManager().getChannel(channelName);
			channel.addMessage(message);
		}
	}
	
	public void saveMessage(ChatMessage message) {
		Statement statement;
		try {
			long messageID = message.getMessageID();
			String origin = message.getOrigin();
			
			ChatChannel channel = getManager().getChannel(message.getChannel());
			if(channel == null) {
				println("Message has no channel: " + message.getChannel());
				return;
			}
			
			int playerID = -1;
			String playerName = "";
			
			if(message instanceof ChatMessagePlayer) {
				ChatMessagePlayer mPlayer = (ChatMessagePlayer)message;
				playerID = mPlayer.getPlayer().getID();
				playerName = mPlayer.getPlayer().getNickname();
			}
			
			String sql = "";
			
			// Upsert the message into the database.
			if (has(TABLE_MESSAGES, "id", "" + messageID)) {
				sql = "UPDATE " + TABLE_MESSAGES 
					+ " SET "
						+ "origin = \"" + origin + "\" AND "
						+ "channel = \"" + message.getChannel() + "\" AND "
						+ "message = \"" + message.getMessage() + "\" AND "
						+ "message_original = \"" + message.getOriginalMessage() + "\" AND "
						+ "edited = \"" + message.isEdited() + "\" AND "
						+ "editor_id = \"" + message.getEditorID() + "\" AND "
						+ "deleted = \"" + message.isDeleted() + "\" AND "
						+ "deleter_id = \"" + message.getDeleterID() + "\" AND "
						+ "modified_timestamp = \"" + message.getModifiedTimestamp() + "\" AND "
						+ "player_id = \"" + playerID + "\" AND "
						+ "player_name = \"" + playerName + "\" "
					+ "WHERE id = " + message.getMessageID() + "\";";
			
			} else {
				sql = "INSERT INTO " + TABLE_MESSAGES
						+ " (id, origin, channel, message, message_original, edited, editor_id, deleted, deleter_id, modified_timestamp, player_id, player_name) "
						+ "VALUES ("
							+ "\"" + messageID + "\","
							+ "\"" + origin + "\","
							+ "\"" + message.getChannel() + "\","
							+ "\"" + message.getMessage() + "\","
							+ "\"" + message.getOriginalMessage() + "\","
							+ "\"" + message.isEdited() + "\","
							+ "\"" + message.getEditorID() + "\","
							+ "\"" + message.isDeleted() + "\","
							+ "\"" + message.getDeleterID() + "\","
							+ "\"" + message.getModifiedTimestamp() + "\","
							+ "\"" + playerID + "\","
							+ "\"" + playerName + "\""
						+ ");";
			}

			statement = createStatement();
			statement.executeUpdate(sql);
			
			// Update the channel history.
			String tableHistory = "sledgehammer_channel_" + channel.getChannelName() + "_history";
			if(!has(tableHistory, "message_id", "" + messageID)) {
				sql = "INSERT INTO " + tableHistory + " (message_id, time_added) VALUES (\"" + messageID + "\", \"" + System.currentTimeMillis() + "\");";
				statement.executeUpdate(sql);
			}
			statement.close();
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public String getID()         { return ID;      }
	public String getName()       { return NAME;    }
	public String getModuleName() { return MODULE;  }
	public String getVersion()    { return VERSION; }

}
