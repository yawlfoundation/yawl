/*
 * Copyright (c) 2004-2015 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.net;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Overlays extra graphics on net canvas. Called from net.paint()
 * Currently limited to drawing 'potential' flows and port symbols on mouse over
 *
 * @author Michael Adams
 * @date 10/04/15
 */
public class NetOverlay {

    Line2D.Double line;
    Point2D targetPort;
    Point2D mouseOverPort;

    public NetOverlay() { }


    public void paint(Graphics g, Color bg) {
        paintLine(g, bg);
        if (mouseOverPort != null) paintPort(g, mouseOverPort);
    }


    public void paintLine(Graphics g, Color bg) {
        if (line != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(bg);
            g2.setXORMode(Color.black);
            g2.draw(line);
            g2.setColor(Color.black);
            g2.setPaintMode();
            Shape poly = createArrowHead(g);
            g2.fill(poly);
            g2.draw(poly);
            paintPort(g, line.getP1());
            if (targetPort != null) paintPort(g, targetPort);
        }
    }


    private void paintPort(Graphics g, Point2D centre) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.DARK_GRAY);
        g2.draw(new Line2D.Double(centre.getX() - 3, centre.getY() - 3,
                centre.getX() + 3, centre.getY() + 3));
        g2.draw(new Line2D.Double(centre.getX() + 3, centre.getY() - 3,
                centre.getX() - 3, centre.getY() + 3));
    }


    public void setLine(Line2D.Double l) {
        line = l;
    }

    public void clear() {
        line = null;
        targetPort = null;
        mouseOverPort = null;
    }


    public void setTarget(Rectangle2D r) {
        targetPort = r == null ? null :
                new Point((int) r.getCenterX(), (int) r.getCenterY());
    }


    public void setMouseOverPort(Rectangle2D r) {
        mouseOverPort = r == null ? null :
                new Point((int) r.getCenterX(), (int) r.getCenterY());
    }


    public Rectangle2D getFlowClip() {
        if (line == null) return null;

        Point2D lineStart = line.getP1();
        Point2D lineEnd = line.getP2();

        double ax = lineStart.getX();
        double ay = lineStart.getY();
        double zx = lineEnd.getX();
        double zy = lineEnd.getY();

        return new Rectangle2D.Double(Math.min(ax, zx) -10, Math.min(ay, zy) -10,
                Math.max(ax, zx) + 10, Math.max(ay, zy) +10);
    }



    private Shape createArrowHead(Graphics g) {
        if (line == null) return null;
        int size = 10;
        Point2D lineStart = line.getP1();
        Point2D lineEnd = line.getP2();
        int d = (int) Math.max(1, lineEnd.distance(lineStart));
        int ax = (int) -(size * (lineEnd.getX() - lineStart.getX()) / d);
        int ay = (int) -(size * (lineEnd.getY() - lineStart.getY()) / d);
        Polygon poly = new Polygon();
        poly.addPoint((int) lineEnd.getX(), (int) lineEnd.getY());
        poly.addPoint((int) (lineEnd.getX() + ax + ay / 2), (int) (lineEnd.getY()
                + ay - ax / 2));
        Point2D last = (Point2D) lineEnd.clone();
        lineEnd.setLocation((int) (lineEnd.getX() + ax),
                (int) (lineEnd.getY() + ay));
        poly.addPoint((int) (last.getX() + ax - ay / 2), (int) (last.getY()
                + ay + ax / 2));
        return poly;
    }

}
