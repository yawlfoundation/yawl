/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.deployment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jdom.Element;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.interfce.EngineGateway;
import au.edu.qut.yawl.engine.interfce.EngineGatewayImpl;
import au.edu.qut.yawl.engine.interfce.InterfaceBInternalServiceController;
import au.edu.qut.yawl.exceptions.YAWLException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.worklist.model.SpecificationData;
import au.edu.qut.yawl.worklist.model.TaskInformation;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;

public class ServiceBuilder {
	List<InterfaceBInternalServiceController> services = new ArrayList<InterfaceBInternalServiceController>();

	URL[] jars = null;
	YAWLURLClassLoader loader = null;
	private EngineGateway engineGateway;

	//some form of recursive iteration through the directory passed in
	//to find all jars beneath it (pretty standard java io) and create URL[]
	public ServiceBuilder(URL[] jars) throws YPersistenceException {
		this.jars = jars;	
		loader = new YAWLURLClassLoader(jars, getClass().getClassLoader());
		engineGateway = new EngineGatewayImpl(true);
	}

	public void setClasspath(URL[] classpath) {
		loader.addToClassPath(classpath);
	}
	
	public void setJars(URL[] jars) {
		this.jars = jars;
		loader = new YAWLURLClassLoader(jars, getClass().getClassLoader());
	}
	
	public void buildServices() {
		services = new ArrayList<InterfaceBInternalServiceController>();
		for (int i = 0; i < jars.length;i++) {
			List<String> classes = DirectoryListener.getClassesNames(jars[i].getFile());
			List list = loader.getServiceInstances(classes);
			for(Object service : list) {
				services.add(new InternalServiceWrapper(service, engineGateway));
			}
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
	
	private static class InternalServiceWrapper extends InterfaceBInternalServiceController {
		private Object service;
		public InternalServiceWrapper(Object service, EngineGateway gateway) {
			this.service = service;
			invoke(
					getMethod("setEngineGateway", new Class[] {Object.class}),
					gateway);
		}
		
		/**
    	 * Utility function that provides a common error message when a method isn't found.
    	 */
    	private Method getMethod(String name, Class[] parameterTypes) {
    		Method method = null;
    		Exception e = null;
			try {
				method = service.getClass().getMethod(name, parameterTypes);
			} catch(SecurityException ex) {
				e = ex;
			} catch(NoSuchMethodException ex) {
				e = ex;
			}
    		if(method == null) {
    			String params = "";
    			if(parameterTypes != null) {
    				for(int index = 0; index < parameterTypes.length; index++) {
    					if(params.length() > 0)
    						params += ", ";
    					params += parameterTypes[index].getSimpleName();
    				}
    			}
    			throw new RuntimeException("Error: Improper service! Method " + name + "(" + params + ") not available!", e);
    		}
    		return method;
    	}
    	
    	/**
    	 * Utility function that provides a common error message when a method throws an exception.
    	 */
    	private Object invoke(Method method, Object... arguments) {
    		Exception e = null;
    		try {
				return method.invoke(service, arguments);
			} catch(IllegalArgumentException ex) {
				e = ex;
			} catch(IllegalAccessException ex) {
				e = ex;
			} catch(InvocationTargetException ex) {
				e = ex;
			}
			String argString = "";
			if(arguments != null) {
				for(int index = 0; index < arguments.length; index++) {
					if(argString.length() > 0)
						argString += ", ";
					argString += arguments[index];
				}
			}
			throw new RuntimeException("Error invoking method " + method.toGenericString()
					+ " with arguments " + argString, e);
    	}
		
		@Override
		public String checkInWorkItem(String workItemID, Element inputData, Element outputData) throws YAWLException {
			throw new UnsupportedOperationException("Cannot call checkInWorkItem on a wrapped service!");
		}

		@Override
		public String checkInWorkItem(String workItemID, String inputData, String outputData) throws YAWLException {
			throw new UnsupportedOperationException("Cannot call checkInWorkItem on a wrapped service!");
		}

		@Override
		public WorkItemRecord checkOut(String workItemID) throws YAWLException {
			throw new UnsupportedOperationException("Cannot call checkOut on a wrapped service!");
		}

		@Override
		protected List<WorkItemRecord> checkOutAllInstancesOfThisTask(WorkItemRecord enabledWorkItem) throws YAWLException {
			throw new UnsupportedOperationException("Cannot call checkOutAllInstancesOfThisTask on a wrapped service!");
		}

		@Override
		public YParameter[] describeRequiredParams() {
			// XXX fix (returns array of length 1 for now...)
			return super.describeRequiredParams();
		}

		@Override
		public WorkItemRecord getCachedWorkItem(String workItemID) {
			throw new UnsupportedOperationException("Cannot call getCachedWorkItem on a wrapped service!");
		}

		@Override
		public List<WorkItemRecord> getChildren(String workItemID) throws YAWLException {
			throw new UnsupportedOperationException("Cannot call getChildren on a wrapped service!");
		}

		@Override
		public String getDocumentation() {
			return (String) invoke(
					getMethod("getDocumentation", null)
					);
		}

		@Override
		public WorkItemRecord getEngineStoredWorkItem(String workItemID) throws YAWLException {
			throw new UnsupportedOperationException("Cannot call getEngineStoredWorkItem on a wrapped service!");
		}

		@Override
		public String getServiceName() {
			return (String) invoke(
					getMethod("getServiceName", null)
					);
		}

		@Override
		public SpecificationData getSpecificationData(String specID) throws YAWLException {
			throw new UnsupportedOperationException("Cannot call getSpecificationData on a wrapped service!");
		}

		@Override
		public List<SpecificationData> getSpecificationPrototypesList() throws YAWLException {
			throw new UnsupportedOperationException("Cannot call getSpecificationPrototypesList on a wrapped service!");
		}

		@Override
		public TaskInformation getTaskInformation(String specificationID, String taskID) throws YAWLException {
			throw new UnsupportedOperationException("Cannot call getTaskInformation on a wrapped service!");
		}

		@Override
		public WorkItemRecord getWorkItem(String workItemID) throws YAWLException {
			throw new UnsupportedOperationException("Cannot call getWorkItem on a wrapped service!");
		}

		@Override
		public void handleCancelledWorkItemEvent(String workItemRecord) {
			invoke(
					getMethod("handleCancelledWorkItemEvent", new Class[] {String.class}),
					workItemRecord);
		}

		@Override
		public void handleCompleteCaseEvent(String caseID, String casedata) {
			invoke(
					getMethod("handleCompleteCaseEvent", new Class[] {String.class, String.class}),
					caseID,
					casedata);
		}

		@Override
		public void handleEnabledWorkItemEvent(String workItemRecord) {
			invoke(
					getMethod("handleEnabledWorkItemEvent", new Class[] {String.class}),
					workItemRecord);
		}

		@Override
		protected Element prepareReplyRootElement(WorkItemRecord enabledWorkItem) throws YAWLException {
			throw new UnsupportedOperationException("Cannot call prepareReplyRootElement on a wrapped service!");
		}
	}
}
