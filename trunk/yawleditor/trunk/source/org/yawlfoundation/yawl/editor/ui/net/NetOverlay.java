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
    BasicStroke stroke;
    float scale;

    public NetOverlay() {
        setScale(1.0);
    }


    public void paint(Graphics g, Color bg) {
        paintLine(g, bg);
        if (mouseOverPort != null) paintPort(g, mouseOverPort);
    }


    public void paintLine(Graphics g, Color bg) {
        if (line != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(bg);
            g2.setXORMode(Color.black);
            g2.setStroke(stroke);
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
        g2.setColor(Color.RED);
        g2.setStroke(stroke);
        float radial = scale * 3;
        g2.draw(new Line2D.Double(centre.getX() - radial, centre.getY() - radial,
                centre.getX() + radial, centre.getY() + radial));
        g2.draw(new Line2D.Double(centre.getX() + radial, centre.getY() - radial,
                centre.getX() - radial, centre.getY() + radial));
    }


    public void setLine(Line2D.Double l) { line = l; }

    public void setScale(double s) {
        scale = (float) s;
        stroke = new BasicStroke(scale);
    }

    public void clear() {
        line = null;
        targetPort = null;
        mouseOverPort = null;
    }


    public void setTarget(Rectangle2D rect) {
        targetPort = getCentre(rect);
    }


    public void setMouseOverPort(Rectangle2D rect) {
        mouseOverPort = getCentre(rect);
    }


    public Rectangle2D getFlowClip() {
        if (line == null) return null;

        Point2D lineStart = line.getP1();
        Point2D lineEnd = line.getP2();

        double ax = lineStart.getX();
        double ay = lineStart.getY();
        double zx = lineEnd.getX();
        double zy = lineEnd.getY();
        double size = getLineHeadSize();

        return new Rectangle2D.Double(
                Math.min(ax, zx) - size,
                Math.min(ay, zy) - size,
                Math.max(ax, zx) + size,
                Math.max(ay, zy) + size
        );
    }



    private Shape createArrowHead(Graphics g) {
        if (line == null) return null;
        double size = getLineHeadSize();
        Point2D lineStart = line.getP1();
        Point2D lineEnd = line.getP2();
        double d = Math.max(1.0, lineEnd.distance(lineStart));
        double ax = -(size * (lineEnd.getX() - lineStart.getX()) / d);
        double ay = -(size * (lineEnd.getY() - lineStart.getY()) / d);
        Polygon poly = new Polygon();
        poly.addPoint((int) lineEnd.getX(), (int) lineEnd.getY());
        poly.addPoint((int) (lineEnd.getX() + ax + ay / 2), (int) (lineEnd.getY()
                + ay - ax / 2));
        Point2D last = (Point2D) lineEnd.clone();
        lineEnd.setLocation(lineEnd.getX() + ax, lineEnd.getY() + ay);
        poly.addPoint((int) (last.getX() + ax - ay / 2), (int) (last.getY()
                + ay + ax / 2));
        return poly;
    }


    private Point2D.Double getCentre(Rectangle2D rect) {
        return rect == null ? null :
                new Point2D.Double(rect.getCenterX(), rect.getCenterY());
    }


    private double getLineHeadSize() { return 10.0 * scale; }

}
