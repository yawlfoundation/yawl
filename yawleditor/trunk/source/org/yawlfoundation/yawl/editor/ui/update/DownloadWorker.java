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

package org.yawlfoundation.yawl.editor.ui.update;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

/**
 * @author Michael Adams
 * @date 27/05/2014
 */
public class DownloadWorker extends SwingWorker<Void, Void> {

    private String _urlBase;
    private String _urlSuffix;
    private String _fileName;
    private long _totalBytes;
    private File _tmpDir;
    private boolean _hasErrors;


    public DownloadWorker(String urlBase, String urlSuffix, String fileName,
                          long totalBytes, File tmpDir) {
        _urlBase = urlBase;
        _urlSuffix = urlSuffix;
        _fileName = fileName;
        _totalBytes = totalBytes;
        _tmpDir = tmpDir;
    }


    protected boolean hasErrors() { return _hasErrors; }

    @Override
    protected Void doInBackground() {
        int bufferSize = 8192;
        byte[] buffer = new byte[bufferSize];
        int progress = 0;
        try {
            URL webFile = new URL(_urlBase + _fileName + _urlSuffix);
            makeDir(_fileName);
            String fileTo = _tmpDir + File.separator + _fileName;
            BufferedInputStream inStream = new BufferedInputStream(webFile.openStream());
            FileOutputStream fos = new FileOutputStream(fileTo);
            BufferedOutputStream outStream = new BufferedOutputStream(fos);

            // read chunks from the input stream and write them out
            int bytesRead;
            while ((bytesRead = inStream.read(buffer, 0, bufferSize)) > 0) {
                outStream.write(buffer, 0, bytesRead);
                progress += bytesRead;
                setProgress(Math.min((int) (progress * 100 / _totalBytes), 100));
                if (isCancelled()) {
                    outStream.close();
                    inStream.close();
                    new File(fileTo).delete();
                    break;
                }
            }

            outStream.close();
            inStream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            _hasErrors = true;
        }

        return null;
    }


    private void makeDir(String fileName) {
        if (fileName.contains("/")) {
            new File(_tmpDir, fileName.substring(0, fileName.indexOf('/'))).mkdirs();
        }
    }

}
