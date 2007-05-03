/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.deployment;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.interfce.EngineGateway;
import au.edu.qut.yawl.engine.interfce.EngineGatewayImpl;
import au.edu.qut.yawl.engine.interfce.InterfaceBInternalServiceController;
import au.edu.qut.yawl.exceptions.YPersistenceException;

public class ServiceBuilder {
	List<InterfaceBInternalServiceController> services = new ArrayList<InterfaceBInternalServiceController>();

	URL[] jars = null;
	private EngineGateway engineGateway;

	//some form of recursive iteration through the directory passed in
	//to find all jars beneath it (pretty standard java io) and create URL[]
	public ServiceBuilder(URL[] jars) throws YPersistenceException {
		this.jars = jars;
		engineGateway = new EngineGatewayImpl(true);
	}
	
	public void setJars(URL[] jars) {
		this.jars = jars;
	}
	
	public void buildServices() {
		services = new ArrayList<InterfaceBInternalServiceController>();
		for (int i = 0; i < jars.length;i++) {
			List<String> classes = DirectoryListener.getClassesNames(jars[i].getFile());
			List<InterfaceBInternalServiceController> serviceClasses = getServiceInstances(classes);
			for(InterfaceBInternalServiceController service : serviceClasses) {
				service.setEngineGateway(engineGateway);
				services.add(service);
			}
		}

		System.out.println("ServiceBuilder found " + services.size() + " services.");
		
		for (int i = 0; i < services.size(); i++) {
			YAWLServiceReference yawlService = new YAWLServiceReference();
			yawlService.setEnabled(true);
			yawlService.setYawlServiceID(services.get(i).getServiceURI());
			yawlService.setDocumentation(services.get(i).getDocumentation());
			try {
				EngineFactory.getExistingEngine().addYawlService(yawlService);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<InterfaceBInternalServiceController> getServices() {
		return Collections.unmodifiableList(services);
	}

	public InterfaceBInternalServiceController getServiceInstance(String url) {
		for (int i = 0; i < services.size(); i++) {
			if (services.get(i).getServiceURI().equals(url)) {
				return services.get(i);
			}
		}
		return null;
	}
	
	public List getServiceInstances(List<String> classnames) {
		ClassLoader loader = getClass().getClassLoader();
		List l = new LinkedList();
		for (int i = 0; i < classnames.size(); i++) {
			try {
				System.out.println("Looking for service: " + classnames.get(i));
				Class c = loader.loadClass(classnames.get(i));
				Class superclass = c.getSuperclass();
				
				while(superclass != null) {
					if(superclass.getName().equals(InterfaceBInternalServiceController.class.getName())) {
						Object service = c.newInstance();
						System.out.println("Found a service " + c.getName());
						l.add(service);
						break;
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
