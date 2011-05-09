/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.util.*;


/**
 *
 * A superclass for any type of task or condition in the YAWL paper.
 * @author Lachlan Aldred
 *
 * @author Michael Adams (updated/refactored for v2 2009)
 *
 */
public abstract class YExternalNetElement extends YNetElement implements YVerifiable {
    protected String _name;
    protected String _documentation;
    public YNet _net;                                 // this element's containing net

    // These maps store references to all preceding and succeeding elements of this
    // element, and the flows that join them.
    // key = id of prior/next task or condition, value = flow between that and this
    private Map<String, YFlow> _presetFlows = new HashMap<String, YFlow>();
    private Map<String, YFlow> _postsetFlows = new HashMap<String, YFlow>();

    // added for reduction rules code & mapping
    private Set<YExternalNetElement> _cancelledBySet = new HashSet<YExternalNetElement>();
    private Set<YExternalNetElement> _yawlMappingSet = new HashSet<YExternalNetElement>();


    public YExternalNetElement(String id, YNet container) {
        super(id);
        _net = container;
    }


    public String getName() { return _name; }

    public void setName(String name) { _name = name; }

    public String getDocumentation() { return _documentation; }

    public void setDocumentation(String doco) { _documentation = doco; }


    /**
     * Gets the net that contains this atomic task.
     * @return the containing net.
     */
    public YNet getNet() { return _net; }


    public String getProperID() {
        return _net.getSpecification().getURI() + "|" + super.getID();
    }


    /**
     * adds a flow to the set of incoming flows for this element
     * @param flow the incoming flow
     */
    public void addPreset(YFlow flow) {
        if (flow != null) {
            YExternalNetElement prior = flow.getPriorElement();
            _presetFlows.put(prior.getID(), flow);
            prior._postsetFlows.put(this.getID(), flow);
        }
    }


    /**
     * adds a flow to the set of outgoing flows for this element
     * @param flow the outgoing flow
     */
    public void addPostset(YFlow flow) {
        if (flow != null) {
            YExternalNetElement next = flow.getNextElement();
            _postsetFlows.put(next.getID(), flow);
            next._presetFlows.put(this.getID(), flow);
        }
    }


    /**
     * removes a flow from the set of incoming flows for this element
     * @param flow the incoming flow
     */
    public void removePresetFlow(YFlow flow) {
        if (flow != null) {
            YExternalNetElement prior = flow.getPriorElement();
            _presetFlows.remove(prior.getID());
            prior._postsetFlows.remove(this.getID());
        }
    }


    /**
     * removes a flow from the set of outgoing flows for this element
     * @param flow the outgoing flow
     */
    public void removePostsetFlow(YFlow flow) {
        if (flow != null) {
            YExternalNetElement next = flow.getNextElement();
            _postsetFlows.remove(next.getID());
            next._presetFlows.remove(this.getID());
        }
   }


    /**
     * gets the set of elements that succeed this element directly via flows between them
     * @return the set of succeeding elements
     */
    public Set<YExternalNetElement> getPostsetElements() {
        Set<YExternalNetElement> postsetElements = new HashSet<YExternalNetElement>();
        for (YFlow flow : _postsetFlows.values()) {
            postsetElements.add(flow.getNextElement());
        }
        return postsetElements;
    }


    /**
     * gets the set of elements that precede this element directly via flows between them
     * @return the set of preceding elements
     */
    public Set<YExternalNetElement> getPresetElements() {
        Set<YExternalNetElement> presetElements = new HashSet<YExternalNetElement>();
        for (YFlow flow : _presetFlows.values()) {
            presetElements.add(flow.getPriorElement());
        }
        return presetElements;
    }


    /**
     * gets the flow between this and the succeeding netElement passed (if any)
     * @param netElement an element that follows this via a flow
     * @return the flow connecting the elements
     */
    public YFlow getPostsetFlow(YExternalNetElement netElement) {
        return _postsetFlows.get(netElement.getID());
    }


    /**
     * gets the flow between this and the preceding netElement passed (if any)
     * @param netElement an element that precedes this via a flow
     * @return the flow connecting the elements
     */
    public YFlow getPresetFlow(YExternalNetElement netElement) {
        return _presetFlows.get(netElement.getID());
    }


    /**
     * gets the set of outgoing flows from this element
     * @return the set of outgoing flows
     */
    public Set<YFlow> getPostsetFlows() {
        return new HashSet<YFlow>(_postsetFlows.values());
    }


    /**
     * gets the set of incoming flows to this element
     * @return the set of incoming flows
     */
    public Set<YFlow> getPresetFlows() {
        return new HashSet<YFlow>(_presetFlows.values());
    }


    /**
     * gets an element on an outgoing flow from this element
     * @param id the id of the element on the outgoing flow
     * @return the element if found, or null if not
     */
    public YExternalNetElement getPostsetElement(String id) {
        return (_postsetFlows.get(id)).getNextElement();
    }


    /**
     * gets an element on an incoming flow to this element
     * @param id the id of the element on the incoming flow
     * @return the element if found, or null if not
     */
    public YExternalNetElement getPresetElement(String id) {
        return (_presetFlows.get(id)).getPriorElement();
    }

    /*************************************************************************/

    //added for reduction rules
    public Set<YExternalNetElement> getCancelledBySet() {
  	    return (_cancelledBySet != null) ?
                new HashSet<YExternalNetElement>(_cancelledBySet) : null;
    }
   
