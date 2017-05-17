package sledgehammer.modules.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.event.ChatMessageEvent;
import sledgehammer.event.ClientEvent;
import sledgehammer.manager.ChatManager;
import sledgehammer.module.SQLModule;
import sledgehammer.objects.Player;
import sledgehammer.objects.chat.ChatChannel;
import sledgehammer.objects.chat.ChatMessage;
import sledgehammer.objects.chat.ChatMessagePlayer;
import sledgehammer.requests.RequestChatChannels;
import zombie.Lua.LuaManager;

public class ModuleChat extends SQLModule {

	public static final String ID      = "ModuleChat";
	public static final String NAME    = "Chat";
	public static final String MODULE  = "core.chat";
	public static final String VERSION = "1.00";
	
	private static final String TABLE_CHANNELS = "sledgehammer_channels";
	private static final String TABLE_MESSAGES = "sledgehammer_messages";
	
	public ModuleChat() {}
	
	public void onLoad() {
		// Establish the SQLite database connection.
		establishConnection("sledgehammer_chat");
		validateTables();
	}

	public void validateTables() {
		Statement statement;
		try {
			statement = createStatement();
			statement.executeUpdate("create table if not exists " + TABLE_CHANNELS + " (name TEXT, description TEXT, context TEXT);");
			statement.executeUpdate("create table if not exists " + TABLE_MESSAGES 
					+ " (id BIGINT, origin TEXT, channel TEXT, message TEXT, message_original TEXT, edited BOOL, editor_id INTEGER, deleted BOOL, deleter_id INTEGER, modified_timestamp TEXT, player_id INTEGER, player_name TEXT, time TEXT);");
			statement.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void onStart() {
		addChannel(new ChatChannel("All"));
		addChannel(new ChatChannel("Global"));
		addChannel(new ChatChannel("Local"));
		addChannel(new ChatChannel("Test"));
	}
	
	public void addChannel(ChatChannel channel) {
		loadChannel(channel);
		getManager().addChatChannel(channel);
	}
	
	
	public void loadChannel(ChatChannel channel) {
		PreparedStatement pStatement;
		ResultSet set;
		Statement statement;
		try {
			statement = createStatement();
			statement.executeUpdate("create table if not exists " + "sledgehammer_channel_" + channel.getChannelName() + "_history (message_id INTEGER, time_added BIGINT);");
			
			pStatement = prepareStatement("SELECT * from " + TABLE_CHANNELS + " WHERE name = \"" + channel.getChannelName() + "\";");
			set = pStatement.executeQuery();
			if(set.next()) {
				println("Loading ChatChannel: " + channel.getChannelName());
				
				// Grab the stored definitions of the ChatChannel.
				String _desc = set.getString("description");
				String _cont = set.getString("context");
				
				// Apply definitions.
				channel.setDescription(_desc);
				channel.setContext(_cont);
				
				// Grab channel history (If any).
				getChannelHistory(channel, 32);
				
			} else {
				println("Creating ChatChannel: " + channel.getChannelName());
				
				// No definitions or history for channels being created.
				
				statement.executeUpdate("INSERT INTO " + TABLE_CHANNELS + " (name, description, context) VALUES (\"" + channel.getChannelName() + "\",\"\",\"" + channel.getContext() + "\");");
			}

			// Close streams.
			set.close();
			pStatement.close();
			statement.close();
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public ChatManager getManager() {
		return SledgeHammer.instance.getChatManager();
	}
	
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
			message.setTime(LuaManager.getHourMinuteJava());
			saveMessage(message);
			
			String channelName = (String) tableMessage.rawget("channel");
			
			ChatChannel channel = SledgeHammer.instance.getChatManager().getChannel(channelName);
			channel.addMessage(message);
			
			ChatMessageEvent e = new ChatMessageEvent(message);
			SledgeHammer.instance.handle(e);
		}
	}
	
	public void getChannelHistory(ChatChannel channel, int length) {
		//TODO: Implement.
		PreparedStatement statement;
		ResultSet set;
		try {
			String sql = "SELECT * FROM " + TABLE_MESSAGES + " WHERE channel = \"" + channel.getChannelName().toLowerCase() + "\" ORDER BY id DESC LIMIT " + length + ";";
			println(sql);
			statement = prepareStatement(sql);
			set = statement.executeQuery();
			
			List<ChatMessage> listMessages = new LinkedList<>();
			
			while(set.next()) {
				long _messageID = set.getLong("id");
				int _playerID = set.getInt("player_id");
				ChatMessage message = getManager().getMessageFromCache(_messageID);
				if(message == null) {
					
					String _channel         = set.getString("Channel");
					String _message         = set.getString("message");
					String _messageOriginal = set.getString("message_original");
					String _playerName      = set.getString("player_name");
					String _time            = set.getString("time");
					String _origin          = set.getString("origin");
					long _modifiedTimestamp = set.getLong("modified_timestamp");
					boolean _edited         = set.getBoolean("edited");
					int _editorID           = set.getInt("editor_id");
					boolean _deleted        = set.getBoolean("deleted");
					int _deleterID          = set.getInt("deleter_id");
					
					if (_playerID != -1 && _origin == ChatMessage.ORIGIN_CLIENT) {
						message = new ChatMessagePlayer(_messageID, _channel, _message, _messageOriginal, _edited,
								_editorID, _deleted, _deleterID, _modifiedTimestamp, _time, _playerID, _playerName);
						message.setOrigin(_origin);
					} else {
						message = new ChatMessage(_messageID, _channel, _message, _messageOriginal, _edited, _editorID,
								_deleted, _deleterID, _modifiedTimestamp, _time);
						message.setOrigin(_origin);
					}
					
					getManager().addMessageToCache(message);
				}
				
				listMessages.add(message);
			}
			for(int index = 0; index < listMessages.size(); index++) {				
				channel.addMessage(listMessages.get(index));
			}
			
			set.close();
			statement.close();
		} catch(SQLException e) {
			e.printStackTrace();
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
						+ "player_name = \"" + playerName + "\" AND"
						+ "time = \"" + message.getTime() + "\" "
					+ "WHERE id = " + message.getMessageID() + "\";";
			
			} else {
				sql = "INSERT INTO " + TABLE_MESSAGES
						+ " (id, origin, channel, message, message_original, edited, editor_id, deleted, deleter_id, modified_timestamp, player_id, player_name, time) "
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
							+ "\"" + playerName + "\","
							+ "\"" + message.getTime() + "\""
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
	
	@SuppressWarnings("unused")
	private void loadAllChannels() {
		PreparedStatement statement;
		ResultSet set;
		try {
			statement = prepareStatement("SELECT * from " + TABLE_CHANNELS + ";");
			set = statement.executeQuery();
			
			while(set.next()) {
				String _name = set.getString("name");
				String _desc = set.getString("description");
				String _cont = set.getString("context");
				ChatChannel channel = new ChatChannel(_name, _desc, _cont);
				getManager().addChatChannel(channel);
			}
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public String getID()         { return ID;      }
	public String getName()       { return NAME;    }
	public String getModuleName() { return MODULE;  }
	public String getVersion()    { return VERSION; }

}