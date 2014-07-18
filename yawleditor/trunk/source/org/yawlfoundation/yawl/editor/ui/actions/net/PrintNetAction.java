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

package org.yawlfoundation.yawl.editor.ui.actions.net;

import org.yawlfoundation.yawl.editor.ui.net.NetPrinter;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetPrintUtilities;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

public class PrintNetAction extends YAWLSelectedNetAction {

    {
        putValue(Action.SHORT_DESCRIPTION, " Print the current net ");
        putValue(Action.NAME, "Print Net...");
        putValue(Action.LONG_DESCRIPTION, "Prints the currently active net");
        putValue(Action.SMALL_ICON, getMenuIcon("printer"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_P));
        putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("shift P"));
    }

    public PrintNetAction() {
        super();
    }

    public void actionPerformed(ActionEvent event) {
        BufferedImage netImage = getNetImage();
        String jobName = "YAWL Net: " + getGraph().getName();

        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        aset.add(getPreferredOrientation(netImage));
        aset.add(new Copies(1));
        aset.add(new JobName(jobName, null));

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new NetPrinter(netImage));

        PrintService[] services = PrinterJob.lookupPrintServices();
        if (services.length > 0) {
            try {
                job.setPrintService(services[0]);
                if (job.printDialog(aset)) {
                    job.print(aset);
                }
            }
            catch (PrinterException pe) {
                pe.printStackTrace();
            }
        }
    }


    private BufferedImage getNetImage() {
        return NetPrintUtilities.toBufferedImage(getGraph(), 10);
    }


    private OrientationRequested getPreferredOrientation(BufferedImage image) {
        return image.getWidth() > image.getHeight() ? OrientationRequested.LANDSCAPE :
                OrientationRequested.PORTRAIT;
    }

}
