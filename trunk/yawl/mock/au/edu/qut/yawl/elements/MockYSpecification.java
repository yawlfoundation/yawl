package au.edu.qut.yawl.elements;

import com.nexusbpm.editor.component.EmailSenderComponent;

public class MockYSpecification {
	public static YSpecification instance;

	static {
    	//TODO move this all into a mock - yuck!
	}
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
		YAWLServiceGateway gate = new YAWLServiceGateway(EmailSenderComponent.class.getName(), specification);
		gate.setName("[send mail support]");
		specification.setDecomposition(gate);
		YAWLServiceGateway gate2 = new YAWLServiceGateway("com.ichg.capsela.domain.component.JythonComponent", specification);
		gate2.setName("[jython scripting support]");
		specification.setDecomposition(gate2);
		net.setInputCondition(new YInputCondition("start", net));
		net.setOutputCondition(new YOutputCondition("end", net));
		YAtomicTask task = new YAtomicTask("email dean", YAtomicTask._AND, YAtomicTask._AND, net); 
		task.setName("email dean");
		task.setDecompositionPrototype(gate);
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
