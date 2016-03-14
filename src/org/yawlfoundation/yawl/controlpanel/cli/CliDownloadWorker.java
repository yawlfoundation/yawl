package org.yawlfoundation.yawl.controlpanel.cli;

import org.yawlfoundation.yawl.controlpanel.update.DownloadWorker;
import org.yawlfoundation.yawl.controlpanel.update.FileNode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

/**
 * @author Michael Adams
 * @author Joerg Evermann
 * @date 3/11/2015
 */
public class CliDownloadWorker extends DownloadWorker {

    private int progressPercent;
    private boolean cancelled;
    private Thread runner;


    public CliDownloadWorker(FileNode fileNode, long totalBytes, File tmpDir) {
        super(fileNode, totalBytes, tmpDir);
        progressPercent = 0;
        cancelled = false;
    }


    public int getProgressValue() { return progressPercent; }


    public void start() {
        runner = new Thread(new Runnable() {
            @Override
            public void run() {
                int progress = 0;
                int bufferSize = 8192;
                byte[] buffer = new byte[bufferSize];
                try {
                    URL webFile = _fileNode.getAbsoluteURL();
                    makeDir(_fileNode.getName());
                    String fileTo = _tmpDir + File.separator + _fileNode.getName();
                    BufferedInputStream inStream = new BufferedInputStream(webFile.openStream());
                    FileOutputStream fos = new FileOutputStream(fileTo);
                    BufferedOutputStream outStream = new BufferedOutputStream(fos);

                    // read chunks from the input stream and write them out
                    int bytesRead;
                    while ((bytesRead = inStream.read(buffer, 0, bufferSize)) > 0) {
                        outStream.write(buffer, 0, bytesRead);
                        progress += bytesRead;
                        progressPercent = Math.min((int) (progress * 100 / _totalBytes), 100);
                        if (cancelled) {
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
            }
        });
        runner.start();
    }


    public boolean isAlive() { return runner != null && runner.isAlive(); }

    public void setCancelled() { cancelled = true; }

}
