/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.icon;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.nexusbpm.editor.exception.EditorException;
import com.nexusbpm.editor.icon.IconSpecification.Size;

import java.util.Properties;

/**
 * This class will is properties-file managed. This way we can specify what
 * property goes with which icon.
 * 
 * @author Matthew Sandoz
 * @created Dec 14, 2006
 */
public class IconFactory {

	/**
	 * Constant used in place of a path for icons.
	 * 
	 * @see "the icon properties file."
	 */
	public final static String MAGIC_ICON_KEYWORD = "ICONMGR___";

	private static final String ICON_PATH_SIZE_PREFIX = "icon_path.size";

	public static final String ICON_PROPERTIES_FILE = "client.icon.properties";

	private static IconFactory INSTANCE;

	/**
	 * Store all the modified icons in a map so that we don't need to create a
	 * new one each time.
	 */
	private Map<IconSpecification, ImageIcon> iconMap = new HashMap<IconSpecification, ImageIcon>();

	private Properties iconProperties = new Properties();

	public static IconFactory getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new IconFactory();
		}
		return INSTANCE;
	}

	private IconFactory() {
		InputStream input = this.getClass().getResourceAsStream(
				ICON_PROPERTIES_FILE);
		try {
			iconProperties.load(input);
		} catch (IOException e) {
			throw new Error(ICON_PROPERTIES_FILE + " not found.");
		} finally {
			Closer.close(input);
		}
	}

	public AnimatedIcon getAnimatedRotatingIcon(IconSpecification spec,
			int framesPerSecond, int divisionsPerFrame, JComponent host) {
		double increment = 2 * Math.PI / divisionsPerFrame;
		double startRadians = increment;
		double endRadians = 2 * Math.PI;
		List<ImageIcon> list = new ArrayList<ImageIcon>();
		for (double radians = startRadians; radians < endRadians + increment
				/ 2; radians += increment) {
			IconSpecification cloneSpec = spec.clone();
			spec.setRotateRadians(radians);
			ImageIcon frame = getIcon(cloneSpec);
			list.add(frame);
		}
		Object[] iconFrames = list.toArray();
		AnimatedIcon animatedIcon = new AnimatedIcon(iconFrames,
				framesPerSecond, host);
		return animatedIcon;
	}

	public ImageIcon getIcon(IconSpecification spec) {
		ImageIcon icon = iconMap.get(spec);
		if (icon == null) {
			BufferedImage bufferedImage = this.getBufferedImage(spec
					.getSourceImageName(), spec.getImageSize());

			// Apply convolutions/transformations:
			if (spec.isRotated()) {
				bufferedImage = rotate(bufferedImage, spec.getRotateRadians());
			}
			if (spec.isOverlayed()) {
				bufferedImage = overlay(bufferedImage,
						spec.getOverlayXOffset(), spec.getOverlayYOffset(),
						getBufferedImage(spec.getOverlayImageName(), spec
								.getOverlayImageSize()));
			}
			if (spec.getBrightness() != 1.0f) {
				float bs = spec.getBrightness();
				RescaleOp colc = new RescaleOp(new float[] { bs, bs, bs, bs },
						new float[] { 0, 0, 0, 0 }, null);
				colc.filter(bufferedImage, bufferedImage);
			}
			if (spec.isGreyscale()) {
				ColorConvertOp colc = new ColorConvertOp(ColorSpace
						.getInstance(ColorSpace.CS_GRAY), null);
				bufferedImage = colc.filter(bufferedImage, null);
			}
			icon = new ImageIcon(bufferedImage);
			iconMap.put(spec, icon);
		}
		return icon;
	}

	public BufferedImage overlay(BufferedImage bufferedImage, int xOffset,
			int yOffset, Image overlayImage) {
		int outputHeight = Math.max(overlayImage.getHeight(null) + yOffset,
				bufferedImage.getHeight());
		int outputWidth = Math.max(overlayImage.getWidth(null) + xOffset,
				bufferedImage.getWidth());

		Graphics2D subcontext = bufferedImage.createGraphics();
		subcontext.drawImage(overlayImage, xOffset, outputHeight
				- overlayImage.getHeight(null) - yOffset, null);
		return bufferedImage;
	}

	public BufferedImage rotate(BufferedImage bufferedImage, double radians) {
		BufferedImage bufferedImage2 = new BufferedImage(bufferedImage
				.getWidth(), bufferedImage.getHeight(),
				BufferedImage.TYPE_4BYTE_ABGR);

		AffineTransform tx = new AffineTransform();

		tx.rotate(radians, bufferedImage.getWidth() / 2 - 1, bufferedImage
				.getHeight() / 2 - 1);
		AffineTransformOp op = new AffineTransformOp(tx,
				AffineTransformOp.TYPE_BILINEAR);
		op.filter(bufferedImage, bufferedImage2);
		return bufferedImage2;
	}

	public ImageIcon getIcon(String key) {
		return getIcon(new IconSpecification(key, Size.MEDIUM));
	}

	protected URL getImageUrl(String key, IconSpecification.Size size) {
		URL iconUrl = null;
		String iconPath = iconProperties.getProperty(key);
		if (iconPath != null) {
			if (iconPath.startsWith(MAGIC_ICON_KEYWORD)) {
				String prefixPath = "";
				prefixPath = iconProperties.getProperty(ICON_PATH_SIZE_PREFIX
						+ "." + size.toString().toLowerCase());

				iconPath = iconPath
						.replaceFirst(MAGIC_ICON_KEYWORD, prefixPath);
			}
			iconUrl = ApplicationIcon.class.getResource(iconPath);
		}
		return iconUrl;
	}

	public Image getImage(String key, IconSpecification.Size size) {
		URL imageUrl = getImageUrl(key, size);
		return new ImageIcon(imageUrl).getImage();
	}

	public BufferedImage getBufferedImage(String key,
			IconSpecification.Size size) {
		Image i = getImage(key, size);
		BufferedImage b = new BufferedImage(i.getWidth(null),
				i.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g = b.createGraphics();
		g.drawImage(i, 0, 0, null);
		return b;
	}

	public static void main(String[] args) {
		IconSpecification spec = new IconSpecification();
		IconFactory factory = IconFactory.getInstance();
		spec.setSourceImageName("Flow");
		spec.setImageSize(Size.LARGE);
		spec.setOverlayImageName("overlay.green_check");
		spec.setOverlayImageSize(Size.TINY);

		ImageIcon icon = factory.getIcon(spec);
		JButton button = new JButton(icon);
		JPanel panel = new JPanel();
		JFrame frame = new JFrame("test");

		AnimatedIcon ai = factory.getAnimatedRotatingIcon(spec, 30, 60, button);
		panel.add(button);
		ImagePanel imagePanel = new ImagePanel();
		AnimatedIcon ai2 = factory.getAnimatedRotatingIcon(spec, 60, 60, imagePanel);
		imagePanel.setImageIcon(ai2);
		panel.add(imagePanel);
		button.setIcon(ai);
		ai.start();
		ai2.start();
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
	}

	public static class ImagePanel extends JPanel{
		ImageIcon imageIcon;
		public ImagePanel(ImageIcon imageIcon) {
			super();
			this.imageIcon = imageIcon;
		}
		public ImagePanel() {
			super();
			this.setPreferredSize(new Dimension(64, 64));
		}
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (imageIcon != null) g.drawImage(imageIcon.getImage(), 0,0, null);
		}
		public ImageIcon getImageIcon() {
			return imageIcon;
		}
		public void setImageIcon(ImageIcon imageIcon) {
			this.imageIcon = imageIcon;
			this.repaint();
		}
	}
}

