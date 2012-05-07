/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.elements;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.elements.data.external.AbstractExternalDBGateway;
import org.yawlfoundation.yawl.elements.data.external.ExternalDBGatewayFactory;
import org.yawlfoundation.yawl.elements.e2wfoj.E2WFOJNet;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.elements.state.YMarking;
import org.yawlfoundation.yawl.elements.state.YOrJoinUtils;
import org.yawlfoundation.yawl.elements.state.YSetOfMarkings;
import org.yawlfoundation.yawl.engine.YPersistenceManager;
import org.yawlfoundation.yawl.exceptions.YDataStateException;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.util.*;

import java.util.*;

/**
 * 
 * The implementation of a net in the YAWL semantics - A container for tasks and conditions.
 * @author Lachlan Aldred
 * 
 */
public final class YNet extends YDecomposition {

    private YInputCondition _inputCondition;
    private YOutputCondition _outputCondition;
    private Map<String, YExternalNetElement> _netElements =
            new HashMap<String, YExternalNetElement>();
    private Map<String, YVariable> _localVariables = new HashMap<String, YVariable>();
    private String _externalDataGateway;
    private YNet _clone;


    public YNet(String id, YSpecification specification) {
        super(id, specification);
    }


    public void setInputCondition(YInputCondition inputCondition) {
        _inputCondition = inputCondition;
        _netElements.put(inputCondition.getID(), inputCondition);
    }


    public void setOutputCondition(YOutputCondition outputCondition) {
        _outputCondition = outputCondition;
        _netElements.put(outputCondition.getID(), outputCondition);
    }


    /** This method removes a net element together with preSet, postSet, reset,
     * cancelledBy sets.
     */
    public void removeNetElement(YExternalNetElement netElement) {

        for (YExternalNetElement preset : netElement.getPresetElements()) {
            YFlow flow = new YFlow(preset, netElement);
            preset.removePostsetFlow(flow);
        }

        for (YExternalNetElement postset : netElement.getPostsetElements()) {
            YFlow flow = new YFlow(netElement, postset);
            postset.removePresetFlow(flow);
        }

        // need to remove from removeSet and cancelledBySet as well
        if (netElement instanceof YTask) {
            YTask task = (YTask) netElement;
            Set<YExternalNetElement> removeSet = task.getRemoveSet();
            if (removeSet != null) {
                for (YExternalNetElement element : removeSet) {
                    element.removeFromCancelledBySet(task);
                 }
            }
        }

        // check if a place or condition is part of any cancellation sets
        Set<YExternalNetElement> cancelledBy = netElement.getCancelledBySet();
        if (cancelledBy != null) {
            for (YExternalNetElement element : cancelledBy) {
                ((YTask) element).removeFromRemoveSet(netElement);
            }
        }

        _netElements.remove(netElement.getID());
    }


    public void addNetElement(YExternalNetElement netElement) {
        _netElements.put(netElement.getID(), netElement);
    }


    public Map<String, YExternalNetElement> getNetElements() {
        return new HashMap<String, YExternalNetElement>(_netElements);
    }

    public List<YTask> getNetTasks() {
        List<YTask> result = new ArrayList<YTask>();
        for (YNetElement element : _netElements.values()) {
            if (element instanceof YTask)
                result.add((YTask) element);
        }
        return result;
    }


    /**
     * Method getInputCondition.
     * @return YConditionInterface
     */
    public YInputCondition getInputCondition() {
        return this._inputCondition;
    }


    /**
     * Method getOutputCondition.
     * @return YCondition
     */
    public YOutputCondition getOutputCondition() {
        return _outputCondition;
    }


    /**
     *
     * @param id
     * @return YExternalNetElement
     */
    public YExternalNetElement getNetElement(String id) {
        return _netElements.get(id);
    }


    public void setExternalDataGateway(String gateway) {
        _externalDataGateway = gateway;
    }


    public String getExternalDataGateway() {
        return _externalDataGateway;
    }


    /**
     * Used to verify that the net conforms to syntax of YAWL.
     */
    public void verify(YVerificationHandler handler) {
        super.verify(handler);

        if (_inputCondition == null) {
            handler.error(this, this + " must contain input condition.");
        }
        if (_outputCondition == null) {
            handler.error(this, this + " must contain output condition.");
        }

        for (YExternalNetElement element : _netElements.values()) {
            if (element instanceof YInputCondition && ! element.equals(_inputCondition)) {
                handler.error(this, "Only one Input Condition allowed per net.");
            }
            if (element instanceof YOutputCondition && ! element.equals(_outputCondition)) {
                handler.error(this, "Only one Output Condition allowed per net.");
            }
            element.verify(handler);
        }
        for (YVariable var : _localVariables.values()) {
            var.verify(handler);
        }
        //check that all elements in the net are on a directed path from 'i' to 'o'.
        verifyDirectedPath(handler);
        
        new YNetLocalVarVerifier(this).verify(handler);
    }


