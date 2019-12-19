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

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.net.MalformedURLException;
import java.net.URL;

/**
* @author Michael Adams
* @date 12/08/2014
*/
public class FileNode {

    private String urlFilePath;
    private String diskFilePath;
    private String md5;
    private long size;
    private String urlBase;

    public FileNode(XNode node, String url) {
        urlFilePath = node.getAttributeValue("name");
        md5 = node.getAttributeValue("md5");
        size = StringUtil.strToInt(node.getAttributeValue("size"), 0);
        urlBase = url;
    }


    public String getURLFilePath() { return urlFilePath; }

    public long getSize() { return size; }

    public String getMd5() { return md5; }


    // disk path differs from url path on win os
    public void setDiskFilePath(String n) { diskFilePath = n; }

    public String getDiskFilePath() { return diskFilePath; }


    public boolean matches(FileNode other) { return md5.equals(other.md5); }

    public String toString() { return urlFilePath; }


    public URL getAbsoluteURL() {
        try {
            String simpleName = urlFilePath.contains("/") ?
                    urlFilePath.substring(urlFilePath.lastIndexOf('/') + 1) : urlFilePath;
            return new URL(urlBase + simpleName);
        }
        catch (MalformedURLException mue) {
            return null;
        }
    }

}
