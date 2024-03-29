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

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.File;
import java.util.*;

/**
 * Read methods for checksums.xml
 * @author Michael Adams
 * @date 12/08/2014
 */
public class ChecksumsReader {

    // this is a list of unsupported apps in version 5 (or later). A quick fix. Future
    // work will include removing these from checksums for a more thorough cleanup
    private final List<String> _ignoredApps =
            List.of("procletService", "twitterService", "yawlSMSInvoker");

    private XNode _root;

    public ChecksumsReader(File f) {
        load(f);
    }


    public String getVersion() { return getValue(_root, "version"); }

    public XNode getNode(String name) {
        return _root != null ? _root.getChild(name) : null;
    }

    protected String getTimestamp() { return getValue(_root, "timestamp"); }


    protected XNode getLibNode() { return getNode("lib"); }


    protected XNode getWebAppsNode() { return getNode("webapps"); }


    protected XNode getAppNode(String appName) {
        XNode webappsNode = getWebAppsNode();
        return webappsNode != null ? webappsNode.getChild(appName) : null;
    }


    public XNode getControlPanelNode() { return getNode("controlpanel"); }


    public XNode getControlPanelFileNode() {
        XNode node = getControlPanelNode();
        return node != null ? node.getChild("file") : null;
    }

    protected List<XNode> getAppFileList(String appName) {
        return getAppList(appName, "files");
    }

    protected List<XNode> getAppLibList(String appName) {
        return getAppList(appName, "lib");
    }

    protected String getLibHash() {
        XNode libNode = getLibNode();
        return libNode != null ? libNode.getAttributeValue("hash") : null;
    }


    protected List<XNode> getLibList() { return getChildren(getLibNode()); }

    protected Map<String, FileNode> getLibMap(PathResolver pathResolver) {
        Map<String, FileNode> map = new HashMap<String, FileNode>();
        for (XNode node : getLibList()) {
            map.put(node.getAttributeValue("name"),
                    new FileNode(node, pathResolver.get(node.getAttributeValue("path"))));
        }
        return map;
    }

    protected List<XNode> getWebappsList() { return getChildren(getWebAppsNode()); }

    protected List<String> getWebAppNames() {
        List<String> names = new ArrayList<String>();
        for (XNode appNode : getWebappsList()) {
            String name = appNode.getName();
            if (! isIgnoredApp(name)) {
                names.add(appNode.getName());
            }
        }
        return names;
    }


    protected List<XNode> getRequiredLibs(List<String> appNames) {
        Set<String> requiredLibNames = getRequiredLibNames(appNames);
        List<XNode> requiredLibs = new ArrayList<XNode>();
        for (XNode libNode : getLibList()) {
            if (requiredLibNames.contains(libNode.getAttributeValue("name"))) {
                requiredLibs.add(libNode);
            }
        }
        return requiredLibs;
    }


    protected String getBuildNumber(String appName) {
        XNode appNode = appName.equals("controlpanel") ? getControlPanelNode() :
                getAppNode(appName);
        return appNode != null ? appNode.getChildText("build") : null;
    }


    protected XNode getYawlLibNode() {
        if (_root != null) {
            XNode yawlNode = _root.getChild("yawllib");
            if (yawlNode != null) {
                return yawlNode.getChild();
            }
        }
        return null;
    }


    protected String getYawlLibHash() {
        XNode node = getYawlLibNode();
        return node != null ? node.getAttributeValue("md5") : null;
    }


    protected String getControlPanelHash() {
        XNode node = getControlPanelFileNode();
        return node != null ? node.getAttributeValue("md5") : null;
    }


    private List<XNode> getAppList(String appName, String childName) {
        XNode appNode = getAppNode(appName);
        return appNode != null ? getChildren(appNode.getChild(childName)) :
                 Collections.<XNode>emptyList();
    }


    private Set<String> getRequiredLibNames(List<String> appNames) {
        Set<String> libNames = new HashSet<String>();
        for (String appName : appNames) {
            for (XNode libNode : getAppLibList(appName)) {
                libNames.add(libNode.getAttributeValue("name"));
            }
        }
        return libNames;
    }


    private void load(File checkSumsFile) {
        if (checkSumsFile.exists()) {
            _root = new XNodeParser().parse(StringUtil.fileToString(checkSumsFile));
        }
    }


    private String getValue(XNode node, String key) {
        return node != null ? node.getChildText(key) : null;
    }


    private List<XNode> getChildren(XNode node) {
        return node != null ? node.getChildren() : Collections.<XNode>emptyList();
    }


    private boolean isIgnoredApp(String appName) {
        return _ignoredApps.contains(appName);
    }

}
