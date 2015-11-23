package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
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

    protected Differ _differ;
    protected String _error;

    protected static final String BASE_URL = "http://sourceforge.net/";
    public static final String CHECKSUM_FILE = "checksums.xml";
    public static final String SOURCE_URL = BASE_URL +
            "projects/yawl/files/updatecache/engine/";
    public static final String SF_DOWNLOAD_SUFFIX = "/download";

    public UpdateChecker() { super(); }


    public Void doInBackground() {
        check();
        return null;
    }


    public void check() {
        try {
            checkURLAvailable(BASE_URL);
            _differ = new Differ(loadLatestCheckSums(), loadCurrentCheckSums());
        }
        catch (IOException ioe) {
            _error = "Error: " + ioe.getMessage();
        }
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


    public void download(String fromURL, File toFile) throws IOException {
        URL webFile = new URL(fromURL);
        ReadableByteChannel rbc = Channels.newChannel(webFile.openStream());
        FileOutputStream fos = new FileOutputStream(toFile);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    }


    protected File loadCurrentCheckSums() throws IOException {
        File current = FileUtil.getLocalCheckSumFile();
        if (! current.exists()) {
            throw new IOException("Unable to determine current build version");
        }
        return current;
    }

    protected File loadLatestCheckSums() throws IOException {
        File latest = new File(getTmpDir(), CHECKSUM_FILE);
        download(SOURCE_URL + "lib/" + CHECKSUM_FILE + SF_DOWNLOAD_SUFFIX, latest);
        if (! latest.exists()) {
            throw new IOException("Unable to determine latest build version");
        }
        return latest;
    }


    protected void checkURLAvailable(String urlStr) throws IOException {
        if (! TomcatUtil.isResponsive(new URL(urlStr))) {
            throw new IOException("Update server is offline or unavailable");
        }
    }


    private File getTmpDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }


    private String getCurrentVersion() {
        return _differ != null ? _differ.getCurrentVersion() : null;
    }

    private String getLatestVersion() {
        return _differ != null ? _differ.getLatestVersion() : null;
    }

}

