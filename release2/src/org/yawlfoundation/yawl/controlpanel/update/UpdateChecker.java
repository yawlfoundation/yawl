package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.controlpanel.util.TomcatUtil;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * @author Michael Adams
 * @date 18/08/2014
 */
public class UpdateChecker extends SwingWorker<Void, Void> {

    private Differ _differ;
    private String _error;

    public static final String CHECKSUM_FILE = "checksums.xml";
    protected static final String BASE_URL = "http://sourceforge.net/";
    protected static final String SOURCE_URL =
            BASE_URL + "projects/yawl/files/updatecache/engine/";
    protected static final String SF_DOWNLOAD_SUFFIX = "/download";

    public UpdateChecker() { }


    public Void doInBackground() {
        try {
            checkURLAvailable(BASE_URL);
            _differ = new Differ(loadLatestCheckSums(), loadCurrentCheckSums());
        }
        catch (IOException ioe) {
            _error = "Error: " + ioe.getMessage();
        }
        return null;
    }


    public Differ getDiffer() { return _differ; }

    public String getError() { return _error; }


    public boolean isNewVersion() {
        return _differ != null && _differ.isNewVersion();
    }


    public boolean hasUpdates() {
        return _differ != null && _differ.hasUpdates();
    }


    public boolean hasError() {
        return _error != null || getCurrentVersion() == null ||
                getLatestVersion() == null;
    }


    public String getErrorMessage() {
        if (_error != null) return _error;
        if (getCurrentVersion() == null) {
            return "Error: Unable to determine current version";
        }
        if (getLatestVersion() == null) {
            return "Error: Unable to determine latest version";
        }
        return null;
    }


    private File loadCurrentCheckSums() throws IOException {
        File current = new File(TomcatUtil.getCatalinaHome() + "/yawllib", CHECKSUM_FILE);
        if (! current.exists()) {
            throw new IOException("Unable to determine current build version");
        }
        return current;
    }

    private File loadLatestCheckSums() throws IOException {
        File latest = new File(getTmpDir(), CHECKSUM_FILE);
        download(SOURCE_URL + "lib/" + CHECKSUM_FILE + SF_DOWNLOAD_SUFFIX, latest);
        if (! latest.exists()) {
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


    private File getTmpDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }


    private void checkURLAvailable(String urlStr) throws IOException {
        if (! TomcatUtil.isResponsive(new URL(urlStr))) {
            throw new IOException("Update server is offline or unavailable");
        }
    }


    private String getCurrentVersion() {
        return _differ != null ? _differ.getCurrentVersion() : null;
    }

    private String getLatestVersion() {
        return _differ != null ? _differ.getLatestVersion() : null;
    }

}

