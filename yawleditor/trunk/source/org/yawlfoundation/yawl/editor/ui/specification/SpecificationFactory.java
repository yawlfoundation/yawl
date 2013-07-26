package org.yawlfoundation.yawl.editor.ui.specification;

import org.yawlfoundation.yawl.editor.ui.elements.model.InputCondition;
import org.yawlfoundation.yawl.editor.ui.elements.model.OutputCondition;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.YAWLEditorDesktop;
import org.yawlfoundation.yawl.elements.YNet;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author Michael Adams
 * @date 26/07/13
 */
public class SpecificationFactory {

    private static final int MARGIN = 50;


    public void build() {
        NetGraph graph = SpecificationModel.getInstance().newSpecification();
        YNet net = (YNet) graph.getNetModel().getDecomposition();
        populateGraph(net, graph);
        YAWLEditorDesktop.getInstance().openNet(graph);
        Publisher.getInstance().publishAddNetEvent();
    }


    private void populateGraph(YNet net, NetGraph graph) {
        Rectangle bounds = getCanvasBounds();
        InputCondition inputCondition = new InputCondition(
                getInputConditionPoint(bounds, graph), net.getInputCondition());
        addCondition(graph, inputCondition);

        OutputCondition outputCondition = new OutputCondition(
                getOutputConditionDefaultPoint(bounds, graph), net.getOutputCondition());
        addCondition(graph, outputCondition);
    }


    private void addCondition(NetGraph graph, YAWLVertex vertex) {
        graph.addElement(vertex);
        String name = vertex.getName();
        if (name != null) graph.setElementLabel(vertex, name);
    }


    private Point2D getInputConditionPoint(Rectangle bounds, NetGraph graph) {
        Dimension size = InputCondition.getVertexSize();
        return graph.snap(new Point((MARGIN)  - (size.width/2),
                (int) (bounds.getHeight()/2) - (size.height/2)));
    }

    private Point2D getOutputConditionDefaultPoint(Rectangle bounds, NetGraph graph) {
        Dimension size = InputCondition.getVertexSize();
        return graph.snap(new Point((int) (bounds.getWidth()-MARGIN) - (size.width/2),
                (int) (bounds.getHeight()/2)  - (size.height/2)));
    }


    private Rectangle getCanvasBounds() {
        return cropRectangle(YAWLEditorDesktop.getInstance().getBounds(), 15);

    }

    private Rectangle cropRectangle(Rectangle r, int crop) {
         return new Rectangle(r.x, r.y, r.width - crop, r.height - crop);
     }

}
