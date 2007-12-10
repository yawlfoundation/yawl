/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements;

import org.yawlfoundation.yawl.exceptions.YDataStateException;
import org.yawlfoundation.yawl.exceptions.YDataValidationException;
import org.yawlfoundation.yawl.unmarshal.XMLValidator;
import org.yawlfoundation.yawl.util.YVerificationMessage;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;


/**
 *
 * A superclass for any type of task or condition in the YAWL paper.
 * @author Lachlan Aldred
 *
 */
public abstract class YExternalNetElement extends YNetElement implements YVerifiable {
    protected String _name;
    protected String _documentation;
    public YNet _net;
    private Map _presetFlows = new HashMap();
    private Map _postsetFlows = new HashMap();

   //added for reduction rules code
    private Set _cancelledBySet = new HashSet();
    
    //added for reduction rules mapping
    private Set _yElementsSet = new HashSet();
    
    public YExternalNetElement(String id, YNet container) {
        super(id);
        _net = container;
    }


    /**
     * Method getName.
     * @return String
     */
    public String getName() {
        return _name;
    }


    public void setName(String name) {
        _name = name;
    }


    public String getDocumentation() {
        return _documentation;
    }


    public String getProperID() {
        return _net.getSpecification().getID() + "|" + super.getID();
    }


    public void setDocumentation(String _documentation) {
        this._documentation = _documentation;
    }


    public void setPreset(YFlow flowsInto) {
        if (flowsInto != null) {
            _presetFlows.put(flowsInto.getPriorElement().getID(), flowsInto);
            flowsInto.getPriorElement()._postsetFlows.put(flowsInto.getNextElement().getID(), flowsInto);
        }
    }


    public void setPostset(YFlow flowsInto) {
        if (flowsInto != null) {
            _postsetFlows.put(flowsInto.getNextElement().getID(), flowsInto);
            flowsInto.getNextElement()._presetFlows.put(flowsInto.getPriorElement().getID(), flowsInto);
        }
    }


    /**
     * Method getPostsetElement.
     * @param id
     * @return YExternalNetElement
     */
    public YExternalNetElement getPostsetElement(String id) {
/*        if (_postset.size() > 0) {
            return (YExternalNetElement) this._postset.get(id);
        } else*/ {
            return ((YFlow) this._postsetFlows.get(id)).getNextElement();
        }
    }


    /**
     * Method getPresetElement.
     * @param id
     * @return YExternalNetElement
     */
    public YExternalNetElement getPresetElement(String id) {
        return ((YFlow) _presetFlows.get(id)).getPriorElement();
    }


    public Set getPostsetElements() {
        Set postsetElements = new HashSet();
        Collection flowSet = _postsetFlows.values();
        for (Iterator iterator = flowSet.iterator(); iterator.hasNext();) {
            YFlow flow = (YFlow) iterator.next();
            postsetElements.add(flow.getNextElement());
        }
        return postsetElements;
    }


    public Set getPresetElements() {
        Set presetElements = new HashSet();
        Collection flowSet = _presetFlows.values();
        for (Iterator iterator = flowSet.iterator(); iterator.hasNext();) {
            YFlow flow = (YFlow) iterator.next();
            presetElements.add(flow.getPriorElement());
        }
        return presetElements;
    }

 public void removePresetFlow(YFlow flowsInto){

   if (flowsInto != null) {
            _postsetFlows.remove(flowsInto.getNextElement().getID());
            flowsInto.getNextElement()._presetFlows.remove(flowsInto.getPriorElement().getID());
        }

   }

   public void removePostsetFlow(YFlow flowsInto){

   if (flowsInto != null) {
            _postsetFlows.remove(flowsInto.getNextElement().getID());
            flowsInto.getNextElement()._presetFlows.remove(flowsInto.getPriorElement().getID());
        }

   }
   
   //added for reduction rules   
    public Set getCancelledBySet(){
   	if (_cancelledBySet != null) {
            return new HashSet(_cancelledBySet);
        }
        return null;
    }
   
   public void addToCancelledBySet(YTask t){
   	if (t != null && t instanceof YTask)
   	 {	_cancelledBySet.add(t);	
   	 }
   }
   
   public void removeFromCancelledBySet(YTask t){
   	 if (t != null && t instanceof YTask)
   	 {/*	for (Iterator i = _cancelledBySet.iterator(); i.hasNext();) {
         YTask re = (YTask) i.next(); 
         if (re.getID().equals(t.getID()))
         {
          _cancelledBySet.remove(re);
         }	
        }	*/	
       _cancelledBySet.remove(t); 
   	 }	
   }
   
