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

package org.yawlfoundation.yawl.editor.ui.util;

import org.yawlfoundation.yawl.util.YBuildProperties;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Michael Adams
 * @date 23/05/2014
 */
public class VersionProperties {

    public YBuildProperties load() {
        return load(getClass().getResourceAsStream("/version.properties"));
    }


    public YBuildProperties load(String fileName) throws IOException {
        return load(new FileInputStream(fileName));
    }


    public YBuildProperties load(InputStream is) {
        YBuildProperties buildProperties = null;
        if (is != null) {
            buildProperties = new YBuildProperties();
            buildProperties.load(is);
        }
        return buildProperties;
    }

}
