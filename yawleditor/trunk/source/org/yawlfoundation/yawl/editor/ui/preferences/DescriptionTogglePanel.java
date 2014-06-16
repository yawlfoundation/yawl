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

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * @author Michael Adams
 * @date 19/05/2014
 */
public class DescriptionTogglePanel extends DefaultsCheckBoxPanel implements PreferencePanel {


    public DescriptionTogglePanel(ActionListener listener) {
        super("Show Description panel in Properties pane", KeyEvent.VK_D,
                UserSettings.getShowPropertyDescriptions(), listener);
    }


    public void applyChanges() {
        YAWLEditor.getPropertySheet().setDescriptionVisible(isSelected());
        YAWLEditor.getPropertySheet().refresh();
        UserSettings.setShowPropertyDescriptions(isSelected());
    }

}
