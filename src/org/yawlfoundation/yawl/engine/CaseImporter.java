/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.exceptions.YEngineStateException;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.util.*;

/**
 * @author Michael Adams
 * @date 2/10/18
 */
public class CaseImporter {

    private final YEngine _engine;
    private final Logger _log = LogManager.getLogger(this.getClass());

    public CaseImporter(YEngine engine) {
        _engine = engine;
    }


    public int add(String caseListXML) throws YEngineStateException, YPersistenceException {
        XNode root = parse(caseListXML);
        List<YNetRunner> runners = new ArrayList<YNetRunner>();
        List<YWorkItem> workitems = new ArrayList<YWorkItem>();
        for (XNode caseNode : root.getChildren()) {
            runners.addAll(makeRunnerList(caseNode));
            workitems.addAll(makeWorkItemList(caseNode));
        }
        Collections.sort(runners, new RunnerComparator());

        importAndStart(runners, workitems);

        return root.getChildCount();
    }


    private XNode parse(String xml) throws YEngineStateException {
        XNode root = new XNodeParser().parse(xml);
        if (root == null) {
            throw new YEngineStateException("Invalid xml for import of case(s)");
        }
        return root;
    }


    private void importAndStart(List<YNetRunner> runners, List<YWorkItem> workitems)
            throws YPersistenceException {
        YEngineRestorer restorer = new YEngineRestorer(_engine, YEngine._pmgr);
        restorer.setImportingCases(true);
        restorer.restoreProcessInstances(runners);
        restorer.restoreWorkItems(workitems);
        restorer.restartRestoredProcessInstances();
    }


    private List<YNetRunner> makeRunnerList(XNode caseNode) throws YEngineStateException {
        XNode nRunnerList = caseNode.getChild("runners");
        if (nRunnerList == null) {
            throw new YEngineStateException("No net runners found to import for case");
        }
        List<YNetRunner> allRunners = new ArrayList<YNetRunner>();
        List<YIdentifier> parents = new ArrayList<YIdentifier>();
        Map<String, Set<YIdentifier>> parentChildMap =
                new HashMap<String, Set<YIdentifier>>();
        for (XNode nRunner : nRunnerList.getChildren()) {
            YNetRunner runner = makeRunner(nRunner);
            allRunners.add(runner);
            String parentID = nRunner.getChildText("parent");
            if (parentID == null) {
                parents.add(runner.getCaseID());
            }
            else {
                addChild(parentChildMap, parentID, runner.getCaseID());
            }
        }
        reuniteIdentifiers(parents, parentChildMap);
        return allRunners;
    }


    private YNetRunner makeRunner(XNode nRunner) throws YEngineStateException {
        YIdentifier caseID = makeIdentifier(nRunner.getChild("identifier"));
        YNetRunner runner = new YNetRunner();
        runner.set_caseID(caseID.toString());
        runner.set_caseIDForNet(caseID);
        runner.setContainingTaskID(nRunner.getChildText("containingtask"));
        runner.setSpecificationID(makeSpecID(nRunner.getChild("specificationid")));
        runner.setStartTime(StringUtil.strToLong(nRunner.getChildText("starttime"),0));
        runner.setExecutionStatus(nRunner.getChildText("executionstatus"));
        runner.setNetData(makeNetData(caseID.toString(), nRunner.getChild("netdata")));
        runner.set_caseObserverStr(nRunner.getChildText("observer"));
        runner.setEnabledTaskNames(toSet(nRunner.getChild("enabledtasks")));
        runner.setBusyTaskNames(toSet(nRunner.getChild("busytasks")));
        runner.set_timerStates(makeTimerStates(nRunner.getChild("timerstates")));
        return runner;
    }


    private List<YWorkItem> makeWorkItemList(XNode caseNode) throws YEngineStateException {
        XNode nWIList = caseNode.getChild("workitems");
        if (nWIList == null) {
            throw new YEngineStateException("No workitems found to import for case");
        }
        Map<String, Set<YWorkItem>> parentChildMap = new HashMap<String, Set<YWorkItem>>();
        Set<YWorkItem> parents = new HashSet<YWorkItem>();
        List<YWorkItem> allItems = new ArrayList<YWorkItem>();
        for (XNode nItem : nWIList.getChildren()) {
            YWorkItem item = makeWorkItem(nItem);
            allItems.add(item);
            if (item.isParent()) {
                parents.add(item);
            }
            else {
                String pid = nItem.getChildText("parent");
                addChild(parentChildMap, pid, item);
            }
        }
        reuniteItems(parents, parentChildMap);
        return allItems;
    }


