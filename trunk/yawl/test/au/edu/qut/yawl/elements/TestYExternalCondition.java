/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.exceptions.*;
import junit.framework.TestCase;
import org.jdom.Document;
import org.jdom.Element;
import au.edu.qut.yawl.engine.*;
import au.edu.qut.yawl.engine.domain.YCaseData;

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
        spec.setBetaVersion(YSpecification._Beta2);
        _net = new YNet("aNetName", spec);
        YVariable v = new YVariable(null);
        v.setName("stubList");
        v.setUntyped(true);
        v.setInitialValue("<stub/><stub/><stub/>");
        _net.setLocalVariable(v);
        YCaseData casedata = new YCaseData();
        _net.initializeDataStore(casedata);
        _net.initialise();
	_condition = new YCondition("c1", "C1", _net);
        _condition2 = new YCondition("c2", "C2", _net);
        _aTask = new YAtomicTask("at1", YAtomicTask._XOR, YAtomicTask._AND, _net);
        YFlow f = new YFlow(_condition, _aTask);
        _aTask.setPreset(f);
        f = new YFlow(_aTask, _condition2);
        _aTask.setPostset(f);
    }



	public void testConstructor(){
		assertTrue(_condition != null);
        assertTrue(_condition.getName().equals("C1"));
	}


    public void testMovingIdentifiers() throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        YIdentifier id = new YIdentifier();
        assertTrue(id.getLocations().size() == 0);
        assertFalse(id.getLocations().contains(_condition));
        _condition.add(id);
        assertTrue("locations should contain C1 ",
                id.getLocations().contains(_condition) && id.getLocations().size() == 1);
        assertTrue(id.getLocations().contains(_condition));
        assertTrue(_aTask.t_enabled(id));
        YIdentifier childID = null;
        childID = (YIdentifier) _aTask.t_fire().get(0);

        assertTrue("locations should be empty ", id.getLocations().size() == 1);
        assertTrue(id.getLocations().iterator().next().equals(_aTask));
        assertFalse(id.getLocations().contains(_condition));
        _aTask.t_start(childID);
        Document d = new Document();d.setRootElement(new Element("data"));
        _aTask.t_complete(childID, d);
        assertTrue(_condition2.getAmount(id) == 1);
        assertTrue(id.getLocations().contains(_condition2)
                && id.getLocations().size() == 1);
    }
}
