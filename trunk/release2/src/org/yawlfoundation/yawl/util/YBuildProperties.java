/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * Author: Michael Adams
 * Creation Date: 16/04/2010
 */
public class YBuildProperties {

    Properties _buildProps;


    public YBuildProperties() { }


    public void load(InputStream inputStream) {
        try {
            _buildProps = new Properties();
            _buildProps.load(inputStream);
        }
        catch (Exception e) {
            _buildProps = null;
        }
    }


    public String getBuildNumber() {
        return _buildProps.getProperty("BuildNumber");
    }

    public String getVersion() {
        return _buildProps.getProperty("Version");
    }

    public String getBuildDate() {
        return _buildProps.getProperty("BuildDate");
    }

    public String getFullVersion() {
        return getVersion() + " (b." + getBuildNumber() + ")";
    }


    public String toXML() {
        XNode root = new XNode("buildproperties");
        for (Object o : _buildProps.keySet()) {
            String key = (String) o;
            String value = _buildProps.getProperty(key);
            root.addChild(key, value);
        }
        return root.toString();
    }


}
