package org.yawlfoundation.yawl.worklet.rdr;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.worklet.support.Library;
import org.yawlfoundation.yawl.worklet.support.Persister;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 17/09/2014
 */
public class RdrSetLoader {

    private static final Logger _log  = LogManager.getLogger(RdrSetLoader.class);


    public RdrSet load(YSpecificationID specID) {
        RdrSet rdrSet = loadSet(specID);
        if (rdrSet == null) {
            Map<RuleType, RdrTreeSet> treeMap = loadFile(getFile(specID.getUri()));
            if (! (treeMap == null || treeMap.isEmpty())) {
                rdrSet = new RdrSet(specID);
                rdrSet.setTreeMap(treeMap);
                Persister.insert(rdrSet);
            }
        }
        return rdrSet;
    }


    public RdrSet load(String processName) {
        RdrSet rdrSet = loadSet(processName);
        if (rdrSet == null) {
            Map<RuleType, RdrTreeSet> treeMap = loadFile(getFile(processName));
            if (! (treeMap == null || treeMap.isEmpty())) {
                rdrSet = new RdrSet(processName);
                rdrSet.setTreeMap(treeMap);
                Persister.insert(rdrSet);
            }
        }
        return rdrSet;
    }


    public Map<RuleType, RdrTreeSet> loadFile(File ruleFile) {
        return load(JDOMUtil.fileToDocument(ruleFile));
    }


    public Map<RuleType, RdrTreeSet> load(Document doc) {
        if (doc == null) return Collections.emptyMap();  // no such file or unsuccessful load
        Map<RuleType, RdrTreeSet> treeMap = new HashMap<RuleType, RdrTreeSet>();
        try {
            Element root = doc.getRootElement();      // spec

            // extract the rule nodes for each exception type
            for (Element e : root.getChildren()) {       // these are exception type tags
                String exName = e.getName();
                if (exName.equalsIgnoreCase("selection")) {
                    buildItemLevelTree(treeMap, RuleType.ItemSelection, e);
                }
                else if (exName.equalsIgnoreCase("abort")) {
                    buildItemLevelTree(treeMap, RuleType.ItemAbort, e);
                }
                else if (exName.equalsIgnoreCase("timeout")) {
                    buildItemLevelTree(treeMap, RuleType.ItemTimeout, e);
                }
                else if (exName.equalsIgnoreCase("resourceUnavailable")) {
                    buildItemLevelTree(treeMap, RuleType.ItemResourceUnavailable, e);
                }
                else if (exName.equalsIgnoreCase("violation")) {
                    buildItemLevelTree(treeMap, RuleType.ItemConstraintViolation, e);
                }
                else if (exName.equalsIgnoreCase("external")) {
                    getExternalRules(treeMap, e) ;
                }
                else if (exName.equalsIgnoreCase("constraints")) {
                    getConstraintRules(treeMap, e) ;
                }

                // if 'task' is a child of 'root', this is a version one rules file
                // so treat it as though it contains selection rules only
                else if (exName.equalsIgnoreCase("task")) {
                    buildItemLevelTree(treeMap, RuleType.ItemSelection, root);
                }
            }
            return treeMap;
        }
        catch (Exception ex) {
            _log.error("Exception retrieving rule nodes from rules file", ex);
            return Collections.emptyMap();
        }
    }


    /*******************************************************************************/

    /**
     * Constructs a rule tree for each set of constraint rules in the rules file
     * i.e. pre & post constraint rule sets at the case and task levels
     * @param e the JDOM Element representation of the rule tree
     * @return true if the rules were loaded successfully
     */
    private boolean getConstraintRules(Map<RuleType, RdrTreeSet> treeMap, Element e) {
        for (Element eCon : e.getChildren()) {
            String conName = eCon.getName();
            Element ePre = eCon.getChild("pre");
            Element ePost = eCon.getChild("post");

            if (conName.equalsIgnoreCase("case")) {
                if (ePre != null) buildCaseLevelTree(treeMap, RuleType.CasePreconstraint, ePre) ;
                if (ePost != null) buildCaseLevelTree(treeMap, RuleType.CasePostconstraint, ePost) ;
            }
            else if (conName.equalsIgnoreCase("item")) {
                if (ePre != null) buildItemLevelTree(treeMap, RuleType.ItemPreconstraint, ePre) ;
                if (ePost != null) buildItemLevelTree(treeMap, RuleType.ItemPostconstraint, ePost) ;
            }
        }
        return true ;
    }


