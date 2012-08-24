/**
 * This is downloaded and modified by Jingxin Xu from http://www.jguru.com/faq/view.jsp?EID=53764
 */

package org.yawlfoundation.yawl.editor.ui.actions.element;

import javax.swing.table.*;
import javax.swing.*;

import java.awt.*;

public class ConfigurationTableCellRenderer extends DefaultTableCellRenderer {

    private static final Color blockedColor = Color.RED;
    private static final Color hiddenColor = new Color(204,102,0);
    private static final Color activatedColor = new Color(8,84,8);


    public ConfigurationTableCellRenderer() { }


    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {

        JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);

        cell.setVerticalAlignment(JLabel.CENTER);
        cell.setHorizontalAlignment(JLabel.CENTER);
        cell.setVerticalTextPosition(JLabel.CENTER);
        cell.setHorizontalTextPosition(JLabel.CENTER);

        if (cell.getText().equals("blocked")) {
            cell.setForeground(blockedColor);
        }
        else if(cell.getText().equals("hidden")) {
            cell.setForeground(hiddenColor);
        }
        else if(cell.getText().equals("activated")) {
            cell.setForeground(activatedColor);
        }
        return cell;
    }
}
