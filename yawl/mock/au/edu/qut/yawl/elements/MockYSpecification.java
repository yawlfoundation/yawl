/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.elements;

import au.edu.qut.yawl.elements.data.YInputParameter;
import au.edu.qut.yawl.elements.data.YOutputParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import com.nexusbpm.editor.component.EmailSenderComponent;

/**
 * The purpose of theMockYSpecification is to provide an example of how to build
 * a robust specification that will be compatible with capsela-style variables
 * and to provide a ready-made specification to use in testing.
 * 
 * @author Matthew Sandoz
 * 
 */
public class MockYSpecification {
	private static final String SCHEMA_URL = "http://www.w3.org/2001/XMLSchema-instance";

	public static YSpecification instance;

	private static final String[] jythonProps = new String[] { "command",
			"output", "error" };

	private static final String[] jythonVals = new String[] {
			"print 3.141592653589793", "systemout", "errorout" };

	private static final String[] mailProps = new String[] { "fromAddress",
			"toAddresses", "subject", "body", "markupAttachment" };

	private static final String[] mailVals = new String[] {
			"matthew.sandoz@ichg.com", "dean.mao@ichg.com",
			"Did you get that thing I sent you?",
			"This is the message body we expect to deliver.",
			"Not sure what this attachment is for" };

	private static YAWLServiceGateway createEmailGateway(
			YSpecification specification) {
		YAWLServiceGateway gate = new YAWLServiceGateway(
				EmailSenderComponent.class.getName(), specification);
		gate.setName("[send mail support]");
		for (String prop : mailProps) {
			YInputParameter gatewayVar = new YInputParameter(gate,
					YInputParameter._INPUT_PARAM_TYPE);
			gatewayVar.setDataTypeAndName("string", prop, SCHEMA_URL);
			gate.setInputParam(gatewayVar);
			YOutputParameter gwVar2 = new YOutputParameter(gate,
					YOutputParameter._OUTPUT_PARAM_TYPE);
			gwVar2.setDataTypeAndName("string", prop, SCHEMA_URL);
			gate.setOutputParameter(gwVar2);
		}
		specification.setDecomposition(gate);
		return gate;
	}

	private static YAtomicTask createEmailTask(YAWLServiceGateway gate, YNet net) {
		YAtomicTask task = new YAtomicTask("email dean", YAtomicTask._AND,
				YAtomicTask._AND, net);
		task.setName("email dean");
		generateTaskVariables(net, gate, task, mailProps, mailVals);

		net.addNetElement(task);
		return task;
	}

	private static YAWLServiceGateway createJythonGateway(
			YSpecification specification) {
		YAWLServiceGateway gate = new YAWLServiceGateway(
				"com.ichg.capsela.domain.component.JythonComponent",
				specification);
		gate.setName("[jython scripting support]");

		YAWLServiceReference yawlServiceReference = new YAWLServiceReference();
		yawlServiceReference
				.setYawlServiceID("http://localhost:8080/yawlWSInvoker/");
		yawlServiceReference.setYawlServiceGateway(gate);
		gate.setYawlService(yawlServiceReference);

		YInputParameter wsdlLocation = new YInputParameter(gate,
				YInputParameter._INPUT_PARAM_TYPE);
		wsdlLocation.setDataTypeAndName("string", "wsdlLocation", SCHEMA_URL);
		wsdlLocation
				.setInitialValue("http://localhost:8080/JythonService/services/JythonService?wsdl");
		gate.setInputParam(wsdlLocation);

		YInputParameter operationName = new YInputParameter(gate,
				YInputParameter._INPUT_PARAM_TYPE);
		operationName.setDataTypeAndName("string", "operationName", SCHEMA_URL);
		operationName.setInitialValue("execute");
		gate.setInputParam(operationName);
		for (String prop : jythonProps) {
			YInputParameter gatewayVar = new YInputParameter(gate,
					YInputParameter._INPUT_PARAM_TYPE);
			gatewayVar.setDataTypeAndName("string", prop, SCHEMA_URL);
			gate.setInputParam(gatewayVar);
			YOutputParameter gwVar2 = new YOutputParameter(gate,
					YOutputParameter._OUTPUT_PARAM_TYPE);
			gwVar2.setDataTypeAndName("string", prop, SCHEMA_URL);
			gate.setOutputParameter(gwVar2);
		}

		specification.setDecomposition(gate);
		return gate;
	}

	private static YAtomicTask createJythonTask(YAWLServiceGateway gate,
			YNet net) {
		YAtomicTask task = new YAtomicTask("quote of the day",
				YAtomicTask._AND, YAtomicTask._AND, net);
		task.setName("quote of the day");
		task.setDecompositionPrototype(gate);
		generateTaskVariables(net, gate, task, jythonProps, jythonVals);
		net.addNetElement(task);
		return task;
	}

	private static YNet createRootNet(YSpecification specification) {
		YNet net = new YNet("My test net", specification);
		net.setRootNet("true");
		specification.setRootNet(net);
		return net;
	}

	private static void generateTaskVariables(YNet net,
			YAWLServiceGateway gate, YAtomicTask task, String[] props,
			String[] vals) {
		String taskPath = task.getID() + ".";
		String path = "/" + net.getId() + "/" + taskPath;
		String gatewayPath = "/" + gate.getId() + "/";
		task.setDecompositionPrototype(gate);
		for (String prop : props) {
			task.setDataBindingForInputParam(path + prop, prop);
			task.setDataBindingForOutputExpression(gatewayPath + prop, taskPath
					+ prop);
		}
		for (int i = 0; i < props.length; i++) {
			YVariable var = new YVariable(net);
			var.setDataTypeAndName("string", task.getID() + "." + props[i],
					SCHEMA_URL);
			var.setInitialValue(vals[i]);
			net.setLocalVariable(var);
		}
	}

	public static YSpecification getNewSpecification() {
		YSpecification spec = new YSpecification(
				"virtual://memory/home/sandozm/templates/testing/testspec.xml");
		initSpec(spec);
		return spec;
	}

	public static YSpecification getSpecification() {
		if (instance == null) {
			instance = getNewSpecification();
		}
		return instance;
	}

	private synchronized static void initSpec(YSpecification specification) {
		specification.setName("My test specification.xml");
		specification.setBetaVersion(YSpecification._Beta7_1);
		specification.setMetaData(new YMetaData());

		YAWLServiceGateway gate = createEmailGateway(specification);
		YAWLServiceGateway gate2 = createJythonGateway(specification);

		YNet net = createRootNet(specification);

		net.setInputCondition(new YInputCondition("start", net));
		net.setOutputCondition(new YOutputCondition("end", net));

		YAtomicTask task = createEmailTask(gate, net);
		YAtomicTask task2 = createJythonTask(gate2, net);

		YFlow flow = new YFlow(net.getInputCondition(), task2);
		net.getInputCondition().setPostset(flow);

		flow = new YFlow(task2, task);
		task2.setPostset(flow);

		flow = new YFlow(task, net.getOutputCondition());
		net.getOutputCondition().setPreset(flow);
	}

}
