package com.nexusbpm.editor.icon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * This class is used in the build process to copy only the necessary icons
 * specified in the client.icons.properties file to the build directories.
 * 
 * @author Dean Mao
 * @created Sep 14, 2004
 */
public class BootstrapIcons {

  private static String smallIconPrefix;

  private static String mediumIconPrefix;

  private static String largeIconPrefix;

  private static String hugeIconPrefix;

  private static String sourceIconPath;

  private static String targetIconPath;

  private static String imagesPrefix = "images/";

  /**
   * Copies the necessary icons from the specified source directory to the
   * specified target directory.
   * @param sourceIconPath the source directory.
   * @param targetIconPath the target directory.
   */
  public static void bootstrap(String sourceIconPath, String targetIconPath) {
    BootstrapIcons.sourceIconPath = sourceIconPath;
    BootstrapIcons.targetIconPath = targetIconPath;

    Properties properties = new Properties();
    String fileName = ApplicationIcon.ICON_PROPERTIES_FILE;
	  InputStream input = ApplicationIcon.class.getResourceAsStream( fileName );
	  if( input == null )
		  throw new Error( fileName + " not found." );
	  try {
		  properties.load( input );
	  }
	  catch( IOException e ) {
		  throw new Error( "problem loading properties file" );
	  }
	  finally {
		  Closer.close( input );
	  }

    smallIconPrefix = properties.getProperty("icon_path.size.small");
    mediumIconPrefix = properties.getProperty("icon_path.size.medium");
    largeIconPrefix = properties.getProperty("icon_path.size.large");
    hugeIconPrefix = properties.getProperty("icon_path.size.huge");

    for (Iterator iter = properties.keySet().iterator(); iter.hasNext();) {
      String key = (String) iter.next();
      String value = properties.getProperty(key);
      if (value.endsWith(".png") || value.endsWith(".gif") || value.endsWith(".jpg") || value.endsWith(".jpeg")) {
        if (value.startsWith(ApplicationIcon.MAGIC_ICON_KEYWORD)) {
          copy(value.replaceFirst(ApplicationIcon.MAGIC_ICON_KEYWORD, smallIconPrefix));
          copy(value.replaceFirst(ApplicationIcon.MAGIC_ICON_KEYWORD, mediumIconPrefix));
          copy(value.replaceFirst(ApplicationIcon.MAGIC_ICON_KEYWORD, largeIconPrefix));
          copy(value.replaceFirst(ApplicationIcon.MAGIC_ICON_KEYWORD, hugeIconPrefix));
        } else {
          copy(value.replaceFirst(ApplicationIcon.MAGIC_ICON_KEYWORD, imagesPrefix));
        }
      }
    }
  }

  private static void copy(String iconPath) {
    String source = sourceIconPath + "/" + iconPath;
    String target = targetIconPath + "/" + iconPath;
    String osName = System.getProperties().getProperty("os.name");
    boolean isWindows = osName.startsWith("Windows");
    source = ( isWindows ? source.replaceAll("/", "\\\\") : source );
    target = ( isWindows ? target.replaceAll("/", "\\\\") : target );
    if (! (new File(target)).exists()) {
      System.out.println("Copying from: " + source + " to: " + target);
    try {
      File f1 = new File(source);
      File f2 = new File(target);
      InputStream in = new FileInputStream(f1);
      OutputStream out = new FileOutputStream(f2);
      byte[] buf = new byte[1024];
      int len;
      while( (len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
      in.close();
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    }
  }

  /**
   * Copies the necessary icons from the specified source directory to the
   * specified target directory.
   * @param args an array whose first element indicates the source directory and
   *             second element indicates the target directory.
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      throw new Error("required two arguments, source icon directory and target icon directory");
    }
    bootstrap(args[0], args[1]);
  }
}