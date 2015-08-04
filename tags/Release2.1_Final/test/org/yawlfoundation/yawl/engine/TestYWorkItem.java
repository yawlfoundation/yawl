package org.yawlfoundation.yawl.engine;

import junit.framework.TestCase;
import org.yawlfoundation.yawl.authentication.YClient;
import org.yawlfoundation.yawl.elements.YAtomicTask;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;

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
    private YTask _task;
    private YWorkItem _workItem;
    private YIdentifier _childIdentifier;

    public TestYWorkItem(String name){
        super(name);
    }


    public void setUp() throws Exception{
        _identifier = new YIdentifier(null);
        _childIdentifier = _identifier.createChild(null);
        _workItemID = new YWorkItemID(_identifier, "task-123");
        _task = new YAtomicTask("task-123", YTask._XOR, YTask._AND, null);
        _workItem = new YWorkItem(null, new YSpecificationID("ASpecID"), _task, _workItemID, true, false);
    }


    public void testCreateChild() throws YPersistenceException {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        YWorkItem child = _workItem.createChild(null, _childIdentifier);
        YIdentifier id = _childIdentifier.createChild(null);
        assertEquals(child.getParent(), _workItem);
        assertNull(child.createChild(null, id));
        assertNull(_workItem.createChild(null, new YIdentifier(null)));
        assertNull(child.getChildren());
        assertNull(_workItem.getParent());
        assertEquals(_workItem.getChildren().iterator().next(), child);
        assertTrue(child.getStatus().equals(YWorkItemStatus.statusFired));
        assertNotNull(child.getEnablementTime());
        assertEquals(child.getEnablementTime(), _workItem.getEnablementTime());
        assertFalse( child.getFiringTime().before(_workItem.getEnablementTime()));
        assertTrue(child.allowsDynamicCreation());
        YClient fred = new YClient("fred", "password", null);
        child.setStatusToStarted(null, fred);
        assertEquals(child.getExternalClient().getUserName(), "fred");
        Exception e = null;
        try{
            _workItem.setStatusToStarted(null, fred);
        }catch(Exception f){
            e= f;
        }
        assertNotNull("Should have thown an exception.",e);
        try{
            child.setStatusToComplete(null,false);
        }catch(Exception f){
            e= f;
        }
        assertNotNull("Should have thown an exception.",e);

    }


    public void testConstructor(){
        assertNull(_workItem.getParent());
        assertTrue(_workItem.getStatus().equals(YWorkItemStatus.statusEnabled));
    }
}
