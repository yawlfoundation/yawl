/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.deployment;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class DirectoryListener extends Thread {

	private String directory = null;

	private List<ServiceJar> listOfJars = new LinkedList<ServiceJar>();
	private List<URL> classpath = new LinkedList<URL>();

	private ServiceBuilder servicebuilder = null;

	public static void main(String args[]) {
		DirectoryListener l = new DirectoryListener("c:");
		l.initServiceDirectory();
		l.start();
	}

	//check for updates
	//URL[] jars = findAllJarsIn(directory);

	public DirectoryListener(String directory) {

		this.directory = directory;


	}

	public DirectoryListener() {

	}
	
	public ServiceBuilder initServiceDirectory() {

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
		servicebuilder.setClasspath(classpath.toArray(new URL[0]));
		System.out.println("directory initialised");
		servicebuilder.buildServices();
		return servicebuilder;
	}

	public void findJarsInDirectory(String directory) {
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
					} else {

						classpath.add(children[i].toURL());

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

	public static List getClassesNames (String jarName){
		ArrayList classes = new ArrayList ();

		try{
			JarInputStream jarFile = new JarInputStream
			(new FileInputStream (jarName));
			JarEntry jarEntry;

			while(true) {
				jarEntry=jarFile.getNextJarEntry ();
				if(jarEntry == null){
					break;
				}
				if(jarEntry.getName ().endsWith (".class"))  {
					classes.add (jarEntry.getName().replaceAll("/", "\\."));
				}
			}
		}
		catch( Exception e){
			e.printStackTrace ();
		}
		System.out.println(classes.size());
		return classes;
	}

}
