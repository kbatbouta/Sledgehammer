package sledgehammer.modules.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import sledgehammer.module.SQLModule;
import sledgehammer.util.ZUtil;
import zombie.network.DataBaseBuffer;

public class ModuleCore extends SQLModule {

	public static final String ID      = "sledgehammer_core";
	public static final String NAME    = "Core"             ;
	public static final String VERSION = "1.00"             ;
	
	public CoreLuaEventListener luaEventListener = null;
	
	private CoreCommandListener commandListener;
	private CoreEventListener eventListener;
	
	private static String TABLE_GLOBAL_MUTE       = "sledgehammer_global_mute"      ;
	private static String TABLE_GLOBAL_MESSAGES   = "sledgehammer_global_messages"  ;
	private static String TABLE_PLAYER_PROPERTIES = "sledgehammer_player_properties";
	
	private List<PeriodicMessage> listPeriodicMessages;
	private Map<String, PeriodicMessage> mapPeriodicMessages;
	
	private long timeThen = 0L;
	
	public ModuleCore() {
		super(DataBaseBuffer.getDatabaseConnection());
	}
	
	private void validateTables() {
		Statement statement = null;
		try {
			
			statement = createStatement();
			statement.executeUpdate("create table if not exists " + TABLE_GLOBAL_MUTE       + " (name TEXT, mute INTEGER NOT NULL CHECK (mute IN (0,1)));");
			statement.executeUpdate("create table if not exists " + TABLE_GLOBAL_MESSAGES   + " (name TEXT, content TEXT, color TEXT, enabled BOOL, time INTEGER, broadcast BOOL);");
			statement.executeUpdate("create table if not exists " + TABLE_PLAYER_PROPERTIES + " (id INTEGER, json TEXT);");
			statement.close();
			
			addTableColumnIfNotExists("bannedid", "username", SQL_STORAGE_CLASS_TEXT);
			
		} catch(SQLException e) {
			stackTrace(e);
			try {				
				statement.close();
			} catch(Exception e2) {
				stackTrace(e2);
			}
		}
	}
	
