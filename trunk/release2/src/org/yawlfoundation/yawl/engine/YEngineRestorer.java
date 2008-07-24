/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.engine;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.elements.state.YInternalCondition;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.logging.YCaseEvent;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.engine.time.YWorkItemTimer;
import org.yawlfoundation.yawl.engine.time.YTimer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Handles the restoration of persisted objects and data pertaining to the Engine
 *
 * @author: Michael Adams
 * Creation Date: 25/06/2008
 */

public class YEngineRestorer {

    private YEngine _engine ;
    private YPersistenceManager _pmgr ;
    private Hashtable<String, YIdentifier> _idLookupTable;
    private Vector<YNetRunner> _runners;
    private Logger _log;


    // CONSTRUCTORS //

    public YEngineRestorer() {}

    public YEngineRestorer(YEngine engine, YPersistenceManager pmgr) {
        _engine = engine ;
        _pmgr = pmgr ;
        _idLookupTable = new Hashtable<String, YIdentifier>();
        _log = Logger.getLogger(this.getClass());
    }


    /**
     * Restores YAWL Services from persistence
     *
     * @throws YPersistenceException if there's a problem reading from the tables
     */
    public void restoreYAWLServices() throws YPersistenceException {
        _log.info("Restoring Services - Starts");
        Query query = _pmgr.createQuery("from YAWLServiceReference");

        for (Iterator it = query.iterate(); it.hasNext();) {
            YAWLServiceReference service = (YAWLServiceReference) it.next();
            _engine.addYawlService(service);
            if (service.get_serviceName().equals("resourceService"))
                _engine.setResourceService(service);
        }
        _log.info("Restoring Services - Ends");
    }


    /**
     * Restores Specifications from persistence
     *
     * @throws YPersistenceException if there's a problem reading from the tables
     */
    public void restoreSpecifications() throws YPersistenceException {
        _log.info("Restoring Specifications - Starts");
        Query query = _pmgr.createQuery("from YSpecFile");

        for (Iterator it = query.iterate(); it.hasNext();) {
            YSpecFile spec = (YSpecFile) it.next();
            String xml = spec.getXML();
            _log.debug("Restoring specification " + spec.getSpecid().getId());
            File f = null;
            try {
                f = File.createTempFile("yawltemp", null);
                BufferedWriter buf = new BufferedWriter(new FileWriter(f));
                buf.write(xml, 0, xml.length());
                buf.close();
                addSpecifications(spec.getSpecid().getId(), f.getAbsolutePath());
            }
            catch (IOException ioe) {
                throw new YPersistenceException("IOException creating temp file when " +
                                                "restoring specifications.");
            }
            finally {
                if (f != null) f.delete();
            }
        }
        _log.info("Restoring Specifications - Ends");
    }


    /**
     * Restores the next available case number from persistence
     *
     * @return a YCaseNbrStore object initialised to the next available case number
     * @throws YPersistenceException if there's a problem reading from the tables
     */
    public YCaseNbrStore restoreNextAvailableCaseNumber() throws YPersistenceException {
        YCaseNbrStore caseNbrStore = YCaseNbrStore.getInstance();
        Query query = _pmgr.createQuery("from YCaseNbrStore");
        if ((query != null) && (! query.list().isEmpty())) {
            caseNbrStore = (YCaseNbrStore) query.iterate().next();
            caseNbrStore.setPersisted(true);               // flag to update only
        }
        else {

            // secondary attempt: eg. if there's no case number stored (as will be
            // the case if this is the first restart after upgrade to v2.0)
            query = _pmgr.createQuery("from YCaseEvent as yce " +
                    "where yce._eventName = 'started' order by yce._eventTime desc");
            if ((query != null) && (! query.list().isEmpty())) {
                YCaseEvent caseEvent = (YCaseEvent) query.iterate().next();

                // only want integral case numbers
                caseNbrStore.setCaseNbr(new Double(caseEvent.get_caseID()).intValue());
            }
        }

        // persisting flag must be reset as it is not itself persisted
        caseNbrStore.setPersisting(true);

        return caseNbrStore ;
    }


    public void restoreProcessInstances() throws YPersistenceException {
        _log.info("Restoring process instances - Starts");
        Query query = _pmgr.createQuery("from YNetRunner order by case_id");

        _runners = new Vector<YNetRunner>();
        for (Iterator it = query.iterate(); it.hasNext();) {
            _runners.add((YNetRunner) it.next());
        }

        _runners = removeDeadRunners(_runners);
        restoreRunners(_runners) ;
        _log.info("Restoring process instances - Ends");
    }


