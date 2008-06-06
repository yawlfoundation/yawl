/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine;

import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import junit.framework.TestCase;

import java.util.Iterator;
import java.util.Set;

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
        YWorkItemID workItemID = new YWorkItemID(identifier, "task-123");
        _parentWorkItem = new YWorkItem(null, new YSpecificationID("ASpecID", 0.1), workItemID, false, false);
        for (int i = 0; i < 5; i++) {
            _parentWorkItem.createChild(null, identifier.createChild(null));
        }
    }


    public void testGetItem() throws YPersistenceException {
        assertTrue(_workitemRepository.getEnabledWorkItems().size() == 0);
        new YWorkItem(null, new YSpecificationID("ASpecID", 0.1), new YWorkItemID(new YIdentifier(), "task4321"), false, false);
        assertEquals(
                _workitemRepository.getWorkItem(
                        _parentWorkItem.getCaseID().toString(), _parentWorkItem.getTaskID()),
                _parentWorkItem);
        _workitemRepository.removeWorkItemFamily(_parentWorkItem);
        assertNull(_workitemRepository.getWorkItem(_parentWorkItem.getCaseID().toString(), _parentWorkItem.getTaskID()));
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
