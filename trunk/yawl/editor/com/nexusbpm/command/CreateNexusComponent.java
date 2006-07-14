/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;

import com.nexusbpm.services.NexusServiceInfo;
import com.nexusbpm.services.data.NexusServiceData;

/**
 * a few conventions
 * 
 * gateways named as: net.getid() + ".." + task.getid()
 * net vars named as: task.getid() + ".." + var.getName()
 *  
 * 
 */
public class CreateNexusComponent implements Command {

	/** constant used when concatenating ids to form a new id */
	private static final String SEPARATOR = "_";

	private static final String VARIABLE_TYPE_STRING = "string";

	private static final String STATUS = "Status";

	private static final String SERVICE_NAME = "ServiceName";

	private static final String LOCAL_INVOKER_URI = "http://localhost:8080/NexusServiceInvoker/";

	private static final String SCHEMA_URL = "http://www.w3.org/2001/XMLSchema";

	private YNet net;
	private String taskName;
	private String taskID;
	private NexusServiceInfo serviceInfo;
	
	private YAtomicTask task;
	
	public CreateNexusComponent(YNet net, String taskName, String taskID, NexusServiceInfo serviceInfo) {
		this.net = net;
		this.taskName = taskName;
		this.taskID = taskID;
		this.serviceInfo = serviceInfo;
	}
	
	public void execute() {
		createNetVariables();
		YAWLServiceGateway gateway = createGateway();
		net.getParent().getDecompositions().add(gateway);
		task = createTask(gateway);
		net.addNetElement(task);
	}

	public void undo() {
	}

	public YAWLServiceGateway createGateway() {
		YAWLServiceGateway retval = new YAWLServiceGateway(net.getId() + SEPARATOR + taskID, net.getParent());
		YAWLServiceReference yawlService = new YAWLServiceReference(LOCAL_INVOKER_URI, retval);
		retval.setYawlService(yawlService);
		for (int i = 0; i < serviceInfo.getVariableNames().length; i++) {
			YParameter iparam = new YParameter(net, YParameter._INPUT_PARAM_TYPE);
			YParameter oparam = new YParameter(net, YParameter._OUTPUT_PARAM_TYPE);
			iparam.setDataTypeAndName(VARIABLE_TYPE_STRING, serviceInfo.getVariableNames()[i],
					SCHEMA_URL);
			oparam.setDataTypeAndName(VARIABLE_TYPE_STRING, serviceInfo.getVariableNames()[i],
					SCHEMA_URL);
			retval.setInputParam(iparam);
			retval.setOutputParameter(oparam);
		}	
		YParameter iparam = new YParameter(net, YParameter._INPUT_PARAM_TYPE);
		YParameter oparam = new YParameter(net, YParameter._OUTPUT_PARAM_TYPE);
		iparam.setDataTypeAndName(VARIABLE_TYPE_STRING, SERVICE_NAME, SCHEMA_URL);
		oparam.setDataTypeAndName(VARIABLE_TYPE_STRING, STATUS, SCHEMA_URL);
		retval.setInputParam(iparam);
		retval.setOutputParameter(oparam);
		return retval;
	}
	
	public YAtomicTask createTask(YAWLServiceGateway gateway) {
		YAtomicTask retval = new YAtomicTask(taskID, YTask._AND, YTask._AND, net);
		retval.setName(taskName);
		retval.setDecompositionPrototype(gateway);
		for (int i = 0; i < serviceInfo.getVariableNames().length; i++) {
			String varName = serviceInfo.getVariableNames()[i];
			retval.setDataBindingForInputParam(createInputBindingString(varName, taskID + SEPARATOR + varName), varName);
			retval.setDataBindingForOutputExpression(createOutputBindingString(taskID + SEPARATOR + varName, varName), taskID + SEPARATOR + varName);
		}
		retval.setDataBindingForInputParam(createInputBindingString(SERVICE_NAME, taskID + SEPARATOR + SERVICE_NAME), SERVICE_NAME);
		retval.setDataBindingForOutputExpression(createOutputBindingString(taskID + SEPARATOR + STATUS, STATUS), taskID + SEPARATOR + STATUS);
		return retval;
	}

	public void createNetVariables() {
		List<YVariable> vars = net.getLocalVariables();
		YVariable var;
		NexusServiceData data = new NexusServiceData();
		for (int i = 0; i < serviceInfo.getVariableNames().length; i++) {
			data.setType( serviceInfo.getVariableNames()[i], serviceInfo.getVariableTypes()[i] );
			try {
				data.set( serviceInfo.getVariableNames()[i], serviceInfo.getInitialValues()[i] );
			} catch (IOException e) {
				e.printStackTrace();
			}
			vars.add(getStringVariable(net, taskID, serviceInfo.getVariableNames()[i], 
					data.getEncodedValue(serviceInfo.getVariableNames()[i])));
		}
		vars.add(getStringVariable(net, taskID, SERVICE_NAME, serviceInfo.getServiceName()));
		vars.add(getStringVariable(net, taskID, STATUS, null));
	}

	private YVariable getStringVariable(YNet net, String taskID, String varName, String initialValue) {
		YVariable var;
		var = new YVariable(net);
		var.setDataTypeAndName(VARIABLE_TYPE_STRING, taskID + SEPARATOR + varName, SCHEMA_URL);
		if (initialValue != null) var.setInitialValue(initialValue);
		return var;
	}

	private String createInputBindingString(String elementName, String variableName) {
		return "<" + elementName + ">" 
		+ "{" + "/" + net.getId() + "/" + variableName + "/text()" + "}" 
		+ "</" + elementName + ">" ;
	}

	private String createOutputBindingString(String elementName, String variableName) {
		return "<" + elementName + ">" 
		+ "{" + "/" + net.getId() + SEPARATOR + taskID + "/" + variableName + "/text()" + "}" 
		+ "</" + elementName + ">" ;
	}


	
	public static void main(String[] parms) {
		YSpecification spec = new YSpecification("specification.xml");
		YNet net = new YNet("jython_network", spec);
		net.setRootNet("true");
		
		YInputCondition inputCondition = new YInputCondition("start", net);
		inputCondition.setName("start");
		net.setInputCondition(inputCondition);
		YOutputCondition outputCondition = new YOutputCondition("end", net);
		outputCondition.setName("end");
		net.setOutputCondition(outputCondition);
				
		spec.getDecompositions().add(net);
		
		CreateNexusComponent createNexusComponent = new CreateNexusComponent(net, "quote of the day", "quote_of_the_day", NexusServiceInfo.SERVICES[0]);
		createNexusComponent.execute();
		
		YFlow flow = new YFlow(net.getInputCondition(), createNexusComponent.task);
		net.getInputCondition().setPostset(flow);

		flow = new YFlow(createNexusComponent.task, net.getOutputCondition());
		createNexusComponent.task.setPostset(flow);
		
		SAXBuilder builder = new SAXBuilder();
		
		try {
			String specString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
					"<specificationSet xmlns=\"http://www.yawl.fit.qut.edu.au/\"\r\n" + 
					"	xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\r\n" + 
					"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"Beta 7.1\"\r\n" + 
					"	xsi:schemaLocation=\"http://www.yawl.fit.qut.edu.au/ YAWL_SchemaBeta7.1.xsd\">" 
					+ spec.toXML() + "</specificationSet>";
			Document d = builder.build(new StringReader(specString));
			new XMLOutputter(Format.getPrettyFormat()).output(d, System.out);
		} catch (Exception e) {}
	}
	
}
