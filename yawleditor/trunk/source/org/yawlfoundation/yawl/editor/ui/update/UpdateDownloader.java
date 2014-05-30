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
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Michael Adams
 * @date 27/05/2014
 */
public class UpdateDownloader extends SwingWorker<Void, Void> {

    private String _url;
    private File _file;
    private long _fileSize;

    private static final long DEFAULT_FILE_SIZE = 1024 * 1024 * 11;       // 11mb


    public UpdateDownloader(String urlStr, File downloadTo) {
        _url = urlStr;
        _file = downloadTo;
    }

    protected File getFileTo() { return _file; }

    @Override
    protected Void doInBackground() throws Exception {
        URL webFile = new URL(_url);
        _fileSize = getFileSize(webFile);
        int bufferSize = (int) (_fileSize / 1000);
        byte[] buffer = new byte[bufferSize];
        BufferedInputStream inStream = new BufferedInputStream(webFile.openStream());
        FileOutputStream fos = new FileOutputStream(_file);
        BufferedOutputStream outStream = new BufferedOutputStream(fos);
        long progress = 0;
        setProgress(0);

        // read chunks from the input stream and write them out
        int bytesRead;
        while ((bytesRead = inStream.read(buffer, 0, bufferSize)) > 0) {
            outStream.write(buffer, 0, bytesRead);
            progress += bytesRead;
            setProgress(Math.min((int)(progress * 100/_fileSize), 100));
            if (isCancelled()) {
                outStream.close();
                inStream.close();
                _file.delete();
                break;
            }
        }

        outStream.close();
        inStream.close();
        return null;
    }


    public long getFileSize() {
        if (_fileSize == 0) {
            try {
                URL webFile = new URL(_url);
                _fileSize = getFileSize(webFile);
            }
            catch (MalformedURLException mue) {
                // ignore - fall through
            }
        }
        return _fileSize;
    }


    private long getFileSize(URL url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.getInputStream();
            return conn.getContentLength();
        }
        catch (IOException e) {
            return DEFAULT_FILE_SIZE;
        }
        finally {
            if (conn != null) conn.disconnect();
        }
    }

}
