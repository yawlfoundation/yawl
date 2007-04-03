/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.deployment;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

import au.edu.qut.yawl.engine.interfce.InterfaceBInternalServiceController;

public class YAWLURLClassLoader extends URLClassLoader {
	public YAWLURLClassLoader(URL[] jars) {
		super(jars);
	}
	
	public void addToClassPath(URL[] jar) {
		for (int i = 0; i < jar.length; i++) {
			this.addURL(jar[i]);
		}
	}
	
	public Class findClass(String s) {
		Class c = null;
		try {
			c = Class.forName(s);
			return c;
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			c = super.findClass(s);		
			return c;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public List<InterfaceBInternalServiceController> getServiceInstances(List<String> classnames) {
		List l = new LinkedList();
		for (int i = 0; i < classnames.size(); i++) {
			try {
				Class c = findClass(classnames.get(i).substring(0,classnames.get(i).indexOf(".class")));
				Class superclass = c.getSuperclass();

				while(superclass != null && !superclass.equals(Object.class)) {
					if(superclass.getName().equals("au.edu.qut.yawl.engine.interfce.InterfaceBInternalServiceController")) {
						System.out.println("Found a service " + c.getName());
						InterfaceBInternalServiceController s = (InterfaceBInternalServiceController) c.newInstance();
						l.add(s);
					}
					superclass = superclass.getSuperclass();
				}
				
				/*
				 * We will ignore all errors simply because if the
				 * JAR file does not work, and classes are not in the
				 * classpath we do not load the file. The errors
				 * should be logged and displayed to users though
				 * */
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (NoClassDefFoundError e) {
				e.printStackTrace();
			} catch (java.lang.LinkageError e) {
				e.printStackTrace();
			}
		}

		return l;
	}
}
