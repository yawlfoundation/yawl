/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;

import java.util.*;

/**
 * @author Michael Adams
 * @date 28/02/2014
 */
public class TableRowFactory {

    public List<VariableRow> createRows(YDecomposition decomposition, YTask task) {
        String elementID = task != null ? task.getID() : decomposition.getID();
        Set<String> ioNames = new HashSet<String>();
        java.util.List<VariableRow> rows = new ArrayList<VariableRow>();

        // join inputs & outputs, then add them, and add input-only parameters too
        for (String name : decomposition.getInputParameterNames()) {
            YParameter input = decomposition.getInputParameters().get(name);
            if (decomposition.getOutputParameterNames().contains(name)) {
                YParameter output = decomposition.getOutputParameters().get(name);
                if (input.getDataTypeName().equals(output.getDataTypeName())) {
                    ioNames.add(name);
                }
            }
            rows.add(new VariableRow(input, ioNames.contains(name), elementID));
        }

        // add output only
        Set<String> dummyLocalNames = new HashSet<String>();
        for (String name : decomposition.getOutputParameterNames()) {
            if (! ioNames.contains(name)) {
                rows.add(new VariableRow(decomposition.getOutputParameters().get(name),
                        elementID));
                dummyLocalNames.add(name);
            }
        }

        if (decomposition instanceof YNet) {

            // add locals that weren't created to 'shadow' output-only parameters
            for (YVariable variable : ((YNet) decomposition).getLocalVariables().values()) {
                if (! dummyLocalNames.contains(variable.getName())) {
                    rows.add(new VariableRow(variable, elementID));
                }
            }
        }

        Collections.sort(rows);
        return rows;
    }

}
