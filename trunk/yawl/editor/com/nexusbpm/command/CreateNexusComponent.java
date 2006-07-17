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

import operation.NexusWorkflow;
import operation.WorkflowOperation;

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
 * gateways named as: net.getid() + ".." + task.getid() net vars named as:
 * task.getid() + ".." + var.getName()
 * 
 * 
 */
public class CreateNexusComponent implements Command {

	/** constant used when concatenating ids to form a new id */

	private YNet net;

	private String taskName;

	private String taskID;

	private NexusServiceInfo serviceInfo;

	private YAtomicTask task;

	/**
	 * @param net
	 * @param taskName
	 * @param taskID
	 * @param serviceInfo
	 */
	public CreateNexusComponent(YNet net, String taskName, String taskID,
			NexusServiceInfo serviceInfo) {
		this.net = net;
		this.taskName = taskName;
		this.taskID = taskID;
		this.serviceInfo = serviceInfo;
	}

	public void execute() {
		WorkflowOperation.createNetVariables(taskID, net, serviceInfo);
		YAWLServiceGateway gateway = WorkflowOperation.createGateway(taskID, net, serviceInfo);
		net.getParent().getDecompositions().add(gateway);
		task = WorkflowOperation.createTask(taskID, taskName, net, gateway, serviceInfo);
		net.addNetElement(task);
	}

	public void undo() {
		throw new UnsupportedOperationException(
				"nexus insert undo not yet implemented");
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

		CreateNexusComponent createNexusComponent = new CreateNexusComponent(
				net, "quote of the day", "quote_of_the_day",
				NexusServiceInfo.SERVICES[0]);
		createNexusComponent.execute();

		YFlow flow = new YFlow(net.getInputCondition(),
				createNexusComponent.task);
		net.getInputCondition().setPostset(flow);

		flow = new YFlow(createNexusComponent.task, net.getOutputCondition());
		createNexusComponent.task.setPostset(flow);

		SAXBuilder builder = new SAXBuilder();

		try {
			String specString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
					+ "<specificationSet xmlns=\"http://www.yawl.fit.qut.edu.au/\"\r\n"
					+ "	xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\r\n"
					+ "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"Beta 7.1\"\r\n"
					+ "	xsi:schemaLocation=\"http://www.yawl.fit.qut.edu.au/ YAWL_SchemaBeta7.1.xsd\">"
					+ spec.toXML() + "</specificationSet>";
			Document d = builder.build(new StringReader(specString));
			new XMLOutputter(Format.getPrettyFormat()).output(d, System.out);
		} catch (Exception e) {
		}
	}

}
