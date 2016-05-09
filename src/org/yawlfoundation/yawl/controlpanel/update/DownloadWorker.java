package org.yawlfoundation.yawl.controlpanel.update;

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

    protected FileNode _fileNode;
    protected long _totalBytes;
    protected File _tmpDir;
    protected String _errorMsg;


    public DownloadWorker(FileNode fileNode, long totalBytes, File tmpDir) {
        _fileNode = fileNode;
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
            URL webFile = _fileNode.getAbsoluteURL();
            makeDir(_fileNode.getDiskFilePath());
            String fileTo = _tmpDir + File.separator + _fileNode.getDiskFilePath();
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
            _errorMsg = e.getMessage();
        }

        return null;
    }


    protected void makeDir(String name) {
        char sep = File.separatorChar;
        int pos = name.lastIndexOf(sep);
        if (pos > -1) {
            new File(_tmpDir.getAbsolutePath() + sep + name.substring(0, pos + 1)).mkdirs();
        }
    }

}
