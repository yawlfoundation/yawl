/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.core.resourcing.entity;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.editor.core.resourcing.ResourceDataSet;
import org.yawlfoundation.yawl.editor.core.resourcing.validation.InvalidConstraintReference;
import org.yawlfoundation.yawl.resourcing.constraints.AbstractConstraint;
import org.yawlfoundation.yawl.resourcing.constraints.GenericConstraint;
import org.yawlfoundation.yawl.resourcing.interactions.ResourceParseException;

import java.util.List;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 12/07/13
 */
public class ConstraintSet extends EntityCollection<AbstractConstraint> {

    public ConstraintSet() { this(false); }

    public ConstraintSet(boolean allowDuplicates) {
            super(allowDuplicates);
    }


    public boolean add(String name) {
        return add(name, null);
    }


    public boolean add(String name, Map<String, String> params) {
        AbstractConstraint constraint = ResourceDataSet.getConstraint(name);
        if (constraint != null) {

            // if its one of ours, we want the simple class name only
            String constraintName = constraint.getCanonicalName();
            if (constraintName.startsWith(YAWL_PACKAGE_ROOT)) {
                name = constraintName.substring(constraintName.lastIndexOf('.') + 1);
            }

            // create new generic constraint - only used to generate xml on save
            add(new GenericConstraint(name, params));
        }
        else addInvalidReference(new InvalidConstraintReference(name));
        return constraint != null;
    }


    public AbstractConstraint get(String name) {
        for (AbstractConstraint f : getAll()) {
            if (f.getCanonicalName().endsWith(name)) return f;
        }
        return null;
    }


    public void parse(Element e, Namespace nsYawl) throws ResourceParseException {
        Element eConstraints = e.getChild("constraints", nsYawl);
        if (eConstraints != null) {
            List<Element> constraints = eConstraints.getChildren("constraint", nsYawl);
            if (constraints == null)
                throw new ResourceParseException(
                        "No constraint elements found in constraints element");

            for (Element eConstraint : constraints) {
                String constraintClassName = eConstraint.getChildText("name", nsYawl);
                if (constraintClassName != null) {
                    add(constraintClassName, parseParams(eConstraint, nsYawl));
                }
                else throw new ResourceParseException("Missing constraint element: name");
            }
        }
    }

}
