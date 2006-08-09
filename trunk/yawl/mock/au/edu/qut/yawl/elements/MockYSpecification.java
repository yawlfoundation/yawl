/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.elements;

import java.awt.geom.Rectangle2D;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import operation.WorkflowOperation;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.NexusWorkflow;
import com.nexusbpm.command.Command;
import com.nexusbpm.command.CreateFlowCommand;
import com.nexusbpm.command.CreateNetCommand;
import com.nexusbpm.command.CreateNexusComponentCommand;
import com.nexusbpm.command.CreateSpecificationCommand;
import com.nexusbpm.editor.persistence.YTaskEditorExtension;
import com.nexusbpm.editor.tree.SharedNode;
import com.nexusbpm.editor.tree.SharedNodeTreeModel;
import com.nexusbpm.services.NexusServiceInfo;

/**
 * The purpose of theMockYSpecification is to provide an example of how to build
 * a robust specification that will be compatible with capsela-style variables
 * and to provide a ready-made specification to use in testing. It is also a 
 * testbed for systematic methods of setting variables in specifications. 
 * 
 * @author Matthew Sandoz
 * @author Nathan Rose
 */
public class MockYSpecification {
    // TODO this class needs to be refactored to use the operations in WorkflowOperation
	private static final String SCHEMA_URL = "http://www.w3.org/2001/XMLSchema";

	public static YSpecification instance;
	
	public static final String rootNetName = "My test net";

	private static final String[] jythonProps = new String[] { "code",
			"output", "error", "ServiceName" };

	private static final String[] jythonVals = new String[] {
			"print 3.141592653589793", "systemout", "errorout", "Jython" };

	private static final String[] emailProps = new String[] { "fromAddress",
			"toAddresses", "subject", "body", "markupAttachment", "ServiceName" };

	private static final String[] emailVals = new String[] {
			"matthew.sandoz@ichg.com", "dean.mao@ichg.com",
			"Did you get that thing I sent you?",
			"This is the message body we expect to deliver.",
			"Not sure what this attachment is for", "EmailSender" };

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
        YAWLServiceGateway gate = new YAWLServiceGateway("EmailSender", specification);
//		YAWLServiceGateway gate = new YAWLServiceGateway(
//				EmailSenderComponent.class.getName(), specification);
		gate.setName("[send mail support]");
		for (String prop : emailProps) {
			YParameter gatewayVar = new YParameter(gate,
					YParameter._INPUT_PARAM_TYPE);
			gatewayVar.setDataTypeAndName("string", prop, SCHEMA_URL);
			gate.setInputParam(gatewayVar);
			YParameter gwVar2 = new YParameter(gate,
					YParameter._OUTPUT_PARAM_TYPE);
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
		task.setID("email dean");
		YTaskEditorExtension editor = new YTaskEditorExtension(task);
		editor.setBounds(new Rectangle2D.Double(100,200, 120, 250));
		generateTaskVariables(net, gate, task, emailProps, emailVals);
		generateTaskWebserviceVariables(net, gate, task, gatewayProps, emailGatewayVals);

		net.addNetElement(task);
		return task;
	}

	private static YAWLServiceGateway createJythonGateway(
			YSpecification specification) {
		YAWLServiceGateway gate = new YAWLServiceGateway("JythonComponent", specification);
//        YAWLServiceGateway gate = new YAWLServiceGateway(JythonComponent.class
//				.getName(), specification);
		gate.setName("[jython scripting support]");

		YAWLServiceReference yawlServiceReference = new YAWLServiceReference();
		yawlServiceReference
				.setYawlServiceID("http://localhost:8080/yawlWSInvoker/");
		yawlServiceReference.setYawlServiceGateway(gate);
		gate.setYawlService(yawlServiceReference);

		YParameter wsdlLocation = new YParameter(gate,
				YParameter._INPUT_PARAM_TYPE);
		wsdlLocation.setDataTypeAndName("string", "YawlWSInvokerWSDLLocation",
				SCHEMA_URL);
//		wsdlLocation
//				.setInitialValue("http://localhost:8080/JythonService/services/JythonService?wsdl");
		gate.setInputParam(wsdlLocation);

		YParameter operationName = new YParameter(gate,
				YParameter._INPUT_PARAM_TYPE);
		operationName.setDataTypeAndName("string",
				"YawlWSInvokerOperationName", SCHEMA_URL);
//		operationName.setInitialValue("execute");
		gate.setInputParam(operationName);

		YParameter portName = new YParameter(gate,
				YParameter._INPUT_PARAM_TYPE);
		portName.setDataTypeAndName("string", "YawlWSInvokerPortName",
				SCHEMA_URL);
//		portName.setInitialValue("WHATS IT FOR??");
		gate.setInputParam(portName);

		for (String prop : jythonProps) {
			YParameter gatewayVar = new YParameter(gate,
					YParameter._INPUT_PARAM_TYPE);
			gatewayVar.setDataTypeAndName("string", prop, SCHEMA_URL);
			gate.setInputParam(gatewayVar);
			YParameter gwVar2 = new YParameter(gate,
					YParameter._OUTPUT_PARAM_TYPE);
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
		task.setID("quote of the day");
		task.setDecompositionPrototype(gate);
		YTaskEditorExtension editor = new YTaskEditorExtension(task);
		editor.setBounds(new Rectangle2D.Double(200,100, 220, 150));
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
		String taskPath = task.getID() + NexusWorkflow.NAME_SEPARATOR;
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
			var.setDataTypeAndName("string", task.getID() + NexusWorkflow.NAME_SEPARATOR + props[i],
					SCHEMA_URL);
			var.setInitialValue(vals[i]);
			net.setLocalVariable(var);
		}
	}

