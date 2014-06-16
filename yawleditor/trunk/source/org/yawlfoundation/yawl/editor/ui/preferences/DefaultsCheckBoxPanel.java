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

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 19/05/2014
 */
public abstract class DefaultsCheckBoxPanel extends JPanel implements PreferencePanel {

    private final ActionListener _listener;
    private final JCheckBox _checkBox;

    public DefaultsCheckBoxPanel(String label, int mnemonicKey, boolean selected,
                                 ActionListener listener) {
        super(new BorderLayout());
        setBorder(new EmptyBorder(0,5,0,0));
        _listener = listener;
        _checkBox = createCheckBox(label, mnemonicKey, selected);
        add(_checkBox, BorderLayout.WEST);
    }


    public abstract void applyChanges();

    protected boolean isSelected() { return _checkBox.isSelected(); }


    private JCheckBox createCheckBox(String label, int mnemonicKey, boolean selected) {
        JCheckBox checkBox = new JCheckBox(label);
        checkBox.setMnemonic(mnemonicKey);
        checkBox.setSelected(selected);
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
