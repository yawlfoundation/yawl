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

package org.yawlfoundation.yawl.worklet.rdr;

import org.jdom2.Element;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;
import org.yawlfoundation.yawl.worklet.exception.ExletAction;
import org.yawlfoundation.yawl.worklet.exception.ExletTarget;
import org.yawlfoundation.yawl.worklet.support.RdrConversionTools;

import java.util.*;

/**
 *  This class represents the conclusion primitives of an RdrNode rule.
 *
 *  @author Michael Adams
 *  v0.8, 04-09/2006
 */
public class RdrConclusion {

    private long id;                      // for hibernate

    private List<Primitive> _primitives;
    private RdrPair _pair = null ;       // stored here for wr.saveSearchResults()

    public RdrConclusion() { }

    public RdrConclusion(Element conc) { parseSteps(conc); }


    public void setConclusion(Element conc) {
        parseSteps(conc);
    }


    public String getAction(int i) {
        Primitive p = getPrimitive(i);
        return p != null ? p.action : null;
    }

    public String getTarget(int i) {
        Primitive p = getPrimitive(i);
        return p != null ? p.target : null;
    }


    public int getCount() {
        return _primitives != null ? _primitives.size() : 0;
    }


    public void setSelectionPrimitive(String workletName) {
        _primitives = null;                        // only one prim allowed for selection
        addPrimitive("select", workletName);
    }


    public void addCompensationPrimitive(String workletName) {
        addPrimitive("compensate", workletName);
    }


    public void addCompensationPrimitive(List<String> workletNames) {
        addCompensationPrimitive(RdrConversionTools.StringListToString(workletNames));
    }


    public void addPrimitive(ExletAction action, ExletTarget target) {
        addPrimitive(action.toString(), target.toString());
    }


    public boolean equals(Object o) {
        if (_primitives == null) return false;
        if (o instanceof RdrConclusion) {
            RdrConclusion other = (RdrConclusion) o;
            if (other._primitives == null) return false;
            return _primitives.equals(other._primitives);
        }
        return false;
    }

    public int hashCode() {
        return _primitives != null ? _primitives.hashCode() : 17 * 31;
    }


    public boolean isNullConclusion() { return (getCount() == 0); }


    public String toString() {
        XNode node = toXNode();
        return node != null ? node.toPrettyString() : null;
    }


    public Element toElement() {
        XNode node = toXNode();
        return node != null ? node.toElement() : null;
    }

    
    public String toXML() {
        return toString();
    }


    public XNode toXNode() {
        if (! (_primitives == null || _primitives.isEmpty())) {
            XNode node = new XNode("conclusion");
            for (Primitive p : _primitives) {
                XNode primitive = new XNode("step");
                primitive.addAttribute("index", p.index);
                primitive.addChild("action", p.action);
                primitive.addChild("target", p.target);
                node.addChild(primitive);
            }
            return node;
        }
        return null;
    }

    
    public void fromXML(String xml) {
        XNode node = new XNodeParser().parse(xml);
        if (node != null) parseSteps(node.toElement());
    }


    public RdrPair getLastPair() { return _pair ; }

    public void setLastPair(RdrPair pair) { _pair = pair ; }


    // primitive indexes start at 1, not zero
    private Primitive getPrimitive(int index) {
        return _primitives != null && index < _primitives.size() ?
                _primitives.get(index - 1) : null;
    }


    private void addPrimitive(String action, String target) {
        if (_primitives == null) _primitives = new ArrayList<Primitive>();
        _primitives.add(new Primitive(_primitives.size() + 1, action, target));
    }


    private void parseSteps(Element eConc) {
        Map<Integer, Element> sortedMap = new TreeMap<Integer, Element>();
        for (Element step : eConc.getChildren()) {
            Integer i;
            if (step.getName().equals("step")) {                    // version 2
                i = Integer.parseInt(step.getAttributeValue("index"));
            }
            else {
                i = Integer.parseInt(step.getName().substring(1));  // "_1"
            }
            sortedMap.put(i, step);
        }
        rationaliseSteps(sortedMap);
    }

    private void rationaliseSteps(Map<Integer, Element> sortedMap) {
        _primitives = new ArrayList<Primitive>();
        int i = 1;
        for (Element step : sortedMap.values()) {
            String action = step.getChildText("action");
            String target = step.getChildText("target");
            _primitives.add(new Primitive(i++, action, target));
        }
    }


    /*******************************************************************************/

    private class Primitive implements Comparable<Primitive> {
        int index;
        String action;
        String target;

        Primitive(int i, String a, String t) { index = i; action = a; target = t; }

        public int compareTo(Primitive other) {return index - other.index; }
    }

}
