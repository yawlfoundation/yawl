package org.yawlfoundation.yawl.editor.swing.data;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author Mike Fowler
 *         Date: Oct 28, 2005
 * @author Michael Adams for 2.1 04/2010
 */
public class ExtendedAttributeRenderer extends Component implements TableCellRenderer {

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        if (column == 0) {
            ExtendedAttribute attribute = (ExtendedAttribute) table.getValueAt(row, 1);
            return renderPlainCell(attribute, (String) value);
        }

        ExtendedAttribute attribute = (ExtendedAttribute) value;
        if (attribute.getType().equals("boolean")) {
            return attribute.getComponent();
        }
        else if (attribute.getComponent() instanceof JComboBox) {
            return attribute.getComponent();
        }
        else if (attribute.getType().equals("color")) {
            return renderColourCell(attribute);
        }
        else if (attribute.isNumericType()) {
            return renderNumericCell(attribute);
        }
        else {
            return renderPlainCell(attribute, null);
        }
    }


    private JLabel renderColourCell(ExtendedAttribute attribute) {
        JLabel label = renderPlainCell(attribute, attribute.getValue());
        Color color = attribute.hexToColour(attribute.getValue());
        label.setOpaque(true);
        label.setBackground(color);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setToolTipText("RGB value: " + color.getRed() + ", "
                                 + color.getGreen() + ", "
                                 + color.getBlue());
        return label;
    }

    private JLabel renderNumericCell(ExtendedAttribute attribute) {
        JLabel label = renderPlainCell(attribute, attribute.getValue());
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    private JLabel renderPlainCell(ExtendedAttribute attribute, String value) {
        JLabel label = new JLabel(value);
        if (attribute.getAttributeType() == ExtendedAttribute.USER_ATTRIBUTE) {
            label.setForeground(Color.BLUE);
        }
        return label;
    }

}
