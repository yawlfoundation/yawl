/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.util.List;

import operation.WorkflowOperation;
import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;

import com.nexusbpm.services.NexusServiceInfo;

/**
 * The CreateNexusComponent creates the necessary net variables,
 * task, default task mappings, and Nexus gateway to correctly 
 * call a Nexus service.
 * 
 * @author Matthew Sandoz
 * @author Nathan Rose
 */
public class CreateNexusComponentCommand extends AbstractCommand {
    private DataProxy<YNet> netProxy;
    
    private DataContext context;
    private DataProxyStateChangeListener listener;

	private String taskName;
	private String taskID;
	private NexusServiceInfo serviceInfo;

	private YAtomicTask task;
    private DataProxy<YAtomicTask> taskProxy;
    
    private YAWLServiceGateway gateway;
    private DataProxy<YAWLServiceGateway> gatewayProxy;
    
    private List<YVariable> netVariables;

	/**
	 * @param netNode
	 * @param taskName
	 * @param taskID
	 * @param serviceInfo
	 */
	public CreateNexusComponentCommand( DataProxy netProxy, String taskName, String taskID,
			NexusServiceInfo serviceInfo, DataProxyStateChangeListener listener ) {
		this.netProxy = netProxy;
		this.taskName = taskName;
		this.taskID = taskID;
		this.serviceInfo = serviceInfo;
        this.context = netProxy.getContext();
        this.listener = listener;
	}
    
	/**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        WorkflowOperation.attachDecompositionToSpec( gateway, netProxy.getData().getParent() );
        WorkflowOperation.attachNetElementToNet( task, netProxy.getData() );
        WorkflowOperation.attachVariablesToNet( netVariables, netProxy.getData() );
        DataProxy specProxy = context.getDataProxy( netProxy.getData().getParent() );
        context.attachProxy( gatewayProxy, gateway, specProxy );
        context.attachProxy( taskProxy, task, netProxy );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() throws Exception {
        WorkflowOperation.detachDecompositionFromSpec( gateway );
        WorkflowOperation.detachNetElementFromNet( task );
        WorkflowOperation.detachVariablesFromNet( netVariables );
        DataProxy specProxy = context.getDataProxy( netProxy.getData().getParent() );
        context.detachProxy( gatewayProxy, gateway, specProxy );
        context.detachProxy( taskProxy, task, netProxy );
    }

    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() throws Exception {
        taskID = WorkflowOperation.getAvailableNetElementID( netProxy.getData(), taskID );
        gateway = WorkflowOperation.createNexusGateway( taskID, netProxy.getData(), serviceInfo );
        task = WorkflowOperation.createNexusTask( taskID, taskName, netProxy.getData(), gateway, serviceInfo );
        netVariables = WorkflowOperation.createNexusVariables( taskID, netProxy.getData(), serviceInfo );
        
        gatewayProxy = context.createProxy( gateway, listener );
        taskProxy = context.createProxy( task, listener );
    }
    
//	/**
//	 * 
//	 * @todo move this to a test class...
//	 * @param parms
//	 */
//	public static void main(String[] parms) {
//		YSpecification spec = new YSpecification("specification.xml");
//		YNet net = new YNet("jython_network", spec);
//		net.setRootNet("true");
//
//		YInputCondition inputCondition = new YInputCondition("start", net);
//		inputCondition.setName("start");
//		net.setInputCondition(inputCondition);
//		YOutputCondition outputCondition = new YOutputCondition("end", net);
//		outputCondition.setName("end");
//		net.setOutputCondition(outputCondition);
//		spec.getDecompositions().add(net);
//
//		CreateNexusComponent jythonComponent = new CreateNexusComponent(
//				net, "quote of the day", "quote_of_the_day",
//				NexusServiceInfo.SERVICES[0]);
//		jythonComponent.execute();
//
//		CreateNexusComponent emailComponent = new CreateNexusComponent(
//				net, "email dean", "email_dean",
//				NexusServiceInfo.SERVICES[1]);
//		emailComponent.execute();
//
//		
//		YFlow flow = new YFlow(net.getInputCondition(),
//				jythonComponent.task);
//		net.getInputCondition().setPostset(flow);
//
//		flow = new YFlow(jythonComponent.task, emailComponent.task);
//		jythonComponent.task.setPostset(flow);
//
//		flow = new YFlow(emailComponent.task, net.getOutputCondition());
//		jythonComponent.task.setPostset(flow);
//
//		YVariable var1 = net.getLocalVariable("quote_of_the_day__output");
//		YVariable var2 = net.getLocalVariable("email_dean__body");
//		WorkflowOperation.remapInputVariable(var1, var2);
//		
//		SAXBuilder builder = new SAXBuilder();
//
//		try {
//			String specString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
//					+ "<specificationSet xmlns=\"http://www.yawl.fit.qut.edu.au/\"\r\n"
//					+ "	xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\r\n"
//					+ "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"Beta 7.1\"\r\n"
//					+ "	xsi:schemaLocation=\"http://www.yawl.fit.qut.edu.au/ YAWL_SchemaBeta7.1.xsd\">"
//					+ spec.toXML() + "</specificationSet>";
//			Document d = builder.build(new StringReader(specString));
//			new XMLOutputter(Format.getPrettyFormat()).output(d, System.out);
//		} catch (Exception e) {
//		}
//	}
}
