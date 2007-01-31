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
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.YEngine;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.AbstractTransactionalTestCase;
import au.edu.qut.yawl.persistence.dao.AbstractHibernateDAOTestCase;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 30/05/2003
 * Time: 15:32:26
 * 
 */
public class TestYWorkItemRepository extends AbstractTransactionalTestCase {
    private YWorkItemRepository _workitemRepository;
    private YWorkItem _parentWorkItem;
    private YEngine _engine;

    public TestYWorkItemRepository() {
        super();
    }


    public void setUp() throws Exception {
    	super.setUp();
        _engine =  EngineFactory.createYEngine();
        _workitemRepository = YWorkItemRepository.getInstance();
//        _workitemRepository.clear();
        YIdentifier identifier = new YIdentifier();
        _engine.getDao().save(identifier);
//        YIdentifier.saveIdentifier( identifier, null, null );
//        YWorkItemID workItemID = new YWorkItemID(identifier, "task-123");
        _parentWorkItem = new YWorkItem("ASpecID", identifier, "task-123", false, false);
        _engine.getDao().save(_parentWorkItem);
//        YWorkItem.saveWorkItem( _parentWorkItem );
        for (int i = 0; i < 5; i++) {
        	YIdentifier childIdentifier = identifier.createChild();
        	_engine.getDao().save(childIdentifier);
            YWorkItem childWorkItem = _parentWorkItem.createChild(childIdentifier);
            _engine.getDao().save(childWorkItem);
        }
        _engine.getDao().save(_parentWorkItem);
    }

    public void testGetItem() throws YPersistenceException {
        assertTrue(_workitemRepository.getEnabledWorkItems().size() == 0);
        new YWorkItem("A spec", new YIdentifier(), "task4321", false, false);
        assertEquals(
                _workitemRepository.getWorkItem(
                        _parentWorkItem.getCaseID().toString(), _parentWorkItem.getTaskID()),
                _parentWorkItem);
        _workitemRepository.removeWorkItemFamily(_parentWorkItem);
        assertNull(_workitemRepository.getWorkItem(_parentWorkItem.getCaseID().toString(), _parentWorkItem.getTaskID()));
    }

    public void testGetParentItems() throws YPersistenceException {
//    	_workitemRepository.clear();
    	int prevCount = _workitemRepository.getParentWorkItems().size();
//    	assertEquals( "incorrect number of parent work items in repository", 0, _workitemRepository.getParentWorkItems().size());
    	
    	// create a couple work items
    	YIdentifier identifier = new YIdentifier();
    	_engine.getDao().save(identifier);
    	YWorkItem item = new YWorkItem("A spec",
    			identifier, "task4321", false, false);
    	item.setStatus( YWorkItem.Status.IsParent );
    	_engine.getDao().save(item);

    	identifier = new YIdentifier();
    	_engine.getDao().save(identifier);
    	item = new YWorkItem("A spec",
    			identifier, "task4321", false, false);
    	item.setStatus( YWorkItem.Status.Cancelled );
    	_engine.getDao().save(item);
    	
    	identifier = new YIdentifier();
    	_engine.getDao().save(identifier);
    	item = new YWorkItem("A spec",
    			identifier, "task4321", false, false);
    	item.setStatus( YWorkItem.Status.IsParent );
    	_engine.getDao().save(item);

    	identifier = new YIdentifier();
    	_engine.getDao().save(identifier);
    	item = new YWorkItem("A spec",
    			identifier, "task4321", false, false);
    	item.setStatus( YWorkItem.Status.Fired );
    	_engine.getDao().save(item);

    	assertEquals( "repository " + _workitemRepository.getParentWorkItems() + "contains wrong number of items", prevCount + 2, 
    			_workitemRepository.getParentWorkItems().size());
    }
    
    public void testGetCompletedItems() throws YPersistenceException {
    	assertTrue( "" + _workitemRepository.getCompletedWorkItems().size(),
    			_workitemRepository.getCompletedWorkItems().size() == 0 );
    	
    	// create a couple work items
    	YIdentifier identifier = new YIdentifier();
    	_engine.getDao().save(identifier);
    	YWorkItem item = new YWorkItem("A spec",
    			identifier, "task4321", false, false);
    	item.setStatus( YWorkItem.Status.Complete );
    	_engine.getDao().save(item);
    	
    	identifier = new YIdentifier();
    	_engine.getDao().save(identifier);
    	item = new YWorkItem("A spec",
    			identifier, "task4321", false, false);
    	item.setStatus( YWorkItem.Status.Cancelled );
    	_engine.getDao().save(item);

    	identifier = new YIdentifier();
    	_engine.getDao().save(identifier);
    	item = new YWorkItem("A spec",
    			identifier, "task4321", false, false);
    	item.setStatus( YWorkItem.Status.IsParent );
    	_engine.getDao().save(item);

    	identifier = new YIdentifier();
    	_engine.getDao().save(identifier);
    	item = new YWorkItem("A spec",
    			identifier, "task4321", false, false);
    	item.setStatus( YWorkItem.Status.Complete );
    	_engine.getDao().save(item);
    	
    	assertEquals( "incorrect number of completed work items in repository" + _workitemRepository.getCompletedWorkItems(), 2, _workitemRepository.getCompletedWorkItems().size() );
    }
    
    public void testRemoveWorkItemsForNullCase() throws YPersistenceException {
    	try {
    		_workitemRepository.removeWorkItemsForCase( null );
    		fail( "An exception should have been thrown." );
    	}
    	catch( IllegalArgumentException e ) {
    		// proper exception was thrown
    	}
    }

    public void testChildren() throws YPersistenceException {
        Set enabledItems = _workitemRepository.getFiredWorkItems();
        Iterator iter = _parentWorkItem.getChildren().iterator();
        while (iter.hasNext()) {
            YWorkItem child = (YWorkItem) iter.next();
            assertEquals(_parentWorkItem, child.getParent());
        }
    }
}
