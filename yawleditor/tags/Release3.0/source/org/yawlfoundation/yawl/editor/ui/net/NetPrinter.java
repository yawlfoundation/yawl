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
import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;

/**
 * @author Michael Adams
 * @date 18/07/2014
 */
public class NetPrinter implements Printable {

    private PrintRequestAttributeSet attributes;
    private java.util.List<NetGraph> netList;

    private static final int CAPTION_HEIGHT = 16;


    public NetPrinter() {
        initAttributes();
    }


    public void setJobName(String jobName) {
        attributes.add(new JobName(jobName, null));
    }


    public void printNet(NetGraph net) {
        netList = new ArrayList<NetGraph>(1);
        netList.add(net);
        setJobName("YAWL Net: " + net.getName());
        print();
    }


    public void printSpecification() {
        netList = new ArrayList<NetGraph>();
        for (NetGraphModel model : SpecificationModel.getNets()) {
            netList.add(model.getGraph());
        }
        setJobName("YAWL Specification: " + SpecificationModel.getHandler().getURI());
        print();
    }


    public void print() {
        setCursor(Cursor.WAIT_CURSOR);
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        PrintService[] services = PrinterJob.lookupPrintServices();
        setCursor(Cursor.DEFAULT_CURSOR);

        if (services.length > 0) {
            try {
                job.setPrintService(services[0]);
                if (job.printDialog()) {
                    job.print(attributes);
                }
            }
            catch (PrinterException pe) {
                showError(pe.getMessage());
            }
        }
        else {
            showError("Unable to locate any printers.");
        }
    }


    public int print(Graphics g, PageFormat pf, int index) throws PrinterException {
        if (netList == null || index >= netList.size()) {
            return Printable.NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        printImage(g2d, netList.get(index), pf);
        printCaption(g2d, index, pf);
        return Printable.PAGE_EXISTS;
    }


    private void initAttributes() {
        attributes = new HashPrintRequestAttributeSet();
        attributes.add(new Copies(1));
    }


    private BufferedImage printImage(Graphics2D g, NetGraph net, PageFormat pf) {
        PrettyOutputStateManager stateManager = new PrettyOutputStateManager(net);
        stateManager.makeGraphOutputReady();
        BufferedImage image = net.getImage(Color.WHITE, 0);
        image = fitToPage(image, pf);
        Point p = getOrigin(image, pf);
        g.drawImage(image, p.x, p.y, null);
        stateManager.revertNetGraphToPreviousState();
        return image;
    }


    private void printCaption(Graphics2D g, int index, PageFormat pf) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String caption = makeCaption(index);
        int pageHeight = (int) pf.getImageableHeight();
        int pageWidth = (int) pf.getImageableWidth();

        int captionWidth = (int) g.getFontMetrics().getStringBounds(caption, g).getWidth();
        g.drawString(caption, (pageWidth - captionWidth) / 2,
                pageHeight - (CAPTION_HEIGHT /4));
    }


    private String makeCaption(int index) {
        NetGraph net = netList.get(index);
        StringBuilder s = new StringBuilder();
        s.append("Specification: ");
        s.append(getPreferredSpecificationName());
        s.append(" - Net: ");
        s.append(net.getName());
        if (netList.size() > 1) {
            if (net.getNetModel().isRootNet()) {
                s.append(" [root]");
            }
            s.append(" (").append(index +1 ).append('/')
                    .append(netList.size()).append(')');
        }
        return s.toString();
    }


    private String getPreferredSpecificationName() {
        YSpecificationHandler handler = SpecificationModel.getHandler();
        String title = handler.getTitle();
        return title != null ? title : handler.getURI();
    }


    private BufferedImage fitToPage (BufferedImage image, PageFormat pf) {
        if (shouldRotate(image, pf)) {
            image = Scalr.rotate(image, Scalr.Rotation.CW_270);
        }
        double pageWidth = pf.getImageableWidth();
        double pageHeight = pf.getImageableHeight() - CAPTION_HEIGHT;
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        if (imageWidth > pageWidth || imageHeight > pageHeight) {
            double heightDiff = imageHeight - pageHeight;
            double widthDiff = imageWidth - pageWidth;
            double scale = (heightDiff > widthDiff) ? pageHeight : pageWidth;
            return Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, (int) scale);
        }
        return image;
    }


    private boolean shouldRotate(BufferedImage image, PageFormat pf) {
        int pageWidth = (int) pf.getImageableWidth();
        return image.getWidth() > pageWidth && image.getWidth() > image.getHeight();
    }


    private Point getOrigin(BufferedImage image, PageFormat pf) {
          return new Point((int) (pf.getImageableWidth() - image.getWidth()) / 2,
                    (int) (pf.getImageableHeight()
                            - image.getHeight() - CAPTION_HEIGHT) / 2);
    }


    private void showError(String msg) {
        JOptionPane.showMessageDialog(YAWLEditor.getInstance(), msg,
                "Printer Error", JOptionPane.ERROR_MESSAGE);
    }


    private void setCursor(int cursor) {
        YAWLEditor.getInstance().setCursor(Cursor.getPredefinedCursor(cursor));
    }

}
