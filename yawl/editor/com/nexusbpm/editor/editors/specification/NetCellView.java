/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.editors.specification;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.VertexView;

import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.persistence.dao.DatasourceFolder;
import au.edu.qut.yawl.persistence.dao.DatasourceRoot;

import com.nexusbpm.editor.icon.ApplicationIcon;
import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.services.NexusServiceConstants;

/**
 * The purpose of this class is to hold onto view-specific settings, identifying resources
 * Needed to pass to the renderer etc to draw the cell. This includes specifying the label
 * and icon, though they are actually rendered in the renderer.   
 * @author SandozM
 *
 */
public class NetCellView extends VertexView {

  
  private static final int DEFAULT_WIDTH = 50;
  private static final int DEFAULT_HEIGHT = 50;
  
  
  private static transient NetCellRenderer renderer = new NetCellRenderer();

  public NetCellView(NetCell cell) {
    super(cell);
  }
  
  public String getLabel() {
	  return ((NetCell) this.getCell()).getAttributes().get(NetCell.LABEL).toString();
  }
  
  public ImageIcon getIcon() {
	  
	  return ApplicationIcon.getIcon(getIconName(), ApplicationIcon.SMALL_SIZE);
  }

 
  private String getIconName() {
      String iconName = "Component";
      YExternalNetElement task = ((NetCell) this.getCell()).getTask();
		if (task instanceof YAtomicTask) {
			String serviceName = task.getID() 
				+ NexusServiceConstants.NAME_SEPARATOR 
				+ NexusServiceConstants.SERVICENAME_VAR;
			String value = null;
			if (task.getParent() != null) {
				YVariable var = task.getParent().getLocalVariable(serviceName);
				if (var != null) {
					value = var.getInitialValue();
				}
				if (value != null) iconName = value;
			}
		} else iconName = task.getClass().getName();
      return iconName;
  }
  
  public Point2D getPerimeterPoint(Point2D source, Point2D p) {
	    Rectangle2D bounds = this.getBounds();
	    Rectangle2D iconOffsetBounds = renderer.getBounds();

    double x = bounds.getX() + iconOffsetBounds.getX();
    double y = bounds.getY() + iconOffsetBounds.getY();
    double a = DEFAULT_WIDTH / 2;
    double b = DEFAULT_HEIGHT / 2;
    double eccentricity = Math.sqrt(1 - ((b / a) * (b / a)));

    double width = bounds.getWidth();
    double height = renderer.getPreferredSize().getHeight();

    double xCenter = (x + (width / 2));
    double yCenter = (y + (height / 2));
    double dx = p.getX() - xCenter;
    double dy = p.getY() - yCenter;

    double theta = Math.atan2(dy, dx);
    double eSquared = eccentricity * eccentricity;
    double rPrime = a * Math.sqrt((1 - eSquared) / (1 - (eSquared * (Math.cos(theta) * Math.cos(theta)))));

    double ex = rPrime * Math.cos(theta);
    double ey = rPrime * Math.sin(theta);
    return new Point2D.Double(ex + xCenter, ey + yCenter);
  }

  public CellViewRenderer getRenderer() {
    return renderer;
  }
    
}