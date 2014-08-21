package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.util.XNode;

import java.util.*;

/**
 * @author Michael Adams
 * @date 12/08/2014
 */
public class UpdateList {

    private final Set<FileNode> _downloads;
    private final Set<FileNode> _deletes;
    private final String _appName;                 // null for lib


    protected UpdateList(String appName) {
        _downloads = new HashSet<FileNode>();
        _deletes = new HashSet<FileNode>();
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


    protected void merge(UpdateList other) {
        _downloads.addAll(other._downloads);
        _deletes.addAll(other._deletes);
    }


    private void add(Set<FileNode> list, FileNode node) { list.add(node); }

    private void add(Set<FileNode> list, XNode node) { list.add(new FileNode(node)); }

    private List<String> getNames(Set<FileNode> list) {
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

    public String toString() {
        return (isAppList() ? _appName : "lib") + " " + _downloads.size() + "/" +
                _deletes.size();
    }

}
