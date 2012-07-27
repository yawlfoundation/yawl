/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ShowConfigurationJPanel.java
 *
 * Created on 16/01/2010, 10:46:09 PM
 */

package org.yawlfoundation.yawl.editor.ui.swing.data;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.GraphCell;

import java.awt.*;
import java.util.Map;

/**
 *
 * @author jingxin
 */
public class ShowConfigurationJPanel extends javax.swing.JPanel implements GraphCell{

	
	 java.net.URL url = getClass().getResource("allowed.gif");
	    Image image = new javax.swing.ImageIcon(url).getImage();

	    public ShowConfigurationJPanel(){
	        this.setSize(13,15 );
	    }
	    @Override
	public void paintComponent(Graphics g)
	{
	    g.drawImage(image, 0, 0, null);
	}

		public Map changeAttributes(Map change) {
			// TODO Auto-generated method stub
			return null;
		}

		public AttributeMap getAttributes() {
			// TODO Auto-generated method stub
			return null;
		}
		
		public void setAttributes(AttributeMap map) {
			// TODO Auto-generated method stub
			
		}

}
