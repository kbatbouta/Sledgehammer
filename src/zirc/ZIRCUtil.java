package zirc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import zirc.module.Module;
import zombie.GameWindow;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.GameServer;

public class ZIRCUtil {
	
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
	
	static String fs = File.separator;
	public static String pluginLocation = GameWindow.getCacheDir() + fs + "Server" + fs + "ZIRC" + fs + "plugins" + fs;
	static File pluginFolder = new File(pluginLocation);
	
	static Module loadPlugin(String name) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		String pluginName = pluginLocation + name + ".jar";
		File pluginFile = new File(pluginName);
		if(!pluginFile.exists()) throw new IllegalArgumentException("Jar file not found: " + pluginName);
		
		Map<String,String> pluginSettings = getSettings(pluginName);
		String module = pluginSettings.get("module");
		if(module == null) throw new IllegalArgumentException("plugin.txt is not valid: " + pluginName);
		
		URL url = pluginFile.toURI().toURL();
		URL[] urls = {url};
		ClassLoader loader = new URLClassLoader(urls);
		
		List<String> listClasses = new ArrayList<>();
		
		JarFile jarFile = new JarFile(pluginName);
		Enumeration<?> e = jarFile.entries();
		while (e.hasMoreElements()) {
	        JarEntry entry = (JarEntry) e.nextElement();
	        if(entry.isDirectory() || !entry.getName().endsWith(".class")) continue;
		    String className = entry.getName().substring(0,entry.getName().length()-6);
		    className = className.replace('/', '.');
		    listClasses.add(className);
		}
		jarFile.close();
		
		for(String clazz : listClasses) loader.loadClass(clazz);
		
		Class<?> classToLoad = Class.forName(module, true, loader);
		Module instance = (Module)classToLoad.newInstance();
		instance.setPluginSettings(pluginSettings);
		instance.setJarName(name);
		return instance;
	}
	
	static void verifyPluginFolder() {
		if(!pluginFolder.exists()) pluginFolder.mkdirs();
	}
	
	private static Map<String,String> getSettings(String fileName) {
		URL url;
		
		Map<String,String> listSettings = new HashMap<>();
		try {
			url = new URL("jar:file:" + fileName + "!/plugin.txt");
			InputStream is = url.openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				String line;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if(line.toLowerCase().startsWith("module:")) {
						listSettings.put("module", line.split(":")[1]);
					}
				}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return listSettings;
	}
	
	
	
}
