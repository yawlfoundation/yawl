package org.yawlfoundation.yawl.stateless.monitor;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.engine.YNetData;
import org.yawlfoundation.yawl.engine.YWorkItemStatus;
import org.yawlfoundation.yawl.exceptions.YStateException;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.stateless.elements.*;
import org.yawlfoundation.yawl.stateless.elements.marking.YIdentifier;
import org.yawlfoundation.yawl.stateless.elements.marking.YInternalCondition;
import org.yawlfoundation.yawl.stateless.engine.YAnnouncer;
import org.yawlfoundation.yawl.stateless.engine.YNetRunner;
import org.yawlfoundation.yawl.stateless.engine.YWorkItem;
import org.yawlfoundation.yawl.stateless.engine.YWorkItemID;
import org.yawlfoundation.yawl.stateless.engine.time.YWorkItemTimer;
import org.yawlfoundation.yawl.stateless.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.util.*;

/**
 * @author Michael Adams
 * @date 30/6/2022
 */
public class YCaseImporter {

    private static final Namespace YAWL_NAMESPACE = Namespace.getNamespace("yawl",
            "http://www.yawlfoundation.org/yawlschema");

    private final Map<String, YIdentifier> _idLookupTable = new HashMap<>();
    private final Set<YWorkItem> _timedItems = new HashSet<>();
    private YSpecification _spec;


    public YCaseImporter() { }


    public List<YNetRunner> unmarshal(String caseXML, YAnnouncer announcer)
            throws YStateException, YSyntaxException {
        Document doc = JDOMUtil.stringToDocument(caseXML);
        Element root = doc.getRootElement();
        _spec = unmarshalSpecification(root);
        List<YNetRunner> runners = unmarshalRunners(root);
        List<YWorkItem> workitems = unmarshalWorkItems(root);
        runners.sort(new RunnerComparator());
        attachRunners(runners, announcer);
        restoreWorkItems(workitems, runners);
        return runners;
    }


    private YSpecification unmarshalSpecification(Element root) throws YSyntaxException {
        Element specNode = root.getChild("specificationSet", YAWL_NAMESPACE);
        List<YSpecification> specList = YMarshal.unmarshalSpecifications(
                JDOMUtil.elementToString(specNode));
        if (specList.isEmpty()) {
            throw new YSyntaxException("Failed to unmarshal specification");
        }
        return specList.get(0);   // only going to be one
    }


    private List<YNetRunner> unmarshalRunners(Element caseNode) throws YStateException {
        Element nRunnerList = caseNode.getChild("runners");
        if (nRunnerList == null) {
            throw new YStateException("No net runners found to import for case");
        }
        List<YNetRunner> allRunners = new ArrayList<>();
        List<YIdentifier> parents = new ArrayList<>();
        Map<String, Set<YIdentifier>> parentChildMap = new HashMap<>();
        for (Element nRunner : nRunnerList.getChildren()) {
            YNetRunner runner = unmarshalRunner(nRunner);
            allRunners.add(runner);
            String parentID = nRunner.getChildText("parent");
            if (StringUtil.isNullOrEmpty(parentID)) {
                parents.add(runner.getCaseID());
            }
            else {
                addChild(parentChildMap, parentID, runner.getCaseID());
            }
        }
        reuniteRunnerFamilies(parents, parentChildMap);
        return allRunners;
    }


    private YNetRunner unmarshalRunner(Element nRunner) throws YStateException {
        YIdentifier caseID = unmarshalIdentifier(nRunner.getChild("identifier"));
        YNetRunner runner = new YNetRunner();
        runner.set_caseIDForNet(caseID);
        runner.setContainingTaskID(getChildText(nRunner, "containingtask"));
        runner.setSpecificationID(_spec.getSpecificationID());
        runner.setStartTime(StringUtil.strToLong(nRunner.getChildText("starttime"),0));
        runner.setExecutionStatus(getChildText(nRunner, "executionstatus"));
        runner.setNetData(unmarshalNetData(caseID.toString(), nRunner.getChild("netdata")));
        runner.setEnabledTaskNames(toSet(nRunner.getChild("enabledtasks")));
        runner.setBusyTaskNames(toSet(nRunner.getChild("busytasks")));
        runner.set_timerStates(unmarshalTimerStates(nRunner.getChild("timerstates")));
        return runner;
    }


