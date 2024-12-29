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

package org.yawlfoundation.yawl.resourcing.allocators;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.Set;

/**
 * Allocates a workitem to a participant on a round-robin basis, to the participant who
 * has not performed the task for the longest time.
 *
 *  Create Date: 23/08/2007. Last Date: 12/11/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 2.0
 */

public class RoundRobinByAllocationTime extends AbstractAllocator {

    public RoundRobinByAllocationTime() {
        super();
        setName(this.getClass().getSimpleName()) ;        
        setDisplayName("Round Robin (by Allocation time)");
        setDescription("The Round-Robin (by Allocation Time) allocator distributes a " +
                "workitem to the participant in the distribution set who was allocated " +
                "(as opposed to completed) the task the least recently.");
    }


    public Participant performAllocation(Set<Participant> participants,
                                         WorkItemRecord wir) {
        return allocateOnStatus(participants, wir, EventLogger.event.allocate);
    }
    
}
