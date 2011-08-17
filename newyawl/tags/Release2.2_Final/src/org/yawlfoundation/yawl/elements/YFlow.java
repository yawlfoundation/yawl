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

import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
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
    private String _documentation;

    public YFlow(YExternalNetElement priorElement, YExternalNetElement nextElement) {
        _priorElement = priorElement;
        _nextElement = nextElement;
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


    public String getDocumentation() {
        return _documentation;
    }


    public void setDocumentation(String documentation) {
        _documentation = documentation;
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
        }
        else if (_priorElement._net != _nextElement._net) {
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
        }
        else {
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
        StringBuilder xml = new StringBuilder("<flowsInto>");
        xml.append("<nextElementRef id=\"")
           .append(_nextElement.getID())
           .append("\"/>");

        if (_xpathPredicate != null) {
            xml.append("<predicate");
            if (_evalOrdering != null) {
                xml.append(" ordering=\"")
                   .append(_evalOrdering)
                   .append("\"");
            }
            xml.append(">")
               .append(JDOMUtil.encodeEscapes(_xpathPredicate))
               .append("</predicate>");
        }
        if (_isDefaultFlow) xml.append("<isDefaultFlow/>");
        if (_documentation != null)
            xml.append(StringUtil.wrap(_documentation, "documentation"));

        xml.append("</flowsInto>");
        return xml.toString();
    }


    public int compareTo(Object o) {
        YFlow other = (YFlow) o;
        if (this.getEvalOrdering() != null && other.getEvalOrdering() != null) {
            return this.getEvalOrdering().compareTo(other.getEvalOrdering());
        }
        else if (this.isDefaultFlow() && other.isDefaultFlow()) {
            return 0;
        }
        else if (this.isDefaultFlow()) {
            return 1;
        }
        else if (other.isDefaultFlow()) {
            return -1;
        }
        return 0;
    }
}
