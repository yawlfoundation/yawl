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

package org.yawlfoundation.yawl.editor.ui.properties.data.binding.references;

import org.yawlfoundation.yawl.editor.core.data.BindingReference;
import org.yawlfoundation.yawl.editor.ui.swing.JSingleSelectTable;

import javax.swing.table.TableRowSorter;
import java.util.List;

/**
* @author Michael Adams
* @date 3/08/12
*/
class BindingReferencesTable extends JSingleSelectTable {

    public BindingReferencesTable() {
        super();
        consumeEnterKeyWraps();
        setModel(new BindingReferencesTableModel());
        setRowHeight(getRowHeight() + 5);
        setRowSelectionAllowed(true);
        setFillsViewportHeight(true);
        getColumnModel().getColumn(3).setPreferredWidth(30);
        getColumnModel().getColumn(4).setPreferredWidth(250);
        setRowSorter(new TableRowSorter(getModel()));
    }


    public void setRows(List<BindingReference> rows) {
        ((BindingReferencesTableModel) getModel()).setRows(rows);
    }


    public BindingReference getSelectedReference() {
        return ((BindingReferencesTableModel) getModel())
                .getReferenceAtRow(getSelectedRow());
    }

}