    private List<YWorkItem> unmarshalWorkItems(Element caseNode) throws YStateException {
        Map<String, Set<YWorkItem>> parentChildMap = new HashMap<>();
        Set<YWorkItem> parents = new HashSet<>();
        List<YWorkItem> allItems = new ArrayList<>();
        for (Element runnerNode : caseNode.getChild("runners").getChildren()) {
            Element nWIList = runnerNode.getChild("workitems");
            if (nWIList != null) {
                for (Element nItem : nWIList.getChildren()) {
                    YWorkItem item = unmarshalWorkItem(nItem);
                    allItems.add(item);
                    if (item.isParent()) {
                        parents.add(item);
                    }
                    else {
                        String pid = nItem.getChildText("parent");
                        addChild(parentChildMap, pid, item);
                    }
                }
            }
        }
        reuniteWorkItemFamilies(parents, parentChildMap);
        return allItems;
    }


    private YWorkItem unmarshalWorkItem(Element nItem) throws YStateException {
        YWorkItem item = new YWorkItem();
        item.set_thisID(nItem.getChildText("id"));
        item.setSpecID(_spec.getSpecificationID());
        unmarshalTimestamps(item, nItem);
        unmarshalWorkItemData(item, nItem);
        item.set_status(getChildText(nItem, "status"));
        item.set_prevStatus(getChildText(nItem, "prevstatus"));
        item.set_allowsDynamicCreation(toBoolean(nItem.getChildText("allowsdynamic")));
        item.setRequiresManualResourcing(toBoolean(nItem.getChildText("manualresourcing")));
        item.setTimerParameters(hydrateTimerParameters(nItem));
        if (toBoolean(nItem.getChildText("timerstarted"))) {
            _timedItems.add(item);
        }
        item.setTimerExpiry(toLong(getChildText(nItem,"timerexpiry")));
        item.setCodelet(getChildText(nItem, "codelet"));
        item.set_deferredChoiceGroupID(getChildText(nItem, "deferredgroupid"));
        return item;
    }

    
    private <T> void addChild(Map<String, Set<T>> parentChildMap, String parentID, T child) {
        if (parentID != null) {
            Set<T> children = parentChildMap.computeIfAbsent(parentID, k -> new HashSet<T>());
            children.add(child);
        }
    }


    private void reuniteRunnerFamilies(List<YIdentifier> parents,
                                       Map<String, Set<YIdentifier>> parentChildMap) {
        for (YIdentifier parent : parents) {
            String pid = parent.getId();
            Set<YIdentifier> children = parentChildMap.get(pid);
            if (children != null) {
                parent.setChildren(new ArrayList<>(children));
                for (YIdentifier child : children) {
                    child.set_parent(parent);
                }
            }
        }
    }


    private void reuniteWorkItemFamilies(Set<YWorkItem> parents,
                                         Map<String, Set<YWorkItem>> parentChildMap) {
        for (YWorkItem parent : parents) {
            String pid = parent.get_thisID();
            Set<YWorkItem> children = parentChildMap.get(pid);
            if (children != null) {
                parent.setChildren(children);
                for (YWorkItem child : children) {
                    child.set_parent(parent);
                }
            }
        }
    }


    private YIdentifier unmarshalIdentifier(Element nIdentifier) {
        YIdentifier id = new YIdentifier(nIdentifier.getAttributeValue("id"));
        Element nLocations = nIdentifier.getChild("locations");
        List<String> locations = new ArrayList<String>();
        for (Element nLocation : nLocations.getChildren()) {
            locations.add(nLocation.getText());
        }
        id.setLocationNames(locations);

        Element nChildren = nIdentifier.getChild("children");
        List<YIdentifier> list = new ArrayList<>();
        for (Element nChild : nChildren.getChildren()) {
            YIdentifier childID = unmarshalIdentifier(nChild);
            childID.set_parent(id);
            list.add(childID);
        }
        id.setChildren(list);

        return id;
    }


