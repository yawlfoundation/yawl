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
