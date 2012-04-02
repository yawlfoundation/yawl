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

import org.jdom.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.worklet.exception.ExletAction;
import org.yawlfoundation.yawl.worklet.exception.ExletTarget;
import org.yawlfoundation.yawl.worklet.support.RdrConversionTools;

import java.util.List;

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

    private Element _conclusion = null ;
    private RdrNode[] _pair = null ;       // stored here for wr.saveSearchResults()

    public RdrConclusion() { }

    public RdrConclusion(Element conc) {

        // a search for an exception handler returns an element with the text "null"
        // when none of the nodes conditions evaluates to true - that is, there is
        // no exception to be handled. Thus, we only want to set the conclusion if
        // something other than "null" is returned - i.e. an exception has been identified.
        if (! conc.getText().equals("null"))
            _conclusion = conc ;
    }
    
    

    public void setConclusion(Element conc) {
        _conclusion = conc ;
    }

    public Element getConclusion() {
        return _conclusion ;
    }

    public String getAction(int i) {
        return getText("_" + i, "action");
    }

    public String getTarget(int i) {
        return getText("_" + i, "target");
    }

    private String getText(String index, String child) {
        Element block = _conclusion.getChild(index);
        return block != null ? block.getChildText(child) : "";
    }

    public int getCount() {
        int count = 0;
        if (_conclusion != null) {
           List children = _conclusion.getChildren();
           if (children != null) count = children.size();
        }
        return count;
    }
    
    
    public void setSelectionPrimitive(String workletName) {
        XNode primitive = new XNode("_1");
        primitive.addChild("action", "select");
        primitive.addChild("target", workletName);
        _conclusion = null;                        // only one prim allowed for selection
        addPrimitive(primitive);
    }


    public void addCompensationPrimitive(String workletName) {
        XNode primitive = new XNode("_" + (getCount() + 1));
        primitive.addChild("action", "compensate");
        primitive.addChild("target", workletName);
        addPrimitive(primitive);
    }


    public void addCompensationPrimitive(List<String> workletNames) {
        addCompensationPrimitive(RdrConversionTools.StringListToString(workletNames));
    }


    public void addPrimitive(ExletAction action, ExletTarget target) {
        XNode primitive = new XNode("_" + (getCount() + 1));
        primitive.addChild("action", action.toString());
        primitive.addChild("target", target.toString());
        addPrimitive(primitive);
    }


    private void addPrimitive(XNode primitive) {
        if (_conclusion == null) {
            XNode temp = new XNode("conclusion");
            temp.addChild(primitive);
            _conclusion = temp.toElement();
        }
        else {
            _conclusion.addContent(primitive.toElement().detach());
        }
    }


    public boolean nullConclusion() {
        return (getCount() == 0);
    }

    public String toString() {
        return JDOMUtil.elementToString(_conclusion);
    }
    
    public String toXML() {
        return toString();
    }
    
    public void fromXML(String xml) {
        _conclusion = JDOMUtil.stringToElement(xml);
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

}
