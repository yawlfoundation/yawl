/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.worklet.rdr;

import au.edu.qut.yawl.worklet.support.*;

import java.lang.*;
import java.util.*;

import org.jdom.Element ;


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
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.7, 10/12/2005    
*/


public class RdrNode {
	
	// node members
    private RdrNode parent = null;
    private RdrNode trueChild = null;
    private RdrNode falseChild = null;

    private int nodeId;   
    private String condition;
    private String conclusion;
    private String cornerstone;  
    
    private Logger _log ;
     


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
    			   String pConclusion,
    			   String pCornerStone ){
    				   	
       nodeId        = id;
       parent        = pParent;
       trueChild     = pTrueChild;
       falseChild    = pFalseChild;
       condition     = pCondition;
       conclusion    = pConclusion;
       cornerstone   = pCornerStone;
       
   	    _log = new Logger("rdrNode.log");

    }

	/** 
	 *  Construct a node with all the default values.
	 *  @param id - the node id for the new node
	 */
    public RdrNode(int id){
    	this(id, null, null, null, "", "", "");
    }
    
    
    /**
     *  Construct a node with the basic values provided
	 *  @param id - the node id of the new node
	 *  @param Parent - the parent node of this node
	 *  @param Condition - the condition stored in this node
	 *  @param Conclusion - the conclusion stored in this node
	 */

    public RdrNode(int id, RdrNode parent, 
                   String condition, String conclusion) {
    	this(id, parent, null, null, condition, conclusion, "") ;
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

    public String getConclusion() {
        return(conclusion);
    }

    public String getCornerStone() {
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

    public void setConclusion(String newConclusion) {
        conclusion = newConclusion;
    }

    public void setCornerStone(String newCorner) {
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
	    catch( RDRConditionException rde ) {      // bad condition found
            _log.write(rde) ;
            pair[0] = null ;
            pair[1] = null ;
      }
      return pair ;
   }
   
//===========================================================================//
	
    /** returns a basic String representation of this node */
    public String toString(){
    	return "Node "+ nodeId + ": " + condition + " -> " + conclusion;
    }
    
//===========================================================================//
//===========================================================================//
	
}