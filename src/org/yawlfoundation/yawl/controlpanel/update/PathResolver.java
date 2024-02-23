/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.util.XNode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 11/03/2016
 */
public class PathResolver {
    String host;
    String base;
    String check;
    String suffix;
    Map<String, String> paths;

    PathResolver(XNode pathsNode) {
        paths = new HashMap<String, String>();
        if (pathsNode != null) {
            for (XNode pathNode : pathsNode.getChildren()) {
                String id = pathNode.getAttributeValue("id");
                String value = pathNode.getText();
                if ("host".equals(id)) {
                    host = value;
                }
                else if ("base".equals(id)) {
                    base = value;
                }
                else if ("check".equals(id)) {
                    check = value;
                }
                else if ("suffix".equals(id)) {
                    suffix = value;
                }
                else {
                    paths.put(id, value);
                }
            }
        }
    }


    String get(String id) {
        if (id == null) return "";
        if (id.equals("host")) return host;
        if (id.equals("base")) return base;
        if (id.equals("check")) return check;
        if (id.equals("suffix")) return suffix;
        String path = paths.get(id);
        return path != null ? host + base + path : "";
    }

}

