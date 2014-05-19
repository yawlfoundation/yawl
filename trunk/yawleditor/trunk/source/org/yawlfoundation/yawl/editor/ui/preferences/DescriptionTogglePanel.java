/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.preferences;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * @author Michael Adams
 * @date 19/05/2014
 */
public class DescriptionTogglePanel extends JPanel implements PreferencePanel {

    private final ActionListener _listener;
    private final JCheckBox _checkBox;

    public DescriptionTogglePanel(ActionListener listener) {
        super();
        _listener = listener;
        _checkBox = createCheckBox();
        add(_checkBox);
    }


    public void applyChanges() {
        YAWLEditor.getPropertySheet().setDescriptionVisible(_checkBox.isSelected());
        YAWLEditor.getPropertySheet().refresh();
        UserSettings.setShowPropertyDescriptions(_checkBox.isSelected());
    }


    private JCheckBox createCheckBox() {
        JCheckBox checkBox = new JCheckBox("Show Description panel in Properties pane");
        checkBox.setBorder(new EmptyBorder(5, 0, 5, 0));
        checkBox.setMnemonic(KeyEvent.VK_D);
        checkBox.setSelected(UserSettings.getShowPropertyDescriptions());
        checkBox.setAlignmentX(LEFT_ALIGNMENT);
        checkBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                announceChange();
            }
        });
        return checkBox;
    }


    private void announceChange() {
        _listener.actionPerformed(
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
    }

}
