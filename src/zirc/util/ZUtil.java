package zirc.util;

import zirc.ZIRC;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.GameServer;

public class ZUtil {
	
	public static UdpConnection getConnection(IsoPlayer player) {
		return GameServer.getConnectionFromPlayer(player);
	}
	
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
		
		ZIRC.println("Error: " + errorText + ": " + e.getMessage());
		for(StackTraceElement o : e.getStackTrace()) {
			ZIRC.println(o);
		}
	}

}