	private static void generateTaskWebserviceVariables(YNet net,
			YAWLServiceGateway gate, YAtomicTask task, String[] props,
			String[] vals) {
		String taskPath = task.getID() + NexusWorkflow.NAME_SEPARATOR;
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
		YTaskEditorExtension editor = new YTaskEditorExtension(inputCondition);
		editor.setBounds(new Rectangle2D.Double(100,100, 120, 150));
		net.setInputCondition(inputCondition);
		YOutputCondition outputCondition = new YOutputCondition("end", net);
		outputCondition.setName("end");
		editor = new YTaskEditorExtension(outputCondition);
		editor.setBounds(new Rectangle2D.Double(200,200, 220, 250));
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
    
    public static void makeSpec( SharedNode parent ) throws Exception {
        DataContext context = parent.getProxy().getContext();
        DataProxy parentProxy = parent.getProxy();
        SharedNodeTreeModel model = (SharedNodeTreeModel) parent.getTreeModel();
        
        Set<SharedNode> oldChildren = new HashSet<SharedNode>();
        
        for( int index = 0; index < parent.getChildCount(); index++ ) {
            oldChildren.add( (SharedNode) parent.getChildAt( index ) );
        }
        
        Command createSpec = new CreateSpecificationCommand(
                parentProxy,
                "Test Specification",
                model );
        createSpec.execute();
        
        SharedNode specNode = null;
        
        for( int index = 0; index < parent.getChildCount(); index++ ) {
            if( ! oldChildren.contains( parent.getChildAt( index ) ) ) {
                specNode = (SharedNode) parent.getChildAt( index );
                break;
            }
        }
        
        assert specNode != null : "specNode was null";
        assert specNode.getProxy().getData() instanceof YSpecification : "invalid child of parent";
        
        DataProxy<YSpecification> specProxy = specNode.getProxy();
        
        Command createNet = new CreateNetCommand(
                specProxy,
                "Test Root Net",
                model );
        createNet.execute();
        
        DataProxy<YNet> netProxy = context.getDataProxy(
                specProxy.getData().getDecompositions().get( 0 ) );
        
        assert netProxy != null : "net proxy was null";
        
        NexusServiceInfo jython = NexusServiceInfo.getServiceWithName( "Jython" );
        Command createJython = new CreateNexusComponentCommand(
                netProxy,
                jython.getServiceName(),
                jython.getServiceName(),
                jython,
                model );
        createJython.execute();
        
        NexusServiceInfo emailSender = NexusServiceInfo.getServiceWithName( "EmailSender" );
        Command createEmailSender = new CreateNexusComponentCommand(
                netProxy,
                emailSender.getServiceName(),
                emailSender.getServiceName(),
                emailSender,
                model );
        createEmailSender.execute();
        
        DataProxy<YInputCondition> inputProxy = null;
        DataProxy<YAtomicTask> jythonProxy = null;
        DataProxy<YAtomicTask> emailSenderProxy = null;
        DataProxy<YOutputCondition> outputProxy= null;
        
        for( int index = 0; index < netProxy.getData().getNetElements().size(); index++ ) {
            YExternalNetElement element = netProxy.getData().getNetElements().get( index );
            if( element instanceof YAtomicTask ) {
                NexusServiceInfo info = WorkflowOperation.getNexusServiceInfoForTask( (YAtomicTask) element );
                if( info != null ) {
                    if( info.getServiceName().equals( "Jython" ) ) {
                        jythonProxy = context.getDataProxy( element );
                    }
                    else if( info.getServiceName().equals( "EmailSender" ) ) {
                        emailSenderProxy = context.getDataProxy( element );
                    }
                }
            }
            else if( element instanceof YInputCondition ) {
                inputProxy = context.getDataProxy( element );
            }
            else if( element instanceof YOutputCondition ) {
                outputProxy = context.getDataProxy( element );
            }
        }
        
        assert inputProxy != null : "input proxy was null";
        assert jythonProxy != null : "jython proxy was null";
        assert emailSenderProxy != null : "email sender proxy was null";
        assert outputProxy != null : "output proxy was null";
        
        Command flow = new CreateFlowCommand(
                inputProxy,
                jythonProxy,
                model );
        flow.execute();
        
        flow = new CreateFlowCommand(
                jythonProxy,
                emailSenderProxy,
                model );
        flow.execute();
        
        flow = new CreateFlowCommand(
                emailSenderProxy,
                outputProxy,
                model );
        flow.execute();
        
        // TODO insert data
        
        // TODO connect data transfer
    }
    
}
