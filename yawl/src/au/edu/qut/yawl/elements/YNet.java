/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Where;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.elements.e2wfoj.E2WFOJNet;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.elements.state.YMarking;
import au.edu.qut.yawl.elements.state.YOrJoinUtils;
import au.edu.qut.yawl.elements.state.YSetOfMarkings;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.schema.Instruction;
import au.edu.qut.yawl.schema.XMLToolsForYAWL;
import au.edu.qut.yawl.util.YVerificationMessage;

/**
 *
 * The implementation of a net in the YAWL paper.   - A container for tasks and conditions.
 * @author Lachlan Aldred
 *
 *
 * ***************************************************************************************
 *
 * an object of this class contains a set of inter-connected tasks (action elements) and
 * conditions (stateful elements).  In addition to the parameter definitions inherited from
 * its superclass (YDecomposition) YNet allows local variables to be defined.
 *
 * Set class to non-final for purposes of hibernate
 *
 * @hibernate.subclass discriminator-value="1"
 */
@Entity
@DiscriminatorValue("net")
public class YNet extends YDecomposition {
    /**
     * One should only change the serialVersionUID when the class method signatures have changed.  The
     * UID should stay the same so that future revisions of the class can still be backwards compatible
     * with older revisions if method signatures have not changed.
     * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
     */
    private static final long serialVersionUID = 2006030080l;

    private List<YExternalNetElement> _netElements = new ArrayList<YExternalNetElement>();
    /**
     * All accesses to this collection should be done through the getter, {@link #getLocalVariables()}.
     * Adding to/removing from the collection should be done directly.
     */
    private List<YVariable> _localVariables = new ArrayList<YVariable>();
    private YNet _clone;

    @Transient
    public String getRootNet() {
    	String attribute = this.getAttribute("isRootNet");
        return Boolean.toString("true".equals(attribute));
    }

    @Transient
    public void setRootNet(String isRootNet) {
    	this.setAttribute("isRootNet", isRootNet);
    }

    /**
     * Null constructor for hibernate use only
     *
     */
    protected YNet() {
    }

    public YNet(String id, YSpecification specification) {
        super(id, specification);
    }


    /**
     * Method getInputCondition.
     * @return YConditionInterface
     * @hibernate.one-to-one name="inputCondition"
     *     class="au.edu.qut.yawl.elements.YInputCondition"
     */
//  @OneToOne(mappedBy="parent", cascade = {CascadeType.ALL})
//  @OnDelete(action=OnDeleteAction.CASCADE)
  @Transient
    public YInputCondition getInputCondition() {
	    YInputCondition retval = null;
  		for (YExternalNetElement element: _netElements) {
  			if (element instanceof YInputCondition) {
  				retval = (YInputCondition) element;
  				break;
  			}
  		}
  		return retval;
    }

//  @OneToOne(mappedBy="parent", cascade = {CascadeType.ALL})
//  @OnDelete(action=OnDeleteAction.CASCADE)
  @Transient
    public void setInputCondition(YInputCondition inputCondition) {
	  if (!_netElements.contains(inputCondition)) { 
	        	_netElements.add(inputCondition);
	  }
    }

    /**
     * Method getOutputCondition.
     * @return YCondition
     */
//@OneToOne(mappedBy="parent", cascade = {CascadeType.ALL})
//@OnDelete(action=OnDeleteAction.CASCADE)
  @Transient
    public YOutputCondition getOutputCondition() {
		YOutputCondition retval = null;
		for (YExternalNetElement element: _netElements) {
			if (element instanceof YOutputCondition) {
				retval = (YOutputCondition) element;
				break;
			}
		}
		return retval;
    }

//@OneToOne(mappedBy="parent", cascade = {CascadeType.ALL})
//@OnDelete(action=OnDeleteAction.CASCADE)
@Transient
    public void setOutputCondition(YOutputCondition outputCondition) {
        if (!_netElements.contains(outputCondition)) 
        	_netElements.add(outputCondition);
    }


    public void addNetElement(YExternalNetElement netElement) {
        _netElements.add(netElement);
    }


