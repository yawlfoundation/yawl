package org.yawlfoundation.yawl.editor.core.api.util;

import org.yawlfoundation.yawl.util.StringUtil;

import java.io.*;
import java.nio.channels.FileChannel;

/**
 * Generic File Utilities
 *
 * @author Michael Adams
 * @date 5/09/11
 */
public class FileUtil {

    private final static int WRITE_BUFFER_SIZE = 32768;

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
     * in the filemane.
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


}
