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

package org.yawlfoundation.yawl.editor.ui.swing;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.yawlfoundation.yawl.analyser.YAnalyserEvent;
import org.yawlfoundation.yawl.analyser.YAnalyserEventListener;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.specification.validation.AnalysisCanceller;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class AnalysisDialog extends JDialog implements YAnalyserEventListener {
    private JPanel contentPane;
    private JButton btnClose;
    private JTextPane txtOutput;
    private JCheckBox cbxKeepOpen;
    private JLabel lblHeader;
    private JScrollPane scrollpane;
    private JButton btnCancel;
    private boolean cancelled;
    private AnalysisCanceller _owner;
    private final String header;

    public AnalysisDialog(String title, JFrame owner) {
        super(owner);
        setContentPane(contentPane);
        header = "Analysing " + title + ": ";
        lblHeader.setText(header);
        cancelled = false;
        setModal(false);
        getRootPane().setDefaultButton(btnClose);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setKeepOpenCheckbox();
        setMinimumSize(new Dimension(385, 160));
        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClose();
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                lblHeader.setText(header + "Stop Pending...");
                _owner.cancel();
                cancelled = true;
            }
        });

        pack();
        position();
        setVisible(true);
    }

    public void refresh() {
        cancelled = false;
        setText("");
    }

    public void write(String msg) {
        setText(txtOutput.getText() + msg + '\n');
    }

    public void setText(String msg) {
        txtOutput.setText(msg);
        txtOutput.setCaretPosition(msg.length());
    }

    public String getText() {
        return txtOutput.getText();
    }

    public void setOwner(AnalysisCanceller owner) {
        _owner = owner;
    }

    public boolean isCancelled() {
        return cancelled;
    }


    public void finished() {
        lblHeader.setText(header + (cancelled ? "Cancelled." : " Completed."));
        if (!cbxKeepOpen.isSelected())
            onClose();
        else {
            btnCancel.setEnabled(false);
            btnClose.setEnabled(true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }
    }

    public void yAnalyserEvent(final YAnalyserEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                switch (event.getEventType()) {
                  case Init : refresh() ; break;
                  case Message : write(event.getMessage()); break;
                  case Cancelled : if (! cancelled) cancelled = true; else break;
                  case Completed : finished(); break;
              }
            }
        });
    }


    private void setKeepOpenCheckbox() {
        cbxKeepOpen.setSelected(UserSettings.getKeepAnalysisDialogOpen());
    }

    private void setKeepOpenPreference() {
        UserSettings.setKeepAnalysisDialogOpen(cbxKeepOpen.isSelected());
    }

    // put the dialog in bottom right of editor frame
    private void position() {
        Point parentLocation = YAWLEditor.getInstance().getLocationOnScreen();
        Dimension parentSize = YAWLEditor.getInstance().getSize();
        Dimension dialogSize = this.getSize();
        int x = parentLocation.x + parentSize.width - dialogSize.width;
        int y = parentLocation.y + parentSize.height - dialogSize.height;
        setLocation(x, y);
    }


    private void onClose() {
        setKeepOpenPreference();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(false);            // using dispose here will cause threading issues
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 10, 10, 10), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        cbxKeepOpen = new JCheckBox();
        cbxKeepOpen.setSelected(true);
        cbxKeepOpen.setText("Keep open when analysis completes        ");
        cbxKeepOpen.setMnemonic('K');
        cbxKeepOpen.setDisplayedMnemonicIndex(0);
        panel1.add(cbxKeepOpen, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnClose = new JButton();
        btnClose.setEnabled(false);
        btnClose.setText("Close");
        panel2.add(btnClose, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnCancel = new JButton();
        btnCancel.setText("Stop");
        panel2.add(btnCancel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 0, 10), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(500, 200), null, 0, false));
        lblHeader = new JLabel();
        lblHeader.setText("");
        panel3.add(lblHeader, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(151, 29), null, 0, false));
        scrollpane = new JScrollPane();
        panel3.add(scrollpane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(500, 200), null, 0, false));
        txtOutput = new JTextPane();
        txtOutput.setEditable(false);
        txtOutput.setEnabled(true);
        txtOutput.setText("");
        scrollpane.setViewportView(txtOutput);
        lblHeader.setLabelFor(scrollpane);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