    public void addToCancelledBySet(YTask t){
 	      if (t != null) _cancelledBySet.add(t);
    }
   
    public void removeFromCancelledBySet(YTask t){
 	      if (t != null) _cancelledBySet.remove(t);
    }


    //added for reduction rules mappings
    public Set<YExternalNetElement> getYawlMappings() {
        return (_yawlMappingSet != null) ?
                new HashSet<YExternalNetElement>(_yawlMappingSet) : null;
    }
   
    public void addToYawlMappings(YExternalNetElement e){
        _yawlMappingSet.add(e);
    }

    public void addToYawlMappings(Set<YExternalNetElement> elements){
 	      _yawlMappingSet.addAll(elements);
    }

     /************************************************************************/

    public List<YVerificationMessage> verify() {
        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();
        messages.addAll(verifyPresetFlows());
        messages.addAll(verifyPostsetFlows());
        return messages;
    }


    protected List<YVerificationMessage> verifyPostsetFlows() {
        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();
        if (_net == null) {
            messages.add(new YVerificationMessage(this,
                    this + " This must have a net to be valid.",
                    YVerificationMessage.ERROR_STATUS));
        }
        if (_postsetFlows.size() == 0) {
            messages.add(new YVerificationMessage(this,
                    this + " The postset size must be > 0",
                    YVerificationMessage.ERROR_STATUS));
        }
        for (YFlow flow : _postsetFlows.values()) {
            if (flow.getPriorElement() != this) {
                messages.add(new YVerificationMessage(
                        this, "The XML based imports should never cause this ... any flow that "
                        + this
                        + " contains should have the getPriorElement() point back to "
                        + this +
                        " [END users should never see this message.]",
                        YVerificationMessage.ERROR_STATUS));
            }
            messages.addAll(flow.verify(this));
        }
        return messages;
    }


    protected List<YVerificationMessage> verifyPresetFlows() {
        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();
        if (_presetFlows.size() == 0) {
            messages.add(new YVerificationMessage(this,
                    this + " The preset size must be > 0",
                    YVerificationMessage.ERROR_STATUS));
        }
        for (YFlow flow : _presetFlows.values()) {
            if (flow.getNextElement() != this) {
                messages.add(new YVerificationMessage(this,
                        "The XML Schema would have caught this... But the getNextElement()" +
                        " method must point to the element containing the flow in its preset." +
                        " [END users should never see this message.]",
                        YVerificationMessage.ERROR_STATUS));
            }
            if (!flow.getPriorElement().getPostsetElements().contains(this)) {
                messages.add(new YVerificationMessage(this, this + " has a preset element " +
                        flow.getPriorElement() + " that does not have " + this +
                        " as a postset element.", YVerificationMessage.ERROR_STATUS));
            }
        }
        return messages;
    }


    public Object clone() throws CloneNotSupportedException {
        YExternalNetElement copy = (YExternalNetElement) super.clone();
        copy._net = _net.getCloneContainer();

        /* it may appear more natural to add the cloned
        net element into the cloned net in the net class, but when cloning a task with a remove
        set element that is not yet cloned it tries to recover by cloning those objects backwards
        through the postsets to an already cloned object.   If this backwards traversal sends the
        runtime stack back to the element that started this traversal you end up with an infinite loop.
        */
        copy._net.addNetElement(copy);

        if (_net.getCloneContainer().hashCode() != copy._net.hashCode()) {
            throw new RuntimeException();
        }

        copy._postsetFlows = new HashMap<String, YFlow>();
        copy._presetFlows = new HashMap<String, YFlow>();
        for (YFlow flow : _postsetFlows.values()) {
            String nextElmID = flow.getNextElement().getID();
            YExternalNetElement nextElemClone = copy._net.getNetElement(nextElmID);
            if (nextElemClone == null) {
                nextElemClone = (YExternalNetElement) flow.getNextElement().clone();
            }
            YFlow clonedFlow = new YFlow(copy, nextElemClone);
            clonedFlow.setEvalOrdering(flow.getEvalOrdering());
            clonedFlow.setIsDefaultFlow(flow.isDefaultFlow());
            clonedFlow.setXpathPredicate(flow.getXpathPredicate());
            clonedFlow.setDocumentation(flow.getDocumentation());
            copy.addPostset(clonedFlow);
        }
        return copy;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder();
        if (_name != null) xml.append(StringUtil.wrap(_name, "name"));
        if (_documentation != null)
            xml.append(StringUtil.wrap(_documentation, "documentation"));

        for (YFlow flow : _postsetFlows.values()) {
            String flowsToXML = flow.toXML();
            if (this instanceof YTask) {
                YExternalNetElement nextElement = flow.getNextElement();
                if (nextElement instanceof YCondition) {
                    YCondition nextCondition = (YCondition) nextElement;
                    if (nextCondition.isImplicit()) {
                        YExternalNetElement declaredNextElement =
                                nextCondition.getPostsetElements().iterator().next();
                        YFlow declaredFlow = new YFlow(this, declaredNextElement);
                        declaredFlow.setEvalOrdering(flow.getEvalOrdering());
                        declaredFlow.setXpathPredicate(flow.getXpathPredicate());
                        declaredFlow.setIsDefaultFlow(flow.isDefaultFlow());
                        declaredFlow.setDocumentation(flow.getDocumentation());
                        flowsToXML = declaredFlow.toXML();
                    }
                    else {
                        flowsToXML = flow.toXML();
                    }
                }
            }
            xml.append(flowsToXML);
        }
        return xml.toString();
    }

}
