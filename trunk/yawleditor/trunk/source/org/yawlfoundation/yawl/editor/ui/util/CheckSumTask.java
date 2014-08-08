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

import org.yawlfoundation.yawl.util.AbstractCheckSumTask;
import org.yawlfoundation.yawl.util.CheckSummer;

import java.io.File;
import java.io.IOException;

/**
 * @author Michael Adams
 * @date 5/07/2014
 */
public class CheckSumTask extends AbstractCheckSumTask {

    public String toXML(File checkDir, CheckSummer summer) throws IOException {
        StringBuilder s = new StringBuilder();
        s.append(XML_HEADER).append('\n');
        s.append(COMMENT).append('\n');
        s.append("<release>\n");
        s.append("\t<version>").append(getProjectProperty("version"))
                .append("</version>\n");
        s.append("\t<build>").append(getBuildNumber()).append("</build>\n");
        s.append("\t<timestamp>").append(now()).append("</timestamp>\n");
        s.append("\t<files>\n");
        for (File file : getFileList(checkDir)) {
            if (shouldBeIncluded(file)) {
                s.append("\t\t<file name=\"")
                 .append(getRelativePath(checkDir, file.getAbsolutePath()))
                 .append("\" md5=\"").append(summer.getMD5Hex(file))
                 .append("\" size=\"").append(file.length())
                 .append("\" timestamp=\"")
                 .append(formatTimestamp(file.lastModified()))
                 .append("\"/>\n");
            }
        }
        s.append("\t</files>\n");
        s.append("</release>");
        return s.toString();
    }

}
