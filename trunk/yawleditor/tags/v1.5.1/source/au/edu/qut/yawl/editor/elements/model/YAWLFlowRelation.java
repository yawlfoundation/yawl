/*
 * Created on 28/10/2003
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

import java.lang.Comparable;
import java.lang.ClassCastException;

import java.util.HashMap;

import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.GraphConstants;

public class YAWLFlowRelation extends DefaultEdge implements YAWLCell, Comparable { 
  
  /* ALL attributes of this object are to be stored in 
   * serializationProofAttributeMap, meaning we won't get problems
   * with incompatible XML serializations as we add new attributes
   * in the future. 
   */
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private HashMap serializationProofAttributeMap = new HashMap();

  public YAWLFlowRelation() {
    super();
    buildContent(); 
  }
  
  private void buildContent() {
    HashMap map = new HashMap();

    GraphConstants.setLineEnd(map, GraphConstants.ARROW_TECHNICAL);
    GraphConstants.setEndFill(map, true);
    GraphConstants.setLineStyle(map, GraphConstants.STYLE_ORTHOGONAL);
    GraphConstants.setBendable(map, false);
    GraphConstants.setEditable(map, true);

    GraphConstants.setDisconnectable(map, true);
    GraphConstants.setConnectable(map, true);
    
    getAttributes().applyMap(map);

    setPredicate("true()");
    setPriority(0);
  }
  
  public boolean connectsTwoTasks() {
    if (isTaskPort(this.getSource()) &&
        isTaskPort(this.getTarget())) {
      return true;
    }
    return false;
  }
  
  private boolean isTaskPort(Object port) {
    if (port == null) {
      return false;
    }
    YAWLPort yawlPort = (YAWLPort) port;
    if(yawlPort.getParent() instanceof YAWLTask ||
       yawlPort.getParent() instanceof Decorator) {
      return true;
    }
    return false;
  }
  
  public boolean isRemovable() {
    return true;
  }
  
  public boolean isCopyable() {
    YAWLPort sourcePort = (YAWLPort) this.getSource();
    YAWLPort targetPort = (YAWLPort) this.getTarget();
    
    if (sourcePort == null || targetPort == null) {
      return false;
    }
    if (((YAWLCell) sourcePort.getParent()).isCopyable() &&
        ((YAWLCell) targetPort.getParent()).isCopyable()) {
      return true;  
    }
    return false;
  }
  
  public boolean isBroken() {
    YAWLPort sourcePort = (YAWLPort) this.getSource();
    YAWLPort targetPort = (YAWLPort) this.getTarget();
    
    if (sourcePort == null || targetPort == null) {
      return true;
    }
    return false;
  }
  
  public boolean generatesOutgoingFlows() {
    return false;
  }
  
  public boolean acceptsIncommingFlows() {
    return false;
  }

  public void setPriority(int priority) {
    serializationProofAttributeMap.put("priority",new Integer(priority));
  }
  
  public int getPriority() {
    return ((Integer) serializationProofAttributeMap.get("priority")).intValue();
  }
  
  public void incrementPriority() {
    setPriority(getPriority()+1);
  }
  
  public void decrementPriority() {
    setPriority(getPriority()-1);
  }
  
  public String getPredicate() {
    return (String) serializationProofAttributeMap.get("predicate");
  }

  public void setPredicate(String predicate) {
    serializationProofAttributeMap.put("predicate",predicate);
  }
  
  public int compareTo(Object object) throws ClassCastException {
    YAWLFlowRelation otherFlow = (YAWLFlowRelation) object;
    if (getPriority() < otherFlow.getPriority()) {
      return -1;
    }else if (getPriority() > otherFlow.getPriority()) {
      return 1;
    }
    else { // equal
      return 0;
    }
  }
  
  public String getTargetLabel() {
    if (getTarget() == null) {
      return "";
    }
    Object target = ((YAWLPort)getTarget()).getParent();
    if (target instanceof YAWLVertex) {
      return ((YAWLVertex)target).getLabel();
    }
    if (target instanceof JoinDecorator) {
      return ((JoinDecorator)target).getTask().getLabel();
    }
    return "";
  }
  
  public boolean isDefaultFlow() {
    if (hasOrSplitAsSource() || hasXorSplitAsSource()) {
      SplitDecorator decorator = (SplitDecorator) ((YAWLPort)getSource()).getParent();
      if (decorator.getFlowsInPriorityOrder().last().equals(this)) {
        return true;
      }
    }
    return false;
  }
  
  public boolean hasOrSplitAsSource() {
    return hasSplitAsSource(Decorator.OR_TYPE);    
  }
  
  public boolean hasXorSplitAsSource() {
    return hasSplitAsSource(Decorator.XOR_TYPE);    
  }
  
  public boolean requiresPredicate() {
    return (hasOrSplitAsSource() || hasXorSplitAsSource());
  }
  
  public boolean hasSplitAsSource(int type) {
    Object source = ((YAWLPort)getSource()).getParent();
    if (source instanceof SplitDecorator) {
      SplitDecorator decorator = (SplitDecorator) source;
      if (decorator.getType() == type) {
        return true;
      }
    }
    return false;
  }
  
  public boolean connectsTaskToItself() {
    if (getSourceTask() == null || getTargetTask() == null) {
      return false;
    }
    if (getSourceTask().equals(getTargetTask())) {
      return true;
    }
    return false; 
  }
  
  public YAWLTask getSourceTask() {
    if (getSourceVertex() instanceof YAWLTask) {
      return (YAWLTask) getSourceVertex();
    }
    return null;
  }
  
  public YAWLTask getTargetTask() {
    if (getTargetVertex() instanceof YAWLTask) {
      return (YAWLTask) getTargetVertex();
    }
    return null;
  }
  
  public boolean connectsElements() {
    if (getSource() != null && getTarget() != null) {
      return true;
    }
    return false;
  }
  
  public YAWLVertex getSourceVertex() {
    if (getSource() == null) {
      return null;
    }
    return getVertexFrom(
        ((YAWLPort)getSource()).getParent()
    );
  }
  
  public YAWLVertex getTargetVertex() {
    if (getTarget() == null) {
      return null;
    }
    return getVertexFrom(
        ((YAWLPort)getTarget()).getParent()
    );
  }
  
  private YAWLVertex getVertexFrom(Object cell) {
    assert cell != null : "null YAWLCell passed to getVertexFrom()";
    
    if (cell instanceof Decorator) {
      Decorator cellAsDecorator = (Decorator) cell;
      return cellAsDecorator.getTask();
    }
    if (cell instanceof YAWLVertex) {
      return (YAWLVertex) cell;
    }
    return null;
  }
}
