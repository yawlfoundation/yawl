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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;

import au.edu.qut.yawl.persistence.PersistableObject;
import au.edu.qut.yawl.util.YVerificationMessage;

/**
 * 
 * @author Lachlan Aldred
 * Date: 25/09/2003
 * Time: 18:29:10
 * 
 * 
 * ***************************************************************************************
 * 
 *  objects of this class are like the arcs that join tasks to conditions in a process 
 *  model.  They are bi-directional, meaning that they refer to both their preceding 
 *  task/condition, and refer to their succeeding task/condition.  Likewise each 
 *  task/condition is "aware" of the flows preceding it, and the flows succeeding it.  
 *  Therefore tasks/conditions, and their flows form up a bi-directional linked list, of 
 *  sorts (except it's more of a graph than a list, but you get the idea).
 *  
 *  @hibernate.class table="FLOW"
 */
@Entity
public class YFlow implements Comparable, PersistableObject, ExtensionListContainer {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;
	
    private YExternalNetElement _priorElement;
    private YExternalNetElement _nextElement;
    private String _xpathPredicate;
    private Integer _evalOrdering;
    private boolean _isDefaultFlow;
    private List<Element> _internalExtensions = new ArrayList<Element>();

    /**
     * AJH: Added to support flow/link labels
     */
    private String _documentation;
    
    /**
     * Null constructor inserted for hibernate
     */
    protected YFlow() {
    }

    public YFlow(YExternalNetElement priorElement, YExternalNetElement nextElement) {
        this._priorElement = priorElement;
        this._nextElement = nextElement;
    }


    /**
     * 
     * @return
     * @hibernate.many-to-one column="NET_ELEMENT_ID"
     *    class="au.edu.qut.yawl.elements.YExternalNetElement"
     * TODO Figure this puppy out
     */
    @ManyToOne
    public YExternalNetElement getPriorElement() {
        return _priorElement;
    }
    
    @ManyToOne
    public void setPriorElement(YExternalNetElement element) {
    	_priorElement = element;
    }

    @Transient
    public Object getParent() {
    	Object retval = null;
    	if (_priorElement != null && _priorElement._net != null) {
    		retval = _priorElement._net;
    	}
    	else if (_nextElement != null && _nextElement._net != null) {
    		retval = _nextElement._net;
    	}    	
    	return retval;
    }
    
    
	/**
     * 
     * @return
     * @hibernate.many-to-one column="NET_ELEMENT_ID" class="au.edu.qut.yawl.elements.YExternalNetElement"
     * TODO Figure this puppy out
     */
    @ManyToOne
    public YExternalNetElement getNextElement() {
    	return _nextElement;
    }
    
    @ManyToOne
    public void setNextElement(YExternalNetElement element) {
    	_nextElement = element;
    }
    @Transient
    public YExternalNetElement getNextElementXml() {
    	return new YExternalNetElement(_nextElement.getID(), null);
    }
    
    public void setNextElementXml(YExternalNetElement element) {
    	_nextElement = element;
    }
    
    
    
    private Long _id;
    
    /**
     * Inserted for hibernate use only
     * @return
     * @hibernate.id column="FLOW_ID" generator-class="sequence" unsaved-value="-1"
     */
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    protected Long getID() {
		return _id;
	}

	/**
	 * Inserted for hibernate use only
	 * @param id
	 */
    protected void setID( Long id ) {
		_id = id;
	}


    /**
     * 
     * @return
     * @hibernate.property column="XPATH_PREDICATE"
     */
    @Column(name="xpath_predicate")
    public String getXpathPredicate() {
        return _xpathPredicate;
    }


    public void setXpathPredicate(String xpathPredicate) {
    	_xpathPredicate = xpathPredicate;
    }


    /**
     * 
     * @return
     * @hibernate.property column="EVAL_ORDERING"
     */
    @Column(name="eval_ordering")
    public Integer getEvalOrdering() {
        return _evalOrdering;
    }

    public void setEvalOrdering(Integer evalOrdering) {
        _evalOrdering = evalOrdering;
    }


    /**
     * @hibernate.property column="IS_DEFAULT_FLOW"
     */
    @Column(name="is_defaultflow")
    public boolean isDefaultFlow() {
        return _isDefaultFlow;
    }

    public void setDefaultFlow(boolean isDefault) {
        _isDefaultFlow = isDefault;
    }

    /**
     * AJH : Added
     * @return
	 * @hibernate.property
     */
    @Column(name="documentation")
    public String getDocumentation()
    {
        return _documentation;
    }

    /**
     * AJH: Added
     * @param _documentation
     */
    public void setDocumentation(String _documentation)
    {
        this._documentation = _documentation;
    }

