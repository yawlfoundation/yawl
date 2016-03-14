package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.net.MalformedURLException;
import java.net.URL;

/**
* @author Michael Adams
* @date 12/08/2014
*/
public class FileNode {

    private String name;
    private String md5;
    private long size;
    private String urlStr;

    public FileNode(XNode node, String url) {
        name = node.getAttributeValue("name");
        md5 = node.getAttributeValue("md5");
        size = StringUtil.strToInt(node.getAttributeValue("size"), 0);
        urlStr = url;
    }


    public String getName() { return name; }

    public long getSize() { return size; }

    public String getMd5() { return md5; }


    public boolean matches(FileNode other) { return md5.equals(other.md5); }

    public String toString() { return name; }


    public URL getAbsoluteURL() {
        try {
            String simpleName = name.contains("/") ?
                    name.substring(name.lastIndexOf('/') + 1) : name;
            return new URL(urlStr + simpleName);
        }
        catch (MalformedURLException mue) {
            return null;
        }
    }

}
