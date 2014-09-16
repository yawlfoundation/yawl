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

package org.yawlfoundation.yawl.editor.ui.properties;

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.*;
import org.yawlfoundation.yawl.elements.YDecomposition;

import java.util.Arrays;

/**
 * @author Michael Adams
 * @date 2/07/12
 */
public class PropertiesLoader
        implements GraphStateListener, SpecificationStateListener, FileStateListener {

    private NetGraph _graph;
    private Binder _binder;
    private final NetProperties _netProperties;
    private final CellProperties _cellProperties;
    private final FlowProperties _flowProperties;
    private final DecompositionProperties _decompositionProperties;
    private final MultiFlowProperties _multiFlowProperties;
    private final MultiCellProperties _multiCellProperties;
    private final MultiDecompositionProperties _multiDecompositionProperties;
    private FileState _lastFileState;


    public PropertiesLoader() {
        _netProperties = new NetProperties();
        _cellProperties = new CellProperties();
        _flowProperties = new FlowProperties();
        _decompositionProperties = new DecompositionProperties();
        _multiFlowProperties = new MultiFlowProperties();
        _multiCellProperties = new MultiCellProperties();
        _multiDecompositionProperties = new MultiDecompositionProperties();
        subscribe();
        _lastFileState = FileState.Closed;
    }


    public void setGraph(NetGraph graph) {
        if (graph != null) {
            if (! graph.equals(_graph)) {
                _graph = graph;
                _netProperties.setGraph(graph);
                _cellProperties.setGraph(graph);
                _flowProperties.setGraph(graph);
                _decompositionProperties.setGraph(graph);
                _multiFlowProperties.setGraph(graph);
                _multiCellProperties.setGraph(graph);
                _multiDecompositionProperties.setGraph(graph);
            }
            showNetProperties();
        }
    }

    private void subscribe() {
        Publisher.getInstance().subscribe((SpecificationStateListener) this);
        Publisher.getInstance().subscribe((FileStateListener) this);
        Publisher.getInstance().subscribe(this,            // GraphStateListener
              Arrays.asList(GraphState.NoElementSelected,
                        GraphState.ElementsSelected,
                        GraphState.OneElementSelected));
    }


    private void bind(YPropertiesBean propertiesBean, YBeanInfo beanInfo) {
        _binder = new Binder(propertiesBean, beanInfo);
    }

    private void unbind() {
        if (_binder != null) _binder.unbind();
    }

    private void setGraph() {
        setGraph(YAWLEditor.getNetsPane().getSelectedGraph());
    }

    public void graphSelectionChange(GraphState state, GraphSelectionEvent event) {
        if (event == null) return;
        switch(state) {
            case NoElementSelected: {
                setGraph();
                break;
            }
            case ElementsSelected: {
                Object[] cells = _graph.getSelectionCells();
                if (cells.length > 1) {
                    showMultiCellProperties(cells);
                    break;
                }
                Object cell = cells[0];
                if (cell instanceof YAWLFlowRelation) {
                    showFlowProperties((YAWLFlowRelation) cell);
                }
                else if (cell instanceof VertexContainer) {
                    showVertexProperties(((VertexContainer) cell).getVertex());
                }
                else if (cell instanceof YAWLVertex) {
                    showVertexProperties((YAWLVertex) cell);
                }
                break;
            }
        }
    }


    public void specificationStateChange(SpecificationState state) {
        switch (state) {
            case NoNetsExist : {
                unbind();
                break;
            }
            case NetsExist: {
                unbind();
                setGraph();
                break;
            }
        }
    }

    // state transition: Closed - Busy - Open - Busy (saving) - Open - Busy - Closed
    public void specificationFileStateChange(FileState state) {
        switch(state) {
            case Open: {
                if (_lastFileState == FileState.Busy) {
                    _netProperties.firePropertyChange("Version",
                            SpecificationModel.getHandler().getVersion().toDouble());
                }
                _lastFileState = FileState.Open;
                break;
            }
            case Busy: {                             // busy before open and when saving
                if (_lastFileState == FileState.Open) {
                    _lastFileState = FileState.Busy;
                }
                break;
            }
            case Closed: {
                unbind();
                _lastFileState = FileState.Closed;
                break;
            }
        }
    }


    private void showVertexProperties(YAWLVertex vertex) {
        if (isTaskDecomposition(vertex)) {
            showDecompositionProperties(vertex);
        }
        else showCellProperties(vertex);
    }


    private void showFlowProperties(YAWLFlowRelation flow) {
        unbind();
        _flowProperties.setFlow(flow);
        bind(_flowProperties, new FlowBeanInfo());
    }


    private void showDecompositionProperties(YAWLVertex vertex) {
        unbind();
        _decompositionProperties.setVertex(vertex);
        bind(_decompositionProperties, new DecompositionBeanInfo(vertex));
    }


    private void showCellProperties(YAWLVertex vertex) {
         unbind();
         _cellProperties.setVertex(vertex);
         bind(_cellProperties, new CellBeanInfo(vertex));
    }

    private void showNetProperties() {
        unbind();
        bind(_netProperties, new NetBeanInfo());
    }


    private void showMultiCellProperties(Object[] cells) {
        int flows = 0;
        for (Object o : cells) {
            if (o instanceof YAWLFlowRelation) flows++;
        }
        if (flows == cells.length) showMultiFlowProperties(cells);  // all flows
        else if (flows > 0) showNetProperties();   // flows + elements, so no props
        else showMultiVertexProperties(cells);

    }


    private void showMultiFlowProperties(Object[] cells) {
        unbind();
        _multiFlowProperties.setFlows(cells);
        bind(_multiFlowProperties, new MultiFlowBeanInfo());
    }


    private void showMultiVertexProperties(Object[] cells) {
        unbind();
        YDecomposition decomposition = PropertyUtil.getCommonDecomposition(cells);
        if (decomposition != null) {
            _multiDecompositionProperties.setCells(cells);
            bind(_multiDecompositionProperties, new MultiDecompositionBeanInfo(cells));
        }
        else {
            _multiCellProperties.setCells(cells);
            bind(_multiCellProperties, new MultiCellBeanInfo(cells));
        }
    }


    private boolean isTaskDecomposition(Object cell) {
        return (cell instanceof YAWLTask) && ((YAWLTask) cell).getDecomposition() != null;
    }


}
