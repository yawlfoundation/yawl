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

@Deprecated
public class YAWLURLClassLoader extends URLClassLoader {
	private ClassLoader parent;
	public YAWLURLClassLoader(URL[] jars, ClassLoader parent) {
		super(jars, null);
		this.parent = parent;
	}
	
	public void addToClassPath(URL[] jar) {
		for (int i = 0; i < jar.length; i++) {
			this.addURL(jar[i]);
		}
	}
	
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		// First, check if the class has already been loaded
		Class c = findLoadedClass(name);
		if(c == null) {
			try {
				if(name.startsWith("java.") || name.startsWith("sun.")) {
					c = getSystemClassLoader().loadClass(name);
				} else {
					c = parent.loadClass(name);
				}
			} catch(ClassNotFoundException e) {
				// If still not found, then invoke findClass in order
				// to find the class.
				c = findClass(name);
			}
		}
		if(resolve) {
			resolveClass(c);
		}
		return c;
	}
	
	public List getServiceInstances(List<String> classnames) {
		List l = new LinkedList();
		for (int i = 0; i < classnames.size(); i++) {
			try {
				System.out.println(classnames.get(i));
//				Class c = Class.forName(classnames.get(i), true, this);
//				Class c = findClass(classnames.get(i));
				Class c = loadClass(classnames.get(i), true);
				Class superclass = c.getSuperclass();
				
				while(superclass != null) {
					if(superclass.getName().equals(InterfaceBInternalServiceController.class.getName())) {
						System.out.println("Found a service " + c.getName());
						Object service = c.newInstance();
						if(service != null) {
							l.add(service);
						} else {
							System.out.println("error instantiating " + c.getCanonicalName());
						}
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
			} catch(ClassNotFoundException e) {
				e.printStackTrace();
			} catch(NullPointerException e) {
				e.printStackTrace();
			}
		}

		return l;
	}
}
