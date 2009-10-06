/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.elements;

import java.util.*;

/**
 * This class collects the set of all currently enabled transitions (ie. tasks) of a net.
 * It is designed to provide a completely correct implementation of the YAWL deferred
 * choice semantics.
 *
 * Enabled transitions are grouped by the id of the enabling place (condition). For 
 * each place: if there is one or more composite tasks enabled, one of the composite
 * tasks is chosen (randomly if more than one) and all other tasks are not fired;
 * otherwise, all the atomic tasks are enabled, allowing a choice to be made from the
 * environment, and each atomic task is 'stamped' with an identifier that services may
 * use to identify tasks that are part of the same group.
 *
 * This class is used exclusively by YNetRunner's continueIfPossible() and fireTasks()
 * methods.
 *
 * Author: Michael Adams
 * Date: 5/03/2008
 */
public class YEnabledTransitionSet {

    // a table of [place id, set of enabled transitions]
    private Hashtable<String, TaskGroup> transitions = new Hashtable<String, TaskGroup>();

    // the only constructor
    public YEnabledTransitionSet() { }


    /**
     * Gets the list of condition ids that are enabling this task
     * @param task the enabled task
     * @return a list of condition ids
     */
    private List<String> getFlowsFromIDs(YTask task) {
        List<String> result = new ArrayList<String>();
        Set presets = task.getPresetFlows();
        if (presets != null) {
            Iterator itr = presets.iterator();
            while (itr.hasNext()) {
                YFlow flow = (YFlow) itr.next();
                YNetElement prior = flow.getPriorElement();
                if (prior != null) result.add(prior.getID()) ;
            }
        }
        return result ;
    }


    /**
     * Adds a task to the group with the id passed
     * @param id the id of the enabling condition
     * @param task the enabled task
     */
    private void add(String id, YTask task) {
        TaskGroup group = transitions.get(id) ;
        if (group != null)                              // already a group for this id
            group.add(task);
        else                                            // create a new group
            transitions.put(id, new TaskGroup(task));
    }


    /**
     * Adds an enabled task to the relevant task group
     * @param task the enabled task
     */
    public void add(YTask task) {
         List<String> presetIDs = getFlowsFromIDs(task) ;
         for (String id : presetIDs) add(id, task);
    }


    /**
     * Gets the final set(s) of enabled transitions (one for each enabling place)
     * @return the list of groups
     */
    public List<TaskGroup> getEnabledTaskGroups() {
        return new ArrayList<TaskGroup>(transitions.values());
    }

    /** @return true if there are no enabled transitions in this set */
    public boolean isEmpty() {
        return transitions.isEmpty();
    }


  /*********************************************************************************/

    /**
     * A group of YTasks plus an identifier
     */

    public class TaskGroup extends ArrayList<YTask> {

        private String _id ;                           // the group id of this group


        /** Constructor with no args */
        TaskGroup() {
            super();
            _id = UUID.randomUUID().toString();       // generate a new id for this group
        }

        /**
         * Constructor with an initial group member
         * @param task the first task in this group
         */
        TaskGroup(YTask task) {
            this();
            add(task);
        }


        /**
         * Gets the list of atomic tasks in this group
         * @return the list of enabled atomic tasks
         */
        public List<YAtomicTask> getEnabledAtomicTasks() {
            List<YAtomicTask> result = new ArrayList<YAtomicTask>();
            for (YTask task : this)
                if (task instanceof YAtomicTask) result.add((YAtomicTask) task);
            return result;
        }


        /**
         * Gets the list of composite tasks in this group
         * @return the list of enabled composite tasks
         */
        public List<YCompositeTask> getEnabledCompositeTasks() {
            List<YCompositeTask> result = new ArrayList<YCompositeTask>();
            for (YTask task : this)
                if (task instanceof YCompositeTask) result.add((YCompositeTask) task);
            return result;
        }


        /** @return the number of atomic tasks in this group */
        public int getAtomicTaskCount() {
            return getEnabledAtomicTasks().size();
        }


        /** @return the number of composite tasks in this group */
        public int getCompositeTaskCount() {
            return getEnabledCompositeTasks().size();
        }


        /** @return true if this group has at least one composite task */
        public boolean hasEnabledCompositeTasks() {
            return getCompositeTaskCount() > 0 ;
        }


        /** @return the generated group id for this group */
        public String getID() {
            return _id ;
        }


        /**
         * YAWL semantics are that if there is more than one composite task enabled
         * by a condition, one must be non-deterministically chosen to fire.
         *  
         * @return null if there are no composite tasks in this group, the only
         * composite task if there is one, or a randomly chosen one if there are
         * several
         */
        public YCompositeTask getRandomCompositeTaskFromTaskGroup() {
            List<YCompositeTask> taskList = getEnabledCompositeTasks();
            switch (taskList.size()) {
                case 0 : return null;
                case 1 : return taskList.get(0);
                default: return taskList.get(new Random().nextInt(taskList.size())) ;
            }
        }
    }

}
