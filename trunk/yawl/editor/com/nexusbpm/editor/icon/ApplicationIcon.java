package com.nexusbpm.editor.icon;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nexusbpm.editor.exception.EditorException;

/**
 * This class will be a properties-file managed class. This way we can specify
 * what property goes with which icon.
 * 
 * @author Dean Mao
 * @created Sep 14, 2004
 */
public class ApplicationIcon {

  private static final Log LOG = LogFactory.getLog(ApplicationIcon.class);
  /** The icon properties file. */
  public final static String ICON_PROPERTIES_FILE = "client.icon.properties";
  /**
   * Constant used in place of a path for icons.
   * @see "the icon properties file."
   */
  public final static String MAGIC_ICON_KEYWORD = "ICONMGR___";

  /**
   * Store all the modified icons in a static map so that we don't need to
   * create a new one for different controller instances that use the same icon.
   */
  private static Map _preGeneratedIconMap = new HashMap();

  private static Properties _iconProperties;

  /** Constant for a small icon. */
  public static final int SMALL_SIZE = 0;
  /** Constant for a medium icon. */
  public static final int MEDIUM_SIZE = 1;
  /** Constant for a large icon. */
  public static final int LARGE_SIZE = 2;
  /** Constant for a huge icon. */
  public static final int HUGE_SIZE = 3;

	private static Properties loadProperties( String fileName ) {
		InputStream input = ApplicationIcon.class.getResourceAsStream( fileName );
		if( input == null )
			throw new Error( fileName + " not found." );
		Properties properties = new Properties();
		try {
			properties.load( input );
		}
		catch( IOException e ) {
		}
		finally {
			Closer.close( input );
		}
		return properties;
	}//loadProperties()

  /**
   * Return an animated icon for the specified type of component that has a
   * rotating sub-icon overlay.
   * @param className the class name of the component.
   * @param framesPerSecond the number of frames per second for the animation.
   * @return the animated icon for the specified type of component.
   */
  public static AnimatedIcon getAnimatedRotatingIcon(String className, int framesPerSecond) {
      RenderingHints iconAttrib = RenderingHints.ICON_LARGE_BRIGHT;
      iconAttrib.setKey("bright-animated");
      double startRadians = 0;
      double endRadians = 2 * Math.PI;
      double increment = Math.PI / 8;
      ArrayList list = new ArrayList();
      for (double radians = startRadians; radians < endRadians; radians += increment) {
        iconAttrib.setKey(className + "theta:" + radians);
        iconAttrib.setRotated(true);
        iconAttrib.setRotationRadians(radians);
        ImageIcon frame = getIcon(className, iconAttrib);
        list.add(frame);
      }
      Object[] iconFrames = list.toArray();
      AnimatedIcon animatedIcon = new AnimatedIcon(iconFrames, framesPerSecond); 
	  return animatedIcon;
  }

