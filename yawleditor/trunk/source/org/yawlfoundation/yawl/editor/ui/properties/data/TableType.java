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

package org.yawlfoundation.yawl.editor.ui.properties.data;

/**
* @author Michael Adams
* @date 10/08/12
*/
public enum TableType {

    Net {
        public VariableTableModel getModel() { return new NetVarTableModel(); }

        public int getPreferredWidth() { return 600; }

        public String getName() { return "Net"; }
    },

    TaskInput {
        public VariableTableModel getModel() { return new TaskInputVarTableModel(); }

        public int getPreferredWidth() { return 350; }

        public String getName() { return "Decomposition Input"; }
    },

    TaskOutput {
        public VariableTableModel getModel() { return new TaskOutputVarTableModel(); }

        public int getPreferredWidth() { return 350; }

        public String getName() { return "Decomposition Output"; }
    };


    public abstract VariableTableModel getModel();

    public abstract int getPreferredWidth();

    public abstract String getName();
}
