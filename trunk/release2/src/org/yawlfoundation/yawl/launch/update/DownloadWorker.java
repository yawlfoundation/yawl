package org.yawlfoundation.yawl.launch.update;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

/**
 * @author Michael Adams
 * @date 19/08/2014
 */
public class DownloadWorker extends SwingWorker<Void, Void> {

    private String _urlBase;
    private String _urlSuffix;
    private String _fileName;
    private long _totalBytes;
    private File _tmpDir;
    private String _errorMsg;


    public DownloadWorker(String urlBase, String urlSuffix, String fileName,
                          long totalBytes, File tmpDir) {
        _urlBase = urlBase;
        _urlSuffix = urlSuffix;
        _fileName = fileName;
        _totalBytes = totalBytes;
        _tmpDir = tmpDir;
    }


    protected boolean hasErrors() { return _errorMsg != null; }

    protected String getError() { return _errorMsg; }

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
            _errorMsg = e.getMessage();
        }

        return null;
    }


    private void makeDir(String fileName) {
        if (fileName.contains("/")) {
            new File(_tmpDir, fileName.substring(0, fileName.indexOf('/'))).mkdirs();
        }
    }

}
