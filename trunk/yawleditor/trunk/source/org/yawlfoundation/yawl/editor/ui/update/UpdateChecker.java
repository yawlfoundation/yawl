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

import org.yawlfoundation.yawl.editor.ui.util.VersionProperties;
import org.yawlfoundation.yawl.util.HttpURLValidator;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.YBuildProperties;

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

    private YBuildProperties current, latest;
    private String error;

    private static final String VERSION_PROPERTIES = "version.properties";
    protected static final String BASE_URL = "http://yawlfoundation.org/";
    protected static final String SOURCE_URL = BASE_URL + "latest/three/";

    public UpdateChecker() { }


    public Void doInBackground() {
        try {
            checkURLAvailable(BASE_URL);
            current = loadCurrentVersion();
            latest = loadLatestVersion();
        }
        catch (IOException ioe) {
            error = "Error: " + ioe.getMessage();
        }
        return null;
    }

    public YBuildProperties getCurrent() { return current; }

    public YBuildProperties getLatest() { return latest; }

    public String getError() { return error; }


    public int getCurrentBuildNumber() {
        return current != null ? getBuildNumber(current) : -1;
    }

    public int getLatestBuildNumber() {
        return latest != null ? getBuildNumber(latest) : -1;
    }

    public String getCurrentVersion() {
        return current != null ? current.getVersion() : null;
    }

    public String getLatestVersion() {
        return latest != null ? latest.getVersion() : null;
    }

    public String getCurrentBuildDate() {
        return current != null ? current.getBuildDate() : null;
    }

    public String getLatestBuildDate() {
        return latest != null ? latest.getBuildDate() : null;
    }

    public boolean hasUpdate() {
        int latestBuild = getLatestBuildNumber();
        return latestBuild > 0 && latestBuild != getCurrentBuildNumber();
    }

    public boolean hasError() {
        return error != null || getCurrentBuildNumber() <= 0 ||
                getLatestBuildNumber() <= 0;
    }


    public String getErrorMessage() {
        if (error != null) return error;
        if (getCurrentBuildNumber() <= 0) {
            return "Error: Unable to determine current version";
        }
        if (getLatestBuildNumber() <= 0) {
            return "Error: Unable to determine latest version";
        }
        return null;
    }


    private int getBuildNumber(YBuildProperties properties) {
        return StringUtil.strToInt(properties.getBuildNumber(), -1);
    }

    private YBuildProperties loadCurrentVersion() throws IOException {
        YBuildProperties current = new VersionProperties().load();
        if (current == null) {
            throw new IOException("Unable to determine current build version");
        }
        return current;
    }


    private YBuildProperties loadLatestVersion() throws IOException {
        File f = new File(getTmpDir(), VERSION_PROPERTIES);
        download(SOURCE_URL + VERSION_PROPERTIES, f);
        YBuildProperties latest = new VersionProperties().load(f);
        if (latest == null) {
            throw new IOException("Unable to determine latest build version");
        }
        return latest;
    }

    private void download(String fromURL, File toFile) throws IOException {
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

}
