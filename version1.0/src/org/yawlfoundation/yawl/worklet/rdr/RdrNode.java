/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */
package org.yawlfoundation.yawl.worklet.rdr;

import org.yawlfoundation.yawl.worklet.support.*;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.lang.*;
import org.jdom.Element ;
import org.apache.log4j.Logger;


/** A Ripple Down Rule Node implementation. 
 *
 *  Each RdrNode contains an individual rule. The RDRTree class maintains a 
 *  set of these nodes.
 *  
 *  ==========        ===========        ===========
 *  | RdrSet | 1----M | RdrTree | 1----M | RdrNode |
 *  ==========        ===========        ===========
 *                                           ^^^
 *  @author Michael Adams
 *  v0.8, 04-09/2006
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
    
    private Logger _log = Logger.getLogger("org.yawlfoundation.yawl.worklet.rdr.RdrNode");


    /** Default constructor */
    public RdrNode(){
    }
    
    
	/** 
	 *  Construct a new RdrNode
	 *  @param id - the node id of the new node
	 *  @param pParent - the parent node of this node
	 *  @param pTrueChild - the node on this node's true branch
	 *  @param pFalseChild - the node on this node's false branch
	 *  @param pCondition - the condition stored in this node
	 *  @param pConclusion - the conclusion stored in this node
	 *  @param pCornerStone - the cornertone case data for this node
	 */
    public RdrNode(int id,
    			   RdrNode pParent, 
    			   RdrNode pTrueChild,
    			   RdrNode pFalseChild,
    			   String pCondition,
    			   Element pConclusion,
    			   Element pCornerStone ){
 
       nodeId        = id;
       parent        = pParent;
       trueChild     = pTrueChild;
       falseChild    = pFalseChild;
       condition     = pCondition;
       conclusion    = pConclusion;
       cornerstone   = pCornerStone;
    }

	/** 
	 *  Construct a node with all the default values.
	 *  @param id - the node id for the new node
	 */
    public RdrNode(int id){
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
    
//===========================================================================//
	

    // GETTERS //
    
    public int getNodeId(){
        return(nodeId);
    }
    
    public String getNodeIdAsString() {
    	return "" + nodeId ;
    }

    public String getCondition() {
        return(condition);
    }

    public Element getConclusion() {
        return(conclusion);
    }

    public Element getCornerStone() {
        return(cornerstone);
    }

    public RdrNode getFalseChild(){
        return(falseChild);
    }

    public RdrNode getTrueChild(){
        return(trueChild);
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

	        if(ce.evaluate(condition, caseData)){ // if condition evals to True
	            if(trueChild == null) {           // ...and no exception rule
	                pair[0] = this;               // this is last satisfied 
	                pair[1] = this;               // and last searched 
	            }
	            else {                            // test the exception rule 
	                pair = trueChild.recursiveSearch(caseData, this);
	            }
	        }
	        else {                                // if condition evals to False
	            if(falseChild == null){           // ...and no if-not rule 
	                pair[0] = lastTrueNode;       // pass on last satisfied 
	                pair[1] = this;               // and this is last searched
	            }
	            else{                             // test the next if-not rule 
	                pair = falseChild.recursiveSearch(caseData,lastTrueNode);
	            }
	        }
	    }
	    catch( RdrConditionException rde ) {      // bad condition found
            _log.error("Search Exception", rde) ;
            pair[0] = null ;
            pair[1] = null ;
      }
      return pair ;
   }
   
//===========================================================================//
	
    /** returns a String representation of this node */
    public String toString(){

        StringBuffer s = new StringBuffer("RDR NODE RECORD:");

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

        StringBuffer s = new StringBuffer("<ruleNode>");

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