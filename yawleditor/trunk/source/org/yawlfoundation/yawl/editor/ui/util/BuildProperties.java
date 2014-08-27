/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.util;

import org.yawlfoundation.yawl.editor.ui.update.UpdateChecker;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Adams
 * @date 23/05/2014
 */
public class BuildProperties {

    XNode _node;

    // default constructor - uses checksums.xml file in lib dir of this installation
    public BuildProperties() {
        load(new File(FileLocations.getLibPath(), UpdateChecker.CHECKSUM_FILE));
    }

    public BuildProperties(String fileName) { load(new File(fileName)); }

    public BuildProperties(File f) { load(f); }


    public String getVersion() { return getValue("version"); }

    public String getBuild() { return getValue("build"); }

    public String getTimestamp() { return getValue("timestamp"); }

    public String getFullVersionText() {
        return getVersion() + " (build " + getBuild() + ")";
    }

    public List<XNode> getFileList() {
        return _node != null ? _node.getChild("files").getChildren() :
                Collections.<XNode>emptyList();
    }

    public String getEditorJarName() {
        for (XNode node : getFileList()) {
            String name = node.getAttributeValue("name");
            if (name.startsWith("YAWLEditor")) {
                return name;
            }
        }
        return null;
    }


    private void load(File checkSumsFile) {
        if (checkSumsFile.exists()) {
            _node = new XNodeParser().parse(StringUtil.fileToString(checkSumsFile));
        }
    }


    private String getValue(String key) {
        return _node != null ? _node.getChildText(key) : null;
    }

}