  /**
   * Gets an icon for the specified type of component using the given rendering
   * hints.
   * @param classname the class name of the component.
   * @param attribute the rendering hints.
   * @return the icon for the specified type of components according to the
   *         given rendering hints.
   */
  public static ImageIcon getIcon(String classname, RenderingHints attribute) {
    ImageIcon icon = (ImageIcon) _preGeneratedIconMap.get(classname + attribute.getKey());

    if (icon == null) {
      icon = getIcon(classname, attribute.getSize());

      // Apply convolutions/transformations:
      if (attribute.isRotated()) {
        BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
            BufferedImage.TYPE_INT_ARGB);
        BufferedImage bufferedImage2 = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D context = bufferedImage.createGraphics();
        context.drawImage(icon.getImage(), 0, 0, null);

        double radians = attribute.getRotationRadians();
        AffineTransform tx = new AffineTransform();

//        double sin = Math.abs(Math.sin(radians));
//        double cos = Math.abs(Math.cos(radians));
//        int w = icon.getIconWidth();
//        int h = icon.getIconHeight();
//        int neww = (int)Math.floor(w*cos+h*sin);
//        int newh = (int)Math.floor(h*cos+w*sin);
        
//        tx.translate((neww-w)/2, (newh-h)/2);
//        tx.rotate(radians, w/2, h/2);
        tx.rotate(radians, icon.getIconWidth()/2 - 1, icon.getIconHeight()/2 - 1);
        
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        op.filter(bufferedImage, bufferedImage2);
        icon = new ImageIcon(bufferedImage2);
      }
      if (attribute.isBright()) {
        BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D context = bufferedImage.createGraphics();
        context.drawImage(icon.getImage(), 0, 0, null);
        float bs = attribute.getBrightnessScale();
        RescaleOp colc = new RescaleOp(new float[] { bs, bs, bs, bs }, new float[] { 0, 0, 0, 0 }, null);
        colc.filter(bufferedImage, bufferedImage);
        icon = new ImageIcon(bufferedImage);
      }
      if (attribute.isGrayscale()) {
        BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D context = bufferedImage.createGraphics();
        context.drawImage(icon.getImage(), 0, 0, null);
        ColorConvertOp colc = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        colc.filter(bufferedImage, bufferedImage);
        icon = new ImageIcon(bufferedImage);
      }
      if (attribute.isOverlayed()) {
        ImageIcon overlayIcon = attribute.getOverlayIcon();
        int xOffset = attribute.getXOffset();
        int yOffset = attribute.getYOffset();

        int outputHeight = Math.max(overlayIcon.getIconHeight() + yOffset, icon.getIconHeight());
        int outputWidth = Math.max(overlayIcon.getIconWidth() + xOffset, icon.getIconWidth());

        BufferedImage bufferedImage = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D context = bufferedImage.createGraphics();
        context.drawImage(icon.getImage(), 0, 0, null);
        context.drawImage(overlayIcon.getImage(), xOffset, yOffset, null);
        icon = new ImageIcon(bufferedImage);
      }
      _preGeneratedIconMap.put(classname + attribute.getKey(), icon);
    }
    return icon;
  }

  /**
   * Gets the icon determined by the specified key.
   * @param key the key specifying the icon to return.
   * @return the icon for the specified key.
   */
  public static ImageIcon getIcon(String key) {
    return getIcon(key, MEDIUM_SIZE);
  }

  /**
   * Gets the icon of the specified size determined by the specified key.
   * @param key the key specifying the icon to return.
   * @param size the size of icon to return.
   * @return the icon for hte specified key.
   */
  public static ImageIcon getIcon(String key, int size) {
    initialize();
    String iconPath = _iconProperties.getProperty(key);
    if (iconPath != null) {
      try {
        if (iconPath.startsWith(MAGIC_ICON_KEYWORD)) {
          String prefixPath = "";

          switch (size) {
          case SMALL_SIZE: 
            prefixPath = _iconProperties.getProperty("icon_path.size.small");
            break;
          case MEDIUM_SIZE: 
            prefixPath = _iconProperties.getProperty("icon_path.size.medium");
            break;
          case LARGE_SIZE: 
            prefixPath = _iconProperties.getProperty("icon_path.size.large");
            break;
          case HUGE_SIZE: 
            prefixPath = _iconProperties.getProperty("icon_path.size.huge");
            break;
          default:
          	throw new EditorException( "Unknown icon size: " + size );
          }
          iconPath = iconPath.replaceFirst(MAGIC_ICON_KEYWORD, prefixPath);
        }
        URL iconURL = ApplicationIcon.class.getResource(iconPath);
        ImageIcon icon = new ImageIcon(iconURL);

        // these 3 lines are for backwards compatibility with the old icons in
        // the
        // images folder.
        //        if ((!iconPath.startsWith(MAGIC_ICON_KEYWORD)) && size != 1) {
        //          icon = new ImageIcon(icon.getImage().getScaledInstance(27, 27,
        // Image.SCALE_SMOOTH));
        //        }

        return icon;
      } catch (Exception e) {
        throw new Error("Cannot continue.  Path to icon: \"" + iconPath + "\" is incorrect!", e);
      }
    }
    throw new Error("Cannot continue.  Missing icon file for: " + key + ";path=" + iconPath + ";size=" + size);
  }

  private static void initialize() {
    if (_iconProperties == null) {
      _iconProperties = loadProperties(ICON_PROPERTIES_FILE);
      RenderingHints.initializeRenderAttributes();
    }
  }

}