    /**
     * Constructs a rule tree for each set of external rules in the rules file
     * i.e. pre & post constraint rule sets at the case and task levels
     * @param e the JDOM Element representation of the rule tree
     * @return true if the rules were loaded successfully
     */
    private boolean getExternalRules(Map<RuleType, RdrTreeSet> treeMap, Element e) {
        for (Element eChild : e.getChildren()) {                // 'case' or 'item'
            String childName = eChild.getName() ;

            if (childName.equalsIgnoreCase("case"))
                buildCaseLevelTree(treeMap, RuleType.CaseExternalTrigger, eChild);
            else if (childName.equalsIgnoreCase("item"))
                buildItemLevelTree(treeMap, RuleType.ItemExternalTrigger, eChild);
        }
        return true ;
    }


    /**
     * Construct a tree for each task specified in the rules file
     * @param e - the Element containing the rules of each task
     * @return the list of trees constructed
     */
    private void buildItemLevelTree(Map<RuleType, RdrTreeSet> treeMap,
                                    RuleType ruleType, Element e) {
        RdrTreeSet treeSet = new RdrTreeSet(ruleType);
        for (Element eChild : e.getChildren()) {
            RdrTree tree = buildTree(eChild);
            if (tree != null) treeSet.add(tree);
        }
        if (! treeSet.isEmpty()) {
            treeMap.put(ruleType, treeSet);
            Persister.insert(treeSet);
        }
    }


    private void buildCaseLevelTree(Map<RuleType, RdrTreeSet> treeMap,
                                         RuleType ruleType, Element e) {
        RdrTreeSet treeSet = new RdrTreeSet(ruleType);
        RdrTree rdrTree = buildTree(e);
        if (rdrTree != null) {
            treeSet.add(rdrTree);
            treeMap.put(ruleType, treeSet);
            Persister.insert(treeSet);
        }
    }


    /**
     * Constructs an RdrTree from the JDOM Element passed
     * @param task - the Element containing a representation of the tree
     * @return the list of trees constructed
     */
    private RdrTree buildTree(Element task) {
        String taskId = task.getAttributeValue("name");
        RdrTree rdrTree = new RdrTree(taskId);

        List<Element> nodeList = task.getChildren();    //the rdr nodes for this task

        //get the root node (always stored as node 0)
        Element rootNode = nodeList.get(0);
        RdrNode root = buildFromNode(rootNode, nodeList);  // build from root
        rdrTree.setRootNode(root);
        Persister.insert(rdrTree);
        return rdrTree;
    }


    /**
     *  recursively build a tree from the node and list passed
     *  @param xNode contains the xml elements for a single RDR node definition
     *  @param nodeList is the list of all xNodes for a single task
     *  @return the root node of the constructed tree
     */
    private RdrNode buildFromNode(Element xNode, List<Element> nodeList) {
        String childId;
        RdrNode rdrNode = new RdrNode();

        // populate the node
        rdrNode.setNodeId(xNode.getChildText("id")) ;
        rdrNode.setCondition(xNode.getChildText("condition"));
        rdrNode.setCornerStone(xNode.getChild("cornerstone"));

        RdrConclusion rdrConclusion = new RdrConclusion(xNode.getChild("conclusion"));
        Persister.insert(rdrConclusion);
        rdrNode.setConclusion(rdrConclusion);
        Persister.insert(rdrNode);

        // do true branch recursively
        childId = xNode.getChildText("trueChild") ;
        if (childId.compareTo("-1") != 0) {
            Element eTrueChild = getNodeWithId(childId, nodeList) ;
            rdrNode.setTrueChild(buildFromNode(eTrueChild, nodeList));
            rdrNode.getTrueChild().setParent(rdrNode) ;
        }

        // do false branch recursively
        childId = xNode.getChildText("falseChild") ;
        if (childId.compareTo("-1") != 0) {
            Element eFalseChild = getNodeWithId(childId, nodeList) ;
            rdrNode.setFalseChild(buildFromNode(eFalseChild, nodeList));
            rdrNode.getFalseChild().setParent(rdrNode) ;
        }
        Persister.update(rdrNode);
        return rdrNode;
    }

    /** find the node with the id passed in the List of xml nodes */
    private Element getNodeWithId(String id, List<Element> nodeList) {

        // find the node with this id
        for (Element eNode : nodeList) {
            if (id.equals(eNode.getChildText("id"))) return eNode;
        }
        return null ;
    }


    private File getFile(String name) {
        return new File(Library.wsRulesDir + name + ".xrs");
    }


    /********************************************************************************/

    private RdrSet loadSet(YSpecificationID specID) {
        String id = specID.getIdentifier();
        String whereClause = id != null ? "_specID.identifier='" + id + "'" :
                "_specID.uri='" + specID.getUri() +"'";                     // version 1
        return loadSetWhere(whereClause);
    }


    private RdrSet loadSet(String name) {
        return loadSetWhere("_processName='" + name + "'");
    }


    private RdrSet loadSetWhere(String whereClause) {
        List list = Persister.getInstance().getObjectsForClassWhere("RdrSet", whereClause);
        return ! (list == null || list.isEmpty()) ? (RdrSet) list.get(0) : null;
    }

}
