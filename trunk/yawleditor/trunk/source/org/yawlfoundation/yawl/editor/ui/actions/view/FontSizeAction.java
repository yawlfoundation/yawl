/*
 * Created on 9/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.ui.actions.view;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractDoneDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FontSizeAction extends YAWLBaseAction {

    private static final FontSizeDialog dialog = new FontSizeDialog();
    private boolean isFirstInvocation = false;

    {
        putValue(Action.SHORT_DESCRIPTION, " Set the element label font size. ");
        putValue(Action.NAME, "Label Font Size...");
        putValue(Action.LONG_DESCRIPTION, "Set the element label font size.");
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_S));
    }

    public FontSizeAction() { }

    public void actionPerformed(ActionEvent event) {
        if (!isFirstInvocation) {
            dialog.setLocationRelativeTo(YAWLEditor.getInstance());
            isFirstInvocation = true;
        }
        dialog.setVisible(true);
    }
}

class FontSizeDialog extends AbstractDoneDialog {

    protected JSpinner labelFontSizeSpinner;

    public FontSizeDialog() {
        super("Set Element Label Font Size", true);

        setContentPanel(getFontSizePanel());
        getDoneButton().addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (labelFontSizeSpinner.isEnabled()) {
                            setFontSize(getFontSize(),
                                    (Integer) labelFontSizeSpinner.getModel().getValue()
                            );
                        }
                    }
                }
        );
    }

    protected void makeLastAdjustments() {
        pack();
        setResizable(false);
    }

    public void setVisible(boolean state) {
        if (state) setSpinnerValue();
        super.setVisible(state);
    }

    private JPanel getFontSizePanel() {

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel panel = new JPanel(gbl);
        panel.setBorder(new EmptyBorder(12,12,0,11));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0,0,0,5);
        gbc.anchor = GridBagConstraints.EAST;

        JLabel label = new JLabel("Label Font Size:");
        label.setDisplayedMnemonicIndex(0);
        panel.add(label, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;

        labelFontSizeSpinner = getLabelFontSizeSpinner();

        label.setLabelFor(labelFontSizeSpinner);

        panel.add(labelFontSizeSpinner, gbc);

        return panel;
    }

    private JSpinner getLabelFontSizeSpinner() {
        return new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
    }

    private void setSpinnerValue() {
        labelFontSizeSpinner.setEnabled(false);
        labelFontSizeSpinner.getModel().setValue(getFontSize());
        labelFontSizeSpinner.setEnabled(true);
        pack();
    }

    private int getFontSize() {
        return SpecificationModel.getInstance().getFontSize();
    }

    private void setFontSize(int oldSize, int newSize) {
        SpecificationModel.getInstance().setFontSize(oldSize, newSize);
    }
}

