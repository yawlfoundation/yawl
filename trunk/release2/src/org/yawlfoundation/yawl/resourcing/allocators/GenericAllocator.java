/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.allocators;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.Set;

/**
 *  A base or generic allocator class which allows external entities such as the
 *  Editor the ability to instantiate it as a medium for specification XML generation
 *
 *  Create Date: 23/08/2007. Last Date: 09/11/2007
 * 
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 2.0
 */

public class GenericAllocator extends AbstractAllocator {

    public GenericAllocator(String name) { super(name); }

    public GenericAllocator() { super() ; }

    public Participant performAllocation(Set<Participant> resources, WorkItemRecord wir) {
        return null;
    }
}
