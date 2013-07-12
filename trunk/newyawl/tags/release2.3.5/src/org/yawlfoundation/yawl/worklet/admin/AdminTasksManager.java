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

import org.apache.log4j.Logger;

import java.util.*;


/**
 *  This class maintains a set of AdministrationTask instances - outstanding
 *  tasks for the Worklet Administrator to attend to.
 *
 *  @author Michael Adams
 *  @version 0.8, 04-09/2006
 */

public class AdminTasksManager {

    private Map _tasks ;                              // set of tasks to attend to
    private String _nextID ;                          // next unique id

    private static Logger _log = Logger.getLogger(AdminTasksManager.class);


    /** the constructor */
    public AdminTasksManager() {
        _tasks = new HashMap();
        _nextID = "0";
    }

    /******************************************************************************/

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
        AdministrationTask result = new AdministrationTask(caseID, title, scenario,
                                                           process, taskType);
        addTask(result) ;
        return result ;
    }

    /** this version is for item level tasks */
    public AdministrationTask addTask(String caseID, String itemID, String title,
                                      String scenario, String process, int taskType) {
        AdministrationTask result = new AdministrationTask(caseID, itemID, title,
                                                        scenario, process, taskType);
        addTask(result) ;
        return result ;
    }

    /******************************************************************************/

    /**
     * Adds a task to the set of outstanding tasks
     * @param task - the task to add
     */
    public void addTask(AdministrationTask task) {
        task.setID(_nextID);
        _tasks.put(_nextID, task);
        _nextID = inc(_nextID);                                     // increment the id
    }

    /******************************************************************************/

    /**
     * Removes a task from the set of outstanding tasks
     * @param id - the id number of the task to remove
     * @return the removed task
     */
    public AdministrationTask removeTask(String id) {
        AdministrationTask result = null;
        if (_tasks.containsKey(id))  {
            result = (AdministrationTask) _tasks.get(id);
            _tasks.remove(id);
        }
        else _log.error("Can't remove administration task - id does not exist: " + id);

        return result ;
    }

    /******************************************************************************/

    // VARIOUS GETTERS //

    /** @return the set of outstanding tasks */
    public Map getAllTasks() {
        return _tasks ;
    }

    /******************************************************************************/

    /** @return the set of outstanding tasks as an ArrayList */
    public ArrayList getAllTasksAsList() {
       return new ArrayList(_tasks.values());
    }

    /******************************************************************************/

    /**
     * Retrieves a task by its id number
     * @param id - the id to retrieve
     * @return the task the owns that id
     */
    public AdministrationTask getTask(String id) {
        AdministrationTask result = null;
        if (_tasks.containsKey(id))
            result = (AdministrationTask) _tasks.get(id);
        else
            _log.error("Can't get administration task - id does not exist: " + id);

        return result;
    }

    /******************************************************************************/

    /**
     * Retrieves a list of all outstanding tasks of the specified type
     * @param taskType - the type of task to retrieve
     * @return a list of all those tasks of the type specified
     */
    public ArrayList getAdminTasksForType(int taskType) {
        ArrayList result = new ArrayList() ;
        AdministrationTask task ;
        int i, maxID = Integer.parseInt(_nextID);

        // for each task (by id number)
        for (i=0; i<maxID; i++) {
           if (_tasks.containsKey(String.valueOf(i))) {
               task = (AdministrationTask) _tasks.get(String.valueOf(i));

               // if this task is of the type specified, add it to the result list
               if (task.getTaskType() == taskType)
                  result.add(task);
           }
        }

        if (result.isEmpty()) result = null ;          // return null if no matches
        return result ;
    }

    /******************************************************************************/

    /**
     * Retrieves a task by its title
     * @param taskTitle - the title of the task required
     * @return the task that has that title
     */
    public AdministrationTask getAdminTaskbyTitle(String taskTitle) {
        AdministrationTask task ;
        ArrayList list = new ArrayList(_tasks.values());
        Iterator itr = list.iterator();

        if (itr != null) {
            while (itr.hasNext()) {
                task = (AdministrationTask) itr.next() ;
                if (task.getTitle().equalsIgnoreCase(taskTitle))
                   return task ;
            }
        }
        return null ;
    }

    /******************************************************************************/

    /**
     * Retrieves a list of task titles for all tasks of the specified type
     * @param taskType - the type of task to get the titles for
     * @return a list of titles for that type
     */
    public ArrayList getTaskTitlesForType(int taskType) {
        return getTitlesFromList(getAdminTasksForType(taskType)) ;
    }

    /******************************************************************************/

    /** @return a list of all titles from all outstanding tasks */
    public ArrayList getAllTaskTitles() {
        return getTitlesFromList(new ArrayList(_tasks.values())) ;
    }

    /******************************************************************************/

    /**
     * Retrieves a list of titles from the list of tasks passed
     * @param list - a list of AdministrationTasks
     * @return the list of titles of those tasks
     */
    private ArrayList getTitlesFromList(ArrayList list) {
        ArrayList result = new ArrayList() ;
        AdministrationTask task ;
        Iterator itr = list.iterator();

        if (itr != null) {
            while (itr.hasNext()) {
                task = (AdministrationTask) itr.next() ;
                result.add(task.getTitle());
            }
        }

        if (result.isEmpty()) result = null ;        // return null if no matches
        return result ;
    }

    /******************************************************************************/

    /** increments nextID by one */
    private String inc(String numStr) {
        int num = Integer.parseInt(numStr);
        return String.valueOf(++num);
    }

    /******************************************************************************/

    public String toString() {
        StringBuilder s = new StringBuilder() ;
        List tasks = getAllTasksAsList() ;
        Iterator itr = tasks.iterator();

        while (itr.hasNext()) {
            s.append(itr.next().toString());
        }
        s.append("NEXT ID: ");
        s.append(_nextID);
        s.append('\n');

        return s.toString() ;
    }

    /******************************************************************************/
    /******************************************************************************/

} // end AdminTasksManager.
