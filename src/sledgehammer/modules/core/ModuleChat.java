package sledgehammer.modules.core;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import se.krka.kahlua.vm.KahluaTable;
import sledgehammer.SledgeHammer;
import sledgehammer.event.ClientEvent;
import sledgehammer.module.SQLModule;
import sledgehammer.objects.Player;
import sledgehammer.objects.chat.ChatChannel;
import sledgehammer.objects.chat.ChatMessagePlayer;
import sledgehammer.requests.RequestChatChannels;

public class ModuleChat extends SQLModule {

	public static final String ID      = "ModuleChat";
	public static final String NAME    = "Chat";
	public static final String MODULE  = "core.chat";
	public static final String VERSION = "1.00";
	
	private static final String TABLE_CHANNELS = "sledgehammer_channels";
	
	public ModuleChat() {
		
	}
	
	public void onLoad() {
		// Establish the SQLite database connection.
		establishConnection("sledgehammer_chat");
		validateTables();
	}
	
	public void validateTables() {
		Statement statement;
		try {
			statement = createStatement();
			statement.executeUpdate("create table if not exists " + TABLE_CHANNELS + " (id INTEGER PRIMARY KEY ASC, name TEXT, description TEXT);");
			statement.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
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
			String channelName = (String) tableMessage.rawget("channel");
			
			ChatChannel channel = SledgeHammer.instance.getChatManager().getChannel(channelName);
			channel.addMessage(message);
		}
	}
	
	public String getID()         { return ID;      }
	public String getName()       { return NAME;    }
	public String getModuleName() { return MODULE;  }
	public String getVersion()    { return VERSION; }

}
