/*
 * Created on 18/12/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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

package au.edu.qut.yawl.editor.net;

import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import au.edu.qut.yawl.editor.data.DataVariableSet;
import au.edu.qut.yawl.editor.data.Decomposition;

import au.edu.qut.yawl.editor.elements.model.YAWLCell;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.elements.model.YAWLVertex;
import au.edu.qut.yawl.editor.elements.model.VertexContainer;
import au.edu.qut.yawl.editor.elements.model.JoinDecorator;
import au.edu.qut.yawl.editor.elements.model.SplitDecorator;
import au.edu.qut.yawl.editor.elements.model.DecoratorPort;
import au.edu.qut.yawl.editor.elements.model.ElementUtilities;

import au.edu.qut.yawl.editor.elements.model.YAWLCondition;
import au.edu.qut.yawl.editor.elements.model.YAWLFlowRelation;

import au.edu.qut.yawl.editor.net.utilities.NetUtilities;
import au.edu.qut.yawl.editor.specification.SpecificationModel;

import org.jgraph.graph.Port;
import org.jgraph.graph.Edge;

import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.ParentMap;
import org.jgraph.graph.ConnectionSet;

public class NetGraphModel extends DefaultGraphModel {
  
  private boolean isStartingNet = false;
  private NetGraph graph;
  private Decomposition decomposition;
  
  public NetGraphModel(NetGraph graph) {
    super();   
    this.graph = graph;
    decomposition = new Decomposition();
  }
  
  public NetGraph getGraph() {
    return this.graph;
  }
  
  public void setName(String name) {
    String oldName = getName();
    decomposition.setLabel(name);
    if (!oldName.equals("")) {
      SpecificationModel.getInstance().propogateNetNameChange(oldName, this);
    }
  }

  public String getName() {
    return decomposition.getLabel();
  }
  
  public void setIsStartingNet(boolean isStartingNet) {
    this.isStartingNet = isStartingNet;

    if (getGraph().getFrame() != null) {
      if (isStartingNet) {
        getGraph().getFrame().setClosable(false);
      } else {
        getGraph().getFrame().setClosable(true);
      }
    }
  }
    
  public boolean getIsStartingNet() {
   return this.isStartingNet;
  }
  
  public boolean isStartingNet() {
    return getIsStartingNet();
  }

  public Decomposition getDecomposition() {
    return this.decomposition;
  }
  
  public void setDecomposition(Decomposition decomposition) {
    this.decomposition = decomposition;
  }
  
  public DataVariableSet getVariableSet() {
    return decomposition.getVariables();
  }
  
  public void setVariableSet(DataVariableSet variableSet) {
    if (variableSet == null) {
      decomposition.setVariables(new DataVariableSet());
    } else {
      decomposition.setVariables(variableSet);
    }
  }
  
  public void remove(Object[] cells) {
    removeCellsAndEdges(getRemovableCellsOf(cells));    
  }
  
  private HashSet getRemovableCellsOf(Object[] cells) {
    HashSet removableCells = new HashSet();
      
    for(int i = 0; i < cells.length ; i++) {
      if (cells[i] instanceof YAWLCell) {
        YAWLCell element = (YAWLCell) cells[i];
        if (element.isRemovable()) {
          removableCells.add(cells[i]);         
        }
      } else {
        removableCells.add(cells[i]);          
      }
    }
    return removableCells;    
  }
    
  private void removeCellsAndEdges(final Set cells) {
    HashSet cellsAndTheirEdges = new HashSet(cells);
    cellsAndTheirEdges.addAll(
      getDescendantList(this, cells.toArray())
    );
    cellsAndTheirEdges.addAll(getEdges(this,cells.toArray()));
    removeCellsFromCancellationSets(cellsAndTheirEdges);

    super.remove(cellsAndTheirEdges.toArray());

    compressFlowPriorities();
  }
  
  private void compressFlowPriorities() {
    Set tasksWithSplitDecorators = NetUtilities.getTasksWitSplitDecorators(this);
    Iterator i = tasksWithSplitDecorators.iterator();
    while (i.hasNext()) {
      SplitDecorator decorator = ((YAWLTask)i.next()).getSplitDecorator();
      decorator.compressFlowPriorities();
    }
  }
  
  private void removeCellsFromCancellationSets(final Set cells) {
    Object[] cellsAsObjects = cells.toArray();
    Object[] tasksWithCancellationSets = NetUtilities.getTasksWithCancellationSets(this).toArray();
    for (int i = 0; i < tasksWithCancellationSets.length; i++) {
      YAWLTask task = (YAWLTask) tasksWithCancellationSets[i];
      if (task.equals(this.getGraph().viewingCancellationSetOf())) {
        this.getGraph().changeCancellationSet(null);
      }
      CancellationSet set = task.getCancellationSet();
      for(int j = 0; j < cellsAsObjects.length; j++) {
        if (cellsAsObjects[j] instanceof YAWLCell) {
          set.removeMember((YAWLCell)cellsAsObjects[j]);
        }
      }
    }
  }

  public Map cloneCells(Object[] cells) {
    Object[] clones = new Object[cells.length];
    int j = 0; 
      
    for(int i = 0; i < cells.length ; i++) {
      if (cells[i] instanceof YAWLVertex) {
        YAWLVertex element = (YAWLVertex) cells[i];
        if (element.isCopyable()) {
          clones[j++] = cells[i];          
        }
      } else {
        clones[j++] = cells[i];          
      }
    }
    
    Map clonedCells = super.cloneCells(clones);
    return clonedCells;
  }
  
  public boolean connectionAllowable(Port source, Port target) {
    boolean rulesAdheredTo = true;
    
    YAWLCell sourceCell = (YAWLCell) getParent(source);
    YAWLCell targetCell = (YAWLCell) getParent(target);
    
    if(sourceCell == targetCell) {
       rulesAdheredTo = false;      
    }
    
    if (sourceCell instanceof YAWLCondition &&
        targetCell instanceof YAWLCondition) {
      rulesAdheredTo = false;      
    }
    if (areConnectedAsSourceAndTarget(sourceCell, targetCell)) { 
      rulesAdheredTo = false;                  
    }
    if (!generatesOutgoingFlows(sourceCell)) {
      rulesAdheredTo = false;                  
    }
    if (!acceptsIncommingFlows(targetCell)) {
      rulesAdheredTo = false;                  
    }
    if (selfReferencingTaskNotUsingLongeEdgePorts(source, target)) {
      rulesAdheredTo = false;
    }
    return rulesAdheredTo;
  }
  
  private boolean selfReferencingTaskNotUsingLongeEdgePorts(Port source, Port target) {
    if (!(source instanceof DecoratorPort) || !(target instanceof DecoratorPort)) {
      return false;
    } 
    
    DecoratorPort sourceAsDecoratorPort = (DecoratorPort) source;
    DecoratorPort targetAsDecoratorPort = (DecoratorPort) target;

    if (sourceAsDecoratorPort.getDecorator().getTask().equals(
        targetAsDecoratorPort.getDecorator().getTask())) {
      if (sourceAsDecoratorPort.isLongEdgePort() && 
          targetAsDecoratorPort.isLongEdgePort())  {
        return false;
      }
      return true;
    }

    return false;
  }

  public boolean areConnected(YAWLCell sourceCell, YAWLCell targetCell) {
    if (areConnectedAsSourceAndTarget(sourceCell, targetCell)) {
      return true;
    }
    if (areConnectedAsSourceAndTarget(targetCell, sourceCell)) {
      return true;
    }
    return false;    
  }
  
  public boolean areConnectedAsSourceAndTarget(YAWLCell sourceCell, 
                                               YAWLCell targetCell) {
    Iterator setIterator = 
      getEdges(this, new Object[] {sourceCell}).iterator();
    while(setIterator.hasNext()) {
      Edge edge = (Edge) setIterator.next(); 
      if ((getTargetOf(edge) == targetCell)) {
        return true;    
      }
    }
    return false;    
  }

  public YAWLCell getTargetOf(Edge edge) {
    return ElementUtilities.getTargetOf(this, edge);
  }

  public YAWLCell getSourceOf(Edge edge) {
    return ElementUtilities.getSourceOf(this, edge);
  }
  
  public boolean acceptsIncommingFlows(YAWLCell vertex) {
    if (vertex instanceof YAWLTask && hasIncommingFlow(vertex)) {
      return false;
    }
    return true;
  }
  
  public boolean hasIncommingFlow(YAWLCell cell) {
    Iterator setIterator = 
      getEdges(this, new Object[] { cell} ).iterator();
    while(setIterator.hasNext()) {
      Edge edge = (Edge) setIterator.next(); 
      if (getTargetOf(edge) == cell ) {
        return true;    
      }
    }
    return false;    
  }
  
  public boolean generatesOutgoingFlows(YAWLCell cell) {
    if (cell instanceof YAWLTask && hasOutgoingFlow(cell)) {
      return false;
    }
    return true;
  }

  public boolean hasOutgoingFlow(YAWLCell cell) {
    Iterator setIterator = 
      getEdges(this, new Object[] {cell}).iterator();
    while(setIterator.hasNext()) {
      Edge edge = (Edge) setIterator.next(); 
      if (getSourceOf(edge) == cell ) {
        return true;    
      }
    }
    return false;    
  }
  
  public void setJoinDecorator(YAWLTask task, int type, int position) {
    HashSet objectsToInsert       = new HashSet();
    HashSet objectsToDelete       = new HashSet();
    ConnectionSet flowsToRedirect = new ConnectionSet();
    ParentMap parentMap           = new ParentMap();

    JoinDecorator oldJoinDecorator     = null;
		JoinDecorator newJoinDecorator     = null;

    YAWLFlowRelation onlyIncommingFlow = null;
    
    if (task.hasJoinDecorator()) {
      
      if (task.getJoinDecorator().getType()     == type && 
          task.getJoinDecorator().getCardinalPosition() == position) {
           return;
      }

      onlyIncommingFlow = task.getJoinDecorator().getOnlyFlow();
      oldJoinDecorator = deleteOldJoinDecorator(task, type, position,
                                                objectsToDelete, parentMap,
                                                flowsToRedirect);
    } else {
      onlyIncommingFlow = task.getOnlyIncommingFlow();
    }
    
    if (position != YAWLTask.NOWHERE && 
        type     != JoinDecorator.NO_TYPE) {

      VertexContainer decoratedTask = 
        getVertexContainer(task, objectsToInsert, parentMap); 

      newJoinDecorator = addNewJoinDecorator(task, type, position,
                                             objectsToInsert, parentMap, 
                                             decoratedTask);
    
      if (onlyIncommingFlow != null) {
        flowsToRedirect.connect(onlyIncommingFlow,
                                onlyIncommingFlow.getSource(),
                                newJoinDecorator.getDefaultPort());
      }
      
      if (oldJoinDecorator != null) {
        reconnectFlowsToNewJoinDecorator(oldJoinDecorator,
                                         newJoinDecorator,
                                         flowsToRedirect);
      }
    }

    applyAllDecoratorChanges(objectsToDelete, 
                             objectsToInsert, 
                             flowsToRedirect, 
                             parentMap);    
  }
  
  private JoinDecorator deleteOldJoinDecorator(YAWLTask task, int type, int position,
                                               HashSet objectsToDelete, ParentMap parentMap,
                                               ConnectionSet flowsToRedirect) {
    JoinDecorator oldJoinDecorator = null;                           
                                        
    parentMap.addEntry(task.getJoinDecorator(),null);
    objectsToDelete.add(task.getJoinDecorator());

    if (position == YAWLTask.NOWHERE || type == JoinDecorator.NO_TYPE) {
      if (task.getJoinDecorator().getOnlyFlow() != null) {
        flowsToRedirect.connect(task.getJoinDecorator().getOnlyFlow(),
                                task.getJoinDecorator().getOnlyFlow().getSource(),
                                task.getPortAt(task.getJoinDecorator().getCardinalPosition()));
      }
    } else {
      oldJoinDecorator = task.getJoinDecorator();
    }
    return oldJoinDecorator;
  }

  private JoinDecorator addNewJoinDecorator(YAWLTask task, int type, int position, 
                                            HashSet objectsToInsert,ParentMap parentMap,
                                            VertexContainer decoratedTask) {

    JoinDecorator joinDecorator = new JoinDecorator(task,type,position);
    
    objectsToInsert.add(joinDecorator);
    parentMap.addEntry(joinDecorator,decoratedTask);
    return joinDecorator;
  }

  private void reconnectFlowsToNewJoinDecorator(JoinDecorator oldJoinDecorator, 
                                                JoinDecorator newJoinDecorator,
                                                ConnectionSet flowsToRedirect) {
    for(int i = 0; i < JoinDecorator.PORT_NUMBER; i++) {
      Set flows = oldJoinDecorator.getPortAtIndex(i).getEdges();
      Iterator flowIterator = flows.iterator();
      while (flowIterator.hasNext()) {
        YAWLFlowRelation flow = (YAWLFlowRelation) flowIterator.next();
        flowsToRedirect.connect(flow,
                                flow.getSource(),
                                newJoinDecorator.getPortAtIndex(i));
      }
    }
  }

  public void setSplitDecorator(YAWLTask task, int type, int position) {
    HashSet objectsToInsert       = new HashSet();
    HashSet objectsToDelete       = new HashSet();
    ConnectionSet flowsToRedirect = new ConnectionSet();
    ParentMap parentMap           = new ParentMap();

    SplitDecorator   oldSplitDecorator = null;
		SplitDecorator   newSplitDecorator = null;
    
    YAWLFlowRelation onlyOutgoingFlow  = null;

    if (task.hasSplitDecorator()) {
      if (task.getSplitDecorator().getType()     == type && 
          task.getSplitDecorator().getCardinalPosition() == position) {
           return;
      }
      
      onlyOutgoingFlow = task.getSplitDecorator().getOnlyFlow();
      oldSplitDecorator = deleteOldSplitDecorator(task, type, position,
                                                  objectsToDelete, parentMap,
                                                  flowsToRedirect);

     } else {
      onlyOutgoingFlow = task.getOnlyOutgoingFlow();
    }

    if (position != YAWLTask.NOWHERE && 
        type     != SplitDecorator.NO_TYPE) {

      VertexContainer decoratedTask = 
        getVertexContainer(task, objectsToInsert, parentMap); 

      newSplitDecorator = addNewSplitDecorator(task, type, position,
                                               objectsToInsert, parentMap, 
                                               decoratedTask);
                               
      if (onlyOutgoingFlow != null) {
        flowsToRedirect.connect(onlyOutgoingFlow,
                                newSplitDecorator.getDefaultPort(),
                                onlyOutgoingFlow.getTarget());
      }
      
      if (oldSplitDecorator != null) {
        reconnectFlowsToNewSplitDecorator(oldSplitDecorator,
                                          newSplitDecorator,
                                          flowsToRedirect);
      }
    }

    applyAllDecoratorChanges(objectsToDelete, 
                             objectsToInsert, 
                             flowsToRedirect, 
                             parentMap);    
  }

  private SplitDecorator deleteOldSplitDecorator(YAWLTask task, int type, int position,
                                                 HashSet objectsToDelete, ParentMap parentMap,
                                                 ConnectionSet flowsToRedirect) {
    SplitDecorator oldSplitDecorator = null;

    parentMap.addEntry(task.getSplitDecorator(),null);
    objectsToDelete.add(task.getSplitDecorator());

    if (position == YAWLTask.NOWHERE || type == SplitDecorator.NO_TYPE) {
      if (task.getSplitDecorator().getOnlyFlow() != null) {
        flowsToRedirect.connect(task.getSplitDecorator().getOnlyFlow(),
                                task.getPortAt(task.getSplitDecorator().getCardinalPosition()),
                                task.getSplitDecorator().getOnlyFlow().getTarget());
      }
    } else {
      oldSplitDecorator = task.getSplitDecorator();
    }
    return oldSplitDecorator;
  }

  private SplitDecorator addNewSplitDecorator(YAWLTask task, int type, int position, 
                                              HashSet objectsToInsert,ParentMap parentMap,
                                              VertexContainer decoratedTask) {
                                                
    SplitDecorator splitDecorator = new SplitDecorator(task,type,position);
    
    objectsToInsert.add(splitDecorator);
    parentMap.addEntry(splitDecorator,decoratedTask);
    return splitDecorator;
  }

  private void reconnectFlowsToNewSplitDecorator(SplitDecorator oldSplitDecorator, 
                                                 SplitDecorator newSplitDecorator,
                                                 ConnectionSet flowsToRedirect) {

    for(int i = 0; i < SplitDecorator.PORT_NUMBER; i++) {
      Set flows = oldSplitDecorator.getPortAtIndex(i).getEdges();
      Iterator flowIterator = flows.iterator();
      while (flowIterator.hasNext()) {
        YAWLFlowRelation flow = (YAWLFlowRelation) flowIterator.next();
        flowsToRedirect.connect(flow,
                                newSplitDecorator.getPortAtIndex(i),
                                flow.getTarget());
      }
    }
  }
  
  private void applyAllDecoratorChanges(HashSet objectsToDelete, 
                                        HashSet objectsToInsert, 
                                        ConnectionSet flowsToRedirect, 
                                        ParentMap parentMap) {
    if (objectsToDelete.size() > 0 || objectsToInsert.size() > 0 || 
        flowsToRedirect.size() > 0 || parentMap.size() > 0) {
      this.insert(objectsToInsert.toArray(),null, flowsToRedirect, parentMap, null); 
      this.remove(getDescendantList(this,objectsToDelete.toArray()).toArray());
    }
  }
  
  public VertexContainer getVertexContainer(YAWLVertex vertex, 
                                            HashSet    objectsToInsert, 
                                            ParentMap  parentMap) {
                                                  
    VertexContainer vertexContainer = (VertexContainer) vertex.getParent();
    if (vertexContainer == null) {
      vertexContainer = new VertexContainer();
      objectsToInsert.add(vertexContainer);
      parentMap.addEntry(vertex, vertexContainer);
    }
    return vertexContainer;
  }
}
