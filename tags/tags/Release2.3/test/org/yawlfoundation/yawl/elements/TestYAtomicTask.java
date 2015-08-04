package org.yawlfoundation.yawl.elements;

import junit.framework.TestCase;
import org.jdom.Document;
import org.jdom.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.YNetData;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.schema.YSchemaVersion;
import org.yawlfoundation.yawl.util.YMessagePrinter;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * 
 * Author: Lachlan Aldred
 * Date: 28/04/2003
 * Time: 11:12:39
 * 
 */
public class TestYAtomicTask extends TestCase{
	private YAtomicTask _atomicTask1;
    private YDecomposition _ydecomp;
    private String _activityID = "Activity1";
    private YAtomicTask _atomicTask2;
    private YCondition _c1;

    /**
	 * Constructor for TaskTest
	 */
	public TestYAtomicTask(String name) {
		super(name);
	}


    public void setUp() throws YPersistenceException {
        YSpecification spec = new YSpecification("");
        spec.setVersion(YSchemaVersion.Beta2);
        YNet deadNet = new YNet("aNet", spec);
        YVariable v = new YVariable(null);
        v.setName("stubList");
        v.setUntyped(true);
        v.setInitialValue("<stub/><stub/><stub/>");
        deadNet.setLocalVariable(v);
        YNetData casedata = new YNetData();
        deadNet.initializeDataStore(null, casedata);
        deadNet.initialise(null);
        _atomicTask1 = new YAtomicTask("AT1", YAtomicTask._AND, YAtomicTask._AND, deadNet);
        _atomicTask1.setDecompositionPrototype(new YAWLServiceGateway(_activityID, spec));
        _atomicTask1.setUpMultipleInstanceAttributes(
                "3", "5", "3", YMultiInstanceAttributes._creationModeStatic);
        Map map = new HashMap();
        map.put("stub","/data/stubList");
        _atomicTask1.setMultiInstanceInputDataMappings("stub", "for $d in /stubList/* return $d");
        _atomicTask1.setDataMappingsForTaskStarting(map);
        _ydecomp = _atomicTask1.getDecompositionPrototype();
        YParameter p = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        p.setName("stub");
        p.setUntyped(true);
        _ydecomp.addInputParameter(p);
        YAtomicTask before = new YAtomicTask("before", YAtomicTask._OR, YAtomicTask._AND, deadNet);
        YAtomicTask after = new YAtomicTask("after", YAtomicTask._OR, YAtomicTask._AND, deadNet);
        _c1 = new YCondition("c1", "c1", deadNet);
        YFlow f = new YFlow(_c1, _atomicTask1);
        _atomicTask1.addPreset(f);
        f = new YFlow(_atomicTask1, _c1);
        _atomicTask1.addPostset(f);
        _atomicTask2 = new YAtomicTask("at2", YAtomicTask._AND, YAtomicTask._AND, deadNet);
        f = new YFlow(before, _atomicTask2);
        _atomicTask2.addPreset(f);
        f = new YFlow(_atomicTask2, after);
        _atomicTask2.addPostset(f);
    }


    public void testFullAtomicTask(){
        List messages = _atomicTask1.verify();
        if(messages.size() > 0){
            YMessagePrinter.printMessages(messages);
            System.out.println("(YVerificationMessage)messages.get(0)).class.getName( = " + messages.get(0).getClass().getName());
            fail(((YVerificationMessage)messages.get(0)).getMessage() + " " + messages.size());
        }
    }


    public void testEmptyAtomicTask(){
        List messages = _atomicTask2.verify();
        if(messages.size() > 0){
            YMessagePrinter.printMessages(messages);
            fail(((YVerificationMessage)messages.get(0)).getMessage() + " num-messages: " + messages.size());
        }
    }


    public void testFireAtomicTask() throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        _c1.add(null, new YIdentifier(null));
        List l = null;
        l = _atomicTask1.t_fire(null);

        Iterator i = l.iterator();
        while(i.hasNext() && _atomicTask1.t_isBusy()){
            YIdentifier id = (YIdentifier) i.next();
            _atomicTask1.t_start(null, id);
            Document d = new Document(new Element("data"));
            _atomicTask1.t_complete(null, id, d);
        }
        assertFalse(_atomicTask1.t_isBusy());
    }
}





