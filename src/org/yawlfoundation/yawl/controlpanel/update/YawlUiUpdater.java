package org.yawlfoundation.yawl.controlpanel.update;

import org.apache.commons.io.FileUtils;
import org.yawlfoundation.yawl.controlpanel.YControlPanel;
import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.controlpanel.util.TomcatUtil;
import org.yawlfoundation.yawl.util.HttpUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
    private final String YAWL_LIB_PATH = TomcatUtil.getCatalinaHome() +
            "/yawllib/";
    private final String YAWL_LIB_JAR_NAME = "yawl-lib-" + YControlPanel.VERSION + ".jar";
    private final String UPDATE_JAR_NAME = "yawlui-update.jar";

    public YawlUiUpdater() { }


    public boolean hasUpdate() {
        int local = StringUtil.strToInt(getLocalBuildNumber(), -1);
        int remote = StringUtil.strToInt(getRemoteBuildNumber(), -1) + checkForUpdate();
        return local != -1 && remote != -1 && local < remote;
    }

    
    public String getLocalBuildNumber() {
        return getLocalBuildNumber(LOCAL_PATH + "classes/build.properties");
    }


    public String getRemoteBuildNumber() {
        return getRemoteBuildNumber(REMOTE_PATH + "pom.xml");
    }


    public AppUpdate getAppUpdate() {
        AppUpdate appUpdate = new AppUpdate("yawlui");
        appUpdate.addDownload(createUpdateFileNode());
        appUpdate.addDownload(createLibUpdateFileNode());
        appUpdate.addDownload(createIconUpdateFileNode());
        return appUpdate;
    }


    private FileNode createUpdateFileNode() {
        XNode node = new XNode("node");
        node.addAttribute("name", UPDATE_JAR_NAME);
        node.addAttribute("md5", "0");
        node.addAttribute("size", "300000");
        return new UIFileNode(node, REMOTE_PATH + "update/");
    }

    private FileNode createLibUpdateFileNode() {
        XNode node = new XNode("node");
        node.addAttribute("name", "yawlui-lib-update.jar");
        node.addAttribute("md5", "0");
        node.addAttribute("size", "504000");
        return new UIFileNode(node, REMOTE_PATH + "update/");
    }

    private FileNode createIconUpdateFileNode() {
        XNode node = new XNode("node");
        node.addAttribute("name", "yawlui-icon-update.jar");
        node.addAttribute("md5", "0");
        node.addAttribute("size", "9000");
        return new UIFileNode(node, REMOTE_PATH + "update/");
    }
    

    private String getLocalBuildNumber(String path) {
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


    private String getRemoteBuildNumber(String path) {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File latest = new File(tmpDir, "pom.xml");
        try {
            URL url = new URL(path);
            HttpUtil.download(url, latest);
            String content = StringUtil.fileToString(latest);

            // extract the latest build number from pom
            String version = StringUtil.extract(content,
                    "(?<=<version>\\d+\\.\\d\\.)[\\d|].*(?=<\\/version>)");
            return version != null ? version : "N/A";
        }
        catch (Exception e) {
            return "N/A";
        }
    }

    private int checkForUpdate() {
        File check = new File(getLibDir(), "vcf-leaflet-1.0.1.jar");
        return check.exists() ? 0 : 1;
    }


    private String getClassesDir() {
        return LOCAL_PATH + "classes/";
    }


    private String getLibDir() {
        return LOCAL_PATH + "lib/";
    }


    class UIFileNode extends FileNode {

        public UIFileNode(XNode node, String url) {
            super(node, url);
        }

        public void doUpdate(File tmpDir) {
            File updateJar = FileUtil.makeFile(tmpDir.getAbsolutePath(), UPDATE_JAR_NAME);
            File updateLibJar = FileUtil.makeFile(tmpDir.getAbsolutePath(), "yawlui-lib-update.jar");
            File updateIconJar = FileUtil.makeFile(tmpDir.getAbsolutePath(), "yawlui-icon-update.jar");
            File classesDir = new File(getClassesDir());
            File libDir = new File(getLibDir());
            File backupDir = new File(tmpDir, "ui-classes");
            File iconDir =  new File(TomcatUtil.getCatalinaHome()
                        + "/webapps/yawlui/icons/");
            if (updateJar.exists()) {
                try {

                    // backup existing files to tmp
                    FileUtils.moveDirectory(classesDir, backupDir);

                    // unpack jar to classes dir
                    FileUtil.unzip(updateJar, classesDir);

                    FileUtil.unzip(updateLibJar, libDir);
                    FileUtil.unzip(updateIconJar, iconDir);

                    // copy existing application.properties from tmp to classes
                    File oldProps = new File(backupDir, "application.properties");
                    File newProps = new File(classesDir, "application.properties");
                    File merged = mergeProps(oldProps, newProps);
                    FileUtils.copyFileToDirectory(merged, classesDir);

                    // copy in the newest yawl library
                    File yawlLibJar = new File(YAWL_LIB_PATH, YAWL_LIB_JAR_NAME);
                    FileUtils.copyFileToDirectory(yawlLibJar, libDir);

                    // success: delete the tmp dir
                    FileUtils.deleteDirectory(backupDir);
                }
                catch (Exception e) {

                    // rollback changes
                    try {
                        if (backupDir.exists()) {
                            FileUtils.deleteDirectory(classesDir);
                            File classesBackupDir = new File(backupDir, "classes");
                            FileUtils.moveDirectory(classesBackupDir, classesDir);
                        }
                    }
                    catch (IOException ex) {
                        //
                    }
                }
            }
        }

        private File mergeProps(File oldProps, File newProps) {
            Path oldPath = oldProps.toPath();
            Path newPath = newProps.toPath();

            try {
                List<String> lines1 = Files.readAllLines(oldPath);
                List<String> lines2 = Files.readAllLines(newPath);
                if (lines1.size() == lines2.size()) {
                    return oldProps;
                }

                for (int i = lines1.size(); i < lines2.size(); ++i) {
                    lines1.add(lines2.get(i));
                }
                Files.write(oldPath, lines1);
            }
            catch (IOException e) {
                // fall through
            }
            return oldPath.toFile();
        }
    }
    
}
