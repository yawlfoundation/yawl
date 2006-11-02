package au.edu.qut.yawl.editor.foundations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtilities {

  /**
   * Moves one file to another. Note that if a file already exists with the same
   * name as <code>targetFile</code> this method will overwrite its contents.
   * @param sourceFile
   * @param targetFile
   * @throws IOException
   */
  public static void move(String sourceFile, String targetFile) throws IOException {
    copy(sourceFile, targetFile);
    new File(sourceFile).delete();
  }

  
  /**
   * Copies one file to another. Note that if a file already exists with the same
   * name as <code>targetFile</code> this method will overwrite its contents.
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
}