    /**
     *
     * @return
     * @hibernate.map role="netElements" cascade="all-delete-orphan"
     * @hibernate.key column="DECOMPOSITION_ID"
     * @hibernate.index column="NET_ELEMENT_ID" type="string" length="255"
     *   not-null="true"
     * @hibernate.one-to-many
     *   class="au.edu.qut.yawl.elements.YExternalNetElement"
     */
    @OneToMany(mappedBy="parent", cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
    //@OnDelete(action=OnDeleteAction.CASCADE)
    public List<YExternalNetElement> getNetElements() {
        return _netElements;
    }
    /**
     * Inserted for hibernate TODO Set to protected later
     * @param list
     */
    @OneToMany(mappedBy="parent", cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
    //@OnDelete(action=OnDeleteAction.CASCADE)
    public void setNetElements(List<YExternalNetElement> list) {
    	_netElements = list;
    }

    /**
     *
     * @param id
     * @return YExternalNetElement
     */
    @Transient
    public YExternalNetElement getNetElement(String id) {
        YExternalNetElement retval = null;
        if (id == null) throw new NullPointerException(
        		"cannot retrieve a null id element from collection.");
        for (YExternalNetElement element: getNetElements()) {
        	if (id.equals(element.getID())) {
        		retval = element;
        		break;
        	}
        }
        return retval;
    }


    /**
     * Used to verify that the net conforms to syntax of YAWL.
     * @return a List of error messages.
     */
    @Override
    public List<YVerificationMessage> verify() {
        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();
        messages.addAll(super.verify());
        if (this.getInputCondition() == null) {
            messages.add(new YVerificationMessage(this, this + " must contain input condition.",
                    YVerificationMessage.ERROR_STATUS));
        }
        if (this.getOutputCondition() == null) {
            messages.add(new YVerificationMessage(this, this + " must contain output condition.",
                    YVerificationMessage.ERROR_STATUS));
        }
        Iterator netEls = this._netElements.iterator();
        while (netEls.hasNext()) {
            YExternalNetElement nextElement = (YExternalNetElement) netEls.next();
            if ((nextElement instanceof YInputCondition) && !getInputCondition().equals(nextElement)) {
                messages.add(new YVerificationMessage(this, "Only one inputCondition allowed, " +
                        "and it must be _inputCondition.", YVerificationMessage.ERROR_STATUS));
            }
            if ((nextElement instanceof YOutputCondition) && !getOutputCondition().equals(nextElement)) {
                messages.add(new YVerificationMessage(this, "Only one outputCondition allowed, " +
                        "and it must be _outputCondition.", YVerificationMessage.ERROR_STATUS));
            }
            messages.addAll(nextElement.verify());
        }
        for (Object element : getLocalVariables()) {
            YVariable var = (YVariable) element;
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
        Set<YExternalNetElement> visitedFw = new HashSet<YExternalNetElement>();
        Set<YExternalNetElement> visitingFw = new HashSet<YExternalNetElement>();
        visitingFw.add(getInputCondition());
        Set<YExternalNetElement> visitedBk = new HashSet<YExternalNetElement>();
        Set<YExternalNetElement> visitingBk = new HashSet<YExternalNetElement>();
        visitingBk.add(getOutputCondition());
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
        Set allElements = new HashSet(_netElements);
        allElements.add(getInputCondition());
        allElements.add(getOutputCondition());
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


    public static Set<YExternalNetElement> getPostset(Collection<YExternalNetElement> elements) {
        Set<YExternalNetElement> postset = new HashSet<YExternalNetElement>();
        Iterator iter = elements.iterator();
        while (iter.hasNext()) {
            YNetElement ne = (YNetElement) iter.next();
            if (!(ne instanceof YOutputCondition) && (ne instanceof YExternalNetElement)) {
                postset.addAll(((YExternalNetElement) ne).getPostsetElements());
            }
        }
        return postset;
    }


    public static Set<YExternalNetElement> getPreset(Collection<YExternalNetElement> elements) {
        Set<YExternalNetElement> preset = new HashSet<YExternalNetElement>();
        Iterator iter = elements.iterator();
        while (iter.hasNext()) {
            YExternalNetElement ne = (YExternalNetElement) iter.next();
            if ((ne != null) && !(ne instanceof YInputCondition)) {
                preset.addAll(ne.getPresetElements());
            }
        }
        return preset;
    }


    @Override
    public Object clone() {
        try {
        	
            _clone = (YNet) super.clone();
            _clone._netElements = new ArrayList<YExternalNetElement>();

            Set<YExternalNetElement> visited = new HashSet<YExternalNetElement>();
            Set<YExternalNetElement> visiting = new HashSet<YExternalNetElement>();
            visiting.add(getInputCondition());
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
            _clone._localVariables = new ArrayList<YVariable>();
            Collection params = getLocalVariables();
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
            return null;
        }
    }

    @Transient
    protected YNet getCloneContainer() {
        return _clone;
    }

    public YDecomposition deepClone() {
        return deepClone( new YNet() );
    }

    protected YDecomposition deepClone( YDecomposition net ) {
        super.deepClone( net );
        try {
            YNet clone = (YNet) net;
            
            clone._localVariables = new ArrayList<YVariable>();
            for( YVariable variable : _localVariables ) {
                YVariable cloneVar = (YVariable) variable.clone();
                cloneVar.setParent( clone );
                clone._localVariables.add( cloneVar );
            }
            
            Map<YExternalNetElement, YExternalNetElement> origToCloneMap =
                new HashMap<YExternalNetElement, YExternalNetElement>();
            
            clone._netElements = new ArrayList<YExternalNetElement>();
            for( YExternalNetElement element : _netElements ) {
                YExternalNetElement elementClone = element.deepClone();
                elementClone.setParent( clone );
                clone._netElements.add( elementClone );
                origToCloneMap.put( element, elementClone );
            }
            
            // restitch the flows
            for( YExternalNetElement element : _netElements ) {
                for( YFlow flow : element.getPostsetFlows() ) {
                    YExternalNetElement source = origToCloneMap.get( flow.getPriorElement() );
                    YExternalNetElement sink = origToCloneMap.get( flow.getNextElement() );
                    
                    YFlow newFlow = new YFlow( source, sink );
                    source.getPostsetFlows().add( newFlow );
                    sink.getPresetFlows().add( newFlow );
                }
            }
            
            // restitch the remove sets for the tasks
            for( YExternalNetElement element : _netElements ) {
                if( element instanceof YTask ) {
                    YTask origTask = (YTask) element;
                    YTask cloneTask = (YTask) origToCloneMap.get( origTask );
                    
                    for( YExternalNetElement item : origTask.getRemoveSet() ) {
                        cloneTask.getRemoveSet().add( origToCloneMap.get( item ) );
                    }
                }
            }
            
            return clone;
        }
        catch( CloneNotSupportedException e ) {
            throw new Error( e );
        }
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
    @Transient
    public boolean orJoinEnabled(YTask orJoin, YIdentifier caseID) throws YPersistenceException {

        if ((orJoin == null) || (caseID == null)) {
            throw new RuntimeException("Irrelavant to check the enabledness of an orjoin if " +
                    "this is called with null params.");
        }

        if (orJoin.getJoinType() != YTask._OR) {
            throw new RuntimeException(orJoin + " must be an OR-Join.");
        }

        YMarking actualMarking = new YMarking(caseID, orJoin.getParent());
        List locations = new Vector(actualMarking.getLocations());
        List<YExternalNetElement> preSet = orJoin.getPresetElements();
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
        List<YExternalNetElement> tasks = YOrJoinUtils.reduceToEnabled(currentlyConsideredMarking, orJoin);
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
            _localVariables.add(variable);
        } else if (null != variable.getElementName()) {
            _localVariables.add(variable);            
        }
    }

    
    @Override
    public String toXML() {
        StringBuffer xml = new StringBuffer();
        xml.append(super.toXML());
        // getLocalVariables() returns a sorted list
        List<YVariable> variables = getLocalVariables();

        for( YVariable variable : variables ) {
                xml.append(variable.toXML());
            }
        xml.append("<processControlElements>");
        xml.append(getInputCondition().toXML());

        Set<YExternalNetElement> visitedFw = new HashSet<YExternalNetElement>();
        Set<YExternalNetElement> visitingFw = new HashSet<YExternalNetElement>();
        visitingFw.add(getInputCondition());
        do {
            visitedFw.addAll(visitingFw);
            visitingFw = getPostset(visitingFw);
            visitingFw.removeAll(visitedFw);
            xml.append(produceXMLStringForSet(visitingFw));
        } while (visitingFw.size() > 0);
        Collection remainingElements = new ArrayList(_netElements);
        remainingElements.removeAll(visitedFw);
        xml.append(produceXMLStringForSet(remainingElements));
        xml.append(getOutputCondition().toXML());
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
                if ((condition instanceof YInputCondition) ||
                        (condition instanceof YOutputCondition) || condition.isImplicit()) {
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
    @Override
    public void initialise() throws YPersistenceException {
        super.initialise();
        Iterator iter = getLocalVariables().iterator();
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
            addData(initialValuedXMLDOM);
        }
    }


    public void setIncomingData(Element incomingData) throws YSchemaBuildingException, YDataStateException, YPersistenceException {
        Iterator iter = getInputParameters().iterator();
        while (iter.hasNext()) {
            YParameter parameter = (YParameter) iter.next();
            Element actualParam = incomingData.getChild(parameter.getName());
            if (parameter.isMandatory() && (actualParam == null)) {
                throw new IllegalArgumentException("The input data for Net:" + getId() +
                        " is missing mandatory input data for a parameter (" + parameter.getName() + ").  " +
                        " Alternatively the data is there but the query in the super net produced data with" +
                        " the wrong name(Check your specification). "
                        + new XMLOutputter(Format.getPrettyFormat()).outputString(incomingData).trim());
            }
        }
        if (getParent().isSchemaValidating()) {
            String schema = buildSchema();
            YExternalNetElement.validateDataAgainstTypes(schema, incomingData, getId());

        }
        List actualParams = incomingData.getChildren();
        while (actualParams.size() > 0) {
            Element element = (Element) actualParams.get(0);
            element.detach();
            if (getInputParameterNames().contains(element.getName())) {
                addData(element);
            } else {
                throw new IllegalArgumentException("Element " + element +
                        " is not a valid input parameter of " + this);
            }
        }
    }


    @Transient
    private String buildSchema() throws YSchemaBuildingException {
        XMLToolsForYAWL xmlt = getParent().getToolsForYAWL();
        Collection params = getInputParameters();

        Instruction[] instructionsArr = xmlt.buildInstructions(params);

        return xmlt.createYAWLSchema(instructionsArr, getRootDataElementName());
    }

    @Transient
    public Set<YExternalNetElement> getBusyTasks() {
        Set<YExternalNetElement> busyTasks = new HashSet<YExternalNetElement>();
        for (YExternalNetElement netElement : _netElements) {
            if (netElement instanceof YTask) {
                YTask task = (YTask) netElement;
                if (task.t_isBusy()) {
                    busyTasks.add(task);
                }
            }
        }
        return busyTasks;
    }

    @Transient
    public Set<YExternalNetElement> getEnabledTasks(YIdentifier id) throws YPersistenceException {
        Set<YExternalNetElement> enabledTasks = new HashSet<YExternalNetElement>();
        for (Iterator iterator = _netElements.iterator(); iterator.hasNext();) {
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

    @Transient
    public boolean usesSimpleRootData() {
        return getRootDataElementName().equals("data");
    }

    @Transient
    public YVariable getLocalVariable(String name) {
    	YVariable retval = null;
    	if (name != null) {
	        for(YVariable entry:getLocalVariables()) {

	        	if (name.equals(entry.getName()) || name.equals(entry.getElementName())) {
	        		retval = entry;
	        	}
	        }
    	}
        return retval;
    }
    
    @OneToMany(mappedBy="decomposition", cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @Where(clause="variable_type='variable'")
    public List<YVariable> getLocalVariables() {
        Collections.sort(_localVariables);
        return _localVariables;
    }

    @OneToMany(mappedBy="decomposition", cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @Where(clause="variable_type='variable'")
    protected void setLocalVariables(List<YVariable> outputParam) {
    	this._localVariables = outputParam;
    	for (YVariable parm: _localVariables) {
            parm.setParent(this);
        }
    }

    public void removeNetElement(YExternalNetElement element) {
    	element.removeAllFlows();
    	getNetElements().remove(element);
    }
}
