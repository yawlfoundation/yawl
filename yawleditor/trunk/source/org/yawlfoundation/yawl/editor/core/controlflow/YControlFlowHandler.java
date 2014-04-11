/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.core.controlflow;

import org.apache.xerces.util.XMLChar;
import org.yawlfoundation.yawl.editor.core.exception.IllegalIdentifierException;
import org.yawlfoundation.yawl.editor.core.identity.ElementIdentifiers;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.util.XNode;

import java.util.*;

/**
 * @author Michael Adams
 * @date 31/08/12
 */
public class YControlFlowHandler {

    private YSpecification _specification;
    private final ElementIdentifiers _identifiers;

    private static final String BASIC_RESOURCING_SPEC = createBasicResourcingSpec();

    public YControlFlowHandler() { _identifiers = new ElementIdentifiers(); }

    public YControlFlowHandler(YSpecification specification) {
        this();
        setSpecification(specification);
    }

    public void close() {
        _specification = null;
        _identifiers.clear();
    }


    public void setSpecification(YSpecification specification) {
        _specification = specification;
        _identifiers.load(specification);
    }

    public YSpecification getSpecification() { return _specification; }


    /*** Net CRUD ***/

    public int getNetCount() {
        return getNets().size();
    }

    public Set<String> getDecompositionIds() {
        Set<String> ids = new HashSet<String>();
        if (_specification != null) {
            for (YDecomposition decomposition : _specification.getDecompositions()) {
                ids.add(decomposition.getID());
            }
        }
        return ids;
    }


    public Set<YNet> getNets() {
        Set<YNet> nets = new HashSet<YNet>();
        if (_specification != null) {
            for (YDecomposition decomposition : _specification.getDecompositions()) {
                if (decomposition instanceof YNet) nets.add((YNet) decomposition);
            }
        }
        return nets;
    }


    public Set<YNet> getSubNets() {
        if (_specification != null) {
            Set<YNet> nets = getNets();
            nets.remove(getRootNet());
            return nets;
        }
        return Collections.emptySet();
    }


    public YNet createRootNet(String netName)
            throws YControlFlowHandlerException, IllegalIdentifierException {
        checkSpecificationExists();
        YNet root = addNet(netName);
        setRootNet(root);
        return root;
    }

    public YNet getRootNet() {
        return _specification != null ? _specification.getRootNet() : null; }

    public void setRootNet(YNet net) throws YControlFlowHandlerException {
        checkSpecificationExists();
        _specification.setRootNet(net);
    }

    public YNet addNet(String netName)
            throws YControlFlowHandlerException, IllegalIdentifierException {
        checkSpecificationExists();
        YNet net = createNet(netName);
        _specification.addDecomposition(net);
        return net;
    }

    public String addNet(YNet net) throws YControlFlowHandlerException{
        checkSpecificationExists();
        String uniqueID = checkID(net.getID());
        if (! uniqueID.equals(net.getID())) net.setID(uniqueID);
        _specification.addDecomposition(net);
        return uniqueID;
    }

    public YNet getNet(String netName) {
        if (_specification == null) return null;
        YDecomposition decomposition = _specification.getDecomposition(netName);
        return (decomposition instanceof YNet) ? (YNet) decomposition : null;
    }

    public YNet removeNet(String netID) throws YControlFlowHandlerException {
        YNet net = getNet(netID);
        if (net != null) {
            if (net.equals(_specification.getRootNet())) {
                raise("Removing the root net is not allowed");
            }
            return (YNet) _specification.removeDecomposition(netID);
        }
        return null;
    }


    // pre: specification exists (already checked through #addNet)
    private YNet createNet(String netName) throws IllegalIdentifierException {
        if (netName == null) netName = "Net";
        YNet net = new YNet(checkID(netName), _specification);
        net.setInputCondition(new YInputCondition(checkID("InputCondition"), net));
        net.setOutputCondition(new YOutputCondition(checkID("OutputCondition"), net));
        return net;
    }


    /*** task decomposition CRUD ***/

    public YAWLServiceGateway addTaskDecomposition(String name)
            throws YControlFlowHandlerException, IllegalIdentifierException {
        checkSpecificationExists();
        YAWLServiceGateway gateway = new YAWLServiceGateway(
                checkDecompositionID(name), _specification);
        _specification.addDecomposition(gateway);
        return gateway;
    }


    public String addTaskDecomposition(YAWLServiceGateway decomposition)
            throws YControlFlowHandlerException {
        checkSpecificationExists();
        String uniqueID = checkID(decomposition.getID());
        if (! uniqueID.equals(decomposition.getID())) decomposition.setID(uniqueID);
        _specification.addDecomposition(decomposition);
        return uniqueID;
    }


