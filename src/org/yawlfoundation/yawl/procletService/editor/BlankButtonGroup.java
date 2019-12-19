/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.procletService.editor;

import javax.swing.*;

public class BlankButtonGroup
    extends ButtonGroup {
  AbstractButton blank;

  public BlankButtonGroup() {
    super();
    blank = new Button();
    blank.setVisible(false);
    blank.setEnabled(false);
    add(blank);
    setSelected(blank.getModel(), true);
  }

  /**
   * Sets the selected value for the <code>ButtonModel</code>.
   * Only one button in the group may be selected at a time.
   * If a button is unselected, then the hidden button 'blank' becomes selected.
   * @param m the <code>ButtonModel</code>
   * @param b <code>true</code> if this button is to be
   *   selected, otherwise <code>false</code>
   */
  public void setSelected(ButtonModel m, boolean b) {
    if (b && m != blank.getModel()) {
      super.setSelected(m, b);
    }
    else {
      super.setSelected(blank.getModel(), true);
    }
  }
}

