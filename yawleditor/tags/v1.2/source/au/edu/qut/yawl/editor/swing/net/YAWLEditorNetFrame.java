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

import au.edu.qut.yawl.editor.net.*;
import au.edu.qut.yawl.editor.specification.SpecificationModel;

public class YAWLEditorNetFrame extends JInternalFrame {

  private static final SpecificationModel model = 
    SpecificationModel.getInstance();
  
  private NetGraph graph;

  {
    setBackground(Color.WHITE);
    setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
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
    setGraph(newGraph);
    
    String newTitleString = "";
    boolean validNameFound = false;
    int counter = 0;
    while (!validNameFound) {
    	counter++;
			newTitleString = new String("New Net " + counter);
			if (SpecificationModel.getInstance().getNetModelFromName(newTitleString) == null) {
				validNameFound = true;
			}
    }
    setNetName(newTitleString);
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
    model.removeNet(getGraph().getNetModel());
  }

  public void setGraph(NetGraph graph) {
    this.graph = graph;
    JScrollPane scrollPane = new JScrollPane(graph);
    graph.setFrame(this);
    scrollPane.getViewport().setViewSize(graph.getSize());
    
    getContentPane().add(scrollPane); 
    JComponent contents = (JComponent) getContentPane();
    contents.setPreferredSize(graph.getSize());
    setSize(getPreferredSize());
    model.addNet(getGraph().getNetModel());
  }
  
  public NetGraph getGraph() {
    return graph;
  }
  
  public void setNetName(String title) {
    setTitle(title);
    if (getGraph() != null) {
      getGraph().setName(title);
    }
  }
  
  public void showRenameDialog() {
    String oldName = getGraph().getNetModel().getName();
    String newName = null;
    while(SpecificationModel.getInstance().getNetModelFromName(newName) != getGraph().getNetModel()) {
      newName = JOptionPane.showInputDialog(this,
                                            "Change Net Name to:",
                                            oldName);
      if (newName == null) {
        newName = oldName;
      }
      if (SpecificationModel.getInstance().isValidNameForNets(newName)) {
        setNetName(newName);
      }
    }
  }
  
  private void installEventListener() {
    final YAWLEditorNetFrame thisFrame = this;
    addInternalFrameListener(new InternalFrameAdapter() {
      public void internalFrameClosing(InternalFrameEvent ife) {
          thisFrame.setVisible(false);
          thisFrame.removeFromSpecification();
      }
    });
  }
}
