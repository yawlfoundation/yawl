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

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.worklet.support.ConditionEvaluator;
import org.yawlfoundation.yawl.worklet.support.Library;
import org.yawlfoundation.yawl.worklet.support.RdrConditionException;


/**
 *  A Ripple Down Rule Node implementation.
 *
 *  Each RdrNode contains an individual rule. The RDRTree class maintains a 
 *  set of these nodes.
 *
 *  @author Michael Adams
 *  v0.8, 04-09/2006
 */
 /*  ==========        ===========        ===========
 *  | RdrSet | 1----M | RdrTree | 1----M | RdrNode |
 *  ==========        ===========        ===========
 *                                           ^^^
*/

public class RdrNode {
	
	// node members
    private RdrNode parent = null;
    private RdrNode trueChild = null;
    private RdrNode falseChild = null;

    private int nodeId;   
    private String condition;
    private Element conclusion;
    private Element cornerstone;
    
    private Logger _log = Logger.getLogger(this.getClass());


    /** Default constructor */
    public RdrNode() { }
    
    
	/** 
	 *  Construct a new RdrNode
	 *  @param id - the node id of the new node
	 *  @param pParent - the parent node of this node
	 *  @param pTrueChild - the node on this node's true branch
	 *  @param pFalseChild - the node on this node's false branch
	 *  @param pCondition - the condition stored in this node
	 *  @param pConclusion - the conclusion stored in this node
	 *  @param pCornerStone - the cornerstone case data for this node
	 */
    public RdrNode(int id,
    			   RdrNode pParent, 
    			   RdrNode pTrueChild,
    			   RdrNode pFalseChild,
    			   String pCondition,
    			   Element pConclusion,
    			   Element pCornerStone) {
 
       nodeId        = id;
       parent        = pParent;
       trueChild     = pTrueChild;
       falseChild    = pFalseChild;
       condition     = pCondition;
       conclusion    = pConclusion;
       cornerstone   = pCornerStone;
    }

	/** 
	 *  Construct a node with all default values.
	 *  @param id - the node id for the new node
	 */
    public RdrNode(int id) {
    	this(id, null, null, null, "", null, null);
    }
    
    
    /**
     *  Construct a node with the basic values provided
	 *  @param id - the node id of the new node
	 *  @param parent - the parent node of this node
	 *  @param condition - the condition stored in this node
	 *  @param conclusion - the conclusion stored in this node
	 */

    public RdrNode(int id, RdrNode parent, 
                   String condition, Element conclusion) {
    	this(id, parent, null, null, condition, conclusion, null) ;
    }


    /**
     *  Construct a node with the basic values provided
	 *  @param condition - the condition stored in this node
	 *  @param conclusion - the conclusion stored in this node
     *  @param cornerstone - the data set that led to the creation of this node
	 */

    public RdrNode(String condition, Element conclusion, Element cornerstone) {
    	this(-1, null, null, null, condition, conclusion, cornerstone) ;
    }

//===========================================================================//
	

    // GETTERS //
    
    public int getNodeId(){
        return (nodeId);
    }
    
    public String getNodeIdAsString() {
    	return String.valueOf(nodeId);
    }

    public String getCondition() {
        return (condition);
    }

    public Element getConclusion() {
        return (conclusion);
    }

    public Element getCornerStone() {
        return (cornerstone);
    }

    public RdrNode getFalseChild(){
        return (falseChild);
    }

    public RdrNode getTrueChild(){
        return (trueChild);
    }
    
    public RdrNode getParent() {
    	return (parent);
    }

//===========================================================================//
	

    // SETTERS //
    
    public void setNodeId(int id) {
        nodeId = id;
    }
    
    public void setNodeId(String id) {
    	nodeId = Integer.parseInt(id) ;
    }

    public void setCondition(String newCondition) {
        condition = newCondition;
    }

    public void setConclusion(Element newConclusion) {
        conclusion = newConclusion;
    }

    public void setCornerStone(Element newCorner) {
        cornerstone = newCorner;
    }

