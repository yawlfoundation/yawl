/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;

/**
 * @author Michael Adams
 * @date 18/07/2014
 */
public class NetPrinter implements Printable {

    private java.util.List<BufferedImage> imageList;


    public NetPrinter(java.util.List<BufferedImage> list) {
        imageList = list;
    }


    public NetPrinter(BufferedImage image) {
        imageList = new ArrayList<BufferedImage>(1);
        imageList.add(image);
    }


    public int print(Graphics g, PageFormat pf, int index) throws PrinterException {
        if (imageList == null || index >= imageList.size()) {
            return Printable.NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        g.drawImage(fitToPage(imageList.get(index), pf), 0, 0, null);
        return Printable.PAGE_EXISTS;
    }


    private BufferedImage fitToPage (BufferedImage image, PageFormat pf) {
        int pageWidth = (int) pf.getImageableWidth();
        int pageHeight = (int) pf.getImageableHeight();
        if (pf.getOrientation() == PageFormat.PORTRAIT) {
            if (image.getWidth() > pageWidth)
                return Scalr.resize(image, pageWidth);
            if (image.getHeight() > pageHeight)
                return Scalr.resize(image, pageHeight);
        }
        else {
            if (image.getWidth() > pageHeight)
                return Scalr.resize(image, pageHeight);
            if (image.getHeight() > pageWidth)
                return Scalr.resize(image, pageWidth);
        }
        return image;
    }
}
