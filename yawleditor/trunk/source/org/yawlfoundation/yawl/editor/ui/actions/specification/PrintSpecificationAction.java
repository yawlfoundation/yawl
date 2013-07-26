  /*
 * Created on 09/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.ui.actions.specification;

import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
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
import java.util.Iterator;
import java.util.Locale;

public class PrintSpecificationAction extends YAWLOpenSpecificationAction {
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
    putValue(Action.SHORT_DESCRIPTION, " Print this specification ");
    putValue(Action.NAME, "Print...");
    putValue(Action.LONG_DESCRIPTION, "Print this specification");
    putValue(Action.SMALL_ICON, getPNGIcon("printer"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_P));
    putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("P"));
  }
  
  public PrintSpecificationAction() {
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
    printEachNetSeparately();
    // printAllNetsAsOneJob();
  }
  
  private void printEachNetSeparately() {
    // I tried doing this straight via a BufferedImage, but the service lookup
    // returns null for that DocFlavor.  Even then, ServerUI complains that 
    // it does not suppord RenderableImages anyway.
    
    PrintService service = null;
    
    try {
      service = ServiceUI.printDialog(null, 200, 200,
                                      printServices, 
                                      defaultService, 
                                      flavor, 
                                      printAttribs);
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

    if (service != null) {
      
      Iterator netIterator = SpecificationModel.getInstance().getNets().iterator();
      
      while (netIterator.hasNext()) {
        
        // this could be nicer... we could aggregate all the nets into a single print job. 
        // unfortunately, none of the printers on our network report that they can handle multiple
        // documents. I would literally have to build a SimpleDoc with each of the diagrams
        // embedded in it. 
        
        NetGraphModel graphModel = (NetGraphModel) netIterator.next();

        jobName = new JobName(
            SpecificationModel.getInstance().getFileName() + " - " + graphModel.getName(),
            Locale.ENGLISH);
        
        printAttribs.add(jobName);
        
        NetPrintUtilities.print(graphModel.getGraph(), service, flavor, printAttribs);
      }
    } 
  }
  
  /*
  private void printAllNetsAsOneJob() {
    // I tried doing this straight via a BufferedImage, but the service lookup
    // returns null for that DocFlavor.  Even then, ServerUI complains that 
    // it does not suppord RenderableImages anyway.
    
    printAttribs.add(MultipleDocumentHandling.SINGLE_DOCUMENT_NEW_SHEET);

    MultiDocPrintService[] multiDocPrintService = PrintServiceLookup.lookupMultiDocPrintServices(new DocFlavor[] { flavor }, printAttribs);
    
    MultiDocPrintService service = (MultiDocPrintService) ServiceUI.printDialog(null, 200, 200,
      multiDocPrintService, defaultService, flavor, printAttribs);

    if (service != null) {
      NetGraphUtilities.printAll(
          SpecificationModel.getInstance().getNets(), 
          service, flavor, printAttribs
      );
    }
    printAttribs.remove(MultipleDocumentHandling.SINGLE_DOCUMENT_NEW_SHEET);
  }
  */
}