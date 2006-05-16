/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.worklet.rdr;

import au.edu.qut.yawl.worklet.support.*;

import java.util.*;
import java.io.*;

import org.jdom.* ;
import org.jdom.input.*;

import org.apache.log4j.Logger;

/**
 *  Maintains a set of RdrTrees for a particular specification. There is 
 *  one RdrTree for each worklet enabled task in a specification.
 *
 *  ==========        ===========        ===========
 *  | RdrSet | 1----M | RdrTree | 1----M | RdrNode |
 *  ==========        ===========        ===========
 *     ^^^
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.7, 10/12/2005
 */

public class RdrSet {

    /* _ruleSets: - key is specId (i.e. its name) as String
	 *            - value is an ArrayList of RdrTrees, one for each task 
	 *              in this spec
	 */
	private Map _ruleSets ;
	
	private static Logger _log ;

    private int _ruleType ;

    //constants to specify which set of rules to load/use
    public static final int SELECTION = 0 ;
    public static final int EXCEPTION = 1 ;


    /** Default constructor */
	public RdrSet() {
		_ruleSets = new HashMap() ;
        _ruleType = SELECTION ;
        _log = Logger.getLogger("au.edu.qut.yawl.worklet.rdr.RdrSet");
    }

    /** Contructor specifying a ruletype */
    public RdrSet(int rtype) {
        _ruleSets = new HashMap() ;
        _ruleType = rtype ;
        _log = Logger.getLogger("au.edu.qut.yawl.worklet.rdr.RdrSet");
    }

//===========================================================================//
	
	/** clears the RdrSet for rebuilding */ 
	public void refresh() {
		_ruleSets = new HashMap();
	}

//===========================================================================//

    /**
     *  Retrieves a specified RdrTree 
     *  @param specId - the specification the tree serves
     *  @param taskId - the task the tree represents
     *  @return the RDRTree for the specified spec and task
     */	
	public RdrTree getRdrTree(String specId, String taskId) {
		
		// load rules into Set for this spec if not already loaded		
		if (! _ruleSets.containsKey(specId))
			if (! loadRulesForSpec(specId)) return null ;   // no rules loaded	
			
		// get the List of RdrTrees for this spec
		ArrayList trees = (ArrayList) _ruleSets.get(specId) ;
		
		//get task name from task id
		String taskName = Library.getTaskNameFromId(taskId) ;
				
		// find the rule set for this task
		return getTreeFromList(trees, taskName) ;
	}
	
//===========================================================================//
	
    /** get the tree for the specified task from the List of trees passed */
	private RdrTree getTreeFromList(ArrayList list, String task) {
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
	
    /** load a tree from file */
	private boolean loadRulesForSpec(String specId) {
		String fileName = specId + ".xrs" ;           // xrs = Xml Rule Set
		String rulepath ;                             // path to xrs files
        Document doc ;                                // doc to hold rules

        if (_ruleType == SELECTION)
            rulepath = Library.wsSelRulesDir + fileName ;
        else
            rulepath = Library.wsExRulesDir + fileName ;

        if (Library.fileExists(rulepath))
           doc = loadDocument(rulepath);
        else return false;                           // no such file

        if (doc == null) return false ;              // unsuccessful file load
	    
        try {
           Element root = doc.getRootElement();      // spec 
           
           List childTasks = root.getChildren();     // these are tasks
           ArrayList treeList = new ArrayList() ;
           
           // extract the rule nodes for each task
           for (int i=0;i<childTasks.size();i++) {
           	   Element eleTask = (Element) childTasks.get(i) ;
           	   RdrTree tree = buildTree(specId, eleTask) ;
           	   treeList.add(tree) ;
           }
           
           // store the loaded trees for this spec
           _ruleSets.put(specId, treeList) ;
           
           return true ;
        }   
        catch (Exception e) {
           _log.error("Exception retrieving Element from rules file", e);
           return false ;
        }
     
    }    
	    
//===========================================================================//
	
	/** loads the specified xml file into a JDOM Document */
	private Document loadDocument(String fileName) {

        try {
 	        SAXBuilder builder = new SAXBuilder();
            return builder.build(new File(fileName));	
	    } 
	    catch (JDOMException jdx) {
		     _log.error("LoadDocument method: JDOM Exception parsing file: " +
			             fileName, jdx);
	        return null ;
	    } 
	    catch (FileNotFoundException fnfx) {
			_log.error("LoadDocument method: File Not Found Exception: " +
			              fileName, fnfx);
	        return null ;
	    }
	    catch (IOException iox) {
			_log.error("LoadDocument method: Java IO Exception with file: " +
			             fileName, iox);
	        return null ;
	    } 

	}
	
//===========================================================================//
	
    /** constructs an RdrTree from the JDOM Element passed */	
	private RdrTree buildTree(String specId, Element task) {
		String taskId = task.getAttributeValue("name");
		RdrTree result = new RdrTree(specId, taskId) ;  
		
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
		rNode.setConclusion(xNode.getChildText("conclusion"));
		rNode.setCornerStone(xNode.getChildText("cornerstone"));
		
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
//===========================================================================//

}