    private YNetData unmarshalNetData(String caseID, Element dataNode) {
        String data = dataNode.getText();
        YNetData netData = new YNetData(caseID);
        netData.setData(data);
        return netData;
    }


    private void unmarshalWorkItemData(YWorkItem item, Element nItem) {
        Element nData = nItem.getChild("data");
        if (! (nData == null || nData.getText().isEmpty())) {
            item.setDataElement(JDOMUtil.stringToElement(JDOMUtil.decodeEscapes(nData.getText())));
        }
    }


    private Map<String, String> unmarshalTimerStates(Element nStates) {
        Map<String,String> stateMap = new HashMap<>();
        for (Element nState : nStates.getChildren()) {
             stateMap.put(nState.getChildText("taskname"),
                     nState.getChildText("state"));
        }
        return stateMap;
    }


    private void unmarshalTimestamps(YWorkItem item, Element nItem) {
         Date timestamp = toDate(getChildText(nItem, "enablement"));
         if (timestamp != null) item.set_enablementTime(timestamp);
         timestamp = toDate(getChildText(nItem, "firing"));
         if (timestamp != null) item.set_firingTime(timestamp);
         timestamp = toDate(getChildText(nItem, "start"));
         if (timestamp != null) item.set_startTime(timestamp);
     }


    private Date toDate(String timeStr) {
        return ! (timeStr == null || "0".equals(timeStr)) ?
                new Date(StringUtil.strToLong(timeStr, 0)) : null;
    }


    private Set<String> toSet(Element node) {
        Set<String> set = new HashSet<>();
        for (Element child : node.getChildren()) {
            set.add(child.getText());
        }
        return set;
    }

    private boolean toBoolean(String bValue) {
        return "true".equalsIgnoreCase(bValue);
    }

    private long toLong(String lvalue) {
        return StringUtil.strToLong(lvalue,0);
    }


    class RunnerComparator implements Comparator<YNetRunner> {

        @Override
        public int compare(YNetRunner r1, YNetRunner r2) {
            return r1.getCaseID().toString().compareTo(r2.getCaseID().toString());
        }
    }


    /*******************************************************************************/

    private void attachRunners(List<YNetRunner> runners, YAnnouncer announcer)
            throws YStateException {
        Map<String, YNetRunner> runnerMap = attachNets(runners);
        for (YNetRunner runner : runners) {
            runner.setAnnouncer(announcer);
            YNet net = runner.getNet();
            if (runner.getContainingTaskID() == null) {

                // This is a root net runner
                attachNetIdentifiers(runnerMap, runner.getCaseID(), null, net);
//                _engine.addRunner(runner);
//            } else {
//                _engine.getNetRunnerRepository().add(runner);         // a subnet
            }

            // restore enabled and busy tasks
            for (String busytask : runner.getBusyTaskNames()) {
                YTask task = (YTask) net.getNetElement(busytask);
                runner.addBusyTask(task);
                task.setNetRunner(runner);
            }
            for (String enabledtask : runner.getEnabledTaskNames()) {
                YTask task = (YTask) net.getNetElement(enabledtask);
                runner.addEnabledTask(task);
                task.setNetRunner(runner);
            }

            // restore any timer variables
            runner.restoreTimerStates();

            // create a clean announcement transport
            runner.refreshAnnouncements();
        }
    }


