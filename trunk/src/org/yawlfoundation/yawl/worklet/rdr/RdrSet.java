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

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.worklet.support.Persister;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    private long id;                       // for Hibernate

    private YSpecificationID _specID ;
    private String _processName;
    private Map<RuleType, RdrTreeSet> _treeMap;

    protected static final String CASE_LEVEL_TREE_FLAG = "__case_level_tree__";


    /**
     * Default constructor
     * @param specID the specification this rule set belongs to
     */
    public RdrSet(YSpecificationID specID) {
        _specID = specID ;
    }


    public RdrSet(String processName) {
        _processName = processName ;
    }


    private RdrSet() { }                // for hibernate


  //===========================================================================//

    /** clears and rebuilds the RdrSet */
    public void refresh() {
        RdrSetLoader loader = new RdrSetLoader();
        RdrSet reloaded = _specID != null ? loader.load(_specID) : loader.load(_processName);
        if (reloaded != null) {
            setTreeMap(new HashMap<RuleType, RdrTreeSet>(reloaded._treeMap));
        }
    }


    /** @return true if this spec's ruleset is not empty */
    public boolean hasRules() {
        return ! (_treeMap == null || _treeMap.isEmpty());
    }


    public void addTree(RdrTree tree, RuleType treeType) {
        RdrTreeSet treeSet = _treeMap.get(treeType);
        if (treeSet == null) {
            treeSet = new RdrTreeSet(treeType);
            _treeMap.put(treeType, treeSet);
        }
        treeSet.add(tree);
        Persister.update(this);
    }


    /**
     *  Retrieves a specified RdrTree for the specified task
     *
     *  @param treeType - the tree exception type
     *  @param taskId - the task the tree represents
     *  @return the RDRTree for the specified spec and task
     */
    public RdrTree getTree(RuleType treeType, String taskId) {
        if (treeType.isCaseLevelType()) taskId = CASE_LEVEL_TREE_FLAG;
        RdrTreeSet treeSet = _treeMap.get(treeType);
        return treeSet != null ? treeSet.get(taskId) : null;
    }

    public RdrTree getTree(RuleType treeType) { return getTree(treeType, null); }


    public void setTreeMap(Map<RuleType, RdrTreeSet> map) { _treeMap = map; }


    public boolean save() {
        Persister.update(this);
        return true;
    }
    
    
    public String toXML() {
         return toXNode().toPrettyString();
    }
    
    
    private XNode toXNode() {
        XNode ruleSet = createRootXNode();
        treeToXNode(getTree(RuleType.CasePreconstraint), ruleSet, "constraints",
                "case", "pre");
        treeToXNode(getTree(RuleType.CasePostconstraint, null), ruleSet, "constraints",
                "case", "post");
        treeToXNode(getTree(RuleType.CaseExternalTrigger), ruleSet,  "external", "case");

        treeListToXNode(_treeMap.get(RuleType.ItemSelection), ruleSet, "selection");
        treeListToXNode(_treeMap.get(RuleType.ItemPreconstraint), ruleSet,
                "constraints", "item", "pre");
        treeListToXNode(_treeMap.get(RuleType.ItemPostconstraint), ruleSet,
                "constraints", "item", "post");
        treeListToXNode(_treeMap.get(RuleType.ItemAbort), ruleSet, "abort");
        treeListToXNode(_treeMap.get(RuleType.ItemTimeout), ruleSet, "timeout");
        treeListToXNode(_treeMap.get(RuleType.ItemResourceUnavailable), ruleSet,
                "resourceUnavailable");
        treeListToXNode(_treeMap.get(RuleType.ItemConstraintViolation), ruleSet, "violation");
        treeListToXNode(_treeMap.get(RuleType.ItemExternalTrigger), ruleSet, "external", "item");
        return ruleSet;
    }
    
    private XNode createRootXNode() {
        String rootTag = _specID != null ? "spec" :
                _processName != null ? "process" : "root";
        XNode xRoot = new XNode(rootTag);
        String name = getName();
        if (name != null) xRoot.addAttribute("name", name);
        return xRoot;
    }
    
    
    protected String getName() {
        return _specID != null ? _specID.getUri() : _processName;
    }


    protected String getProcessName() { return _processName; }

    protected YSpecificationID getSpecificationID() { return _specID; }


    private void treeListToXNode(RdrTreeSet treeSet, XNode parent, String... names) {
        if (treeSet != null) {
            for (RdrTree tree : treeSet.getAll()) {
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


    public String toString() {
        String id = _specID != null ? _specID.toString() : _processName;
        return "RuleSet for: " + id;
    }


    public void fromXML(String xml) {
        _treeMap = new RdrSetLoader().load(JDOMUtil.stringToDocument(xml));
    }

//===========================================================================//


    private void setTreeSet(Set<RdrTreeSet> treeSet) {
        if (_treeMap == null) _treeMap = new HashMap<RuleType, RdrTreeSet>();
        for (RdrTreeSet tree : treeSet) {
            _treeMap.put(tree.getRuleType(), tree);
        }
    }

    private Set<RdrTreeSet> getTreeSet() {
        return _treeMap != null ? new HashSet<RdrTreeSet>(_treeMap.values()) : null;
    }

//===========================================================================//

}
