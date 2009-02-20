/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */

package org.yawlfoundation.yawl.elements;

import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.util.List;
import java.util.Vector;

/**
 * 
 * @author Lachlan Aldred
 * Date: 25/09/2003
 * Time: 18:29:10
 * 
 */
public class YFlow implements Comparable {

    private YExternalNetElement _priorElement;
    private YExternalNetElement _nextElement;
    private String _xpathPredicate;
    private Integer _evalOrdering;
    private boolean _isDefaultFlow;

    /**
     * AJH: Added to support flow/link labels
     */
    private String _documentation;

    public YFlow(YExternalNetElement priorElement, YExternalNetElement nextElement) {
        this._priorElement = priorElement;
        this._nextElement = nextElement;
    }


    public YExternalNetElement getPriorElement() {
        return _priorElement;
    }


    public YExternalNetElement getNextElement() {
        return _nextElement;
    }


    public String getXpathPredicate() {
        return _xpathPredicate;
    }


    public void setXpathPredicate(String xpathPredicate) {
        _xpathPredicate = xpathPredicate;
    }


    public Integer getEvalOrdering() {
        return _evalOrdering;
    }

    public void setEvalOrdering(Integer evalOrdering) {
        _evalOrdering = evalOrdering;
    }


    public boolean isDefaultFlow() {
        return _isDefaultFlow;
    }


    public void setIsDefaultFlow(boolean isDefault) {
        _isDefaultFlow = isDefault;
    }

    /**
     * AJH : Added
     * @return
     */
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
                messages.add(new YVerificationMessage(caller,
                        caller + " [error] null prior element",
                        YVerificationMessage.ERROR_STATUS));
            }
            if (_nextElement == null) {
                messages.add(new YVerificationMessage(caller,
                        caller + " [error] null next element",
                        YVerificationMessage.ERROR_STATUS));
            }
        } else if (_priorElement._net != _nextElement._net) {
            messages.add(new YVerificationMessage(caller, caller
                    + " any flow from any Element (" + _priorElement +
                    ") to any Element (" + _nextElement + ") " +
                    "must occur with the bounds of the same net.",
                    YVerificationMessage.ERROR_STATUS));
        }
        if (_priorElement instanceof YTask) {
            YTask priorElement = (YTask) _priorElement;
            int priorElementSplitType = priorElement.getSplitType();
            if (priorElementSplitType == YTask._AND) {
                if (_xpathPredicate != null) {
                    messages.add(new YVerificationMessage(caller, caller
                            + " any flow from any AND-split (" + _priorElement
                            + ") may not have an xpath predicate.",
                            YVerificationMessage.ERROR_STATUS));
                }
                if (_isDefaultFlow) {
                    messages.add(new YVerificationMessage(caller, caller
                            + " any flow from any AND-split (" + _priorElement
                            + ") may not have a default flow.",
                            YVerificationMessage.ERROR_STATUS));
                }
            }
            //AND-split or OR-split
            if (priorElementSplitType != YTask._XOR) {
                if (_evalOrdering != null) {
                    messages.add(new YVerificationMessage(caller, caller
                            + " any flow from any non XOR-split (" + _priorElement
                            + ") may not have an eval ordering.",
                            YVerificationMessage.ERROR_STATUS));
                }
            }
            //OR-split or XOR-split
            if (priorElementSplitType != YTask._AND) {
                //both must have at least one
                if (_xpathPredicate == null && !_isDefaultFlow) {
                    messages.add(new YVerificationMessage(caller, caller
                            + " any flow from any XOR/OR-split (" + _priorElement
                            + ") must have either a predicate or be a default flow.",
                            YVerificationMessage.ERROR_STATUS));
                }
                //check XOR-split
                if (priorElementSplitType == YTask._XOR) {
                    //has predicate XOR isDefault
                    if (_xpathPredicate != null && _isDefaultFlow) {
                        messages.add(new YVerificationMessage(caller, caller
                                + " any flow from any XOR-split (" + _priorElement
                                + ") must have either a predicate or " +
                                "be a default flow (cannot be both).",
                                YVerificationMessage.ERROR_STATUS));
                    }
                    //has predicate implies has ordering
                    if (_xpathPredicate != null && _evalOrdering == null) {
                        messages.add(new YVerificationMessage(caller, caller
                                + " any flow from any XOR-split (" + _priorElement
                                + ") that has a predicate, must have an eval ordering.",
                                YVerificationMessage.ERROR_STATUS));
                    }
                }
                //check OR-split
                else {
                    //must have predicates
                    if (_xpathPredicate == null) {
                        messages.add(new YVerificationMessage(caller, caller
                                + " any flow from any OR-split (" + _priorElement
                                + ") must have a predicate.",
                                YVerificationMessage.ERROR_STATUS));
                    }
                    //must not have ordering
                    else if (_evalOrdering != null) {
                        messages.add(new YVerificationMessage(caller, caller
                                + " any flow from any OR-split (" + _priorElement
                                + ") must not have an ordering.",
                                YVerificationMessage.ERROR_STATUS));
                    }
                }
            }
        } else {
            if (_xpathPredicate != null) {
                messages.add(new YVerificationMessage(caller, caller
                        + " [error] any flow from any condition (" + _priorElement
                        + ") may not contain a predicate.",
                        YVerificationMessage.ERROR_STATUS));
            }
            if (_evalOrdering != null) {
                messages.add(new YVerificationMessage(caller, caller
                        + " [error] any flow from any condition (" + _priorElement
                        + ") may not contain an eval ordering.",
                        YVerificationMessage.ERROR_STATUS));
            }
            if (_isDefaultFlow) {
                messages.add(new YVerificationMessage(caller, caller
                        + " [error] any flow from any condition (" + _priorElement
                        + ") may not be a default flow.",
                        YVerificationMessage.ERROR_STATUS));
            }
            if (_nextElement instanceof YCondition) {
                messages.add(new YVerificationMessage(caller, caller
                        + " [error] any flow from any condition (" + _priorElement
                        + ") to any other YConditionInterface (" + _nextElement +
                        ") is not allowed.", YVerificationMessage.ERROR_STATUS));
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
        xml.append("<flowsInto>" +
                "<nextElementRef id=\"" + _nextElement.getID() + "\"/>");
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
        YFlow f = (YFlow) o;
        if (this.getEvalOrdering() != null && f.getEvalOrdering() != null) {
            return this.getEvalOrdering().compareTo(f.getEvalOrdering());
        } else if (this.isDefaultFlow() && f.isDefaultFlow()) {
            return 0;
        } else if (this.isDefaultFlow()) {
            return 1;
        } else if (f.isDefaultFlow()) {
            return -1;
        }
        return 0;
    }
}
