/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements.state;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YNetElement;
import au.edu.qut.yawl.elements.YTask;

/**
 * 
 * @author Lachlan Aldred
 * Date: 26/06/2003
 * Time: 14:08:30
 * 
 */
public class YOrJoinUtils {

    public static synchronized List<YExternalNetElement> reduceToEnabled(YMarking marking, YTask orJoin) {
        if (marking == null || orJoin == null) {
            return null;
        }
        Set<YExternalNetElement> visitedFw = new HashSet<YExternalNetElement>();
        Set<YExternalNetElement> visitingFw = new HashSet<YExternalNetElement>();
        for (Iterator iterator = marking.getLocations().iterator(); iterator.hasNext();) {
            YNetElement element = (YNetElement) iterator.next();
            if (element instanceof YExternalNetElement) {
                visitingFw.add((YExternalNetElement) element);
            }
        }
        Set<YExternalNetElement> visitedBk = new HashSet<YExternalNetElement>();
        Set<YExternalNetElement> visitingBk = new HashSet<YExternalNetElement>();
        visitingBk.add(orJoin);
        /*  REPEAT
                oldVisitedFw := visitedFw;
                visitedFw := visitedFw UNION
                    {n isin N.nodes | EXISTS v isin visitedFw : n isin postset(v)
                    AND n.joinType <> "OR"}
            UNTIL oldvisitedFw = visitedFw; */
        do {
            visitedFw.addAll(visitingFw);
            visitingFw = YNet.getPostset(visitingFw);
            HashSet orJoins = new HashSet();
            for (Iterator iterator = visitingFw.iterator(); iterator.hasNext();) {
                YExternalNetElement element = (YExternalNetElement) iterator.next();
                if (element instanceof YTask) {
                    if (((YTask) element).getJoinType() == YTask._OR) {
                        orJoins.add(element);
                    }
                }
            }
            visitingFw.removeAll(orJoins);
            visitingFw.removeAll(visitedFw);

        } while (visitingFw.size() > 0);
        /*  REPEAT
                oldVisitedBk := visitedBk;
                visitedBk := visitedBk UNION
                    {n isin N.nodes | EXISTS v isin visitedBk : n isin preset(v)
                    AND n.joinType <> "OR"}
            UNTIL oldvisitedBk = visitedBk; */
        do {
            visitedBk.addAll(visitingBk);
            visitingBk = YNet.getPreset(visitingBk);
            HashSet orJoins = new HashSet();
            for (Iterator iterator = visitingBk.iterator(); iterator.hasNext();) {
                YExternalNetElement element = (YExternalNetElement) iterator.next();
                if (element instanceof YTask) {
                    if (((YTask) element).getJoinType() == YTask._OR) {
                        orJoins.add(element);
                    }
                }
            }
            visitingBk.removeAll(orJoins);
            visitingBk.removeAll(visitedBk);
        } while (visitingBk.size() > 0);
        List<YExternalNetElement> enabledNetElements = new ArrayList<YExternalNetElement>();
        for (Iterator iterator = visitedFw.iterator(); iterator.hasNext();) {
            YExternalNetElement element = (YExternalNetElement) iterator.next();
            if (visitedBk.contains(element)) {
                if (element instanceof YTask) {
                    if (marking.nonOrJoinEnabled((YTask) element)) {
                        enabledNetElements.add(element);
                    }
                }
            }
        }
        return enabledNetElements;
    }


    public static synchronized YTask pickOptimalEnabledTask(
    		List<YExternalNetElement> enabledTasks, YTask orJoin) {
        YTask taskWithShortestPath = null;
        int numSteps = Integer.MAX_VALUE;
        for (Iterator iterator = enabledTasks.iterator(); iterator.hasNext();) {
            YTask task = (YTask) iterator.next();
            int num = countStepsInShortestPath(task, orJoin);
            if (taskWithShortestPath == null) {
                taskWithShortestPath = task;
                numSteps = num;
            } else {
                if (num < numSteps) {
                    taskWithShortestPath = task;
                    numSteps = num;
                }
            }
        }
        return taskWithShortestPath;
    }

    public static synchronized YTask pickOptimalEnabledTask(
    		List<YExternalNetElement> enabledTasks, YTask orJoin,
            YMarking currentlyConsideredMarking, YSetOfMarkings markingsAlreadyConsidered) {
    	List<YExternalNetElement> enabledTasksThatWillChangeTheMarking =
                getEnabledTasksThatWillChangeTheMarking(
                        enabledTasks, orJoin, currentlyConsideredMarking, markingsAlreadyConsidered);
        if (enabledTasksThatWillChangeTheMarking.size() > 0) {
            return pickOptimalEnabledTask(enabledTasksThatWillChangeTheMarking, orJoin);
        } else {
            return pickOptimalEnabledTask(enabledTasks, orJoin);
        }
    }

    private static List<YExternalNetElement> getEnabledTasksThatWillChangeTheMarking(
    		List<YExternalNetElement> enabledTasks, YTask orjoin, YMarking currentlyConsideredMarking,
            YSetOfMarkings markingsAlreadyConsidered) {
    	List<YExternalNetElement> enabledTasksThatWillChangeTheMarking = new ArrayList<YExternalNetElement>();
        for (Iterator iterator = enabledTasks.iterator(); iterator.hasNext();) {
            YTask task = (YTask) iterator.next();
            List<YExternalNetElement> outputConditions = task.getPostsetElements();
            if (!currentlyConsideredMarking.getLocations().containsAll(outputConditions)) {
                enabledTasksThatWillChangeTheMarking.add(task);
            }
        }
        return enabledTasksThatWillChangeTheMarking;
    }


    public static List<YExternalNetElement> getRelevantPostset(YTask aTask, YTask orJoin) {
    	List<YExternalNetElement> returnSet = new ArrayList<YExternalNetElement>();
    	Set<YExternalNetElement> visiting = new HashSet<YExternalNetElement>();
        visiting.add(orJoin);
        Set<YExternalNetElement> visited = new HashSet<YExternalNetElement>();
        while (visiting.size() > 0) {
            visited.addAll(visiting);
            visiting = YNet.getPreset(visiting);
            visiting.removeAll(visited);
        }
        for (Iterator postsetIter = aTask.getPostsetElements().iterator();
             postsetIter.hasNext();) {
            YCondition condition = (YCondition) postsetIter.next();
            if (visited.contains(condition)) {
                returnSet.add(condition);
            }
        }
        return returnSet;
    }


    private static synchronized int countStepsInShortestPath(YTask task, YTask orJoin) {
    	Set<YExternalNetElement> visiting = new HashSet<YExternalNetElement>();
        visiting.add(task);
        Set<YExternalNetElement> visited = new HashSet<YExternalNetElement>();
        for (int i = 0; visiting.size() > 0; i++) {
            if (visiting.contains(orJoin)) {
                return i;
            }
            visited.addAll(visiting);
            visiting = YNet.getPostset(visiting);
            visiting.removeAll(visited);
        }
        throw new RuntimeException("There is no number of steps between unconnected tasks.");
    }


}
