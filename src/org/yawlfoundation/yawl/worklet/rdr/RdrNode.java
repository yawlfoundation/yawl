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

import org.apache.logging.log4j.LogManager;
import org.jdom2.Element;
import org.yawlfoundation.yawl.elements.YAttributeMap;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;
import org.yawlfoundation.yawl.worklet.support.ConditionEvaluator;
import org.yawlfoundation.yawl.worklet.rdrutil.RdrConditionException;


/**
 *  A Ripple Down Rule Node implementation.
 *
 *  Each RdrNode contains an individual rule. The RdrTree class maintains a
 *  set of these nodes.
 *
 *  @author Michael Adams
 *  v0.8, 04-09/2006
 */

public class RdrNode implements Cloneable {
	
    private long id;                                      // for hibernate & toString

	// node members
    private RdrNode parent = null;
    private RdrNode trueChild = null;
    private RdrNode falseChild = null;

    private String condition;
    private RdrConclusion conclusion;
    private Element cornerstone;
    private String description;

    private YAttributeMap _attributes;

    private static final ConditionEvaluator EVALUATOR = new ConditionEvaluator();


    /** Default constructor */
    public RdrNode() { }
    
    
	/** 
	 *  Construct a new RdrNode
	 *  @param pParent - the parent node of this node
	 *  @param pTrueChild - the node on this node's true branch
	 *  @param pFalseChild - the node on this node's false branch
	 *  @param pCondition - the condition stored in this node
	 *  @param pConclusion - the conclusion stored in this node
	 *  @param pCornerStone - the cornerstone case data for this node
	 */
    public RdrNode(RdrNode pParent,
    			   RdrNode pTrueChild,
    			   RdrNode pFalseChild,
    			   String pCondition,
    			   Element pConclusion,
    			   Element pCornerStone) {
 
       parent        = pParent;
       trueChild     = pTrueChild;
       falseChild    = pFalseChild;
       condition     = pCondition;
       conclusion    = new RdrConclusion(pConclusion);
       cornerstone   = pCornerStone;
    }


	/** 
	 *  Construct a node from an xml representation
	 *  @param xml - the xml that describes the new node
	 */
     public RdrNode(String xml) { fromXML(xml); }


    /**
   	 *  Construct a node from an XNode representation
   	 *  @param node - the x-node that describes the new node
   	 */
    public RdrNode(XNode node) {
         fromXNode(node);
    }


    /**
     *  Construct a node with the basic values provided
	 *  @param parent - the parent node of this node
	 *  @param condition - the condition stored in this node
	 *  @param conclusion - the conclusion stored in this node
	 */
    public RdrNode(RdrNode parent, String condition, Element conclusion) {
    	this(parent, null, null, condition, conclusion, null) ;
    }


    /**
     *  Construct a node with the basic values provided
	 *  @param condition - the condition stored in this node
	 *  @param conclusion - the conclusion stored in this node
     *  @param cornerstone - the data set that led to the creation of this node
	 */
    public RdrNode(String condition, Element conclusion, Element cornerstone) {
    	this(null, null, null, condition, conclusion, cornerstone) ;
    }


    /**
     *  Construct a node with the basic values provided
	 *  @param condition - the condition stored in this node
	 *  @param conclusion - the conclusion stored in this node
     *  @param cornerstone - the data set that led to the creation of this node
	 */
    public RdrNode(String condition, RdrConclusion conclusion, Element cornerstone) {
    	this(null, null, null, condition, conclusion.toElement(), cornerstone) ;
    }


    public RdrNode clone() throws CloneNotSupportedException {
        return (RdrNode) super.clone();
    }

//===========================================================================//
	

    // GETTERS //
    
    public long getNodeId(){
        return id;
    }

    public String getCondition() {
        return condition;
    }

    public RdrConclusion getConclusion() {
        return conclusion;
    }

    public Element getCornerStone() {
        return cornerstone;
    }

    public RdrNode getFalseChild(){
        return falseChild;
    }

    public RdrNode getTrueChild(){
        return trueChild;
    }
    
    public RdrNode getParent() {
    	return parent;
    }
    
    public String getDescription() {
        return description;
    }

    public boolean isLeaf() { return trueChild == null && falseChild == null; }
    

//===========================================================================//
	

    // SETTERS //

    public void setNodeID(long nodeID) { id = nodeID; }
    
    public void setCondition(String newCondition) {
        condition = newCondition;
    }

