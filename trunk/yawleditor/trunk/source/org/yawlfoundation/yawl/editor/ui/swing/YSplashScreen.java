/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

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
    private static final Color spark = new Color(255,220,220);
    private Rectangle2D.Double textArea;
    private Rectangle2D.Double progressArea;
    private Graphics2D graphics;

    public void init() {
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

    public void showText(String str) {
        if (splash != null && splash.isVisible()) {

            // draw the text
            graphics.setPaint(Color.BLACK);
            graphics.drawString(str, (int) (textArea.getX() + 10),
                    (int) (textArea.getY() + 15));

            // make sure it's displayed
            splash.update();
        }
    }


    public void updateProgress(int pct) {
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

            // draw the 'spark' at the leading end
            int sparkWidth = 20;
            graphics.setPaint(new GradientPaint(
                    x + doneWidth - sparkWidth, y + 1, Color.RED.darker(),
                    x + doneWidth, y + 1, spark));
            graphics.fillRect(x + doneWidth - sparkWidth, y + 1, sparkWidth, hgt - 1);

            // make sure it's displayed
            splash.update();
        }
    }


    public void close() {
        updateProgress(100);
        if (splash != null && splash.isVisible()) splash.close();
    }
}
