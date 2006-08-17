/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.domain;

import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.exceptions.YPersistenceException;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 30/05/2003
 * Time: 15:32:26
 * 
 */
public class TestYWorkItemRepository extends TestCase {
    private YWorkItemRepository _workitemRepository;
    private YWorkItem _parentWorkItem;


    public TestYWorkItemRepository(String name) {
        super(name);
    }


    public void setUp() throws Exception {
        _workitemRepository = YWorkItemRepository.getInstance();
        _workitemRepository.clear();
        YIdentifier identifier = new YIdentifier();
        YIdentifier.saveIdentifier( identifier, null, null );
        YWorkItemID workItemID = new YWorkItemID(identifier, "task-123");
        _parentWorkItem = new YWorkItem("ASpecID", workItemID, false, false);
        YWorkItem.saveWorkItem( _parentWorkItem, null, null );
        for (int i = 0; i < 5; i++) {
            _parentWorkItem.createChild(identifier.createChild());
        }
    }

    public void tearDown() {
    	_workitemRepository.clear();
    }

    public void testGetItem() throws YPersistenceException {
        assertTrue(_workitemRepository.getEnabledWorkItems().size() == 0);
        new YWorkItem("A spec", new YWorkItemID(new YIdentifier(), "task4321"), false, false);
        assertEquals(
                _workitemRepository.getWorkItem(
                        _parentWorkItem.getCaseID().toString(), _parentWorkItem.getTaskID()),
                _parentWorkItem);
        _workitemRepository.removeWorkItemFamily(_parentWorkItem);
        assertNull(_workitemRepository.getWorkItem(_parentWorkItem.getCaseID().toString(), _parentWorkItem.getTaskID()));
    }

    public void testGetParentItems() throws YPersistenceException {
    	_workitemRepository.clear();
    	assertTrue( "" + _workitemRepository.getParentWorkItems().size(),
    			_workitemRepository.getParentWorkItems().size() == 0 );
    	
    	// create a couple work items
    	YWorkItem item = new YWorkItem("A spec",
    			new YWorkItemID(new YIdentifier(), "task4321"), false, false);
    	item.setStatus( YWorkItem.Status.IsParent );
    	item = new YWorkItem("A spec",
    			new YWorkItemID(new YIdentifier(), "task4321"), false, false);
    	item.setStatus( YWorkItem.Status.Cancelled );
    	item = new YWorkItem("A spec",
    			new YWorkItemID(new YIdentifier(), "task4321"), false, false);
    	item.setStatus( YWorkItem.Status.IsParent );
    	item = new YWorkItem("A spec",
    			new YWorkItemID(new YIdentifier(), "task4321"), false, false);
    	item.setStatus( YWorkItem.Status.Fired );
    	
    	assertTrue( "" + _workitemRepository.getParentWorkItems(),
    			_workitemRepository.getParentWorkItems().size() == 2 );
    }
    
    public void testGetCompletedItems() throws YPersistenceException {
    	assertTrue( "" + _workitemRepository.getCompletedWorkItems().size(),
    			_workitemRepository.getCompletedWorkItems().size() == 0 );
    	
    	// create a couple work items
    	YWorkItem item = new YWorkItem("A spec",
    			new YWorkItemID(new YIdentifier(), "task4321"), false, false);
    	item.setStatus( YWorkItem.Status.Complete );
    	item = new YWorkItem("A spec",
    			new YWorkItemID(new YIdentifier(), "task4321"), false, false);
    	item.setStatus( YWorkItem.Status.Cancelled );
    	item = new YWorkItem("A spec",
    			new YWorkItemID(new YIdentifier(), "task4321"), false, false);
    	item.setStatus( YWorkItem.Status.IsParent );
    	item = new YWorkItem("A spec",
    			new YWorkItemID(new YIdentifier(), "task4321"), false, false);
    	item.setStatus( YWorkItem.Status.Complete );
    	
    	assertTrue( "" + _workitemRepository.getCompletedWorkItems(),
    			_workitemRepository.getCompletedWorkItems().size() == 2 );
    }
    
    public void testRemoveWorkItemsForNullCase() {
    	try {
    		_workitemRepository.removeWorkItemsForCase( null );
    		fail( "An exception should have been thrown." );
    	}
    	catch( IllegalArgumentException e ) {
    		// proper exception was thrown
    	}
    }

    public void testChildren() {
        Set enabledItems = _workitemRepository.getFiredWorkItems();
        Iterator iter = _parentWorkItem.getChildren().iterator();
        while (iter.hasNext()) {
            YWorkItem child = (YWorkItem) iter.next();
            assertEquals(_parentWorkItem, child.getParent());
        }
    }
}
