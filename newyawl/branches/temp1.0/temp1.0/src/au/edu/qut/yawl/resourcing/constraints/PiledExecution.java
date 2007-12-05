/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.constraints;

import au.edu.qut.yawl.resourcing.resource.Participant;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;

import java.util.Set;

/**
 * An implementation of the Piled Execution constraint
 *
 *  Create Date: 03/08/2007. Last Date: 12/11/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 1.0
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
        // todo
        return null ;
    }



}
