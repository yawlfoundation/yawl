/*
 * Created on 26/01/2004, 18:22:08
 * YAWLEditor v1.0 
 * 
 * Copyright (C) 2004 Lindsay Bradford
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

package au.edu.qut.yawl.editor.swing.net;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;

import au.edu.qut.yawl.editor.foundations.ResourceLoader;
import au.edu.qut.yawl.editor.net.*;
import au.edu.qut.yawl.editor.net.utilities.NetUtilities;
import au.edu.qut.yawl.editor.specification.SpecificationModel;
import au.edu.qut.yawl.editor.specification.SpecificationUtilities;

public class YAWLEditorNetFrame extends JInternalFrame {

  private static final long serialVersionUID = 1L;

  private static final SpecificationModel model = 
    SpecificationModel.getInstance();
  
  private NetGraph net;
  private JScrollPane scrollPane;

  {
    setBackground(Color.WHITE);
    setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
    setFrameIcon(
        NetUtilities.getSubNetIcon()    
    );
    installEventListener();
  }
  
  public YAWLEditorNetFrame(Point location) {
    super(null,
          true, //resizable
          true, //closable
          true, //maximizable
          true);//iconifiable

    setLocation(location);
    NetGraph newGraph = new NetGraph();
    newGraph.buildNewGraphContent();
    
    String newTitleString = "";
    boolean validNameFound = false;
    int counter = 0;
    while (!validNameFound) {
    	counter++;
			newTitleString = new String("New Net " + counter);
			if (SpecificationUtilities.getNetModelFromName(SpecificationModel.getInstance(),newTitleString) == null) {
				validNameFound = true;
			}
    }
    setNet(newGraph, newTitleString);
  }
  
  public YAWLEditorNetFrame(Rectangle bounds, String title) {
    super(null,
          true, //resizable
          true, //closable
          true, //maximizable
          true);//iconifiable

    setBounds(bounds);
    setNetName(title);
  }

  public void removeFromSpecification() {
    model.removeNet(getNet().getNetModel());
  }

  public void setNet(final NetGraph net) {
    setNet(net, null);
  }

  // Specify a title only if the net does not yet have it's own specified,
  // leave null otherwise.
  
  public void setNet(final NetGraph net, final String title) {
    this.net = net;
    scrollPane = new JScrollPane(net);
    net.setFrame(this);
    scrollPane.getViewport().setViewSize(net.getSize());
    
    getContentPane().add(scrollPane); 
    final JComponent contents = (JComponent) getContentPane();
    contents.setPreferredSize(net.getSize());
    setSize(getPreferredSize());
    if (title != null) {
      setTitle(title);
      net.setName(title);
    }
    model.addNet(getNet().getNetModel());
  }
  
  public NetGraph getNet() {
    return net;
  }
  
  public void setNetName(String title) {
    setTitle(title);
    if (getNet() != null) {
      getNet().setName(title);
    }
  }
  
  public void showRenameDialog() {
    String oldName = getNet().getNetModel().getName();
    String newName = null;
    while(SpecificationUtilities.getNetModelFromName(SpecificationModel.getInstance(),newName) != getNet().getNetModel()) {
      newName = JOptionPane.showInputDialog(this,
                                            "Change Net Name to:",
                                            oldName);
      if (newName == null) {
        newName = oldName;
      }
      if (SpecificationModel.getInstance().isValidNewDecompositionName(newName)) {
        setNetName(newName);
      }
    }
  }
  
  private void installEventListener() {
    final YAWLEditorNetFrame thisFrame = this;
    addInternalFrameListener(new InternalFrameAdapter() {
      public void internalFrameClosing(InternalFrameEvent ife) {
        int selectedValue = 
          JOptionPane.showInternalConfirmDialog(thisFrame,
              "Closing this net will delete it from the specification.\n\n" + 
              "Are you sure you want to delete this net?\n", 
              "About to delete the selected net",
              JOptionPane.WARNING_MESSAGE, 
              JOptionPane.YES_NO_OPTION);
        if(selectedValue == JOptionPane.YES_OPTION) {
          thisFrame.setVisible(false);
          thisFrame.removeFromSpecification();
        }
      }
    });
  }
  
  public JScrollPane getScrollPane() {
    return scrollPane;
  }
}
