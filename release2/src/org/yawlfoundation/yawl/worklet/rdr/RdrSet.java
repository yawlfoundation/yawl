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
import org.jdom.Document;
import org.jdom.Element;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.worklet.support.Library;

import java.util.ArrayList;
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
 *      - task constraint violations (i.e. while the task is executing): set of trees,
 *           one for each task in the specification
 *      - spec-level external triggers: one RdrTree
 *      - task-level external triggers: set of trees, one for each task in the
 *           specification
 *
 *  @author Michael Adams
 *  v0.8, 04-09/2006
 */
 /*  ==========        ===========        ===========
 *  | RdrSet | 1----M | RdrTree | 1----M | RdrNode |
 *  ==========        ===========        ===========
 *     ^^^
 */

public class RdrSet {

    private YSpecificationID _specID ;
    private String _processName;
    private boolean _hasRules = false ;    // some specs will have no rules defined

    // Pre & Post case constraints, and case external, have only one tree each
    private RdrTree _specPreTree = null;
    private RdrTree _specPostTree = null;
    private RdrTree _specExternalTree = null;

    // The rest may have a tree for each task
    private List<RdrTree> _selectionTrees = null;
    private List<RdrTree> _preTrees = null ;
    private List<RdrTree> _postTrees = null ;
    private List<RdrTree> _abortTrees = null ;
    private List<RdrTree> _timeoutTrees = null ;
    private List<RdrTree> _resourceTrees = null ;
    private List<RdrTree> _violationTrees = null ;
    private List<RdrTree> _externalTrees = null ;

    private static Logger _log ;


    /**
     * Default constructor
     * @param specID the specification this rule set belongs to
     */
    public RdrSet(YSpecificationID specID) {
        _specID = specID ;
        _log = Logger.getLogger(this.getClass());
        _hasRules = loadRules();
    }
    
    public RdrSet(String processName) {
         _processName = processName ;
         _log = Logger.getLogger(this.getClass());
         _hasRules = loadRules();
     }
    

//===========================================================================//

    /** clears and rebuilds the RdrSet */
    public void refresh() {
        loadRules();
    }

//===========================================================================//

    /** @return true if this spec's ruleset is not empty */
    public boolean hasRules() {
        return _hasRules ;
    }

//===========================================================================//

    /**
     * Gets the previously loaded rdrTree for rules defined at the case-level
     * @param treeType which set of rules are required
     * @return the specified rule tree
     */
    public RdrTree getTree(RuleType treeType) {
        switch (treeType) {
            case CasePreconstraint   : return _specPreTree;
            case CasePostconstaint   : return _specPostTree;
            case CaseExternalTrigger : return _specExternalTree;
            default: return null;
        }
    }

    
    public void addTree(RdrTree tree, RuleType treeType) {
        switch (treeType) {
            case CasePreconstraint       : _specPreTree = tree; break;
            case CasePostconstaint       : _specPostTree = tree; break;
            case CaseExternalTrigger     : _specExternalTree = tree; break;
            case ItemSelection           : 
                if (_selectionTrees == null) _selectionTrees = new ArrayList<RdrTree>();
                _selectionTrees.add(tree); break;
            case ItemPreconstraint       :
                if (_preTrees == null) _preTrees = new ArrayList<RdrTree>();
                _preTrees.add(tree); break;
            case ItemPostconstaint       :
                if (_postTrees == null) _postTrees = new ArrayList<RdrTree>();
                _postTrees.add(tree); break;
            case ItemAbort               :
                if (_abortTrees == null) _abortTrees = new ArrayList<RdrTree>();
                _abortTrees.add(tree); break;
            case ItemTimeout             :
                if (_timeoutTrees == null) _timeoutTrees = new ArrayList<RdrTree>();
                _timeoutTrees.add(tree); break;
            case ItemResourceUnavailable :
                if (_resourceTrees == null) _resourceTrees = new ArrayList<RdrTree>();
                _resourceTrees.add(tree); break;
            case ItemConstraintViolation :
                if (_violationTrees == null) _violationTrees = new ArrayList<RdrTree>();
                _violationTrees.add(tree); break;
            case ItemExternalTrigger     :
                if (_externalTrees == null) _externalTrees = new ArrayList<RdrTree>();
                _externalTrees.add(tree); break;
        }
    }


//===========================================================================//

