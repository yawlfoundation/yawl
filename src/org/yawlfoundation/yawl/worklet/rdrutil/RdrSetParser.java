package org.yawlfoundation.yawl.worklet.rdrutil;

import org.apache.logging.log4j.LogManager;
import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.worklet.rdr.*;

import java.util.*;

/**
 * Parses a rule set expressed as an XML doc
 *
 * @author Michael Adams
 * @date 22/01/2016
 */
public class RdrSetParser {

    public Map<RuleType, RdrTreeSet> parse(String xml, boolean newSet) {
        return parse(JDOMUtil.stringToDocument(xml), newSet);
    }

    public Set<RdrTreeSet> parseSet(String xml, boolean newSet) {
        return new HashSet<RdrTreeSet>(parse(xml, newSet).values());
    }


    // todo: validation
    public Map<RuleType, RdrTreeSet> parse(Document doc, boolean newSet) {
        if (doc == null) return Collections.emptyMap();  // no such file or unsuccessful load
        Map<RuleType, RdrTreeSet> treeMap = new HashMap<RuleType, RdrTreeSet>();
        try {
            Element root = doc.getRootElement();      // spec

            // extract the rule nodes for each exception type
            for (Element e : root.getChildren()) {       // these are exception type tags
                String exName = e.getName();
                if (exName.equalsIgnoreCase("selection")) {
                    buildItemLevelTree(treeMap, RuleType.ItemSelection, e, newSet);
                }
                else if (exName.equalsIgnoreCase("abort")) {
                    buildItemLevelTree(treeMap, RuleType.ItemAbort, e, newSet);
                }
                else if (exName.equalsIgnoreCase("timeout")) {
                    buildItemLevelTree(treeMap, RuleType.ItemTimeout, e, newSet);
                }
                else if (exName.equalsIgnoreCase("resourceUnavailable")) {
                    buildItemLevelTree(treeMap, RuleType.ItemResourceUnavailable, e, newSet);
                }
                else if (exName.equalsIgnoreCase("violation")) {
                    buildItemLevelTree(treeMap, RuleType.ItemConstraintViolation, e, newSet);
                }
                else if (exName.equalsIgnoreCase("external")) {
                    getExternalRules(treeMap, e, newSet) ;
                }
                else if (exName.equalsIgnoreCase("constraints")) {
                    getConstraintRules(treeMap, e, newSet) ;
                }

                // if 'task' is a child of 'root', this is a version one rules file
                // so treat it as though it contains selection rules only
                else if (exName.equalsIgnoreCase("task")) {
                    buildItemLevelTree(treeMap, RuleType.ItemSelection, root, newSet);
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
                                       Element e, boolean newSet) {
        for (Element eCon : e.getChildren()) {
            String conName = eCon.getName();
            Element ePre = eCon.getChild("pre");
            Element ePost = eCon.getChild("post");

            if (conName.equalsIgnoreCase("case")) {
                if (ePre != null) buildCaseLevelTree(treeMap,
                        RuleType.CasePreconstraint, ePre, newSet);
                if (ePost != null) buildCaseLevelTree(treeMap,
                        RuleType.CasePostconstraint, ePost, newSet);
            }
            else if (conName.equalsIgnoreCase("item")) {
                if (ePre != null) buildItemLevelTree(treeMap,
                        RuleType.ItemPreconstraint, ePre, newSet);
                if (ePost != null) buildItemLevelTree(treeMap,
                        RuleType.ItemPostconstraint, ePost, newSet);
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
                                     Element e, boolean newSet) {
        for (Element eChild : e.getChildren()) {                // 'case' or 'item'
            String childName = eChild.getName() ;

            if (childName.equalsIgnoreCase("case"))
                buildCaseLevelTree(treeMap, RuleType.CaseExternalTrigger, eChild, newSet);
            else if (childName.equalsIgnoreCase("item"))
                buildItemLevelTree(treeMap, RuleType.ItemExternalTrigger, eChild, newSet);
        }
        return true ;
    }


    /**
     * Construct a tree for each task specified in the rules file
     * @param e - the Element containing the rules of each task
     */
    private void buildItemLevelTree(Map<RuleType, RdrTreeSet> treeMap,
                                    RuleType ruleType, Element e, boolean newSet) {
        RdrTreeSet treeSet = new RdrTreeSet(ruleType);
        for (Element eChild : e.getChildren()) {
            RdrTree tree = buildTree(eChild, newSet);
            if (tree != null) treeSet.add(tree);
        }
        if (! treeSet.isEmpty()) {
            treeMap.put(ruleType, treeSet);
        }
    }


    private void buildCaseLevelTree(Map<RuleType, RdrTreeSet> treeMap, RuleType ruleType,
                                    Element e, boolean newSet) {
        RdrTreeSet treeSet = new RdrTreeSet(ruleType);
        Element taskElem = e.getChild("task");
        if (taskElem == null) {
            taskElem = convertCaseLevelTree(e);
        }
        RdrTree rdrTree = buildTree(taskElem, newSet); // task = "_case_level_"
        if (rdrTree != null) {
            treeSet.add(rdrTree);
            treeMap.put(ruleType, treeSet);
        }
    }


    /**
     * Constructs an RdrTree from the JDOM Element passed
     * @param task - the Element containing a representation of the tree
     * @return the list of trees constructed
     */
    private RdrTree buildTree(Element task, boolean newSet) {
        String taskId = task.getAttributeValue("name");
        RdrTree rdrTree = new RdrTree(taskId);

        List<Element> nodeList = task.getChildren();    //the rdr nodes for this task

        //get the root node (always stored as node 0)
        Element rootNode = nodeList.get(0);
        RdrNode root = buildFromNode(rootNode, nodeList, newSet);  // build from root
        rdrTree.setRootNode(root);
        return rdrTree;
    }


    /**
     *  recursively build a tree from the node and list passed
     *  @param xNode contains the xml elements for a single RDR node definition
     *  @param nodeList is the list of all xNodes for a single task
     *  @return the root node of the constructed tree
     */
    private RdrNode buildFromNode(Element xNode, List<Element> nodeList,
                                  boolean newSet) {
        RdrNode rdrNode = new RdrNode();

        // populate the node
        if (! newSet) rdrNode.setNodeID(StringUtil.strToLong(xNode.getChildText("id"), 0));
        rdrNode.setCondition(xNode.getChildText("condition"));
        rdrNode.setCornerStone(xNode.getChild("cornerstone"));

        RdrConclusion rdrConclusion = new RdrConclusion(xNode.getChild("conclusion"));
        rdrNode.setConclusion(rdrConclusion);

        // do true branch recursively
        String childId;
        childId = xNode.getChildText("trueChild") ;
        if (childId.compareTo("-1") != 0) {
            Element eTrueChild = getNodeWithId(childId, nodeList) ;
            rdrNode.setTrueChild(buildFromNode(eTrueChild, nodeList, newSet));
            rdrNode.getTrueChild().setParent(rdrNode) ;
        }

        // do false branch recursively
        childId = xNode.getChildText("falseChild") ;
        if (childId.compareTo("-1") != 0) {
            Element eFalseChild = getNodeWithId(childId, nodeList) ;
            rdrNode.setFalseChild(buildFromNode(eFalseChild, nodeList, newSet));
            rdrNode.getFalseChild().setParent(rdrNode) ;
        }
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
