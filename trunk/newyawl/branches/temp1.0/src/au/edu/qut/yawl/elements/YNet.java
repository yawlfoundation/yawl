/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.elements.e2wfoj.E2WFOJNet;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.elements.state.YMarking;
import au.edu.qut.yawl.elements.state.YOrJoinUtils;
import au.edu.qut.yawl.elements.state.YSetOfMarkings;
import au.edu.qut.yawl.engine.YPersistenceManager;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.schema.Instruction;
import au.edu.qut.yawl.schema.XMLToolsForYAWL;
import au.edu.qut.yawl.util.Order;
import au.edu.qut.yawl.util.Sorter;
import au.edu.qut.yawl.util.YVerificationMessage;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.StringReader;
import java.util.*;

/**
 * 
 * The implementation of a net in the YAWL paper.   - A container for tasks and conditions.
 * @author Lachlan Aldred
 * 
 */
public final class YNet extends YDecomposition {


    private YInputCondition _inputCondition;
    private YOutputCondition _outputCondition;
    private Map _netElements = new HashMap();
    private Map _localVariables = new HashMap();
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
    public void removeNetElement(YExternalNetElement netElement){
        
         Set preSet = netElement.getPresetElements();
         Iterator presetIter = preSet.iterator();
         while (presetIter.hasNext())
         { YExternalNetElement next = (YExternalNetElement) presetIter.next();
           YFlow flow = new YFlow(next,netElement);              
           next.removePostsetFlow(flow);
         }            
                  
         Set postSet = netElement.getPostsetElements();
         Iterator postsetIter = postSet.iterator();
         while (postsetIter.hasNext())
         { YExternalNetElement next = (YExternalNetElement) postsetIter.next();
           YFlow flow = new YFlow(netElement,next);              
           next.removePresetFlow(flow);
         }  
        
         //Need to remove from removeSet and cancelledBySet as well.          
         if (netElement instanceof YTask) 
         { YTask t = (YTask) netElement;
           Set removeSet = t.getRemoveSet();
           if (!removeSet.isEmpty()){
            for (Iterator i = removeSet.iterator(); i.hasNext();) {
             YExternalNetElement next = (YExternalNetElement) i.next();
             next.removeFromCancelledBySet(t);
                        
            }
           }          
         }
          
         //Check if a place or condition is part of any cancellation sets
         Set cancelledBy = netElement.getCancelledBySet();
         if (!cancelledBy.isEmpty())
         {
           for (Iterator i = cancelledBy.iterator(); i.hasNext();) {
             YTask t = (YTask) i.next();
             t.removeFromRemoveSet(netElement);
	       }          	
         }
         
         _netElements.remove(netElement.getID()); 
         
    }
    
    public void addNetElement(YExternalNetElement netElement) {
        _netElements.put(netElement.getID(), netElement);
    }


    public Map getNetElements() {
        return new HashMap(_netElements);
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
        return (YExternalNetElement) this._netElements.get(id);
    }


    /**
     * Used to verify that the net conforms to syntax of YAWL.
     * @return a List of error messages.
     */
    public List verify() {
        List messages = new Vector();
        messages.addAll(super.verify());
        if (this._inputCondition == null) {
            messages.add(new YVerificationMessage(this, this + " must contain input condition.",
                    YVerificationMessage.ERROR_STATUS));
        }
        if (this._outputCondition == null) {
            messages.add(new YVerificationMessage(this, this + " must contain output condition.",
                    YVerificationMessage.ERROR_STATUS));
        }
        Iterator netEls = this._netElements.values().iterator();
        while (netEls.hasNext()) {
            YExternalNetElement nextElement = (YExternalNetElement) netEls.next();
            if (nextElement instanceof YInputCondition && !_inputCondition.equals(nextElement)) {
                messages.add(new YVerificationMessage(this, "Only one inputCondition allowed, " +
                        "and it must be _inputCondition.", YVerificationMessage.ERROR_STATUS));
            }
            if (nextElement instanceof YOutputCondition && !_outputCondition.equals(nextElement)) {
                messages.add(new YVerificationMessage(this, "Only one outputCondition allowed, " +
                        "and it must be _outputCondition.", YVerificationMessage.ERROR_STATUS));
            }
            messages.addAll(nextElement.verify());
        }
        for (Iterator iterator = _localVariables.values().iterator(); iterator.hasNext();) {
            YVariable var = (YVariable) iterator.next();
            messages.addAll(var.verify());
        }
        //check that all elements in the net are on a directed path from 'i' to 'o'.
        messages.addAll(verifyDirectedPath());
        return messages;
    }


