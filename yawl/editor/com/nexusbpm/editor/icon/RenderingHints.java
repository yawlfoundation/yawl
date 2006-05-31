package com.nexusbpm.editor.icon;

import javax.swing.ImageIcon;


/**
 * @author Dean Mao
 * @created Sep 14, 2004
 */
public class RenderingHints {
  /** Rendering hints for a small icon. */
  public static RenderingHints ICON_SMALL = new RenderingHints("small", false, false, ApplicationIcon.SMALL_SIZE, false, null);
  
  /** Rendering hints for a medium icon. */
  public static RenderingHints ICON_MEDIUM = new RenderingHints("normal", false, false, ApplicationIcon.MEDIUM_SIZE, false, null);

  /** Rendering hints for a large icon. */
  public static RenderingHints ICON_LARGE = new RenderingHints("large", false, false, ApplicationIcon.LARGE_SIZE, false, null);

  /** Rendering hints for a bright icon. */
  public static RenderingHints ICON_LARGE_BRIGHT = new RenderingHints("bright", false, true, ApplicationIcon.LARGE_SIZE, false, null);

  /** Rendering hints for a large, grayscale icon. */
  public static RenderingHints ICON_LARGE_GRAYSCALE = new RenderingHints("gray", true, true, 2.5f, ApplicationIcon.LARGE_SIZE, false, null);

  /** Rendering hints for a small, grayscale icon. */
  public static RenderingHints ICON_SMALL_GRAYSCALE = new RenderingHints("gray", true, true, 2.5f, ApplicationIcon.SMALL_SIZE, false, null);

  /** Rendering hints for a large icon with an error overlay. */
  public static RenderingHints ICON_LARGE_ERROR = null;

  /** Rendering hints for a large icon with a checkmark overlay. */
  public static RenderingHints ICON_LARGE_CHECK = null;

  /** Rendering hints for an icon with a red-lock overay. */
  public static RenderingHints ICON_NORMAL_RED_LOCK = null;

  /** Rendering hints for an icon with a green-lock overlay. */
  public static RenderingHints ICON_NORMAL_GREEN_LOCK = null;

  /** Rendering hints for an icon with a "play" overlay. */
  public static RenderingHints ICON_NORMAL_PLAY = null;
  
  /**
   * Creates rendering hints for an icon with the specified attributes.
   * @param key the key defining this rendering hints.
   * @param grayscale whether to draw in grayscale.
   * @param bright whether to draw a bright icon.
   * @param brightnessScale a scale value for how bright to make the icon.
   * @param size what size icon to draw.
   * @param overlayed whether the icon uses an overlayed icon.
   * @param overlayIcon the overlay icon to place on the icon.
   */
  public RenderingHints(String key, boolean grayscale, boolean bright, float brightnessScale, int size, boolean overlayed,
      ImageIcon overlayIcon) {
    this(key, grayscale, bright, size, overlayed, overlayIcon);
    this.brightnessScale = brightnessScale;
  }

  /**
   * Creates rendering hints for an icon with the specified attributes.
   * @param key the key defining this rendering hints.
   * @param grayscale whether to draw in grayscale.
   * @param bright whether to draw a bright icon.
   * @param brightnessScale a scale value for how bright to make the icon.
   * @param size what size icon to draw.
   * @param overlayed whether the icon uses an overlayed icon.
   * @param overlayIcon the overlay icon to place on the icon.
   * @param xOffset the horizontal offset for the overlay icon.
   * @param yOffset the vertical offset for the overlay icon.
   */
  public RenderingHints(String key, boolean grayscale, boolean bright, float brightnessScale, int size, boolean overlayed,
      ImageIcon overlayIcon, int xOffset, int yOffset) {
    this(key, grayscale, bright, size, overlayed, overlayIcon);
    this.xOffset = xOffset;
    this.yOffset = yOffset;
    this.brightnessScale = brightnessScale;
  }

  /**
   * Creates rendering hints for an icon with the specified attributes.
   * @param key the key defining this rendering hints.
   * @param grayscale whether to draw in grayscale.
   * @param bright whether to draw a bright icon.
   * @param size what size icon to draw.
   * @param overlayed whether the icon uses an overlayed icon.
   * @param overlayIcon the overlay icon to place on the icon.
   */
  public RenderingHints(String key, boolean grayscale, boolean bright, int size, boolean overlayed,
      ImageIcon overlayIcon) {
    this.key = key;
    this.isGrayscale = grayscale;
    this.isBright = bright;
    this.size = size;
    this.isOverlayed = overlayed;
    this.overlayIcon = overlayIcon;
    this.brightnessScale = 1.2f;
    this.isRotated = false;
  }

