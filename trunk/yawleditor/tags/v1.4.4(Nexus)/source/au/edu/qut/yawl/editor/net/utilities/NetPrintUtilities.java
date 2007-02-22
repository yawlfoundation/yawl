/*
 * Created on 16/09/2004
 * YAWLEditor v1.01 
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
 */

package au.edu.qut.yawl.editor.net.utilities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import java.awt.geom.Rectangle2D;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

import java.util.Set;
import java.util.LinkedList;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.print.Doc;
import javax.print.MultiDoc;
import javax.print.DocPrintJob;
import javax.print.MultiDocPrintJob;
import javax.print.PrintService;
import javax.print.MultiDocPrintService;
import javax.print.SimpleDoc;
import javax.print.DocFlavor;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.net.NetGraphModel;
import au.edu.qut.yawl.editor.net.PrettyOutputStateManager;
import au.edu.qut.yawl.editor.specification.SpecificationModel;

import java.io.IOException;

public class NetPrintUtilities {
  private static final String PNG_FILE_TYPE = "png";
  private static final String JPG_FILE_TYPE = "jpg";
  
  public static void toPNGfile(NetGraph graph, int imageBuffer, String fullFileName) {
    toImageType(graph, imageBuffer, fullFileName, PNG_FILE_TYPE);
  }

  public static void toJPGfile(NetGraph graph, int imageBuffer, String fullFileName) {
    toImageType(graph, imageBuffer, fullFileName, JPG_FILE_TYPE);
  }
  
  private static void toImageType(NetGraph graph, int imageBuffer, String fullFileName, String fileType) {
    if (!fullFileName.toLowerCase().endsWith(fileType)) {
      fullFileName += "." + fileType;
    }
    BufferedImage img = NetPrintUtilities.toBufferedImage(graph, imageBuffer);
    
    try {
      ImageIO.write(img, fileType.toLowerCase(), new File(fullFileName));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public static BufferedImage toBufferedImage(NetGraph net, int imageBuffer) {
    Object[] cells = net.getRoots();

    Rectangle2D bounds = net.getCellBounds(cells);

    Dimension d = bounds.getBounds().getSize();

    String printoutLabel = "Specification ID: " + SpecificationModel.getInstance().getId() + ", Net ID:  " + net.getName();

    int characterHeight = net.getFontMetrics(net.getFont()).getHeight();
    int characterWidth = (int) net.getFontMetrics(net.getFont()).getStringBounds(printoutLabel, net.getGraphics()).getWidth();
    
    BufferedImage image =
      new BufferedImage(
        Math.max(characterWidth, d.width)  + imageBuffer*2,
        d.height + imageBuffer*2 + characterHeight,
        BufferedImage.TYPE_INT_RGB);

    Graphics2D graphics = image.createGraphics();

    graphics.setColor(net.getBackground());
    graphics.fillRect(0, 0, image.getWidth(), image.getHeight());


    graphics.setColor(Color.BLACK);
    graphics.setFont(net.getFont());
    
    // Note: drawing a string, the coordinates are the _bottom, left_ of the string. We
    // must cater for the height of the string in its coordinates.
    
    graphics.drawString(
        printoutLabel,
        imageBuffer, 
        imageBuffer/4 + characterHeight
    );
    
    PrettyOutputStateManager stateManager = 
      new PrettyOutputStateManager(net);
      
    stateManager.makeGraphOutputReady();
    
    graphics.drawImage(net.getImage(Color.WHITE,0),imageBuffer, imageBuffer + characterHeight, null);

    stateManager.revertNetGraphToPreviousState();

    return image;
  }
  
  public static void print(NetGraph graph, 
                           PrintService service, 
                           DocFlavor flavor, 
                           PrintRequestAttributeSet printAttribs) {

    DocPrintJob job = service.createPrintJob();
    DocAttributeSet das = new HashDocAttributeSet();
    
    PrettyOutputStateManager stateManager = 
      new PrettyOutputStateManager(graph);
    stateManager.makeGraphOutputReady();
    
    File tempFile = 
      new File(new File(System.getProperty("user.dir")), 
               graph.getName() + ".png"
    );
    
    try {
      NetPrintUtilities.toPNGfile(graph, 5, tempFile.getAbsolutePath());
      FileInputStream fis = new FileInputStream(tempFile.getAbsoluteFile());
      
      Doc doc = new SimpleDoc(fis, flavor, das);
      
      job.print(doc, printAttribs);
      
      tempFile.delete();
    } catch (Exception e) {
      e.printStackTrace();
    } 

    stateManager.revertNetGraphToPreviousState();
  }
  
  public static void printAll(Set graphs,
                              MultiDocPrintService service, 
                              DocFlavor flavor, 
                              PrintRequestAttributeSet printAttribs) {

    MultiDocPrintJob job = service.createMultiDocPrintJob();
    DocAttributeSet das = new HashDocAttributeSet();
    
    PngMultiDoc multiDoc = new PngMultiDoc();
    
    LinkedList docs = new LinkedList();
    
    Iterator graphsIterator = graphs.iterator();
    
    while(graphsIterator.hasNext()) {
      
      NetGraph graph = ((NetGraphModel) graphsIterator.next()).getGraph();
      
      PrettyOutputStateManager stateManager = 
        new PrettyOutputStateManager(graph);
      stateManager.makeGraphOutputReady();
      
      File tempFile = 
        new File(new File(System.getProperty("user.dir")), 
                 graph.getName() + ".png"
      );
      
      try {
        NetPrintUtilities.toPNGfile(graph, 5, tempFile.getAbsolutePath());
        FileInputStream fis = new FileInputStream(tempFile.getAbsoluteFile());
        
        docs.add(new SimpleDoc(fis, flavor, das));

        tempFile.delete();
      } catch (Exception e) {
        e.printStackTrace();
      } 

      stateManager.revertNetGraphToPreviousState();
    }
    multiDoc.setDocuments(docs);

    try {
      job.print(multiDoc, printAttribs);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

class PngMultiDoc implements MultiDoc {
  private LinkedList pngDocuments = new LinkedList();
  private Iterator   docIterator;
  
  private Doc currentDoc;
  
  public void setDocuments(LinkedList pngDocuments) {
    this.pngDocuments = pngDocuments;
    this.docIterator = this.pngDocuments.iterator();
  }
  
  public Doc getDoc() {
    return currentDoc;
  }
  
  public MultiDoc next() {
    if (docIterator.hasNext()) {
      currentDoc = (Doc) docIterator.next();
      return this;      
    } 
    return null;
  }
}