    private YWorkItem makeWorkItem(XNode nItem) throws YEngineStateException {
        YWorkItem item = new YWorkItem();
        item.set_thisID(nItem.getChildText("id"));
        setSpecID(item, nItem);
        setTimestamps(item, nItem);
        setData(item, nItem);
        item.set_status(nItem.getChildText("status"));
        item.set_prevStatus(nItem.getChildText("prevstatus"));
        item.set_externalClient(nItem.getChildText("client"));
        item.set_allowsDynamicCreation(getBoolean(nItem.getChildText("allowsdynamic")));
        item.setRequiresManualResourcing(getBoolean(nItem.getChildText("manualresourcing")));
        item.setTimerStarted(getBoolean(nItem.getChildText("timerstarted")));
        item.setTimerExpiry(getLong(nItem.getChildText("timerexpiry")));
        item.setCodelet(nItem.getChildText("codelet"));
        item.set_deferredChoiceGroupID(nItem.getChildText("deferredgroupid"));
        return item;
    }


    private <T> void addChild(Map<String, Set<T>> parentChildMap, String parentID, T child) {
        if (parentID != null) {
            Set<T> children = parentChildMap.get(parentID);
            if (children == null) {
                children = new HashSet<T>();
                parentChildMap.put(parentID, children);
            }
            children.add(child);
        }
    }


    private void reuniteItems(Set<YWorkItem> parents,
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
    

    private void reuniteIdentifiers(List<YIdentifier> parents,
                         Map<String, Set<YIdentifier>> parentChildMap) {
        for (YIdentifier parent : parents) {
            String pid = parent.getId();
            Set<YIdentifier> children = parentChildMap.get(pid);
            if (children != null) {
                parent.set_children(new Vector<YIdentifier>(children));
                for (YIdentifier child : children) {
                    child.set_parent(parent);
                }
            }
        }
    }


    private YSpecificationID makeSpecID(XNode specNode) throws YEngineStateException {
        if (specNode == null) {
            throw new YEngineStateException("Null specification for case");
        }
        YSpecificationID id = new YSpecificationID(specNode);
        if (_engine.getSpecification(id) == null) {
            throw new YEngineStateException("Specification for case is not loaded");
        }
        return id;
    }


    private YNetData makeNetData(String caseID, XNode dataNode) {
        XNode innerNode = dataNode.getChild();
        String data = innerNode != null ? innerNode.toString() : null;
        YNetData netData = new YNetData(caseID);
        netData.setData(data);
        return netData;
    }


    private Map<String,String> makeTimerStates(XNode nStates) {
        Map<String,String> stateMap = new HashMap<String, String>();
        for (XNode nState : nStates.getChildren()) {
             stateMap.put(nState.getChildText("taskName"),
                     nState.getChildText("state"));
        }
        return stateMap;
    }


    private YIdentifier makeIdentifier(XNode nIdentifier) {
        YIdentifier id = new YIdentifier(nIdentifier.getAttributeValue("id"));
        XNode nLocations = nIdentifier.getChild("locations");
        List<String> locations = new ArrayList<String>();
        for (XNode nLocation : nLocations.getChildren()) {
            locations.add(nLocation.getText());
        }
        id.setLocationNames(locations);

        XNode nChildren = nIdentifier.getChild("children");
        if (nChildren != null) {
            List<YIdentifier> list = new ArrayList<YIdentifier>();
            for (XNode nChild : nChildren.getChildren()) {
                YIdentifier childID = makeIdentifier(nChild);
                childID.set_parent(id);
                list.add(childID);
            }
            id.set_children(list);
        }
        return id;
    }


    private void setSpecID(YWorkItem item, XNode nItem) throws YEngineStateException {
        YSpecificationID specID = makeSpecID(nItem.getChild("specificationid"));
        item.set_specIdentifier(specID.getIdentifier());
        item.set_specUri(specID.getUri());
        item.set_specVersion(specID.getVersionAsString());
    }


    private void setTimestamps(YWorkItem item, XNode nItem) {
        Date timestamp = makeDate(nItem.getChildText("enablement"));
        if (timestamp != null) item.set_enablementTime(timestamp);
        timestamp = makeDate(nItem.getChildText("firing"));
        if (timestamp != null) item.set_firingTime(timestamp);
        timestamp = makeDate(nItem.getChildText("start"));
        if (timestamp != null) item.set_startTime(timestamp);
    }


    private void setData(YWorkItem item, XNode nItem) {
        XNode nData = nItem.getChild("data");
        if (nData != null && nData.hasChildren()) {
            item.set_dataString(nData.getChild(0).toString());
        }
    }


    private Date makeDate(String timeStr) {
        return ! (timeStr == null || "0".equals(timeStr)) ?
                new Date(StringUtil.strToLong(timeStr, 0)) : null;
    }


    private boolean getBoolean(String bValue) {
        return "true".equalsIgnoreCase(bValue);
    }


    private long getLong(String lvalue) {
        return StringUtil.strToLong(lvalue,0);
    }


    private Set<String> toSet(XNode node) {
        Set<String> set = new HashSet<String>();
        for (XNode child : node.getChildren()) {
            set.add(child.getText());
        }
        return set;
    }


    class RunnerComparator implements Comparator<YNetRunner> {

        @Override
        public int compare(YNetRunner r1, YNetRunner r2) {
            return r1.getCaseID().toString().compareTo(r2.getCaseID().toString());
        }
    }


}
