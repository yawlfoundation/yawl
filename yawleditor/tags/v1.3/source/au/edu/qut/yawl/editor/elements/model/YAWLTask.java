/*
 * Created on 14/11/2003
 * YAWLEditor v1.0 
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
 *
 */

package au.edu.qut.yawl.editor.elements.model;

import au.edu.qut.yawl.editor.data.DataVariableSet;
import au.edu.qut.yawl.editor.data.DataVariable;
import au.edu.qut.yawl.editor.data.ParameterLists;
import au.edu.qut.yawl.editor.data.Parameter;
import au.edu.qut.yawl.editor.data.Decomposition;

import au.edu.qut.yawl.editor.net.CancellationSet;
import au.edu.qut.yawl.editor.resourcing.ResourceMapping;

import java.awt.Point;
import java.awt.Rectangle;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.jgraph.graph.GraphConstants;

public abstract class YAWLTask extends YAWLVertex {

  public YAWLTask() {
    super();
    initialize();
  }

  public YAWLTask(Point startPoint) {
    super(startPoint); 
    initialize();
  }
  
  public void setSerializationProofAttributeMap(HashMap map) {
    this.serializationProofAttributeMap = map;
  }
  
  public HashMap getSerializationProofAttributeMap() {
    return this.serializationProofAttributeMap;
  }
  
  private void initialize() {
    setCancellationSet(new CancellationSet(this));
    setParameterLists(new ParameterLists());
    setDecomposition(null);
    setAllocationResourceMapping((ResourceMapping) ResourceMapping.DEFAULT_ALLOCATION.clone());
    setAuthorisationResourceMapping((ResourceMapping) ResourceMapping.DEFAULT_AUTHORISATION.clone());
  }

  public int hasSplitObjectAt() {
    if (getSplitDecorator() != null) {
      return getSplitDecorator().getCardinalPosition();
    }
    return this.getPositionOfOutgoingFlow();
  }

  public int hasJoinObjectAt() {
    if (getJoinDecorator() != null) {
      return getJoinDecorator().getCardinalPosition();
    }
    return this.getPositionOfIncommingFlow();
  }
  
  public int hasJoinDecoratorAt() {
    return getJoinDecorator() != null ? 
      getJoinDecorator().getCardinalPosition() : NOWHERE;
  }

  public int hasSplitDecoratorAt() {
    return getSplitDecorator() != null ?
      getSplitDecorator().getCardinalPosition() : NOWHERE;
  }

  public boolean hasSplitDecorator() {
    return getSplitDecorator() != null ? true : false;
  }

  public boolean hasJoinDecorator() {
    return getJoinDecorator() != null ? true : false;
  }

  public boolean hasDecoratorAtPosition(int position) {
    return decoratorTypeAtPosition(position) == Decorator.NO_TYPE ? false:true;
  }

  public int decoratorTypeAtPosition(int position) {
    if (getJoinDecorator() != null && 
        getJoinDecorator().getCardinalPosition() == position) {
      return getJoinDecorator().getType();
    }
    if (getSplitDecorator() != null && 
        getSplitDecorator().getCardinalPosition() == position) {
      return getSplitDecorator().getType();
    }
    return Decorator.NO_TYPE;
  }
  
  public YAWLFlowRelation getOnlyOutgoingFlow() {
    if (getPositionOfOutgoingFlow() != NOWHERE) {
      return (YAWLFlowRelation) 
        (getPortAt(getPositionOfOutgoingFlow()).getEdges().toArray())[0];
    }
    return null;
  }

  public YAWLFlowRelation getOnlyIncommingFlow() {
    if (getPositionOfIncommingFlow() != NOWHERE) {
      return (YAWLFlowRelation) 
        (getPortAt(getPositionOfIncommingFlow()).getEdges().toArray())[0];
    }
    return null;
  }
 
  public JoinDecorator getJoinDecorator() {
  	VertexContainer container = (VertexContainer) this.getParent();
  	if (container == null) {
  		return null;
  	}
  	return container.getJoinDecorator();
  }

  public SplitDecorator getSplitDecorator() {
		VertexContainer container = (VertexContainer) this.getParent();
		if (container == null) {
			return null;
		}
		return container.getSplitDecorator();
  }
 
  public Rectangle getBounds() {
    Map map = getAttributes();
    return GraphConstants.getBounds(map);
  }
  
  public Point getLocation() {
    return new Point((int) getBounds().getX(),
                     (int) getBounds().getY());
  }
  
  public int getIncommingFlowCount() {
    return getJoinDecorator() != null ? 
      getJoinDecorator().getFlowCount() : 0;
  }

  public int getOutgoingFlowCount() {
    return getSplitDecorator() != null ? 
      getSplitDecorator().getFlowCount() : 0;
  }
  
  public CancellationSet getCancellationSet() {
    return (CancellationSet) serializationProofAttributeMap.get("cancellationSet");
  }
  
  public void setCancellationSet(CancellationSet cancellationSet) {
    serializationProofAttributeMap.put("cancellationSet",cancellationSet);
  }

