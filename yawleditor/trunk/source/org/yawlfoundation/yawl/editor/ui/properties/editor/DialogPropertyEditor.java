package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.swing.ComponentFactory;
import com.l2fprod.common.swing.PercentLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public abstract class DialogPropertyEditor extends AbstractPropertyEditor {

    protected JComponent label;

    public DialogPropertyEditor(JComponent renderer) {
        label = renderer;
        label.setOpaque(false);

        JButton button = ComponentFactory.Helper.getFactory().createMiniButton();
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDialog();
            }
        });

        JPanel cell = new JPanel(new PercentLayout(PercentLayout.HORIZONTAL, 0));
        cell.add("*", label);
        cell.add(button);
        cell.setOpaque(false);

        editor = cell;
    }


    protected abstract void showDialog();

}

