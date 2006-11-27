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
		boolean found = false;
		for (int i = 0; i < classnames.size(); i++) {
			try {

				Class c = findClass(classnames.get(i).substring(0,classnames.get(i).indexOf(".class")));

				//Class c = findClass("jar:file:/c://jaxen-jdom.jar!/"+classnames.get(i));
				Class superclass = c.getSuperclass();

				if (superclass!=null && superclass.getName()=="au.edu.qut.yawl.engine.interfce.InterfaceBInternalServiceController") {
					System.out.println("Found a service " + c.getName());
					found = true;
					InterfaceBInternalServiceController s = (InterfaceBInternalServiceController) c.newInstance();
					s.setServiceURI("internal://"+c.getName());						
					l.add(s);
				}
				
				/*
				 * We will ignore all errors simply because if the
				 * JAR file does not work, and classes are not in the
				 * classpath we do not load the file. The errors
				 * should be logged and displayed to users though
				 * */
			
			} catch (IllegalAccessException e) {
				//if (found)
					e.printStackTrace();
				found = false;



			} catch (InstantiationException e) {
				//if (found)
					e.printStackTrace();
				found = false;

			} catch (NoClassDefFoundError e) {
				//if (found)
					e.printStackTrace();
				found = false;


				
			} catch (java.lang.LinkageError e) {
//				if (found)
					e.printStackTrace();
				found = false;

				
			}
		}

		return l;
	}
	
}
