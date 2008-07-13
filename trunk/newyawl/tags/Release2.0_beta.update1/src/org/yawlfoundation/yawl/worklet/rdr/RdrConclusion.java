/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */
package org.yawlfoundation.yawl.worklet.rdr;

import org.jdom.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;

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

    public RdrConclusion(Element conc) {

        // a search for an exception handler returns an element with the text "null"
        // when none of the node's conditions evaluates to true - that is, there is
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
        return getText("_" + String.valueOf(i), "action");
    }

    public String getTarget(int i) {
        return getText("_" + String.valueOf(i), "target");
    }

    private String getText(String index, String child) {
        Element block = _conclusion.getChild(index);
        return block.getChildText(child);
    }

    public int getCount() {
        if (_conclusion != null) {
           List children = _conclusion.getChildren();
           return children.size();
        }
        else return 0 ;
    }

    public boolean nullConclusion() {
        return (getCount() == 0);
    }

    public String toString() {
        return JDOMUtil.elementToString(_conclusion);
    }

    public RdrNode[] getLastPair() {
        return _pair ;
    }

    public void setLastPair(RdrNode[] pair) {
        _pair = pair ;
    }

}