    public List<YVerificationMessage> verify(YExternalNetElement caller) {
        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();
        if (_priorElement == null || _nextElement == null) {
            if (_priorElement == null) {
                messages.add(new YVerificationMessage(caller, caller + " [error] null prior element", YVerificationMessage.ERROR_STATUS));
            }
            if (_nextElement == null) {
                messages.add(new YVerificationMessage(caller, caller + " [error] null next element", YVerificationMessage.ERROR_STATUS));
            }
        } else if (_priorElement._net != _nextElement._net) {
        	System.out.println("NETVIOL   :" + _priorElement._net + "-->" + _nextElement._net);
        	System.out.println("NETVIOL-IN:" + _priorElement + "-->" + _nextElement);
            messages.add(new YVerificationMessage(caller, caller
                    + " any flow from any Element (" + _priorElement +
                    ") to any Element (" + _nextElement + ") " +
                    "must occur with the bounds of the same net.", YVerificationMessage.ERROR_STATUS));
        }
        if (_priorElement instanceof YTask) {
            YTask priorElement = (YTask) _priorElement;
            int priorElementSplitType = priorElement.getSplitType();
            if (priorElementSplitType == YTask._AND) {
                if (_xpathPredicate != null) {
                    messages.add(new YVerificationMessage(caller, caller
                            + " any flow from any AND-split (" + _priorElement
                            + ") may not have an xpath predicate.", YVerificationMessage.ERROR_STATUS));
                }
                if (_isDefaultFlow) {
                    messages.add(new YVerificationMessage(caller, caller
                            + " any flow from any AND-split (" + _priorElement
                            + ") may not have a default flow.", YVerificationMessage.ERROR_STATUS));
                }
            }
            //AND-split or OR-split
            if (priorElementSplitType != YTask._XOR) {
                if (_evalOrdering != null) {
                    messages.add(new YVerificationMessage(caller, caller
                            + " any flow from any non XOR-split (" + _priorElement
                            + ") may not have an eval ordering.", YVerificationMessage.ERROR_STATUS));
                }
            }
            //OR-split or XOR-split
            if (priorElementSplitType != YTask._AND) {
                //both must have at least one
                if (_xpathPredicate == null && !_isDefaultFlow) {
                    messages.add(new YVerificationMessage(caller, caller
                            + " any flow from any XOR/OR-split (" + _priorElement
                            + ") must have either a predicate or be a default flow.", YVerificationMessage.ERROR_STATUS));
                }
                //check XOR-split
                if (priorElementSplitType == YTask._XOR) {
                    //has predicate XOR isDefault
                    if (_xpathPredicate != null && _isDefaultFlow) {
                        messages.add(new YVerificationMessage(caller, caller
                                + " any flow from any XOR-split (" + _priorElement
                                + ") must have either a predicate or " +
                                "be a default flow (cannot be both).", YVerificationMessage.ERROR_STATUS));
                    }
                    //has predicate implies has ordering
                    if (_xpathPredicate != null && _evalOrdering == null) {
                        messages.add(new YVerificationMessage(caller, caller
                                + " any flow from any XOR-split (" + _priorElement
                                + ") that has a predicate, must have an eval ordering.", YVerificationMessage.ERROR_STATUS));
                    }
                }
                //check OR-split
                else {
                    //must have predicates
                    if (_xpathPredicate == null) {
                        messages.add(new YVerificationMessage(caller, caller
                                + " any flow from any OR-split (" + _priorElement
                                + ") must have a predicate.", YVerificationMessage.ERROR_STATUS));
                    }
                    //must not have ordering
                    else if (_evalOrdering != null) {
                        messages.add(new YVerificationMessage(caller, caller
                                + " any flow from any OR-split (" + _priorElement
                                + ") must not have an ordering.", YVerificationMessage.ERROR_STATUS));
                    }
                }
            }
        } else {
            if (_xpathPredicate != null) {
                messages.add(new YVerificationMessage(caller, caller
                        + " [error] any flow from any condition (" + _priorElement
                        + ") may not contain a predicate.", YVerificationMessage.ERROR_STATUS));
            }
            if (_evalOrdering != null) {
                messages.add(new YVerificationMessage(caller, caller
                        + " [error] any flow from any condition (" + _priorElement
                        + ") may not contain an eval ordering.", YVerificationMessage.ERROR_STATUS));
            }
            if (_isDefaultFlow) {
                messages.add(new YVerificationMessage(caller, caller
                        + " [error] any flow from any condition (" + _priorElement
                        + ") may not be a default flow.", YVerificationMessage.ERROR_STATUS));
            }
            if (_nextElement instanceof YCondition) {
                messages.add(new YVerificationMessage(caller, caller
                        + " [error] any flow from any condition (" + _priorElement
                        + ") to any other YConditionInterface (" + _nextElement + ") is not allowed.", YVerificationMessage.ERROR_STATUS));
            }
        }
        if (_priorElement instanceof YOutputCondition) {
            messages.add(new YVerificationMessage(caller, caller
                    + " [error] any flow from an OutputCondition (" + _priorElement
                    + ") is not allowed.", YVerificationMessage.ERROR_STATUS));
        }
        if (_nextElement instanceof YInputCondition) {
            messages.add(new YVerificationMessage(caller, caller
                    + " [error] any flow into an InputCondition (" + _nextElement
                    + ") is not allowed.", YVerificationMessage.ERROR_STATUS));
        }
        return messages;
    }


