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

import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.util.*;

/**
 * Manages the set of files to download (installs & updates) or delete (uninstalls) for
 * a particular webapp or set of library jars.
 * @author Michael Adams
 * @date 12/08/2014
 */
public class AppUpdate {

    private final Set<FileNode> _downloads;
    private final Set<FileNode> _deletes;
    private final String _appName;                 // null for lib


    protected AppUpdate(String appName) {
        _downloads = new HashSet<FileNode>();
        _deletes = new HashSet<FileNode>();
        _appName = appName;
    }


    protected void addDownload(FileNode node) { add(_downloads, node); }

    protected void addDownload(XNode xNode, String path) {
        add(_downloads, xNode, path);
    }


    protected void addDeletion(FileNode node) { add(_deletes, node); }

    protected void addDeletion(XNode xNode, String path) {
        add(_deletes, xNode, path);
    }


    protected String getAppName() { return _appName; }

    protected boolean isAppList() { return _appName != null; }

    protected boolean isControlPanelApp() {
        return isAppList() && _appName.equals("controlpanel");
    }

    protected boolean isServiceApp() {
        return isAppList() && ! _appName.equals("orderfulfillment");
    }

    protected boolean isUIApp() {
        return isAppList() && _appName.equals("yawlui");
    }

    protected boolean hasDownloads() { return ! _downloads.isEmpty(); }

    protected boolean hasDeletions() { return ! _deletes.isEmpty(); }

    protected boolean isEmpty() { return ! (hasDownloads() || hasDeletions()); }


    protected List<String> getDownloadNames() { return getNames(_downloads); }

    protected Set<FileNode> getDownloads() { return _downloads; }

    protected List<String> getDeletionNames() { return getNames(_deletes); }

    protected Set<FileNode> getDeletes() { return _deletes; }


    protected long getTotalUpdateSize() {
        long size = 0;
        for (FileNode node : _downloads) {
             size += node.getSize();
        }
        return size;
    }


    protected Map<String, String> getMd5Map() {
        Map<String, String> map = new HashMap<String, String>();
        for (FileNode node : _downloads) {
            map.put(node.getDiskFilePath(), node.getMd5());
        }
        return map;
    }


    protected void merge(AppUpdate other) {
        _downloads.addAll(other._downloads);
        _deletes.addAll(other._deletes);
    }


    private void add(Set<FileNode> list, XNode node, String path) {
        add(list, new FileNode(node, path));
    }


    private void add(Set<FileNode> list, FileNode node) {
        node.setDiskFilePath(fixPath(node.getURLFilePath()));
        list.add(node);
    }


    private List<String> getNames(Set<FileNode> list) {
        List<String> names = new ArrayList<String>();
        for (FileNode node : list) {
            names.add(node.getDiskFilePath());
        }
        return names;
    }


    // takes the path of the downloaded file and prefixes it with the appropriate
    // actual installed path of the file
    private String fixPath(String path) {
        char sep = FileUtil.SEP;
        if (sep == '\\') path = path.replace('/', sep);
        String prefix = "lib";
        if (isAppList()) {
            switch (_appName) {
                case "yawlui" : prefix = ""; break;
                case "controlpanel" : prefix = "controlpanel"; break;
                default : prefix = "webapps" + sep + _appName; break;
            }
        }
        return prefix + sep + path;
    }


    public String toString() {
        return (isAppList() ? _appName : "lib") + " " + _downloads.size() + "/" +
                _deletes.size();
    }

}
