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
import java.util.List;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.interfce.InterfaceBInternalServiceController;

public class ServiceBuilder {

	List<InterfaceBInternalServiceController> services = new ArrayList<InterfaceBInternalServiceController>();

	URL[] jars = null;
	YAWLURLClassLoader loader = null;

	//some form of recursive iteration through the directory passed in
	//to find all jars beneath it (pretty standard java io) and create URL[]
	public ServiceBuilder(URL[] jars) {
		this.jars = jars;	
		loader = new YAWLURLClassLoader(jars);

	}

	public void setClasspath(URL[] classpath) {
		loader.addToClassPath(classpath);
	}
	
	public void setJars(URL[] jars) {
		this.jars = jars;
		loader = new YAWLURLClassLoader(jars);
	}
	
	public void buildServices() {
		services = new ArrayList<InterfaceBInternalServiceController>();
		for (int i = 0; i < jars.length;i++) {
			List<String> classes = DirectoryListener.getClassesNames(jars[i].getFile());
			List<InterfaceBInternalServiceController> l = loader.getServiceInstances(classes);
			services.addAll(l);
		
		}

		System.out.println("here:" + services.size());
		
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
			
			//services.get(i).invoke();
		}

		
	}

	public InterfaceBInternalServiceController getServiceInstance(String url) {
		
		for (int i = 0; i < services.size(); i++) {
			if (services.get(i).getServiceURI().equals(url)) {				
				return services.get(i);
			}
		}
		return null;
	}
	

}