  /**
   * Initializes the rendering hints used for overlaying icons.
   */
  public static void initializeRenderAttributes() {
    if (ICON_LARGE_ERROR == null) {
      ICON_LARGE_ERROR = new RenderingHints("error", true, true, 1.4f, ApplicationIcon.LARGE_SIZE, true, ApplicationIcon
          .getIcon("overlay.red_cross"), 15, 10);

      ICON_LARGE_CHECK = new RenderingHints("check", true, true, 1.4f, ApplicationIcon.LARGE_SIZE, true, ApplicationIcon
          .getIcon("overlay.green_check"), 20, 5);

      ICON_NORMAL_RED_LOCK = new RenderingHints("red lock", false, false, 1.0f, ApplicationIcon.SMALL_SIZE, true, ApplicationIcon
          .getIcon("overlay.red_lock"), 7, 6);

      ICON_NORMAL_GREEN_LOCK = new RenderingHints("green lock", false, false, 1.0f, ApplicationIcon.SMALL_SIZE, true, ApplicationIcon
          .getIcon("overlay.green_lock"), 7, 6);

      ICON_NORMAL_PLAY = new RenderingHints("play", true, false, 1.0f, ApplicationIcon.SMALL_SIZE, true, ApplicationIcon.getIcon("overlay.play"), 0,
          0);
    }
  }
  
  private boolean isRotated;
  /**
   * @return whether the icon is rotated.
   */
  public boolean isRotated() {
    return isRotated;
  }
  /**
   * Sets whether the icon is rotated.
   * @param isRotated whether the icon is rotated.
   */
  public void setRotated(boolean isRotated) {
    this.isRotated = isRotated;
  }
  
  private double rotationRadians;
  /**
   * @return the number of radians that the icon is rotated.
   */
  public double getRotationRadians() {
    return rotationRadians;
  }
  /**
   * Sets the number of radians that the icon is rotated.
   * @param rotationRadians the number of radians that the icon is rotated.
   */
  public void setRotationRadians(double rotationRadians) {
    this.rotationRadians = rotationRadians;
  }
  private float brightnessScale;

  /**
   * @return the brightness scale.
   */
  public float getBrightnessScale() {
    return brightnessScale;
  }
  /**
   * Sets the brightness scale.
   * @param brightnessScale the brightness scale.
   */
  public void setBrightnessScale(float brightnessScale) {
    this.brightnessScale = brightnessScale;
  }
  private int xOffset;

  private int yOffset;

  private boolean isGrayscale;

  private boolean isBright;

  private int size;

  private boolean isOverlayed;

  private ImageIcon overlayIcon;

  private String key;

  /**
   * @return Returns the key.
   */
  public String getKey() {
    return key;
  }

  /**
   * @param key
   *          The key to set.
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * @return the x-offset where the overlay is placed.
   */
  public int getXOffset() {
    return xOffset;
  }

  /**
   * Sets the x-offset where the overlay is placed.
   * @param offset the x-offset where the overlay is placed.
   */
  public void setXOffset(int offset) {
    xOffset = offset;
  }

  /**
   * @return the y-offset where the overlay is placed.
   */
  public int getYOffset() {
    return yOffset;
  }

  /**
   * Sets the y-offset where the overlay is placed.
   * @param offset the y-offset where the overlay is placed
   */
  public void setYOffset(int offset) {
    yOffset = offset;
  }

  /**
   * @return whether the icon is bright.
   */
  public boolean isBright() {
    return isBright;
  }

  /**
   * Sets whether the icon is bright.
   * @param isBright whether the icon is bright.
   */
  public void setBright(boolean isBright) {
    this.isBright = isBright;
  }

  /**
   * @return whether the icon is in grayscale.
   */
  public boolean isGrayscale() {
    return isGrayscale;
  }

  /**
   * Sets whether the icon is in grayscale.
   * @param isGrayscale whether the icon is in grayscale.
   */
  public void setGrayscale(boolean isGrayscale) {
    this.isGrayscale = isGrayscale;
  }

  /**
   * @return whether the icon uses an overlayed icon.
   */
  public boolean isOverlayed() {
    return isOverlayed;
  }

  /**
   * Sets whether the icon uses an overlayed icon.
   * @param isOverlayed whether the icon uses an overlayed icon.
   */
  public void setOverlayed(boolean isOverlayed) {
    this.isOverlayed = isOverlayed;
  }

  /**
   * @return the icon to overlay over the primary icon.
   */
  public ImageIcon getOverlayIcon() {
    return overlayIcon;
  }

  /**
   * Sets the icon to overlay over the primary icon.
   * @param overlayIcon the icon to overlay over the primary icon.
   */
  public void setOverlayIcon(ImageIcon overlayIcon) {
    this.overlayIcon = overlayIcon;
  }

  /**
   * @return the size of the icon.
   * @see "The size constants defined in {@link ApplicationIcon}"
   */
  public int getSize() {
    return size;
  }

  /**
   * Sets the size of the icon.
   * @param size the size of the icon.
   * @see "The size constants defined in {@link ApplicationIcon}"
   */
  public void setSize(int size) {
    this.size = size;
  }

}