/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.Random;
import java.util.Set;

/**
 * Performs allocation based on a random selection from a set
 *
 *  Create Date: 23/08/2007. Last Date: 12/11/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 2.0
 */


public class RandomChoice extends AbstractAllocator {

    public RandomChoice() {
        super() ;
        setName(this.getClass().getSimpleName()) ;
        setDisplayName("Random Choice") ;
        setDescription("The Random Choice allocator chooses one participant from " +
                       "the distribution set on a random basis.");
    }


    /**
     * randomly selects a single participant from the list provided
     * @param participants a distribution set of participants
     * @param wir the work item to allocate
     * @return a single participant, or null if participants is null or empty
     */
    public Participant performAllocation(Set<Participant> participants,
                                         WorkItemRecord wir) {

        if (participants == null) return null ;            // case: null set

        Object[] op = participants.toArray();
        if (op.length == 0) return null ;                  // case: empty set
        if (op.length == 1) return (Participant) op[0] ;   // case: only one member

        return (Participant) op[new Random().nextInt(op.length)];
    }

}
