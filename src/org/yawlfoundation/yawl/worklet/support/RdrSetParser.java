package org.yawlfoundation.yawl.worklet.support;

import org.apache.logging.log4j.LogManager;
import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.worklet.rdr.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses a rule set expressed as an XML doc
 *
 * @author Michael Adams
 * @date 22/01/2016
 */
public class RdrSetParser {

    public Map<RuleType, RdrTreeSet> parse(String xml, boolean persist) {
        return parse(JDOMUtil.stringToDocument(xml), persist);
    }


    // todo: validation
    public Map<RuleType, RdrTreeSet> parse(Document doc, boolean persist) {
        if (doc == null) return Collections.emptyMap();  // no such file or unsuccessful load
        Map<RuleType, RdrTreeSet> treeMap = new HashMap<RuleType, RdrTreeSet>();
        try {
            Element root = doc.getRootElement();      // spec

            // extract the rule nodes for each exception type
            for (Element e : root.getChildren()) {       // these are exception type tags
                String exName = e.getName();
                if (exName.equalsIgnoreCase("selection")) {
                    buildItemLevelTree(treeMap, RuleType.ItemSelection, e, persist);
                }
                else if (exName.equalsIgnoreCase("abort")) {
                    buildItemLevelTree(treeMap, RuleType.ItemAbort, e, persist);
                }
                else if (exName.equalsIgnoreCase("timeout")) {
                    buildItemLevelTree(treeMap, RuleType.ItemTimeout, e, persist);
                }
                else if (exName.equalsIgnoreCase("resourceUnavailable")) {
                    buildItemLevelTree(treeMap, RuleType.ItemResourceUnavailable, e, persist);
                }
                else if (exName.equalsIgnoreCase("violation")) {
                    buildItemLevelTree(treeMap, RuleType.ItemConstraintViolation, e, persist);
                }
                else if (exName.equalsIgnoreCase("external")) {
                    getExternalRules(treeMap, e, persist) ;
                }
                else if (exName.equalsIgnoreCase("constraints")) {
                    getConstraintRules(treeMap, e, persist) ;
                }

                // if 'task' is a child of 'root', this is a version one rules file
                // so treat it as though it contains selection rules only
                else if (exName.equalsIgnoreCase("task")) {
                    buildItemLevelTree(treeMap, RuleType.ItemSelection, root, persist);
                }
            }
            return treeMap;
        }
        catch (Exception ex) {
            LogManager.getLogger(RdrSetLoader.class).error(
                    "Exception retrieving rule nodes from rules file", ex);
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
    private boolean getConstraintRules(Map<RuleType, RdrTreeSet> treeMap,
                                       Element e, boolean persist) {
        for (Element eCon : e.getChildren()) {
            String conName = eCon.getName();
            Element ePre = eCon.getChild("pre");
            Element ePost = eCon.getChild("post");

            if (conName.equalsIgnoreCase("case")) {
                if (ePre != null) buildCaseLevelTree(treeMap,
                        RuleType.CasePreconstraint, ePre, persist) ;
                if (ePost != null) buildCaseLevelTree(treeMap,
                        RuleType.CasePostconstraint, ePost, persist) ;
            }
            else if (conName.equalsIgnoreCase("item")) {
                if (ePre != null) buildItemLevelTree(treeMap,
                        RuleType.ItemPreconstraint, ePre, persist) ;
                if (ePost != null) buildItemLevelTree(treeMap,
                        RuleType.ItemPostconstraint, ePost, persist) ;
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
    private boolean getExternalRules(Map<RuleType, RdrTreeSet> treeMap,
                                     Element e, boolean persist) {
        for (Element eChild : e.getChildren()) {                // 'case' or 'item'
            String childName = eChild.getName() ;

            if (childName.equalsIgnoreCase("case"))
                buildCaseLevelTree(treeMap, RuleType.CaseExternalTrigger, eChild, persist);
            else if (childName.equalsIgnoreCase("item"))
                buildItemLevelTree(treeMap, RuleType.ItemExternalTrigger, eChild, persist);
        }
        return true ;
    }


    /**
     * Construct a tree for each task specified in the rules file
     * @param e - the Element containing the rules of each task
     */
    private void buildItemLevelTree(Map<RuleType, RdrTreeSet> treeMap,
                                    RuleType ruleType, Element e, boolean persist) {
        RdrTreeSet treeSet = new RdrTreeSet(ruleType);
        for (Element eChild : e.getChildren()) {
            RdrTree tree = buildTree(eChild, persist);
            if (tree != null) treeSet.add(tree);
        }
        if (! treeSet.isEmpty()) {
            treeMap.put(ruleType, treeSet);
            if (persist) Persister.insert(treeSet);
        }
    }


    private void buildCaseLevelTree(Map<RuleType, RdrTreeSet> treeMap,
                                         RuleType ruleType, Element e, boolean persist) {
        RdrTreeSet treeSet = new RdrTreeSet(ruleType);
        Element taskElem = e.getChild("task");
        if (taskElem == null) {
            taskElem = convertCaseLevelTree(e);
        }
        RdrTree rdrTree = buildTree(taskElem, persist); // task = "_case_level_"
        if (rdrTree != null) {
            treeSet.add(rdrTree);
            treeMap.put(ruleType, treeSet);
            if (persist) Persister.insert(treeSet);
        }
    }


    /**
     * Constructs an RdrTree from the JDOM Element passed
     * @param task - the Element containing a representation of the tree
     * @return the list of trees constructed
     */
    private RdrTree buildTree(Element task, boolean persist) {
        String taskId = task.getAttributeValue("name");
        RdrTree rdrTree = new RdrTree(taskId);

        List<Element> nodeList = task.getChildren();    //the rdr nodes for this task

        //get the root node (always stored as node 0)
        Element rootNode = nodeList.get(0);
        RdrNode root = buildFromNode(rootNode, nodeList, persist);  // build from root
        rdrTree.setRootNode(root);
        if (persist) Persister.insert(rdrTree);
        return rdrTree;
    }


    /**
     *  recursively build a tree from the node and list passed
     *  @param xNode contains the xml elements for a single RDR node definition
     *  @param nodeList is the list of all xNodes for a single task
     *  @return the root node of the constructed tree
     */
    private RdrNode buildFromNode(Element xNode, List<Element> nodeList, boolean persist) {
        String childId;
        RdrNode rdrNode = new RdrNode();

        // populate the node
        rdrNode.setNodeId(xNode.getChildText("id")) ;
        rdrNode.setCondition(xNode.getChildText("condition"));
        rdrNode.setCornerStone(xNode.getChild("cornerstone"));

        RdrConclusion rdrConclusion = new RdrConclusion(xNode.getChild("conclusion"));
        if (persist) Persister.insert(rdrConclusion);
        rdrNode.setConclusion(rdrConclusion);
        if (persist) Persister.insert(rdrNode);

        // do true branch recursively
        childId = xNode.getChildText("trueChild") ;
        if (childId.compareTo("-1") != 0) {
            Element eTrueChild = getNodeWithId(childId, nodeList) ;
            rdrNode.setTrueChild(buildFromNode(eTrueChild, nodeList, persist));
            rdrNode.getTrueChild().setParent(rdrNode) ;
        }

        // do false branch recursively
        childId = xNode.getChildText("falseChild") ;
        if (childId.compareTo("-1") != 0) {
            Element eFalseChild = getNodeWithId(childId, nodeList) ;
            rdrNode.setFalseChild(buildFromNode(eFalseChild, nodeList, persist));
            rdrNode.getFalseChild().setParent(rdrNode) ;
        }
        if (persist) Persister.update(rdrNode);
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


    private Element convertCaseLevelTree(Element caseElem) {
        Element taskElem = new Element(RdrSet.CASE_LEVEL_TREE_FLAG);
        for (Element child : caseElem.getChildren()) {
            taskElem.addContent(child.clone());
        }
        return taskElem;
    }

}
