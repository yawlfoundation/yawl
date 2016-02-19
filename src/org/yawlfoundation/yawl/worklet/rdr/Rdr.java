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
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.worklet.rdrutil.RdrException;
import org.yawlfoundation.yawl.worklet.support.Persister;

import java.util.Map;
import java.util.Set;

/**
 * A top-level interface into the Rdr Classes
 *
 * @author Michael Adams
 * @date 28/02/12
 */
public class Rdr {

    protected RdrSetLoader _loader = new RdrSetLoader();

    public Rdr() {}

    public RdrPair evaluate(YSpecificationID specID, Element data, RuleType rType) {
        return getConclusion(specID, null, data, rType);
    }

    public RdrPair evaluate(String processName, Element data, RuleType rType) {
        return getConclusion(processName, null, data, rType);
    }

    public RdrPair evaluate(WorkItemRecord wir, Element data, RuleType rType) {
        return getConclusion(new YSpecificationID(wir), wir.getTaskID(), data, rType);
    }

    public RdrPair evaluate(YSpecificationID specID, String taskID, Element data,
                                  RuleType rType) {
        return getConclusion(specID, taskID, data, rType);
    }

    public RdrPair evaluate(String processName, String taskID, Element data, RuleType rType) {
        return getConclusion(processName, taskID, data, rType);
    }


    public RdrNode addNode(YSpecificationID specID, RuleType rType, RdrNode node)
            throws RdrException {
        return addNode(specID, null, rType, node);
    }

    public RdrNode addNode(String processName, RuleType rType, RdrNode node)
            throws RdrException {
        return addNode(processName, null, rType, node);
    }

    public RdrNode addNode(WorkItemRecord wir, RuleType rType, RdrNode node)
            throws RdrException{
        return addNode(new YSpecificationID(wir), wir.getTaskID(), rType, node);
    }

    public RdrNode addNode(YSpecificationID specID, String taskID, RuleType rType,
                           RdrNode node) throws RdrException {
        RdrSet ruleSet = getRdrSet(specID);
        if (ruleSet == null) {
            ruleSet = new RdrSet(specID);
            Persister.insert(ruleSet);
        }
        return addNode(ruleSet, taskID, rType, node);
    }
    
    public RdrNode addNode(String processName, String taskID, RuleType rType,
                           RdrNode node)  throws RdrException {
        RdrSet ruleSet = getRdrSet(processName);
        if (ruleSet == null) {
            ruleSet = new RdrSet(processName);
            Persister.insert(ruleSet);
        }
        return addNode(ruleSet, taskID, rType, node);
    }

    public RdrNode getNode(long nodeID) {
        return nodeID > 0 ? _loader.loadNode(nodeID) : null;
    }

    public RdrNode getNode(YSpecificationID specID, RuleType rType, long nodeID) {
        return getNode(specID, null, rType, nodeID);
    }

    public RdrNode getNode(String processName, RuleType rType, long nodeID) {
        return getNode(processName, null, rType, nodeID);
    }

    public RdrNode getNode(WorkItemRecord wir, RuleType rType, long nodeID) {
        return getNode(new YSpecificationID(wir), wir.getTaskID(), rType, nodeID);
    }

    public RdrNode getNode(YSpecificationID specID, String taskID, RuleType rType, long nodeID) {
        return getNode(getRdrSet(specID), taskID, rType, nodeID);
    }

    public RdrNode getNode(String processName, String taskID, RuleType rType, long nodeID) {
        return getNode(getRdrSet(processName), taskID, rType, nodeID);
    }



    public RdrSet getRdrSet(YSpecificationID specID) {
        return _loader.load(specID);
    }

    public RdrSet getRdrSet(String processName) {
        return _loader.load(processName);
    }


    public Set<String> getRdrSetIDs() { return _loader.getSetIDs(); }


    public RdrSet removeRdrSet(YSpecificationID specID) {
        return _loader.removeSet(specID);
    }

    public RdrSet removeRdrSet(String processName) {
        return _loader.removeSet(processName);
    }


    public void updateTaskIDs(YSpecificationID specID, Map<String, String> updates) {
        updateTaskIDs(getRdrSet(specID), updates);
    }


    public RdrTree getRdrTree(YSpecificationID specID, RuleType rType) {
        return getTree(specID, null, rType);
    }