    /**
     *  Retrieves a specified RdrTree for the specified task
     *
     *  @param treeType - the tree exception type
     *  @param taskName - the task the tree represents
     *  @return the RDRTree for the specified spec and task
     */
    public RdrTree getTree(RuleType treeType, String taskName) {
        List<RdrTree> trees = null ;

        // get the appropriate list of RdrTrees
        switch (treeType) {
            case ItemSelection:
                trees = _selectionTrees; break ;
            case ItemPreconstraint:
                trees = _preTrees; break ;
            case ItemPostconstaint:
                trees = _postTrees; break ;
            case ItemAbort:
                trees = _abortTrees; break ;
            case ItemTimeout:
                trees = _timeoutTrees; break ;
            case ItemResourceUnavailable:
                trees = _resourceTrees; break ;
            case ItemConstraintViolation:
                trees = _violationTrees; break ;
            case ItemExternalTrigger:
                trees = _externalTrees; break ;
        }

        // find the rule tree for this task
        return getTreeForTask(trees, taskName) ;
    }

//===========================================================================//

    /**
     * Gets the tree for the specified task from the List of trees passed
     * @param list the list of trees to search
     * @param task the name of the task
     * @return the tree for the specified task
     */
    private RdrTree getTreeForTask(List<RdrTree> list, String task) {
        if (list == null) return null ;               // no rules for this task

        // compare the taskId of each tree with the task to find
        for (RdrTree tree : list) {
            if (task.equals(tree.getTaskId())) {
                return tree ;                         // return the match
            }
        }
        return null ;                                 // no matches found
    }

//===========================================================================//

