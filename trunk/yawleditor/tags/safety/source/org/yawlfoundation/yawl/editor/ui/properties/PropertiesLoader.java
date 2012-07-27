package org.yawlfoundation.yawl.editor.ui.properties;

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.editor.ui.elements.model.VertexContainer;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.*;

import java.util.Arrays;

/**
 * @author Michael Adams
 * @date 2/07/12
 */
public class PropertiesLoader
        implements GraphStateListener, SpecificationStateListener, FileStateListener {

    private SpecificationModel _model;
    private NetGraph _graph;
    private Binder _binder;
    private NetProperties _netProperties;
    private CellProperties _cellProperties;
    private FlowProperties _flowProperties;
    private DecompositionProperties _decompositionProperties;
    private EventListener _eventListener;


    public PropertiesLoader(SpecificationModel model) {
        _model = model;
        _netProperties = new NetProperties();
        _cellProperties = new CellProperties();
        _flowProperties = new FlowProperties();
        _decompositionProperties = new DecompositionProperties();
        _eventListener = new EventListener();
        subscribe();
    }


    public void setGraph(NetGraph graph) {
        _graph = graph;
        _netProperties.setGraph(graph);
        _cellProperties.setGraph(graph);
        _flowProperties.setGraph(graph);
        _decompositionProperties.setGraph(graph);
        _eventListener.setGraph(graph);
        unbind();
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


    public void graphSelectionChange(GraphState state, GraphSelectionEvent event) {
        if (event == null) return;
        switch(state) {
            case NoElementSelected: {
                unbind();
                showNetProperties();
                break;
            }
            case ElementsSelected: {
                Object cell = event.getCell();
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
                if (_graph != null) showNetProperties();
                break;
            }
        }
    }


    public void specificationFileStateChange(FileState state) {
        switch(state) {
            case Open: {
                break;
            }
            case Closed: {
                unbind();
                break;
            }
        }
    }


    private void showVertexProperties(YAWLVertex vertex) {
        if (vertex instanceof YAWLTask) _eventListener.setTask((YAWLTask) vertex);
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


    private boolean isTaskDecomposition(Object cell) {
        return (cell instanceof YAWLTask) && ((YAWLTask) cell).getDecomposition() != null;
    }

}