    public void restoreWorkItems() throws YPersistenceException {
        _log.info("Restoring work items - Starts");
        List<YWorkItem> toBeRestored = new ArrayList<YWorkItem>();
        List<YWorkItem> toBeRemoved = new ArrayList<YWorkItem>();

        // get workitems from persistence
        Query query = _pmgr.createQuery("from YWorkItem");
        for (Iterator it = query.iterate(); it.hasNext();) {
            YWorkItem witem = (YWorkItem) it.next();
            if (hasRestoredIdentifier(witem)) 
               toBeRestored.add(witem);
            else
               toBeRemoved.add(witem);
        }

        List<YWorkItem> orphans = checkWorkItemFamiliesIntact(toBeRestored);
        toBeRestored.removeAll(orphans);
        toBeRemoved.addAll(orphans);

        for (YWorkItem witem : toBeRestored) {

            // persisted data stored as string - restore to Element
            String data = witem.get_dataString();
            if (data != null) witem.setInitData(JDOMUtil.stringToElement(data));

            // reconstruct the caseID-YIdentifier for this item
            String[] caseTaskSplit = witem.get_thisID().split(":");
            String[] taskUniqueSplit = caseTaskSplit[1].split("!");
            String caseID = caseTaskSplit[0];
            String taskID = taskUniqueSplit[0];
            String uniqueID = taskUniqueSplit[1];

            YIdentifier yCaseID = _idLookupTable.get(caseID);

            // MJF: use the unique id if we have one - stays in synch
            if (uniqueID != null) {
                witem.setWorkItemID(new YWorkItemID(yCaseID, taskID, uniqueID));
            } else {
                witem.setWorkItemID(new YWorkItemID(yCaseID, taskID));
            }
            witem.addToRepository();

            // MJF: for any work items with data, restore to netrunner instance
            witem.restoreDataToNet();
        }

        removeWorkItems(toBeRemoved);

        _log.info("Restoring work items - Ends");
    }


    public Set<YWorkItemTimer> restoreWorkItemTimers() throws YPersistenceException {
        _log.info("Restoring work item timers - Starts");
        Set<YWorkItemTimer> expiredTimers = new HashSet<YWorkItemTimer>();
        Query query = _pmgr.createQuery("from YWorkItemTimer");
        for (Iterator it = query.iterate(); it.hasNext();) {
            YWorkItemTimer witemTimer = (YWorkItemTimer) it.next();
            witemTimer.setPersisting(true);
            
            // check to see if workitem still exists
            YWorkItem witem = _engine.getWorkItem(witemTimer.getOwnerID()) ;
            if (witem == null)
                _engine.deleteObject(witemTimer) ;          // remove from persistence
            else {
                 long endTime = witemTimer.getEndTime();

                // if the deadline has passed, time the workitem out
                if (endTime < System.currentTimeMillis())
                    expiredTimers.add(witemTimer);
                else {
                    // reschedule the workitem's timer
                    YTimer.getInstance().schedule(witemTimer, new Date(endTime));
                    witem.setTimerStarted(true);
                }
            }
        }
        _log.info("Restoring work item timers - Ends");
        return expiredTimers;
    }

    
    public void restartRestoredProcessInstances() throws YPersistenceException {
        /*
          Start net runners. This is a restart of a NetRunner not a clean start,
          therefore, the net runner should not create any new work items, if they
          have already been created.
         */
        _log.info("Restarting restored process instances - Starts");

        for (int i = 0; i < _runners.size(); i++) {
            YNetRunner runner = _runners.get(i);
            _log.debug("Restarting " + runner.get_caseID());
            try {
                runner.start(_pmgr);
            }
            catch (Exception e) {
                throw new YPersistenceException(e.getMessage());
            }
        }
        _log.info("Restarting restored process instances - Ends");

    }

    /*****************************************************************************/

    private YSpecification getSpecification(YNetRunner runner) {
        return _engine.getSpecification(runner.getYNetID(), runner.getYNetVersion());
    }


    private List<YSpecificationID> addSpecifications(String specID, String uri)
            throws YPersistenceException {
        try {
            return _engine.addSpecifications(new File(uri), true, new Vector());
        } catch (Exception e) {
            throw new YPersistenceException("Failure whilst restoring specification [" +
                    specID + "]", e);
        }
    }


