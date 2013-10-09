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

package org.yawlfoundation.yawl.editor.ui.properties.editor;

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;

import java.util.*;


public class ServicesPropertyEditor extends ComboPropertyEditor {

    private static Map<String, YAWLServiceReference> services;

    public static final String DEFAULT_WORKLIST = "Default Worklist";

    public ServicesPropertyEditor() {
        super();
        buildServicesMap();
        setAvailableValues(getValues());
    }


    public static YAWLServiceReference getService(String label) {
        return services.get(label);
    }


    private void buildServicesMap() {
        services = new Hashtable<String, YAWLServiceReference>();
        String label = "";
        for (YAWLServiceReference service : YConnector.getServices()) {
            if (! service.canBeAssignedToTask()) {
                continue;          // ignore services that are not for tasks
            }

            if (service.getUserName().equals("DefaultWorklist")) {
                label = DEFAULT_WORKLIST;
            }
            else if (service.getDocumentation() != null) {
                label = service.getDocumentation();
            }
            else label = service.getUserName();

            services.put(label, service);
        }
    }


    private Object[] getValues() {
        List<String> labels = new ArrayList<String>(services.keySet());
        Collections.sort(labels);

        // move default worklist to first choice
        labels.remove(DEFAULT_WORKLIST);
        labels.add(0, DEFAULT_WORKLIST);
        return labels.toArray();
    }

}