   //added for reduction rules mappings
    public Set getYawlMappings()
   {
   	 if (_yElementsSet != null) {
            return new HashSet(_yElementsSet);
     }
     return null;
    
   }
   
   public void addToYawlMappings(YExternalNetElement e){
    	_yElementsSet.add(e);
   	
   }
   public void addToYawlMappings(Set elements){
   	_yElementsSet.addAll(elements);
   	
   }
   
    public List verify() {
        List messages = new Vector();
        messages.addAll(verifyPostsetFlows());
        messages.addAll(verifyPresetFlows());
        return messages;
    }


    protected List verifyPostsetFlows() {
        List messages = new Vector();
        if (this._net == null) {
            messages.add(new YVerificationMessage(this, this + " This must have a net to be valid.", YVerificationMessage.ERROR_STATUS));
        }
        if (_postsetFlows.size() == 0) {
            messages.add(new YVerificationMessage(this, this + " The postset size must be > 0", YVerificationMessage.ERROR_STATUS));
        }
        Collection postsetFlows = _postsetFlows.values();
        for (Iterator iterator = postsetFlows.iterator(); iterator.hasNext();) {
            YFlow flow = (YFlow) iterator.next();
            if (flow.getPriorElement() != this) {
                messages.add(new YVerificationMessage(
                        this, "The XML based imports should never cause this ... any flow that " + this
                        + " contains should have the getPriorElement() point back to " + this +
                        " [END users should never see this message.]", YVerificationMessage.ERROR_STATUS));
            }
            messages.addAll(flow.verify(this));
        }
        return messages;
    }


    protected List verifyPresetFlows() {
        List messages = new Vector();
        if (_presetFlows.size() == 0) {
            messages.add(new YVerificationMessage(this, this + " The preset size must be > 0", YVerificationMessage.ERROR_STATUS));
        }
        Collection presetFlows = _presetFlows.values();
        for (Iterator iterator = presetFlows.iterator(); iterator.hasNext();) {
            YFlow flow = (YFlow) iterator.next();
            if (flow.getNextElement() != this) {
                messages.add(new YVerificationMessage(this, "The XML Schema would have caught this... But the getNextElement()" +
                        " method must point to the element contianing the flow in its preset." +
                        " [END users should never see this message.]", YVerificationMessage.ERROR_STATUS));
            }
            if (!flow.getPriorElement().getPostsetElements().contains(this)) {
                messages.add(new YVerificationMessage(this, this + " has a preset element " +
                        flow.getPriorElement() + " that does not have " + this + " as a postset element.", YVerificationMessage.ERROR_STATUS));
            }
        }
        return messages;
    }


/*
    protected List verifyPostset() {
        List messages = new Vector();
        if (this._parentDecomposition == null) {
            messages.add(new YVerificationMessage(this, this + " This must have a net to be valid."));
        }
        if (_postset.size() == 0) {
            messages.add(new YVerificationMessage(this, this + " The postset size must be > 0"));
        }
        Iterator iter = _postset.values().iterator();
        while (iter.hasNext()) {
            YExternalNetElement nextInPostset = (YExternalNetElement) iter.next();
            if (this instanceof YCondition) {
                if (nextInPostset instanceof YCondition) {
                    messages.add(new YVerificationMessage(
                            this, this + " cannot be directly connected to another conditon: " + nextInPostset));
                }
            }
            if (!this._parentDecomposition.equals(nextInPostset._parentDecomposition)) {
                messages.add(new YVerificationMessage(
                        this, this + " and " + nextInPostset + " must be contained in the same net."
                        + " (container " + this._parentDecomposition + " & " + nextInPostset._parentDecomposition + ")"));
            }
        }
        return messages;
    }


    protected List verifyPreset() {
        List messages = new Vector();
        if (_preset.size() == 0) {
            messages.add(new YVerificationMessage(this, this + " The preset size must be > 0"));
        }
        Iterator iter = _preset.values().iterator();
        while (iter.hasNext()) {
            YExternalNetElement nextInPreset = (YExternalNetElement) iter.next();
            if (this instanceof YCondition) {
                if (nextInPreset instanceof YCondition) {
                    messages.add(new YVerificationMessage(
                            this, this + " cannot be directly connected to another conditon: " + nextInPreset));
                }
            }
            if (!this._parentDecomposition.equals(nextInPreset._parentDecomposition)) {
                messages.add(new YVerificationMessage(
                        this, this + " and " + nextInPreset + " must be contained in the same net."
                        + " (container " + this._parentDecomposition + " & " + nextInPreset._parentDecomposition + ")"));
            }
        }
        return messages;
    }
*/