    public String toString() {
        String className = getClass().getName();
        return className.substring(className.lastIndexOf('.') + 2) +
                ":from[" + _priorElement + "]to[" + _nextElement + "]";
    }


    public String toXML() {
        StringBuffer xml = new StringBuffer();
        xml.append("<flowsInto>");
        if (_internalExtensions != null && !(_internalExtensions.size() == 0)) {
        	xml.append(getInternalExtensionsAsString());
        }
        xml.append("<nextElementRef id=\"" + _nextElement.getID() + "\"/>");
        if (_xpathPredicate != null) {
            xml.append("<predicate");
            if (_evalOrdering != null) {
                xml.append(" ordering=\"" + _evalOrdering.intValue() + "\"");
            }
            xml.append(">" + YTask.marshal(_xpathPredicate) + "</predicate>");
        }
        if (_isDefaultFlow) {
            xml.append("<isDefaultFlow/>");
        }

        /**
         * AJH: Generate documentation element
         */
        if (_documentation != null)
        {
            xml.append("<documentation>" + _documentation + "</documentation>");
        }

        xml.append("</flowsInto>");
        return xml.toString();
    }


    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.<p>
     *
     * In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of <i>expression</i>
     * is negative, zero or positive.
     *
     * The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)<p>
     *
     * The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.<p>
     *
     * Finally, the implementer must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.<p>
     *
     * It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * @param   o the Object to be compared.
     * @return  a negative integer, zero, or a positive integer as this object
     *		is less than, equal to, or greater than the specified object.
     *
     * @throws ClassCastException if the specified object's type prevents it
     *         from being compared to this Object.
     */
    public int compareTo(Object o) {
    	int retval = 0;
        YFlow f = (YFlow) o;
        if (this.getEvalOrdering() != null && f.getEvalOrdering() != null) {
        	retval =  this.getEvalOrdering().compareTo(f.getEvalOrdering());
        } else if (this.isDefaultFlow() && f.isDefaultFlow()) {
        	retval =  0;
        	if (
            		f.getNextElement() != null 
            		&& f.getNextElement().getID() != null
            		&& this.getNextElement() != null
            		&& this.getNextElement().getID() != null
            ) {
            	retval = this.getNextElement().getID().compareTo(f.getNextElement().getID());
            	if (retval == 0) {
            		if (
                    		f.getPriorElement() != null 
                    		&& f.getPriorElement().getID() != null
                    		&& this.getPriorElement() != null
                    		&& this.getPriorElement().getID() != null
                    ) {
                    	retval = this.getPriorElement().getID().compareTo(f.getPriorElement().getID());
            		}
            	}
        	}
        } else if (this.isDefaultFlow()) {
        	retval = 1;
        } else if (f.isDefaultFlow()) {
        	retval = -1;
        } else if (
        		f.getNextElement() != null 
        		&& f.getNextElement().getID() != null
        		&& this.getNextElement() != null
        		&& this.getNextElement().getID() != null
        ) {
        	retval = this.getNextElement().getID().compareTo(f.getNextElement().getID());
        	if (retval == 0) {
        		if (
                		f.getPriorElement() != null 
                		&& f.getPriorElement().getID() != null
                		&& this.getPriorElement() != null
                		&& this.getPriorElement().getID() != null
                ) {
                	retval = this.getPriorElement().getID().compareTo(f.getPriorElement().getID());
        		}
        	}
        }
        return retval;
    }

    private void setIsDefaultFlow(Object shouldBe) {
    	_isDefaultFlow = true;
    }
    @Transient
    private Object getIsDefaultFlow() {
    	return _isDefaultFlow ? new Object() : null;
    }

 
    @Column(name="extensions", length=32768)
	public String getInternalExtensionsAsString() {
		if (_internalExtensions == null) return "";
		XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
		StringBuffer buffer = new StringBuffer();
		for (Element e: (List<Element>) _internalExtensions) {
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
	public List getInternalExtensions() {
		return _internalExtensions;
	}

	@Transient
	public void setInternalExtensions(List extensions) {
		_internalExtensions = extensions;
	}
}
