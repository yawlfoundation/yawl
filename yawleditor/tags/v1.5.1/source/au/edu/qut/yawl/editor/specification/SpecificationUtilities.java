/*
 * Created on 24/07/2007
 * YAWLEditor v1.5
 *
 * @author Lindsay Bradford
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

package au.edu.qut.yawl.editor.specification;

import java.util.HashSet;

import au.edu.qut.yawl.editor.data.WebServiceDecomposition;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.foundations.XMLUtilities;

import au.edu.qut.yawl.editor.net.NetGraphModel;
import au.edu.qut.yawl.editor.net.utilities.NetCellUtilities;
import au.edu.qut.yawl.editor.net.utilities.NetUtilities;

/**
 * A library of standard utilities that return information on, or manipulate 
 * a selected Specification. 
 * 
 * @author Lindsay Bradford
 */

public final class SpecificationUtilities {

  public static void refreshNetViews(SpecificationModel specification) {
    for (NetGraphModel net : specification.getNets()) {
      net.getGraph().repaint();      
    }
  }

  public static void showGridOfNets(SpecificationModel specification, boolean gridVisible) {
    for (NetGraphModel net : specification.getNets()) {
      net.getGraph().setGridVisible(gridVisible);      
    }
  }
  
  public static void showAntiAliasing(SpecificationModel specification, boolean antiAliased) {
    for (NetGraphModel net : specification.getNets()) {
      net.getGraph().setAntiAliased(antiAliased);
    }
  }
  
  public static void setSpecificationFontSize(SpecificationModel specification, int fontSize) {
    specification.setFontSize(fontSize);

    for (NetGraphModel net : specification.getNets()) {
      NetCellUtilities.propogateFontChangeAcrossNet(
          net.getGraph(), 
          net.getGraph().getFont().deriveFont(
              (float) fontSize
          )
      );
    }
  }
  
  public static HashSet<WebServiceDecomposition> getUsedWebServiceDecompositions(SpecificationModel specification) {
    HashSet<WebServiceDecomposition> usedDecompositions = new HashSet<WebServiceDecomposition>();

    for(NetGraphModel net: SpecificationModel.getInstance().getNets()) {
      
      for(Object taskAsObject: NetUtilities.getAllTasks(net)) {
        YAWLTask task = (YAWLTask) taskAsObject;
        if (task.getDecomposition()!= null  && 
            task.getDecomposition() instanceof WebServiceDecomposition) {
          usedDecompositions.add((WebServiceDecomposition) task.getDecomposition());
        }
      }
    }
    return usedDecompositions;
  }
  
  public static NetGraphModel getNetModelFromName(SpecificationModel specification, String name) {
    if (name == null || name.equals("")) {
      return null;
    }
    
    for(NetGraphModel net: SpecificationModel.getInstance().getNets()) {
      if (net.getName().equals(name)) {
        return net;
      }
    }

    return null;
  }
  
  
  public static boolean isValidDecompositionLabel(SpecificationModel specification, String label) {
    for(WebServiceDecomposition decomposition : specification.getWebServiceDecompositions()) {
      if (decomposition.getLabelAsElementName().equals(XMLUtilities.toValidXMLName(label))) {
        return false;
      }
    }

    for(NetGraphModel net: SpecificationModel.getInstance().getNets()) {
      if (net.getDecomposition().getLabelAsElementName().equals(XMLUtilities.toValidXMLName(label))) {
        return false;
      }
    }
    return true;
  }

}
