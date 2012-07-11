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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *  This class stores a returned conclusion from a selected RdrNode.
 *
 *  The conclusion is a JDOM Element consisting of a number of children of the form:
 *      <_n>
 *        <action>someAction</action>
 *        <target>someTarget</target>
 *      <_n>
 *
 *    where n is an ordinal number that indicates an ordering or sequence of the
 *    primitives that form the conclusion.
 *
 *  @author Michael Adams
 *  v0.8, 04-09/2006
 */
public class RdrConclusion {

    private XNode _conclusion = null ;
    private RdrNode[] _pair = null ;       // stored here for wr.saveSearchResults()

    public RdrConclusion() { }

    public RdrConclusion(Element conc) {

        // a search for an exception handler returns an element with the text "null"
        // when none of the nodes conditions evaluates to true - that is, there is
        // no exception to be handled. Thus, we only want to set the conclusion if
        // something other than "null" is returned - i.e. an exception has been identified.
        if ((! (conc == null || conc.getText().equals("null"))) && conc.getContentSize() > 0)
            setConclusion(conc) ;
    }
    

    public void setConclusion(Element conc) {
        parseSteps(conc);
    }

    public Element getConclusion() {
        return _conclusion.toElement() ;
    }

    public String getAction(int i) {
        return getText(i - 1, "action");
    }

    public String getTarget(int i) {
        return getText(i - 1, "target");
    }

    private String getText(int index, String child) {
        XNode step = _conclusion.getChild(index);
        return step != null ? step.getChildText(child) : "";
    }

    public int getCount() {
        return _conclusion != null ? _conclusion.getChildCount() : 0;
    }
    
    
    public void setSelectionPrimitive(String workletName) {
        _conclusion = null;                        // only one prim allowed for selection
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


    private void addPrimitive(String action, String target) {
        XNode primitive = new XNode("step");
        primitive.addAttribute("index", getCount() + 1);
        primitive.addChild("action", action);
        primitive.addChild("target", target);
        if (_conclusion == null) {
            _conclusion = new XNode("conclusion");
        }
        _conclusion.addChild(primitive);
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
        _conclusion = new XNode("conclusion");
        int i = 1;
        for (Element step : sortedMap.values()) {
            XNode stepNode = _conclusion.addChild("step");
            stepNode.addAttribute("index", i++);
            stepNode.addChild("action", step.getChildText("action"));
            stepNode.addChild("target", step.getChildText("target"));
        }
    }

    public boolean nullConclusion() {
        return (getCount() == 0);
    }

    public String toString() {
        return _conclusion.toPrettyString();
    }
    
    public String toXML() {
        return toString();
    }

    public XNode toXNode() {
        return _conclusion;
    }

    
    public void fromXML(String xml) {
        _conclusion = new XNodeParser().parse(xml);
    }


    public RdrNode[] getLastPair() {
        return _pair ;
    }

    public void setLastPair(RdrNode[] pair) {
        _pair = pair ;
    }

    public boolean isLastPairEqual() {
        return _pair != null && _pair[0] == _pair[1];
    }

    public RdrNode getParentNode() {
        if (_pair != null) {
            return isLastPairEqual() ? getLastTrueNode() : getLastSearchedNode();
        }
        return null;
    }

    public RdrNode getLastTrueNode() {
        return (_pair != null) ? _pair[0] : null;
    }

    public RdrNode getLastSearchedNode() {
        return (_pair != null) ? _pair[1] : null;
    }


}
