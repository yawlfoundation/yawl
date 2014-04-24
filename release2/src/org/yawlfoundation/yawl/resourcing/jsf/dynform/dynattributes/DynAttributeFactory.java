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

package org.yawlfoundation.yawl.resourcing.jsf.dynform.dynattributes;

import com.sun.rave.web.ui.component.PanelLayout;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.DynFormField;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.util.PluginFactory;

import java.util.List;
import java.util.Set;

/**
 * This factory class creates and instantiates instances of the various dynamic form
 * attribute classes found in this package or on external classpaths.
 *
 * Create Date: 18/05/2009.
 *
 * @author Michael Adams
 * @version 2.0
 */

public class DynAttributeFactory {

    private static Set<AbstractDynAttribute> _instances;

    public static void applyAttributes(PanelLayout parentPanel, WorkItemRecord wir, Participant p) {
        for (AbstractDynAttribute attributeClass : getInstances()) {
            attributeClass.applyAttributes(parentPanel, wir, p);
        }
    }

    public static void adjustFields(List<DynFormField> fieldList, WorkItemRecord wir, Participant p) {
        for (AbstractDynAttribute attributeClass : getInstances()) {
            attributeClass.adjustFields(fieldList, wir, p);
        }
    }


    /**
     * Constructs and returns a list of instantiated dynAttribute objects, one for each
     * of the different dynAttribute classes available in this package
     *
     * @return a List of instantiated allocator objects
     */
    private static Set<AbstractDynAttribute> getInstances() {
        if (_instances == null) {
            _instances = PluginFactory.getDynAttributes();
        }
        return _instances;
    }

}