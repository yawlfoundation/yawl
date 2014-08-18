package org.yawlfoundation.yawl.launch.update;

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.File;
import java.util.*;

/**
 * @author Michael Adams
 * @date 12/08/2014
 */
public class ChecksumsReader {

    private XNode _root;

    protected ChecksumsReader(File f) {
        load(f);
    }


    protected String getVersion() { return getValue(_root, "version"); }

    protected String getTimestamp() { return getValue(_root, "timestamp"); }


    protected XNode getLibNode() {
        return _root != null ? _root.getChild("lib") : null;
    }

    protected XNode getWebAppsNode() {
        return _root != null ? _root.getChild("webapps") : null;
    }

    protected XNode getAppNode(String appName) {
        XNode webappsNode = getWebAppsNode();
        return webappsNode != null ? webappsNode.getChild(appName) : null;
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

    protected List<XNode> getWebappsList() { return getChildren(getWebAppsNode()); }

    protected List<String> getWebAppNames() {
        List<String> names = new ArrayList<String>();
        for (XNode appNode : getWebappsList()) {
            names.add(appNode.getName());
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
        XNode appNode = getAppNode(appName);
        return appNode != null ? appNode.getChildText("build") : null;
    }


    protected XNode getYawlLibNode() {
        return _root != null ? _root.getChild("yawllib") : null;
    }


    protected String getYawlLibHash() {
        XNode node = getYawlLibNode();
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

}