    public RdrTree getRdrTree(String processName, RuleType rType) {
        return getTree(processName, null, rType);
    }

    public RdrTree getRdrTree(WorkItemRecord wir, RuleType rType) {
        return getTree(new YSpecificationID(wir), wir.getTaskID(), rType);
    }

    public RdrTree getRdrTree(YSpecificationID specID, String taskID, RuleType rType) {
        return getTree(specID, taskID, rType);
    }

    public RdrTree getRdrTree(String processName, String taskID, RuleType rType) {
        return getTree(processName, taskID, rType);
    }


    /*****************************************************************************/

    
    private RdrNode addNode(RdrSet set, String taskID, RuleType rType, RdrNode node)
            throws RdrException {
        RdrNode addedNode = null;
        RdrTree tree = getTree(set, taskID, rType);
        if (tree != null) {
            addedNode = addNode(tree, node);
        }
        else {
            tree = new RdrTree(taskID);
            RdrNode root = tree.createRootNode();
            set.addTree(tree, rType);
            addedNode = tree.addNode(node, root, true);
        }
        if (addedNode != null) {
            set.save();
        }
        return addedNode;
    }


    private RdrNode addNode(RdrTree tree, RdrNode node) throws RdrException {
        RdrPair pair = tree.search(node.getCornerStone());
        if (pair != null) {
            RdrNode parent = pair.getParentForNewNode();
            if (parent != null) {

                // don't add a duplicate node as a new child
                if (parent.hasIdenticalContent(node)) {
                    throw new RdrException("Failed to add node: Cannot add a node " +
                            "identical to its parent.");
                }
                node.setParent(parent);
                return tree.addNode(node, parent, pair.isPairEqual());
            }
            throw new RdrException("Failed to add node: Could not locate parent node.");
        }
        return null;
    }
    
    
    private RdrNode getNode(RdrSet set, String taskID, RuleType rType, long nodeID) {
        RdrTree tree = getTree(set, taskID, rType);
        if (tree != null) {
            RdrNode node = tree.getNode(nodeID);
            if (node != null) node.setAttributes(tree.getAttributes(), taskID);
            return node;
        }
        return null;
    }
    
    /**
     * Discovers whether this case or item has rules for this exception type, and if so,
     * returns the result of the rule evaluation. Note that if the conclusion
     * returned from the search is empty, no exception has occurred (no rule has been
     * satisfied).
     * @param specID the specification id of the rule set
     * @param taskID item's task id, or null for case-level exception
     * @param data the case data
     * @param rType the type of exception triggered
     * @return an RdrConclusion representing an exception handling process,
     *         or null if no rules are defined for these criteria
     */
    private RdrPair getConclusion(YSpecificationID specID, String taskID,
                                        Element data, RuleType rType) {
        return specID != null ? evaluate(getTree(specID, taskID, rType), data) : null;
    }


    private RdrPair getConclusion(String processName, String taskID,
                                        Element data, RuleType rType) {
        return processName != null ? evaluate(getTree(processName, taskID, rType), data)
                : null;
    }


    private RdrPair evaluate(RdrTree tree, Element data) {
        return tree != null ? tree.search(data) : null;
    }


    private RdrTree getTree(YSpecificationID specID, String taskID, RuleType rType) {
        return getTree(getRdrSet(specID), taskID, rType);     // load rules for spec
    }
    
    
    private RdrTree getTree(String processName, String taskID, RuleType rType) {
        return getTree(getRdrSet(processName), taskID, rType);
    }


    private RdrTree getTree(RdrSet ruleSet, String taskID, RuleType rType) {
        if (ruleSet == null || ! ruleSet.hasRules()) return null;
        RdrTree tree = ruleSet.getTree(rType, taskID) ;
        if (tree != null) tree.setAttributes(ruleSet.getName(), rType);
        return tree;
    }


    public void updateTaskIDs(RdrSet rdrSet, Map<String, String> updates) {
        if (rdrSet == null) return;
        for (RdrTreeSet treeSet : rdrSet.getTreeSet()) {
            if (treeSet.getRuleType().isCaseLevelType()) continue;  // ignore case trees
            for (RdrTree tree : treeSet.getAll()) {
                String taskID = tree.getTaskId();
                if (taskID != null && updates.containsKey(taskID)) {
                    tree.setTaskId(updates.get(taskID));
                    Persister.update(tree);
                }
            }
        }
    }

}
