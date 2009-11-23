package org.yawlfoundation.yawl.resourcing.jsf;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * Rough'n'ready method for estimating widths & heights of Strings in pixels without
 * a Graphics reference
 *
 * Original code: http://forums.sun.com/thread.jspa?threadID=5231347&start=0&tstart=0
 *
 * Modified by Michael Adams for version 2.0
 * Date: 06/09/2009
 */
public class FontUtil {

    public static Rectangle2D getBounds(String s, Font font) {
        if (s == null) s = "";
        AffineTransform xform = null;
        boolean isAntiAliased = true;
        boolean usesFractionalMetrics = true;
        FontRenderContext frc = new FontRenderContext(xform, isAntiAliased, usesFractionalMetrics);
        TextLayout textLayout = new TextLayout(s, font, frc);
        return textLayout.getBounds();
    }

    public static int getWidth(String s, Font font) {
        return (int) getBounds(s, font).getWidth();
    }

    public static int getHeight(String s, Font font) {
        return (int) getBounds(s, font).getHeight();
    }

}


