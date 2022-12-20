package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.controlpanel.util.TomcatUtil;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author Michael Adams
 * @date 19/12/2022
 */
public class YawlUiUpdater {

    private final String LOCAL_PATH = TomcatUtil.getCatalinaHome()
            + "/webapps/yawlui/WEB-INF/";
    private final String REMOTE_PATH =
            "https://raw.githubusercontent.com/yawlfoundation/yawlui/master/";

    private String _errorMsg;


    public YawlUiUpdater() {

    }

    public String getLocalBuildNumber() {
        return getBuildNumber(LOCAL_PATH + "classes/build.properties");
    }


    public String getRemoteBuildNumber() {
        return getBuildNumber(REMOTE_PATH + "update/update.properties");
    }


    
        // get current build
    // get latest build
    // compare

    // download + build.properties


    private String getBuildNumber(String path) {
        try {
            InputStream is = Files.newInputStream(Paths.get(path));
            Properties props = new Properties();
            props.load(is);
            return props.getProperty("ui.service.build", "N/A");

        }
        catch (IOException e) {
            return "N/A";
        }
    }


    private String getClassesDir() {
        return LOCAL_PATH + "classes/";
    }


    private String getLibDir() {
        return LOCAL_PATH + "lib/";
    }


    class Downloader extends SwingWorker<Void, Void> {

        private final URL _url;
        private final File _destDir;
        private final String _path;

        Downloader(URL url, File destDir, String path) {
            _url = url;
            _destDir = destDir;
            _path = path;
        }

        @Override
        protected Void doInBackground() throws Exception {
            int bufferSize = 8192;
            byte[] buffer = new byte[bufferSize];
            int progress = 0;

            try {
                String fileTo = _destDir + File.separator + _path;
                BufferedInputStream inStream = new BufferedInputStream(_url.openStream());
                FileOutputStream fos = new FileOutputStream(fileTo);
                BufferedOutputStream outStream = new BufferedOutputStream(fos);
                int bytesRead;
                while ((bytesRead = inStream.read(buffer, 0, bufferSize)) > 0) {
                    outStream.write(buffer, 0, bytesRead);
                //    progress += bytesRead;
                //    setProgress(Math.min((int) (progress * 100 / _totalBytes), 100));
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
    }

    
}
