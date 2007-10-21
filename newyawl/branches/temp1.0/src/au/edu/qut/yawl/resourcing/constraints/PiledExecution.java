/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.constraints;

import au.edu.qut.yawl.resourcing.constraints.AbstractConstraint;
import au.edu.qut.yawl.resourcing.resource.Participant;

import java.util.*;

/**
 * A ....
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 23/08/2007
 */

public class PiledExecution extends AbstractConstraint {

    public PiledExecution() { super(); }

    public PiledExecution(String name) {
        super(name) ;
        setDescription("The Piled Execution constraint blah blah...");
    }

    public PiledExecution(String name, HashMap params) {
        super(name, params) ;
        setDescription("The Piled Execution constraint blah blah...");       
    }

    public Set<Participant> performConstraint(Set<Participant> l) { return null ; }



}
