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

/**
 * Created by Jingxin XU
 */
package org.yawlfoundation.yawl.editor.ui.actions.element;

import javax.swing.table.*;
import javax.swing.*;

import java.awt.*;

public class PortIDRenderer
       extends DefaultTableCellRenderer {
	
	
	
	public PortIDRenderer(){
		
	}
	
	
	
  public Component getTableCellRendererComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int row,
                                                 int column) {
    Component cell = super.getTableCellRendererComponent(table, value,
    										isSelected, hasFocus,
    										row, column);

    (   (JLabel)   cell).setVerticalAlignment(JLabel.CENTER);   
    (   (JLabel)   cell).setHorizontalAlignment(JLabel.CENTER);   
    (   (JLabel)   cell).setVerticalTextPosition(JLabel.CENTER);   
    (   (JLabel)   cell).setHorizontalTextPosition(JLabel.CENTER);   
   
    return cell;
  }
}
