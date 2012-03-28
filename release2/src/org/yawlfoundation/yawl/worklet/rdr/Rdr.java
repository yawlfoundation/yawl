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

import org.jdom.Element;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

import java.util.Map;

/**
 * A top-level interface into the Rdr Classes
 *
 * @author Michael Adams
 * @date 28/02/12
 */
public class Rdr {

    protected RdrCache _rdrCache = new RdrCache();              // rule set cache

    public Rdr() {}

    public RdrConclusion evaluate(YSpecificationID specID, Element data, RuleType rType) {
        return getConclusion(specID, null, data, rType);
    }

    public RdrConclusion evaluate(String processName, Element data, RuleType rType) {
        return getConclusion(processName, null, data, rType);
    }

    public RdrConclusion evaluate(WorkItemRecord wir, Element data, RuleType rType) {
        return getConclusion(new YSpecificationID(wir), wir.getTaskID(), data, rType);
    }

    public RdrConclusion evaluate(YSpecificationID specID, String taskID, Element data,
                                  RuleType rType) {
        return getConclusion(specID, taskID, data, rType);
    }

    public RdrConclusion evaluate(String processName, String taskID, Element data, RuleType rType) {
        return getConclusion(processName, taskID, data, rType);
    }


    public RdrNode addNode(YSpecificationID specID, RuleType rType, RdrNode node) {
        return addNode(specID, null, rType, node);
    }

    public RdrNode addNode(String processName, RuleType rType, RdrNode node) {
        return addNode(processName, null, rType, node);
    }

    public RdrNode addNode(WorkItemRecord wir, RuleType rType, RdrNode node) {
        return addNode(new YSpecificationID(wir), wir.getTaskID(), rType, node);
    }

    public RdrNode addNode(YSpecificationID specID, String taskID, RuleType rType, RdrNode node) {
        return addNode(getRdrSet(specID), taskID, rType, node);
    }
    
    public RdrNode addNode(String processName, String taskID, RuleType rType, RdrNode node) {
        return addNode(getRdrSet(processName), taskID, rType, node);
    }


    public RdrNode getNode(YSpecificationID specID, RuleType rType, int nodeID) {
        return getNode(specID, null, rType, nodeID);
    }

    public RdrNode getNode(String processName, RuleType rType, int nodeID) {
        return getNode(processName, null, rType, nodeID);
    }

    public RdrNode getNode(WorkItemRecord wir, RuleType rType, int nodeID) {
        return getNode(new YSpecificationID(wir), wir.getTaskID(), rType, nodeID);
    }

    public RdrNode getNode(YSpecificationID specID, String taskID, RuleType rType, int nodeID) {
        return getNode(getRdrSet(specID), taskID, rType, nodeID);
    }

    public RdrNode getNode(String processName, String taskID, RuleType rType, int nodeID) {
        return getNode(getRdrSet(processName), taskID, rType, nodeID);
    }



    public RdrSet getRdrSet(YSpecificationID specID) {
        return _rdrCache.get(specID);
    }

    public RdrSet getRdrSet(String processName) {
        return _rdrCache.get(processName);
    }


    public RdrSet refreshRdrSet(YSpecificationID specID) {
        return _rdrCache.refresh(specID);
    }

    public RdrSet refreshRdrSet(String processName) {
        return _rdrCache.refresh(processName);
    }

    public boolean containsRdrSet(YSpecificationID specID) {
        return _rdrCache.contains(specID);
    }

    public boolean containsRdrSet(String processName) {
        return _rdrCache.contains(processName);
    }
    
    public Map<String, RdrSet> getAllCachedRdrSets() {
        return _rdrCache.getAll();
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

    
    private RdrNode addNode(RdrSet set, String taskID, RuleType rType, RdrNode node) {
        RdrNode addedNode = null;
        RdrTree tree = getTree(set, taskID, rType);
        if (tree != null) {
            RdrConclusion conc = getConclusion(tree, node.getCornerStone());
            if (conc != null) {
                node.setParent(conc.getLastPair()[0]);
                addedNode = tree.addNode(node, conc.getLastPair()[0], conc.isLastPairEqual());
            }
        }
        else {
            tree = new RdrTree(taskID);
            RdrNode root = tree.createRootNode();
            set.addTree(tree, rType);
            addedNode = tree.addNode(node, root, true);
        }
        set.save();
        return addedNode;
    }
    
    
    private RdrNode getNode(RdrSet set, String taskID, RuleType rType, int nodeID) {
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
    private RdrConclusion getConclusion(YSpecificationID specID, String taskID,
                                        Element data, RuleType rType) {
        if (specID != null) {
            return getConclusion(getTree(specID, taskID, rType), data);
        }
        return null ;
    }


    private RdrConclusion getConclusion(String processName, String taskID,
                                        Element data, RuleType rType) {
        if (processName != null) {
            return getConclusion(getTree(processName, taskID, rType), data);
        }
        return null ;
    }


    private RdrConclusion getConclusion(RdrTree tree, Element data) {
        if (tree != null) {
            RdrConclusion conc = new RdrConclusion(tree.search(data));
            conc.setLastPair(tree.getLastPair());
            return conc ;
        }
        return null ;
    }

    private RdrTree getTree(YSpecificationID specID, String taskID, RuleType rType) {
        return getTree(getRdrSet(specID), taskID, rType);     // load rules for spec
    }
    
    
    private RdrTree getTree(String processName, String taskID, RuleType rType) {
        return getTree(getRdrSet(processName), taskID, rType);
    }


    private RdrTree getTree(RdrSet ruleSet, String taskID, RuleType rType) {
        if (! ruleSet.hasRules()) return null;
        RdrTree tree = rType.isCaseLevelType() ? ruleSet.getTree(rType) :
               ruleSet.getTree(rType, taskID) ;
        if (tree != null) tree.setAttributes(ruleSet.getName(), rType);
        return tree;
    }

}
