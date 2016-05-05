package sledgehammer.util;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import sledgehammer.SledgeHammer;
import zombie.GameWindow;
import zombie.characters.IsoPlayer;
import zombie.core.Core;
import zombie.core.raknet.UdpConnection;
import zombie.network.DataBaseBuffer;
import zombie.network.GameServer;

public class ZUtil {
	
	/**
	 * The location for plug-ins, as a String. 
	 */
	public static String pluginLocation = SledgeHammer.instance.getFolder() + File.separator + "plugins" + File.separator;
	
	/**
	 * The location for plug-ins, as a File.
	 */
	public static File pluginFolder = new File(ZUtil.pluginLocation);
	
	/**
	 * Initializes the plug-in folder, before using it.
	 */
	public static void initPluginFolder() {
		if (!ZUtil.pluginFolder.exists())
			ZUtil.pluginFolder.mkdirs();
	}
	
	/**
	 * Returns a UdpConnection instance tied with the IsoPlayer instance given.
	 * @param player
	 * @return
	 */
	public static UdpConnection getConnection(IsoPlayer player) {
		return GameServer.getConnectionFromPlayer(player);
	}
	
	/**
	 * 
	 * @param username
	 * @return
	 */
	public static IsoPlayer getPlayer(String username) {
		return GameServer.getPlayerByUserName(username);
	}
	
	public static IsoPlayer getPlayer(UdpConnection connection) {
		long guid = connection.getConnectedGUID();
		for(Object o : GameServer.PlayerToAddressMap.keySet()) {
			if(o != null) {
				Long value = (Long) GameServer.PlayerToAddressMap.get(o);
				if(value.longValue() == guid) {
					return (IsoPlayer) o;
				}
			}
		}
		return null;
	}
	
	public static boolean isClass(String className) {
	    try  {
	        Class.forName(className);
	        return true;
	    }  catch (final ClassNotFoundException e) {
	        return false;
	    }
	}
	
	public static void printStackTrace(Exception e) {
		printStackTrace((String)null, e);
	}
	
	public static void printStackTrace(String errorText, Exception e) {
		if(errorText == null) {
			errorText = "";
		} else if(!errorText.isEmpty()) {
			errorText = errorText.trim() + ": ";
		}
		
		SledgeHammer.println("Error: " + errorText + ": " + e.getMessage());
		for(StackTraceElement o : e.getStackTrace()) {
			SledgeHammer.println(o);
		}
	}
	
	/**
   	 * Returns whether or not the vanilla whitelist has a user set to admin.
   	 * @param username
   	 * @return
   	 * @throws SQLException
   	 */
	public static boolean isUserAdmin(String username) {		
		
		String admin = "";

		try {
			// Create a statement with the vanilla database file, in whitelist where the admin status is stored.
			PreparedStatement stat = DataBaseBuffer.getDatabaseConnection().prepareStatement("SELECT * FROM whitelist WHERE world = ?");
			stat.setString(1, Core.GameSaveWorld);
			
			// Execute the query, and grab the results.
			ResultSet rs = stat.executeQuery();
	
			// Go through each user entry.
			while(rs.next()) {
				
				// Grab the name and set to lowercase to match.
				String name = rs.getString("username").toLowerCase().trim();
				
				// This is the username. Grab admin status and break.
				if(name.equalsIgnoreCase(username)) {
					admin = rs.getString("admin");
					break;
				}
			}
			
			// Close the SQLite handlers.
			rs.close();
			stat.close();
		} catch(SQLException e) {
			SledgeHammer.println("ERROR: Failure to check if user is admin: " + username + " Error: " + e.getMessage());
			printStackTrace(e);
		}
		
		// Return whether or not the result is a boolean true.
		return admin.equalsIgnoreCase("true") || admin.equalsIgnoreCase("1");
	}

}
