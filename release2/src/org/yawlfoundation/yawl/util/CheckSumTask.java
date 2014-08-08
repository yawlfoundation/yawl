package org.yawlfoundation.yawl.util;

import java.io.File;
import java.io.IOException;

/**
 * @author Michael Adams
 * @date 5/07/2014
 */
public class CheckSumTask extends AbstractCheckSumTask {

    public String toXML(File baseDir, CheckSummer summer) throws IOException {
        StringBuilder s = new StringBuilder();
        s.append(XML_HEADER).append('\n');
        s.append(COMMENT).append('\n');
        s.append("<build>\n");
        appendVersion(s);
        appendLibs(s, baseDir, summer);
        appendApp(s, baseDir, summer);
        s.append("</build>");
        return s.toString();
    }


    private void appendVersion(StringBuilder s) {
        s.append("\t<version>").append(getProjectProperty("app.version"))
                .append("</version>\n");
        s.append("\t<build>").append(getBuildNumber(getAppName())).append("</build>\n");
        s.append("\t<timestamp>").append(now()).append("</timestamp>\n");
    }


    private void appendLibs(StringBuilder s, File baseDir, CheckSummer summer) {
        s.append("\t<lib>\n");
        for (File file : getFileList(getDir(baseDir, "lib.dir"))) {
            if (shouldBeIncluded(file)) {
                appendFile(s, file, summer, null);
            }
        }
        appendYAWLLib(s, baseDir, summer);
        s.append("\t</lib>\n");
    }


    private void appendApp(StringBuilder s, File baseDir, CheckSummer summer) {
        String appName = getAppName();
        s.append("\t<webapp name=\"").append(appName).append("\">\n");
        appendAppFiles(s, appName, baseDir, summer);
        appendAppLibs(s, appName);
        s.append("\t</webapp>\n");
    }


    private void appendAppFiles(StringBuilder s, String appName, File baseDir,
                                CheckSummer summer) {
        File fileDir = getFileDir(getTempDir(baseDir), appName);
        s.append("\t\t<files>\n");
        for (File file : getFileList(fileDir)) {
            if (shouldBeIncluded(file)) {
                appendFile(s, file, summer, fileDir);
            }
        }
        s.append("\t\t</files>\n");
    }



    private void appendAppLibs(StringBuilder s, String appName) {
        s.append("\t\t<lib>\n");
        for (String member : getPropertyList("webapp_" + appName + ".libs")) {
            s.append("\t\t\t<file name=\"")
             .append(member)
             .append("\"/>\n");
        }
        s.append("\t\t</lib>\n");
    }


    private void appendYAWLLib(StringBuilder s, File baseDir, CheckSummer summer) {
        File outputDir = getDir(baseDir, "output.dir");
        String version = getProjectProperty("app.version");
        File yawlJar = new File(outputDir, "yawl-lib-" + version + ".jar");
        appendFile(s, yawlJar, summer, null);
    }


    private void appendFile(StringBuilder s, File file, CheckSummer summer, File dir) {
        String fileName = dir == null ? file.getName() :
                getRelativePath(dir, file.getAbsolutePath());
        s.append("\t\t");
        if (dir != null) s.append("\t");
        s.append("<file name=\"")
                .append(fileName)
                .append("\" md5=\"").append(getMD5Hex(file, summer))
                .append("\" size=\"").append(file.length())
                .append("\" timestamp=\"")
                .append(formatTimestamp(file.lastModified()))
                .append("\"/>\n");

    }


    private String getMD5Hex(File file, CheckSummer summer) {
        try {
            return summer.getMD5Hex(file);
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


    private String getBuildDate(String appName) {
        return getProjectProperty(appName + ".build.date");
    }


    private File getDir(File baseDir, String subdir) {
        return new File(baseDir, getProjectProperty(subdir));
    }


    private File getTempDir(File baseDir) {
        return new File(baseDir, getProjectProperty("temp.dir"));
    }

    private File getFileDir(File buildDir, String appName) {
        return new File(buildDir, appName);
    }

}
