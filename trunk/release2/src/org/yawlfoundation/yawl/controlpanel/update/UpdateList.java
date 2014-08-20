package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.util.XNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 12/08/2014
 */
public class UpdateList {

    private final List<FileNode> _downloads;
    private final List<FileNode> _deletes;
    private final String _appName;

    protected UpdateList(String appName) {
        _downloads = new ArrayList<FileNode>();
        _deletes = new ArrayList<FileNode>();
        _appName = appName;
    }

    protected UpdateList() {
        this(null);
    }


    protected void addDownload(FileNode node) { add(_downloads, node); }

    protected void addDownload(XNode xNode) { add(_downloads, xNode); }


    protected void addDeletion(FileNode node) { add(_deletes, node); }

    protected void addDeletion(XNode xNode) { add(_deletes, xNode); }


    protected String getAppName() { return _appName; }

    protected boolean isAppList() { return _appName != null; }


    protected boolean hasDownloads() { return ! _downloads.isEmpty(); }

    protected boolean hasDeletions() { return ! _deletes.isEmpty(); }

    protected boolean isEmpty() { return ! (hasDownloads() || hasDeletions()); }


    protected List<String> getDownloadNames() {
        return getNames(_downloads);
    }


    protected List<String> getDeletionNames() {
        return getNames(_deletes);
    }


    protected long getTotalUpdateSize() {
        long size = 0;
        for (FileNode node : _downloads) {
             size += node.getSize();
        }
        return size;
    }


    protected Map<String, String> getMd5Map() {
        Map<String, String> map = new HashMap<String, String>();
        String prefix = getPathPrefix();
        for (FileNode node : _downloads) {
             map.put(prefix + node.getName(), node.getMd5());
        }
        return map;
    }

    private void add(List<FileNode> list, FileNode node) { list.add(node); }

    private void add(List<FileNode> list, XNode node) { list.add(new FileNode(node)); }

    private List<String> getNames(List<FileNode> list) {
        List<String> names = new ArrayList<String>();
        String prefix = getPathPrefix();
        for (FileNode node : list) {
             names.add(prefix + node.getName());
        }
        return names;
    }

    private String getPathPrefix() {
        return isAppList() ? "webapps/" + _appName  + "/" : "lib/";
    }

}
