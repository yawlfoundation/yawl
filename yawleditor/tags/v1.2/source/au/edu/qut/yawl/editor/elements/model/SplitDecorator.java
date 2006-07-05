/*
 * Created on 28/12/2003, 17:20:52
 * YAWLEditor v1.0 
 * 
 * Copyright (C) 2003 Lindsay Bradford
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

package au.edu.qut.yawl.editor.elements.model;

import java.util.Iterator;
import java.util.TreeSet;
import java.util.SortedSet;

import au.edu.qut.yawl.editor.foundations.XMLUtilities;

public class SplitDecorator extends Decorator {
  public SplitDecorator() {
    super();
  }

  public SplitDecorator(YAWLTask task, int type, int position) {
    super(task, type, position);
  }

  public boolean generatesOutgoingFlows() {
    return true;
  }
  
  public boolean acceptsIncommingFlows() {
    return false;
  }
  
  public void compressFlowPriorities() {
    Object[] flowsAsArray = getFlowsInPriorityOrder().toArray();

    // Convert to array (also sorted) and compress the priority range
    // to no longer have gaps.

    for (int j = 0; j < flowsAsArray.length; j++) {
      ((YAWLFlowRelation) flowsAsArray[j]).setPriority(j);
    }
  }
  
  public SortedSet getFlowsInPriorityOrder() {
    DecoratorPort[] ports = this.getPorts();
    TreeSet flows = new TreeSet();
    
    for (int i = 0; i < ports.length; i++) {
      flows.addAll(ports[i].getEdges());
    }
    
    return flows;
  }
  
  public void changeDecompositionInPredicates(String oldLabel, String newLabel) {
    String oldLabelAsElement = XMLUtilities.toValidElementName(oldLabel);
    String newLabelAsElement = XMLUtilities.toValidElementName(newLabel);
    
    Iterator i = getFlowsInPriorityOrder().iterator();
    while (i.hasNext()) {
      YAWLFlowRelation flow = (YAWLFlowRelation) i.next();
      String updatedPredicate = 
        flow.getPredicate().replaceAll(
            "/" + oldLabelAsElement + "/",
            "/" + newLabelAsElement + "/");
      flow.setPredicate(updatedPredicate);
    }
  }

  public void changeVariableNameInPredicates(String oldVariableName, String newVariableName) {
    
    Iterator i = getFlowsInPriorityOrder().iterator();
    while (i.hasNext()) {
      YAWLFlowRelation flow = (YAWLFlowRelation) i.next();
      String updatedPredicate = 
        flow.getPredicate().replaceAll(
            "/" + oldVariableName + "/",
            "/" + newVariableName + "/");
      flow.setPredicate(updatedPredicate);
    }
  }

}
