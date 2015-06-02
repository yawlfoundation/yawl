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

package org.yawlfoundation.yawl.editor.ui.net;

import org.jgraph.graph.*;
import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.core.controlflow.YCompoundFlow;
import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandlerException;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetCellUtilities;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.ui.plugin.YPluginHandler;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.YNetElementEdit;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import java.util.*;

public class NetGraphModel extends DefaultGraphModel implements Comparable<NetGraphModel> {

    private final NetGraph _graph;
    private YDecomposition _decomposition;        // the YNet
    private final YSpecificationHandler _specificationHandler = SpecificationModel.getHandler();
    private List<Object> _lastClonedCells;

    public NetGraphModel(NetGraph graph) {
        super();
        _graph = graph;
    }

    public NetGraph getGraph() {
        return _graph;
    }

    public void setName(String name) {
        _decomposition.setID(name);
    }

    public String getName() {
        return _decomposition.getID();
    }

    public void setIsRootNet(boolean isRootNet) {
        try {
            if (isRootNet) {
                _specificationHandler.getControlFlowHandler().setRootNet(
                        (YNet) _decomposition);
            }
            YAWLEditor.getNetsPane().resetRootNet();
        }
        catch (YControlFlowHandlerException ycfhe) {
            // do nothing, since no spec is loaded
        }
    }

    public boolean isRootNet() {
        return _specificationHandler.getControlFlowHandler().getRootNet().equals(_decomposition);
    }

    public YDecomposition getDecomposition() {
        return _decomposition;
    }

    public void setDecomposition(YDecomposition decomposition) {
        _decomposition = decomposition;
    }

    public void setExternalDataGateway(String gateway) {
        ((YNet) _decomposition).setExternalDataGateway(gateway);
    }

    public String getExternalDataGateway() {
        return ((YNet) _decomposition).getExternalDataGateway();
    }


    /*****************************************************************************/

    public Set getConnectingFlows(Object[] cells) {
        return getEdges(this, cells);
    }

    public Set<Object> removeCells(Object[] cells) {
        return removeCellsAndEdges(getRemovableCellsOf(cells));
    }

    private Set<Object> getRemovableCellsOf(Object[] cells) {
        Set<Object> removableCells = new HashSet<Object>();
        for (Object o : cells) {
            if (o instanceof YAWLCell) {
                if (((YAWLCell) o).isRemovable()) {
                    removableCells.add(o);
                }
            }
            else {
                removableCells.add(o);
            }
        }
        return removableCells;
    }

    private Set<Object> removeCellsAndEdges(final Set<Object> cells) {
        Set<Object> cellsAndTheirEdges = new HashSet<Object>(cells);
        cellsAndTheirEdges.addAll(getDescendants(this, cells.toArray()));
        cellsAndTheirEdges.addAll(getEdges(this, cells.toArray()));
        removeCellsFromCancellationSets(cellsAndTheirEdges);
        YPluginHandler.getInstance().elementsRemoved(this, cellsAndTheirEdges);

        super.remove(cellsAndTheirEdges.toArray());
//        NetGraphModelEdit edit = createRemoveEdit(cellsAndTheirEdges.toArray());
//    	if (edit != null) {
//    		edit.execute();
//    		postEdit(edit);
//    	}

        compressFlowPriorities(
                NetUtilities.getTasksRequiringFlowPredicates(
                        cellsAndTheirEdges
                )
        );
        return cellsAndTheirEdges;
    }

    private void compressFlowPriorities(Set<YAWLTask> tasksRequiringPredicates) {
        for (YAWLTask task : tasksRequiringPredicates) {
            task.getSplitDecorator().compressFlowPriorities();
        }
    }


    private void removeCellsFromCancellationSets(final Set cells) {
        for (YAWLTask taskWithCancellationSet : NetUtilities.getTasksWithCancellationSets(this)) {
            YAWLTask viewingTask = null;
            if (taskWithCancellationSet.equals(getGraph().viewingCancellationSetOf())) {
                viewingTask = taskWithCancellationSet;
                getGraph().changeCancellationSet(null);
            }
            HashSet<YAWLCell> hashset = new HashSet<YAWLCell>();
            for (Object setMember : taskWithCancellationSet.getCancellationSet().getMembers()) {
                if (!cells.contains(setMember)) {
                    hashset.add((YAWLCell) setMember);
                }
            }
            taskWithCancellationSet.getCancellationSet().setMembers(hashset);
            if (viewingTask != null) {
                getGraph().changeCancellationSet(viewingTask);
            }
        }
    }
    

