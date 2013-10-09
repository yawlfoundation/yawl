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

import org.yawlfoundation.yawl.editor.ui.net.utilities.NetPrintUtilities;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Locale;

public class PrintNetAction extends YAWLSelectedNetAction {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private PrintRequestAttributeSet printAttribs = new HashPrintRequestAttributeSet();
  private JobName jobName;

  private DocFlavor flavor = DocFlavor.INPUT_STREAM.PNG;

  private PrintService printServices[];
  private PrintService defaultService;

  {
    putValue(Action.SHORT_DESCRIPTION, " Print the current net ");
    putValue(Action.NAME, "Print Net...");
    putValue(Action.LONG_DESCRIPTION, "Prints the currently active net");
    putValue(Action.SMALL_ICON, getPNGIcon("printer"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_P));
    putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("shift P"));
  }
  
  public PrintNetAction() {
    super();
    
    printAttribs.add(new Copies(1));
    printAttribs.add(MediaSizeName.ISO_A4);
    
    printAttribs.add(OrientationRequested.LANDSCAPE);
    printAttribs.add(Chromaticity.MONOCHROME);
  
    
    Thread lookupThread = new Thread() {
      public void run() {
        printServices = PrintServiceLookup.lookupPrintServices(flavor, printAttribs);
        defaultService = PrintServiceLookup.lookupDefaultPrintService(); 
      }
    };
    lookupThread.start();
  }
  
  public void actionPerformed(ActionEvent event) {
    // I tried doing this straight via a BufferedImage, but the service lookup
    // returns null for that DocFlavor.  Even then, ServerUI complains that 
    // it does not suppord RenderableImages anyway.
    
    jobName = new JobName(
        SpecificationModel.getHandler().getFileName() + " - " + getGraph().getName(),
        Locale.ENGLISH);
    
    printAttribs.add(jobName);
    
    PrintService selectedService = null;
    
    try {
      selectedService = ServiceUI.printDialog(null, 200, 200,
          printServices, defaultService, flavor, printAttribs);

      if (selectedService != null) {
        NetPrintUtilities.print(getGraph(), selectedService, flavor, printAttribs);
      }
    } catch (Exception e) {
      // Something funny is happening with ServiceUI.printDialog. Despite there being
      // a number of printServices available, it takes a while for the dialog to 
      // make use of them, throwing an exception saying that services cannot be null or
      // empty. Until it's willing to behave, we'll just tell the user that printing is 
      // unavailable.
      
      JOptionPane.showMessageDialog(
          null, 
          "Sorry, there are currently no printers available for this request.", 
          "Printing Services Unavailable", 
          JOptionPane.ERROR_MESSAGE);
    } 
  }
}