  public ParameterLists getParameterLists() {
    return (ParameterLists) serializationProofAttributeMap.get("parameterLists");
  }
  
  public void setParameterLists(ParameterLists parameterLists) {
    if (parameterLists != null) {
      serializationProofAttributeMap.put("parameterLists",parameterLists);
    }
  }
  
  public void resetParameterLists() {
    setParameterLists(new ParameterLists());
  }
  
  public void setDecomposition(Decomposition decomposition) {
    serializationProofAttributeMap.put("decomposition",decomposition);
  }
  
  public Decomposition getDecomposition() {
    return (Decomposition) serializationProofAttributeMap.get("decomposition");
  }

  public void setAllocationResourceMapping(ResourceMapping resourceMapping) {
    serializationProofAttributeMap.put("allocationResourceMapping",resourceMapping);
  }
  
  public ResourceMapping getAllocationResourceMapping() {
    return (ResourceMapping) serializationProofAttributeMap.get("allocationResourceMapping");
  }

  public void setAuthorisationResourceMapping(ResourceMapping resourceMapping) {
    serializationProofAttributeMap.put("authorisationResourceMapping",resourceMapping);
  }
  
  public ResourceMapping getAuthorisationResourceMapping() {
    return (ResourceMapping) serializationProofAttributeMap.get("authorisationResourceMapping");
  }
  
  public DataVariableSet getVariables() {
    if (getDecomposition() != null) {
      return getDecomposition().getVariables();
    }
    return null;
  }
  
  public boolean hasBothDecorators() {
    return (hasJoinDecorator() && hasSplitDecorator());
  }

  public boolean hasTopLeftAdjacentDecorators() {
    if (hasDecoratorAtPosition(Decorator.LEFT) && 
        hasDecoratorAtPosition(Decorator.TOP)) {
      return true;
    }
    return false;    
  }
  
  public boolean hasTopRightAdjacentDecorators() {
    if (hasDecoratorAtPosition(Decorator.TOP) && 
        hasDecoratorAtPosition(Decorator.RIGHT)) {
      return true;
    }
    return false;    
  }

  public boolean hasBottomRightAdjacentDecorators() {
    if (hasDecoratorAtPosition(Decorator.RIGHT) && 
        hasDecoratorAtPosition(Decorator.BOTTOM)) {
      return true;
    }
    return false;    
  }

  public boolean hasBottomLeftAdjacentDecorators() {
    if (hasDecoratorAtPosition(Decorator.LEFT) && 
        hasDecoratorAtPosition(Decorator.BOTTOM)) {
      return true;
    }
    return false;    
  }
  
  public boolean hasVerticallyAlignedDecorators() {
    if (hasDecoratorAtPosition(Decorator.TOP) && 
        hasDecoratorAtPosition(Decorator.BOTTOM)) {
      return true;
    }
    return false;
  }
  
  public boolean hasHorizontallyAlignedDecorators() {
    if (hasDecoratorAtPosition(Decorator.LEFT) && 
        hasDecoratorAtPosition(Decorator.RIGHT)) {
      return true;
    }
    return false;    
  }
  
  public boolean hasNoSelfReferencingFlows() {
    HashSet flows = new HashSet();
    
    if (!hasBothDecorators()) {
      return true;
    }
    
    flows.addAll(getSplitDecorator().getFlows());
    flows.addAll(getJoinDecorator().getFlows());

    Iterator flowIterator = flows.iterator();
    while(flowIterator.hasNext()) {
      YAWLFlowRelation flow = (YAWLFlowRelation) flowIterator.next();
      if (flow.connectsTaskToItself()) {
        return false;
      }
    }
    return true;
  }
  
  public void removeInvalidParameters() {
    LinkedList paramsToDelete = new LinkedList();
    Iterator inputParameters = getParameterLists().getInputParameters().getParameters().iterator();
    while (inputParameters.hasNext()) {
      Parameter parameter = (Parameter) inputParameters.next();
      if (!getDecomposition().hasVariableEqualTo(parameter.getVariable())) {
        paramsToDelete.add(parameter);
      }
    }
    
    Iterator oldInputParams = paramsToDelete.iterator();
    while(oldInputParams.hasNext()) {
      Parameter param = (Parameter) oldInputParams.next();
      getParameterLists().getInputParameters().remove(param.getVariable());
    }
    
    //Output parameters are scoped to something other than the task. Don't need 
    // to delete them, but perhaps the queries are broken.
  }
  
  public Object clone() {
    YAWLTask clone = (YAWLTask) super.clone();
    
    CancellationSet clonedCancellationSet = (CancellationSet) getCancellationSet().clone();
    clone.setCancellationSet(clonedCancellationSet);
    clonedCancellationSet.setTriggeringTask(clone);
    
    clone.setParameterLists((ParameterLists) getParameterLists().clone());
    clone.setAllocationResourceMapping((ResourceMapping) getAllocationResourceMapping().clone());
    clone.setAuthorisationResourceMapping((ResourceMapping) getAuthorisationResourceMapping().clone());
    
    return clone;
  }
}