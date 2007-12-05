/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.filters;

import au.edu.qut.yawl.resourcing.resource.Participant;

import java.util.Set;

/**
 *  A base or generic filter class which allows external entities such as the
 *  Editor the ability to instantiate it as a medium for specification XML generation
 *
 *  Create Date: 14/08/2007. Last Date: 12/11/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 1.0
 */

public class GenericFilter extends AbstractFilter {

    public GenericFilter(String name) { super(name) ; }

    public GenericFilter() {}

    public Set<Participant> performFilter(Set<Participant> resources) { return null; }

}