    /** Load a set of trees from rules file
     * @return true if the rules were loaded successfully
     */
    private boolean loadRules() {
        String fileName = getName() + ".xrs" ;        // xrs = Xml Rule Set
        Document doc = null;                                 // doc to hold rules

        String rulepath = Library.wsRulesDir + fileName ;

        if (Library.fileExists(rulepath)) doc = JDOMUtil.fileToDocument(rulepath);
        
        return load(doc);
    }
    
    
    private boolean load(Document doc) {
        if (doc == null) return false ;              // no such file or unsuccessful load

        try {
            Element root = doc.getRootElement();      // spec

            // extract the rule nodes for each exception type
            for (Object o : root.getChildren()) {       // these are exception type tags
                Element e = (Element) o ;
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


    public boolean save() {
        XNode ruleSet = toXNode();
        if (ruleSet.hasChildren()) {
            String rulepath = Library.wsRulesDir + getName() + ".xrs";
            StringUtil.stringToFile(rulepath, ruleSet.toPrettyString(true));
        }
        return ruleSet.hasChildren();
    }
    
    
    public String toXML() {
         return toXNode().toPrettyString();
    }
    
    
    private XNode toXNode() {
        XNode ruleSet = createRootXNode();
        treeToXNode(_specPreTree, ruleSet, "constraints", "case", "pre");
        treeToXNode(_specPostTree, ruleSet, "constraints", "case", "post");
        treeToXNode(_specExternalTree, ruleSet,  "external", "case");
        treeListToXNode(_selectionTrees, ruleSet, "selection");
        treeListToXNode(_preTrees, ruleSet, "constraints", "item", "pre");
        treeListToXNode(_postTrees, ruleSet, "constraints", "item", "post");
        treeListToXNode(_abortTrees, ruleSet, "abort");
        treeListToXNode(_timeoutTrees, ruleSet, "timeout");
        treeListToXNode(_resourceTrees, ruleSet, "resourceUnavailable");
        treeListToXNode(_violationTrees, ruleSet, "violation");
        treeListToXNode(_externalTrees, ruleSet, "external", "item");
        return ruleSet;
    }
    
    private XNode createRootXNode() {
        XNode xRoot;
        if (_specID != null) {
            xRoot = new XNode("spec");
        }
        else if (_processName != null) {
            xRoot = new XNode("process");
        }
        else {
            xRoot = new XNode("root");            // default - should never get to this
        }

        String name = getName();
        if (name != null) xRoot.addAttribute("name", name);

        return xRoot;
    }
    
    
    protected String getName() {
        return _specID != null ? _specID.getUri() : _processName;
    }


    protected String getProcessName() { return _processName; }

    protected YSpecificationID getSpecificationID() { return _specID; }


    private void treeListToXNode(List<RdrTree> treeList, XNode parent, String... names) {
        if (treeList != null) {
            for (RdrTree tree : treeList) {
                treeToXNode(tree, parent, names);
            }
        }
    }
    
    private void treeToXNode(RdrTree tree, XNode parent, String... names) {
        if (tree != null) {
            XNode xNode = getOrCreateNode(parent, names);
            XNode childTree = tree.toXNode();
            if (childTree.getName().equals("task")) {
                xNode.addChild(childTree);                    // task level tree
            }
            else {
                xNode.addChildren(childTree.getChildren());   // case level tree
            }
        }
    }


    private XNode getOrCreateNode(XNode parent, String... names) {
        XNode node = parent;
        for (String name : names) {
            node = node.getOrAddChild(name);
        }
        return node;
    }

//===========================================================================//

    /**
     * Constructs a rule tree for each set of constraint rules in the rules file
     * i.e. pre & post constraint rule sets at the case and task levels
     * @param e the JDOM Element representation of the rule tree
     * @return true if the rules were loaded successfully
     */
    private boolean getConstraintRules(Element e) {
        for (Object o : e.getChildren()) {
            Element eCon = (Element) o;
            String conName = eCon.getName();
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

    /**
     * Constructs a rule tree for each set of external rules in the rules file
     * i.e. pre & post constraint rule sets at the case and task levels
     * @param e the JDOM Element representation of the rule tree
     * @return true if the rules were loaded successfully
     */
    private boolean getExternalRules(Element e) {
        for (Object o : e.getChildren()) {                // 'case' or 'item'
            Element eChild = (Element) o ;
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
     * Construct a tree for each task specified in the rules file
     * @param e - the Element containing the rules of each task
     * @return the list of trees constructed
     */
    private List<RdrTree> getRulesFromElement(Element e) {
        List<RdrTree> treeList = new ArrayList<RdrTree>() ;
        for (Object o : e.getChildren()) {
            treeList.add(buildTree((Element) o)) ;
        }
        return treeList ;
    }


//===========================================================================//

    /**
     * Constructs an RdrTree from the JDOM Element passed
     * @param task - the Element containing a representation of the tree
     * @return the list of trees constructed
     */
    private RdrTree buildTree(Element task) {
        String taskId = task.getAttributeValue("name");
        RdrTree result = new RdrTree(taskId);      

        List nodeList = task.getChildren();    //the rdr nodes for this task

        //get the root node (always stored as node 0)
        Element rootNode = (Element) nodeList.get(0);
        RdrNode root = buildFromNode(rootNode, nodeList);  // build from root
        result.setRootNode(root);
        return result;
    }

//===========================================================================//

    /**
     *  recursively build a tree from the node and list passed 
     *  @param xNode contains the xml elements for a single RDR node definition
     *  @param nodeList is the list of all xNodes for a single task
     *  @return the root node of the constructed tree
     */
    private RdrNode buildFromNode(Element xNode, List nodeList) {
        String childId;
        RdrNode rNode = new RdrNode();

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

        // find the node with this id
        for (Object o : nodeList) {
            Element eNode = (Element) o;
            if (id.equals(eNode.getChildText("id"))) return eNode;
        }
        return null ;
    }
    
    
//===========================================================================//
    
    public String toString() {
        String id = _specID != null ? _specID.toString() : _processName;
        return "RuleSet for process: " + id;
    }


    public String dump() {
        StringBuilder s = new StringBuilder("##### RDR SET #####");
        s.append(Library.newline);

        String id = _specID != null ? _specID.toString() : _processName;
        Library.appendLine(s, "SPECIFICATION ID", id);
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
    
    
    public void fromXML(String xml) {
        load(JDOMUtil.stringToDocument(xml));
    }

//===========================================================================//
//===========================================================================//

}
