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

package org.yawlfoundation.yawl.worklet.admin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.worklet.exception.ExceptionService;
import org.yawlfoundation.yawl.worklet.support.Persister;

import java.util.*;


/**
 *  This class maintains a set of AdministrationTask instances - outstanding
 *  tasks for the Worklet Administrator to attend to.
 *
 *  @author Michael Adams
 *  @version 0.8, 04-09/2006
 */

public class AdminTasksManager {

    private final Map<Integer, AdministrationTask> _tasks;   // set of tasks to attend to
    private Integer _nextID;                                // next unique id

    private static Logger _log = LogManager.getLogger(AdminTasksManager.class);


    /** the constructor */
    public AdminTasksManager() {
        _tasks = new HashMap<Integer, AdministrationTask>();
        _nextID = 0;
        restore();
    }


    /**
     * Creates a new tasks and adds it to the set of outstanding tasks
     * @param caseID - id of the case that caused the task to be raised
     * @param title - user supplied title for the task
     * @param scenario - user supplied scenario describing the task
     * @param process - user described suggestion of what the new process should do
     * @param taskType - rejected selection or external exception
     * @return the new AdministrationTask
     */
    public AdministrationTask addTask(String caseID, String title, String scenario,
                        String process, int taskType) {
        AdministrationTask task = new AdministrationTask(caseID, title, scenario,
                                                           process, taskType);
        addTask(task);
        Persister.insert(task);

        // suspend case pending admin action
        ExceptionService.getInst().suspendCase(caseID);

        return task;
    }


    /** this version is for item level tasks */
    public AdministrationTask addTask(String caseID, String itemID, String title,
                                      String scenario, String process, int taskType) {
        AdministrationTask task = new AdministrationTask(caseID, itemID, title,
                                                        scenario, process, taskType);
        addTask(task);
        Persister.insert(task);

        // suspend item pending admin action
        ExceptionService.getInst().suspendWorkItem(itemID);

        return task;
    }


    /**
     * Adds a task to the set of outstanding tasks
     * @param task - the task to add
     */
    public void addTask(AdministrationTask task) {
        task.setID(_nextID);
        _tasks.put(_nextID, task);
        _nextID++;                                     // increment the id
    }


    /**
     * Removes a task from the set of outstanding tasks
     * @param id - the id number of the task to remove
     * @return the removed task
     */
    public AdministrationTask removeTask(int id) {
        AdministrationTask task = _tasks.remove(id);
        if (task != null) {
            Persister.delete(task);
        }
        else {
            _log.error("Failed to remove administration task - id does not exist: {}", id);
        }
        return task ;
    }

    /******************************************************************************/

    // VARIOUS GETTERS //

    /** @return the set of outstanding tasks */
    public Map getAllTasks() {
        return _tasks ;
    }


    /** @return the set of outstanding tasks as an ArrayList */
    public List<AdministrationTask> getAllTasksAsList() {
       return new ArrayList<AdministrationTask>(_tasks.values());
    }


    /**
     * Retrieves a task by its id number
     * @param id - the id to retrieve
     * @return the task the owns that id
     */
    public AdministrationTask getTask(int id) {
        AdministrationTask task = _tasks.get(id);
        if (task == null) {
            _log.error("Failed to get administration task - id does not exist: {}", id);
        }
        return task;
    }


    /**
     * Retrieves a list of all outstanding tasks of the specified type
     * @param taskType - the type of task to retrieve
     * @return a list of all those tasks of the type specified
     */
    public List<AdministrationTask> getAdminTasksForType(int taskType) {
        List<AdministrationTask> result = new ArrayList<AdministrationTask>();
        for (AdministrationTask task : _tasks.values()) {

            // if this task is of the type specified, add it to the result list
            if (task.getTaskType() == taskType) result.add(task);
        }
        return result ;
    }


    /**
     * Retrieves a task by its title
     * @param taskTitle - the title of the task required
     * @return the task that has that title
     */
    public AdministrationTask getAdminTaskByTitle(String taskTitle) {
        for (AdministrationTask task : _tasks.values()) {
            if (task.getTitle().equalsIgnoreCase(taskTitle)) {
                return task;
            }
        }
        return null;
    }


    /**
     * Retrieves a list of task titles for all tasks of the specified type
     * @param taskType - the type of task to get the titles for
     * @return a list of titles for that type
     */
    public List<String> getTaskTitlesForType(int taskType) {
        return getTitlesFromList(getAdminTasksForType(taskType)) ;
    }


    /** @return a list of all titles from all outstanding tasks */
    public List<String> getAllTaskTitles() {
        return getTitlesFromList(_tasks.values()) ;
    }


    /**
     * Retrieves a list of titles from the list of tasks passed
     * @param list - a list of AdministrationTasks
     * @return the list of titles of those tasks
     */
    private List<String> getTitlesFromList(Collection<AdministrationTask> list) {
        List<String> result = new ArrayList<String>();
        for (AdministrationTask task : list) {
            result.add(task.getTitle());
        }
        return result ;
    }


    /**
     * rebuilds admin tasks from persistence
     */
    private void restore() {
        List items = Persister.getInstance().getObjectsForClass(
                AdministrationTask.class.getName());

        if (items != null) {
            for (Object o : items) {
                addTask((AdministrationTask) o);
            }
        }
    }

}
