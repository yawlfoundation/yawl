/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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
import org.jdom.Document;
import org.jdom.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.worklet.WorkletService;
import org.yawlfoundation.yawl.worklet.support.Library;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *  Maintains a set of RdrTrees for a particular specification, for each of the following
 *  purposes (potentially):
 *   - selection: set of trees, one for each worklet enabled task
 *   - exception:
 *      - spec pre-constraints: one RdrTree
 *      - spec post-constraints: one RdrTree
 *      - task pre-constraints: set of trees, one for each task in the specification
 *      - task post-constraints: set of trees, one for each task in the specification
 *      - task aborts: set of trees, one for each task in the specification
 *      - time-outs: set of trees, one for each task in the specification
 *      - unavailable resources: set of trees, one for each task in the specification
 *      - task constraint violations (i.e. while the task is executing: set of trees,
 *           one for each task in the specification
 *      - spec-level external triggers: one RdrTree
 *      - task-level external triggers: set of trees, one for each task in the
 *           specification
 *
 *  ==========        ===========        ===========
 *  | RdrSet | 1----M | RdrTree | 1----M | RdrNode |
 *  ==========        ===========        ===========
 *     ^^^
 *
 *  @author Michael Adams
 *  v0.8, 04-09/2006
 */

public class RdrSet {

    private String _specID ;
    private boolean _hasRules = false ;    // some specs will have no rules defined

    // Pre & Post case constraints have only one tree each
    private RdrTree _specPreTree = null;
    private RdrTree _specPostTree = null;
    private RdrTree _specExternalTree = null;

    // The rest have a tree for each task - so each is an ArrayList of RdrTrees
    private ArrayList _selectionTrees = null;
    private ArrayList _preTrees = null ;
    private ArrayList _postTrees = null ;
    private ArrayList _abortTrees = null ;
    private ArrayList _timeoutTrees = null ;
    private ArrayList _resourceTrees = null ;
    private ArrayList _violationTrees = null ;
    private ArrayList _externalTrees = null ;


    private static Logger _log ;

    /** Default constructor */
	public RdrSet(String specID) {
         _specID = specID ;
        _log = Logger.getLogger("org.yawlfoundation.yawl.worklet.rdr.RdrSet");
        _hasRules = loadRules();
    }

//===========================================================================//
	
	/** clears the RdrSet for rebuilding */ 
	public void refresh() {
        loadRules();
	}

//===========================================================================//

    /** returns true if this spec's ruleset has associated rules */
    public boolean hasRules() {
        return _hasRules ;
    }

//===========================================================================//

    /**
     * gets the previously loaded rdrTree for rules defined at the case-level
     * @param treeType - which set of rules are required
     * @return the specified rule tree
     */
    public RdrTree getTree(int treeType) {
        if (treeType == WorkletService.XTYPE_CASE_PRE_CONSTRAINTS)
           return _specPreTree ;
        else if (treeType == WorkletService.XTYPE_CASE_POST_CONSTRAINTS)
           return _specPostTree ;
        else if (treeType == WorkletService.XTYPE_CASE_EXTERNAL_TRIGGER)
           return _specExternalTree ;
        else
           return null ;
    }

//===========================================================================//

    /**
     *  Retrieves a specified RdrTree for the specified task
     *  @param treeType - the tree exception type
     *  @param taskName - the task the tree represents
     *  @return the RDRTree for the specified spec and task
     */

    public RdrTree getTree(int treeType, String taskName) {

        ArrayList trees = null ;

         // get the appropriate list of RdrTrees
        switch (treeType) {
            case WorkletService.XTYPE_SELECTION:
     		     trees = _selectionTrees; break ;
            case WorkletService.XTYPE_ITEM_PRE_CONSTRAINTS:
                 trees = _preTrees; break ;
            case WorkletService.XTYPE_ITEM_POST_CONSTRAINTS:
                 trees = _postTrees; break ;
            case WorkletService.XTYPE_WORKITEM_ABORT:
                 trees = _abortTrees; break ;
            case WorkletService.XTYPE_TIMEOUT:
                 trees = _timeoutTrees; break ;
            case WorkletService.XTYPE_RESOURCE_UNAVAILABLE:
                 trees = _resourceTrees; break ;
            case WorkletService.XTYPE_CONSTRAINT_VIOLATION:
                 trees = _violationTrees; break ;
            case WorkletService.XTYPE_ITEM_EXTERNAL_TRIGGER:
                 trees = _externalTrees; break ;
        }

        // find the rule set for this task
		return getTreeForTask(trees, taskName) ;
	}
	
//===========================================================================//
	
    /** get the tree for the specified task from the List of trees passed */
	private RdrTree getTreeForTask(ArrayList list, String task) {
		if (list == null) return null ;               // no rules for this task
		
		// compare the taskId of each tree with the task to find 
		for (int i=0;i<list.size();i++) {
			RdrTree tree = (RdrTree) list.get(i) ;
			if (task.compareTo(tree.getTaskId()) == 0) {
				return tree ;                         // return the match 
			}	
		}
		return null ;                                 // no matches found
	}

//===========================================================================//
	
    /** load a set of trees from rules file */
	private boolean loadRules() {
		String fileName = _specID + ".xrs" ;               // xrs = Xml Rule Set
        Document doc ;                                 // doc to hold rules

        String rulepath = Library.wsRulesDir + fileName ;

        if (Library.fileExists(rulepath))
           doc = JDOMUtil.fileToDocument(rulepath);
        else return false;                           // no such file

        if (doc == null) return false ;              // unsuccessful file load

        try {
           Element root = doc.getRootElement();      // spec 
           
           List exTypes = root.getChildren();     // these are exception type tags

           // extract the rule nodes for each exception type
            Iterator exTypesItr = exTypes.iterator();
            while (exTypesItr.hasNext()) {
                Element e = (Element) exTypesItr.next() ;
                String exName = e.getName();
                if (exName.equalsIgnoreCase("selection")) {
                    _selectionTrees = getRulesFromElement(e);
                }
                else if (exName.equalsIgnoreCase("abort")) {
                    _abortTrees = getRulesFromElement(e);
                }
                else if (exName.equalsIgnoreCase("timeout")) {
                    _timeoutTrees = getRulesFromElement(e);
                }
                else if (exName.equalsIgnoreCase("resourceUnavailable")) {
                    _resourceTrees = getRulesFromElement(e);
                }
                else if (exName.equalsIgnoreCase("violation")) {
                    _violationTrees = getRulesFromElement(e);
                }
                else if (exName.equalsIgnoreCase("external")) {
                    getExternalRules(e) ;
                }
                else if (exName.equalsIgnoreCase("constraints")) {
                    getConstraintRules(e) ;
                }

                // if 'task' is a child of 'root', this is a version one rules file
                // so treat it as though it contains selection rules only 
                else if (exName.equalsIgnoreCase("task")) {
                    _selectionTrees = getRulesFromElement(root);
                }
            }
            return true ;
        }
        catch (Exception ex) {
           _log.error("Exception retrieving Element from rules file", ex);
           return false ;
        }
    }

//===========================================================================//

   /** constructs a rule tree for each set of constraint rules in the rules file
    *  i.e. pre & post constraint rule sets at the case and task levels
    */
   private boolean getConstraintRules(Element e) {

        List constraintTypes = e.getChildren();

        Iterator consItr = constraintTypes.iterator();
        while (consItr.hasNext()) {
            Element eCon = (Element) consItr.next() ;
            String conName = eCon.getName() ;
            Element preCase = eCon.getChild("pre");
            Element postCase = eCon.getChild("post");

            if (conName.equalsIgnoreCase("case")) {
                if (preCase != null)  _specPreTree  = buildTree(preCase) ;
                if (postCase != null) _specPostTree = buildTree(postCase) ;
            }
            else if (conName.equalsIgnoreCase("item")) {
                if (preCase != null)  _preTrees = getRulesFromElement(preCase) ;
                if (postCase != null) _postTrees = getRulesFromElement(postCase) ;
            }
        }
        return true ;
    }

//===========================================================================//

    /** constructs a rule tree for each set of external rules in the rules file
     *  i.e. pre & post constranit rule sets at the case and task levels
     */
    private boolean getExternalRules(Element e) {

         List levelTypes = e.getChildren();                 // 'case' or 'item'

         Iterator levelItr = levelTypes.iterator();
         while (levelItr.hasNext()) {
             Element eChild = (Element) levelItr.next() ;
             String childName = eChild.getName() ;

             if (childName.equalsIgnoreCase("case"))
                 _specExternalTree  = buildTree(eChild) ;
             else if (childName.equalsIgnoreCase("item"))
                  _externalTrees = getRulesFromElement(eChild) ;
         }
         return true ;
     }

//===========================================================================//

    /**
     * construct a tree for each task specified in the rules file
     * @param e - the Element containing the rules of each task
     * @return the list of trees constructed
     */
    private ArrayList getRulesFromElement(Element e) {

        ArrayList treeList = new ArrayList() ;

        List tasks = e.getChildren();

        Iterator tasksItr = tasks.iterator();
        while (tasksItr.hasNext()) {
            Element eTask = (Element) tasksItr.next() ;
        	RdrTree tree = buildTree(eTask) ;
           	treeList.add(tree) ;
        }

        return treeList ;
    }


//===========================================================================//
	
    /** constructs an RdrTree from the JDOM Element passed */
	private RdrTree buildTree(Element task) {
		String taskId = task.getAttributeValue("name");
		RdrTree result = new RdrTree(_specID, taskId) ;
		
		List nodeList = task.getChildren() ;    //the rdr nodes for this task
	
	    //get the root node (always stored as node 0)
	    Element rootNode = (Element) nodeList.get(0) ;
	    RdrNode root = buildFromNode(rootNode, nodeList) ;  // build from root
	    
	    result.setRootNode(root) ;

	    return result ;
	}
	
//===========================================================================//
	
    /** 
     *  recursively build a tree from the node and list passed 
	 *  @param xNode contains the xml elements for a single RDR node definition
	 *  @param nodeList is the list of all xNodes for a single task 
     */
	private RdrNode buildFromNode(Element xNode, List nodeList) {
		String childId ;
		RdrNode rNode = new RdrNode() ;
		
		// populate the node
		rNode.setNodeId(xNode.getChildText("id")) ;
		rNode.setCondition(xNode.getChildText("condition"));
		rNode.setConclusion(xNode.getChild("conclusion"));
		rNode.setCornerStone(xNode.getChild("cornerstone"));
		
		// do true branch recursively
		childId = xNode.getChildText("trueChild") ;
		if (childId.compareTo("-1") != 0) {
			Element eTrueChild = getNodeWithId(childId, nodeList) ;
			rNode.setTrueChild(buildFromNode(eTrueChild, nodeList));
			rNode.getTrueChild().setParent(rNode) ;
		}	
		
		// do false branch recursively	
		childId = xNode.getChildText("falseChild") ;
		if (childId.compareTo("-1") != 0) {
			Element eFalseChild = getNodeWithId(childId, nodeList) ;
			rNode.setFalseChild(buildFromNode(eFalseChild, nodeList));
			rNode.getFalseChild().setParent(rNode) ;
		}
		return rNode;
	}
	
//===========================================================================//
	
    /** find the node with the id passed in the List of xml nodes */
	private Element getNodeWithId(String id, List nodeList) {
		String nodeId ;
		
       // find the node with this id
       for (int i=0;i<nodeList.size();i++) {
       	    Element eNode = (Element) nodeList.get(i) ;
       	    nodeId = eNode.getChildText("id") ;
       	    if ((id.compareTo(nodeId) == 0)) return eNode ;
       }
       return null ;
	}

//===========================================================================//


    public String toString() {
        StringBuilder s = new StringBuilder("##### RDR SET #####");
        s.append(Library.newline);

        Library.appendLine(s, "SPECIFICATION ID", _specID);
        Library.appendLine(s, "SET HAS RULES", String.valueOf(_hasRules));
        s.append(Library.newline);
 
        String specPreTree = (_specPreTree == null)? "null" : _specPreTree.toString();
        String specPostTree = (_specPostTree == null)? "null" : _specPostTree.toString();
        String specExTree = (_specExternalTree == null)? "null" : _specExternalTree.toString();
        String selTrees = (_selectionTrees == null)? "null" : _selectionTrees.toString();
        String preTrees = (_preTrees == null)? "null" : _preTrees.toString();
        String postTrees = (_postTrees == null)? "null" : _postTrees.toString();
        String abortTrees = (_abortTrees == null)? "null" : _abortTrees.toString();
        String toTrees = (_timeoutTrees == null)? "null" : _timeoutTrees.toString();
        String resTrees = (_resourceTrees == null)? "null" : _resourceTrees.toString();
        String violTrees = (_violationTrees == null)? "null" : _violationTrees.toString();
        String exTrees = (_externalTrees == null)? "null" : _externalTrees.toString();

        Library.appendLine(s, "CASE PRE_CONSTRAINT RULE TREE", specPreTree);
        s.append(Library.newline);
        Library.appendLine(s, "CASE POST_CONSTRAINT RULE TREE", specPostTree);
        s.append(Library.newline);
        Library.appendLine(s, "CASE EXTERNAL EXCEPTION RULE TREE", specExTree);
        s.append(Library.newline);
        Library.appendLine(s, "SELECTION TREES", selTrees);
        s.append(Library.newline);
        Library.appendLine(s, "ITEM PRE_CONSTRAINT RULE TREES", preTrees);
        s.append(Library.newline);
        Library.appendLine(s, "ITEM POST_CONSTRAINT RULE TREES", postTrees);
        s.append(Library.newline);
        Library.appendLine(s, "ITEM ABORT RULE TREES", abortTrees);
        s.append(Library.newline);
        Library.appendLine(s, "ITEM TIMEOUT RULE TREES", toTrees);
        s.append(Library.newline);
        Library.appendLine(s, "RESOURCE UNAVAILABLE RULE TREES", resTrees);
        s.append(Library.newline);
        Library.appendLine(s, "CONSTRAINT VIOLATION RULE TREES", violTrees);
        s.append(Library.newline);
        Library.appendLine(s, "ITEM EXTERNAL EXCEPTION RULE TREES", exTrees);
        s.append(Library.newline);

        return s.toString();
    }
	
//===========================================================================//
//===========================================================================//

}
