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
import java.util.List;

/**
 * @author Michael Adams
 * @date 27/05/2014
 */
public class UpdateDownloader extends SwingWorker<Void, Void> {

    private String _urlBase;
    private String _urlSuffix;
    private List<String> _fileNames;
    private long _totalBytes;
    private File _tmpDir;
    private boolean _hasErrors;


    public UpdateDownloader(String urlBase, String urlSuffix, List<String> fileNames,
                            long totalBytes, File tmpDir) {
        _urlBase = urlBase;
        _urlSuffix = urlSuffix;
        _fileNames = fileNames;
        _totalBytes = totalBytes;
        _tmpDir = tmpDir;
    }

    protected File getTmpDir() { return _tmpDir; }

    protected boolean hasErrors() { return _hasErrors; }

    @Override
    protected Void doInBackground() {
        int bufferSize = 8192;
        long progress = 0;
        setProgress(0);
        for (String fileName : _fileNames) {
            try {
                URL webFile = new URL(_urlBase + fileName + _urlSuffix);
                byte[] buffer = new byte[bufferSize];
                makeDir(fileName);
                String fileTo = _tmpDir + File.separator + fileName;
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
                _hasErrors = true;
            }
        }
        return null;
    }


    private void makeDir(String fileName) {
        if (fileName.contains(File.separator)) {
            new File(_tmpDir, fileName.substring(0, fileName.indexOf(File.separator)))
                    .mkdirs();
        }
    }

}