    private Map<String, YNetRunner> attachNets(List<YNetRunner> runners)
            throws YStateException {
        Map<String, YNetRunner> result = new HashMap<>();

        // restore all root nets first
        for (YNetRunner runner : runners) {
            if (runner.getContainingTaskID() == null) { // this is a root net runner
                YNet net = (YNet) _spec.getRootNet().clone();
                if (net == null) {
                    throw new YStateException("Invalid specification"); 
                }
                runner.setNet(net);
                result.put(runner.getCaseID().toString(), runner);
            }
        }

        // now the sub nets
        for (YNetRunner runner : runners) {
            if (runner.getContainingTaskID() != null) {

                // Find the parent runner
                String runnerID = runner.getCaseID().toString();
                String parentID = runnerID.substring(0, runnerID.lastIndexOf("."));
                YNetRunner parentrunner = result.get(parentID);
                if (parentrunner != null) {
                    YNet parentnet = parentrunner.getNet();
                    YCompositeTask task = (YCompositeTask) parentnet.getNetElement(
                            runner.getContainingTaskID());
                    runner.setContainingTask(task);
                    try {
                        YNet net = (YNet) task.getDecompositionPrototype().clone();
                        runner.setNet(net);
                    }
                    catch (CloneNotSupportedException cnse) {
                        String msg = String.format("The decomposition" +
                                "'%s' for  active case '%s' could not be set." +
                                task.getDecompositionPrototype().getID(),
                                runner.getCaseID().toString());
                        throw new YStateException(msg);
                    }
                    result.put(runner.getCaseID().toString(), runner);
                }
            }
        }
        return result;
    }


    protected YIdentifier attachNetIdentifiers(Map<String, YNetRunner> runnermap,
                                               YIdentifier id, YIdentifier parent, YNet net)
            throws YStateException {

        id.set_parent(parent);

        for (YIdentifier child : id.getChildren()) {
            if (child != null) {
                YNetRunner netRunner = runnermap.get(child.toString());
                YNet runnerNet = netRunner != null ? netRunner.getNet() : net;
                YIdentifier caseid = attachNetIdentifiers(runnermap, child, id, runnerNet);

                if (netRunner != null) {
                    netRunner.set_caseIDForNet(caseid);
                }
            }
        }
        return restoreLocations(runnermap, id, parent, net);
    }


    protected YIdentifier restoreLocations(Map<String, YNetRunner> runnermap,
                                           YIdentifier id, YIdentifier parent, YNet net)
            throws YStateException {

        YTask task;
        YNetRunner runner = null;

        // make external list of locations to avoid concurrency exceptions
        List<String> locationNames = new ArrayList<>(id.getLocationNames());
        id.clearLocations();                         // locations are readded below

        for (String name : locationNames) {
            YExternalNetElement element = net.getNetElement(name);

            if (element == null) {
                name = name.substring(0, name.length() - 1);     // remove trailling ']'
                String[] splitname = name.split(":");

                if (parent != null) {
                    runner = runnermap.get(parent.toString());
                }

                // Get the task associated with this condition
                if (name.contains("CompositeTask")) {
                    task = (YTask) runner.getNet().getNetElement(splitname[1]);
                } else {
                    task = (YTask) net.getNetElement(splitname[1]);
                }

                postTaskCondition(task, net, splitname[0], id);
            } else {
                if (element instanceof YTask) {
                    task = (YTask) element;
                    task.setI(id);
                    task.prepareDataDocsForTaskOutput();
                    id.addLocation(task);
                } else if (element instanceof YCondition) {
                    ((YConditionInterface) element).add(id);
                }
            }
        }

        _idLookupTable.put(id.toString(), id);
        return id;
    }

    private void postTaskCondition(YTask task, YNet net, String condName,
                                   YIdentifier id) throws YStateException {
        if (task != null) {
            YInternalCondition condition = null;
            if (condName.startsWith(YInternalCondition._mi_active)) {
                condition = task.getMIActive();
            } else if (condName.startsWith(YInternalCondition._mi_complete)) {
                condition = task.getMIComplete();
            } else if (condName.startsWith(YInternalCondition._mi_entered)) {
                condition = task.getMIEntered();
            } else if (condName.startsWith(YInternalCondition._mi_executing)) {
                condition = task.getMIExecuting();
            } else {
                throw new YStateException("Unknown YInternalCondition state");
            }

            if (condition != null) condition.add(id);
        }
        else {
            if (condName.startsWith("InputCondition")) {
                net.getInputCondition().add(id);
            }
            else if (condName.startsWith("OutputCondition")) {
                net.getOutputCondition().add(id);
            }
        }
    }