	public String toggleGlobalMute(String username) {
		if(username == null) return "Username is null.";
		try {
			List<String> listGlobalMuters = getGloballyMutedUsernames();
			String muted = get(TABLE_GLOBAL_MUTE, "name", username, "mute");
			if(muted != null) {
				if(muted.equals("1")) {
					PreparedStatement statement = prepareStatement("UPDATE " + TABLE_GLOBAL_MUTE + " SET mute = \"0\" where name = \"" + username + "\"");
					statement.executeUpdate();
					statement.close();
					listGlobalMuters.remove(username);
					return "Global mute disabled.";
				} else
				if(muted.equals("0")) {
					PreparedStatement statement = prepareStatement("UPDATE " + TABLE_GLOBAL_MUTE + " SET mute = \"1\" where name = \"" + username + "\"");
					statement.executeUpdate();
					statement.close();
					if(!listGlobalMuters.contains(username)) listGlobalMuters.add(username);
					return "Global mute enabled. To disable it, type \"/globalmute\"";
				}
			} else {
				PreparedStatement statement = prepareStatement("INSERT INTO " + TABLE_GLOBAL_MUTE + " (name, mute) VALUES (\"" + username + "\", \"1\")");
				statement.executeUpdate();
				statement.close();
				if(!listGlobalMuters.contains(username)) listGlobalMuters.add(username);
				return "Global mute enabled. To disable it, type \"/globalmute\"";
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return "Failed to toggle global mute. (Internal Error)";
	}
	
	protected boolean getGlobalMuted(String username) {
		if(username == null) {
			println("getGlobalMuted: Username is null!");
			return false;
		}
		try {
			String muted = get(TABLE_GLOBAL_MUTE, "name", username, "mute");
			if(muted != null) {
				if(muted.equals("1")) {
					return true;
				} else {
					return false;
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public void onLoad() {
		validateTables();
		
		// Initialize the listeners.
		commandListener  = new CoreCommandListener(this);
		eventListener    = new CoreEventListener(this);
		luaEventListener = new CoreLuaEventListener(this);
		
		// Initialize the Lists & Maps.
		listPeriodicMessages = new ArrayList<>();
		mapPeriodicMessages  = new HashMap<>();
		
		try {
			loadPeriodicMessages();
		} catch (SQLException e) {
			stackTrace("Failed to load Periodic Messages.", e);
		}
		
	}

	private void loadPeriodicMessages() throws SQLException {
		Map<String, List<String>> table = getAll(TABLE_GLOBAL_MESSAGES, new String[] { "name", "content", "color", "enabled", "time", "broadcast" });
		List<String> messageNames      = table.get("name");
		List<String> messageContents   = table.get("content");
		List<String> messageColors     = table.get("color");
		List<String> messageEnableds   = table.get("enabled");
		List<String> messageTimes      = table.get("time");
		List<String> messageBroadcasts = table.get("broadcast");
		
		// Grab the total amount of PeriodicMessages.
		int size = messageNames.size();
		
		// Go through each message.
		for(int index = 0; index < size; index++) {
			
			// Grab the values associated with the message.
			String name       = messageNames.get(index);
			String content    = messageContents.get(index);
			String color      = messageColors.get(index);
			boolean enabled   = Boolean.parseBoolean(messageEnableds.get(index));
			int time          = Integer.parseInt(messageTimes.get(index));
			boolean broadcast = Boolean.parseBoolean(messageBroadcasts.get(index));
			
			// Create the PeriodicMessage instance.
			PeriodicMessage periodicMessage = new PeriodicMessage(name, content);
			// Set additional flags.
			periodicMessage.setEnabled(enabled);
			periodicMessage.setTime(time);
			periodicMessage.setColor(color);
			periodicMessage.setBroadcasted(broadcast);
			
			// We will flag this to save, so as to allow third-party plug-ins to
			// add temporary messages.
			periodicMessage.setShouldSave(true);
			
			// Add the PeriodicMessage to the collection.
			addPeriodicMessage(periodicMessage);
			
			println("Periodic Message added: name: " + name + " content: " + content + " color: " + color + " enabled: " + enabled + " time: " + time + " broadcast: " + broadcast);
		}
	}

	/**
	 * Adds a PeriodicMessage to the core, which will be displayed peridically
	 * to all players who have global-chat enabled.
	 * 
	 * @param periodicMessage
	 */
	private void addPeriodicMessage(PeriodicMessage periodicMessage) {
		
		String name = periodicMessage.getName();
		
		PeriodicMessage mapCheck = mapPeriodicMessages.get(name);
		if(mapCheck != null) {
			throw new IllegalArgumentException("PeriodicMessage already exists: " + name + ".");
		}
		
		mapPeriodicMessages.put(name, periodicMessage);
		
		if(!listPeriodicMessages.contains(periodicMessage)) {
			listPeriodicMessages.add(periodicMessage);
		}
		
	}
	
	private void savePeriodicMessage(PeriodicMessage message) {
		try {

			// Grab all variables as strings.
			String name      =      message.getName();
			String content   =      message.getContent();
			String color     =      message.getColor();
			String enabled   = "" + message.isEnabled();
			String time      = "" + message.getTime();
			String broadcast = "" + message.isBroadcasted();

			PreparedStatement statement;
			
			// If the message already exists.
			if(has(TABLE_GLOBAL_MESSAGES, "name", message.getName())) {
				
				// Update content.
				statement = prepareStatement("UPDATE " + TABLE_GLOBAL_MESSAGES + " SET content = \"" + content + "\" WHERE name = \"" + name + "\"");
				statement.executeUpdate();
				statement.close();
				
				// Update color.
				statement = prepareStatement("UPDATE " + TABLE_GLOBAL_MESSAGES + " SET color = \"" + color + "\" WHERE name = \"" + name + "\"");
				statement.executeUpdate();
				statement.close();
				
				// Update enabled.
				statement = prepareStatement("UPDATE " + TABLE_GLOBAL_MESSAGES + " SET enabled = \"" + enabled + "\" WHERE name = \"" + name + "\"");
				statement.executeUpdate();
				statement.close();
				
				// Update time.
				statement = prepareStatement("UPDATE " + TABLE_GLOBAL_MESSAGES + " SET time = \"" + time + "\" WHERE name = \"" + name + "\"");
				statement.executeUpdate();
				statement.close();
				
				// Update broadcast.
				statement = prepareStatement("UPDATE " + TABLE_GLOBAL_MESSAGES + " SET broadcast = \"" + broadcast + "\" WHERE name = \"" + name + "\"");
				statement.executeUpdate();
				statement.close();
				
			// This is brand new. Save as new.
			} else {
				statement = prepareStatement("INSERT INTO " + TABLE_GLOBAL_MESSAGES + " (name, content, color, enabled, time, broadcast) VALUES "
						+ "(\"" + name + "\", \"" + content + "\", \"" + color + "\", \"" + enabled + "\", \"" + time + "\", \"" + broadcast + "\")");
				statement.executeUpdate();
				statement.close();
			}
		} catch(SQLException e) {
			stackTrace("Failed to save PeriodicMessage: " + message.getName() + ".", e);
		}
	}

	public void onUpdate(long delta) {
		
		eventListener.getPlayerTimeStamps().clear();
		
		// Grab the current time.
		long timeNow = System.currentTimeMillis();
		
		// If it has been a minute since the last check.
		if(timeNow - timeThen > 60000) {
			
			// Go through each PeriodicMessage instance.
			for(PeriodicMessage message : listPeriodicMessages) {
				
				// Update the list.
				message.update();
			}
			
			// Set the time to reset the delta.
			timeThen = timeNow;
		}
		
	}
	
	public Map<String, String> getProperties(int id) {
		
		String json = null;
		
		try {
			json = get(TABLE_PLAYER_PROPERTIES, "id", "" + id, "json");
		} catch (SQLException e) {
			stackTrace("Failed to fetch properties json map from the database!", e);
			return new HashMap<>();
		}
		
		if(json == null) return new HashMap<>();
		
		Gson gson = ZUtil.getGson();
		
		return gson.fromJson(json, new TypeToken<HashMap<String, String>>(){}.getType());
	}
	
	public void saveProperties(int id, Map<String, String> mapProperties) {
		Gson gson = ZUtil.getGson();
		String json = gson.toJson(mapProperties);
		
		try {
			set(TABLE_PLAYER_PROPERTIES, "id", "" + id, "json", json);
		} catch (SQLException e) {
			stackTrace("Failed to save player properties for id: " + id + ".", e);
			print("json: " + json);
		}
		
	}

	public CoreCommandListener getCommandListener() {
		return this.commandListener;
	}
	
	public CoreEventListener getEventListener() {
		return this.eventListener;
	}

	public void onStart()  {
		//register(luaEventListener);
	}
	
	public void onStop()   {
		
		for(PeriodicMessage message : listPeriodicMessages) {
			if(message.shouldSave()) {
				savePeriodicMessage(message);
			}
		}
		
		//unregister(luaEventListener);
	}

	public void onUnload() { }
	public String getID()      { return ID     ; }
	public String getName()    { return NAME   ; }
	public String getVersion() { return VERSION; }
	
}
