/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemID;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import junit.framework.TestCase;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 30/05/2003
 * Time: 16:07:19
 * 
 */
public class TestYWorkItem extends TestCase{
    private YIdentifier _identifier;
    private YWorkItemID _workItemID;
    private YWorkItem _workItem;
    private YIdentifier _childIdentifier;

    public TestYWorkItem(String name){
        super(name);
    }


    public void setUp() throws Exception{
        _identifier = new YIdentifier();
        _childIdentifier = _identifier.createChild();
        _workItemID = new YWorkItemID(_identifier, "task-123");
        _workItem = new YWorkItem("ASpecID", _workItemID, true, false);
    }


    public void testCreateChild() throws YPersistenceException {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        YWorkItem child = _workItem.createChild(_childIdentifier);
        YIdentifier id = _childIdentifier.createChild();
        assertEquals(child.getParent(), _workItem);
        assertNull(child.createChild(id));
        assertNull(_workItem.createChild(new YIdentifier()));
        assertNull(child.getChildren());
        assertNull(_workItem.getParent());
        assertEquals(_workItem.getChildren().iterator().next(), child);
        assertTrue(child.getStatus().equals("Fired"));
        assertNotNull(child.getEnablementTime());
        assertEquals(child.getEnablementTime(), _workItem.getEnablementTime());
        assertFalse( child.getFiringTime().before(_workItem.getEnablementTime()));
        assertTrue(child.allowsDynamicCreation());
        child.setStatusToStarted("fred");
        assertEquals(child.getUserWhoIsExecutingThisItem(), "fred");
        Exception e = null;
        try{
            _workItem.setStatusToStarted("fred");
        }catch(Exception f){
            e= f;
        }
        assertNotNull("Should have thown an exception.",e);
        try{
            child.setStatusToComplete();
        }catch(Exception f){
            e= f;
        }
        assertNotNull("Should have thown an exception.",e);

    }


    public void testConstructor(){
        assertNull(_workItem.getParent());
        assertTrue(_workItem.getStatus().equals("Enabled"));
    }
}
