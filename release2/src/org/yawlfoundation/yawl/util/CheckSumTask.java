package org.yawlfoundation.yawl.util;

import java.io.File;
import java.io.IOException;

/**
 * @author Michael Adams
 * @date 5/07/2014
 */
public class CheckSumTask extends AbstractCheckSumTask {


    public String toXML(File baseDir, CheckSummer summer) throws IOException {
        File checksumsFile = getChecksumsFile(baseDir);
        XNode root = parse(checksumsFile);
        if (root == null) {
            throw new IOException("Error locating or parsing checksums file");
        }
        root.getChild("version").setText(getVersion());
        root.getChild("timestamp").setText(now());
        addLibs(root, baseDir, summer);
        addYAWLLib(root, baseDir, summer);
        addApp(root, baseDir, summer);
        return root.toPrettyString(true);
    }


    private void addLibs(XNode root, File baseDir, CheckSummer summer) {
        StringBuilder md5s = new StringBuilder();
        XNode libNode = root.getChild("lib");
        libNode.removeChildren();
        for (File file : getFileList(getLibDir(baseDir))) {
            if (shouldBeIncluded(file)) {
                md5s.append(addFile(libNode, file, summer, null));
            }
        }
        libNode.addAttribute("hash", getMD5Hex(md5s.toString(), summer));
    }


    private void addApp(XNode root, File baseDir, CheckSummer summer) {
        String appName = getAppName();
        XNode appNode = getAppNode(root.getChild("webapps"), appName);
        appNode.addChild("build", getBuildNumber(appName));
        addAppFiles(appNode, appName, baseDir, summer);
        addAppLibs(appNode, appName);
    }


    private XNode getAppNode(XNode webappsNode, String appName) {
        XNode appNode = webappsNode.getOrAddChild(appName);
        appNode.removeChildren();
        return appNode;
    }


     private void addAppFiles(XNode node, String appName, File baseDir,
                                 CheckSummer summer) {
         File appDir = getAppDir(baseDir, appName);
         XNode fileNode = node.addChild("files");
         for (File file : getFileList(appDir)) {
             if (shouldBeIncluded(file)) {
                 addFile(fileNode, file, summer, appDir);
             }
         }
     }


     private void addAppLibs(XNode node, String appName) {
         XNode libNode = node.addChild("lib");
         for (String member : getPropertyList("webapp_" + appName + ".libs")) {
             XNode fileNode = libNode.addChild("file");
             fileNode.addAttribute("name", member);
         }
     }


    private void addYAWLLib(XNode root, File baseDir, CheckSummer summer) {
        XNode yawlLibNode = root.getOrAddChild("yawllib");
        yawlLibNode.removeChildren();
        File outputDir = getOutputDir(baseDir);
        File yawlJar = new File(outputDir, "yawl-lib-" + getVersion() + ".jar");
        addFile(yawlLibNode, yawlJar, summer, null);
    }


    private String addFile(XNode node, File file, CheckSummer summer, File dir) {
        String fileName = dir == null ? file.getName() :
                getRelativePath(dir, file.getAbsolutePath());

        XNode fileNode = node.addChild("file");
        fileNode.addAttribute("name", fileName);
        String md5 = getMD5Hex(file, summer);
        fileNode.addAttribute("md5", md5);
        fileNode.addAttribute("size", file.length());
        fileNode.addAttribute("timestamp", formatTimestamp(file.lastModified()));
        return md5;
    }


    private String getMD5Hex(File file, CheckSummer summer) {
        try {
            return summer.getMD5Hex(file);
        }
        catch (IOException ioe) {
            return "";
        }
    }


    private String getMD5Hex(String s, CheckSummer summer) {
        try {
            return summer.getMD5Hex(s.getBytes());
        }
        catch (IOException ioe) {
            return "";
        }
    }


    private String[] getPropertyList(String group) {
        String members = getProjectProperty(group);
        return members != null ? members.split("\\s+") : new String[0];
    }


    private int getBuildNumber(String appName) {
        String prevBuild = getProjectProperty(appName + ".build.number");
        try {
            return Integer.parseInt(prevBuild) + 1;
        }
        catch (NumberFormatException nfe) {
            return 0;
        }
    }


    private String getVersion() {
        return getProjectProperty("app.version");
    }


    private File getDir(File baseDir, String subdir) {
        return new File(baseDir, getProjectProperty(subdir));
    }


    private File getLibDir(File baseDir) { return getDir(baseDir, "lib.dir"); }

    private File getTempDir(File baseDir) { return getDir(baseDir, "temp.dir"); }

    private File getOutputDir(File baseDir) { return getDir(baseDir, "output.dir"); }

    private File getChecksumsDir(File baseDir) { return getDir(baseDir, "checksums.dir"); }


    private File getAppDir(File baseDir, String appName) {
        return new File(getTempDir(baseDir), appName);
    }


    private File getChecksumsFile(File baseDir) {
        return new File(getChecksumsDir(baseDir), "checksums.xml");
    }

    private XNode parse(File f) {
        return new XNodeParser().parse(StringUtil.fileToString(f));
    }

}
