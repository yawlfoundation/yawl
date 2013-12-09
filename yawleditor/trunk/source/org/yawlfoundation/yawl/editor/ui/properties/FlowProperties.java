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

package org.yawlfoundation.yawl.editor.ui.properties;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetCellUtilities;

/**
 * @author Michael Adams
 * @date 2/07/12
 */
public class FlowProperties extends NetProperties {

    private YAWLFlowRelation flow;

    private static final int offset = 11;
    protected static final String[] STYLES = new String[] {"Orthogonal", "Bezier", "Spline"};


    public FlowProperties() {
        super();
    }


    protected void setFlow(YAWLFlowRelation f) { flow = f; }

    protected YAWLFlowRelation getFlow() { return flow; }


    public String getSource() { return flow.getSourceID(); }      // read only

    public String getTarget() { return flow.getTargetID(); }      // read only

    public boolean getDefault() { return flow.isDefaultFlow(); }

    public String getPredicate() { return flow.getPredicate(); }


    public Integer getOrdering() { return flow.getPriority(); }


    public String getLineStyle() {
        return STYLES[NetCellUtilities.getFlowLineStyle(graph, flow) - offset];
    }

    public void setLineStyle(String style) {
        int pos = offset;
        for (String s : STYLES) {
            if (style.equals(s)) break;
            pos++;
        }
        NetCellUtilities.setFlowStyle(graph, flow, pos);
        setDirty();
    }
}