    public void setFalseChild(RdrNode childNode){
        falseChild = childNode;
    }

    public void setTrueChild(RdrNode childNode){
        trueChild = childNode;
    }

    public void setParent(RdrNode parentNode){
        parent = parentNode;
    }
    
//===========================================================================//
	
   /** 
    *  Recursively search each node using the condition of each to determine
    *  the path to take through the tree of which this node is a member.
    *  @param caseData - a JDOM Element that contains the set of data
    *         attributes and values that are used to evaluate the conditional
    *         expressions
    *  @param lastTrueNode - the RdrNode that contains the last satisfied 
    *         condition
    *  @return a two node array: [0] the last satisfied node 
    *                            [1] the last node searched
    */
    
    public RdrNode[] recursiveSearch(Element caseData, RdrNode lastTrueNode){
        RdrNode[] pair = new RdrNode[2];
        
        ConditionEvaluator ce = new ConditionEvaluator() ;

        try {

	        if (ce.evaluate(condition, caseData)) { // if condition evals to True
	            if (trueChild == null) {            // ...and no exception rule
	                pair[0] = this;                 // this is last satisfied
	                pair[1] = this;                 // and last searched
	            }
	            else {                              // test the exception rule
	                pair = trueChild.recursiveSearch(caseData, this);
	            }
	        }
	        else {                                  // if condition evals to False
	            if (falseChild == null) {           // ...and no if-not rule
	                pair[0] = lastTrueNode;         // pass on last satisfied
	                pair[1] = this;                 // and this is last searched
	            }
	            else {                              // test the next if-not rule
	                pair = falseChild.recursiveSearch(caseData,lastTrueNode);
	            }
	        }
	    }
	    catch( RdrConditionException rde ) {        // bad condition found
            _log.error("Search Exception", rde) ;
            pair[0] = null ;
            pair[1] = null ;
      }
      return pair ;
   }
   
//===========================================================================//
	
    /** returns a String representation of this node */
    public String toString(){

        StringBuilder s = new StringBuilder("RDR NODE RECORD:");

        String par = (parent == null)? "null" : parent.toString();
        String tChild = (trueChild == null)? "null" : trueChild.toString();
        String fChild = (falseChild == null)? "null" : falseChild.toString();

        String nID = String.valueOf(nodeId);
        String conc = (conclusion == null)? "null" : JDOMUtil.elementToString(conclusion);
        String corn = (cornerstone == null)? "null" : JDOMUtil.elementToString(cornerstone);

        Library.appendLine(s, "NODE ID", nID);
        Library.appendLine(s, "CONDITION", condition);
        Library.appendLine(s, "CONCLUSION", conc);
        Library.appendLine(s, "CORNERSTONE", corn);
        Library.appendLine(s, "PARENT NODE", par);
        Library.appendLine(s, "TRUE CHILD NODE", tChild);
        Library.appendLine(s, "FALSE CHILD NODE", fChild);

        return s.toString();
    }

//===========================================================================//

    public String toXML() {
        String par = (parent == null)? "-1" : parent.getNodeIdAsString();
        String tChild = (trueChild == null)? "-1" : trueChild.getNodeIdAsString();
        String fChild = (falseChild == null)? "-1" : falseChild.getNodeIdAsString();
        String conc = (conclusion == null)? "" : JDOMUtil.elementToString(conclusion);
        String corn = (cornerstone == null)? "" : JDOMUtil.elementToString(cornerstone);

        StringBuilder s = new StringBuilder("<ruleNode>");

        Library.appendXML(s, "id", getNodeIdAsString());
        Library.appendXML(s, "parent", par);
        Library.appendXML(s, "trueChild", tChild);
        Library.appendXML(s, "falseChild", fChild);
        Library.appendXML(s, "condition", condition);

        // these two are Elements so are treated differently
        s.append(conc);
        s.append(corn);

        s.append("</ruleNode>");

        return s.toString();
    }

//===========================================================================//
//===========================================================================//
	
}