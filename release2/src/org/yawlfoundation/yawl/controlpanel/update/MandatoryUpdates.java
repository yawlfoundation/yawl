package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.controlpanel.util.TomcatUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michael Adams
 * @date 18/09/2014
 */
public class MandatoryUpdates {

    private static final File LIB_DIR = new File(TomcatUtil.getCatalinaHome(), "yawllib");


    public AppUpdate get() {
        AppUpdate updates = new AppUpdate(null);
        for (FileNode fileNode : getFiles()) {
            if (! new File(LIB_DIR, fileNode.getName()).exists()) {
                updates.addDownload(fileNode);
            }
        }
        return updates;
    }


    private List<FileNode> getFiles() {
        List<FileNode> nodeList = new ArrayList<FileNode>();
        XNode root = loadFileXML();
        if (root != null) {
            XNode libNode = root.getChild("lib");
            if (libNode != null) {
                for (XNode node : libNode.getChildren()) {
                    nodeList.add(new FileNode(node));
                }
            }
        }
        return nodeList;
    }


    private XNode loadFileXML() {
        InputStream is = this.getClass().getResourceAsStream("mandatory.xml");
        String content = StringUtil.streamToString(is);
        return content != null ? new XNodeParser().parse(content) : null;
    }

}
