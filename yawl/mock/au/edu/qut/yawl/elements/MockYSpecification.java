package au.edu.qut.yawl.elements;

import au.edu.qut.yawl.elements.data.YInputParameter;
import au.edu.qut.yawl.elements.data.YOutputParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.jaxb.WebServiceGatewayFactsType.YawlService;

import com.nexusbpm.editor.component.EmailSenderComponent;

public class MockYSpecification {
	public static YSpecification instance;

	public static YSpecification getSpecification() {
		 if (instance == null ) {
			 instance = new YSpecification("virtual://memory/home/sandozm/templates/testing/testspec.xml");		
			 initSpec(instance);
		 }
		 return instance;
	}
	
	public static YSpecification getNewSpecification() {
		YSpecification spec = new YSpecification("virtual://memory/home/sandozm/templates/testing/testspec.xml");
		initSpec(spec);
		return spec;
	}
	
	private synchronized static void initSpec(YSpecification specification) {
		specification.setName("My test specification.xml");
		specification.setBetaVersion(YSpecification._Beta7_1);
		specification.setMetaData(new YMetaData());
		
		YNet net = new YNet("My_test_net", specification);
		net.setName("My test network");
		net.setRootNet("true");
		specification.setRootNet(net);
		
		YVariable varTo = new YVariable(net);
		varTo.setDataTypeAndName("string", "email_dean.toAddresses", "http://www.w3.org/2001/XMLSchema-instance");
		varTo.setInitialValue("dean.mao@ichg.com");
		net.setLocalVariable(varTo);

		YVariable varFrom = new YVariable(net);
		varFrom.setDataTypeAndName("string", "email_dean.fromAddress", "http://www.w3.org/2001/XMLSchema-instance");
		varFrom.setInitialValue("matthew.sandoz@ichg.com");
		net.setLocalVariable(varFrom);

		YVariable varBody = new YVariable(net);
		varBody.setDataTypeAndName("string", "email_dean.body", "http://www.w3.org/2001/XMLSchema-instance");
		varBody.setInitialValue("This is the text we expect to deliver.");
		net.setLocalVariable(varBody);

		YVariable varSubject = new YVariable(net);
		varSubject.setDataTypeAndName("string", "email_dean.subject", "http://www.w3.org/2001/XMLSchema-instance");
		varSubject.setInitialValue("Did you get that thing I sent you?");
		net.setLocalVariable(varSubject);

		YVariable varMarkupAttachment = new YVariable(net);
		varMarkupAttachment.setDataTypeAndName("string", "email_dean.markupAttachment", "http://www.w3.org/2001/XMLSchema-instance");
		varMarkupAttachment.setInitialValue("Not sure what this is for?");
		net.setLocalVariable(varMarkupAttachment);

		YAWLServiceGateway gate = new YAWLServiceGateway(EmailSenderComponent.class.getName(), specification);
		gate.setName("[send mail support]");
		YInputParameter gatewayVar = new YInputParameter(gate, YInputParameter._INPUT_PARAM_TYPE);
		gatewayVar.setDataTypeAndName("string", "body", "http://www.w3.org/2001/XMLSchema-instance");
		gate.setInputParam(gatewayVar);
		YOutputParameter gwVar2 = new YOutputParameter(gate, YOutputParameter._OUTPUT_PARAM_TYPE);
		specification.setDecomposition(gate);
		gwVar2.setDataTypeAndName("string", "body", "http://www.w3.org/2001/XMLSchema-instance");
		gate.setOutputParameter(gwVar2);
		
		YAWLServiceGateway gate2 = new YAWLServiceGateway("com.ichg.capsela.domain.component.JythonComponent", specification);
		gate2.setName("[jython scripting support]");
		
		YAWLServiceReference yawlServiceReference = new YAWLServiceReference();
		yawlServiceReference.setYawlServiceID("http://localhost:8080/yawlWSInvoker/");
		yawlServiceReference.setYawlServiceGateway(gate2);
		gate2.setYawlService(yawlServiceReference);
		specification.setDecomposition(gate2);
		
		net.setInputCondition(new YInputCondition("start", net));
		net.setOutputCondition(new YOutputCondition("end", net));
		
		YAtomicTask task = new YAtomicTask("email dean", YAtomicTask._AND, YAtomicTask._AND, net); 
		task.setName("email dean");
		task.setDecompositionPrototype(gate);
		task.setDataBindingForInputParam("/My_test_net/email_dean.body","body");
		task.setDataBindingForOutputExpression("/" + EmailSenderComponent.class.getName() + "/body", "email_dean.body");

		/**
		 * 	
			private String _body = "";
			private String _subject = "";
			private String _toAddresses = "";
			private String _fromAddress = "capsela@ichotelsgroup.com";
			private String _markupAttachment = "";
		 */
		
		
		net.addNetElement(task);
		
		YAtomicTask task2 = new YAtomicTask("quote of the day", YAtomicTask._AND, YAtomicTask._AND, net); 
		task2.setName("quote of the day");
		task2.setDecompositionPrototype(gate2);
		net.addNetElement(task2);
		
		YFlow flow = new YFlow(net.getInputCondition(), task2);
		net.getInputCondition().setPostset(flow);
		
		flow = new YFlow(task2, task);
		task2.setPostset(flow);
		
		flow = new YFlow(task, net.getOutputCondition());
		net.getOutputCondition().setPreset(flow);
	}
	
}
