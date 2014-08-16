package org.yawlfoundation.yawl.launch.update;

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

/**
* @author Michael Adams
* @date 12/08/2014
*/
class FileNode {

    private String name;
    private String md5;
    private int size;

    public FileNode(XNode node) {
        name = node.getAttributeValue("name");
        md5 = node.getAttributeValue("md5");
        size = StringUtil.strToInt(node.getAttributeValue("size"), 0);
    }


    public String getName() { return name; }

    public long getSize() { return size; }

    public String getMd5() { return md5; }


    public boolean matches(FileNode other) { return md5.equals(other.md5); }
}
