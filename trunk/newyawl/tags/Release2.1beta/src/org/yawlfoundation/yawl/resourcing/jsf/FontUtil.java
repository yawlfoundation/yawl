package org.yawlfoundation.yawl.resourcing.jsf;

import javax.swing.*;
import java.awt.*;

/**
 * Simple class for estimating widths & heights of Strings in pixels
 *
 * Modified by Michael Adams for version 2.0
 * Date: 06/09/2009
 */
public class FontUtil {

    // Uses a generic JComponent (see below) and gives a quite accurate result.
    public static Dimension getFontMetrics(String s, Font font) {
        return new FontBox().getFontBounds(s, font) ;
    }


    
    static class FontBox extends JComponent {

        FontBox() { }

        public Dimension getFontBounds(String s, Font font) {
            FontMetrics fontMetrics = getFontMetrics(font);
            return new Dimension(fontMetrics.stringWidth(s), fontMetrics.getHeight());
        }
    }

}


