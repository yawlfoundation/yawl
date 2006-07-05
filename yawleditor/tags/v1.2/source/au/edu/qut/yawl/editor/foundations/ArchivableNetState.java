/*
 * Created on 27/02/2004
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

package au.edu.qut.yawl.editor.foundations;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;

import java.awt.Rectangle;

import au.edu.qut.yawl.editor.data.Decomposition;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.elements.model.YAWLFlowRelation;
import au.edu.qut.yawl.editor.elements.model.YAWLPort;
import au.edu.qut.yawl.editor.net.NetGraphModel;

import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.ParentMap;
import org.jgraph.graph.ConnectionSet;


/**
 * @author bradforl
 */
public class ArchivableNetState implements Serializable {

	private transient NetGraphModel graphModel;

  private static final int SOURCE = 0;
  private static final int TARGET = 1;

  private Object[] cells; 
  private Map cellViewAttributes;
  private HashMap parentHashMap;
  private HashMap connectionHashMap;
  
  private YAWLTask triggeringTaskOfVisibleCancellationSet;
  private boolean startingNetFlag;

  private Decomposition netDecomposition;
  
  private Rectangle bounds;

	private boolean iconified;
	private Rectangle iconBounds;
	
	private boolean maximised;
  
  public ArchivableNetState() {}

  public ArchivableNetState(NetGraphModel graphModel) {
    this.graphModel = graphModel;
    
    graphModel.getGraph().getGraphLayoutCache().reload();

    generateCellData();

    setCellViewAttributes(GraphConstants.createAttributes(cells, graphModel.getGraph().getGraphLayoutCache()));

    generateConnectionSetData();
    generateParentMapData();

    setBounds(graphModel.getGraph().getFrame().getNormalBounds());
    setIconified(graphModel.getGraph().getFrame().isIcon());
    if (getIconified()) {
    	setIconBounds(graphModel.getGraph().getFrame().getDesktopIcon().getBounds());
    }
    setMaximised(graphModel.getGraph().getFrame().isMaximum());
    setTriggeringTaskOfVisibleCancellationSet(
      graphModel.getGraph().getCancellationSetModel().getTriggeringTask()
    );
    setStartingNetFlag(graphModel.isStartingNet());
    setDecomposition(graphModel.getDecomposition());
  }
  
  private void generateCellData() {
    Object[] cells = graphModel.getGraph().getDescendants(graphModel.getGraph().getRoots());
    cells = graphModel.getGraph().getGraphLayoutCache().order(cells);
    
    setCells(cells);
  }

  public Object[] getCells() {
    return this.cells;
  }
  
  public void setCells(Object[] cells) {
    this.cells = cells;
  }
  
  public boolean getStartingNetFlag(){
    return this.startingNetFlag;
  }
  
  public void setStartingNetFlag(boolean startingNetFlag) {
    this.startingNetFlag = startingNetFlag;
  }
  
  public boolean isStartingNet() {
    return this.startingNetFlag;
  }
  
  public void setIconified(boolean iconified) {
    this.iconified = iconified;
  }
  
  public boolean getIconified() {
    return this.iconified;
  }
  
  private void generateConnectionSetData() {
    connectionHashMap = new HashMap();
    Object[] edges = NetGraphModel.getEdges(graphModel, getCells()).toArray();
    for(int i = 0; i < edges.length; i++) {
      YAWLFlowRelation edge = (YAWLFlowRelation) edges[i];
      YAWLPort source = (YAWLPort) edge.getSource();
      YAWLPort target = (YAWLPort) edge.getTarget();
      
      Vector sourceAndTarget = new Vector();
      sourceAndTarget.add(source);
      sourceAndTarget.add(target);
      
      connectionHashMap.put(edge, sourceAndTarget);
    }
  }
  
  public HashMap getConnectionHashMap() {
    return connectionHashMap;
  }
  
  public void setConnectionHashMap(HashMap connectionHashMap) {
    this.connectionHashMap = connectionHashMap;
  }
  
  public ConnectionSet toConnectionSet(HashMap connectionHashMap) {
    ConnectionSet connectionSet = new ConnectionSet();
    Object[] edges = connectionHashMap.keySet().toArray();
    for(int i = 0; i < edges.length; i++) {
      Object[] sourceAndTarget = ((Vector) parentHashMap.get(edges[i])).toArray();
        connectionSet.connect(edges[i],sourceAndTarget[SOURCE], sourceAndTarget[TARGET]);
    }
    return connectionSet;    
  }
  
  private void generateParentMapData() {
    parentHashMap = new HashMap();
    for(int i = 0; i < cells.length; i++) {
      Vector children = new Vector();
      parentHashMap.put(cells[i], children);
      for(int j = 0; j < cells.length; j++) {
        if (graphModel.getParent(cells[j]) == cells[i]) {
          children.add(cells[j]); 
        }
      }
    }
  }
  
  public HashMap getParentMaps() {
    return this.parentHashMap;
  }
  
  public void setParentMaps(HashMap parentHashMap) {
    this.parentHashMap = parentHashMap;
  }
  
  public ParentMap toParentMap(HashMap parentHashMap) {
    ParentMap parentMap = new ParentMap();
    Object[] parents = parentHashMap.keySet().toArray();
    for(int i = 0; i < parents.length; i++) {
      Object[] children = ((Vector) parentHashMap.get(parents[i])).toArray();
      for(int j = 0; j < children.length; j++) {
        parentMap.addEntry(children[j],parents[i]);
      }
    }
    return parentMap;
  }

  public Map getCellViewAttributes() {
    return this.cellViewAttributes;
  }
  
  public void setCellViewAttributes(Map cellViewAttributes) {
    this.cellViewAttributes = cellViewAttributes;
  }
  
  public Decomposition getDecomposition() {
    return this.netDecomposition;
  }
  
  public void setDecomposition(Decomposition decomposition) {
    this.netDecomposition = decomposition;
  }
  
  public Rectangle getBounds() {
    return this.bounds;
  }
  
  public void setBounds(Rectangle bounds) {
    this.bounds = bounds;
  }

	public Rectangle getIconBounds() {
		return this.iconBounds;
	}
  
  public void setIconBounds(Rectangle iconBounds) {
  	this.iconBounds = iconBounds;
  }
  
  public void setMaximised(boolean maximised) {
  	this.maximised = maximised;
  }
  
  public boolean getMaximised() {
  	return this.maximised;
  }
  
  public YAWLTask getTriggeringTaskOfVisibleCancellationSet() {
  	return this.triggeringTaskOfVisibleCancellationSet;
  }
  
  public void setTriggeringTaskOfVisibleCancellationSet(YAWLTask task) {
  	this.triggeringTaskOfVisibleCancellationSet = task;
  }
}