    public Map cloneCells(Object[] cells) {
        List<Object> clones = new ArrayList<Object>();

        for (Object cell : cells) {
            if (cell instanceof YAWLCell && ((YAWLCell) cell).isCopyable()) {
//                YAWLCell yCell = (YAWLCell) cell;
//                if (yCell.isCopyable()) {
                    clones.add(cell);
//                }
            } else {
                clones.add(cell);
            }
        }
        Map map = super.cloneCells(clones.toArray());
        _lastClonedCells = new ArrayList<Object>(map.values());
        return map;
    }

    public List<Object> getLastClonedCells() { return _lastClonedCells; }

    public boolean acceptsTarget(Object edge, Object port) {
        return connectionAllowable((Port) ((Edge) edge).getSource(), (Port) port, (Edge) edge);
    }

    public boolean acceptsSource(Object edge, Object port) {
        return connectionAllowable((Port) port, (Port) ((Edge) edge).getTarget(), (Edge) edge);
    }

    /**
     * Connects or disconnects the edge and port in this model. Overridden so we can
     * update the YFLows on moves and deletes
     */
    protected void connect(Object edge, Object port, boolean isSource, boolean insert) {
        super.connect(edge, port, isSource, insert);
        if (! insert) return;     // only interested in inserts

        YAWLFlowRelation flow = (YAWLFlowRelation) edge;

         // an insert with a null port denotes a flow removal
        if (port == null) {
            flow.getYFlow().detach();
        }
        else {
            YAWLPort yPort = (YAWLPort) port;
            YAWLVertex vertex = NetCellUtilities.getVertexFromCell(yPort.getParent());
            YCompoundFlow yFlow = flow.getYFlow();

            // if the source or target has changed, update the YFlow
            if (isSource && ! flow.getSourceID().equals(vertex.getID())) {
                flow.setYFlow(yFlow.moveSourceTo(vertex.getYAWLElement()));
            }
            else if (! (isSource || flow.getTargetID().equals(vertex.getID()))) {
                flow.setYFlow(yFlow.moveTargetTo(vertex.getYAWLElement()));
            }
        }
    }

    /**
     * Returns <code>true</code> if a flow relation can validly be drawn
     * from the source port to the target port, <code>false</code> otherwise.
     *
     * @param source The source port
     * @param target The target port
     * @return <code>true</code> if connection allowed.
     */

    public boolean connectionAllowable(Port source, Port target) {
        return connectionAllowable(source, target, null);
    }

    /**
     * This method returns <code>true</code> if a flow relation can validly be
     * drawn from the source port to the target port, ignoring for the time being
     * that the specified edge actually exists.
     * <p/>
     * This takes care of the fact that the checks for whether an edge can be connected
     * to a port via the JGraphModel methods of acceptsTarget() and acceptsSource().
     * Unfortunately, the methods acceptsTarget() and acceptsSource() of JGraphModel
     * are called AFTER the edge has been reconnected. Therefore, we need to ignore
     * the already connected edge in deciding if connections are valid.
     *
     * @param source       The source port
     * @param target       The target port
     * @param edgeToIgnore The edge to ignore
     * @return <code>true</code> if connection allowed.
     */

    private boolean connectionAllowable(Port source, Port target, Edge edgeToIgnore) {
        if (source == null || target == null) return false;

        YAWLCell sourceCell = (YAWLCell) getParent(source);
        YAWLCell targetCell = (YAWLCell) getParent(target);

        if (sourceCell == targetCell) return false;
        if (taskPortIsAlreadyOccupied(source, edgeToIgnore)) return false;
        if (taskPortIsAlreadyOccupied(target, edgeToIgnore)) return false;
        if (taskHasSplitDecorator(source)) return false;
        if (taskHasJoinDecorator(target)) return false;
        if (sourceCell instanceof Condition && targetCell instanceof Condition) {
            return false;
        }
        if (areConnectedAsSourceAndTarget(sourceCell, targetCell, edgeToIgnore)) {
            return false;
        }
        if (!generatesOutgoingFlows(sourceCell, edgeToIgnore)) return false;
        if (!acceptsIncomingFlows(targetCell, edgeToIgnore)) return false;
        if (selfReferencingTaskNotUsingLongeEdgePorts(source, target)) return false;
        return true;
    }

    private boolean taskHasSplitDecorator(Port port) {
        YAWLCell parentCell = (YAWLCell) getParent(port);
        return parentCell instanceof YAWLTask && ((YAWLTask) parentCell).hasSplitDecorator();
    }