    private Vector<YNetRunner> removeDeadRunners(Vector<YNetRunner> runners)
            throws YPersistenceException {
        Vector<YNetRunner> result = new Vector<YNetRunner>();

        for (YNetRunner runner : runners) {
            if (getSpecification(runner) != null) {
                result.add(runner) ;
            }
            else {
                /* This occurs when a specification has been unloaded, but the case is
                   still there. This case is removed, since we must have the
                   specification stored as well. */
                String msg = String.format("YEngineRestorer: The specification '%s' for" +
                         " active case '%s' is not loaded; the active case cannot" +
                         " continue and so has been removed", runner.getYNetID(),
                         runner.get_standin_caseIDForNet().toString());
                _log.warn(msg);
                _pmgr.deleteObject(runner);
            }
        }

        return result ;
    }


    private Hashtable<String, YNetRunner> restoreNets(Vector<YNetRunner> runners)
            throws YPersistenceException {
        Hashtable<String, YNetRunner> result = new Hashtable<String, YNetRunner>();

        for (YNetRunner runner : runners) {
            runner.setEngine(_engine);       // Set engine for parent and composite nets
            if (runner.getContainingTaskID() == null) {

                //This is a root net runner
                YNet net = (YNet) getSpecification(runner).getRootNet().clone();
                runner.setNet(net);
                result.put(runner.get_standin_caseIDForNet().toString(), runner);
            }
            else {

                //This is not a root net, but a decomposition
                // Find the parent runner
                String runnerID = runner.get_standin_caseIDForNet().toString();
                String parentID = runnerID.substring(0, runnerID.lastIndexOf("."));
                YNetRunner parentrunner = result.get(parentID);
                if (parentrunner != null) {
                    _log.debug("Restoring composite YNetRunner: " + parentID);
                    YNet parentnet = parentrunner.getNet();
                    YCompositeTask task = (YCompositeTask) parentnet.getNetElement(
                                                           runner.getContainingTaskID());
                    runner.setContainingTask(task);
                    try {
                        YNet net = (YNet) task.getDecompositionPrototype().clone();
                        runner.setNet(net);
                    }
                    catch (CloneNotSupportedException cnse) {
                        String msg = String.format("YEngineRestorer: The decomposition" +
                                     "'%s' for  active case '%s' could not be set." +
                                     task.getDecompositionPrototype().getID(),
                                     runner.get_standin_caseIDForNet().toString());
                        throw new YPersistenceException(msg);
                    }
                    result.put(runner.get_standin_caseIDForNet().toString(), runner);
                }
            }
        }
        return result ;
    }
    

    private void restoreRunners(Vector<YNetRunner> runners) 
            throws YPersistenceException {

        Hashtable<String, YNetRunner> runnerMap = restoreNets(runners) ;
        for (YNetRunner runner : runners) {
            YNet net = runner.getNet();
            P_YIdentifier pid = runner.get_standin_caseIDForNet();

            if (runner.getContainingTaskID() == null) {

                // This is a root net runner
                YIdentifier id = restoreYIdentifier(runnerMap, pid, null, net);
                runner.set_caseIDForNet(id);
                _engine.addRunner(runner);
            }

            YIdentifier yid = new YIdentifier(runner.get_caseID());
            YWorkItemRepository.getInstance().setNetRunnerToCaseIDBinding(runner, yid);

            Set<String> busytasks = runner.getBusyTaskNames();
            for (String busytask : busytasks) {
                runner.addBusyTask(net.getNetElement(busytask));
            }

            Set<String> enabledtasks = runner.getEnabledTaskNames();
            for (String enabledtask : enabledtasks) {
                YExternalNetElement element = net.getNetElement(enabledtask);
                if (element instanceof YTask) {
                    YTask externalTask = (YTask) element;
                    runner.addEnabledTask(externalTask);
                }
            }

            // restore case & exception observers (where they exist)
            runner.restoreObservers();
        }
    }


