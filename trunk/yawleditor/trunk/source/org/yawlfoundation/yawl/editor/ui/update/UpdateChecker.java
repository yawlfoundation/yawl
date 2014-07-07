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

import org.yawlfoundation.yawl.editor.ui.util.FileLocations;
import org.yawlfoundation.yawl.util.HttpURLValidator;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
* @author Michael Adams
* @date 23/05/2014
*/
public class UpdateChecker extends SwingWorker<Void, Void> {

    private VersionComparer _comparer;
    private String _error;

    protected static final String CHECKSUM_FILE = "checksums.xml";
    protected static final String BASE_URL = "http://sourceforge.net/";
    protected static final String SOURCE_URL =
            BASE_URL + "projects/yawl/files/updatecache/editor/";
    protected static final String SF_DOWNLOAD_SUFFIX = "/download";

    public UpdateChecker() { }


    public Void doInBackground() {
        try {
            checkURLAvailable(BASE_URL);
            _comparer = new VersionComparer(loadLatestCheckSums(), loadCurrentCheckSums());
        }
        catch (IOException ioe) {
            _error = "Error: " + ioe.getMessage();
        }
        return null;
    }


    public VersionComparer getComparer() { return _comparer; }

    public String getError() { return _error; }


    public String getCurrentBuildNumber() {
        return _comparer != null ? _comparer.getCurrentBuild() : null;
    }

    public String getLatestBuildNumber() {
        return _comparer != null ? _comparer.getLatestBuild() : null;
    }


    public boolean hasUpdate() {
        return _comparer != null && _comparer.hasUpdates();
    }

    public boolean hasError() {
        return _error != null || getCurrentBuildNumber() == null ||
                getLatestBuildNumber() == null;
    }


    public String getErrorMessage() {
        if (_error != null) return _error;
        if (getCurrentBuildNumber() == null) {
            return "Error: Unable to determine current version";
        }
        if (getLatestBuildNumber() == null) {
            return "Error: Unable to determine latest version";
        }
        return null;
    }


    private String loadCurrentCheckSums() throws IOException {
        String current = StringUtil.fileToString(FileLocations.getLibPath() +
                File.separator + CHECKSUM_FILE);
        if (current == null) {
            throw new IOException("Unable to determine current build version");
        }
        return current;
    }

    private String loadLatestCheckSums() throws IOException {
        File f = new File(getTmpDir(), CHECKSUM_FILE);
        download(SOURCE_URL + "lib/" + CHECKSUM_FILE + SF_DOWNLOAD_SUFFIX, f);
        String latest = StringUtil.fileToString(f);
        if (latest == null) {
            throw new IOException("Unable to determine latest build version");
        }
        return latest;
    }


    public void download(String fromURL, File toFile) throws IOException {
        URL webFile = new URL(fromURL);
        ReadableByteChannel rbc = Channels.newChannel(webFile.openStream());
        FileOutputStream fos = new FileOutputStream(toFile);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    }


    private String getTmpDir() {
        return System.getProperty("java.io.tmpdir") + File.separator;
    }


    private void checkURLAvailable(String urlStr) throws IOException {
        if (! HttpURLValidator.validate(urlStr).equals("<success/>")) {
            throw new IOException("Update server is offline or unavailable");
        }
    }


    public static void main(String args[]) {
        UpdateChecker checker = new UpdateChecker();
        try {
            checker.download(
                    "http://sourceforge.net/projects/yawl/files/updatecache/editor/lib/checksums.xml/download",
                    new File("/Users/adamsmj/Documents/temp/checkSums.xml"));
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
