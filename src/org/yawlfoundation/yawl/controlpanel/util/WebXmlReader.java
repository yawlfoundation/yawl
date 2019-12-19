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

package org.yawlfoundation.yawl.controlpanel.util;

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.File;

/**
* @author Michael Adams
* @date 22/08/2014
*/
public class WebXmlReader {

    XNode _root;

    public WebXmlReader(String name) {
        File tomcatDir = new File(TomcatUtil.getCatalinaHome());
        File webXmlFile = new File(tomcatDir, "webapps/" + name + "/WEB-INF/web.xml");
        _root = new XNodeParser().parse(StringUtil.fileToString(webXmlFile));
    }


    public String getContextParam(String name) {
        if (_root != null) {
            for (XNode paramNode : _root.getChildren("context-param")) {
                String paramName = paramNode.getChildText("param-name");
                if (paramName != null && paramName.equals(name)) {
                    return paramNode.getChildText("param-value");
                }
            }
        }
        return null;
    }


    public String getIBServletMapping() {
        return getServletMapping(getInterfaceBServletName());
    }


    public String getServletMapping(String name) {
        if (! (_root == null || name == null)) {
            for (XNode mappingNode : _root.getChildren("servlet-mapping")) {
                String mappingName = mappingNode.getChildText("servlet-name");
                if (mappingName != null && mappingName.equals(name)) {
                    String urlPattern = mappingNode.getChildText("url-pattern");
                    return (urlPattern != null && urlPattern.equals("/*")) ? "/" :
                            urlPattern;
                }
            }
        }
        return null;
    }


    public String getInterfaceBServletName() {
        if (_root != null) {
            for (XNode servletNode : _root.getChildren("servlet")) {
                String servletClass = servletNode.getChildText("servlet-class");
                if (servletClass != null &&
                        servletClass.endsWith("InterfaceB_EnvironmentBasedServer")) {
                    return servletNode.getChildText("servlet-name");
                }
            }
        }
        return null;
    }

}
