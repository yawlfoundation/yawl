package org.yawlfoundation.yawl.editor.core.resourcing;

import org.yawlfoundation.yawl.elements.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Collects all tasks that precede a given task in a specification, including those
 * in the sub-nets of composite tasks
 *
 * @author Michael Adams
 * @date 17/07/13
 */
public class PresetWalker {

    /**
     * Gets the set of all atomic tasks preceding the specified task in the containing
     * specification that are required to be resourced.
     * @param task the task to get all the preceding tasks of
     * @return the Set of preceding atomic tasks
     */
    public Set<YAtomicTask> getAtomicTasks(YAtomicTask task) {
        Set<YAtomicTask> precedingTasks = new HashSet<YAtomicTask>();
        for (YAtomicTask priorTask : getAllPrecedingAtomicTasks(task)) {
            YDecomposition decomposition = task.getDecompositionPrototype();
            if (isResourcedDecomposition(decomposition)) {
                precedingTasks.add(priorTask);
            }
        }
        return precedingTasks;
    }


    /**
     * Gets the set of all atomic tasks preceding the specified task in the containing
     * specification.
     * @param task the task to get all the preceding tasks of
     * @return the Set of preceding atomic tasks
     */
    private Set<YAtomicTask> getAllPrecedingAtomicTasks(YTask task) {
        Set<YAtomicTask> precedingTasks = new HashSet<YAtomicTask>();
        boolean isRootNet = isMemberOfRootNet(task);

        for (YExternalNetElement element : getAllPrecedingElements(task)) {
            if (element instanceof YAtomicTask) {
                precedingTasks.add((YAtomicTask) element);
            }

            // for composites, get all their contained atomic tasks
            else if (element instanceof YCompositeTask) {
                precedingTasks.addAll(getAllContainedAtomicTasks((YCompositeTask) element));
            }

            // if we've arrived at the start of a sub-net, recurse using the composite
            // task that houses the sub-net
            else if ((element instanceof YInputCondition) && ! isRootNet) {
                YTask containingTask = getContainingTask(task);
                if (containingTask != null) {
                    precedingTasks.addAll(getAllPrecedingAtomicTasks(containingTask));
                }
            }
        }
        return precedingTasks;
    }


    /**
     * Gets all of the atomic tasks that are members of the sub-net housed by the
     * specified composite task
     * @param compositeTask the task to search the sub-net of
     * @return the Set of atomic tasks
     */
    private Set<YAtomicTask> getAllContainedAtomicTasks(YCompositeTask compositeTask) {
        Set<YAtomicTask> contained = new HashSet<YAtomicTask>();
        YNet net = (YNet) compositeTask.getDecompositionPrototype();
        for (YTask task : net.getNetTasks()) {
            if (task instanceof YAtomicTask) {
                contained.add((YAtomicTask) task);
            }

            // if this sub-net contains a composite task, recurse to get all of its
            // task too
            else if (task instanceof YCompositeTask) {
                contained.addAll(getAllContainedAtomicTasks((YCompositeTask) task));
            }
        }
        return contained;
    }


    /**
     * Returns all of the elements of a net that precede the given task.
     * @param task the task to get all the preceding elements for
     * @return the Set of preceding net elements
     */
    private Set<YExternalNetElement> getAllPrecedingElements(YTask task) {
        Set<YExternalNetElement> visited = new HashSet<YExternalNetElement>();
        Set<YExternalNetElement> visiting = new HashSet<YExternalNetElement>();
        visiting.add(task);

        do {
            visited.addAll(visiting);
            visiting = getPreset(visiting);
            visiting.removeAll(visited);
        }
        while (! visiting.isEmpty());

        visited.remove(task);
        return visited;
    }


    /**
     * Gets all of the preceding net elements for a set of net elements
     * @param elements the set of net elements to get the preceding elements of
     * @return the combined Set of preceding net elements
     */
    private Set<YExternalNetElement> getPreset(Set<YExternalNetElement> elements) {
        Set<YExternalNetElement> preset = new HashSet<YExternalNetElement>();
        for (YExternalNetElement element : elements) {
           if (element != null && !(element instanceof YInputCondition)) {
                preset.addAll(element.getPresetElements());
            }
        }
        return preset;
    }


    /**
     * Checks if a task is a member of the root net of a specification
     * @param task the task to check
     * @return true if its in the root net
     */
    private boolean isMemberOfRootNet(YTask task) {
        YNet containingNet = task.getNet();
        YSpecification specification = containingNet.getSpecification();
        return specification.getRootNet().equals(containingNet);
    }


    /**
     * Checks if a task decomposition requires resourcing parameters
     * @param decomposition the decomposition to check
     * @return true if the decomposition needs resourcing
     */
    private boolean isResourcedDecomposition(YDecomposition decomposition) {
        return decomposition != null && decomposition.requiresResourcingDecisions();
    }


    /**
     * Gets the composite task that houses the sub-net of which the specified task
     * is a member
     * @param task the task to get the parent composite task of
     * @return the containing composite task
     */
    private YTask getContainingTask(YTask task) {
        YNet containingNet = task.getNet();
        YSpecification specification = containingNet.getSpecification();

        // for each net in the specification, except for the sub-net that contains
        // the specified task, check each of its tasks to see if the task's
        // decomposition matches the containing net of the specified task
        for (YDecomposition decomposition : specification.getDecompositions()) {
            if ((decomposition instanceof YNet) && ! decomposition.equals(containingNet)) {
                for (YTask candidateTask : ((YNet) decomposition).getNetTasks()) {
                    YDecomposition taskDecomposition =
                            candidateTask.getDecompositionPrototype();
                    if (taskDecomposition != null && taskDecomposition.equals(containingNet)) {
                        return candidateTask;
                    }
                }
            }
        }
        return null;
    }

}
