/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.core.util;

import org.yawlfoundation.yawl.util.StringUtil;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.security.CodeSource;

/**
 * Generic File Utilities
 *
 * @author Michael Adams
 * @date 5/09/11
 */
public class FileUtil {

    private final static int WRITE_BUFFER_SIZE = 32768;
    private static final String HOME_DIR = setHomeDir();


    /**
     * Moves one file to another. Note that if a file already exists with the same
     * name as <code>targetFile</code> this method will overwrite its contents.
     *
     * @param sourceFile
     * @param targetFile
     * @throws java.io.IOException
     */
    public static void move(String sourceFile, String targetFile) throws IOException {
        copy(sourceFile, targetFile);
        new File(sourceFile).delete();
    }

    public static void backup(String sourceFile, String targetFile) throws IOException {
        File file = new File(sourceFile);
        if (file.exists()) copy(sourceFile, targetFile);
    }


    /**
     * Copies one file to another. Note that if a file already exists with the same
     * name as <code>targetFile</code> this method will overwrite its contents.
     *
     * @param sourceFile
     * @param targetFile
     * @throws IOException
     */
    public static void copy(String sourceFile, String targetFile) throws IOException {
        FileChannel sourceChannel = new FileInputStream(sourceFile).getChannel();
        FileChannel targetChannel = new FileOutputStream(targetFile).getChannel();
        targetChannel.transferFrom(sourceChannel, 0, sourceChannel.size());

        sourceChannel.close();
        targetChannel.close();
    }


    public static void write(String targetFile, String contents) throws IOException {
        PrintStream outputStream = new PrintStream(
                new BufferedOutputStream(
                        new FileOutputStream(targetFile), WRITE_BUFFER_SIZE), false, "UTF-8");
        outputStream.print(contents);
        outputStream.close();
    }

    /**
     * Strips the extension from a filename, assuming that extensions follow the
     * standard convention of being the text following the last '.' character
     * in the filename.
     *
     * @param fileName
     * @return The filename sans its extension
     */

    public static String stripFileExtension(String fileName) {
        int lastDotPos = fileName.lastIndexOf('.');
        return (lastDotPos > -1) ? fileName.substring(0, lastDotPos) : fileName;
    }


    public static String load(String fileName) throws IOException {
        if (fileName == null) {
            throw new IOException("File name is null.");
        }

        String content = StringUtil.fileToString(fileName);
        if (StringUtil.isNullOrEmpty(content)) {
            throw new IOException("File name is invalid, or file contains no data.");
        }

        return content;
    }

    // returns the path from which the editor was loaded (ie. the location of the editor jar)
    private static String setHomeDir() {
        String result = "";
        try {
            Class editorClass = Class.forName("org.yawlfoundation.yawl.editor.core.YConnector");
            CodeSource source = editorClass.getProtectionDomain().getCodeSource();
            if (source != null) {
                URL location = source.getLocation();
                String path = URLDecoder.decode(location.getPath(), "UTF-8");
                if (path.charAt(2) == ':') path = path.substring(1);
                int lastSep = path.lastIndexOf('/') ;
                if (lastSep > -1) result = path.substring(0, lastSep + 1) ;
                if (File.separatorChar != '/')
                    result = result.replace('/', File.separatorChar);
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
            result = System.getProperty("user.dir");   // default to current working dir
        }
        return result;
    }


    public static String getHomeDir() { return HOME_DIR; }



}
