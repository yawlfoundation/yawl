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

package org.yawlfoundation.yawl.editor.ui.properties.editor;

/**
 * @author Michael Adams
 * @date 30/06/2014
 */
public class PositiveIntegerPropertyEditor extends NonNegativeIntegerPropertyEditor {

    protected boolean validate(String value) {
        if (value == null || value.isEmpty()) return true;
        if (value.equals("0")) return error();
        for (char c : value.toCharArray()) {
            if (! Character.isDigit(c)) return error();
        }
        return true;
    }


    private boolean error() {
        currentValue = currentValue.newInstance();
        currentValue.setError("Invalid value, expecting positive integer");
        return false;
    }
}
