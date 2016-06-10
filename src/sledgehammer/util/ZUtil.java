package sledgehammer.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import sledgehammer.SledgeHammer;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.GameServer;

public class ZUtil {
	
	/**
	 * The location for plug-ins, as a String. 
	 */
	public static String pluginLocation = SledgeHammer.getCacheFolder() + File.separator + "plugins" + File.separator;
	
	/**
	 * The location for plug-ins, as a File.
	 */
	public static File pluginFolder = new File(ZUtil.pluginLocation);

	public static Random random = new Random();
	
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
	
	/**
	 * Returns a String representation of the current time.
	 * @return
	 */
	public static String getHourMinuteSeconds() {
		String hours =  Calendar.getInstance().get(11) + "";
		if ( Calendar.getInstance().get(11) < 10) {
			hours = "0" + hours;
		}
		
		String minutes = Calendar.getInstance().get(12) + "";
		if (Calendar.getInstance().get(12) < 10) {
			minutes = "0" + minutes;
		}
		
		String seconds = Calendar.getInstance().get(13) + "";
		if (Calendar.getInstance().get(13) < 10) {
			seconds = "0" + seconds;
		}
		return Calendar.getInstance().get(11) + ":" + minutes + ":" + seconds;
	}
	
	public static void compactList(@SuppressWarnings("rawtypes") List list) {
		List<Integer> listIndexesToRemove = new ArrayList<>();
		Map<Object, Boolean> cacheMap = new HashMap<>();
		
		for(int index = 0; index < list.size(); index++) {
			Object o = list.get(index);
			
			Boolean cached = cacheMap.get(o);
			if(cached == null) {
				cacheMap.put(o, Boolean.valueOf(true));
			} else {
				listIndexesToRemove.add(index);
			}
		}
		
		synchronized(list) {
			for(int index : listIndexesToRemove) list.remove(index);
		}
	}

}
