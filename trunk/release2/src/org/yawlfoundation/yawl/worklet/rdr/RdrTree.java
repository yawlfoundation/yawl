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

package org.yawlfoundation.yawl.worklet.rdr;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.worklet.support.Library;

import java.util.ArrayList;
import java.util.List;


/**
 * A Ripple Down Rule tree implementation.
 *
 *  @author Michael Adams
 *  v0.8, 04-09/2006
 */
 /*
 * This class maintains a set of RdrNodes. Each RdrTree contains the set of
 * rules for one particular task in a specification.
 *  
 *  ==========        ===========        ===========
 *  | RdrSet | 1----M | RdrTree | 1----M | RdrNode |
 *  ==========        ===========        ===========
 *                        ^^^
 *
 */

public class RdrTree {

    private YSpecificationID _specId = null ;           // spec rules are for
    private String _taskId = null ;                   // task rules are for    
    private RdrNode _rootNode = null;
    private RdrNode[] _lastPair = new RdrNode[2];     // see search()
    
    private static Logger _log = Logger.getLogger(RdrTree.class);


    /** Default constructor */
    public RdrTree(){}
    
    
    /**
     *  Constructs an empty tree
     *  @param specId - specification the task is a member of
     *  @param taskId - id of task that this tree will support
     */
     public RdrTree(YSpecificationID specId, String taskId){
    	_taskId = taskId ;
    	_specId = specId ;
    }

//===========================================================================//
	
    // GETTERS //
    
    public RdrNode getRootNode(){
        return _rootNode;
    }
    
    public YSpecificationID getSpecId(){
        return _specId;
    }
    
    public String getTaskId(){
        return _taskId;
    }
    
    public RdrNode[] getLastPair(){
        return(_lastPair);
    }
    
    /**
     * Gets the RdrNode for the id passed
     * @param id - the node id of the node to find
     * @return the node identified by the id, or null if this tree has no matching node
     */
    public RdrNode getNode(int id) {
       return getNode(_rootNode, id) ;
    }


    /**
     * Recursively searches the tree for the node with the id passed
     * @param root the root node of the (sub)-tree
     * @param id - the node id of the node to find
     * @return the node identified by the id, or null if this tree has no matching node
     */
    private RdrNode getNode(RdrNode root, int id) {
        if (root == null) return null;                          // no match - base case
        if (root.getNodeId() == id) return root;                // match found
        RdrNode result = getNode(root.getTrueChild(), id);      // search true branch
        if (result == null) result = getNode(root.getFalseChild(), id);
        return result ;
    }


    /**
     * Gets the condition of each node in this tree
     * @return a List of all node conditions
     */
    public List<String> getAllConditions() {
        return getAllConditions(_rootNode);
    }


    /**
     * Recurses the tree, collecting the condition from each node
     * @param node the root node of this (sub)-tree
     * @return a List of all node conditions
     */
    public List<String> getAllConditions(RdrNode node) {
        List<String> list = new ArrayList<String>();
        if (node != null) {
            list.add(node.getCondition());
            list.addAll(getAllConditions(node.getTrueChild()));
            list.addAll(getAllConditions(node.getFalseChild()));
        }
        return list;
    }
  
//===========================================================================//
    
    // SETTERS //
    
    public void setRootNode(RdrNode root) {
    	_rootNode = root ;
    }
    
    public void setSpecId(YSpecificationID id) {
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
    public Element search(Element caseData) {
    	
    	// recursively search each node in the tree    	
        _lastPair = _rootNode.recursiveSearch(caseData, _rootNode);
        return _lastPair[0] != null ? _lastPair[0].getConclusion() : null;
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
        
        if (trueBranch) {
            parentNode.setTrueChild(temp);
        }
        else {
            parentNode.setFalseChild(temp);
        }
        return(temp);
    }
    
//===========================================================================//
	
    /**
     * Gets the number of nodes in the tree
     * @param root the root node of this (sub)-tree
     * @return the number of nodes in this (sub)-tree, inclusive of the root node
     */
    private int countNodes(RdrNode root) {
        if ( root == null ) return 0;          // empty tree. Base case.

        int count = 1;                                // count the root.
        count += countNodes(root.getTrueChild());      // add left subtree.
        count += countNodes(root.getFalseChild());     // add right subtree.
        return count;
     }

    /**
     * @return the number of nodes in the tree
     */
    private int nodeCount() {
        return countNodes(_rootNode);
    }
    
//===========================================================================//
	
	/** returns a String representation of this tree */
    public String toString(){
    	String n = Library.newline ;
    	return n + "Spec ID: " + _specId + 
    	       n + "Task ID: " + _taskId + n + n +
               toString(_rootNode) ;
    }
    
    /** recursively adds each node to a String representation of the tree */
    private String toString(RdrNode root) {
    	StringBuilder s = new StringBuilder() ;
    	String n = Library.newline ;
    	
        if ( root != null ) {                                   // base case
           s.append("Node ID: ") ;
           s.append(root.getNodeId()) ;
           s.append(n) ;
           
           s.append("Condition: ");
           s.append(root.getCondition());
           s.append(n) ;
           
           s.append("Conclusion: ");
           s.append(JDOMUtil.elementToStringDump(root.getConclusion()));
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