    public YAWLServiceGateway getTaskDecomposition(String name)  {
        try {
            checkSpecificationExists();
            YDecomposition decomposition = _specification.getDecomposition(name);
            return (decomposition instanceof YAWLServiceGateway) ?
                    (YAWLServiceGateway) decomposition : null;
        }
        catch (YControlFlowHandlerException ycfhe) {
            return null;
        }
    }


    public List<YAWLServiceGateway> getTaskDecompositions() {
        try {
            checkSpecificationExists();
            List<YAWLServiceGateway> decompositionList = new ArrayList<YAWLServiceGateway>();
            for (YDecomposition decomposition : _specification.getDecompositions()) {
                if (decomposition instanceof YAWLServiceGateway) {
                    decompositionList.add((YAWLServiceGateway) decomposition);
                }
            }
            return decompositionList;
        }
        catch (YControlFlowHandlerException ycfhe) {
            return Collections.emptyList();
        }
    }


    public YAWLServiceGateway removeTaskDecomposition(String name) {
        if (_specification != null) {
            return (YAWLServiceGateway) _specification.removeDecomposition(name);
        }
        return null;
    }


    public void removeOrphanTaskDecompositions() {
        if (_specification == null) return;

        Set<YDecomposition> orphans = new HashSet<YDecomposition>();
        Set<YAtomicTask> allTasks = getAllAtomicTasks();
        for (YDecomposition decomposition : getTaskDecompositions()) {
            if (isOrphan(decomposition, allTasks)) {
                orphans.add(decomposition);
            }
        }
        for (YDecomposition orphan : orphans) {
            _specification.removeDecomposition(orphan.getID());
        }
    }

     /*** net elements CRUD ***/

    public YCondition addCondition(String netID, String id)
            throws IllegalIdentifierException {
        YNet net = getNet(netID);
        if (net != null) {
            YCondition condition = new YCondition(checkID(id), net);
            net.addNetElement(condition);
            return condition;
        }
        return null;
    }

    public YAtomicTask addAtomicTask(String netID, String id)
            throws IllegalIdentifierException {
        YNet net = getNet(netID);
        if (net != null) {
            YAtomicTask task = new YAtomicTask(checkID(id), YTask._XOR, YTask._AND, net);
            task.setResourcingXML(BASIC_RESOURCING_SPEC);
            net.addNetElement(task);
            return task;
        }
        return null;
    }


    public YAtomicTask addMultipleInstanceAtomicTask(String netID, String id)
            throws IllegalIdentifierException {
        YAtomicTask task = addAtomicTask(netID, id);
        setMultiInstance(task);
        return task;
    }


    public YCompositeTask addCompositeTask(String netID, String id)
            throws IllegalIdentifierException {
        YNet net = getNet(netID);
        if (net != null) {
            YCompositeTask task = new YCompositeTask(
                    checkID(id), YTask._AND, YTask._XOR, net);
            net.addNetElement(task);
            return task;
        }
        return null;
    }


    public YCompositeTask addMultipleInstanceCompositeTask(String netID, String id)
            throws IllegalIdentifierException {
        YCompositeTask task = addCompositeTask(netID, id);
        setMultiInstance(task);
        return task;
    }


    public YCompoundFlow addFlow(String netID, String sourceID, String targetID) {
        YNet net = getNet(netID);
        return net == null ? null : new YCompoundFlow(getNetElement(netID, sourceID),
                       getNetElement(netID, targetID));
    }


    public YExternalNetElement getNetElement(String netID, String id) {
        if (id == null) return null;
        YNet net = getNet(netID);
        return (net != null) ? net.getNetElement(id) : null;
    }


    public YCondition getCondition(String netID, String id) {
        YNet net = getNet(netID);
        if (net != null) {
            YExternalNetElement element = getNetElement(netID, id);
            return (element instanceof YCondition) ? (YCondition) element : null;
        }
        return null;
    }


    public YInputCondition getInputCondition(String netID) {
        YNet net = getNet(netID);
        return (net != null) ? net.getInputCondition() : null;
    }


    public YOutputCondition getOutputCondition(String netID) {
        YNet net = getNet(netID);
        return (net != null) ? net.getOutputCondition() : null;
    }


    public YTask getTask(String netID, String id) {
        YNet net = getNet(netID);
        if (net != null) {
            YExternalNetElement element = getNetElement(netID, id);
            return (element instanceof YTask) ? (YTask) element : null;
        }
        return null;
    }


