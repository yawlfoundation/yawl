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
import org.yawlfoundation.yawl.elements.YAttributeMap;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;
import org.yawlfoundation.yawl.worklet.support.Library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    private long id;                         // for hibernate

    private String taskId = null ;           // task rules are for, null for case level
    private RdrNode rootNode = null;
    private YAttributeMap attributes;


    /** Default constructor */
    public RdrTree() {}
    
    
    /**
     *  Constructs an empty tree
     *  @param taskId - id of task that this tree will support
     */
     public RdrTree(String taskId) {
    	this.taskId = taskId ;
    }

//===========================================================================//
	
    // GETTERS //
    
    public RdrNode getRootNode() {
        return rootNode;
    }

    
    public String getTaskId() {
        return taskId != null ? taskId : RdrSet.CASE_LEVEL_TREE_FLAG;
    }



    /**
     * Gets the RdrNode for the id passed
     * @param id - the node id of the node to find
     * @return the node identified by the id, or null if this tree has no matching node
     */
    public RdrNode getNode(long id) {
       return getNode(rootNode, id) ;
    }


    /**
     * Recursively searches the tree for the node with the id passed
     * @param root the root node of the (sub)-tree
     * @param id - the node id of the node to find
     * @return the node identified by the id, or null if this tree has no matching node
     */
    private RdrNode getNode(RdrNode root, long id) {
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
        return getAllConditions(rootNode);
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

    protected YAttributeMap getAttributes() {
        return attributes != null ? attributes : new YAttributeMap();
    }
  
//===========================================================================//
    
    // SETTERS //
    
    public void setRootNode(RdrNode root) {
    	rootNode = root ;
    }
    

    public void setTaskId(String id) {
    	taskId = id ;
    }

    public void setAttributes(String rdrSetName, RuleType rType) {
        attributes = new YAttributeMap();
        attributes.put("ruleset", rdrSetName);
        attributes.put("ruletype", rType.name());
    }


//===========================================================================//
   
   /**
    *  evaluates the conditions of each transversed node in this tree
    *  @param caseData - a JDOM Element that contains the set of data
    *         attributes and values that are used to evaluate the conditional
    *         expressions
    *  @return the pair of nodes resulting from the rule evaluation
    */ 
    public RdrPair search(Element caseData) {
    	
    	// recursively search each node in the tree
        return rootNode.search(caseData, rootNode);
//
//        RdrPair pair = rootNode.search(caseData, rootNode);
//        if (pair != null) {
//            Hibernate.initialize(pair.getLastTrueNode());
//            Hibernate.initialize(pair.getLastEvaluatedNode());
//        }
//        return pair;
    }
    
//===========================================================================//

	public RdrNode createRootNode() {
        XNode nullConclusion = new XNode("conclusion", "null");
        RdrNode root = new RdrNode(0, null, "true", nullConclusion.toElement());
        root.setDescription("root node");
        setRootNode(root);
        return root;
    }
    
    /** 
	 *  Creates a new empty node.
	 *  @param parentNode The proposed parent node for this node
	 *  @param trueBranch if true, the new node will be placed on the 'true'
	 *         exception branch; if false, the node will be placed on the
	 *        'false' if-not branch
	 */
    public RdrNode addNode(RdrNode parentNode, boolean trueBranch) {
        return addNode(new RdrNode(), parentNode, trueBranch);
    }
    
    
    public RdrNode addNode(RdrNode newNode, RdrNode parentNode, boolean trueBranch) {
    //	newNode.setNodeId(nodeCount());                          // root id=0
        newNode.setParent(parentNode);
        if (trueBranch) {
            parentNode.setTrueChild(newNode);
        }
        else {
            parentNode.setFalseChild(newNode);
        }
        return newNode;
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
        return countNodes(rootNode);
    }
    
//===========================================================================//
	
	/** returns a String representation of this tree */
    public String dump(){
    	String n = Library.newline ;
    	return n + "Task ID: " + taskId + n + n + dump(rootNode) ;
    }
    
    /** recursively adds each node to a String representation of the tree */
    private String dump(RdrNode root) {
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
           s.append(root.getConclusion().toString());
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
           s.append(dump(root.getTrueChild()));     // recurse true branch
           s.append(dump(root.getFalseChild()));    // recurse false branch
      }
        return s.toString() ;
    }

    public String toString() {
        return "task: " + taskId + ", nodes: " + nodeCount();
    }


    public String toXML() {
        return toXNode().toPrettyString();
    }


    public XNode toXNode() { return toXNode("tree"); }   // default


    public XNode toXNode(String name) {
        XNode treeNode;
        if (taskId != null) {
            treeNode = new XNode("task");
            treeNode.addAttribute("name", taskId);
        }
        else treeNode = new XNode(name);

        treeNode.addAttributes(getAttributes());
        treeNode.addChildren(toXNodeList(rootNode));
        return treeNode;
    }

    private List<XNode> toXNodeList(RdrNode rdrNode) {
        List<XNode> nodeList = new ArrayList<XNode>();
        if (rdrNode != null) {
            nodeList.add(rdrNode.toXNode());
            nodeList.addAll(toXNodeList(rdrNode.getTrueChild()));
            nodeList.addAll(toXNodeList(rdrNode.getFalseChild()));
        }
        return nodeList;
    }
    
    
    public void fromXML(String xml) {
        fromXNode(new XNodeParser().parse(xml));
    }
    
    private void fromXNode(XNode node) {
        Map<Long, RdrNode> nodeMap = new HashMap<Long, RdrNode>();
        if (node != null) {
            
            // 2 passes - one to unmarshal the nodes, one to link them
            for (XNode xRuleNode : node.getChildren()) {
                RdrNode rdrNode = new RdrNode();
                rdrNode.fromXNode(xRuleNode);
                nodeMap.put(rdrNode.getNodeId(), rdrNode);
            }
            for (XNode xRuleNode : node.getChildren()) {
                long id = StringUtil.strToLong(xRuleNode.getChildText("id"), -1);
                long parentID = StringUtil.strToLong(xRuleNode.getChildText("parent"), -1);
                long trueChildID = StringUtil.strToLong(xRuleNode.getChildText("trueChild"), -1);
                long falseChildID = StringUtil.strToLong(xRuleNode.getChildText("falseChild"), -1);
                RdrNode rdrNode = nodeMap.get(id);
                if (parentID == -1) {
                    setRootNode(rdrNode);
                } else {
                    rdrNode.setParent(nodeMap.get(parentID));
                }
                if (trueChildID > -1) rdrNode.setTrueChild(nodeMap.get(trueChildID));
                if (falseChildID > -1) rdrNode.setFalseChild(nodeMap.get(falseChildID));
            }
        }
    }

//===========================================================================//
//===========================================================================//
    
}