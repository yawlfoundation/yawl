/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

import org.jdom2.Element;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.AbstractSelector;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.Map;
import java.util.Set;

/**
 * The base class for all constraints.
 * <p/>
 * Create Date: 03/08/2007. Last Date: 12/11/2007
 *
 * @author Michael Adams (BPM Group, QUT Australia)
 * @version 2.0
 */

public abstract class AbstractConstraint extends AbstractSelector {

    /**
     * Constructors
     */

    public AbstractConstraint() { super(); }           // for reflection

    public AbstractConstraint(String name) {
        super(name);
    }

    public AbstractConstraint(String name, Map<String, String> params) {
        super(name, params);
    }

    public AbstractConstraint(String name, String description) {
        super(name, description);
    }


    public AbstractConstraint(String name, String desc, Map<String, String> params) {
        super(name, params);
        _description = desc;
    }

    /*******************************************************************************/

    /**
     * Generates the XML required for the specification file
     *
     * @return an XML'd String containing the member values of an instantiation of this
     *         class
     */
    public String toXML() {
        StringBuilder result = new StringBuilder("<constraint>");
        result.append(super.toXML());
        result.append("</constraint>");
        return result.toString();
    }


    /*******************************************************************************/

    // ABSTRACT METHOD - to be implemented by extending classes //

    /**
     * Performs a constraint against the distribution set passed
     *
     * @param resources the distribution set of participants
     * @param wir       the workitem being resourced
     * @return the constrined distribution set
     */
    public abstract Set<Participant> performConstraint(Set<Participant> resources,
                                                       WorkItemRecord wir);

    /*******************************************************************************/

}
