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

package org.yawlfoundation.yawl.editor.ui.swing.menu;


import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.LinkedList;

import org.yawlfoundation.yawl.editor.ui.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetCellUtilities;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

import org.jgraph.graph.GraphConstants;

public class FlowPopupMenu extends JPopupMenu {

  private YAWLFlowRelation flow;
  private Point marqueePoint;
  private NetGraph  net;
  
  private static final long serialVersionUID = 1L;

  public FlowPopupMenu(NetGraph net, YAWLFlowRelation flow, Point point) {
    super();
    setNet(net);
    setFlowRelation(flow);
    setMarqueePoint(point);
    addMenuItems();
  }

  private void setNet(NetGraph net) {
    this.net = net;
  }

  private void setFlowRelation(YAWLFlowRelation flow) {
    this.flow = flow;
  }

  private void setMarqueePoint(Point point) {
    this.marqueePoint = point;
  }
  
  private void addMenuItems() {
    add(new JMenuItem(new TogglePointAction(net, flow, marqueePoint)));
    
    add(new JSeparator());
    
    LinkedList<JRadioButtonMenuItem> styleButtons = new LinkedList<JRadioButtonMenuItem>();
    
    styleButtons.add(
        new JRadioButtonMenuItem(
            new OrthogonalLineAction(net, flow)
        )
    );

    styleButtons.add(
        new JRadioButtonMenuItem(
            new BezierLineAction(net, flow)
        )
    );

    styleButtons.add(
        new JRadioButtonMenuItem(
            new SplineLineAction(net, flow)
        )
    );

    ButtonGroup styleButtonGroup = new ButtonGroup();
    
    for(JRadioButtonMenuItem item: styleButtons) {
      add(item);
      styleButtonGroup.add(item);
      LineStyleAction action = (LineStyleAction) item.getAction();
      if (action.getStyle() == NetCellUtilities.getFlowLineStyle(net, flow)) {
        item.setSelected(true);
      }
    }
  }
}

class TogglePointAction extends YAWLSelectedNetAction {
  private static final long serialVersionUID = 1L;

  private NetGraph  net;
  private YAWLFlowRelation flow;
  private Point marqueePoint;
  
  {
    putValue(Action.SHORT_DESCRIPTION, "Add or remove bend");
    putValue(Action.NAME, "Add or remove bend");
    putValue(Action.LONG_DESCRIPTION, "Add or remove bend");
    //putValue(Action.SMALL_ICON, getIconByName("Blank"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_A));
  }
  
  public TogglePointAction(NetGraph net, YAWLFlowRelation flow, Point point) {
    setNet(net);
    setFlowRelation(flow);
    setMarqueePoint(point);
  }

  private void setNet(NetGraph net) {
    this.net = net;
  }

  private void setFlowRelation(YAWLFlowRelation flow) {
    this.flow = flow;
  }

  private void setMarqueePoint(Point point) {
    this.marqueePoint = point;
  }
  
  public void actionPerformed(ActionEvent event) {
    NetCellUtilities.togglePointOnFlow(net, flow, marqueePoint);
  }
}

class SplineLineAction extends LineStyleAction {

  {
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_S));
  }

  public SplineLineAction(NetGraph net, YAWLFlowRelation flow) {
    super(net, flow);
  }

  public int getStyle() {
    return GraphConstants.STYLE_SPLINE;
  }
  
  public String getStyleText() {
    return "Spline";
  }
}


class BezierLineAction extends LineStyleAction {
  {
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_B));
  }

  public BezierLineAction(NetGraph net, YAWLFlowRelation flow) {
    super(net, flow);
  }

  public int getStyle() {
    return GraphConstants.STYLE_BEZIER;
  }
  
  public String getStyleText() {
    return "Bezier";
  }
}


class OrthogonalLineAction extends LineStyleAction {
  {
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_O));
  }

  public OrthogonalLineAction(NetGraph net, YAWLFlowRelation flow) {
    super(net, flow);
  }

  public int getStyle() {
    return GraphConstants.STYLE_ORTHOGONAL;
  }
  
  public String getStyleText() {
    return "Orthogonal";
  }
}


abstract class LineStyleAction extends YAWLSelectedNetAction {
  private static final long serialVersionUID = 1L;

  private NetGraph  net;
  private YAWLFlowRelation flow;
  
  public LineStyleAction(NetGraph net, YAWLFlowRelation flow) {
    putValue(Action.SHORT_DESCRIPTION, getStyleText() + " line style");
    putValue(Action.NAME, getStyleText()  + " line style");
    putValue(Action.LONG_DESCRIPTION, getStyleText() + " line style");
    
    setNet(net);
    setFlowRelation(flow);
  }

  private void setNet(NetGraph net) {
    this.net = net;
  }

  private void setFlowRelation(YAWLFlowRelation flow) {
    this.flow = flow;
  }
  
  public void actionPerformed(ActionEvent event) {
    NetCellUtilities.setFlowStyle(net, flow, getStyle());
  }

  abstract public int getStyle();
  
  abstract public String getStyleText();
}