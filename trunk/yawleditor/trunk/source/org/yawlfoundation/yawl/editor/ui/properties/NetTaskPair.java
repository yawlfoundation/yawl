package org.yawlfoundation.yawl.editor.ui.properties;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;

/**
 * @author Michael Adams
 * @date 23/07/12
 */
public class NetTaskPair {

    private YAWLTask _task;
    private NetGraph _graph;
    private YNet _net;
    private YDecomposition _decomposition;
    private String _simpleText;

    public NetTaskPair(YAWLTask task, NetGraph graph) {
        _task = task;
        _graph = graph;
    }

    public NetTaskPair(YNet net, YDecomposition decomposition, YAWLTask task) {
        _net = net;
        _decomposition = decomposition;
        _task = task;
        _simpleText = (_decomposition != null) ? getText(decomposition) : getText(net);
    }


    public YAWLTask getTask() { return _task; }

    public void setTask(YAWLTask task) { _task = task; }


    public NetGraph getGraph() { return _graph; }

    public void setGraph(NetGraph graph) { _graph = graph; }


    public YNet getNet() { return _net; }

    public void setNet(YNet net) { _net = net; }


    public YDecomposition getDecomposition() { return _decomposition; }

    public void setDecomposition(YDecomposition decomp) { _decomposition =  decomp; }


    public String getSimpleText() { return toString(); }

    public void setSimpleText(String text) { _simpleText = text; }


    public String toString() {
        return _simpleText != null ? _simpleText : "";
    }


    private String getText(YDecomposition decomposition) {
        StringBuilder s = new StringBuilder();
        if (decomposition instanceof YNet) {
            int locals = ((YNet) decomposition).getLocalVariables().size();
            if (locals > 0) {
                s.append("Local(").append(locals).append(") ");
            }
        }
        int inputs = decomposition.getInputParameters().size();
        if (inputs > 0) {
            s.append("Input(").append(inputs).append(") ");
        }
        int outputs = decomposition.getOutputParameters().size();
        if (outputs > 0) {
            s.append("Output(").append(outputs).append(")");
        }
        if (s.length() == 0) s.append("None");
        return s.toString();
    }

}

