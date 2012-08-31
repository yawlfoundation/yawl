package org.yawlfoundation.yawl.editor.core.layout;

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.*;
import java.text.NumberFormat;

/**
 * Some static utility methods for layout (un)marshaling
 *
 * @author Michael Adams
 * @date 19/06/12
 */
public class YLayoutUtil {

    private static final Dimension DEFAULT_SIZE = new Dimension(300, 200);
    protected static final Rectangle DEFAULT_RECTANGLE = new Rectangle(0, 0, 32, 32);


    /**
     * Parses an XNode's contents into a Rectangle object
     * @param node the node to parse
     * @param format the number format of the originator's locale
     * @return a Rectangle, using the bounds found in the XNode if valid, or a default
     * Rectangle(0,0,32,32) if not
     */
    protected static Rectangle parseRect(XNode node, NumberFormat format) {
        if (node == null) {
            return DEFAULT_RECTANGLE;
        }
        double x = strToDouble(node.getAttributeValue("x"), format, 0);
        double y = strToDouble(node.getAttributeValue("y"), format, 0);
        double w = strToDouble(node.getAttributeValue("w"), format, 0);
        double h = strToDouble(node.getAttributeValue("h"), format, 0);

        // we need at least some default values for width and height
        if (w == 0 && h == 0) {
            Rectangle defRect = DEFAULT_RECTANGLE;
            defRect.setLocation((int) Math.round(x), (int) Math.round(y));
            return defRect;
        }
        return new Rectangle((int) Math.round(x), (int) Math.round(y),
                (int) Math.round(w), (int) Math.round(h));
    }


    /**
     * Parses an XNode's contents into a Dimension object
     * @param node the node to parse
     * @return a Dimension, using the bounds found in the XNode if valid, or a default
     * Dimension(300,200) if not
     */
    protected static Dimension parseSize(XNode node) {
        int w = StringUtil.strToInt(node.getAttributeValue("w"), 0);
        int h = StringUtil.strToInt(node.getAttributeValue("h"), 0);
        return (w > 0 && h > 0) ? new Dimension(w, h) : DEFAULT_SIZE;
    }


    /**
     * Parses an XNode's contents into a Point2D.Double object
     * @param node the node to parse
     * @param format the number format of the originator's locale
     * @return a Point2D.Double, using the bounds found in the XNode
     */
    protected static Point2D.Double parsePosition(XNode node, NumberFormat format) {
        double x = strToDouble(node.getAttributeValue("x"), format, 0);
        double y = strToDouble(node.getAttributeValue("y"), format, 0);
        return new Point2D.Double(x, y);
    }


    /**
     * Converts a String to a double, using a number format of a specific locale (some
     * locales uses '.' to separate decimals, others use ',')
     * @param s the String to convert
     * @param formatter the number format of the originator's locale
     * @param def a default value to return if the conversion fails
     * @return the converted String if successful, or the default value if not
     */
    private static double strToDouble(String s, NumberFormat formatter, double def) {
        try {
            return formatter.parse(s).doubleValue();
        }
        catch (Exception pe) {
            return def;
        }
    }


    protected static int strToInt(String s) {
        return StringUtil.strToInt(s, 0);
    }


    protected static XNode toPointNode(Point2D.Double point, String name,
                                       NumberFormat formatter) {
        XNode node = new XNode(name);
        node.addAttribute("x", formatter.format(point.getX()));
        node.addAttribute("y", formatter.format(point.getY()));
        return node;
    }


    protected static XNode getRectNode(String name, Rectangle rect, NumberFormat formatter) {
        XNode node = new XNode(name);
        node.addAttribute("x", formatter.format(rect.getX()));
        node.addAttribute("y", formatter.format(rect.getY()));
        node.addAttribute("w", formatter.format(rect.getWidth()));
        node.addAttribute("h", formatter.format(rect.getHeight()));
        return node;
    }

    public static XNode getRectNodeAsInt(String name, Rectangle rect) {
        XNode node = new XNode(name);
        node.addAttribute("x", rect.x);
        node.addAttribute("y", rect.y);
        node.addAttribute("w", rect.width);
        node.addAttribute("h", rect.height);
        return node;
    }

    protected static byte[] loadIcon(String iconPath) throws IOException {
        final int BUF_SIZE = 16384;
        InputStream in = new FileInputStream(iconPath);
        BufferedInputStream inStream = new BufferedInputStream(in);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(BUF_SIZE);
        byte[] buffer = new byte[BUF_SIZE];

        // read chunks from the input stream and write them out
        int bytesRead = 0;
        while ((bytesRead = inStream.read(buffer, 0, BUF_SIZE)) > 0) {
            outStream.write(buffer, 0, bytesRead);
        }
        outStream.flush();
        return outStream.toByteArray();
    }

}
