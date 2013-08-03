package org.yawlfoundation.yawl.editor.ui.net;

import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.PortRenderer;

import java.awt.*;

/**
 * @author Michael Adams
 * @date 31/07/13
 */
public class YPortRenderer extends PortRenderer {

    /**
   	 * Paint the renderer. Overrides superclass paint to add specific painting.
   	 * Note: The preview flag is interpreted as "highlight" in this context.
   	 * (This is used to highlight the port if the mouse is over it.)
   	 */
   	public void paint(Graphics g) {
   		Dimension d = getSize();
        Graphics2D g2 = (Graphics2D)g;
   		if (xorEnabled) {
   			g.setColor(graphBackground);
   //         g2.setComposite(AlphaComposite.Clear);
  // 			g.setXORMode(getForeground());
   		}
        else {
   //         g2.setComposite(AlphaComposite.Src);

        }

   		if (preview) {
//            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
                        // this is the overridden difference: draws a cross instead of a square
            g2.drawLine(1,1,d.width-3,d.height-3);
            g2.drawLine(1,d.height-3, d.width-3, 1);
            g2.setColor(getForeground());

            if (xorEnabled) {
                int x = (d.width-2) / 2;
                int y = (d.height-2) / 2;
                g2.drawLine(x,y,x,y);
            }
   		} else {
   			g.fillRect(0, 0, d.width, d.height);
   		}
   		boolean offset = (GraphConstants.getOffset(view.getAllAttributes()) != null);
   		if (!offset)
   			g.fillRect(1, 1, d.width - 2, d.height - 2);
   		else if (!preview)
   			g.drawRect(1, 1, d.width - 3, d.height - 3);
   	}


}
