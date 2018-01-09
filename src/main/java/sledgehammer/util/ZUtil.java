/*
This file is part of Sledgehammer.

   Sledgehammer is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Sledgehammer is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with Sledgehammer. If not, see <http://www.gnu.org/licenses/>.
 */
package sledgehammer.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.google.gson.Gson;

import sledgehammer.SledgeHammer;

/**
 * TODO: Document.
 *
 * @author Jab
 */
public class ZUtil {

    /**
     * The location for plug-ins, as a String.
     */
    public static String pluginLocation = "plugins" + File.separator;
    /**
     * The location for plug-ins, as a File.
     */
    public static File pluginFolder = new File(ZUtil.pluginLocation);
    public static Random random = new RandomXS128();
    private static Gson gson = new Gson();

    public static boolean isClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

    public static Gson getGson() {
        return gson;
    }

    /**
     * @return Returns a String representation of the current time.
     */
    public static String getHourMinuteSeconds() {
        String minutes = Calendar.getInstance().get(Calendar.MINUTE) + "";
        if (Calendar.getInstance().get(Calendar.MINUTE) < 10) {
            minutes = "0" + minutes;
        }
        String seconds = Calendar.getInstance().get(Calendar.SECOND) + "";
        if (Calendar.getInstance().get(Calendar.SECOND) < 10) {
            seconds = "0" + seconds;
        }
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" + minutes + ":" + seconds;
    }

    @SuppressWarnings("rawtypes")
    public static void compactList(List list) {
        List<Integer> listIndexesToRemove = new ArrayList<>();
        Map<Object, Boolean> cacheMap = new HashMap<>();
        for (int index = 0; index < list.size(); index++) {
            Object o = list.get(index);
            Boolean cached = cacheMap.get(o);
            if (cached == null) {
                cacheMap.put(o, true);
            } else {
                listIndexesToRemove.add(index);
            }
        }
        synchronized (list) {
            try {
                for (int index : listIndexesToRemove)
                    list.remove(index);
            } catch (IndexOutOfBoundsException e) {
                // Catches any asynchronous concurrent modifications.
            }
        }
    }

    /**
     * DirectByteBuffers are garbage collected by using a phantom reference and a
     * reference queue. Every once a while, the JVM checks the reference queue and
     * cleans the DirectByteBuffers. However, as this doesn't happen immediately
     * after discarding all references to a DirectByteBuffer, it's easy to
     * OutOfMemoryError yourself using DirectByteBuffers. This function explicitly
     * calls the Cleaner method of a DirectByteBuffer.
     *
     * @param toBeDestroyed The DirectByteBuffer that will be "cleaned". Utilizes reflection.
     * @throws IllegalAccessException    Thrown if there's a failure to access the reflected Methods.
     * @throws InvocationTargetException Thrown if there's a failure to invoke the reflected Methods.
     * @throws NoSuchMethodException     Thrown if the reflected Methods do not exist.
     */
    public static void destroyDirectByteBuffer(ByteBuffer toBeDestroyed) throws
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
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
            for (String path : paths) {
                if (s.equals(path)) {
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

    public static long getCurrentTimeStamp() {
        return (new Date()).getTime() / 1000L;
    }

    public static long currentTimeStamp() {
        return (System.currentTimeMillis() / 1000);
    }

    public static File[] getFiles(File directory, String extension) {
        List<File> listFiles = new ArrayList<>();
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        // Recursive cannot iterate over parent directories.
                        if (file.getName().equals(".") || file.getName().equals("..") || file.getName().equals("...")) {
                            continue;
                        }
                        // Not a part of PZ.
                        if (file.getName().equalsIgnoreCase("rcon")) {
                            continue;
                        }
                        File[] newFiles = getFiles(file, extension);
                        if (newFiles.length > 0) {
                            Collections.addAll(listFiles, newFiles);
                        }
                    } else {
                        if (file.getName().endsWith(extension)) {
                            listFiles.add(file);
                        }
                    }
                }
            }
        }
        File[] array = new File[listFiles.size()];
        for (int index = 0; index < listFiles.size(); index++) {
            array[index] = listFiles.get(index);
        }
        return array;
    }
}