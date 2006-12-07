/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.worklet.rdr;

import au.edu.qut.yawl.worklet.support.*;
import au.edu.qut.yawl.worklet.WorkletService;
import au.edu.qut.yawl.util.JDOMConversionTools;

import java.util.*;

import org.jdom.* ;

import org.apache.log4j.Logger;

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
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
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
    private ArrayList<RdrTree> _selectionTrees = null;
    private ArrayList<RdrTree> _preTrees = null ;
    private ArrayList<RdrTree> _postTrees = null ;
    private ArrayList<RdrTree> _abortTrees = null ;
    private ArrayList<RdrTree> _timeoutTrees = null ;
    private ArrayList<RdrTree> _resourceTrees = null ;
    private ArrayList<RdrTree> _violationTrees = null ;
    private ArrayList<RdrTree> _externalTrees = null ;


    private static Logger _log ;

    /** Default constructor */
	public RdrSet(String specID) {
         _specID = specID ;
        _log = Logger.getLogger("au.edu.qut.yawl.worklet.rdr.RdrSet");
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

        ArrayList<RdrTree> trees = null ;

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
	private RdrTree getTreeForTask(ArrayList<RdrTree> list, String task) {
		if (list == null) return null ;               // no rules for this task
		
		// compare the taskId of each tree with the task to find 
        for (RdrTree tree : list) {
            if (task.compareTo(tree.getTaskId()) == 0) {
                return tree;                         // return the match
            }
        }
		return null ;                                 // no matches found
	}

//===========================================================================//
	
    /** load a set of trees from rules file */
	private boolean loadRules() {
		String fileName = _specID + ".xrs" ;           // xrs = Xml Rule Set
        Document doc ;                                 // doc to hold rules

        String rulepath = Library.wsRulesDir + fileName ;

        if (Library.fileExists(rulepath))
           doc = JDOMConversionTools.fileToDocument(rulepath);
        else return false;                           // no such file

        if (doc == null) return false ;              // unsuccessful file load

        try {
           Element root = doc.getRootElement();      // spec 
           
           List<Element> exTypes = root.getChildren();   // these are exception type tags

           // extract the rule nodes for each exception type
            for (Element e : exTypes) {
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

        List<Element> constraintTypes = e.getChildren();

       for (Element eCon : constraintTypes) {
           String conName = eCon.getName();
           Element preCase = eCon.getChild("pre");
           Element postCase = eCon.getChild("post");

           // case pre and post constraint rules have only one tree each
           if (conName.equalsIgnoreCase("case")) {
               if (preCase != null) _specPreTree = buildTree(preCase);
               if (postCase != null) _specPostTree = buildTree(postCase);
           } else if (conName.equalsIgnoreCase("item")) {
               if (preCase != null) _preTrees = getRulesFromElement(preCase);
               if (postCase != null) _postTrees = getRulesFromElement(postCase);
           }
       }
        return true ;
    }

//===========================================================================//

    /** constructs a rule tree for each set of external rules in the rules file
     *  i.e. pre & post constranit rule sets at the case and task levels
     */
    private boolean getExternalRules(Element e) {

         List<Element> levelTypes = e.getChildren();               // 'case' or 'item'

        for (Element eChild : levelTypes) {
            String childName = eChild.getName();

            // case level external rules form exactly one tree
            if (childName.equalsIgnoreCase("case"))
                _specExternalTree = buildTree(eChild);
            else if (childName.equalsIgnoreCase("item"))
                _externalTrees = getRulesFromElement(eChild);
        }
         return true ;
     }

//===========================================================================//

    /**
     * construct a tree for each task specified in the rules file
     * @param e - the Element containing the rules of each task
     * @return the list of trees constructed
     */
    private ArrayList<RdrTree> getRulesFromElement(Element e) {

        ArrayList<RdrTree> treeList = new ArrayList<RdrTree>() ;

        List<Element> tasks = e.getChildren();

        // build a tree for each task specified
        for (Element eTask : tasks) {
            RdrTree tree = buildTree(eTask);
            treeList.add(tree);
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
	private RdrNode buildFromNode(Element xNode, List<Element> nodeList) {
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
	private Element getNodeWithId(String id, List<Element> nodeList) {
		String nodeId ;
		
       // find the node with this id
        for (Element eNode : nodeList) {
            nodeId = eNode.getChildText("id");
            if ((id.compareTo(nodeId) == 0)) return eNode;
        }
       return null ;
	}

//===========================================================================//


    public String toString() {
        StringBuffer s = new StringBuffer("##### RDR SET #####");
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
