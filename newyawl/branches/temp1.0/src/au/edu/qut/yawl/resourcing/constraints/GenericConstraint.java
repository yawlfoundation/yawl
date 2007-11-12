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
 *  A base or generic constraint class which allows external entities such as the
 *  Editor the ability to instantiate it as a medium for specification XML generation
 *
 *  Create Date: 14/08/2007. Last Date: 12/11/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 1.0
 */

public class GenericConstraint extends AbstractConstraint {

    public GenericConstraint() { super(); }

    public Set<Participant> performConstraint(Set<Participant> resources,
                                              WorkItemRecord wir) { return null; }

}
