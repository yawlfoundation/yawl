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

import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

import java.util.Set;

/**
 * An implementation of the Piled Execution constraint
 *
 *  Create Date: 03/08/2007. Last Date: 12/11/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 2.0
 */

public class PiledExecution extends AbstractConstraint {

    public PiledExecution() {
        super();
        setName(this.getClass().getSimpleName()) ;
        setDisplayName("Piled Execution");
        setDescription("The Piled Execution constraint allows a participant to choose " +
                       "at runtime to receive all workitems for the chosen task " +
                       "across all cases of the specification.");
    }


    public Set<Participant> performConstraint(Set<Participant> p, WorkItemRecord wir) {
        // currently a stub as piling is handled elsewhere
        return p ;
    }



}