    private void verifyDirectedPath(YVerificationHandler handler) {

        /* Function isValid(YConditionInterface i, YConditionInterface o, Tasks T, Conditions C): Boolean
        BEGIN:
            #Initalize variables:
            visitedFw := {i};
            visitingFw := postset(visitedFw);
            visitedBk := {o};
            visitingBk := preset(visitedBk); */

        Set<YExternalNetElement> visitedFw = new HashSet<YExternalNetElement>();
        Set<YExternalNetElement> visitingFw = new HashSet<YExternalNetElement>();
        visitingFw.add(_inputCondition);
        Set<YExternalNetElement> visitedBk = new HashSet<YExternalNetElement>();
        Set<YExternalNetElement> visitingBk = new HashSet<YExternalNetElement>();
        visitingBk.add(_outputCondition);

        /*  Begin Loop:
                    visitedFw := visitedFw Union visitingFw;
                    visitingFw := postset(visitingFw) - visitedFw;
            Until: visitingFw = {} */

        do {
            visitedFw.addAll(visitingFw);
            visitingFw = getPostset(visitingFw);
            visitingFw.removeAll(visitedFw);
        } while (visitingFw.size() > 0);

        /*  Begin Loop:
                    visitedBk := visitedBk Union visitingBk;
                    visitingBk := preset(visitingBk) - visitedBk;
            Until: visitingBk = {} */

        do {
            visitedBk.addAll(visitingBk);
            visitingBk = getPreset(visitingBk);
            visitingBk.removeAll(visitedBk);
        } while (visitingBk.size() > 0);

        /*  return visitedFw = T U C ^ visitedBk = T U C;
            // returns true iff all t in T are on a directed path from i to o
        END */

        int numElements = _netElements.size();
        Set<YExternalNetElement> allElements = new HashSet<YExternalNetElement>(_netElements.values());
        allElements.add(_inputCondition);
        allElements.add(_outputCondition);
        Set<YExternalNetElement> elementsNotInPath;
        if (visitedFw.size() != numElements) {
            elementsNotInPath = new HashSet<YExternalNetElement>(allElements);
            elementsNotInPath.removeAll(visitedFw);
            for (YExternalNetElement element : elementsNotInPath) {
                handler.error(this, element +
                        " is not on a forward directed path from i to o.");
            }
        }
        if (visitedBk.size() != numElements) {
            elementsNotInPath = new HashSet<YExternalNetElement>(allElements);
            elementsNotInPath.removeAll(visitedBk);
            for (YExternalNetElement element : elementsNotInPath) {
                handler.error(this, element +
                        " is not on a backward directed path from i to o.");
            }
        }
    }


    public static Set<YExternalNetElement> getPostset(Set<YExternalNetElement> elements) {
        Set<YExternalNetElement> postset = new HashSet<YExternalNetElement>();
        for (YExternalNetElement element : elements) {
            if (! (element instanceof YOutputCondition)) {
                postset.addAll(element.getPostsetElements());
            }
        }
        return postset;
    }


    public static Set<YExternalNetElement> getPreset(Set<YExternalNetElement> elements) {
        Set<YExternalNetElement> preset = new HashSet<YExternalNetElement>();
        for (YExternalNetElement element : elements) {
           if (element != null && !(element instanceof YInputCondition)) {
                preset.addAll(element.getPresetElements());
            }
        }
        return preset;
    }


