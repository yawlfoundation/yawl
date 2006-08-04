/*
 * Created on 27/02/2004
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

package au.edu.qut.yawl.editor.foundations;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;

import java.awt.Rectangle;
import java.awt.Point;

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

	/**
   * 
   */
  private static final long serialVersionUID = 1L;

  private transient NetGraphModel graphModel;

  private static final int SOURCE = 0;
  private static final int TARGET = 1;

  /* ALL attributes of this object are to be stored in 
   * serializationProofAttributeMap, meaning we won't get problems
   * with incompatible XML serializations as we add new attributes
   * in the future. 
   */
  
  private HashMap serializationProofAttributeMap = new HashMap();
  
  public ArchivableNetState() {
    setStartingNetFlag(false);
    setIconified(false);
    setMaximised(false);
  }

  public ArchivableNetState(NetGraphModel graphModel) {
    this.graphModel = graphModel;
    
    graphModel.getGraph().getGraphLayoutCache().reload();

    generateCellData();

    setCellViewAttributes(
        GraphConstants.createAttributes(
            getCells(), 
            graphModel.getGraph().getGraphLayoutCache()
        )
    );

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
    setScale(graphModel.getGraph().getScale());
    setVisibleRectangle(
      graphModel.getGraph().getVisibleRect()    
    );
    setDecomposition(graphModel.getDecomposition());
  }
  
  public void setSerializationProofAttributeMap(HashMap map) {
    this.serializationProofAttributeMap = map;
  }
  
  public HashMap getSerializationProofAttributeMap() {
    return this.serializationProofAttributeMap;
  }
  
  private void generateCellData() {
    Object[] cells = graphModel.getGraph().getDescendants(graphModel.getGraph().getRoots());

    // TODO: is there a 5.7.3.1 equivalent for this JGraph 3.1 method?
    // cells = graphModel.getGraph().getGraphLayoutCache().order(cells);
    
    setCells(cells);
  }

  public void setCells(Object[] cells) {
    serializationProofAttributeMap.put("cells",cells);
  }
  
  public Object[] getCells() {
    return (Object[]) serializationProofAttributeMap.get("cells");
  }

  public void setStartingNetFlag(boolean startingNetFlag) {
    serializationProofAttributeMap.put("startingNetFlag",new Boolean(startingNetFlag));
  }
  
  public boolean getStartingNetFlag() {
    return ((Boolean) serializationProofAttributeMap.get("startingNetFlag")).booleanValue();
  }
  
  public boolean isStartingNet() {
    return getStartingNetFlag();
  }

  public void setIconified(boolean iconified) {
    serializationProofAttributeMap.put("iconified",new Boolean(iconified));
  }
  
  public boolean getIconified() {
    return ((Boolean) serializationProofAttributeMap.get("iconified")).booleanValue();
  }
  
  private void generateConnectionSetData() {
    HashMap connectionHashMap = new HashMap();
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
    setConnectionHashMap(connectionHashMap);
  }
  
  public HashMap getConnectionHashMap() {
    return (HashMap) serializationProofAttributeMap.get("connectionMap");
  }
  
  public void setConnectionHashMap(HashMap connectionHashMap) {
    serializationProofAttributeMap.put("connectionMap",connectionHashMap);
  }
  
  public ConnectionSet toConnectionSet(HashMap connectionHashMap) {
    ConnectionSet connectionSet = new ConnectionSet();
    Object[] edges = connectionHashMap.keySet().toArray();
    for(int i = 0; i < edges.length; i++) {
      Object[] sourceAndTarget = ((Vector) getParentMap().get(edges[i])).toArray();
        connectionSet.connect(edges[i],sourceAndTarget[SOURCE], sourceAndTarget[TARGET]);
    }
    return connectionSet;    
  }
  
  private void generateParentMapData() {
    HashMap parentHashMap = new HashMap();
    Object[] cells = getCells();
    for(int i = 0; i < cells.length; i++) {
      Vector children = new Vector();
      parentHashMap.put(cells[i], children);
      for(int j = 0; j < cells.length; j++) {
        if (graphModel.getParent(cells[j]) == cells[i]) {
          children.add(cells[j]); 
        }
      }
    }
    setParentMap(parentHashMap);
  }
  
  public HashMap getParentMap() {
    return (HashMap) serializationProofAttributeMap.get("parentMap");
  }
  
  public void setParentMap(HashMap parentHashMap) {
    serializationProofAttributeMap.put("parentMap",parentHashMap);
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
    return (Map) serializationProofAttributeMap.get("cellViewAttributes");
  }
  
  public void setCellViewAttributes(Map cellViewAttributes) {
    serializationProofAttributeMap.put("cellViewAttributes",cellViewAttributes);
  }
  
  public Decomposition getDecomposition() {
    return (Decomposition) serializationProofAttributeMap.get("decomposition");
  }
  
  public void setDecomposition(Decomposition decomposition) {
    serializationProofAttributeMap.put("decomposition",decomposition);
  }
  
  public Rectangle getBounds() {
    return (Rectangle) serializationProofAttributeMap.get("bounds");
  }
  
  public void setBounds(Rectangle bounds) {
    serializationProofAttributeMap.put("bounds",bounds);
  }

  public Rectangle getIconBounds() {
    return (Rectangle) serializationProofAttributeMap.get("iconBounds");
  }
  
  public void setIconBounds(Rectangle iconBounds) {
    serializationProofAttributeMap.put("iconBounds",iconBounds);
  }
  
  public void setMaximised(boolean maximised) {
    serializationProofAttributeMap.put("maximised",new Boolean(maximised));
  }
  
  public boolean getMaximised() {
    return ((Boolean) serializationProofAttributeMap.get("maximised")).booleanValue();
  }

  public void setTriggeringTaskOfVisibleCancellationSet(YAWLTask task) {
    serializationProofAttributeMap.put("triggeringTaskOfVisibleCancellationSet",task);
  }
  
  public YAWLTask getTriggeringTaskOfVisibleCancellationSet() {
    return (YAWLTask) serializationProofAttributeMap.get("triggeringTaskOfVisibleCancellationSet");
  }
  
  public double getScale() {
    try {
      return ((Double) serializationProofAttributeMap.get("scale")).doubleValue();
    } catch (Exception e) {
      return 0;
    }
  }
  
  public void setScale(double scale) {
    serializationProofAttributeMap.put("scale",new Double(scale));
  }
  
  public Rectangle getVisibleRectangle() {
    return (Rectangle) serializationProofAttributeMap.get("visibleRectangle");
  }
  
  public void setVisibleRectangle(Rectangle rectangle) {
    serializationProofAttributeMap.put("visibleRectangle", rectangle);
  }
}
