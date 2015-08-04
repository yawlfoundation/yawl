/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.constraints;

import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

import java.util.Set;

/**
 * Separation of Duties - a constraint that disallows a participant who has completed a
 * 'familiar task' in the current case from being allocated a task applying this
 * constraint.
 *
 *  Create Date: 03/08/2007. Last Date: 12/11/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 2.0
 */

public class SeparationOfDuties extends AbstractConstraint {


    public SeparationOfDuties() {
        super();
        setName(this.getClass().getSimpleName());
        addKey("familiarTask");
        setDisplayName("Separation of Duties") ;
        setDescription("The Separation of Duties constraint ensures that a " +
                       "distribution set for a task will not include any " +
                       "participant who completed a work item of the " +
                       "'familiar task' in the current case.");        
    }

    /**
     * Ensures that any participants who completed the 'familiar task' in the current
     * case are removed from the distribution set
     * @param resources the distribution set of participants
     * @param wir the workitem to be resourced
     * @return the constrained distribution set
     */
    public Set<Participant> performConstraint(Set<Participant> resources,
                                              WorkItemRecord wir) {
        String famTaskID = getParamValue("familiarTask") ;
        if (famTaskID != null) {
            Set<Participant> pSet = ResourceManager.getInstance()
                                                   .getWhoCompletedTask(famTaskID, wir);
            if (pSet != null)
                for (Participant p : pSet) resources.remove(p);
        }
        return resources ;
    }



}
