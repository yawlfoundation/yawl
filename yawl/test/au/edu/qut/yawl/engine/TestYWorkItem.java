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
import au.edu.qut.yawl.persistence.AbstractTransactionalTestCase;
import au.edu.qut.yawl.persistence.dao.AbstractHibernateDAOTestCase;
import junit.framework.TestCase;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 30/05/2003
 * Time: 16:07:19
 * 
 */
public class TestYWorkItem extends AbstractTransactionalTestCase{
    private YIdentifier _identifier;
    private YWorkItemID _workItemID;
    private YWorkItem _workItem;
    private YIdentifier _childIdentifier;
    
    private YIdentifier _deadlockedWorkItemIdentifier;
    private YWorkItemID _deadlockedWorkItemID;
    private YWorkItem _deadlockedWorkItem;

    public TestYWorkItem(){
        super();
    }


    public void setUp() throws Exception{
    	super.setUp();
        _identifier = new YIdentifier();
//        getDAO().save(_identifier);
//        YIdentifier.saveIdentifier( _identifier, null, null );
        _childIdentifier = _identifier.createChild();
//        _workItemID = new YWorkItemID(_identifier, "task-123");
        _workItem = new YWorkItem("ASpecID", _identifier, "task-123", true, false);
//        getDAO().save(_workItem);
//        YWorkItem.saveWorkItem( _workItem );
        
        _deadlockedWorkItemIdentifier = new YIdentifier();
//        _deadlockedWorkItemID = new YWorkItemID(_deadlockedWorkItemIdentifier, "task-abc");
        _deadlockedWorkItem = new YWorkItem("AnotherSpecID", _deadlockedWorkItemIdentifier, "task-abc", true, true );
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
        assertNotNull(child.getChildren());
        assertEquals(child.getChildren().size(), 0);
        assertNull(_workItem.getParent());
        assertEquals(_workItem.getChildren().iterator().next(), child);
        assertTrue(child.getStatus().equals(YWorkItem.Status.Fired));
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
            child.setStatusToComplete(false);
        }catch(Exception f){
            e= f;
        }
        assertNotNull("Should have thown an exception.",e);

    }
    
    public void testProperStatusChange() throws YPersistenceException {
    	assertTrue(""+_workItem.getStatus(), _workItem.getStatus().equals(YWorkItem.Status.Enabled));
    	assertNull(_workItem.getParent());
    	YWorkItem child = _workItem.createChild(_workItem.getCaseID().createChild());
    	assertNotNull(child);
    	assertTrue(""+_workItem.getStatus(), _workItem.getStatus().equals(YWorkItem.Status.IsParent));
    	assertNotNull(child.toXML(), child.getStatus());
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Fired));
    	
    	child.setStatusToStarted("admin");
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Executing));
    	child.setStatusToComplete(false);
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Complete));
    	child.setStatusToDelete();
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Cancelled));
    }
    
    public void testRollBackStatusSuccess() throws YPersistenceException {
    	assertTrue(""+_workItem.getStatus(), _workItem.getStatus().equals(YWorkItem.Status.Enabled));
    	assertNull(_workItem.getParent());
    	YWorkItem child = _workItem.createChild(_workItem.getCaseID().createChild());
    	assertNotNull(child);
    	assertTrue(""+_workItem.getStatus(), _workItem.getStatus().equals(YWorkItem.Status.IsParent));
    	assertNotNull(child.toXML(), child.getStatus());
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Fired));
    	
    	child.setStatusToStarted("admin");
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Executing));
    	child.rollBackStatus();
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Fired));
    	child.setStatusToStarted("admin");
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Executing));
    	child.setStatusToComplete(false);
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Complete));
    	child.setStatusToDelete();
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Cancelled));
    }
    
    public void testImproperRollBack() throws YPersistenceException {
    	assertTrue(""+_workItem.getStatus(), _workItem.getStatus().equals(YWorkItem.Status.Enabled));
    	try {
    		_workItem.rollBackStatus();
    		fail("Should have thrown an exception.");
    	}
    	catch(RuntimeException e) {
    		// proper exception was thrown
    	}
    	
    	assertTrue(""+_workItem.getStatus(), _workItem.getStatus().equals(YWorkItem.Status.Enabled));
    	assertNull(_workItem.getParent());
    	YWorkItem child = _workItem.createChild(_workItem.getCaseID().createChild());
    	assertNotNull(child);
    	assertTrue(""+_workItem.getStatus(), _workItem.getStatus().equals(YWorkItem.Status.IsParent));
    	assertNotNull(child.toXML(), child.getStatus());
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Fired));
    	
    	try {
    		child.rollBackStatus();
    		fail("Should have thrown an exception.");
    	}
    	catch(RuntimeException e) {
    		// proper exception was thrown
    	}
    	child.setStatusToStarted("admin");
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Executing));
    	child.setStatusToComplete(false);
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Complete));
    	try {
    		child.rollBackStatus();
    		fail("Should have thrown an exception.");
    	}
    	catch(RuntimeException e) {
    		// proper exception was thrown
    	}
    	child.setStatusToDelete();
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Cancelled));
    	try {
    		child.rollBackStatus();
    		fail("Should have thrown an exception.");
    	}
    	catch(RuntimeException e) {
    		// proper exception was thrown
    	}
    	try {
    		_deadlockedWorkItem.rollBackStatus();
    		fail("Should have thrown an exception.");
    	}
    	catch(RuntimeException e) {
    		// proper exception was thrown
    	}
    }
    
    public void testImproperStart() throws YPersistenceException {
    	assertTrue(""+_workItem.getStatus(), _workItem.getStatus().equals(YWorkItem.Status.Enabled));
    	try {
    		_workItem.setStatusToStarted( "admin" );
    		fail("Should throw an exception since work item is not in fired state");
    	}
    	catch(RuntimeException e) {
    		// proper exception thrown.
    	}
    	
    	assertTrue(""+_workItem.getStatus(), _workItem.getStatus().equals(YWorkItem.Status.Enabled));
    	assertNull(_workItem.getParent());
    	YWorkItem child = _workItem.createChild(_workItem.getCaseID().createChild());
    	assertNotNull(child);
    	assertTrue(""+_workItem.getStatus(), _workItem.getStatus().equals(YWorkItem.Status.IsParent));
    	assertNotNull(child.toXML(), child.getStatus());
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Fired));
    	
    	child.setStatusToStarted("admin");
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Executing));
    	try {
    		child.setStatusToStarted( "admin" );
    		fail("Should throw an exception since work item is not in fired state");
    	}
    	catch(RuntimeException e) {
    		// proper exception thrown.
    	}
    	child.setStatusToComplete(false);
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Complete));
    	try {
    		child.setStatusToStarted( "admin" );
    		fail("Should throw an exception since work item is not in fired state");
    	}
    	catch(RuntimeException e) {
    		// proper exception thrown.
    	}
    	child.setStatusToDelete();
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Cancelled));
    	try {
    		child.setStatusToStarted( "admin" );
    		fail("Should throw an exception since work item is not in fired state");
    	}
    	catch(RuntimeException e) {
    		// proper exception thrown.
    	}
    	try {
    		_deadlockedWorkItem.setStatusToStarted( "admin" );
    		fail("Should throw an exception since work item is not in fired state");
    	}
    	catch(RuntimeException e) {
    		// proper exception thrown.
    	}
    }
    
    public void testImproperComplete() throws YPersistenceException {
    	assertTrue(""+_workItem.getStatus(), _workItem.getStatus().equals(YWorkItem.Status.Enabled));
    	try {
    		_workItem.setStatusToComplete(false);
    		fail("Should throw an exception since work item is not in executing state");
    	}
    	catch(RuntimeException e) {
    		// proper exception thrown.
    	}
    	
    	assertTrue(""+_workItem.getStatus(), _workItem.getStatus().equals(YWorkItem.Status.Enabled));
    	assertNull(_workItem.getParent());
    	YWorkItem child = _workItem.createChild(_workItem.getCaseID().createChild());
    	assertNotNull(child);
    	assertTrue(""+_workItem.getStatus(), _workItem.getStatus().equals(YWorkItem.Status.IsParent));
    	assertNotNull(child.toXML(), child.getStatus());
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Fired));
    	
    	try {
    		child.setStatusToComplete(false);
    		fail("Should throw an exception since work item is not in executing state");
    	}
    	catch(RuntimeException e) {
    		// proper exception thrown.
    	}
    	child.setStatusToStarted("admin");
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Executing));
    	child.setStatusToComplete(false);
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Complete));
    	try {
    		child.setStatusToComplete(false);
    		fail("Should throw an exception since work item is not in executing state");
    	}
    	catch(RuntimeException e) {
    		// proper exception thrown.
    	}
    	child.setStatusToDelete();
    	assertTrue(""+child.getStatus(), child.getStatus().equals(YWorkItem.Status.Cancelled));
    	try {
    		child.setStatusToComplete(false);
    		fail("Should throw an exception since work item is not in executing state");
    	}
    	catch(RuntimeException e) {
    		// proper exception thrown.
    	}
    	try {
    		_deadlockedWorkItem.setStatusToComplete(false);
    		fail("Should throw an exception since work item is not in executing state");
    	}
    	catch(RuntimeException e) {
    		// proper exception thrown.
    	}
    }
    
    public void testSetWorkItemWithSiblingsToComplete() throws YPersistenceException {
    	assertTrue(""+_workItem.getStatus(), _workItem.getStatus().equals(YWorkItem.Status.Enabled));
    	assertNull(_workItem.getParent());
    	YWorkItem child1 = _workItem.createChild(_workItem.getCaseID().createChild());
    	assertNotNull(child1);
    	assertTrue(""+_workItem.getStatus(), _workItem.getStatus().equals(YWorkItem.Status.IsParent));
    	assertNotNull(child1.toXML(), child1.getStatus());
    	assertTrue(""+child1.getStatus(), child1.getStatus().equals(YWorkItem.Status.Fired));
    	
    	YWorkItem child2 = _workItem.createChild(_workItem.getCaseID().createChild());
    	assertNotNull(child2);
    	assertTrue(""+_workItem.getStatus(), _workItem.getStatus().equals(YWorkItem.Status.IsParent));
    	assertNotNull(child2.toXML(), child2.getStatus());
    	assertTrue(""+child2.getStatus(), child2.getStatus().equals(YWorkItem.Status.Fired));
    	
    	YWorkItem child3 = _workItem.createChild(_workItem.getCaseID().createChild());
    	assertNotNull(child3);
    	assertTrue(""+_workItem.getStatus(), _workItem.getStatus().equals(YWorkItem.Status.IsParent));
    	assertNotNull(child3.toXML(), child3.getStatus());
    	assertTrue(""+child3.getStatus(), child3.getStatus().equals(YWorkItem.Status.Fired));
    	
    	child1.setStatusToStarted("admin");
    	assertTrue(""+child1.getStatus(), child1.getStatus().equals(YWorkItem.Status.Executing));
    	
    	child2.setStatusToStarted("admin");
    	assertTrue(""+child2.getStatus(), child2.getStatus().equals(YWorkItem.Status.Executing));
    	
    	child1.setStatusToComplete(false);
    	assertTrue(""+child1.getStatus(), child1.getStatus().equals(YWorkItem.Status.Complete));
    	
    	child2.setStatusToComplete(false);
    	assertTrue(""+child2.getStatus(), child2.getStatus().equals(YWorkItem.Status.Complete));
    	
    	child3.setStatusToStarted("admin");
    	assertTrue(""+child3.getStatus(), child3.getStatus().equals(YWorkItem.Status.Executing));
    	child3.setStatusToComplete(false);
    	assertTrue(""+child3.getStatus(), child3.getStatus().equals(YWorkItem.Status.Complete));
    	
    	child1.setStatusToDelete();
    	assertTrue(""+child1.getStatus(), child1.getStatus().equals(YWorkItem.Status.Cancelled));
    	child2.setStatusToDelete();
    	assertTrue(""+child2.getStatus(), child2.getStatus().equals(YWorkItem.Status.Cancelled));
    	child3.setStatusToDelete();
    	assertTrue(""+child3.getStatus(), child3.getStatus().equals(YWorkItem.Status.Cancelled));
    }
    
    public void testConstructor(){
        assertNull(_workItem.getParent());
        assertNotNull(_workItem.toXML(), _workItem.getStatus());
        assertTrue(""+_workItem.getStatus(), _workItem.getStatus().equals(YWorkItem.Status.Enabled));
        
        assertNull(_deadlockedWorkItem.getParent());
        assertNotNull(_deadlockedWorkItem.toXML(), _deadlockedWorkItem.getStatus());
        assertTrue(""+_deadlockedWorkItem.getStatus(),
        		_deadlockedWorkItem.getStatus().equals(YWorkItem.Status.Deadlocked));
    }
}
