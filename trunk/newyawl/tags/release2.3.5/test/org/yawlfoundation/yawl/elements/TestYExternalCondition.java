package org.yawlfoundation.yawl.elements;

import junit.framework.TestCase;
import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.YNetData;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.schema.YSchemaVersion;

/**
 * @author aldredl
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TestYExternalCondition extends TestCase {
    private YNet _net;
    private YCondition _condition;
    private YTask _aTask;
    private YCondition _condition2;


    /**
	 * Constructor for ConditionTest.
	 * @param name
	 */
	public TestYExternalCondition(String name) {
		super(name);
	}


    public void setUp() throws YPersistenceException {
        YSpecification spec = new YSpecification("");
        spec.setVersion(YSchemaVersion.Beta2);
        _net = new YNet("aNetName", spec);
        YVariable v = new YVariable(null);
        v.setName("stubList");
        v.setUntyped(true);
        v.setInitialValue("<stub/><stub/><stub/>");
        _net.setLocalVariable(v);
        YNetData casedata = new YNetData();
        _net.initializeDataStore(null, casedata);
        _net.initialise(null);
	_condition = new YCondition("c1", "C1", _net);
        _condition2 = new YCondition("c2", "C2", _net);
        _aTask = new YAtomicTask("at1", YAtomicTask._XOR, YAtomicTask._AND, _net);
        YFlow f = new YFlow(_condition, _aTask);
        _aTask.addPreset(f);
        f = new YFlow(_aTask, _condition2);
        _aTask.addPostset(f);
    }



	public void testConstructor(){
		assertTrue(_condition != null);
        assertTrue(_condition.getName().equals("C1"));
	}


    public void testMovingIdentifiers() throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        YIdentifier id = new YIdentifier(null);
        assertTrue(id.getLocations().size() == 0);
        assertFalse(id.getLocations().contains(_condition));
        _condition.add(null, id);
        assertTrue("locations should contain C1 ",
                id.getLocations().contains(_condition) && id.getLocations().size() == 1);
        assertTrue(id.getLocations().contains(_condition));
        assertTrue(_aTask.t_enabled(id));
        YIdentifier childID = null;
        childID = (YIdentifier) _aTask.t_fire(null).get(0);

        assertTrue("locations should be empty ", id.getLocations().size() == 1);
        assertTrue(id.getLocations().iterator().next().equals(_aTask));
        assertFalse(id.getLocations().contains(_condition));
        _aTask.t_start(null, childID);
        Document d = new Document();d.setRootElement(new Element("data"));
        _aTask.t_complete(null, childID, d);
        assertTrue(_condition2.getAmount(id) == 1);
        assertTrue(id.getLocations().contains(_condition2)
                && id.getLocations().size() == 1);
    }
}