    protected void restoreWorkItems(List<YWorkItem> workItems, List<YNetRunner> runners) {
        List<YWorkItem> toBeRestored = new ArrayList<>();

        for (YWorkItem witem : workItems) {
            if (hasRestoredIdentifier(witem)) {
                toBeRestored.add(witem);
            }
        }

        List<YWorkItem> orphans = checkWorkItemFamiliesIntact(toBeRestored);
        toBeRestored.removeAll(orphans);

        for (YWorkItem witem : toBeRestored) {

            // reconstruct the caseID-YIdentifier for this item
            String id = witem.get_thisID();
            int delim1 = id.indexOf(':');
            int delim2 = id.indexOf('!');
            String caseID = id.substring(0, delim1);
            String taskID;
            String uniqueID = null;
            if (delim2 > -1) {
                taskID = id.substring(delim1 + 1, delim2);
                uniqueID = id.substring(delim2 + 1);
            }
            else {
                taskID = id.substring(delim1 + 1);
            }

            YIdentifier yCaseID = _idLookupTable.get(caseID);

            // MJF: use the unique id if we have one - stays in synch
            if (uniqueID != null) {
                witem.setWorkItemID(new YWorkItemID(yCaseID, taskID, uniqueID));
            }
            else {
                witem.setWorkItemID(new YWorkItemID(yCaseID, taskID));
            }

            for (YNetRunner runner : runners) {
                for (YTask task : runner.getActiveTasks()) {
                    if (task.getID().equals(taskID)) {
                        witem.setTask(task);
                        break;
                    }
                }
            }
            witem.addToRepository();
        }
        restartWorkItemTimers();
    }


    private void restartWorkItemTimers() {
        for (YWorkItem item : _timedItems) {
            item.setTimerStarted(true);
            if ("Active".equals(item.getTimerStatus())) {
                item.setTimer(new YWorkItemTimer(item, new Date(item.getTimerExpiry())));
            }
        }
    }

    
    private boolean hasRestoredIdentifier(YWorkItem item) {
        String[] caseTaskSplit = item.get_thisID().split(":");
        return _idLookupTable.get(caseTaskSplit[0]) != null;
    }


    /**
     * When restoring workitems, this method checks (1) if a parent workitem is in the
     * list of items to restore, all of its children are in the list also; and (2) each
     * child workitem in the list has a parent. If either is false, the workitem is
     * put in a list of items to not be restored and to be removed from persistence
     *
     * @param itemList the list of workitems to potentially restore
     * @return the sublist of items not to restore (if any)
     */
    private List<YWorkItem> checkWorkItemFamiliesIntact(List<YWorkItem> itemList) {
        List<YWorkItem> orphans = new ArrayList<>();
        for (YWorkItem witem : itemList) {
            if (witem.getStatus().equals(YWorkItemStatus.statusIsParent)) {
                Set<YWorkItem> children = witem.getChildren();
                if ((children != null) && (!new HashSet<>(itemList).containsAll(children))) {
                    orphans.add(witem);
                }
            }
            else {
                YWorkItem parent = witem.getParent();
                if ((parent != null) && (!itemList.contains(parent))) {
                    orphans.add(witem);
                }
            }
        }
        return orphans;
    }


    /**
     * Gets the YTask task id passed.
     *
     * @param taskID the task ID
     * @return the task reference
     */
    private YTask getTask(String taskID) {
        for (YDecomposition decomposition : _spec.getDecompositions()) {
            if (decomposition instanceof YNet) {
                YNet net = (YNet) decomposition;
                YExternalNetElement element = net.getNetElement(taskID);
                if (element instanceof YTask) {
                    return (YTask) element;
                }
            }
        }
        return null;
    }


    private YTimerParameters hydrateTimerParameters(Element node) {
        Element timerElement = node.getChild("timerparameters");
        if (timerElement != null) {
            XNode timerNode = new XNodeParser().parse(JDOMUtil.elementToString(timerElement));
            return new YTimerParameters().fromXNode(timerNode);
        }
        return null;
    }


    // JDOM returns "" if child text is missing, we want nulls
    private String getChildText(Element e, String name) {
        String text = e.getChildText(name);
        return StringUtil.isNullOrEmpty(text) ? null : text;
    }
    
}
