/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.deployment;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import au.edu.qut.yawl.exceptions.YPersistenceException;

public class DirectoryListener extends Thread {
	private String directory = null;

	private List<ServiceJar> listOfJars = new LinkedList<ServiceJar>();

	private ServiceBuilder servicebuilder = null;

	public static void main(String args[]) throws Exception {
		DirectoryListener l = new DirectoryListener("c:");
		l.initServiceDirectory();
		l.start();
	}

	public DirectoryListener(String directory) {
		this.directory = directory;
	}

	public DirectoryListener() {
	}
	
	public ServiceBuilder initServiceDirectory() throws YPersistenceException {
		System.out.println("D:" + directory);
		if (directory!=null ) {
			findJarsInDirectory(directory);
		} else {
			findJarsInDirectory(new File("./common/lib").getAbsolutePath());
			
			String classpath = System.getProperty("java.class.path"); 

			String separator = System.getProperty("path.separator");
			StringTokenizer classpathelements = new StringTokenizer(classpath,separator);
			while (classpathelements.hasMoreTokens()) {
				String elem = classpathelements.nextToken();
				File file = new File(elem);
				if (file.isFile()) {

					if (elem.endsWith("yawl.jar")) {
						try {
							System.out.println("found jar: " + elem);
							listOfJars.add(new ServiceJar(file.toURL(),file.lastModified()));												
						} catch (MalformedURLException e) {
							//todo: Proper handling of exception
							e.printStackTrace();
						}
					}
				} else {

					if (file.isDirectory()) {
						findJarsInDirectory(elem);
					}
				}
			}
		}
		
		servicebuilder = new ServiceBuilder(compileJarList(listOfJars));
		System.out.println("directory initialised");
		servicebuilder.buildServices();
		return servicebuilder;
	}

	public void findJarsInDirectory(String directory) {
		System.out.println("d:" + directory);
		File dir = new File(directory);

		File[] children = dir.listFiles();
		if (children == null) {
		} else {
			for (int i=0; i<children.length; i++) {
				try {
					URL filename = children[i].toURL();
					if (filename.toString().endsWith("yawl.jar")) {
						System.out.println("found jar in directory: " + filename);

						listOfJars.add(new ServiceJar(filename,children[i].lastModified()));
					}
				} catch (MalformedURLException e) {
					//todo: Proper handling of exception
					e.printStackTrace();
				}
			}
		}
	}

	public URL[] compileJarList(List<ServiceJar> servicejarlist) {
		URL[] jarlist = new URL[servicejarlist.size()]; 
		for (int j = 0; j < servicejarlist.size();j++) {
			jarlist[j] = servicejarlist.get(j).getFilelocation();
		}
		return jarlist;
	}

	public void run() {
		
		while (true) {
			boolean updated = false;
			if (directory!=null ) {
				File dir = new File(directory);

				File[] children = dir.listFiles();
				System.out.println("files: " + children.length);

				for (int i = 0; i < children.length;i++) {

					try {
						URL filename = children[i].toURL();
						if (filename.toString().endsWith(".jar")) {

							boolean found = false;

							for (int j = 0; j < listOfJars.size();j++) {
								System.out.println("looking for jars");

								System.out.println(children[i].toURL());
								System.out.println(listOfJars.get(j).getFilelocation());
								
								if (children[i].toURL().equals(listOfJars.get(j).getFilelocation())) {
									found = true;
									if  (children[i].lastModified() > listOfJars.get(j).getModified()) {
										listOfJars.get(j).setModified(children[i].lastModified());
										System.out.println("updated jar");
										updated = true;
										//has been updated, reload services
									}							
								} 				
							}

							if (!found) {
								System.out.println("new jar");
								//new jar, load services from jar
								listOfJars.add(new ServiceJar(children[i].toURL(),children[i].lastModified()));
								updated = true;
							}
						}
					} catch (MalformedURLException e) {
						//todo: Proper handling of exception
						e.printStackTrace();

					}


				}
				if (updated) {
					servicebuilder.setJars(compileJarList(listOfJars));
					servicebuilder.buildServices();
				}
			}
			try {				
				Thread.sleep(5000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static List<String> getClassesNames (String jarName){
		ArrayList<String> classes = new ArrayList<String>();

		try {
			JarFile jarFile = new JarFile(jarName);

			for(JarEntry entry : Collections.list(jarFile.entries())) {
				if(entry != null && !entry.isDirectory()) {
//					System.out.println("entry:" + entry.getName());
					if(entry.getName().contains("YAWLService") &&
							entry.getName().endsWith(".properties")) {
						Properties properties = new Properties();
						properties.load(jarFile.getInputStream(entry));
						String serviceClass = properties.getProperty("serviceClass");
						if(serviceClass != null && serviceClass.length() > 0) {
							classes.add(serviceClass);
						}
					}
				}
			}
		}
		catch(Exception e){
			e.printStackTrace ();
		}
		System.out.println(classes.size() + " classes in " + jarName);
		return classes;
	}
}
