package org.yawlfoundation.yawl.engine;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.jdom.JDOMException;
import org.yawlfoundation.yawl.elements.YCondition;
import org.yawlfoundation.yawl.elements.YExternalNetElement;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 6/06/2003
 * Time: 12:28:34
 * 
 */
public class TestEngineSystem2 extends TestCase {
    private YIdentifier _idForBottomNet;
//    private YIdentifier _idForTopNet;
    private YWorkItemRepository _workItemRepository;
    private int _sleepTime = 100;
    private YNetRunner _netRunner;
    private YEngine _engine;
    private YSpecification _specification;

    public TestEngineSystem2(String name) {
        super(name);
    }


    public void setUp() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException {
        URL fileURL = getClass().getResource("YAWL_Specification4.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        _specification = YMarshal.
                unmarshalSpecifications(StringUtil.fileToString(yawlXMLFile.getAbsolutePath())).get(0);

        _engine = YEngine.getInstance();
        _workItemRepository = _engine.getWorkItemRepository();
    }




    public void testMultimergeNets() throws YDataStateException, YStateException, YEngineStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        synchronized(this){
        EngineClearer.clear(_engine);
        _engine.loadSpecification(_specification);
        YIdentifier id = _engine.startCase(_specification.getSpecificationID(), null,
                null, null, new YLogDataItemList(), null, false);
        _netRunner = _engine._netRunnerRepository.get(id);
        try {
            //enabled btop
            Set currWorkItems = _workItemRepository.getEnabledWorkItems();
            YWorkItem anItem = (YWorkItem) currWorkItems.iterator().next();
            assertTrue("currWorkItems "+ currWorkItems +
                    " anItem.getTaskID() " + anItem.getTaskID(),
                    currWorkItems.size() == 1 && anItem.getTaskID().equals("b-top"));
            Thread.sleep(_sleepTime);
            //fire btop
            anItem = _engine.startWorkItem(anItem, _engine.getExternalClient("admin"));
            assertTrue(
                    "Expected b-top: got: " + anItem.getTaskID(),
                    anItem.getTaskID().equals("b-top"));
            assertTrue(anItem.getCaseID().toString().indexOf(".")>0);

            currWorkItems = _workItemRepository.getEnabledWorkItems();
            assertTrue(currWorkItems.isEmpty());
            currWorkItems = _workItemRepository.getExecutingWorkItems();
            assertTrue("" + currWorkItems.size(), currWorkItems.size() == 1);
            anItem = (YWorkItem) currWorkItems.iterator().next();
            Thread.sleep(_sleepTime);
            //complete btop
            _engine.completeWorkItem(anItem, "<data/>", null, YEngine.WorkItemCompletion.Normal);
            //c-top and d-top are enabled - fire one
            assertTrue(_workItemRepository.getEnabledWorkItems().size() == 2);
            while(_workItemRepository.getEnabledWorkItems().size() > 1){
                anItem = (YWorkItem) _workItemRepository.getEnabledWorkItems().iterator().next();
                assertTrue(anItem.getTaskID(), anItem.getTaskID().equals("c-top") || anItem.getTaskID().equals("d-top"));
                anItem = _engine.startWorkItem(anItem, _engine.getExternalClient("admin"));
                assertTrue(anItem != null);
            }
            assertTrue(_workItemRepository.getExecutingWorkItems().size() == 1);
            assertTrue(containsItemForTask("c-top", _workItemRepository.getExecutingWorkItems())
                    || containsItemForTask("d-top", _workItemRepository.getExecutingWorkItems()));
            //complete it
            while(_workItemRepository.getExecutingWorkItems().size() > 0){
                anItem = (YWorkItem) _workItemRepository.getExecutingWorkItems().iterator().next();
                _engine.completeWorkItem(anItem, "<data/>", null, YEngine.WorkItemCompletion.Normal);
            }
            assertTrue(_workItemRepository.getWorkItems().size() == 2);
            //now e-top is enabled and either c-top or d-top are enabled
            assertTrue(containsItemForTask("e-top", _workItemRepository.getEnabledWorkItems())
                    &&
                    (containsItemForTask("c-top", _workItemRepository.getEnabledWorkItems())
                    || containsItemForTask("d-top", _workItemRepository.getEnabledWorkItems()))
                    );
            _idForBottomNet = (YIdentifier)
                    _netRunner.getCaseID().getChildren().iterator().next();
            anItem = _workItemRepository.get(_idForBottomNet.toString(), "e-top");
            //asert that we can start e-top
            YWorkItem eTp = _engine.getWorkItem(
                    _idForBottomNet.toString() +
                    ":" + "e-top");
            assertTrue(eTp != null);
            _engine.startWorkItem(eTp, _engine.getExternalClient("admin"));
            assertTrue(_workItemRepository.getExecutingWorkItems().size() == 1);

            YNetRunner bottomNetRunner = _engine.getNetRunner(_idForBottomNet);
            assertNotNull(_idForBottomNet);
            assertNotNull(bottomNetRunner);
            while(_workItemRepository.getExecutingWorkItems().size() > 0){
                anItem = _workItemRepository.getExecutingWorkItems().iterator().next();
                _engine.completeWorkItem(anItem, "<data/>", null, YEngine.WorkItemCompletion.Normal);
            }
            Thread.sleep(1000);
            YNetRunner bottomNetRunner2 = _engine.getNetRunner(_idForBottomNet);
            assertNull(bottomNetRunner2);
//System.out.println("idforbottomnet " + _idForBottomNet + " netrunners: "+ _workItemRepository._caseToNetRunnerMap);
            assertTrue("locations " + _idForBottomNet.getLocations(), bottomNetRunner.isCompleted());
            assertFalse(_netRunner.isAlive());
//            assertTrue(bottomNetRunner.isAlive());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        _workItemRepository.clear();
        }
    }




    private boolean containsItemForTask(String taskID, Set workItems){
        for (Iterator iterator = workItems.iterator(); iterator.hasNext();) {
            YWorkItem item = (YWorkItem) iterator.next();
            if(item.getTaskID().equals(taskID)){
                return true;
            }
        }
        return false;
    }



    public void testMultimergeWorkItems() throws YDataStateException, YEngineStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        synchronized(this){
        EngineClearer.clear(_engine);
        _engine.loadSpecification(_specification);
            YIdentifier id = _engine.startCase(_specification.getSpecificationID(), null,
                    null, null, new YLogDataItemList(), null, false);
           _netRunner = _engine._netRunnerRepository.get(id);
        try {
            //enabled btop
            Set currWorkItems = _workItemRepository.getEnabledWorkItems();
            YWorkItem anItem = (YWorkItem) currWorkItems.iterator().next();
            assertTrue(currWorkItems.size() == 1 && anItem.getTaskID().equals("b-top"));
            Thread.sleep(_sleepTime);
            //fire btop
            anItem = _engine.startWorkItem(anItem, _engine.getExternalClient("admin"));
            assertTrue(anItem != null);
            currWorkItems = _workItemRepository.getEnabledWorkItems();
            assertTrue(currWorkItems.isEmpty());
            currWorkItems = _workItemRepository.getExecutingWorkItems();
            assertTrue("" + currWorkItems.size(), currWorkItems.size() == 1);
            anItem = (YWorkItem) currWorkItems.iterator().next();
            Thread.sleep(_sleepTime);
            //complete btop
            _engine.completeWorkItem(anItem, "<data/>", null, YEngine.WorkItemCompletion.Normal);

            //c-top and d-top are enabled - fire both
            assertTrue(_workItemRepository.getEnabledWorkItems().size() == 2);
            while(_workItemRepository.getEnabledWorkItems().size() > 0){
                anItem = (YWorkItem) _workItemRepository.getEnabledWorkItems().iterator().next();
                assertTrue(anItem.getTaskID(), anItem.getTaskID().equals("c-top") || anItem.getTaskID().equals("d-top"));
                anItem = _engine.startWorkItem(anItem, _engine.getExternalClient("admin"));
                assertNotNull(anItem);
            }
            assertTrue(_workItemRepository.getExecutingWorkItems().size() == 2);
            assertTrue(containsItemForTask("c-top", _workItemRepository.getExecutingWorkItems())
                    && containsItemForTask("d-top", _workItemRepository.getExecutingWorkItems()));
            //complete both
            while(_workItemRepository.getExecutingWorkItems().size() > 0){
                anItem = (YWorkItem) _workItemRepository.getExecutingWorkItems().iterator().next();
                _engine.completeWorkItem(anItem, "<data/>", null, YEngine.WorkItemCompletion.Normal);
            }
            assertTrue(_workItemRepository.getWorkItems().size() == 1);
            //now e-top is enabled once for two tokens
            assertTrue(containsItemForTask("e-top", _workItemRepository.getEnabledWorkItems()));
            _idForBottomNet = (YIdentifier)
                    _netRunner.getCaseID().getChildren().iterator().next();
            YNetRunner bottomNetRunner = _engine.getNetRunner(_idForBottomNet);
            Collection eTopPreset = bottomNetRunner.getNetElement("e-top").getPresetElements();
            for (Iterator iterator = eTopPreset.iterator(); iterator.hasNext();) {
                YCondition ec = (YCondition) iterator.next();
                assertTrue(ec.containsIdentifier());
            }
            anItem = _workItemRepository.get(_idForBottomNet.toString(), "e-top");
            //assert that we can start e-top
            YWorkItem eTop = _engine.getWorkItem(
                _idForBottomNet.toString() +
                ":" + "e-top");
            assertNotNull(eTop);
            _engine.startWorkItem(eTop, _engine.getExternalClient("admin"));
            assertTrue(_workItemRepository.getExecutingWorkItems().size() == 1);

            YNetRunner netRunner = _engine.getNetRunner(_idForBottomNet);
            while(_workItemRepository.getExecutingWorkItems().size() > 0){
                anItem = _workItemRepository.getExecutingWorkItems().iterator().next();
                String caseIdStr = anItem.getCaseID().toString();
                String taskID    = anItem.getTaskID();
                _engine.completeWorkItem(anItem, "<data/>", null, YEngine.WorkItemCompletion.Normal);
            }

            Iterator eTopPresetIterator = eTopPreset.iterator();
            YCondition ec1, ec2;
            ec1 = (YCondition)eTopPresetIterator.next();
            ec2 = (YCondition)eTopPresetIterator.next();
            Thread.sleep(1000);
            assertTrue(netRunner.isCompleted());    //remove
            assertFalse(netRunner.isAlive());       //remove
            netRunner = _engine.getNetRunner(_idForBottomNet);
            assertNull(netRunner);
            assertTrue(_netRunner.isCompleted());
            assertFalse(_netRunner.isAlive());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        _workItemRepository.clear();
        }
    }




    public void testStateAlignmentBetween_WorkItemRepository_and_Net()
            throws YSchemaBuildingException, YSyntaxException, JDOMException, YEngineStateException, IOException, YAuthenticationException, YDataStateException, YStateException, YQueryException, YPersistenceException {
        URL fileURL = getClass().getResource("TestOrJoin.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        YSpecification specification = null;
        specification = (YSpecification) YMarshal.
                unmarshalSpecifications(StringUtil.fileToString(yawlXMLFile.getAbsolutePath())).get(0);
        _engine = YEngine.getInstance();
        EngineClearer.clear(_engine);
        _engine.loadSpecification(specification);
        YIdentifier caseID = _engine.startCase(specification.getSpecificationID(), null,
                null, null, new YLogDataItemList(), null, false);
           {
            YWorkItem itemA = _engine.getAvailableWorkItems().iterator().next();
            _engine.startWorkItem(itemA, _engine.getExternalClient("admin"));

            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            itemA = (YWorkItem) _engine.getChildrenOfWorkItem(
                    itemA).iterator().next();
            _engine.completeWorkItem(itemA, "<data/>", null, YEngine.WorkItemCompletion.Normal);
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        {
            YWorkItem itemF = null;
            Iterator it = _engine.getAvailableWorkItems().iterator();
            while (it.hasNext()) {
                YWorkItem item = (YWorkItem) it.next();
                if (item.getTaskID().equals("F")) {
                    itemF = item;
                    break;
                }
            }
            _engine.startWorkItem(itemF, _engine.getExternalClient("admin"));
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            itemF = (YWorkItem) _engine.getChildrenOfWorkItem(itemF).iterator().next();
            _engine.completeWorkItem(itemF, "<data/>", null, YEngine.WorkItemCompletion.Normal);
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        {
            YWorkItem itemB = null;
            Iterator it = _engine.getAvailableWorkItems().iterator();
            while (it.hasNext()) {
                YWorkItem item = (YWorkItem) it.next();
                if (item.getTaskID().equals("B")) {
                    itemB = item;
                    break;
                }
            }
            _engine.startWorkItem(itemB, _engine.getExternalClient("admin"));
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            itemB = (YWorkItem) _engine.getChildrenOfWorkItem(itemB).iterator().next();
            _engine.completeWorkItem(itemB, "<data/>", null, YEngine.WorkItemCompletion.Normal);
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        {
            YWorkItem itemA = (YWorkItem) _engine.getAvailableWorkItems()
                    .iterator().next();
            //get the real net elements that contain the identifier tokens.
            List locations = caseID.getLocations();
            for (int i = 0; i < locations.size(); i++) {
                YExternalNetElement element = (YExternalNetElement) locations.get(i);
                if (element instanceof YTask) {
                    YTask task = (YTask) element;
                    Set preset = task.getPresetElements();
                    Set items = _engine.getAvailableWorkItems();
                    //If the task is enabled we test that the work items reflect this.
                    if (task.t_enabled(caseID)) {
                        boolean tokenFound = false;
                        //Check that the conitions in the preset confirm the enabled
                        //state of the task.
                        for (Iterator presetIter = preset.iterator(); presetIter.hasNext();) {
                            YCondition condition = (YCondition) presetIter.next();
                            if (condition.containsIdentifier()) {
                                tokenFound = true;
                            }
                        }
                        assertTrue("State misaligment found: task [" +
                                task + "] is enabled but has no tokens in " +
                                "its preset2.", tokenFound);
                        //Check that there is one work enabled work item to match the enabled
                        //state of the task.
                        boolean workItemFound = false;
                        for (Iterator iterator = items.iterator(); iterator.hasNext();) {
                            YWorkItem anItem = (YWorkItem) iterator.next();
                            if(anItem.getTaskID().equals(task.getID())){
                                if(! anItem.getStatus().equals(YWorkItemStatus.statusEnabled)){
                                    fail("The work item [" + anItem + "] " +
                                            "should be enabled because the corresponding " +
                                            "task is enabled but this wokr item is in " +
                                            "the state : [" + anItem.getStatus() + "]");
                                }
                                workItemFound = true;
                            }
                            assertTrue("State misaligment found: task [" +
                                task + "] is enabled but there is no corresponding" +
                                    " work item found.", workItemFound);
                        }
                    //if the task is busy we test if the work items reflect this.
                    }else if (task.t_isBusy()) {
                        boolean tokenFound = false;
                        //Check that the locations of the id confirm the busy state of the task
                        List idlocs = caseID.getLocations();
                        for (Iterator idlocsIter = idlocs.iterator(); idlocsIter.hasNext();) {
                            YExternalNetElement netElem = (YExternalNetElement) idlocsIter.next();
                            if (netElem.getID().equals(task.getID())) {
                                tokenFound = true;
                            }
                        }
                        assertTrue("State misaligment found: task [" +
                                task + "] is busy but there is no token inside it.",
                                tokenFound);
                        //Check that there is at least one fired or executing work item
                        //to match the busy state of the task.
                        int workItemCount = 0;
                        for (Iterator iterator = items.iterator(); iterator.hasNext();) {
                            YWorkItem anItem = (YWorkItem) iterator.next();
                            if(anItem.getTaskID().equals(task.getID())){
                                if(! (anItem.getStatus().equals(YWorkItemStatus.statusFired)
                                ||  anItem.getStatus().equals(YWorkItemStatus.statusExecuting)
                                ||  anItem.getStatus().equals(YWorkItemStatus.statusIsParent))){
                                    fail("The work item [" + anItem + "] " +
                                            "should be fired, executing, or isParent " +
                                            "because the corresponding " +
                                            "task is busy but this wokr item is in " +
                                            "the state : [" + anItem.getStatus() + "]");
                                }
                                workItemCount++;
                            }
                            assertTrue("State misaligment found: task [" +
                                task + "] is enabled but there is no corresponding" +
                                    " work item found.", workItemCount > 1);
                        }
                    }
                    //the task must be inactive, we test if the work items reflect this.
                    else {
                        boolean tokenNotFound = true;
                        //Check that the locations of the id confirm the
                        //task is indeed not busy
                        List idlocs = caseID.getLocations();
                        for (Iterator idlocsIter = idlocs.iterator(); idlocsIter.hasNext();) {
                            YExternalNetElement netElem = (YExternalNetElement) idlocsIter.next();
                            if (netElem.getID().equals(task.getID())) {
                                tokenNotFound = false;
                            }
                        }
                        assertTrue("State misaligment found: task [" +
                                task + "] is inactive but there is a token inside it.",
                                tokenNotFound);
                        //Check that there are no work items
                        //because the task is meant to be not enabled and not busy.
                        int workItemCount = 0;
                        for (Iterator iterator = items.iterator(); iterator.hasNext();) {
                            YWorkItem anItem = (YWorkItem) iterator.next();
                            if(anItem.getTaskID().equals(task.getID())){
                                workItemCount++;
                            }
                            assertTrue("State misaligment found: task [" +
                                task + "] is not busy and not enabled but " +
                                    "a work item found.", workItemCount == 0);
                        }
                   }
                }
            }
        }
    }


    public static void main(String args[]) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestEngineSystem2.class);
        return suite;
    }
}
