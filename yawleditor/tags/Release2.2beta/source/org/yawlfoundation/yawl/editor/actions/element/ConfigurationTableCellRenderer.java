/**
 * This is downloaded and modified by Jingxin Xu from http://www.jguru.com/faq/view.jsp?EID=53764
 */

package org.yawlfoundation.yawl.editor.actions.element;

import javax.swing.table.*;
import javax.swing.*;

import java.awt.*;

public class ConfigurationTableCellRenderer
       extends DefaultTableCellRenderer {
	
	private Color myColor = null;
	
	public ConfigurationTableCellRenderer(){
		
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
    	
    	
    	if(((JLabel) cell).getText().equals("blocked")){
			this.myColor = Color.RED;		
		}else if(((JLabel) cell).getText().equals("hidden")){
			this.myColor = new Color(204,102,0);
		}else if(((JLabel) cell).getText().equals("activated")){
			this.myColor = new Color(8,84,8);
		}
       cell.setForeground(this.myColor);
       return cell;
  }
}