    private boolean taskHasJoinDecorator(Port port) {
        YAWLCell parentCell = (YAWLCell) getParent(port);
        return parentCell instanceof YAWLTask && ((YAWLTask) parentCell).hasJoinDecorator();
    }


    private boolean taskPortIsAlreadyOccupied(Port port, Edge edgeToIgnore) {
        YAWLCell parentCell = (YAWLCell) getParent(port);

        if (! (parentCell instanceof YAWLTask)) {
            return false;
        }

        Iterator portIterator = port.edges();
        while (portIterator.hasNext()) {
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

        return sourceAsDecoratorPort.getDecorator().getTask().equals(
                targetAsDecoratorPort.getDecorator().getTask()) &&
                !(sourceAsDecoratorPort.isLongEdgePort() &&
                        targetAsDecoratorPort.isLongEdgePort());

    }

    public boolean areConnected(YAWLCell sourceCell, YAWLCell targetCell) {
        return areConnectedAsSourceAndTarget(sourceCell, targetCell) ||
                areConnectedAsSourceAndTarget(targetCell, sourceCell);
    }

    public boolean areConnectedAsSourceAndTarget(YAWLCell sourceCell,
                                                 YAWLCell targetCell) {
        return areConnectedAsSourceAndTarget(sourceCell, targetCell, null);
    }

    private boolean areConnectedAsSourceAndTarget(YAWLCell sourceCell,
                                                  YAWLCell targetCell,
                                                  Edge edgeToIgnore) {
        for (Object o : getEdges(this, new Object[]{sourceCell})) {
            Edge edge = (Edge) o;
            if ((getTargetOf(edge) == targetCell && !edge.equals(edgeToIgnore))) {
                return true;
            }
        }
        return false;
    }

    public YAWLCell getTargetOf(Edge edge) {
        return (YAWLCell) getTargetVertex(this, edge);
    }

    public YAWLCell getSourceOf(Edge edge) {
        return (YAWLCell) getSourceVertex(this, edge);
    }

    public boolean acceptsIncomingFlows(YAWLCell vertex) {
        return acceptsIncomingFlows(vertex, null);
    }

    public boolean acceptsIncomingFlows(YAWLCell cell, Edge edge) {
        return ! ((cell instanceof InputCondition) || (cell instanceof SplitDecorator) ||
                  (cell instanceof YAWLTask && hasIncomingFlow(cell, edge)));
    }

    public boolean hasIncomingFlow(YAWLCell cell) {
        return hasIncomingFlow(cell, null);
    }

    public boolean hasIncomingFlow(YAWLCell cell, Edge edgeToIgnore) {
        for (Object o : getEdges(this, new Object[]{cell})) {
            Edge edge = (Edge) o;
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
        return ! ((cell instanceof OutputCondition) || (cell instanceof JoinDecorator) ||
                  (cell instanceof YAWLTask && hasOutgoingFlow(cell, edge)));
    }

    public boolean hasOutgoingFlow(YAWLCell cell) {
        return hasOutgoingFlow(cell, null);
    }

    public boolean hasOutgoingFlow(YAWLCell cell, Edge edgeToIgnore) {
        for (Object o : getEdges(this, new Object[]{cell})) {
            Edge edge = (Edge) o;
            if (getSourceOf(edge) == cell && !edge.equals(edgeToIgnore)) {
                return true;
            }
        }
        return false;
    }

    public void setJoinDecorator(YAWLTask task, int type, int position) {
        HashSet objectsToInsert = new HashSet();
        HashSet objectsToDelete = new HashSet();
        ConnectionSet flowsToRedirect = new ConnectionSet();
        ParentMap parentMap = new ParentMap();

        JoinDecorator oldJoinDecorator = null;
        JoinDecorator newJoinDecorator = null;

        YAWLFlowRelation onlyIncomingFlow = null;

        if (task.hasJoinDecorator()) {

            if (task.getJoinDecorator().getType() == type &&
                    task.getJoinDecorator().getCardinalPosition() == position) {
                return;
            }

            onlyIncomingFlow = task.getJoinDecorator().getOnlyFlow();
            oldJoinDecorator = deleteOldJoinDecorator(task, type, position,
                    objectsToDelete, parentMap,
                    flowsToRedirect);
        } else {
            onlyIncomingFlow = task.getOnlyIncomingFlow();
        }

        if (position != YAWLTask.NOWHERE &&
                type != JoinDecorator.NO_TYPE) {

            VertexContainer decoratedTask =
                    getVertexContainer(task, objectsToInsert, parentMap);

            newJoinDecorator = addNewJoinDecorator(task, type, position,
                    objectsToInsert, parentMap,
                    decoratedTask);

            if (onlyIncomingFlow != null) {
                flowsToRedirect.connect(onlyIncomingFlow,
                        onlyIncomingFlow.getSource(),
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

        parentMap.addEntry(task.getJoinDecorator(), null);
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
                                              HashSet objectsToInsert, ParentMap parentMap,
                                              VertexContainer decoratedTask) {

        JoinDecorator joinDecorator = new JoinDecorator(task, type, position);

        objectsToInsert.add(joinDecorator);
        parentMap.addEntry(joinDecorator, decoratedTask);
        return joinDecorator;
    }

    private void reconnectFlowsToNewJoinDecorator(JoinDecorator oldJoinDecorator,
                                                  JoinDecorator newJoinDecorator,
                                                  ConnectionSet flowsToRedirect) {
        for (int i = 0; i < JoinDecorator.PORT_NUMBER; i++) {
            Set flows = oldJoinDecorator.getPortAtIndex(i).getEdges();
            for (Object flow1 : flows) {
                YAWLFlowRelation flow = (YAWLFlowRelation) flow1;
                flowsToRedirect.connect(flow,
                        flow.getSource(),
                        newJoinDecorator.getPortAtIndex(i));
            }
        }
    }

    public void setSplitDecorator(YAWLTask task, int type, int position) {
        HashSet objectsToInsert = new HashSet();
        HashSet objectsToDelete = new HashSet();
        ConnectionSet flowsToRedirect = new ConnectionSet();
        ParentMap parentMap = new ParentMap();

        SplitDecorator oldSplitDecorator = null;
        SplitDecorator newSplitDecorator = null;

        YAWLFlowRelation onlyOutgoingFlow = null;

        if (task.hasSplitDecorator()) {
            if (task.getSplitDecorator().getType() == type &&
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
                type != SplitDecorator.NO_TYPE) {

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

        parentMap.addEntry(task.getSplitDecorator(), null);
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
                                                HashSet objectsToInsert, ParentMap parentMap,
                                                VertexContainer decoratedTask) {

        SplitDecorator splitDecorator = new SplitDecorator(task, type, position);

        objectsToInsert.add(splitDecorator);
        parentMap.addEntry(splitDecorator, decoratedTask);
        return splitDecorator;
    }

    private void reconnectFlowsToNewSplitDecorator(SplitDecorator oldSplitDecorator,
                                                   SplitDecorator newSplitDecorator,
                                                   ConnectionSet flowsToRedirect) {

        for (int i = 0; i < SplitDecorator.PORT_NUMBER; i++) {
            Set flows = oldSplitDecorator.getPortAtIndex(i).getEdges();
            for (Object o : flows) {
                YAWLFlowRelation flow = (YAWLFlowRelation) o;
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
            this.insert(objectsToInsert.toArray(), null, flowsToRedirect, parentMap, null);
            this.removeCells(getDescendants(this, objectsToDelete.toArray()).toArray());
        }
    }

    public VertexContainer getVertexContainer(YAWLVertex vertex,
                                              HashSet objectsToInsert,
                                              ParentMap parentMap) {

        VertexContainer vertexContainer = (VertexContainer) vertex.getParent();
        if (vertexContainer == null) {
            vertexContainer = new VertexContainer();
            objectsToInsert.add(vertexContainer);
            parentMap.addEntry(vertex, vertexContainer);
        }
        return vertexContainer;
    }

    public int compareTo(NetGraphModel otherModel) {
        return getName().compareTo(otherModel.getName());
    }


    protected GraphModelEdit createEdit(Object[] inserted, Object[] removed,
   			Map attributes, ConnectionSet cs, ParentMap pm, UndoableEdit[] edits) {
   		GraphModelEdit edit = new NetGraphModelEdit(inserted, removed, attributes,
   				cs, pm);
   		if (edit != null) {
   			if (edits != null)
   				for (int i = 0; i < edits.length; i++)
   					edit.addEdit(edits[i]);
   			edit.end();
   		}
   		return edit;
   	}


    /**************************************************************************/
    // Has to be an inner class

    public class NetGraphModelEdit extends GraphModelEdit {

        public NetGraphModelEdit(Object[] inserted, Object[] removed,
                                 Map attributes, ConnectionSet connectionSet, ParentMap parentMap) {
            super(inserted, removed, attributes, connectionSet, parentMap);
        }

        public void redo() throws CannotRedoException {
            super.redo();
            YNetElementEdit.apply(getInserted(), getRemoved());
        }

        public void undo() throws CannotUndoException {
            super.undo();
            YNetElementEdit.apply(getInserted(), getRemoved());
        }
    }

}