    private List verifyDirectedPath() {
        List messages = new Vector();
        /* Function isValid(YConditionInterface i, YConditionInterface o, Tasks T, Conditions C): Boolean
        BEGIN:
            #Initalize variables:
            visitedFw := {i};
            visitingFw := postset(visitedFw);
            visitedBk := {o};
            visitingBk := preset(visitedBk); */
        Set visitedFw = new HashSet();
        Set visitingFw = new HashSet();
        visitingFw.add(_inputCondition);
        Set visitedBk = new HashSet();
        Set visitingBk = new HashSet();
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
        Set allElements = new HashSet(_netElements.values());
        allElements.add(_inputCondition);
        allElements.add(_outputCondition);
        if (visitedFw.size() != numElements) {
            Set elementsNotInPath = new HashSet(allElements);
            elementsNotInPath.removeAll(visitedFw);
            Iterator elementsNotInPathIt = elementsNotInPath.iterator();
            while (elementsNotInPathIt.hasNext()) {
                messages.add(new YVerificationMessage(this, elementsNotInPathIt.next() +
                        " is not on a forward directed path from i to o.", YVerificationMessage.ERROR_STATUS));
            }
        }
        if (visitedBk.size() != numElements) {
            Set elementsNotInPath = new HashSet(allElements);
            elementsNotInPath.removeAll(visitedBk);
            Iterator elementsNotInPathIt = elementsNotInPath.iterator();
            while (elementsNotInPathIt.hasNext()) {
                messages.add(new YVerificationMessage(this, elementsNotInPathIt.next() +
                        " is not on a backward directed path from i to o.", YVerificationMessage.ERROR_STATUS));
            }
        }
        return messages;
    }


    public static Set getPostset(Set elements) {
        Set postset = new HashSet();
        Iterator iter = elements.iterator();
        while (iter.hasNext()) {
            YNetElement ne = (YNetElement) iter.next();
            if (!(ne instanceof YOutputCondition) && ne instanceof YExternalNetElement) {
                postset.addAll(((YExternalNetElement) ne).getPostsetElements());
            }
        }
        return postset;
    }


    public static Set getPreset(Set elements) {
        Set preset = new HashSet();
        Iterator iter = elements.iterator();
        while (iter.hasNext()) {
            YExternalNetElement ne = (YExternalNetElement) iter.next();

            if (ne != null && !(ne instanceof YInputCondition)) {
                preset.addAll(ne.getPresetElements());
            }
        }
        return preset;
    }