    public boolean setJoin(String netID, String id, int joinType)
            throws YControlFlowHandlerException {
        return setJoin(getTask(netID, id), joinType);
    }

    public boolean setJoin(YTask task, int joinType) throws YControlFlowHandlerException {
        validateSplitParameters(task, joinType);
        if (task.getJoinType() == joinType) return false;    // nothing to change

        task.setJoinType(joinType);
        return true;
    }


    public boolean setSplit(String netID, String id, int splitType)
            throws YControlFlowHandlerException {
        return setSplit(getTask(netID, id), splitType);
    }

    public boolean setSplit(YTask task, int splitType) throws YControlFlowHandlerException {
        validateSplitParameters(task, splitType);
        int oldType = task.getSplitType();
        if (oldType == splitType) return false;    // nothing to change

        YFlow defaultFlow = getDefaultFlow(task);
        task.setSplitType(splitType);
        int ordering = 0;
        for (YFlow flow : task.getPostsetFlows()) {
            if (splitType == YTask._AND) {
                flow.setEvalOrdering(null);
                flow.setXpathPredicate(null);
                flow.setIsDefaultFlow(false);
            }
            else if (splitType == YTask._XOR) {
                flow.setEvalOrdering(ordering++);
            }
            else {
                flow.setEvalOrdering(null);
            }
            if (oldType == YTask._AND) {
                flow.setXpathPredicate("true()");
            }
        }
        if (oldType == YTask._AND) {
            setDefaultFlow(task, splitType);
        }
        else if (defaultFlow != null) {
            defaultFlow.setXpathPredicate(splitType == YTask._OR ? "true()" : null);
        }
        return true;
    }


    public YAtomicTask getAtomicTask(String netID, String id) {
        YTask task = getTask(netID, id);
        return (task instanceof YAtomicTask) ? (YAtomicTask) task : null;
    }


    public YCompositeTask getCompositeTask(String netID, String id) {
        YTask task = getTask(netID, id);
        return (task instanceof YCompositeTask) ? (YCompositeTask) task : null;
    }


    public YCompoundFlow getFlow(String netID, String sourceID, String targetID) {
        YCompoundFlow flow = null;
        YNet net = getNet(netID);
        if (net != null) {
            YExternalNetElement source = getNetElement(netID, sourceID);
            YExternalNetElement target = getNetElement(netID, targetID);
            if (source == null || target == null) {
                return null;
            }

            // if flow connects two tasks, get the implicit condition between them too
            if ((source instanceof YTask) && (target instanceof YTask)) {
                String implicitID = "c{" + source.getID() + "_" + target.getID() + "}";
                YCondition implicit = getCondition(netID, implicitID);
                YFlow incoming = getFlow(source, implicit);
                YFlow outgoing = getFlow(implicit, target);
                flow = new YCompoundFlow(incoming, implicit, outgoing);
            }
            else flow = new YCompoundFlow(getFlow(source, target));
        }
        return flow;
    }


    public boolean removeNetElement(YExternalNetElement element) {
        return removeNetElement(element.getNet().getID(), element);
    }

    public boolean removeNetElement(String netID, YExternalNetElement element) {
        YNet net = getNet(netID);
        return net != null && net.removeNetElement(element);
    }


    public YCondition removeCondition(String netID, String id) {
        YCondition condition = getCondition(netID, id);
        if (condition != null) removeNetElement(netID, condition);
        return condition;
    }


    public YTask removeTask(String netID, String id) {
        YTask task = getTask(netID, id);
        if (task != null) removeNetElement(netID, task);
        return task;
    }


    public YAtomicTask removeAtomicTask(String netID, String id) {
        YAtomicTask task = getAtomicTask(netID, id);
        if (task != null) removeNetElement(netID, task);
        return task;
    }


    public YCompositeTask removeCompositeTask(String netID, String id) {
        YCompositeTask task = getCompositeTask(netID, id);
        if (task != null) removeNetElement(netID, task);
        return task;
    }


    public YCompoundFlow removeFlow(String netID, String sourceID, String targetID) {
        YCompoundFlow flow = getFlow(netID, sourceID, targetID);
        if (flow != null) {
            flow.detach();
        }
        return flow;
    }


    public boolean setCancellationSet(String netID, String id,
                                   List<YExternalNetElement> newSet) {
        return setCancellationSet(getTask(netID, id), newSet);
    }

