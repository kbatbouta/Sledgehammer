package sledgehammer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.jar.JarFile;

import sledgehammer.util.CreateJarFile;

public class Agent {
	
	public static void premain(String args, Instrumentation inst) {
		
		new File("natives/").mkdirs();
		new File("saves/").mkdirs();
		new File("plugins/").mkdirs();
		new File("settings/").mkdirs();
		new File("steamapps/").mkdirs();
		
		File craftboid = new File("natives/CraftBoid.jar");
		
		Settings.getInstance().readSettings();
		String pzDirectory = Settings.getInstance().getPZDirectory();
		
		
		if(pzDirectory == null || pzDirectory.isEmpty()) {
			String input = null;
			Scanner scanner = new Scanner(System.in); 
			while(pzDirectory == null) {				
				System.out.println("Please enter the directory for the Project Zomboid Dedicated Server:");
				input = scanner.nextLine();
				File directory = new File(input);
				if(directory.exists() && directory.isDirectory()) {
					File zombieDirectory = new File(input + File.separator + "java" + File.separator + "zombie");
					if(zombieDirectory.exists() && zombieDirectory.isDirectory()) {						
						pzDirectory = input;
					} else {
						System.out.println("This is a directory, but it does not contain Project Zomboid files.");
					}
				} else {
					System.out.println("This is not a valid directory.");
				}
			}
		}

		pzDirectory = pzDirectory.replace("\\", "/");
		if(pzDirectory.endsWith("/")) {
			pzDirectory = pzDirectory.substring(0, pzDirectory.length() - 1);
		}
		
		System.out.println("CraftBoid: PZDirectory: \"" + pzDirectory + "\"");
		
		String _classDir = pzDirectory + "/java";
		String _nativeDir = pzDirectory + "/natives";
		
		File[] classDirectories = new File[] {				
				new File(_classDir + "/com"),
				new File(_classDir + "/de"),
				new File(_classDir + "/fmod"),
				new File(_classDir + "/javax"),
				new File(_classDir + "/org"),
				new File(_classDir + "/se"),
				new File(_classDir + "/zombie"),
		};
		
		File[] additionalFiles = new File[] {
			new File(_nativeDir + "/RakNet32.dll"),
			new File(_nativeDir + "/RakNet64.dll"),
			new File(_nativeDir + "/ZNetJNI32.dll"),
			new File(_nativeDir + "/ZNetJNI64.dll"),
			new File(_nativeDir + "/ZNetNoSteam32.dll"),
			new File(_nativeDir + "/ZNetNoSteam64.dll"),
			new File(pzDirectory + "/steam_api.dll"),
			new File(pzDirectory + "/steam_api64.dll"),
			new File(pzDirectory + "/steamclient.dll"),
			new File(pzDirectory + "/steamclient64.dll"),
			new File(pzDirectory + "/tier0_s.dll"),
			new File(pzDirectory + "/tier0_s64.dll"),
			new File(pzDirectory + "/vstdlib_s.dll"),
			new File(pzDirectory + "/vstdlib_s64.dll"),
			
			// JARS
			new File(_classDir + "/jinput.jar"),	
			new File(_classDir + "/lwjgl.jar"),
			new File(_classDir + "/lwjgl_util.jar"),
			new File(_classDir + "/sqlite-jdbc-3.8.10.1.jar"),
			new File(_classDir + "/uncommons-maths-1.2.3.jar")
		};
		
		if(!craftboid.exists()) {			
			CreateJarFile.createJarArchive(craftboid, classDirectories, new File[] {});
		}
		
		try {
			inst.appendToSystemClassLoaderSearch(new JarFile(craftboid));
		} catch (IOException e) { 
			e.printStackTrace();
		}
		
		for(File file : additionalFiles) {
			File dest = new File("natives/" + file.getName());
			try {
				Files.copy(file.toPath(), dest.toPath());
			} catch(FileAlreadyExistsException e) {
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			if(dest.getName().endsWith("jar")) {
				System.out.println("Craftboid: Loading library: " + dest.getName());
				try {
					inst.appendToSystemClassLoaderSearch(new JarFile(dest));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		File from, dest;
		
		String[] filesToCopy = new String[] {
				pzDirectory + "/stdlib.lbc",
				pzDirectory + "/stdlib.lua",
				pzDirectory + "/serialize.lua",
				pzDirectory + "/steam_appid.txt", 
				pzDirectory + "/media"
		};
		
		try {
			for(String file : filesToCopy) {
				from = new File(file);
				dest = new File(from.getName());
				
				if(from.isFile()) {					
					boolean copied = false;
					try {					
						Files.copy(from.toPath(), dest.toPath());
						copied = true;
					} catch(FileAlreadyExistsException e) {
						
					}
					if(copied) {						
						System.out.println("Craftboid: Copied " + file + "...");
					}
				} else {
					copyFolder(from, dest, pzDirectory);
				}
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Settings.getInstance().setPZDirectory(pzDirectory);
		Settings.getInstance().save();
		
	}
	
	public static void copyFolder(File src, File dest, String slice)
	    	throws IOException{

    	if(src.isDirectory()){

    		//if directory not exists, create it
    		if(!dest.exists()){
    		   dest.mkdir();
    		   System.out.println("Craftboid: Copied " + "../" + dest.getName());
    		}

    		//list all the directory contents
    		String files[] = src.list();

    		for (String file : files) {
    		   //construct the src and dest file structure
    		   File srcFile = new File(src, file);
    		   File destFile = new File(dest, file);
    		   //recursive copy
    		   copyFolder(srcFile,destFile, slice);
    		}

    	}else{
    		
    		if(!dest.exists() || dest.length() != src.length()) {
    		//if file, then copy it
    		//Use bytes stream to support all file types
    		InputStream in = new FileInputStream(src);
    	        OutputStream out = new FileOutputStream(dest);

    	        byte[] buffer = new byte[1024];

    	        int length;
    	        //copy the file content in bytes
    	        while ((length = in.read(buffer)) > 0){
    	    	   out.write(buffer, 0, length);
    	        }

    	        in.close();
    	        out.close();
    	        System.out.println("Craftboid: Copied " + "../" + dest.getName());
      		}
    	}
    }
}
