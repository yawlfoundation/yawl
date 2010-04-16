package org.yawlfoundation.yawl.editor.swing.data;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author Mike Fowler
 *         Date: Oct 28, 2005
 */
public class ExtendedAttributeRenderer extends Component implements TableCellRenderer {

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        ExtendedAttribute attribute = (ExtendedAttribute) value;
        if (attribute.getType().equals("boolean")) {
//            JCheckBox box = new JCheckBox() ;
//            box.setSelected(attribute.getValue().equalsIgnoreCase("true"));
//            box.setHorizontalAlignment(SwingConstants.CENTER);
//            return box;
            return attribute.getComponent();
        }
        else if (attribute.getComponent() instanceof JComboBox) {
            return attribute.getComponent();
        }
        else if (attribute.getType().equals("color")) {
            return renderColourCell(attribute);
        }
        else {
            return new JLabel(attribute.getValue());
        }
    }


    private JLabel renderColourCell(ExtendedAttribute attribute) {
        JLabel label = new JLabel(attribute.getValue());
        Color color = attribute.hexToColour(attribute.getValue());
        label.setOpaque(true);
        label.setBackground(color);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setToolTipText("RGB value: " + color.getRed() + ", "
                                 + color.getGreen() + ", "
                                 + color.getBlue());
        return label;
    }

}
