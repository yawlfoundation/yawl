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

import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;

public abstract class AbstractDoneDialog extends JDialog {

    private enum ButtonType { Done, Cancel }

    private JButton _doneButton = buildDoneButton();
    private JButton _cancelButton = buildCancelButton();
    private JPanel _buttonPanel;
    private boolean _showCancelButton;
    private ButtonType buttonSelected = ButtonType.Cancel;
    protected boolean closeCancelled = false;


    /**
     * ***********************************************************
     */

    public AbstractDoneDialog(String title,
                              boolean modality,
                              JPanel contentPanel,
                              boolean showCancelButton) {
        super();
        setTitle(title);
        setModal(modality);
        _showCancelButton = showCancelButton;
        setContentPanel(bindContentAndButton(contentPanel));
        setUndecorated(false);
    }

    public AbstractDoneDialog(String title, boolean modality, boolean showCancelButton) {
        super();
        setTitle(title);
        setModal(modality);
        _showCancelButton = showCancelButton;
    }

    public AbstractDoneDialog(String title, boolean modality) {
        super();
        setTitle(title);
        setModal(modality);
        _showCancelButton = true;
    }

    public AbstractDoneDialog() {
        super();
    }


    /**
     * ***********************************************************
     */

    public void setContentPanel(JPanel contentPanel) {
        getContentPane().add(bindContentAndButton(contentPanel), BorderLayout.CENTER);
        makeLastAdjustments();
    }


    public JButton getDoneButton() {
        return _doneButton;
    }


    public JButton getCancelButton() {
        return _cancelButton;
    }


    public boolean cancelButtonSelected() {
        return buttonSelected == ButtonType.Cancel;
    }


    /**
     * ***********************************************************
     */

    protected JPanel getButtonPanel() {
        return _buttonPanel;
    }


    protected void makeLastAdjustments() {} // override as necessary


    /**
     * ***********************************************************
     */

    private JPanel bindContentAndButton(JPanel contentPanel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(buildButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }


    private JButton buildDoneButton() {
        JButton button = new JButton("Done");
        button.setMnemonic(KeyEvent.VK_D);
        button.setMargin(new Insets(2, 11, 3, 12));

        final JDialog dialog = this;
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!closeCancelled) dialog.setVisible(false);
                SpecificationUndoManager.getInstance().setDirty(true);
                buttonSelected = ButtonType.Done;
                closeCancelled = false;            // reset for next time
            }
        });

        if (!_showCancelButton) {
            button.setDefaultCapable(true);
        }
        return button;
    }


    private JButton buildCancelButton() {
        JButton button = new JButton("Cancel");
        button.setMnemonic(KeyEvent.VK_C);
        button.setMargin(new Insets(2, 11, 3, 12));

        final JDialog dialog = this;
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                buttonSelected = ButtonType.Cancel;
            }
        });

        if (_showCancelButton) {
            button.setDefaultCapable(true);
        }
        return button;
    }


    private JPanel buildButtonPanel() {
        _buttonPanel = new JPanel();
        _buttonPanel.setBorder(new EmptyBorder(17, 12, 11, 11));
        _buttonPanel.setLayout(new BoxLayout(_buttonPanel, BoxLayout.LINE_AXIS));
        _buttonPanel.add(Box.createHorizontalGlue());

        if (_showCancelButton) {
             _buttonPanel.add(_cancelButton);
            _buttonPanel.add(Box.createHorizontalStrut(10));

            LinkedList<JButton> buttonList = new LinkedList<JButton>();
            buttonList.add(_doneButton);
            buttonList.add(_cancelButton);
            JUtilities.equalizeComponentSizes(buttonList);
        }
        _buttonPanel.add(_doneButton);
        _buttonPanel.add(Box.createHorizontalGlue());

        return _buttonPanel;
    }

}
