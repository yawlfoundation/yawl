package org.yawlfoundation.yawl.resourcing.util;

import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * Author: Michael Adams
 * Creation Date: 13/02/2009
 */

public class FourEyesCache {

    class CaseCache {}
    private String _caseid ;
    private Map<String, TaskCompleterSet> _taskTable;

    public FourEyesCache(String caseid) {
        _caseid = caseid ;
        _taskTable = new Hashtable<String, TaskCompleterSet>();
    }


    public String getCaseID() { return _caseid ; }


    public TaskCompleterSet addTask(String taskid) {
        TaskCompleterSet taskSet = new TaskCompleterSet(taskid);
        _taskTable.put(taskid, taskSet);
        return taskSet ;
    }


    public void addCompleter(String taskid, Participant p) {
        TaskCompleterSet taskSet = _taskTable.get(taskid) ;
        if (taskSet == null) taskSet = addTask(taskid);
        taskSet.add(p);
    }


    public void removeCompleter(String taskid, Participant p) {
        TaskCompleterSet taskSet = _taskTable.get(taskid) ;
        if (taskSet != null) taskSet.remove(p);
    }


    public Set<Participant> getCompleters(String taskid) {
        TaskCompleterSet taskSet = _taskTable.get(taskid) ;
        if (taskSet != null)
            return taskSet.getCompleters();
        else return null;
    }

    
    /******************************************************************************/

    class TaskCompleterSet {
        private String _taskid ;
        private Set<Participant> _completers ;

        public TaskCompleterSet(String taskid) {
            _taskid = taskid ;
            _completers = new HashSet<Participant>();
        }


        public String getTaskID() { return _taskid; }

        public void add(Participant p) { _completers.add(p); }

        public void remove(Participant p) { _completers.remove(p); }

        public Set<Participant> getCompleters() { return _completers; }
    }
}
