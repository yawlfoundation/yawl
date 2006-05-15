/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.worklet.rdr;

import au.edu.qut.yawl.worklet.support.*;

import java.util.*;

import org.jdom.Element ;


/** A Ripple Down Rule tree implementation.
 *
 *  This class maintains a set of RdrNodes. Each RdrTree contains the set of
 *  rules for one particular task in a specification.
 *  
 *  ==========        ===========        ===========
 *  | RdrSet | 1----M | RdrTree | 1----M | RdrNode |
 *  ==========        ===========        ===========
 *                        ^^^
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.7, 10/12/2005
 */

public class RdrTree {

    private String _specId = null ;                   // spec rules are for
    private String _taskId = null ;                   // task rules are for    
    private RdrNode _rootNode = null;
    private RdrNode[] _lastPair = new RdrNode[2];     // see search()
    
    private Logger _log ;                        // log file for debug messages 

    /** Default constructor */
    public RdrTree(){
    }
    
    
    /**
     *  Constructs an empty tree
     *  @param specId - specification the task is a member of
     *  @param taskId - id of task that this tree will support
     */
     public RdrTree(String specId, String taskId){
    	_taskId = taskId ;                
    	_specId = specId ;
    	
    	_log = new Logger("rdrtree.log");

    }	

//===========================================================================//
	
    // GETTERS //
    
    public RdrNode getRootNode(){
        return _rootNode;
    }
    
    public String getSpecId(){
        return _specId;
    }
    
    public String getTaskId(){
        return _taskId;
    }
    
    public RdrNode[] getLastPair(){
        return(_lastPair);
    }
    
    /**
     *  Returns the RdrNode for the id passed
     *  @param id - the node id of the node to return
     */
    public RdrNode getNode(int id) {
       return getNode(_rootNode, id) ;
    }
    
    /** recursively searches for the node id passed */    
    private RdrNode getNode(RdrNode root, int id) {
        RdrNode result ;
        if (root == null) return null ;            // no match - base case
        else {
        	if (root.getNodeId() == id) return root ;    // match found
        	result = getNode(root.getTrueChild(), id) ;
        	if (result == null) result = getNode(root.getFalseChild(), id) ;
        	return result ;
        }
    }
  
//===========================================================================//
    
    // SETTERS //
    
    public void setRootNode(RdrNode root) {
    	_rootNode = root ;
    }
    
    public void setSpecId(String id) {
    	_specId = id ;
    }
    
    public void setTaskId(String id) {
    	_taskId = id ;
    }

//===========================================================================//
   
   /**
    *  evaluates the conditions of each transversed node in this tree
    *  @param caseData - a JDOM Element that contains the set of data
    *         attributes and values that are used to evaluate the conditional
    *         expressions
    *  @return the conclusion of the last node satisfied
    */ 
    public String search(Element caseData){
    	
    	// recursively search each node in the tree    	
        _lastPair = _rootNode.recursiveSearch(caseData, _rootNode);
        if (_lastPair[0] != null)
           return (_lastPair[0].getConclusion());
        else
           return null ;   
    }  
    
//===========================================================================//

	/** 
	 *  Creates a new empty node.
	 *  @param parentNode The proposed parent node for this node
	 *  @param trueBranch if true, the new node will be placed on the 'true'
	 *         exception branch; if false, the node will be placed on the
	 *        'false' if-not branch
	 */
    public RdrNode addNode(RdrNode parentNode, boolean trueBranch)
    {
    	int nextID = nodeCount() + 1;
        RdrNode temp = new RdrNode(nextID);
        
        if(trueBranch) {
            parentNode.setTrueChild(temp);
        }
        else {
            parentNode.setFalseChild(temp);
        }
        return(temp);
    }
    
//===========================================================================//
	
    /** returns the number of nodes in the tree */
    private int countNodes(RdrNode root) {
        if ( root == null ) return 0;          // empty tree. Base case.
        else { 
           int count = 1;                                // count the root.
           count += countNodes(root.getTrueChild());      // add left subtree.
           count += countNodes(root.getFalseChild());     // add right subtree.
           return count;  
        }
     }

    /**
     * returns the number of nodes in the tree
     */
    public int nodeCount(){
        return(countNodes(_rootNode));
    }
    
//===========================================================================//
	
	/** returns a String representation of this tree */
    public String toString(){
    	String n = Library.newline ;
    	return "Spec ID: " + _specId + n +
    	       "Task ID: " + _taskId + n + n + 
               toString(_rootNode) ;
    }
    
    /** recursively adds each node to a String representation of the tree */
    private String toString(RdrNode root) {
    	StringBuffer s = new StringBuffer() ;
    	String n = Library.newline ;
    	
        if ( root != null ) {                                   // base case
           s.append("Node ID: ") ;
           s.append(root.getNodeId()) ;
           s.append(n) ;
           
           s.append("Condition: ");
           s.append(root.getCondition());
           s.append(n) ;
           
           s.append("Conclusion: ");
           s.append(root.getConclusion());
           s.append(n) ;
           
           if (root.getTrueChild() != null) {
              s.append("True Child ID: ");
              s.append(root.getTrueChild().getNodeId());
              s.append(n) ;   
           } 
                     	
           if (root.getFalseChild() != null) {
              s.append("False Child ID: ");
              s.append(root.getFalseChild().getNodeId());
              s.append(n) ;
           }
              
           s.append(n) ;                
           s.append(toString(root.getTrueChild()));     // recurse true branch 
           s.append(toString(root.getFalseChild()));    // recurse false branch
      }
        return s.toString() ;
     } 

//===========================================================================//
//===========================================================================//
    
}