package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.controlpanel.util.TomcatUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.io.File;
import java.util.*;

/**
 * @author Michael Adams
 * @date 12/08/2014
 */
public class Differ {

    private ChecksumsReader _latest;
    private ChecksumsReader _current;

    public Differ(File latest, File current) {
        _latest = new ChecksumsReader(latest);
        _current = new ChecksumsReader(current);
    }

    public String getLatestVersion() { return _latest.getVersion(); }

    public String getCurrentVersion() { return _current.getVersion(); }

    public String getLatestTimestamp() { return _latest.getTimestamp(); }

    public String getCurrentTimestamp() { return _current.getTimestamp(); }

    public String getLatestBuild(String appName) {
        return _latest.getBuildNumber(appName);
    }

    public String getCurrentBuild(String appName) {
        return _current.getBuildNumber(appName);
    }


    public boolean isNewVersion() {
        return ! getLatestVersion().equals(getCurrentVersion());
    }


    public boolean hasUpdate(String appName) {
        return isDifferent(getCurrentBuild(appName), getLatestBuild(appName));
    }


    public boolean hasLibChange() {
        return isDifferent(_current.getLibHash(), _latest.getLibHash());
    }


    public boolean hasYawlLibChange() {
        return isDifferent(_current.getYawlLibHash(), _latest.getYawlLibHash());
    }

    public boolean hasControlPanelChange() {
        return isDifferent(_current.getControlPanelHash(), _latest.getControlPanelHash());
    }

    public String getNewJarName() {
        XNode node = _latest.getControlPanelFileNode();
        return node != null ? node.getAttributeValue("name") : null;
    }


    public boolean hasUpdates() {
        if (hasLibChange() || hasYawlLibChange() || hasControlPanelChange()) {
            return true;      // short circuit
        }
        for (String appName : getInstalledWebAppNames()) {
            if (hasUpdate(appName)) return true;
        }
        return false;
    }


    public List<AppUpdate> getUpdatesList() throws IllegalStateException {
        return diff();
    }

    public List<String> getWebAppNames() {
        return _latest.getWebAppNames();
    }

    public List<String> getInstalledWebAppNames() {
        List<String> installed = new ArrayList<String>();
        List<String> available = _current.getWebAppNames();
        File webAppsDir = new File(TomcatUtil.getCatalinaHome(), "webapps");
        for (File f : FileUtil.getDirList(webAppsDir)) {
            String name = f.getName();
            if (available.contains(name)) installed.add(name);
        }
        return installed;
    }


    public List<String> getInstalledLibNames() {
        List<String> installed = new ArrayList<String>();
        File libDir = new File(TomcatUtil.getCatalinaHome(), "yawllib");
        for (File f : FileUtil.getFileList(libDir)) {
            installed.add(f.getName());
        }
        return installed;

    }

    public AppUpdate getAppFiles(String appName, boolean adding) {
        AppUpdate upList = new AppUpdate(appName);
        for (XNode node : _latest.getAppFileList(appName)) {
            if (adding) upList.addDownload(node);
            else upList.addDeletion(node);
        }
        return upList;
    }


    // for new installs
    public AppUpdate getAppLibs(String appName) {
        List<String> installed = getInstalledLibNames();
        AppUpdate upList = new AppUpdate(null);
        Map<String, FileNode> libMap = _latest.getLibMap();
        for (XNode node : _latest.getAppLibList(appName)) {
            String name = node.getAttributeValue("name");
            if (! installed.contains(name)) {
                FileNode fileNode = libMap.get(name);
                if (fileNode != null) upList.addDownload(fileNode);
            }
        }
        return upList;
    }

    public Set<String> getRequiredLibNames() {
        Set<String> libNames = new HashSet<String>();
        for (XNode node : _latest.getRequiredLibs(getInstalledWebAppNames())) {
            libNames.add(node.getAttributeValue("name"));
        }

        // checksums.xml,the yawl lib jar & props files are also needed in the lib dir
        libNames.add(UpdateChecker.CHECKSUM_FILE);
        libNames.add(_latest.getYawlLibNode().getAttributeValue("name"));
        libNames.add("log4j.properties");
        libNames.add("hibernate.properties");
        return libNames;
    }


