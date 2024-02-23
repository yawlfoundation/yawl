/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A set of rule trees (one per task) for a particular rule type
 *
 * @author Michael Adams
 * @date 23/09/2014
 */
public class RdrTreeSet {

    private long id;

    private RuleType _ruleType;
    private Set<RdrTree> _treeSet;

    private RdrTreeSet() { }


    public RdrTreeSet(RuleType ruleType) {
        this();
        _ruleType = ruleType;
    }


    public RuleType getRuleType() { return _ruleType; }


    public RdrTree get() { return get(RdrSet.CASE_LEVEL_TREE_FLAG); }


    public RdrTree get(String taskId) {
        if (! (_treeSet == null || taskId == null)) {
            for (RdrTree tree : _treeSet) {
                if (tree.getTaskId().equals(taskId)) {
                    return tree;
                }
            }
        }
        return null;
    }


    public Set<RdrTree> getAll() {
        return _treeSet != null ? _treeSet : Collections.<RdrTree>emptySet();
    }

    // called at design time
    public Set<String> getAllTasks() {
        Set<String> taskIdSet = new HashSet<String>();
        if (_treeSet != null) {
            for (RdrTree tree : _treeSet) {
                taskIdSet.add(tree.getTaskId());
            }
        }
        return taskIdSet;
    }


    public void add(RdrTree tree) {
        if (tree != null) {
            if (_treeSet == null) {
                _treeSet = new HashSet<RdrTree>();
            }
            _treeSet.add(tree);
        }
    }


    public RdrTree remove(String taskID) {
        if (_ruleType.isCaseLevelType()) {
            taskID = RdrSet.CASE_LEVEL_TREE_FLAG;
        }
        RdrTree tree = get(taskID);
        if (! (_treeSet == null || tree == null)) {
            _treeSet.remove(tree);
        }
        return tree;
    }


    public boolean isEmpty() { return _treeSet == null || _treeSet.isEmpty(); }


    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) &&
                id == ((RdrTreeSet) o).id;

    }


    public int hashCode() {
        long seed = (id != 0) ? id : _ruleType.hashCode();
        return (int) (seed ^ (seed >>> 32));
    }


    // for hibernate

    private Set<RdrTree> getTreeSet() {
        return _treeSet;
    }


    private void setTreeSet(Set<RdrTree> treeSet) { _treeSet = treeSet; }

}
