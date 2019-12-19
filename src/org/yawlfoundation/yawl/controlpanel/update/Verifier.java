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

import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.util.CheckSummer;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 23/08/2014
 */
public class Verifier extends SwingWorker<Boolean, Void> {

    private Map<String, String> _md5Map;


    public Verifier(Map<String, String> md5Map) {
        _md5Map = md5Map;
    }


    @Override
    protected Boolean doInBackground() throws Exception {
        return verify();
    }


    public boolean verify() {
        if (_md5Map == null) return false;
        CheckSummer summer = new CheckSummer();
        File dir = FileUtil.getTmpDir();
        for (String key : _md5Map.keySet()) {
            String fileName = dir.getAbsolutePath() + File.separator + key;
            try {
                if (! summer.compare(fileName, _md5Map.get(key))) {
                    return false;
                }
            }
            catch (IOException ioe) {
                return false;
            }
        }
        return true;
    }

}
