/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jdom.JDOMException;

import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.exceptions.YAuthenticationException;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.persistence.AbstractTransactionalTestCase;
import au.edu.qut.yawl.unmarshal.YMarshal;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 6/06/2003
 * Time: 12:28:34
 * 
 */
public class TestEngineSystem2 extends AbstractTransactionalTestCase {
    private YWorkItemRepository _workItemRepository = YWorkItemRepository.getInstance();
    private int _sleepTime = 100;
    private YNetRunner _netRunner;
    private AbstractEngine _engine;
    private YSpecification _specification;
    private File yawlXMLFile;

    public TestEngineSystem2(String name) {
        super(name);
    }

    public void setUp() throws Exception {
    	super.setUp();
        URL fileURL = getClass().getResource("YAWL_Specification4.xml");
        yawlXMLFile = new File(fileURL.getFile());
        _specification = null;

        _specification = (YSpecification) YMarshal.
                unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);

        _engine =  EngineFactory.createYEngine();
    }

    public void testMultimergeNets() throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException, IOException, JDOMException {
        synchronized(this){
        _engine.addSpecifications(yawlXMLFile, true, new ArrayList());
        YIdentifier id = _engine.startCase(null, _specification.getID(), null, null);
        _netRunner = _engine.getNetRunner(id);
        try {
//            YNetRunner _netRunner = _basicEngine2.getNetRunner();
//            YIdentifier _idForBottomNet;
            //enabled btop
            Set currWorkItems = _workItemRepository.getEnabledWorkItems();
            YWorkItem anItem = (YWorkItem) currWorkItems.iterator().next();
            assertTrue("currWorkItems "+ currWorkItems +
                    " anItem.getTaskID() " + anItem.getTaskID(),
                    currWorkItems.size() == 1 && anItem.getTaskID().equals("b-top"));
            Thread.sleep(_sleepTime);
            //fire btop
            anItem = _engine.startWorkItem(anItem.getIDString(), "admin");
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
            _engine.completeWorkItem(anItem.getIDString(), "<data/>", false);
            //c-top and d-top are enabled - fire one
            assertTrue(_workItemRepository.getEnabledWorkItems().size() == 2);
            while(_workItemRepository.getEnabledWorkItems().size() > 1){
                anItem = (YWorkItem) _workItemRepository.getEnabledWorkItems().iterator().next();
                assertTrue(anItem.getTaskID(), anItem.getTaskID().equals("c-top") || anItem.getTaskID().equals("d-top"));
                anItem = _engine.startWorkItem(anItem.getIDString(), "admin");
                assertTrue(anItem != null);
            }
            assertTrue(_workItemRepository.getExecutingWorkItems().size() == 1);
            assertTrue(containsItemForTask("c-top", _workItemRepository.getExecutingWorkItems())
                    || containsItemForTask("d-top", _workItemRepository.getExecutingWorkItems()));
            //complete it
            while(_workItemRepository.getExecutingWorkItems().size() > 0){
                anItem = (YWorkItem) _workItemRepository.getExecutingWorkItems().iterator().next();
                _engine.completeWorkItem(anItem.getIDString(), "<data/>", false);
            }
            assertTrue(_workItemRepository.getWorkItems().size() == 2);
            //now e-top is enabled and either c-top or d-top are enabled
            assertTrue(containsItemForTask("e-top", _workItemRepository.getEnabledWorkItems())
                    &&
                    (containsItemForTask("c-top", _workItemRepository.getEnabledWorkItems())
                    || containsItemForTask("d-top", _workItemRepository.getEnabledWorkItems()))
                    );
            YIdentifier _idForBottomNet =
                    _netRunner.getCaseID().getChildren().iterator().next();
            _workItemRepository.getWorkItem(_idForBottomNet.toString(), "e-top");
            //asert that we can start e-top
            YWorkItem eTp = _engine.getWorkItem(
                    _idForBottomNet.toString() +
                    ":" + "e-top");
            assertTrue(eTp != null);
            _engine.startWorkItem(eTp.getIDString(), "admin");
            assertTrue(_workItemRepository.getExecutingWorkItems().size() == 1);

            YNetRunner bottomNetRunner = _workItemRepository.getNetRunner(
                    _idForBottomNet);
            assertNotNull(_idForBottomNet);
            assertNotNull(bottomNetRunner);
            while(_workItemRepository.getExecutingWorkItems().size() > 0){
                anItem = (YWorkItem) _workItemRepository.getExecutingWorkItems().iterator().next();
                _engine.completeWorkItem(anItem.getIDString(), "<data/>", false);
            }
            Thread.sleep(100);
            YNetRunner bottomNetRunner2 = _workItemRepository.getNetRunner(_idForBottomNet);
            assertNull(bottomNetRunner2);
//System.out.println("idforbottomnet " + _idForBottomNet + " netrunners: "+ _workItemRepository._caseToNetRunnerMap);
            assertTrue("locations " + _idForBottomNet.getLocationsForNet(bottomNetRunner.getNet()), bottomNetRunner.isCompleted());
            assertFalse(_netRunner.isAlive());
//            assertTrue(bottomNetRunner.isAlive());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        _workItemRepository.clear();
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



    public void testMultimergeWorkItems() throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException, IOException, JDOMException {
        synchronized(this){
        _engine.addSpecifications(yawlXMLFile, false, new ArrayList());
        YIdentifier id = _engine.startCase(null, _specification.getID(), null, null);
        _netRunner = _engine.getNetRunner(id);
        try {
//            YNetRunner _netRunner = _basicEngine2.getNetRunner();
//            YIdentifier _idForBottomNet;
            //enabled btop
            Set currWorkItems = _workItemRepository.getEnabledWorkItems();
            YWorkItem anItem = (YWorkItem) currWorkItems.iterator().next();
            assertTrue(currWorkItems.size() == 1 && anItem.getTaskID().equals("b-top"));
            Thread.sleep(_sleepTime);
            //fire btop
            anItem = _engine.startWorkItem(anItem.getIDString(), "admin");
            assertTrue(anItem != null);
            currWorkItems = _workItemRepository.getEnabledWorkItems();
            assertTrue(currWorkItems.isEmpty());
            currWorkItems = _workItemRepository.getExecutingWorkItems();
            assertTrue("" + currWorkItems.size(), currWorkItems.size() == 1);
            anItem = (YWorkItem) currWorkItems.iterator().next();
            Thread.sleep(_sleepTime);
            //complete btop
            _engine.completeWorkItem(anItem.getIDString(), "<data/>", false);

            //c-top and d-top are enabled - fire both
            assertTrue(_workItemRepository.getEnabledWorkItems().size() == 2);
            while(_workItemRepository.getEnabledWorkItems().size() > 0){
                anItem = (YWorkItem) _workItemRepository.getEnabledWorkItems().iterator().next();
                assertTrue(anItem.getTaskID(), anItem.getTaskID().equals("c-top") || anItem.getTaskID().equals("d-top"));
                anItem = _engine.startWorkItem(anItem.getIDString(), "admin");
                assertNotNull(anItem);
            }
            assertTrue(_workItemRepository.getExecutingWorkItems().size() == 2);
            assertTrue(containsItemForTask("c-top", _workItemRepository.getExecutingWorkItems())
                    && containsItemForTask("d-top", _workItemRepository.getExecutingWorkItems()));
            //complete both
            while(_workItemRepository.getExecutingWorkItems().size() > 0){
                anItem = (YWorkItem) _workItemRepository.getExecutingWorkItems().iterator().next();
                _engine.completeWorkItem(anItem.getIDString(), "<data/>", false);
            }
            assertTrue(_workItemRepository.getWorkItems().size() == 1);
            //now e-top is enabled once for two tokens
            assertTrue(containsItemForTask("e-top", _workItemRepository.getEnabledWorkItems()));
            YIdentifier _idForBottomNet = (YIdentifier)
                    _netRunner.getCaseID().getChildren().iterator().next();
            YNetRunner bottomNetRunner = _workItemRepository.getNetRunner(_idForBottomNet);
            Collection eTopPreset = bottomNetRunner.getNetElement("e-top").getPresetElements();
            for (Iterator iterator = eTopPreset.iterator(); iterator.hasNext();) {
                YCondition ec = (YCondition) iterator.next();
                assertTrue(ec.containsIdentifier());
            }
            _workItemRepository.getWorkItem(_idForBottomNet.toString(), "e-top");
            //assert that we can start e-top
            YWorkItem eTop = _engine.getWorkItem(
                _idForBottomNet.toString() +
                ":" + "e-top");
            assertNotNull(eTop);
            _engine.startWorkItem(eTop.getIDString(), "admin");
            assertTrue(_workItemRepository.getExecutingWorkItems().size() == 1);

            YNetRunner netRunner = _workItemRepository.getNetRunner(_idForBottomNet);
            while(_workItemRepository.getExecutingWorkItems().size() > 0){
                anItem = (YWorkItem) _workItemRepository.getExecutingWorkItems().iterator().next();
                _engine.completeWorkItem(anItem.getIDString(), "<data/>", false);
            }

            Thread.sleep(1000);
            assertTrue(netRunner.isCompleted());    //remove
            assertFalse(netRunner.isAlive());       //remove
            netRunner = _workItemRepository.getNetRunner(_idForBottomNet);
            assertNull(netRunner);
//System.out.println("_netRunner locations = " + _netRunner.getCaseID().getLocations());
            assertTrue(_netRunner.isCompleted());
            assertFalse(_netRunner.isAlive());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        _workItemRepository.clear();
        }
    }




    public void testStateAlignmentBetween_WorkItemRepository_and_Net()
            throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException, YAuthenticationException, YDataStateException, YStateException, YQueryException, YPersistenceException {
        URL fileURL = getClass().getResource("TestOrJoin.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        YSpecification specification;
        specification = (YSpecification) YMarshal.
                unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);
        _engine =  EngineFactory.createYEngine();
        _engine.loadSpecification(specification);
        YIdentifier caseID = _engine.startCase(null, specification.getID(), null, null);
        {
            YWorkItem itemA = (YWorkItem) _engine.getAvailableWorkItems().iterator().next();
            _engine.startWorkItem(itemA.getIDString(), "admin");

            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            itemA = (YWorkItem) _engine.getChildrenOfWorkItem(
                    itemA).iterator().next();
            _engine.completeWorkItem(itemA.getIDString(), "<data/>", false);
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
            _engine.startWorkItem(itemF.getIDString(), "admin");
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            itemF = (YWorkItem) _engine.getChildrenOfWorkItem(itemF).iterator().next();
            _engine.completeWorkItem(itemF.getIDString(), "<data/>", false);
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
            _engine.startWorkItem(itemB.getIDString(), "admin");
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            itemB = (YWorkItem) _engine.getChildrenOfWorkItem(itemB).iterator().next();
            _engine.completeWorkItem(itemB.getIDString(), "<data/>", false);
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        {
        	YNetRunner r = _engine.getNetRunner(caseID);
            YWorkItem itemA = (YWorkItem) _engine.getAvailableWorkItems().iterator().next();
            //get the real net elements that contain the identifier tokens.
            List locations = caseID.getLocationsForNet(r.getNet());
            for (int i = 0; i < locations.size(); i++) {
                YExternalNetElement element = (YExternalNetElement) locations.get(i);
                if (element instanceof YTask) {
                    YTask task = (YTask) element;
                    List preset = task.getPresetElements();
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
                                if(! anItem.getStatus().equals(YWorkItem.Status.Enabled)){
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
                        List idlocs = caseID.getLocationsForNet(r.getNet());
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
                                if(! (anItem.getStatus().equals(YWorkItem.Status.Fired)
                                ||  anItem.getStatus().equals(YWorkItem.Status.Executing)
                                ||  anItem.getStatus().equals(YWorkItem.Status.IsParent))){
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
                        List idlocs = caseID.getLocationsForNet(r.getNet());
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