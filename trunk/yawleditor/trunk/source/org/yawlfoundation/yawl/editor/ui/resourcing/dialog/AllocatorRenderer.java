package org.yawlfoundation.yawl.editor.ui.resourcing.dialog;

import org.yawlfoundation.yawl.resourcing.AbstractSelector;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;

/**
 * @author Michael Adams
 * @date 24/06/13
 */
public class AllocatorRenderer extends BasicComboBoxRenderer {

    JLabel _cell = new JLabel();

    @Override
    public Component getListCellRendererComponent(
            JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value != null) {
            _cell.setText(((AbstractSelector) value).getDisplayName());
        }
        return _cell;
    }
}
