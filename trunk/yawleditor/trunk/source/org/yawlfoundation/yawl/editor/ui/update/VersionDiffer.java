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

package org.yawlfoundation.yawl.editor.ui.update;

import org.yawlfoundation.yawl.editor.ui.util.BuildProperties;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 7/07/2014
 */
public class VersionDiffer {

    private BuildProperties _latest;
    private BuildProperties _current;
    private List<FileNode> _downloadList;
    private List<FileNode> _deleteList;
    private boolean _newVersion;


    public VersionDiffer(File latest, File current) {
        _latest = new BuildProperties(latest);
        _current = new BuildProperties(current);
        _downloadList = new ArrayList<FileNode>();
        _deleteList = new ArrayList<FileNode>();
        execute();
    }

    public String getLatestVersion() { return _latest.getVersion(); }

    public String getLatestBuild() { return _latest.getBuild(); }

    public String getLatestTimestamp() { return _latest.getTimestamp(); }

    public String getCurrentVersion() { return _current.getVersion(); }

    public String getCurrentBuild() { return _current.getBuild(); }

    public String getCurrentTimestamp() { return _current.getTimestamp(); }


    public boolean hasUpdates() {
        return ! (_downloadList.isEmpty() && _deleteList.isEmpty());
    }


    public boolean isNewVersion() { return _newVersion; }


    public List<String> getDownloadList() {
        List<String> list = new ArrayList<String>();
        if (isNewVersion()) {
            list.add("YAWLEditor" + getLatestVersion() + ".zip");
        }
        else {
            for (FileNode node : _downloadList) {
                list.add(node.name);
            }
        }
        return list;
    }


    public Map<String, String> getDownloadMD5Map() {
        Map<String, String> map = new HashMap<String, String>();
        for (FileNode node : _downloadList) {
            map.put(node.name, node.md5);
        }
        return map;
    }


    public List<String> getDeleteList() {
        List<String> list = new ArrayList<String>();
        for (FileNode node : _deleteList) {
            list.add(node.name);
        }
        return list;
    }


    public long getDownloadSize() {
        if (isNewVersion()) {
            return 1024*1024*12;             // 12Mb.
        }
        long size = 0;
        for (FileNode node : _downloadList) {
            size += node.size;
        }
        return size;
    }


    public String getCurrentInfo() {
        return getInfo("Current", getCurrentVersion(), getCurrentBuild());
    }


    public String getLatestInfo() {
        return getInfo("New", getLatestVersion(), getLatestBuild());
    }


    private String getInfo(String label, String version, String build) {
        StringBuilder s = new StringBuilder();
        s.append(label).append(" Version: ").append(version)
                .append(" (build ").append(build).append(')');
        return s.toString();
    }


    private void execute() {
        if (_latest == null || _current == null || (getLatestBuild() != null &&
                getLatestBuild().equals(getCurrentBuild()))) {
            return ;
        }
        _newVersion = ! getLatestVersion().equals(getCurrentVersion());

        if (! _newVersion) {
            Map<String, FileNode> latestMap = getFileMap(_latest.getFileList());
            Map<String, FileNode> currentMap = getFileMap(_current.getFileList());
            compareMaps(latestMap, currentMap);
        }
    }


    private Map<String, FileNode> getFileMap(List<XNode> fileList) {
        Map<String, FileNode> fileMap = new HashMap<String, FileNode>();
        for (XNode child : fileList) {
            FileNode fileNode = new FileNode(child);
            fileMap.put(fileNode.name, fileNode);
        }
        return fileMap;
    }


    private void compareMaps(Map<String, FileNode> latestMap,
                                     Map<String, FileNode> currentMap) {


        for (String fileName : latestMap.keySet()) {
            FileNode node = latestMap.get(fileName);

            // if latest in current, compare md5's - if same, don't add download list
            if (currentMap.containsKey(fileName) && node.matches(currentMap.get(fileName))) {
                continue;
            }

            // latest entry is not in current, or different md5's, so add to download list
            _downloadList.add(node);

        }

        // if current entry not in latest, add to delete list
        for (String fileName : currentMap.keySet()) {
            if (! latestMap.containsKey(fileName)) {
                _deleteList.add(currentMap.get(fileName));
            }
        }

    }

    /******************************************************************************/

    class FileNode {
        String name;
        String md5;
        int size;

        FileNode(XNode node) {
            name = node.getAttributeValue("name");
            md5 = node.getAttributeValue("md5");
            size = StringUtil.strToInt(node.getAttributeValue("size"), 0);
        }

        boolean matches(FileNode other) { return md5.equals(other.md5); }
    }

}
