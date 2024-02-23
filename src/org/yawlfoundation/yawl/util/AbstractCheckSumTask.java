/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.util;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Michael Adams
 * @date 5/07/2014
 */
public abstract class AbstractCheckSumTask extends Task {

    private String _antRootDir;
    private String _antToDir;
    private String _antToFile;
    private String _antAppName;
    protected String _antLocations;
    private List<String> _antIncludes;
    private List<String> _antExcludes;
    private Project _project;

    private static final String DEFAULT_ROOT_DIR = ".";
    private static final String DEFAULT_TO_FILE = "checksums.xml";
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    protected static final String COMMENT =
           "This file is used for auto-updating. PLEASE DO NOT MODIFY OR DELETE";


    // these setters are called from the Ant task
    public void setRootDir(String dir) { _antRootDir = dir; }

    public void setToDir(String dir) { _antToDir = dir; }

    public void setFile(String file) { _antToFile = file; }

    public void setAppName(String name) { _antAppName = name; }

    public void setIncludes(String includes) {
        _antIncludes = toList(includes);
    }

    public void setExcludes(String excludes) {
        _antExcludes = toList(excludes);
    }

    public void setLocations(String locations) { _antLocations = locations; }


    protected String getAppName() { return _antAppName; }


    // called from Ant task
    public void execute() throws BuildException {
        _project = getProject();
        try {
            createOutputXML(_antRootDir, _antToDir, _antToFile);
        }
        catch (IOException ioe) {
            throw new BuildException(ioe.getMessage());
        }
    }


    public abstract String toXML(File checkDir, CheckSummer summer) throws IOException;


    /*****************************************************************************/

    protected String getProjectProperty(String key) {
        return _project != null ? _project.getProperty(key) : "";
    }


    protected String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date());
    }

    protected String formatTimestamp(long time) {
        return DATE_FORMAT.format(new Date(time));
    }


    protected List<String> toList(String expressions) {
        if (expressions != null) {
            return Arrays.asList(expressions.split("\\s+"));
        }
        return Collections.emptyList();
    }


    protected List<File> getFileList(File f) {
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


    protected boolean shouldBeIncluded(File f) {
        return ! shouldBeExcluded(f) &&
                (_antIncludes.isEmpty() || containsMatch(f, _antIncludes));
    }


    protected boolean shouldBeExcluded(File file) {
        return file.getName().startsWith(".") || file.getAbsolutePath().contains(".svn") ||
                containsMatch(file, _antExcludes);
    }


    protected String getRelativePath(File absoluteDir, String fileName) {

        // remove the absolute path from the start and the file sep from the end
        return fileName.replace(absoluteDir.getAbsolutePath(), "").substring(1);
    }


    protected void writeToFile(File f, String content) throws IOException {
        BufferedWriter buf = new BufferedWriter(new FileWriter(f));
        buf.write(content, 0, content.length());
        buf.close();
    }


    /***********************************************************************/

    private void createOutputXML(String rootDir, String toDir, String file)
            throws IOException {
        if (rootDir == null) rootDir = DEFAULT_ROOT_DIR;
        if (toDir == null) toDir = rootDir; else toDir = rootDir + "/" + toDir;
        if (file == null) file = DEFAULT_TO_FILE;
        if (_antIncludes == null) _antIncludes = Collections.emptyList();
        if (_antExcludes == null) _antExcludes = Collections.emptyList();
        String xml = toXML(new File(rootDir), new CheckSummer());
        writeToFile(new File(toDir, file), xml);
    }


    public boolean containsMatch(File file, List<String> maskList) {
        if (maskList.isEmpty()) return false;            // short circuit
        for (String mask : maskList) {
            if (mask.startsWith("*") && file.getName().endsWith(mask.substring(1))) {
                return true;
            }
            else if (mask.endsWith("/*") && containsDir(file, mask)) {
                return true;
            }
            else if (mask.endsWith("*") && file.getName().startsWith(removeLastChar(mask))) {
                return true;
            }
            else if (mask.equals(file.getName())) {
                return true;
            }
        }
        return false;
    }


    private boolean containsDir(File f, String mask) {
        return f.getAbsolutePath().contains("/" + removeLastChar(mask));
    }


    private String removeLastChar(String s) {
        return s.substring(0, s.length()-1);
    }


    /**************************************************************************/

    protected abstract class FileLocations {

        protected XNode paths = new XNode("paths");    // default
        protected Map<String, String> locations = new HashMap<String, String>();

        public FileLocations(String fileName) {
            if (fileName != null) {
                String xml = StringUtil.fileToString(fileName);
                if (xml != null) {
                    XNode root = new XNodeParser().parse(xml);
                    if (root != null) {
                        paths = root.getChild("paths");
                        loadLocations(root);
                    }
                }
            }
        }

        public XNode getPaths() { return paths; }

        public String get(String fileName) {
            String path = locations.get(fileName);
            return path != null ? path : "";
        }

        public abstract void loadLocations(XNode root);
    }

}
