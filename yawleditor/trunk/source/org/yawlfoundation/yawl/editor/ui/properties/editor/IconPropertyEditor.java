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

package org.yawlfoundation.yawl.editor.ui.properties.editor;

import org.yawlfoundation.yawl.editor.ui.util.IconList;

import javax.swing.*;

/**
 * @author Michael Adams
 * @date 24/07/12
 */
public class IconPropertyEditor extends ComboPropertyEditor {

    private final IconList _list = IconList.getInstance();
    private static final String INTERNAL_PATH =
            "org/yawlfoundation/yawl/editor/ui/resources/taskicons";

    public IconPropertyEditor() {
        super();
        setAvailableValues(_list.getShortNames().toArray());
        setAvailableIcons(_list.getDropDownIcons().toArray(new Icon[_list.getSize()]));
    }


    public Object getValue() {
        int selectedIndex = ((JComboBox)editor).getSelectedIndex();
        if (selectedIndex == 0) return null;     // "None"
        String path = _list.getName(selectedIndex);
        if (path.contains(INTERNAL_PATH)) {
            path = path.substring(path.lastIndexOf('/')+1);
        }
        return path;
    }


    public void setValue(Object value) {
        if (value == null) return;

        String selection = (String) value;
        if (selection.equals("None")) ((JComboBox) editor).setSelectedIndex(0);
        for (int i=0; i < _list.getSize(); i++) {
            if (_list.getName(i).endsWith(selection)) {
                ((JComboBox) editor).setSelectedIndex(i);
                break;
            }
        }
    }

}
