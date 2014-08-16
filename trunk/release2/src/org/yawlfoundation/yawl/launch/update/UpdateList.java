package org.yawlfoundation.yawl.launch.update;

import org.yawlfoundation.yawl.util.XNode;

import java.util.ArrayList;
import java.util.List;

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


    protected void addUpdate(FileNode node) { add(_downloads, node); }

    protected void addUpdate(XNode xNode) { add(_downloads, xNode); }


    protected void addDeletion(FileNode node) { add(_deletes, node); }

    protected void addDeletion(XNode xNode) { add(_deletes, xNode); }


    protected String getAppName() { return _appName; }

    protected boolean isAppList() { return _appName != null; }


    protected boolean hasUpdates() { return ! _downloads.isEmpty(); }

    protected boolean hasDeletions() { return ! _deletes.isEmpty(); }

    protected boolean isEmpty() { return ! (hasUpdates() || hasDeletions()); }


    protected List<String> getUpdateNames() {
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


    private void add(List<FileNode> list, FileNode node) { list.add(node); }

    private void add(List<FileNode> list, XNode node) { list.add(new FileNode(node)); }

    private List<String> getNames(List<FileNode> list) {
        List<String> names = new ArrayList<String>();
        for (FileNode node : list) {
             names.add(node.getName());
        }
        return names;
    }

}
