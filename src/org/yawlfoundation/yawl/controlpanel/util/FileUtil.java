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

package org.yawlfoundation.yawl.controlpanel.util;

import org.apache.commons.io.FileUtils;
import org.yawlfoundation.yawl.controlpanel.YControlPanel;
import org.yawlfoundation.yawl.controlpanel.update.UpdateConstants;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Michael Adams
 * @date 23/08/2014
 */
public class FileUtil {

    private static final String HOME_DIR = setHomeDir();
    public static final char SEP = File.separatorChar;


    public static File getJarFile() throws URISyntaxException {
        return new File(FileUtil.class.getProtectionDomain()
                        .getCodeSource().getLocation().toURI());
    }


    public static boolean delete(File root, String fileName) {
        File toDelete = makeFile(root.getAbsolutePath(), fileName);
        return toDelete.exists() && toDelete.delete();
    }


    /**
     * Removes a directory and its contents
     * @param dir the dir to remove
     */
    public static void purgeDir(File dir) {
        if (dir != null) {
            File[] fileList = dir.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isDirectory()) purgeDir(file);
                    file.delete();
                }
            }
            dir.delete();
        }
    }


    /**
     * Gets a list of sub-dirs in the specified dir. Note: Non-recursive
     * @param dir the dir to traverse
     * @return the list of sub-dirs
     */
    public static  List<File> getDirList(File dir) {
        List<File> dirList = new ArrayList<File>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) dirList.add(file);
            }
        }
        return dirList;
    }


    /**
     * Gets a list of files in the specified dir. Note: Non-recursive, no dirs included
     * @param f the dir to traverse
     * @return the list of files
     */
    public static  List<File> getFileList(File f) {
        List<File> fileList = new ArrayList<File>();
        File[] files = f.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory()) fileList.add(file);
            }
        }
        return fileList;
    }


    public static File makeFile(String path, String fileName) {
        if (fileName.contains("" + SEP)) {
            int sepPos = fileName.lastIndexOf(SEP);
            String dirPart = fileName.substring(0, sepPos);
            String filePart = fileName.substring(sepPos + 1);
            File dir = new File(path + SEP + dirPart);
            dir.mkdirs();
            return new File(dir, filePart);
        }
        return new File(path, fileName);
    }


    public static void copy(File sourceFile, File targetFile) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel targetChannel = null;
        try {
            sourceChannel = new FileInputStream(sourceFile).getChannel();
            targetChannel = new FileOutputStream(targetFile).getChannel();
            targetChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
        finally {
            if (sourceChannel != null) closeQuietly(sourceChannel);
            if (targetChannel != null) closeQuietly(targetChannel);
        }
    }


    public static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        }
        catch (IOException ignore) {
            //
        }
    }


    public static File getTmpDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }


    public static String getHomeDir() { return HOME_DIR; }


    public static String buildPath(String... files) {
        return StringUtil.join(Arrays.asList(files), SEP);
    }


    public static boolean isWindows() { return isOS("win"); }

    public static boolean isMac() { return isOS("mac"); }


    public static File getLocalCheckSumFile() {
        return new File(buildPath(TomcatUtil.getCatalinaHome(), "yawllib",
                UpdateConstants.CHECK_FILE));
    }


    public static String getJarName() {
        try {
            String path = getJarPath();
            if (path != null) {
                return new File(path).getName();
            }
        }
        catch (Exception e) {
            // fall through
        }
        return "YawlControlPanel" + YControlPanel.VERSION;
    }


    public static void unzip(File source, File target) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(Files.newInputStream(source.toPath()));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(target, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            }
            else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }


    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }
    

    private static boolean isOS(String osName) {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().startsWith(osName);
    }


    private static String setHomeDir() {
        String result = "";
        try {
            String path = getJarPath();
            if (path != null) {
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


    private static String getJarPath() throws ClassNotFoundException,
            UnsupportedEncodingException {
        Class cpClass = Class.forName("org.yawlfoundation.yawl.controlpanel.YControlPanel");
        CodeSource source = cpClass.getProtectionDomain().getCodeSource();
        return source == null ? null :
                URLDecoder.decode(source.getLocation().getPath(), "UTF-8");
    }


    public static void updateYawlLibInUI() {
        String tomcatRoot = TomcatUtil.getCatalinaHome();
        String yawlLibJarName = "yawl-lib-" + YControlPanel.VERSION + ".jar";
        String yawlLibJarPath = tomcatRoot + File.separator + "yawllib" +
                File.separator + yawlLibJarName;
        String uiLibDirName = tomcatRoot  + File.separator + "webapps" + File.separator +
                "yawlui" + File.separator + "WEB-INF" + File.separator + "lib" + File.separator;
        String uiLibJarPath = uiLibDirName + yawlLibJarName;
        
        File yawlLibJar = new File(yawlLibJarPath);
        File uiLibJar = new File(uiLibJarPath);
        
        if (FileUtils.isFileNewer(yawlLibJar, uiLibJar)) {
            try {
                FileUtils.copyFile(yawlLibJar, uiLibJar);
            }
            catch (IOException e) {
                // proceed as normal
            }
        }
    }

}
