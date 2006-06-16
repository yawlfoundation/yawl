package au.edu.qut.yawl.persistence.dao;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nexusbpm.editor.component.EmailSenderComponent;
import com.nexusbpm.editor.tree.DatasourceRoot;

import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YMetaData;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.elements.YSpecification;

public class SpecificationMemoryDAO implements SpecificationDAO{
    
    private Map<Long, YSpecification> specs = new HashMap<Long, YSpecification>();
    private long nextdbID = 1;
    public static YSpecification testSpec = new YSpecification("virtual://memory/home/sandozm/templates/testing/testspec.xml");
    static {
    	//TODO move this all into a mock - yuck!
			testSpec.setName("My test specification.xml");
			testSpec.setBetaVersion(YSpecification._Beta7_1);
			testSpec.setMetaData(new YMetaData());
			YNet net = new YNet("My_test_net", testSpec);
			net.setName("My test network");
			net.setRootNet("true");
			testSpec.setRootNet(net);
			YAWLServiceGateway gate = new YAWLServiceGateway(EmailSenderComponent.class.getName(), testSpec);
			gate.setName("[send mail support]");
			testSpec.setDecomposition(gate);
			YAWLServiceGateway gate2 = new YAWLServiceGateway("com.ichg.capsela.domain.component.JythonComponent", testSpec);
			gate2.setName("[jython scripting support]");
			testSpec.setDecomposition(gate2);
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
    
    public SpecificationMemoryDAO() {
    	save(testSpec);
    }    
    
    public boolean delete(YSpecification m) {
        specs.remove(m.getID());
        return true;
    }

    public YSpecification retrieve(Object m) {
    	return specs.get(m);
    }

    public int save(YSpecification spec) {
        if (spec.getDbID() == null) spec.setDbID(getNextdbID());
    	specs.put(spec.getDbID(), spec);
        return 0;
    }

    public Serializable getKey(YSpecification m) {
        return m.getID();
    }

	public List getChildren(Object o) {
		if (o == null) throw new IllegalArgumentException("Parent can not be null");
		List retval = new ArrayList();
		if (o instanceof String || o instanceof DatasourceRoot) {

		String path = o.toString();
		for (YSpecification value: specs.values()) {
			String key = value.getID();
			if (key != null && key.startsWith(path)) {
				if (ab(path, key) != null) {
					retval.add(ab(path, key));
				} else {
					retval.add(value);
				}
			}
		}
		}
		return retval;
	}

	private long getNextdbID() {
		return nextdbID++;
	}

	private static String ab(String a, String b) {
		String retval = null;
//		a = "/home/msandoz/a/b/c.efg";
//		b = "/home/msandoz/a/b/c.efg";
		if (b.startsWith(a)) {
			int x = b.lastIndexOf("/");
			int y = b.indexOf("/", a.length() + 1);
//			System.out.println("stats:" + x + ":" + y + ":" + a + ":" + b);
			if (x > a.length()) {
				retval = b.substring(0, y);
			}
		}
//		System.out.println(retval);
		return retval;
	}
	
}
