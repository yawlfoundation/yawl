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

package org.yawlfoundation.yawl.editor.ui.swing;

public abstract class JOrderedSingleSelectTable extends JSingleSelectTable {

  public JOrderedSingleSelectTable() {
    super();
  }

  public JOrderedSingleSelectTable(int rows) {
    super(rows);
  }
  
  public AbstractOrderedRowTableModel getOrderedTableModel() {
    return (AbstractOrderedRowTableModel) getModel();
  }
  
  public void moveRowUp() {
    getOrderedTableModel().raiseRow(getSelectedRow());
    selectRow(getSelectedRow() - 1);
  }

  public void moveRowDown() {
    getOrderedTableModel().lowerRow(getSelectedRow());
    selectRow(getSelectedRow() + 1);
  }
}