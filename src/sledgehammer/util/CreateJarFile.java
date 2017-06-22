package sledgehammer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipException;

public class CreateJarFile {

	public static int BUFFER_SIZE = 10240;

	public static void createJarArchive(File archiveFile, File[] directories, File[] additionalFiles) {
		try {
			byte buffer[] = new byte[BUFFER_SIZE];
			// Open archive file
			FileOutputStream stream = new FileOutputStream(archiveFile);
			JarOutputStream out = new JarOutputStream(stream, new Manifest());

			for(File directory : directories) {				
				File[] files = getFiles(directory, ".class");
				
				String split = directory.getName();
				
				for(File file : files) {
					String path = getJarPath(file, split);
					System.out.println("CraftBoid: Copied " + file.getName());
					
					
//				 Add archive entry
					JarEntry jarAdd = new JarEntry(split + "/" + path);
					jarAdd.setTime(file.lastModified());
					try {					
						out.putNextEntry(jarAdd);
					} catch(ZipException e) {
						continue;
					}
					
					// Write file to archive
					FileInputStream in = new FileInputStream(file);
					while (true) {
						int nRead = in.read(buffer, 0, buffer.length);
						if (nRead <= 0)
							break;
						out.write(buffer, 0, nRead);
					}
					in.close();
				}
			}
			
			for(File file : additionalFiles) {
				String path = file.getName();
				
				JarEntry jarAdd = new JarEntry(path);
				jarAdd.setTime(file.lastModified());
				try {					
					out.putNextEntry(jarAdd);
				} catch(ZipException e) {
					continue;
				}
				
				// Write file to archive
				FileInputStream in = new FileInputStream(file);
				while (true) {
					int nRead = in.read(buffer, 0, buffer.length);
					if (nRead <= 0)
						break;
					out.write(buffer, 0, nRead);
				}
				in.close();
			}

			out.close();
			stream.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Error: " + ex.getMessage());
		}
	}
	
	private static String getJarPath(File file, String directory) {
		
		String ext = file.getAbsolutePath();
		String[] split = ext.replace("\\", "/").split("/");
    	
    	int positionBegin = 0;
    	
    	for(int index = 0; index < split.length; index++) {
    		if(split[index].equalsIgnoreCase(directory)) {
    			positionBegin = index + 1;
    			break;
    		}
    	}
    	String classPath = "";
   
    	for(int index = positionBegin; index < split.length; index++) {
    		classPath += split[index] + "/";
    	}
    	
    	classPath = classPath.substring(0, classPath.length() - 1);
    	return classPath;
	}

	private static File[] getFiles(File directory, String extension) {

		List<File> listFiles = new ArrayList<File>();

		if (directory.exists()) {
			File[] files = directory.listFiles();

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
						for (File newFile : newFiles) {
							listFiles.add(newFile);
						}
					}
				} else {
					if (file.getName().endsWith(extension)) {
						listFiles.add(file);
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