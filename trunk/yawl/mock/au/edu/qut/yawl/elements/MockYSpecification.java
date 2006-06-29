/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.elements;

import java.net.URI;
import java.net.URISyntaxException;

import au.edu.qut.yawl.elements.data.YInputParameter;
import au.edu.qut.yawl.elements.data.YOutputParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import com.nexusbpm.editor.component.EmailSenderComponent;
import com.nexusbpm.editor.component.JythonComponent;
import command.EditorCommand;

/**
 * The purpose of theMockYSpecification is to provide an example of how to build
 * a robust specification that will be compatible with capsela-style variables
 * and to provide a ready-made specification to use in testing. It is also a 
 * testbed for systematic methods of setting variables in specifications. 
 * 
 * @author Matthew Sandoz
 * 
 */
public class MockYSpecification {
	private static final String SCHEMA_URL = "http://www.w3.org/2001/XMLSchema";

	public static YSpecification instance;
	
	public static final String rootNetName = "My test net";

	private static final String[] jythonProps = new String[] { "code",
			"output", "error" };

	private static final String[] jythonVals = new String[] {
			"print 3.141592653589793", "systemout", "errorout" };

	private static final String[] emailProps = new String[] { "fromAddress",
			"toAddresses", "subject", "body", "markupAttachment" };

	private static final String[] emailVals = new String[] {
			"matthew.sandoz@ichg.com", "dean.mao@ichg.com",
			"Did you get that thing I sent you?",
			"This is the message body we expect to deliver.",
			"Not sure what this attachment is for" };

	private static final String[] gatewayProps = new String[]{
		"YawlWSInvokerWSDLLocation",
		"YawlWSInvokerOperationName",
		"YawlWSInvokerPortName"
	};
	
	private static final String[] jythonGatewayVals = new String[]{
		"http://localhost:8080/JythonService/services/JythonService?wsdl",
		"execute",
		""
	};
	private static final String[] emailGatewayVals = new String[]{
		"http://localhost:8080/JythonService/services/JythonService?wsdl",
		"execute",
		""
	};
	
	private static YAWLServiceGateway createEmailGateway(
			YSpecification specification) {
		YAWLServiceGateway gate = new YAWLServiceGateway(
				EmailSenderComponent.class.getName(), specification);
		gate.setName("[send mail support]");
		for (String prop : emailProps) {
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
		generateTaskVariables(net, gate, task, emailProps, emailVals);
		generateTaskWebserviceVariables(net, gate, task, gatewayProps, emailGatewayVals);

		net.addNetElement(task);
		return task;
	}

	private static YAWLServiceGateway createJythonGateway(
			YSpecification specification) {
		YAWLServiceGateway gate = new YAWLServiceGateway(JythonComponent.class
				.getName(), specification);
		gate.setName("[jython scripting support]");

		YAWLServiceReference yawlServiceReference = new YAWLServiceReference();
		yawlServiceReference
				.setYawlServiceID("http://localhost:8080/yawlWSInvoker/");
		yawlServiceReference.setYawlServiceGateway(gate);
		gate.setYawlService(yawlServiceReference);

		YInputParameter wsdlLocation = new YInputParameter(gate,
				YInputParameter._INPUT_PARAM_TYPE);
		wsdlLocation.setDataTypeAndName("string", "YawlWSInvokerWSDLLocation",
				SCHEMA_URL);
//		wsdlLocation
//				.setInitialValue("http://localhost:8080/JythonService/services/JythonService?wsdl");
		gate.setInputParam(wsdlLocation);

		YInputParameter operationName = new YInputParameter(gate,
				YInputParameter._INPUT_PARAM_TYPE);
		operationName.setDataTypeAndName("string",
				"YawlWSInvokerOperationName", SCHEMA_URL);
//		operationName.setInitialValue("execute");
		gate.setInputParam(operationName);

		YInputParameter portName = new YInputParameter(gate,
				YInputParameter._INPUT_PARAM_TYPE);
		portName.setDataTypeAndName("string", "YawlWSInvokerPortName",
				SCHEMA_URL);
//		portName.setInitialValue("WHATS IT FOR??");
		gate.setInputParam(portName);

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
		generateTaskWebserviceVariables(net, gate, task, gatewayProps, jythonGatewayVals);
		net.addNetElement(task);
		return task;
	}

	private static YNet createRootNet(YSpecification specification) {
		YNet net = new YNet(rootNetName, specification);
		net.setName(rootNetName);
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

	private static void generateTaskWebserviceVariables(YNet net,
			YAWLServiceGateway gate, YAtomicTask task, String[] props,
			String[] vals) {
		String taskPath = task.getID() + ".";
		String path = "/" + net.getId() + "/" + taskPath;
		String gatewayPath = "/" + gate.getId() + "/";
		task.setDecompositionPrototype(gate);
		for (int i = 0; i < props.length; i++) {
			String prop = props[i]; 
			String val = vals[i]; 
			task.setDataBindingForInputParam("<" + prop + ">" + val + "</" + prop + ">", prop);
		}
	}

	
	
	public static YSpecification getNewSpecification() {
		YSpecification spec = null;
		try {
			spec = new YSpecification((
					new URI(
							"virtual",
							"memory", 
							"/home/sandozm/templates/testing/Spec François.xml", 
							null, 
							null
							)).toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
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
		specification.setName("My test specification");
		specification.setBetaVersion(YSpecification._Beta7_1);
		specification.setMetaData(new YMetaData());

		YAWLServiceGateway gate = createEmailGateway(specification);
		YAWLServiceGateway gate2 = createJythonGateway(specification);

		YNet net = createRootNet(specification);

		YInputCondition inputCondition = new YInputCondition("start", net);
		inputCondition.setName("start");
		net.setInputCondition(inputCondition);
		YOutputCondition outputCondition = new YOutputCondition("end", net);
		outputCondition.setName("end");
		net.setOutputCondition(outputCondition);

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