    public void setConclusion(RdrConclusion newConclusion) {
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
    
    public void setDescription(String desc) {
        description = desc;
    }

    public void setAttributes(YAttributeMap map, String taskName) {
        _attributes = map;
        if (taskName != null) _attributes.put("taskName", taskName);
    }


    /**
     * Compares another node to this for equality in condition, conclusion and
     * cornerstone values
     * @param other the other node
     * @return true if they match on the 3 c's
     */
    public boolean hasIdenticalContent(RdrNode other) {
        return other != null && condition.equals(other.getCondition()) &&
                conclusion.equals(other.getConclusion()) &&
                equalContent(cornerstone, other.getCornerStone());
    }


    private boolean equalContent(Element e, Element eOther) {
        return JDOMUtil.elementToString(e).equals(JDOMUtil.elementToString(eOther));
    }


    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) &&
                id == ((RdrNode) o).id;

    }

    public int hashCode() {
        if (id > 0) return (int) (id ^ (id >>> 32));
        if (condition != null) return condition.hashCode();
        return super.hashCode();
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
    *  @return an RdrPair with two nodes: [0] the last satisfied node
    *                                     [1] the last node searched
    */
    public RdrPair search(Element caseData, RdrNode lastTrueNode) {
        RdrPair pair = null;

        try {
	        if (EVALUATOR.evaluate(condition, caseData)) {  // if condition evals to True
	            if (trueChild == null) {                    // ...and no exception rule

                    // this is last satisfied and last searched
                    pair = new RdrPair(this, this);
	            }
	            else {                                      // test the exception rule
	                pair = trueChild.search(caseData, this);
	            }
	        }
	        else {                                         // if condition evals to False
	            if (falseChild == null) {                  // ...and no if-not rule

                    // pass on last satisfied, and this is last searched
	                pair = new RdrPair(lastTrueNode, this);
	            }
	            else {                                     // test the next if-not rule
	                pair = falseChild.search(caseData, lastTrueNode);
	            }
	        }
	    }
	    catch (RdrConditionException rde) {               // bad condition found
            LogManager.getLogger(RdrNode.class).error("Rule Search Exception: {}",
                    rde.getMessage());
      }
      return pair ;
   }
   
//===========================================================================//
	
    /** returns a String representation of this node */
    public String toString(){
        StringBuilder s = new StringBuilder("RDR Node: ");
        s.append("id-").append(id);
        if (parent != null) s.append(" parent-").append(parent.getNodeId());
        return s.toString();
    }

//===========================================================================//

    public String toXML() {
        return toXNode().toPrettyString();
    }
    
    
    public XNode toXNode() {
        XNode node = new XNode("ruleNode");
        if (_attributes != null) node.addAttributes(_attributes);
        node.addChild("id", id);
        node.addChild("parent", parent != null ? parent.getNodeId() : -1);
        node.addChild("trueChild", trueChild != null ? trueChild.getNodeId() : -1);
        node.addChild("falseChild", falseChild != null ? falseChild.getNodeId() : -1);
        node.addChild("condition", condition, true);                // escape contents
        if (conclusion != null) {
            node.addChild(conclusion.toXNode());
        }
        if (cornerstone != null) node.addContent(JDOMUtil.elementToString(cornerstone));
        if (description != null) node.addChild("description", description);
        return node;
    }
    
    
    public void fromXML(String xml) {
        fromXNode(new XNodeParser().parse(xml));
    }


    /**
     * Only called when adding a new node, so it will have no id, parent or children,
     * or for WorkletRunner transport, where connected nodes are not required
     * @param xNode
     */
    protected void fromXNode(XNode xNode) {
        if (xNode != null) {
            _attributes = new YAttributeMap(xNode.getAttributes());
            id = StringUtil.strToInt(xNode.getChildText("id"), -1);
            parent = null;
            trueChild = null;
            falseChild = null;
            condition = xNode.getChildText("condition", true);     // decode escapes
            XNode concNode = xNode.getChild("conclusion");
            if (concNode != null) conclusion = new RdrConclusion(concNode.toElement());
            XNode cornNode = xNode.getChild("cornerstone");
            if (cornNode != null) cornerstone = cornNode.toElement();
            description = xNode.getChildText("description");
        }
    }


    // For Hibernate

    private String getCornerstoneString() {
        return JDOMUtil.elementToString(cornerstone);
    }

    private void setCornerstoneString(String css) {
        cornerstone = JDOMUtil.stringToElement(css);
    }

}