    public Object clone() {
        try {
            _clone = (YNet) super.clone();
            _clone._netElements = new HashMap();

            Set visited = new HashSet();
            Set visiting = new HashSet();
            visiting.add(_inputCondition);
            do {
                Iterator iter = visiting.iterator();
                while (iter.hasNext()) {
                    YExternalNetElement element = (YExternalNetElement) iter.next();
                    element.clone();
                    //_clone.addNetElement(elementCopy);//redundant call see YExternalNetElement.clone
                }
                //ensure traversal of each element only occurs once
                visited.addAll(visiting);
                visiting = getPostset(visiting);
                visiting.removeAll(visited);
            } while (visiting.size() > 0);
            _clone._localVariables = new HashMap();
            Collection params = _localVariables.values();
            for (Iterator iterator = params.iterator(); iterator.hasNext();) {
                YVariable variable = (YVariable) iterator.next();
                YVariable copyVar = (YVariable) variable.clone();
                _clone.setLocalVariable(copyVar);
            }
            _clone._data = (Document) this._data.clone();
            //do cleanup of class variable _clone before returning.
            Object temp = _clone;
            _clone = null;
            return temp;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }


    protected YNet getCloneContainer() {
        return _clone;
    }


/*
    public boolean orJoinEnabled(YTask orJoin, YIdentifier caseID) {
        if (orJoin == null || caseID == null) {
            throw new RuntimeException("Irrelavant to check the enabledness of an orjoin if " +
                    "this is called with null params.");
        }
        YMarking actualMarking = new YMarking(caseID);
        YSetOfMarkings setOfMarkings = new YSetOfMarkings();
        setOfMarkings.addMarking(actualMarking);
        if (orJoin.getJoinType() != orJoin._OR) {
            throw new RuntimeException(orJoin + " must be an OR-Join.");
        }
        List locations = actualMarking.getLocations();
        for (Iterator locIter = locations.iterator(); locIter.hasNext();) {
            YNetElement element = (YNetElement) locIter.next();
            if (orJoin.getPresetElements().contains(element)) {
                return determineEnabledness(
                        orJoin, actualMarking, new YMarking(caseID), setOfMarkings);
            }
        }
        //dont waste time on an orjoin with no tokens in preset
        return false;
    }
*/
    public boolean orJoinEnabled(YTask orJoin, YIdentifier caseID) {

        if (orJoin == null || caseID == null) {
            throw new RuntimeException("Irrelavant to check the enabledness of an orjoin if " +
                    "this is called with null params.");
        }

        if (orJoin.getJoinType() != YTask._OR) {
            throw new RuntimeException(orJoin + " must be an OR-Join.");
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
//dont waste time on an orjoin with no tokens in preset
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


    public Map getLocalVariables() {
        return _localVariables;
    }


    public String toXML() {
        StringBuffer xml = new StringBuffer();
        xml.append(super.toXML());
        List variables = new ArrayList(_localVariables.values());

        Order order = new Order() {
            public boolean lessThan(Object a, Object b) {
                YVariable var1 = (YVariable) a;
                YVariable var2 = (YVariable) b;
                String var1Nm = var1.getName() != null ?
                        var1.getName() : var1.getElementName();
                String var2Nm = var2.getName() != null ?
                        var2.getName() : var2.getElementName();

                if (var1Nm != null && var2Nm != null) {
                    return var1Nm.compareTo(var2Nm) < 0;
                } else if (var1Nm == null) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        Object[] sortedVars = Sorter.sort(order, variables.toArray());
        if (null != sortedVars) {
            for (int i = 0; i < sortedVars.length; i++) {
                YVariable variable = (YVariable) sortedVars[i];
                xml.append(variable.toXML());
            }
        }
        xml.append("<processControlElements>");
        xml.append(_inputCondition.toXML());

        Set visitedFw = new HashSet();
        Set visitingFw = new HashSet();
        visitingFw.add(_inputCondition);
        do {
            visitedFw.addAll(visitingFw);
            visitingFw = getPostset(visitingFw);
            visitingFw.removeAll(visitedFw);
            xml.append(produceXMLStringForSet(visitingFw));
        } while (visitingFw.size() > 0);
        Collection remainingElements = new ArrayList(_netElements.values());
        remainingElements.removeAll(visitedFw);
        xml.append(produceXMLStringForSet(remainingElements));
        xml.append(_outputCondition.toXML());
        xml.append("</processControlElements>");
        return xml.toString();
    }


    private String produceXMLStringForSet(Collection elementCollection) {
        StringBuffer xml = new StringBuffer();
        for (Iterator iterator = elementCollection.iterator(); iterator.hasNext();) {
            YExternalNetElement element = (YExternalNetElement) iterator.next();
            if (element instanceof YTask) {
                xml.append(element.toXML());
            } else {
                YCondition condition = (YCondition) element;
                if (condition instanceof YInputCondition ||
                        condition instanceof YOutputCondition || condition.isImplicit()) {
                    //do nothing
                } else {
                    xml.append(condition.toXML());
                }
            }
        }
        return xml.toString();
    }


    /**
     * Initialises the variable/parameter declarations so that the net may execute.
     */
    public void initialise(YPersistenceManager pmgr) throws YPersistenceException {
        super.initialise(pmgr);
        Iterator iter = _localVariables.values().iterator();
        while (iter.hasNext()) {
            YVariable variable = (YVariable) iter.next();
            Element initialValuedXMLDOM = null;
            String varElementName = variable.getName() != null ?
                    variable.getName() :
                    variable.getElementName();
            if (variable.getInitialValue() != null) {
                String initialValue = variable.getInitialValue();

                initialValue =
                        "<" + varElementName + ">" +
                        initialValue +
                        "</" + varElementName + ">";
                try {
                    SAXBuilder builder = new org.jdom.input.SAXBuilder();
                    Document doc = builder.build(new StringReader(initialValue));
                    initialValuedXMLDOM = doc.detachRootElement();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                initialValuedXMLDOM = new Element(varElementName);
            }
            addData(pmgr, initialValuedXMLDOM);
        }
    }


    public void setIncomingData(YPersistenceManager pmgr, Element incomingData) throws YSchemaBuildingException, YDataStateException, YPersistenceException {
        Iterator iter = getInputParameters().values().iterator();
        while (iter.hasNext()) {
            YParameter parameter = (YParameter) iter.next();
            Element actualParam = incomingData.getChild(parameter.getName());
            if (parameter.isMandatory() && actualParam == null) {
                throw new IllegalArgumentException("The input data for Net:" + getID() +
                        " is missing mandatory input data for a parameter (" + parameter.getName() + ").  " +
                        " Alternatively the data is there but the query in the super net produced data with" +
                        " the wrong name(Check your specification). "
                        + new XMLOutputter(Format.getPrettyFormat()).outputString(incomingData).trim());
            }
        }
        if (getSpecification().isSchemaValidating()) {
            String schema = buildSchema();
            YExternalNetElement.validateDataAgainstTypes(schema, incomingData, getID());

        }
        List actualParams = incomingData.getChildren();
        while (actualParams.size() > 0) {
            Element element = (Element) actualParams.get(0);
            element.detach();
            if (getInputParameters().containsKey(element.getName())) {
                addData(pmgr, element);
            } else {
                throw new IllegalArgumentException("Element " + element +
                        " is not a valid input parameter of " + this);
            }
        }
    }


    private String buildSchema() throws YSchemaBuildingException {
        XMLToolsForYAWL xmlt = getSpecification().getToolsForYAWL();
        Collection params = getInputParameters().values();

        Instruction[] instructionsArr = xmlt.buildInstructions(params);

        return xmlt.createYAWLSchema(instructionsArr, getRootDataElementName());
    }


    public Set getBusyTasks() {
        Set busyTasks = new HashSet();
        for (Iterator iterator = _netElements.values().iterator(); iterator.hasNext();) {
            YExternalNetElement element = (YExternalNetElement) iterator.next();
            if (element instanceof YTask) {
                YTask task = (YTask) element;
                if (task.t_isBusy()) {
                    busyTasks.add(task);
                }
            }
        }
        return busyTasks;
    }

    public Set getEnabledTasks(YIdentifier id) {
        Set enabledTasks = new HashSet();
        for (Iterator iterator = _netElements.values().iterator(); iterator.hasNext();) {
            YExternalNetElement element = (YExternalNetElement) iterator.next();
            if (element instanceof YTask) {
                YTask task = (YTask) element;
                if (task.t_enabled(id)) {
                    enabledTasks.add(task);
                }
            }
        }
        return enabledTasks;
    }

    public boolean usesSimpleRootData() {
        return getRootDataElementName().equals("data");
    }
}