    public Object clone() {
        try {
            _clone = (YNet) super.clone();
            _clone._netElements = new HashMap<String, YExternalNetElement>();

            Set<YExternalNetElement> visited = new HashSet<YExternalNetElement>();
            Set<YExternalNetElement> visiting = new HashSet<YExternalNetElement>();
            visiting.add(_inputCondition);

            do {
                for (YExternalNetElement element : visiting) {
                    element.clone();
                }

                //ensure traversal of each element only occurs once
                visited.addAll(visiting);
                visiting = getPostset(visiting);
                visiting.removeAll(visited);
            } while (visiting.size() > 0);

            _clone._localVariables = new HashMap<String, YVariable>();
            for (YVariable variable : _localVariables.values()) {
                YVariable copyVar = (YVariable) variable.clone();
                _clone.setLocalVariable(copyVar);
            }
            _clone._externalDataGateway = _externalDataGateway;
            _clone._data = (Document) this._data.clone();

            //do cleanup of class variable _clone before returning.
            Object temp = _clone;
            _clone = null;
            return temp;
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }


    protected YNet getCloneContainer() {
        return _clone;
    }


    public boolean orJoinEnabled(YTask orJoin, YIdentifier caseID) {

        if (orJoin == null || caseID == null) {
            throw new RuntimeException("Irrelevant to check the enabledness of an orjoin if " +
                    "this is called with null params.");
        }

        if (orJoin.getJoinType() != YTask._OR) {
            throw new RuntimeException(orJoin + " is not an OR-Join.");
        }

        YMarking actualMarking = new YMarking(caseID);
        List locations = new Vector(actualMarking.getLocations());
        Set preSet = orJoin.getPresetElements();
        if (locations.containsAll(preSet)) {
            return true;
        }

        boolean callORjoin = false;
        for (Iterator locIter = locations.iterator(); locIter.hasNext();) {
            YNetElement element = (YNetElement) locIter.next();
            if (preSet.contains(element)) {
                callORjoin = true;
            }
        }

        if (callORjoin) {
            try {
                E2WFOJNet e2Net = new E2WFOJNet(this, orJoin);
                e2Net.restrictNet(actualMarking);
                e2Net.restrictNet(orJoin);
                return e2Net.orJoinEnabled(actualMarking, orJoin);
            } catch (Exception e) {
                throw new RuntimeException("Exception in OR-join call:" + e);
            }

        }
        // don't waste time on an orjoin with no tokens in preset
        return false;
    }


    /**
     * Not used since Moe added a new OR algorithm to the system.
     * @param orJoin the orjoin
     * @param actualMarking
     * @param currentlyConsideredMarking
     * @param markingsAlreadyConsidered
     * @return whether or not the orjoin is enabled.
     */
    protected boolean determineEnabledness(YTask orJoin, YMarking actualMarking,
                                         YMarking currentlyConsideredMarking,
                                         YSetOfMarkings markingsAlreadyConsidered) {
        Set tasks = YOrJoinUtils.reduceToEnabled(currentlyConsideredMarking, orJoin);
        while (tasks.size() > 0) {
            YTask task = YOrJoinUtils.pickOptimalEnabledTask(
                    tasks, orJoin, currentlyConsideredMarking, markingsAlreadyConsidered);
            tasks.remove(task);
            YSetOfMarkings markingSet = currentlyConsideredMarking.reachableInOneStep(task, orJoin);
            while (markingSet.size() > 0) {
                YMarking aMarking = markingSet.removeAMarking();
                if (aMarking.isBiggerEnablingMarkingThan(actualMarking, orJoin)) {
                    return false;
                } else if (aMarking.deadLock(orJoin)) {
                    continue;
                }
                boolean skip = false;
                for (Iterator markingsIterator = markingsAlreadyConsidered.getMarkings().iterator();
                     markingsIterator.hasNext() && !skip;) {
                    YMarking consideredMarking = (YMarking) markingsIterator.next();
                    if (aMarking.strictlyGreaterThanOrEqualWithSupports(consideredMarking)) {
                        skip = true;
                    }
                    if (aMarking.strictlyLessThanWithSupports(consideredMarking)) {
                        skip = true;
                    }
                }
                if (skip) {
                    continue;
                }
                markingsAlreadyConsidered.addMarking(aMarking);
                return determineEnabledness(orJoin, actualMarking, aMarking, markingsAlreadyConsidered);
            }
        }
        return true;
    }


    public void setLocalVariable(YVariable variable) {
        if (null != variable.getName()) {
            _localVariables.put(variable.getName(), variable);
        } else if (null != variable.getElementName()) {
            _localVariables.put(variable.getElementName(), variable);
        }
    }


    public Map<String, YVariable> getLocalVariables() {
        return _localVariables;
    }


    public YVariable getLocalOrInputVariable(String name) {
        return _localVariables.containsKey(name) ? _localVariables.get(name) :
               getInputParameters().get(name);
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder();
        xml.append(super.toXML());
        for (YVariable variable : getLocalVarsSorted()) {
            xml.append(variable.toXML());
        }
        xml.append("<processControlElements>");
        xml.append(_inputCondition.toXML());

        Set<YExternalNetElement> visitedFw = new HashSet<YExternalNetElement>();
        Set<YExternalNetElement> visitingFw = new HashSet<YExternalNetElement>();
        visitingFw.add(_inputCondition);
        do {
            visitedFw.addAll(visitingFw);
            visitingFw = getPostset(visitingFw);
            visitingFw.removeAll(visitedFw);
            xml.append(produceXMLStringForSet(visitingFw));
        } while (visitingFw.size() > 0);

        Set<YExternalNetElement> remainingElements =
                new HashSet<YExternalNetElement>(_netElements.values());
        remainingElements.removeAll(visitedFw);
        xml.append(produceXMLStringForSet(remainingElements));
        xml.append(_outputCondition.toXML());
        xml.append("</processControlElements>");
        if (_externalDataGateway != null) {
            xml.append(StringUtil.wrap(_externalDataGateway, "externalDataGateway"));
        }
        return xml.toString();
    }


    private String produceXMLStringForSet(Set<YExternalNetElement> elements) {
        StringBuilder xml = new StringBuilder();
        for (YExternalNetElement element : elements) {
            if (element instanceof YTask) {
                xml.append(element.toXML());
            }
            else {
                YCondition condition = (YCondition) element;
                if (! (condition instanceof YInputCondition ||
                        condition instanceof YOutputCondition || condition.isImplicit())) {
                    xml.append(condition.toXML());
                }
            }
        }
        return xml.toString();
    }


    private List<YVariable> getLocalVarsSorted() {
        List<YVariable> variables = new ArrayList<YVariable>(_localVariables.values());
        Collections.sort(variables, new Comparator<YVariable>() {
            public int compare(YVariable var1, YVariable var2) {
                if ((var1 == null) || (var1.getPreferredName() == null)) return -1;
                if ((var2 == null) || (var2.getPreferredName() == null)) return 1;
                return var1.getPreferredName().compareTo(var2.getPreferredName());
            }
        });
        return variables;
    }


    /**
     * Initialises the variable/parameter declarations so that the net may execute.
     */
    public void initialise(YPersistenceManager pmgr) throws YPersistenceException {
        super.initialise(pmgr);
        for (YVariable variable : _localVariables.values()) {
            String varElementName = variable.getPreferredName();            
            if (variable.getInitialValue() != null) {
                addData(pmgr, new XNode(varElementName,
                        variable.getInitialValue()).toElement());
            } 
            else {
                addData(pmgr, new Element(varElementName));
            }
        }
    }


    public void setIncomingData(YPersistenceManager pmgr, Element incomingData)
            throws YDataStateException, YPersistenceException {
        for (YParameter parameter : getInputParameters().values()) {
            Element actualParam = incomingData.getChild(parameter.getName());
            if (parameter.isMandatory() && actualParam == null) {
                throw new IllegalArgumentException("The input data for Net:" + getID() +
                        " is missing mandatory input data for a parameter (" + parameter.getName() + ").  " +
                        " Alternatively the data is there but the query in the super net produced data with" +
                        " the wrong name (Check your specification). "
                        + new XMLOutputter(Format.getPrettyFormat()).outputString(incomingData).trim());
            }

            // remove any attributes - not required and cause validation errors if left
            if ((actualParam != null) && ! actualParam.getAttributes().isEmpty()) {
                JDOMUtil.stripAttributes(actualParam);
            }
        }

        // validate against schema
        getSpecification().getDataValidator().validate(
                                   getInputParameters().values(), incomingData, getID());

        for (Element element : incomingData.getChildren()) {
            if (getInputParameters().containsKey(element.getName())) {
                addData(pmgr, element.clone());
            }
            else {
                throw new IllegalArgumentException("Element " + element +
                        " is not a valid input parameter of " + this);
            }
        }
    }

    public Set<YTask> getBusyTasks() {
        return getActiveTasks(null, "busy") ;
    }

    public Set<YTask> getEnabledTasks(YIdentifier id) {
        return getActiveTasks(id, "enabled") ;
    }

    public Set<YTask> getActiveTasks(YIdentifier id, String taskType) {
        Set<YTask> activeTasks = new HashSet<YTask>();
        for (YExternalNetElement element : _netElements.values()) {
            if (element instanceof YTask) {
                YTask task = (YTask) element;
                if ((taskType.equals("enabled") && task.t_enabled(id)) ||
                    (taskType.equals("busy") && task.t_isBusy())) {
                    activeTasks.add(task);
                }
            }
        }
        return activeTasks;
    }

    // only called when a case successfully completes
    public void postCaseDataToExternal(String caseID) {
        AbstractExternalDBGateway gateway = getInstantiatedExternalDataGateway();
        if (gateway != null) {
            gateway.updateFromCaseData(getSpecification().getSpecificationID(),
                      caseID,
                      new ArrayList<YParameter>(getOutputParameters().values()),
                      _data.getRootElement());
        }
    }

    // called when a case begins
    public Element getCaseDataFromExternal(String caseID) {
        AbstractExternalDBGateway gateway = getInstantiatedExternalDataGateway();
        if (gateway != null) {
            return gateway.populateCaseData(getSpecification().getSpecificationID(),
                           caseID,
                           new ArrayList<YParameter>(getInputParameters().values()),
                           new ArrayList<YVariable>(getLocalVariables().values()),
                           _data.getRootElement());
        }
        else return null;
    }

    private AbstractExternalDBGateway getInstantiatedExternalDataGateway() {
        if (_externalDataGateway != null) {
            return ExternalDBGatewayFactory.getInstance(_externalDataGateway);
        }
        else return null;
    }


    public boolean usesSimpleRootData() {
        return getRootDataElementName().equals("data");
    }
}
