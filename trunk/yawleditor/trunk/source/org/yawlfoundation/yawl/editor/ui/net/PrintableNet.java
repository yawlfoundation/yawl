/*
 * Created on 27/09/2004
 */
package org.yawlfoundation.yawl.editor.ui.net;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

public class PrintableNet implements Printable {
  private NetGraph net;
  
  public PrintableNet(NetGraph net) {
    this.net = net;
  }

  public int print(Graphics g, PageFormat pF, int page) {
    int pw = (int) pF.getImageableWidth();
    int ph = (int) pF.getImageableHeight();
    int cols = (net.getWidth() / pw) + 1;
    int rows = (net.getHeight() / ph) + 1;
    int pageCount = cols * rows;
    if (page >= pageCount)
      return NO_SUCH_PAGE;
    int col = page % cols;
    int row = page % rows;
    g.translate(-col*pw, -row*ph);
    g.setClip(col*pw, row*ph, pw, ph);
    net.paint(g);
    g.translate(col*pw, row*ph);
    return PAGE_EXISTS;
  }
}
