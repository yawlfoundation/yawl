/*
 * Created on 18/12/2003
 * YAWLEditor v1.0 
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

package au.edu.qut.yawl.editor.net;

import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import javax.swing.undo.UndoableEdit;

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
import au.edu.qut.yawl.editor.elements.model.InputCondition;
import au.edu.qut.yawl.editor.elements.model.OutputCondition;

import au.edu.qut.yawl.editor.elements.model.YAWLFlowRelation;
import au.edu.qut.yawl.editor.foundations.ResourceLoader;

import au.edu.qut.yawl.editor.net.utilities.NetUtilities;

import org.jgraph.graph.Port;
import org.jgraph.graph.Edge;

import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.ParentMap;
import org.jgraph.graph.ConnectionSet;

public class NetGraphModel extends DefaultGraphModel {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
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
    decomposition.setLabel(name);
  }

  public String getName() {
    return decomposition.getLabel();
  }
  
  public void setIsStartingNet(boolean isStartingNet) {
    this.isStartingNet = isStartingNet;

    if (getGraph().getFrame() != null) {
      if (isStartingNet) {
        getGraph().getFrame().setClosable(false);
        getGraph().getFrame().setFrameIcon(
            ResourceLoader.getImageAsIcon("/au/edu/qut/yawl/editor/resources/menuicons/" +
                "StartingNetInternalFrame.gif")  
        );
      } else {
        getGraph().getFrame().setClosable(true);
        getGraph().getFrame().setFrameIcon(
            ResourceLoader.getImageAsIcon("/au/edu/qut/yawl/editor/resources/menuicons/" +
                "SubNetInternalFrame.gif")
        );
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
    NetUtilities.resizeNetIfNecessary(getGraph());
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
      getDescendants(this,cells.toArray())
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
      YAWLTask viewingTask = null;
      if (task.equals(this.getGraph().viewingCancellationSetOf())) {
        viewingTask = task;
        this.getGraph().changeCancellationSet(null);
      }
      CancellationSet set = task.getCancellationSet();
      for(int j = 0; j < cellsAsObjects.length; j++) {
        if (cellsAsObjects[j] instanceof YAWLCell) {
          set.removeMember((YAWLCell)cellsAsObjects[j]);
        }
      }
      if (viewingTask != null) {
        this.getGraph().changeCancellationSet(viewingTask);
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
  
  public boolean acceptsTarget(Object edge, Object port) {
    return connectionAllowable((Port) ((Edge) edge).getSource(), (Port) port, (Edge) edge);
  }

  public boolean acceptsSource(Object edge, Object port) {
    return connectionAllowable((Port) port, (Port) ((Edge) edge).getTarget(), (Edge) edge);
  }
  
  /**
   * Returns <code>true</code> if a flow relation can validly be drawn
   * from the source port to the target port, <code>false</code> otherwise.
   * @param source The source port
   * @param target The target port
   * @return <code>true</code> if conection allowed.
   */
  
  public boolean connectionAllowable(Port source, Port target) {
    return connectionAllowable(source, target, null);
  }
  
  /**
   * This method returns <code>true</code> if a flow relation can validly be
   * drawn from the source port to the target port, ignoring for the time being
   * that the specified edge actually exists.
   * <p>
   * This takes care of the fact that the checks for whether an edge can be connected
   * to a port via the JGraphModel methods of acceptsTarget() and acceptsSource().
   * Unfortunately, the methods acceptsTarget() and acceptsSource() of JGraphModel 
   * are called AFTER the edge has been reconnected. Therefore, we need to ignore 
   * the already connected edge in deciding if connections are valid.
   * @param source The source port
   * @param target The target port
   * @param edgeToIgnore The edge to ignore
   * @return <code>true</code> if connection allowed.
   */
  
  private boolean connectionAllowable(Port source, Port target, Edge edgeToIgnore) {
    boolean rulesAdheredTo = true;
    
    YAWLCell sourceCell = (YAWLCell) getParent(source);
    YAWLCell targetCell = (YAWLCell) getParent(target);
    
    if(sourceCell == targetCell) {
      // System.out.println("source and target are the same cell");
      rulesAdheredTo = false;      
    }
    
    if (source == null || target == null) {
      // System.out.println("disconnected flows are not allowed.");
      return false;
    }
    
    if (taskPortIsAlreadyOccupied(source, edgeToIgnore)) {
      rulesAdheredTo = false;
    }

    if (taskPortIsAlreadyOccupied(target, edgeToIgnore)) {
      rulesAdheredTo = false;
    }
    
    if (taskHasSplitDecorator(source)) {
      rulesAdheredTo = false;
    }

    if (taskHasJoinDecorator(target)) {
      rulesAdheredTo = false;
    }

    
    if (sourceCell instanceof YAWLCondition &&
        targetCell instanceof YAWLCondition) {
      // System.out.println("source and target are both conditions");
      rulesAdheredTo = false;      
    }
    if (areConnectedAsSourceAndTarget(sourceCell, targetCell, edgeToIgnore)) { 
      // System.out.println("source and target are already connected");
      rulesAdheredTo = false;                  
    }
    if (!generatesOutgoingFlows(sourceCell, edgeToIgnore)) {
      // System.out.println("source cannot generate outgoing flows");
      rulesAdheredTo = false;                  
    }
    if (!acceptsIncommingFlows(targetCell, edgeToIgnore)) {
      // System.out.println("target cannot accept incoming flows");
      rulesAdheredTo = false;                  
    }
    if (selfReferencingTaskNotUsingLongeEdgePorts(source, target)) {
      // System.out.println("self-referencing task cannot use its long edge ports");
      rulesAdheredTo = false;
    }
    return rulesAdheredTo;
  }
  
  private boolean taskHasSplitDecorator(Port port) {
    YAWLCell parentCell = (YAWLCell) getParent(port);
    
    if (!(parentCell instanceof YAWLTask)) {
      return false;
    }
    
    YAWLTask task = (YAWLTask) parentCell;
    if (task.hasSplitDecorator()) {
      return true;
    }
    
    return false;
  }

  private boolean taskHasJoinDecorator(Port port) {
    YAWLCell parentCell = (YAWLCell) getParent(port);
    
    if (!(parentCell instanceof YAWLTask)) {
      return false;
    }
    
    YAWLTask task = (YAWLTask) parentCell;
    if (task.hasJoinDecorator()) {
      return true;
    }
    
    return false;
  }

  
  private boolean taskPortIsAlreadyOccupied(Port port, Edge edgeToIgnore) {
    YAWLCell parentCell = (YAWLCell) getParent(port);
    
    if (!(parentCell instanceof YAWLTask)) {
      return false;
    }

    Iterator portIterator = port.edges();
    while(portIterator.hasNext()) {
      Edge portEdge = (Edge) portIterator.next();
      if (!portEdge.equals(edgeToIgnore)) {
        return true;
      }
    } 

    return false;
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
    return areConnectedAsSourceAndTarget(sourceCell, targetCell, null);
  }
  
  private boolean areConnectedAsSourceAndTarget(YAWLCell sourceCell, 
                                               YAWLCell targetCell,
                                               Edge edgeToIgnore) {
    Iterator setIterator = 
      getEdges(this, new Object[] {sourceCell}).iterator();
    while(setIterator.hasNext()) {
      Edge edge = (Edge) setIterator.next(); 
      if ((getTargetOf(edge) == targetCell && !edge.equals(edgeToIgnore))) {
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
    return acceptsIncommingFlows(vertex, null);
  }
  
  public boolean acceptsIncommingFlows(YAWLCell cell, Edge edge) {
    if (cell instanceof InputCondition) {
      return false;
    }
    if (cell instanceof YAWLTask && hasIncommingFlow(cell, edge)) {
      return false;
    }
    if (cell instanceof SplitDecorator) {
      return false;
    }
    return true;
  }
  
  public boolean hasIncommingFlow(YAWLCell cell) {
    return hasIncommingFlow(cell, null);
  }
  
  public boolean hasIncommingFlow(YAWLCell cell, Edge edgeToIgnore) {
    Iterator setIterator = 
      getEdges(this, new Object[] { cell} ).iterator();
    while(setIterator.hasNext()) {
      Edge edge = (Edge) setIterator.next(); 
      if (getTargetOf(edge) == cell && !edge.equals(edgeToIgnore)) {
        return true;    
      }
    }
    return false;    
  }

  public boolean generatesOutgoingFlows(YAWLCell cell) {
    return generatesOutgoingFlows(cell, null);
  }
  
  public boolean generatesOutgoingFlows(YAWLCell cell, Edge edge) {
    if (cell instanceof OutputCondition) {
      return false;
    }
    if (cell instanceof YAWLTask && hasOutgoingFlow(cell, edge)) {
      return false;
    }
    if (cell instanceof JoinDecorator) {
      return false;
    }
    return true;
  }

  public boolean hasOutgoingFlow(YAWLCell cell) {
    return hasOutgoingFlow(cell, null);
  }
  
  public boolean hasOutgoingFlow(YAWLCell cell, Edge edgeToIgnore) {
    Iterator setIterator = 
      getEdges(this, new Object[] {cell}).iterator();
    while(setIterator.hasNext()) {
      Edge edge = (Edge) setIterator.next(); 
      if (getSourceOf(edge) == cell &&  !edge.equals(edgeToIgnore)) {
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
      this.remove(getDescendants(this,objectsToDelete.toArray()).toArray());
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
  
  public void edit(Map attributes, ConnectionSet cs, ParentMap pm, UndoableEdit[] e) {
    super.edit(attributes, cs, pm, e);
    NetUtilities.resizeNetIfNecessary(getGraph());
  }
  
  public void insert(Object[] roots, Map attributes, ConnectionSet cs,
                       ParentMap pm, UndoableEdit[] edits) {
    super.insert(roots, attributes, cs, pm, edits);
    NetUtilities.resizeNetIfNecessary(getGraph());
  }
}
