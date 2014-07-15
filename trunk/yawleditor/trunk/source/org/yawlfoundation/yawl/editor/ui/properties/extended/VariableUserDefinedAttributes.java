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

package org.yawlfoundation.yawl.editor.ui.properties.extended;

import org.yawlfoundation.yawl.editor.ui.util.FileLocations;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

/**
 * @author Michael Adams
 * @date 21/08/13
 */
public class VariableUserDefinedAttributes extends UserDefinedAttributes {

    private String _filePath;

    private static final VariableUserDefinedAttributes INSTANCE =
            new VariableUserDefinedAttributes();


    public static VariableUserDefinedAttributes getInstance() {
        return INSTANCE;
    }


    protected String getFilePath() {
        if (_filePath == null) {
            _filePath = UserSettings.getVariableAttributesFilePath();
            if (_filePath == null) {
                _filePath = FileLocations.getDefaultVariableAttributePath();
            }
        }
        return _filePath;
    }


}