    public YIdentifier restoreYIdentifier(Hashtable<String, YNetRunner> runnermap,
                                         P_YIdentifier pid, YIdentifier parent, YNet net)
            throws YPersistenceException {

        YIdentifier id = new YIdentifier(pid.toString());
        YNet sendnet = net;
        id.set_parent(parent);
        List list = pid.get_children();

        if (list.size() > 0) {
            List<YIdentifier> idlist = new Vector<YIdentifier>();

            for (int i = 0; i < list.size(); i++) {
                P_YIdentifier child = (P_YIdentifier) list.get(i);
                YNetRunner netRunner = runnermap.get(child.toString());
                if (netRunner != null) {
                    sendnet = netRunner.getNet();
                }
                YIdentifier caseid = restoreYIdentifier(runnermap, child, id, sendnet);

                if (netRunner != null) {
                    netRunner.set_caseIDForNet(caseid);
                }
                idlist.add(caseid);
            }
            id.set_children(idlist);
        }

        YTask task;
        for (int i = 0; i < pid.getLocationNames().size(); i++) {
            String name = (String) pid.getLocationNames().get(i);
            YExternalNetElement element = net.getNetElement(name);

            if (element == null) {
                name = name.substring(0, name.length() - 1);     // remove trailling ']'
                String[] splitname = name.split(":");

                // Get the task associated with this condition
                if (name.indexOf("CompositeTask") != -1) {
                    YNetRunner runner = runnermap.get(parent.toString());
                    task = (YTask) runner.getNet().getNetElement(splitname[1]);
                }
                else {
                    task = (YTask) net.getNetElement(splitname[1]);
                }

                // Check if we need to find the parent task and post conditions against it
                if ((task == null) && (parent != null)) {
                    YNetRunner runner = runnermap.get(parent.toString());
                    Object obj = runner.getNet().getNetElement(splitname[1]);
                    if (obj instanceof YTask) {
                        task = (YTask) obj;
                    }
                }

                postTaskCondition(task, net, splitname[0], id) ;

            }
            else {
                if (element instanceof YTask) {
                    task = (YTask) element;
                    task.setI(id);
                    task.prepareDataDocsForTaskOutput();
                    id.addLocation(_pmgr, task);
                }
                else if (element instanceof YCondition) {
                   ((YConditionInterface) element).add(_pmgr, id);
                }
            }
        }

        _idLookupTable.put(id.toString(), id);
        return id;
    }


    private void postTaskCondition(YTask task, YNet net, String condName, YIdentifier id)
            throws YPersistenceException{
        if (task != null) {
            _log.debug("Posting conditions on task " + task);
            YInternalCondition condition = null;
            if (condName.startsWith(YInternalCondition._mi_active)) {
                condition = task.getMIActive();
            }
            else if (condName.startsWith(YInternalCondition._mi_complete)) {
                condition = task.getMIComplete();
            }
            else if (condName.startsWith(YInternalCondition._mi_entered)) {
                condition = task.getMIEntered();
            }
            else if (condName.startsWith(YInternalCondition._executing)) {
                condition = task.getMIExecuting();
            }
            else {
                _log.error("Unknown YInternalCondition state");
            }
            if (condition != null) condition.add(_pmgr, id);
        }
        else {
            if (condName.startsWith("InputCondition")) {
                net.getInputCondition().add(_pmgr, id);
            }
            else if (condName.startsWith("OutputCondition")) {
                net.getOutputCondition().add(_pmgr, id);
            }
        }
    }


    /**
     * Checks if a workitem restored from persistence has had its YIdentifier
     * previously restored.
     * @param item the workitem to check
     * @return true if there has been a YIdentifier restored for the workitem
     */
    private boolean hasRestoredIdentifier(YWorkItem item) {
        String[] caseTaskSplit = item.get_thisID().split(":");
        return _idLookupTable.get(caseTaskSplit[0]) != null;        
    }


    /**
     * When restoring workitems, this method checks (1) if a parent workitem is in the
     * list of items to restore, all of its children are in the list also; and (2) each
     * child workitem in the list has a parent. If either is false, the workitem is
     * put in a list of items to not be restored and to be removed from persistence
     * @param itemList the list of workitems to potentially restore
     * @return the sublist of items not to restore (if any)
     */
    private List<YWorkItem> checkWorkItemFamiliesIntact(List<YWorkItem> itemList) {
        List<YWorkItem> orphans = new ArrayList<YWorkItem>();
        for (YWorkItem witem : itemList) {
            if (witem.getStatus().equals(YWorkItemStatus.statusIsParent)) {
                Set<YWorkItem> children = witem.getChildren();
                if ((children != null) && (! itemList.containsAll(children))) {
                    orphans.add(witem);
                }
            }
            else {
                YWorkItem parent = witem.getParent();
                if ((parent != null) && (! itemList.contains(parent))) {
                    orphans.add(witem);
                }
            }
        }
        return orphans ;
    }


    /**
     * Removes the workitems in the list from persistence
     * @param items the workitems to remove
     */
    private void removeWorkItems(List<YWorkItem> items) {
        try {

            // clear child items first (to avoid foreign key constraint exceptions)
            for (YWorkItem item : items) {
                if (! item.getStatus().equals(YWorkItemStatus.statusIsParent))
                    _pmgr.deleteObject(item);
            }

            // now clear any parents
            for (YWorkItem item : items) {
                if (item.getStatus().equals(YWorkItemStatus.statusIsParent))
                    _pmgr.deleteObject(item);
            }
        }
        catch (YPersistenceException ype) {
            _log.error("Exception removing orphaned workitems from persistence.", ype);
        }

    }

}