    public Object clone() throws CloneNotSupportedException {
        YExternalNetElement copy = (YExternalNetElement) super.clone();
        copy._net = _net.getCloneContainer();
        copy._net.addNetElement(copy);/* it may appear more natural to add the cloned
        net element into the cloned net in the net class, but when cloning a task with a remove
        set element that is not yet cloned it tries to recover by cloning those objects backwards
        through the postsets to an already cloned object.   If this backwards traversal sends the
        runtime stack back to the element that started this traversal you end up with an infinite loop.
        */
        if (_net.getCloneContainer().hashCode() != copy._net.hashCode()) {
            throw new RuntimeException();
        }
/*
        copy._preset = new HashMap();
        copy._postset = new HashMap();
        Iterator iter = this._preset.values().iterator();
        while (iter.hasNext()) {
            YExternalNetElement postsetElement = (YExternalNetElement) iter.next();
            String elemID = postsetElement.getURI();
            YExternalNetElement postsetElementClone = copy._parentDecomposition.getNetElement(elemID);
            if (postsetElementClone == null) {
                postsetElementClone = (YExternalNetElement) postsetElement.clone();
            }
            copy.setPreset(postsetElementClone);
        }
*/
        copy._postsetFlows = new HashMap();
        copy._presetFlows = new HashMap();
        for (Iterator iterator = _postsetFlows.values().iterator(); iterator.hasNext();) {
            YFlow flow = (YFlow) iterator.next();
            String nextElmID = flow.getNextElement().getID();
            YExternalNetElement nextElemClone = copy._net.getNetElement(nextElmID);
            if (nextElemClone == null) {
                nextElemClone = (YExternalNetElement) flow.getNextElement().clone();
            }
            YFlow clonedFlow = new YFlow(copy, nextElemClone);
            clonedFlow.setEvalOrdering(flow.getEvalOrdering());
            clonedFlow.setIsDefaultFlow(flow.isDefaultFlow());
            clonedFlow.setXpathPredicate(flow.getXpathPredicate());
            /**
             * AJH: Added for flow/link labels
			*/
            clonedFlow.setDocumentation(flow.getDocumentation());
            copy.setPostset(clonedFlow);
        }
        return copy;
    }


    public String toXML() {
        StringBuffer xml = new StringBuffer();
        if (_name != null) {
            xml.append("<name>")
                    .append(_name)
                    .append("</name>");
        }
        if (_documentation != null) {
            xml.append("<documentation>")
                    .append(_documentation)
                    .append("</documentation>");
        }
        ArrayList postSetFlows = new ArrayList(_postsetFlows.values());
        for (int i = 0; i < postSetFlows.size(); i++) {
            YFlow flow = (YFlow) postSetFlows.get(i);
            String flowsToXML = flow.toXML();
            if (this instanceof YTask) {
                YExternalNetElement nextElement = flow.getNextElement();
                if (nextElement instanceof YCondition) {
                    YCondition nextCondition = (YCondition) nextElement;
                    if (nextCondition.isImplicit()) {
                        YExternalNetElement declaredNextElement =
                                (YExternalNetElement) nextCondition.getPostsetElements().iterator().next();
                        YFlow declaredFlow = new YFlow(this, declaredNextElement);
                        declaredFlow.setEvalOrdering(flow.getEvalOrdering());
                        declaredFlow.setXpathPredicate(flow.getXpathPredicate());
                        declaredFlow.setIsDefaultFlow(flow.isDefaultFlow());
                        /**
                         * AJH: Added for flow/link labels
						 */
                        declaredFlow.setDocumentation(flow.getDocumentation());
                        flowsToXML = declaredFlow.toXML();
                    } else {
                        flowsToXML = flow.toXML();
                    }
                }
            }
            xml.append(flowsToXML);
        }
        return xml.toString();
    }


    public YFlow getPostsetFlow(YExternalNetElement netElement) {
        return (YFlow) _postsetFlows.get(netElement.getID());
    }


    public Set getPostsetFlows() {
        return new HashSet(_postsetFlows.values());
    }


    /**
     * Validates the data against the schema
     * @param rawDecompositionData the raw decomposition data
     * @throws org.yawlfoundation.yawl.exceptions.YDataStateException if data does not pass validation.
     */
    public static void validateDataAgainstTypes(String schema, Element rawDecompositionData, String source)
            throws YDataStateException {
        XMLValidator validator = new XMLValidator();
        XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());

        String dataInput = output.outputString(rawDecompositionData);

        String errors = validator.checkSchema(
                schema,
                dataInput);

        if (errors.length() > 0) {
            throw new YDataValidationException(
                    schema,
                    rawDecompositionData,
                    errors,
                    source,
                    "Problem with process model.  Schema validation failed");
        }
    }
}
