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

    protected void addDownload(XNode xNode) { add(_downloads, xNode); }


    protected void addDeletion(FileNode node) { add(_deletes, node); }

    protected void addDeletion(XNode xNode) { add(_deletes, xNode); }


    protected String getAppName() { return _appName; }

    protected boolean isAppList() { return _appName != null; }

    protected boolean isControlPanelApp() {
        return isAppList() && _appName.equals("controlpanel");
    }


    protected boolean hasDownloads() { return ! _downloads.isEmpty(); }

    protected boolean hasDeletions() { return ! _deletes.isEmpty(); }

    protected boolean isEmpty() { return ! (hasDownloads() || hasDeletions()); }


    protected List<String> getDownloadNames() { return getNames(_downloads); }

    protected List<String> getDeletionNames() { return getNames(_deletes); }


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
             map.put(fixPath(node.getName()), node.getMd5());
        }
        return map;
    }


    protected void merge(AppUpdate other) {
        _downloads.addAll(other._downloads);
        _deletes.addAll(other._deletes);
    }


    private void add(Set<FileNode> list, FileNode node) { list.add(node); }

    private void add(Set<FileNode> list, XNode node) { list.add(new FileNode(node)); }


    private List<String> getNames(Set<FileNode> list) {
        List<String> names = new ArrayList<String>();
        for (FileNode node : list) {
             names.add(fixPath(node.getName()));
        }
        return names;
    }


    private String fixPath(String path) {
        char sep = FileUtil.SEP;
        if (sep == '\\') path = path.replace('/', sep);
        return (isAppList() ? _appName.equals("controlpanel") ? "controlpanel" :
                "webapps" + sep + _appName : "lib") + sep + path;
    }


    public String toString() {
        return (isAppList() ? _appName : "lib") + " " + _downloads.size() + "/" +
                _deletes.size();
    }

}
