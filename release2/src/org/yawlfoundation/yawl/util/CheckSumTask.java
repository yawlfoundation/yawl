package org.yawlfoundation.yawl.util;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Michael Adams
 * @date 5/07/2014
 */
public class CheckSumTask extends Task {

    private String _antRootDir;
    private String _antToDir;
    private String _antToFile;
    private List<String> _antIncludes;
    private List<String> _antExcludes;

    private static final SimpleDateFormat _sdf =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private static final String DEFAULT_ROOT_DIR = ".";
    private static final String DEFAULT_TO_FILE = "checksums.xml";

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String COMMENT =
           "<!-- This file is used for auto-updating. PLEASE DO NOT MODIFY OR DELETE -->";


    public void setRootDir(String dir) { _antRootDir = dir; }

    public void setToDir(String dir) { _antToDir = dir; }

    public void setFile(String file) { _antToFile = file; }

    public void setIncludes(String includes) {
        _antIncludes = toExtnList(includes);
    }

    public void setExcludes(String excludes) {
        _antExcludes = toExtnList(excludes);
    }


    // called from Ant task
    public void execute() throws BuildException {
        try {
            createOutputXML(_antRootDir, _antToDir, _antToFile);
        }
        catch (IOException ioe) {
            throw new BuildException(ioe.getMessage());
        }
    }


    public void createOutputXML(String rootDir, String toDir, String file)
            throws IOException {
        if (rootDir == null) rootDir = DEFAULT_ROOT_DIR;
        if (toDir == null) toDir = rootDir;
        if (file == null) file = DEFAULT_TO_FILE;
        createOutputXML(new File(rootDir), new File(toDir, file));
    }


    public void createOutputXML(File checkDir, File outFile) throws IOException {
        CheckSummer summer = new CheckSummer();
        if (_antIncludes == null) _antIncludes = Collections.emptyList();
        if (_antExcludes == null) _antExcludes = Collections.emptyList();
        String now = now();
        StringBuilder s = new StringBuilder();
        s.append(XML_HEADER).append('\n');
        s.append(COMMENT).append('\n');
        s.append("<release>\n");
        s.append("\t<version>").append(getProjectProperty("version"))
                .append("</version>\n");
        s.append("\t<build>").append(getBuildNumber()).append("</build>\n");
        s.append("\t<timestamp>").append(now).append("</timestamp>\n");
        s.append("\t<files>\n");
        for (File file : getFileList(checkDir)) {
            if (shouldBeIncluded(file)) {
                s.append("\t\t<file name=\"")
                 .append(getRelativePath(checkDir, file.getAbsolutePath()))
                 .append("\" md5=\"").append(summer.getMD5Hex(file))
                 .append("\" size=\"").append(file.length())
                 .append("\" timestamp=\"")
                 .append(_sdf.format(new Date(file.lastModified())))
                 .append("\"/>\n");
            }
        }
        s.append("\t</files>\n");
        s.append("</release>");
        writeToFile(outFile, s.toString());
    }


    private int getBuildNumber() {
        String prevBuild = getProjectProperty("build.number");
        try {
            return Integer.parseInt(prevBuild) + 1;
        }
        catch (NumberFormatException nfe) {
            return 0;
        }
    }

    private String getProjectProperty(String key) {
        Project project = getProject();
        return project != null ? project.getProperty(key) : "";
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date());
    }


    private List<String> toExtnList(String extn) {
        if (extn != null) {
            List<String> list = new ArrayList<String>();
            for (String s : extn.trim().split(" ")) {
                int i = s.lastIndexOf('.');
                if (i > -1) list.add(s.substring(i));
            }
            return list;
        }
        return Collections.emptyList();
    }


    private List<File> getFileList(File f) {
        List<File> fileList = new ArrayList<File>();
        if (! f.isDirectory()) {
            fileList.add(f);
        }
        else {
            File[] files = f.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) fileList.add(file);
                    else fileList.addAll(getFileList(file));
                }
            }
        }
        return fileList;
    }


    private boolean shouldBeIncluded(File f) {
        String fileName = f.getAbsolutePath();
        int dot = fileName.lastIndexOf('.');
        if (dot == -1) return false;
        String extn = fileName.substring(dot);
        if (_antExcludes.contains(extn)) return false;
        return _antIncludes.isEmpty() || _antIncludes.contains(extn);
    }


    private void writeToFile(File f, String content) throws IOException {
        BufferedWriter buf = new BufferedWriter(new FileWriter(f));
        buf.write(content, 0, content.length());
        buf.close();
    }


    private String getRelativePath(File absoluteDir, String fileName) {

        // remove the absolute path from the start and the file sep from the end
        return fileName.replace(absoluteDir.getAbsolutePath(), "").substring(1);
    }

}
