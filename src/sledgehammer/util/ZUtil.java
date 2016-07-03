package sledgehammer.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import sledgehammer.SledgeHammer;

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
	
	/**
	 * DirectByteBuffers are garbage collected by using a phantom reference and
	 * a reference queue. Every once a while, the JVM checks the reference queue
	 * and cleans the DirectByteBuffers. However, as this doesn't happen
	 * immediately after discarding all references to a DirectByteBuffer, it's
	 * easy to OutOfMemoryError yourself using DirectByteBuffers. This function
	 * explicitly calls the Cleaner method of a DirectByteBuffer.
	 * 
	 * @param toBeDestroyed
	 *            The DirectByteBuffer that will be "cleaned". Utilizes
	 *            reflection.
	 * 
	 */
	public static void destroyDirectByteBuffer(ByteBuffer toBeDestroyed) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {

		// Not sure if needed. JDK8
		// Preconditions.checkArgument(toBeDestroyed.isDirect(), "toBeDestroyed
		// isn't direct!");

		Method cleanerMethod = toBeDestroyed.getClass().getMethod("cleaner");
		cleanerMethod.setAccessible(true);
		Object cleaner = cleanerMethod.invoke(toBeDestroyed);
		Method cleanMethod = cleaner.getClass().getMethod("clean");
		cleanMethod.setAccessible(true);
		cleanMethod.invoke(cleaner);

	}
	
	public static void addDir(String s) throws IOException {
		try {
			// This enables the java.library.path to be modified at runtime
			// From a Sun engineer at
			// http://forums.sun.com/thread.jspa?threadID=707176
			//
			Field field = ClassLoader.class.getDeclaredField("usr_paths");
			field.setAccessible(true);
			String[] paths = (String[]) field.get(null);
			for (int i = 0; i < paths.length; i++) {
				if (s.equals(paths[i])) {
					return;
				}
			}
			String[] tmp = new String[paths.length + 1];
			System.arraycopy(paths, 0, tmp, 0, paths.length);
			tmp[paths.length] = s;
			field.set(null, tmp);
			System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + s);
		} catch (IllegalAccessException e) {
			throw new IOException("Failed to get permissions to set library path");
		} catch (NoSuchFieldException e) {
			throw new IOException("Failed to get field handle to set library path");
		}
	}

}
