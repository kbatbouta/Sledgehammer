package zirc.modules.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import zirc.ZIRC;
import zirc.module.SQLModule;
import zirc.util.Chat;
import zombie.network.DataBaseBuffer;

public class ModuleCore extends SQLModule {

	public static final String ID = "zirc_core";
	
	private CoreCommandListener commandListener;
	private CoreEventListener eventListener;
	
	private static String TABLE_GLOBAL_MUTE = "zirc_global_mute";
	
	public ModuleCore() {
		super(DataBaseBuffer.getDatabaseConnection());
	}
	
	private void validateTables() {
		Statement statement = null;
		try {
			statement = createStatement();
			statement.executeUpdate("create table if not exists " + TABLE_GLOBAL_MUTE + " (name TEXT, mute INTEGER NOT NULL CHECK (mute IN (0,1)));");
			statement.close();
		} catch(SQLException e) {
			e.printStackTrace();
			try {				
				statement.close();
			} catch(Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	public String toggleGlobalMute(String username) {
		if(username == null) return "Username is null.";
		try {
			Chat chat = ZIRC.instance.getChat();
			List<String> listGlobalMuters = chat.getGlobalMuters();
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
		commandListener = new CoreCommandListener(this);
		eventListener = new CoreEventListener(this);
	}

	@Override
	public void onStart() {
		
	}

	@Override
	public void onUpdate(long delta) {
		eventListener.update();
	}

	@Override
	public void onStop() {
		
	}

	@Override
	public void onUnload() {
		
	}

	@Override
	public String getModuleName() {
		return "ZIRC-Core";
	}

	@Override
	public String getVersion() {
		return "1.00";
	}
	
	public CoreCommandListener getCommandListener() {
		return this.commandListener;
	}
	
	public CoreEventListener getEventListener() {
		return this.eventListener;
	}

	@Override
	public String getModuleID() {
		return ID;
	}
	
}
