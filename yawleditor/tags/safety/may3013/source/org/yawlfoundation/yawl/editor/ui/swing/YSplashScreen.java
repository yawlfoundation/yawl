package org.yawlfoundation.yawl.editor.ui.swing;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Based on code sourced from wiki.netbeans.org
 * @author Michael Adams
 *
 * @date 26/07/12
 */
public class YSplashScreen {

    private static final SplashScreen splash = SplashScreen.getSplashScreen();
    private static Rectangle2D.Double textArea;
    private static Rectangle2D.Double progressArea;
    private static Graphics2D graphics;

    public static void init() {
        if (splash != null) {
            int height = splash.getSize().height;
            int width = splash.getSize().width;

            textArea = new Rectangle2D.Double(15., height * 0.88, width * .45, 32.);
            progressArea = new Rectangle2D.Double(8., height * .92, width - 8., 4.);

            // create the Graphics environment for drawing status info
            graphics = splash.createGraphics();
            graphics.setFont(new Font("Dialog", Font.BOLD, 12));
        }
    }

    public static void showText(String str) {
        if (splash != null && splash.isVisible()) {

            // draw the text
            graphics.setPaint(Color.BLACK);
            graphics.drawString(str, (int) (textArea.getX() + 10),
                    (int) (textArea.getY() + 15));

            // make sure it's displayed
            splash.update();
        }
    }


    public static void updateProgress(int pct) {
        if (splash != null && splash.isVisible()) {

            // Calculate the width corresponding to the correct percentage
            int x = (int) progressArea.getMinX();
            int y = (int) progressArea.getMinY();
            int wid = (int) progressArea.getWidth();
            int hgt = (int) progressArea.getHeight();

            int doneWidth = Math.round(pct*wid/100.f);
            doneWidth = Math.max(0, Math.min(doneWidth, wid-1));  // limit 0-width

            // fill the done part one pixel smaller than the outline
            graphics.setPaint(Color.RED.darker());
            graphics.fillRect(x, y + 1, doneWidth, hgt - 1);

            // make sure it's displayed
            splash.update();
        }
    }


    public static void close() {
        updateProgress(100);
    }
}
