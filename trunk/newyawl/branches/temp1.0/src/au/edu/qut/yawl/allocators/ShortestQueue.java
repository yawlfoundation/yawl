/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.allocators;

import au.edu.qut.yawl.resourcing.resource.Participant;
import au.edu.qut.yawl.resourcing.WorkQueue;

import java.util.*;

/**
 * Performs allocation based on work queue length
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 23/08/2007
 */


public class ShortestQueue extends AbstractAllocator {

    public ShortestQueue() { super(); }

    public ShortestQueue(String name) {
        super(name) ;
        setDescription("The Shortest-Queue allocator blah blah.");
    }

    public ShortestQueue(String name, HashMap params) {
        super(name, params) ;
        setDescription("The Shortest-Queue allocator blah blah.");
    }

    /**
     * selects the Participant with the least number of workitems in the 'allocated'
     * workqueue
     * @param resSet the set of Participants to compare
     * @return the Participant with the shortest queue
     */
    public Participant performAllocation(Set<Participant> resSet) {
        int shortest = 10000 ;
        int qSize ;
        Participant result = null ;
        for (Participant p : resSet) {
            if (p.getWorkQueues() != null)
                qSize = p.getWorkQueues().getQueueSize(WorkQueue.ALLOCATED) ;
            else
                qSize = 0 ;
            if (qSize < shortest) {
                shortest = qSize;
                result = p ;
                if (qSize == 0) break ;            // got an empty queue, go no further
            }
        }
        return result ;
    }

    public Set getParamKeys() {return null ; }
}
