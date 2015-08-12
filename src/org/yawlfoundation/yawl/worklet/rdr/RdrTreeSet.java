package org.yawlfoundation.yawl.worklet.rdr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 23/09/2014
 */
public class RdrTreeSet {

    private long id;

    private RuleType _ruleType;
    private Map<String, RdrTree> _treeMap;

    private RdrTreeSet() { }

    protected RdrTreeSet(RuleType ruleType) {
        this(ruleType, new HashMap<String, RdrTree>());
    }

    protected RdrTreeSet(RuleType ruleType, Map<String, RdrTree> treeMap) {
        _ruleType = ruleType;
        _treeMap = treeMap;
    }


    public RuleType getRuleType() { return _ruleType; }


    public RdrTree get() { return get(RdrSet.CASE_LEVEL_TREE_FLAG); }

    public RdrTree get(String taskId) { return _treeMap.get(taskId); }

    public Set<RdrTree> getAll() { return new HashSet<RdrTree>(_treeMap.values()); }


    public void add(RdrTree tree) { _treeMap.put(tree.getTaskId(), tree); }


    public boolean isEmpty() { return _treeMap.isEmpty(); }


    // for hibernate

    private Set<RdrTree> getTreeSet() {
        return new HashSet<RdrTree>(_treeMap.values());
    }

    private void setTreeSet(Set<RdrTree> treeSet) {
        _treeMap = new HashMap<String, RdrTree>();
        if (treeSet != null) {
            for (RdrTree tree : treeSet) {
                _treeMap.put(tree.getTaskId(), tree);
            }
        }
    }
}
