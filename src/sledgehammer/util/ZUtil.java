package sledgehammer.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
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
	 * 
	 * @param username
	 * 
	 * @return
	 */
	public static IsoPlayer getPlayerByUsername(String username) {
		return getPlayerByUsername(username, false);
	}

	/**
	 * 
	 * @param nickname
	 * 
	 * @return
	 */
	public static IsoPlayer getPlayerByNickname(String nickname) {
		return getPlayerByNickname(nickname, false);
	}
	
	/**
	 * 
	 * @param nickname
	 * 
	 * @return
	 */
	public static IsoPlayer getPlayerByNicknameDirty(String nickname) {
		return getPlayerByNickname(nickname, true);
	}

	/**
	 * 
	 * @param username
	 * 
	 * @return
	 */
	public static IsoPlayer getPlayerByUsernameDirty(String username) {
		return getPlayerByUsername(username, true);
	}
	
	
	/**
	 * Returns a UdpConnection instance tied with the IsoPlayer instance given.
	 * 
	 * @param player
	 * 
	 * @return
	 */
	public static UdpConnection getConnection(IsoPlayer player) {
		Long guid = GameServer.PlayerToAddressMap.get(player);

		if (guid == null)
			return null;

		return SledgeHammer.instance.getUdpEngine().getActiveConnection(guid.longValue());
	}
	
	/**
	 * Returns a Player, based on a user-name.
	 * 
	 * If the wild-card parameter is flag as true, this method will search to
	 * see if the user-name given is contained in a player's user-name.
	 * 
	 * @param username
	 *
	 * @param wildcard
	 *
	 * @return
	 */
	private static IsoPlayer getPlayerByUsername(String username, boolean wildcard) {

		if (wildcard) username = username.toLowerCase().trim();
		
		for (UdpConnection connection : SledgeHammer.instance.getConnections()) {
			for (int playerIndex = 0; playerIndex < 4; ++playerIndex) {
				IsoPlayer player = connection.players[playerIndex];
				if (player != null) {
					String usernameNext = player.getUsername().toLowerCase();
					if (wildcard) {
						if (usernameNext.contains(username)) return player;
					} else {
						if (usernameNext.equals(username)) return player;
					}
				}
			}
		}

		return null;
	}
	
	/**
	 * Returns a Player, based on a nick-name.
	 * 
	 * If the wild-card parameter is flag as true, this method will search to
	 * see if the nick-name given is contained in a player's nick-name.
	 * 
	 * @param nickname
	 *
	 * @param wildcard
	 *
	 * @return
	 */
	private static IsoPlayer getPlayerByNickname(String nickname, boolean wildcard) {

		if (wildcard) nickname = nickname.toLowerCase().trim();
		
		for (UdpConnection connection : SledgeHammer.instance.getConnections()) {
			for (int playerIndex = 0; playerIndex < 4; ++playerIndex) {
				IsoPlayer player = connection.players[playerIndex];
				if (player != null) {
					String usernameNext = player.getPublicUsername().toLowerCase();
					if (wildcard) {
						if (usernameNext.contains(nickname)) return player;
					} else {
						if (usernameNext.equals(nickname)) return player;
					}
				}
			}
		}

		return null;
	}
	
	/**
	 * Returns a Player, based on the UdpConnection given.
	 * 
	 * @param connection
	 * 
	 * @return
	 */
	public static IsoPlayer getPlayer(UdpConnection connection) {
		long guid = connection.getConnectedGUID();
		
		for(IsoPlayer player : GameServer.PlayerToAddressMap.keySet()) {
			if(player != null) {
				Long value = (Long) GameServer.PlayerToAddressMap.get(player);
				if(value.longValue() == guid) {
					return (IsoPlayer) player;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Returns a Player, based on a user-name. If the name given doesn't match any
	 * connected user-names, a second search will occur, attempting to match
	 * nicknames with the name.
	 * 
	 * @param username
	 * @return
	 */
	public static IsoPlayer getPlayer(String name) {
		IsoPlayer player = null;
		
		player = getPlayerByUsername(name, false);
		
		if(player == null) {
			player = getPlayerByNickname(name, false);
		}
		
		return player;
	}
	
	/**
	 * Returns a Player, based on a user-name. If the name given doesn't match any
	 * connected user-names, a second search will occur, attempting to match
	 * nicknames with the name.
	 * 
	 * If the wild-card parameter is flag as true, a 2nd iteration of the search
	 * will be made if the player is not located, searching for the name given,
	 * to see if the name is contained in a user-name, or nickname of a player.
	 * 
	 * @param name
	 * 
	 * @param wildcard
	 * 
	 * @return
	 */
	public static IsoPlayer getPlayerDirty(String name, boolean wildcard) {
		IsoPlayer player = null;
		
		player = getPlayerByUsername(name, false);
		
		if(player == null) {
			player = getPlayerByNickname(name, false);
		}
		
		if(player == null && wildcard) {
			
			name = name.toLowerCase().trim();
			
			player = getPlayerByUsername(name, true);
			
			if(player == null) {
				player = getPlayerByNickname(name, true);
			}
		}
		
		return player;
	}
	
	/**
	 * Returns a Player, based on a username.
	 * 
	 * @param username
	 * 
	 * @return
	 */
	public static IsoPlayer getPlayerDirty(String username) {
		return getPlayerDirty(username, true);
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
	
	@SuppressWarnings("rawtypes")
	public static void compactList(List list) {
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
			try{
				for(int index : listIndexesToRemove) list.remove(index);
			} catch(IndexOutOfBoundsException e) {
				// Catches any asynchronous concurrent modifications.
			}
		}
	}
	
	public static void addDir(String s) throws IOException {
	    try {
	        // This enables the java.library.path to be modified at runtime
	        // From a Sun engineer at http://forums.sun.com/thread.jspa?threadID=707176
	        //
	        Field field = ClassLoader.class.getDeclaredField("usr_paths");
	        field.setAccessible(true);
	        String[] paths = (String[])field.get(null);
	        for (int i = 0; i < paths.length; i++) {
	            if (s.equals(paths[i])) {
	                return;
	            }
	        }
	        String[] tmp = new String[paths.length+1];
	        System.arraycopy(paths,0,tmp,0,paths.length);
	        tmp[paths.length] = s;
	        field.set(null,tmp);
	        System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + s);
	    } catch (IllegalAccessException e) {
	        throw new IOException("Failed to get permissions to set library path");
	    } catch (NoSuchFieldException e) {
	        throw new IOException("Failed to get field handle to set library path");
	    }
	}

}
