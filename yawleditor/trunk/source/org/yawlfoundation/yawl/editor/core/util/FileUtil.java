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
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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

    public static void copy(File sourceFile, File targetFile) throws IOException {
        FileChannel sourceChannel = new FileInputStream(sourceFile).getChannel();
        FileChannel targetChannel = new FileOutputStream(targetFile).getChannel();
        targetChannel.transferFrom(sourceChannel, 0, sourceChannel.size());

        sourceChannel.close();
        targetChannel.close();
    }


    public static void copy(String sourceFile, String targetFile) throws IOException {
        copy(new File(sourceFile), new File(targetFile));
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


    public static void purgeDir(File dir) {
        if (dir != null) {
            File[] fileList = dir.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isDirectory()) purgeDir(file);
                    file.delete();
                }
            }
        }
    }


    public static void copyDir(File sourceDir, File targetDir) throws IOException {
        if (sourceDir != null && sourceDir.exists()) {
            File[] fileList = sourceDir.listFiles();
            if (fileList != null) {
                if (! targetDir.exists()) targetDir.mkdir();
                for (File file : fileList) {
                    copy(file, new File(targetDir, file.getName()));
                }
            }
        }
    }


    public static void unzip(File zipFile, File targetDir) throws IOException {
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry ze = zis.getNextEntry();
        while (ze != null) {
            if (! ze.isDirectory()) {
                File f = new File(targetDir, ze.getName());
                f.getParentFile().mkdirs();                // create subdirs as required
                FileOutputStream fos = new FileOutputStream(f);
                int len;
                byte buffer[] = new byte[1024];
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            ze = zis.getNextEntry();
        }
        zis.close();
    }


    public static void zip(File zipFile, File source) throws IOException {
        if (source != null) {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
            int relativePathOffset = source.getAbsolutePath().length() + 1;
            for (String path : getFilePaths(source)) {
                ZipEntry ze = new ZipEntry(path.substring(relativePathOffset));
                zos.putNextEntry(ze);
                FileInputStream fis = new FileInputStream(path);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
                fis.close();
            }
            zos.close();
        }
    }


    public static List<String> getFilePaths(File f) {
        List<String> paths = new ArrayList<String>();
        if (! f.isDirectory()) {
            paths.add(f.getAbsolutePath());
        }
        else {
            File[] files = f.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) paths.add(file.getAbsolutePath());
                    else paths.addAll(getFilePaths(file));
                }
            }
        }
        return paths;
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


    public static boolean isWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().startsWith("win");
    }


    public static File makeFile(String base, String suffix) {
        if (suffix.contains("/")) {
            int sepPos = suffix.lastIndexOf('/');
            String dirPart = suffix.substring(0, sepPos);
            String filePart = suffix.substring(sepPos + 1);
            File dir = new File(base + File.separator + dirPart);
            dir.mkdirs();
            return new File(dir, filePart);
        }
        return new File(base, suffix);
    }


}
