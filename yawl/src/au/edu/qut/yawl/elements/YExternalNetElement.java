/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;

import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YDataValidationException;
import au.edu.qut.yawl.persistence.PolymorphicPersistableObject;
import au.edu.qut.yawl.unmarshal.XMLValidator;
import au.edu.qut.yawl.util.YVerificationMessage;


/**
 * 
 * A superclass for any type of task or condition in the YAWL paper.
 * @author Lachlan Aldred
 * 
 * 
 * @hibernate.class table="EXTERNAL_NET_ELEMENT" discriminator-value="0"
 * @hibernate.discriminator column="NET_ELEMENT_TYPE_ID" type="integer"
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="net_element_type",
    discriminatorType=DiscriminatorType.STRING
)
@DiscriminatorValue("external_net_element")

public class YExternalNetElement extends YNetElement implements Parented, YVerifiable, PolymorphicPersistableObject, ExtensionListContainer {
    protected String _name;
    protected String _documentation;
    public YNet _net;
    @Transient
    private Collection<YFlow> _presetFlows = new TreeSet<YFlow>();
    @Transient
    private Collection<YFlow> _postsetFlows = new TreeSet<YFlow>();
    private List<Element> _internalExtensions;
    private Long _dbid;
	private static final long serialVersionUID = 2006030080l;
	/**
     * Null constructor for hibernate
     *
     */
    public YExternalNetElement() {
    	super();
    }

    public YExternalNetElement(String id, YNet container) {
        super(id);
        _net = container;
    }

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    @Column(name="extern_id")
    public Long getDbID() {
    	return _dbid;
    }

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    @Column(name="extern_id")
    public void setDbID(Long id) {
    	_dbid = id;
    }
    
    /**
     * @return the id of this YNetElement
     */
    @Column(name="net_id")
    public String getID() {
        return super.getID();
    }
    
    /**
     * Set method only used by hibernate
     * 
     * @param id
     */
    @Column(name="net_id")
    protected void setID(String id) {
    	super.setID(id);
    }
    
    @Transient
    public Object getParent() {return _net;}

    
    /**
     * Inserted for hibernate for one-to-many collection from YNet.getNetElements()
     * @return
     * @hibernate.many-to-one column="DECOMPOSITION_ID"
     *    class="au.edu.qut.yawl.elements.YNet"
     */
    @ManyToOne
    public YNet getContainer() {
    	return _net;
    }
    /**
     * Inserted for hibernate
     * @param net
     */
    protected void setContainer(YNet net) {
    	_net = net;
    }
    
    /**
     * Method getName.
     * @return String
     * @hibernate.property column="NAME"
     */
    @Column(name="name")
	public String getName() {
        return _name;
    }


    public void setName(String name) {
        _name = name;
    }


    /**
     * 
     * @return
     * @hibernate.property column="DOCUMENTATION"
     */
    @Column(name="documentation")
    public String getDocumentation() {
        return _documentation;
    }

    @Transient
    public String getProperID() {
        return _net.getSpecification().getID() + "|" + super.getID();
    }


    public void setDocumentation(String _documentation) {
        this._documentation = _documentation;
    }


    @Transient
     public void setPreset(YFlow flowsInto) {
        if (flowsInto != null) {
            _presetFlows.add(flowsInto);
            flowsInto.getPriorElement()._postsetFlows.add(flowsInto);
        }
    }


    @Transient
     public void setPostset(YFlow flowsInto) {
        if (flowsInto != null) {
            _postsetFlows.add(flowsInto);
            flowsInto.getNextElement()._presetFlows.add(flowsInto);
        }
    }


    /**
     * Method getPostsetElement.
     * @param id
     * @return YExternalNetElement
     */
    public YExternalNetElement getPostsetElement(String id) {
    	Iterator<YFlow> iter = _postsetFlows.iterator();
    	while (iter.hasNext()) {
    		YFlow flow = iter.next();
    		if (id.equals(flow.getNextElement().getID())) {
    			return flow.getNextElement();
    		}
    	}
    	return null;
    }


    /**
     * Method getPresetElement.
     * @param id
     * @return YExternalNetElement
     */
    public YExternalNetElement getPresetElement(String id) {
    	Iterator<YFlow> iter = _presetFlows.iterator();
    	while (iter.hasNext()) {
    		YFlow flow = iter.next();
    		if (id.equals(flow.getPriorElement().getID())) {
    			return flow.getPriorElement();
    		}
    	}
    	return null;
    }
    
    @Transient
    public List<YExternalNetElement> getPostsetElements() {
        List<YExternalNetElement> postsetElements = new ArrayList<YExternalNetElement>();
        for (YFlow flow:_postsetFlows) {
            postsetElements.add(flow.getNextElement());
        }
        return postsetElements;
    }
    
    @Transient
    public List<YExternalNetElement> getPresetElements() {
        List<YExternalNetElement> elements = new ArrayList<YExternalNetElement>();
        for (YFlow flow:_presetFlows) {
        	elements.add(flow.getPriorElement());
        }
        return elements;
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
        for (Iterator iterator = _postsetFlows.iterator(); iterator.hasNext();) {
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
        for (Iterator iterator = _presetFlows.iterator(); iterator.hasNext();) {
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
        copy._postsetFlows = new ArrayList<YFlow>();
        copy._presetFlows = new ArrayList<YFlow>();
        for (Iterator iterator = _postsetFlows.iterator(); iterator.hasNext();) {
            YFlow flow = (YFlow) iterator.next();
            String nextElmID = flow.getNextElement().getID();
            YExternalNetElement nextElemClone = copy._net.getNetElement(nextElmID);
            if (nextElemClone == null) {
                nextElemClone = (YExternalNetElement) flow.getNextElement().clone();
            }
            YFlow clonedFlow = new YFlow(copy, nextElemClone);
            clonedFlow.setEvalOrdering(flow.getEvalOrdering());
            clonedFlow.setDefaultFlow(flow.isDefaultFlow());
            clonedFlow.setXpathPredicate(flow.getXpathPredicate());
            copy.setPostset(clonedFlow);
        }
        return copy;
    }


    public String toXML() {
        StringBuffer xml = new StringBuffer();
        if (_internalExtensions != null) {
    		XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
        	for (Object exten: _internalExtensions) {
    			String representation = outputter.outputString((Element)exten);
    			xml.append(representation);
        	}
        }
        if (_internalExtensions != null && !(_internalExtensions.size() == 0)) {
        	xml.append(getInternalExtensionsAsString());
        }
        if( _name != null ) {
			xml.append( "<name>" ).append( _name ).append( "</name>" );
		}
        if( _documentation != null ) {
			xml.append( "<documentation>" ).append( _documentation ).append( "</documentation>" );
        }
        for( YFlow flow:_postsetFlows) {
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
                        declaredFlow.setDefaultFlow(flow.isDefaultFlow());
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
    	Iterator<YFlow> iter = _postsetFlows.iterator();
    	while (iter.hasNext()) {
    		YFlow flow = iter.next();
    		if (netElement.getID().equals(flow.getNextElement().getID())) {
    			return flow;
    		}
    	}
    	return null;
    }

    @Transient
    public Collection<YFlow> getPostsetFlows() {
        return _postsetFlows;
    }

    @Transient
    public void setPostsetFlows(Collection<YFlow> flows) {
    	for (YFlow flow: flows) {
    		flow.setPriorElement(this);
    	}
//    	_postsetFlows.clear();
    	_postsetFlows.addAll(flows);
    }

    @OneToMany(mappedBy="priorElement",cascade = {CascadeType.ALL})
    public List<YFlow> getPostsetFlowsAsList() {
        return new ArrayList<YFlow>(_postsetFlows);
        //return (List<YFlow>)_postsetFlows;
    }

    @OneToMany(mappedBy="priorElement",cascade = {CascadeType.ALL})
    public void setPostsetFlowsAsList(List<YFlow> flows) {
    	for (YFlow flow: flows) {
			flow.setPriorElement(this);
		}
		_postsetFlows.clear();
		for (YFlow flow: flows) {
			if (flow.getNextElement() != null) {
				flow.getNextElement().getPresetFlows().add(flow);
			}
		}
    	_postsetFlows.addAll(flows);
    }

   @Transient
   public Collection<YFlow> getPresetFlows() {
        return _presetFlows;
    }

   @Transient
    public void setPresetFlows(Collection<YFlow> flows) {
    	_presetFlows = flows;
    }

   @OneToMany(mappedBy="nextElement",cascade = {CascadeType.ALL})
   public List<YFlow> getPresetFlowsAsList() {
       return new ArrayList<YFlow>(_presetFlows);
   }

   @OneToMany(mappedBy="nextElement",cascade = {CascadeType.ALL})
    public void setPresetFlowsAsList(List<YFlow> flows) {
	   for (YFlow flow: flows) {
    		flow.setNextElement(this);
    	}
//	   _presetFlows.clear();
		for (YFlow flow: flows) {
			if (flow.getNextElement() != null) {
				flow.getNextElement().getPresetFlows().add(flow);
			}
		}
    	_presetFlows.addAll(flows);
    }

    /**
     * Validates the data against the schema
     * @param rawDecompositionData the raw decomposition data
     * @throws au.edu.qut.yawl.exceptions.YDataStateException if data does not pass validation.
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
    
    
    @Column(name="extensions", length=32768)
	public String getInternalExtensionsAsString() {
		if (_internalExtensions == null) return "";
		XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
		StringBuffer buffer = new StringBuffer();
		for (Element e: _internalExtensions) {
			String representation = outputter.outputString(e);
			buffer.append(representation);
		}
    	return buffer.toString();
	}

    @Column(name="extensions", length=32768)
	public void setInternalExtensionsAsString(String extensions) {
		_internalExtensions = new ArrayList<Element>();
		if (extensions == null || extensions.length() == 0) return;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		SAXBuilder sb = new SAXBuilder();
    	try {
			Document d = sb.build(new InputSource(new StringReader("<fragment>" + extensions + "</fragment>")));
			Iterator i = d.getDescendants(); 
			while(i.hasNext()) {
				Element element = (Element) i.next();
				if (element.getAttributeValue(ExtensionListContainer.IDENTIFIER_ATTRIBUTE) != null) {
					_internalExtensions.add(element);
				}
			}
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Transient
	public List<Element> getInternalExtensions() {
		return _internalExtensions;
	}

	@Transient
	public void setInternalExtensions(List<Element> extensions) {
		_internalExtensions = extensions;
	}
}
