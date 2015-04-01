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
    private Map<String, TaskGroup> transitions = new HashMap<String, TaskGroup>();

    // the only constructor
    public YEnabledTransitionSet() { }


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
    public List<TaskGroup> getAllTaskGroups() {
        return new ArrayList<TaskGroup>(transitions.values());
    }


    /** @return true if there are no enabled transitions in this set */
    public boolean isEmpty() {
        return transitions.isEmpty();
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
     * Gets the list of condition ids that are enabling this task
     * @param task the enabled task
     * @return a list of condition ids
     */
    private List<String> getFlowsFromIDs(YTask task) {
        List<String> result = new ArrayList<String>();
        for (YFlow flow : task.getPresetFlows()) {
            YNetElement prior = flow.getPriorElement();
            if (prior != null) result.add(prior.getID()) ;
        }
        return result ;
    }


    /*********************************************************************************/

    /**
     * A group of YTasks plus an identifier
     */

    public class TaskGroup {

        private String _id ;                           // the group id of this group
        private List<YCompositeTask> _compositeTasks;
        private List<YAtomicTask> _atomicTasks;
        private List<YAtomicTask> _emptyAtomicTasks;

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
         * Add a task to this group
         * @param task the task to add
         * @return true if the added task was not already in the group
         */
        public boolean add(YTask task) {
            if (task instanceof YCompositeTask) {
                return addCompositeTask((YCompositeTask) task);
            }
            else if (task instanceof YAtomicTask) {
                return addAtomicTask((YAtomicTask) task);
            }
            return false;
        }


        /**
         * Gets the list of atomic tasks in this group
         * @return the list of enabled atomic tasks
         */
        public List<YAtomicTask> getEnabledAtomicTasks() {
            return _atomicTasks != null ? _atomicTasks
                    : Collections.<YAtomicTask>emptyList();
        }


        /**
         * Gets the list of 'empty' atomic tasks in this group
         * @return the list of enabled atomic tasks
         */
        public List<YAtomicTask> getEnabledEmptyTasks() {
            return _emptyAtomicTasks != null ? _emptyAtomicTasks
                    : Collections.<YAtomicTask>emptyList();
        }


        /**
         * Gets the list of composite tasks in this group
         * @return the list of enabled composite tasks
         */
        public List<YCompositeTask> getEnabledCompositeTasks() {
            return _compositeTasks != null ? _compositeTasks
                    : Collections.<YCompositeTask>emptyList();
        }


        /**
         * If there is more than one task in this group (aka. a deferred choice) and
         * there's at least one empty task amongst them, then the yawl semantics will
         * mean that the empty task will 'win' the choice, and the others in the group
         * will be cancelled. Because timing of the cancellation announcements will
         * precede that of the enablement of those tasks, it is necessary to preempt
         * the enablement announcements. This method is called from YNetRunner.fireTasks
         * so that it knows whether to announce the firings or not.
         * @return true if the group contains an empty atomic task.
         */
        public boolean hasEmptyAtomicTask() {
            return getEmptyTaskCount() > 0;
        }


        /** @return the number of atomic tasks in this group */
        public int getAtomicTaskCount() {
            return _atomicTasks != null ? _atomicTasks.size() : 0;
        }


        /** @return the number of composite tasks in this group */
        public int getCompositeTaskCount() {
            return _compositeTasks != null ? _compositeTasks.size() : 0;
        }


        /** @return the number of composite tasks in this group */
        public int getEmptyTaskCount() {
            return _emptyAtomicTasks != null ? _emptyAtomicTasks.size() : 0;
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
            if (_compositeTasks == null) return null;
            switch (_compositeTasks.size()) {
                case 0 : return null;
                case 1 : return _compositeTasks.get(0);
                default: return _compositeTasks.get(
                        new Random().nextInt(_compositeTasks.size())) ;
            }
        }


        private boolean addCompositeTask(YCompositeTask task) {
            if (_compositeTasks == null) _compositeTasks = new ArrayList<YCompositeTask>();
            return _compositeTasks.add(task);
        }


        private boolean addAtomicTask(YAtomicTask task) {
            if (task.getDecompositionPrototype() == null) {
                if (_emptyAtomicTasks == null) _emptyAtomicTasks = new ArrayList<YAtomicTask>();
                return _emptyAtomicTasks.add(task);
            }
            if (_atomicTasks == null) _atomicTasks = new ArrayList<YAtomicTask>();
            return _atomicTasks.add(task);
        }

    }

}
