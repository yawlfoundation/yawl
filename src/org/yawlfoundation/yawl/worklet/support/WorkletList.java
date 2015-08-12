package org.yawlfoundation.yawl.worklet.support;

import org.yawlfoundation.yawl.util.StringUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;

/**
 * @author Michael Adams
 * @date 6/03/15
 */
public class WorkletList {

    private Vector<String> _list;

    public static final boolean WITH_EXTN = true;
    public static final boolean WITHOUT_EXTN = false;


    public Vector<String> getAll() { return getAll(WITH_EXTN); }

    public Vector<String> getAll(boolean extn) {
        if (_list == null) {
            populateList(extn);
        }
        return _list;
    }


    private void populateList(boolean extn) {
        _list = new Vector<String>();
        File wDir = new File(Library.wsWorkletsDir);
        String [] fileNames = wDir.list(new FilenameFilter() {
            public boolean accept(File file, String s) {
                String lcs = s.toLowerCase();
                return lcs.endsWith(".yawl") || lcs.endsWith(".xml");
            }
        });
        if (fileNames != null) {
            for (String f : fileNames) {
                if (! extn) f = snipExtn(f);
                if (f != null) _list.add(f);
            }
        }
    }


    private String snipExtn(String f) {
        if (f == null) return null;
        int dot = f.lastIndexOf('.');
        return dot == -1 ? f : f.substring(0, dot);
    }
}