    private List<AppUpdate> diff() throws IllegalStateException {
        if (_latest == null || _current == null) return Collections.emptyList();
        List<AppUpdate> updates = new ArrayList<AppUpdate>();
        compareYAWLLib(updates);
        compareWebApps(updates);
        compareControlPanel(updates);
        return updates;
    }


    private void compareYAWLLib(List<AppUpdate> updates) throws IllegalStateException {
        AppUpdate appUpdate = compareFileNodes(_current.getYawlLibNode(),
                _latest.getYawlLibNode(), null);
        if (appUpdate != null) updates.add(appUpdate);
    }


    private void compareControlPanel(List<AppUpdate> updates) throws IllegalStateException {
        AppUpdate appUpdate = compareFileNodes(_current.getControlPanelFileNode(),
                _latest.getControlPanelFileNode(), "controlpanel");
        if (appUpdate != null) updates.add(appUpdate);
    }


    private AppUpdate compareFileNodes(XNode currentNode, XNode latestNode, String appName) {
        AppUpdate appUpdate = null;
        if (! (currentNode == null || latestNode == null)) {
            String currentMd5 = currentNode.getAttributeValue("md5");
            String latestMd5 = latestNode.getAttributeValue("md5");
            if (! (currentMd5 == null || latestMd5 == null || currentMd5.equals(latestMd5))) {
                appUpdate = new AppUpdate(appName);
                appUpdate.addDownload(latestNode);
            }
        }
        return appUpdate;
    }


    private void compareWebApps(List<AppUpdate> updates) throws IllegalStateException {
        List<String> installedWebAppNames = getInstalledWebAppNames();
        for (String appName : installedWebAppNames) {
            if (hasUpdate(appName)) {
                AppUpdate appUpdate = compareFileLists(
                        _latest.getAppFileList(appName),
                        _current.getAppFileList(appName), appName);
                if (! appUpdate.isEmpty()) updates.add(appUpdate);
            }
        }
        if (hasLibChange()) {
            AppUpdate appUpdate = compareFileLists(
                    _latest.getRequiredLibs(installedWebAppNames),
                    _current.getRequiredLibs(installedWebAppNames), null);
            if (! appUpdate.isEmpty()) updates.add(appUpdate);
        }
    }


    private AppUpdate compareFileLists(List<XNode> latestList, List<XNode> currentList,
                                  String appName) throws IllegalStateException {
        checkNotNullOrEmpty(latestList);
        checkNotNullOrEmpty(currentList);
        Map<String, FileNode> latestMap = getFileMap(latestList);
        Map<String, FileNode> currentMap = getFileMap(currentList);
        return compareMaps(latestMap, currentMap, appName);
    }


    private Map<String, FileNode> getFileMap(List<XNode> fileList) {
        Map<String, FileNode> fileMap = new HashMap<String, FileNode>();
        for (XNode child : fileList) {
            FileNode fileNode = new FileNode(child);
            fileMap.put(fileNode.getName(), fileNode);
        }
        return fileMap;
    }


    private AppUpdate compareMaps(Map<String, FileNode> latestMap,
                             Map<String, FileNode> currentMap,
                             String appName) {

        AppUpdate appUpdate = new AppUpdate(appName);
        for (String fileName : latestMap.keySet()) {
            FileNode node = latestMap.get(fileName);

            // if latest in current, compare md5's - if same, don't add download list
            if (currentMap.containsKey(fileName) && node.matches(currentMap.get(fileName))) {
                continue;
            }

            // latest entry is not in current, or different md5's, so add to download list
            appUpdate.addDownload(node);
        }

        // if current entry not in latest, add to delete list
        for (String fileName : currentMap.keySet()) {
            if (! latestMap.containsKey(fileName)) {
                appUpdate.addDeletion(currentMap.get(fileName));
            }
        }
        return appUpdate;
    }


    private boolean isDifferent(String one, String two) {
        return ! (one == null || two == null || one.equals(two));
    }


    private void checkNotNullOrEmpty(List<?> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalStateException("Unable to compare versions.");
        }
    }

}