    public boolean setCancellationSet(YTask task, List<YExternalNetElement> newSet) {
        if (task != null) {

            // clean-up any existing elements no longer in set
            for (YExternalNetElement element : task.getRemoveSet()) {
                if (! newSet.contains(element)) {
                    task.removeFromRemoveSet(element);
                }
            }
            task.addRemovesTokensFrom(newSet);
        }
        return task != null;
    }


    public ElementIdentifiers getIdentifiers() { return _identifiers; }

    public Map<String, String> rationaliseIdentifiers() {
        return _identifiers.rationaliseIfRequired(_specification);
    }

    public String replaceID(String oldID, String newID) {
        _identifiers.removeIdentifier(oldID);
        return checkID(newID);
    }

    public String checkID(String id) throws IllegalIdentifierException {
        if (! isValidXMLIdentifier(id)) {
            throw new IllegalIdentifierException("Illegal XML identifier: '" + id + "'");
        }
        return _identifiers.getIdentifier(id).toString();
    }

    public String checkDecompositionID(String id) throws IllegalIdentifierException {
        if (! isValidXMLIdentifier(id)) {
            throw new IllegalIdentifierException("Illegal XML identifier: '" + id + "'");
        }
        if (getDecompositionIds().contains(id)) {
            throw new IllegalIdentifierException(
                    "Identifier: '" + id + "' is already in use");
        }
        return id;
    }

    public boolean isValidXMLIdentifier(String id) {
        return ! (id == null || id.toLowerCase().startsWith("xml")
                || ! XMLChar.isValidName(id));
    }


    private void setMultiInstance(YTask task) {
        if (task != null) {
            task.setUpMultipleInstanceAttributes("1", "2", "2",
                    YMultiInstanceAttributes.CREATION_MODE_STATIC);
        }
    }


    private YFlow getFlow(YExternalNetElement source, YExternalNetElement target) {
        if (! (source == null || target == null)) {
            return source.getPostsetFlow(target);
        }
        return null;
    }

    private YFlow getDefaultFlow(YTask task) {
        for (YFlow flow : task.getPostsetFlows()) {
            if (flow.isDefaultFlow()) {
                return flow;
            }
        }
        return null;
    }


    // called when changing a task split type from AND to OR or XOR
    private YFlow setDefaultFlow(YTask task, int splitType) {
        YFlow chosenFlow = null;
        Set<YFlow> flowSet = task.getPostsetFlows();

        if (! flowSet.isEmpty()) {
            int randomIndex = new Random().nextInt(flowSet.size());
            Iterator<YFlow> itr = flowSet.iterator();
            for (int i=0; i<=randomIndex; i++) {
                if (itr.hasNext()) chosenFlow = itr.next();
            }
        }
        if (chosenFlow != null) {
            chosenFlow.setIsDefaultFlow(true);
            if (splitType == YTask._XOR) {       // XOR default can't have a predicate
                chosenFlow.setXpathPredicate(null);
            }
        }
        return chosenFlow;
    }

    private void validateSplitParameters(YTask task, int splitType)
            throws YControlFlowHandlerException {
        if (! (splitType == YTask._AND ||
                splitType == YTask._XOR ||
                splitType == YTask._OR)) {
            throw new YControlFlowHandlerException("Invalid split type");
        }
        if (task == null) {
            throw new YControlFlowHandlerException("Task is null");
        }
    }


    private Set<YAtomicTask> getAllAtomicTasks() {
        Set<YAtomicTask> taskSet = new HashSet<YAtomicTask>();
        for (YNet net : getNets()) {
            for (YTask task : net.getNetTasks()) {
                if (task instanceof YAtomicTask) {
                    taskSet.add((YAtomicTask) task);
                }
            }
        }
        return taskSet;
    }


    private static String createBasicResourcingSpec() {
        XNode node = new XNode("resourcing");
        node.addChild("offer");
        node.addChild("allocate");
        node.addChild("start");
        for (XNode child : node.getChildren()) {
            child.addAttribute("initiator", "user");
        }
        return node.toString();
    }

     private boolean isOrphan(YDecomposition decomposition, Set<YAtomicTask> allTasks) {
         for (YAtomicTask task : allTasks) {
             YDecomposition decompOfTask = task.getDecompositionPrototype();
             if ((decompOfTask != null) && (decompOfTask == decomposition)) {
                 return false;
             }
         }
         return true;    // no task references the decomposition
     }


    private void checkSpecificationExists() throws YControlFlowHandlerException {
        if (_specification == null) raise("No specification is loaded");
    }

    private void raise(String msg) throws YControlFlowHandlerException {
        throw new YControlFlowHandlerException(msg);
    }

}
