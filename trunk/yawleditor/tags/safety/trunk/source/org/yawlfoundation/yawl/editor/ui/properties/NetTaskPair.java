package org.yawlfoundation.yawl.editor.ui.properties;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.elements.YNet;

/**
 * @author Michael Adams
 * @date 23/07/12
 */
public class NetTaskPair {

    private YAWLTask _task;
    private NetGraph _graph;
    private YNet _net;
    private String _simpleText;

    public NetTaskPair(YAWLTask task, NetGraph graph) {
        _task = task;
        _graph = graph;
    }

    public NetTaskPair(YNet net) {
        _net = net;
        if (_net != null) _simpleText = getTextFromNet();
    }


    public YAWLTask getTask() { return _task; }

    public void setTask(YAWLTask task) { _task = task; }


    public NetGraph getGraph() { return _graph; }

    public void setGraph(NetGraph graph) { _graph = graph; }


    public YNet getNet() { return _net; }

    public void setNet(YNet net) { _net = net; }


    public String getSimpleText() { return toString(); }

    public void setSimpleText(String text) { _simpleText = text; }


    public String toString() {
        return _simpleText != null ? _simpleText : "";
    }


    private String getTextFromNet() {
        StringBuilder s = new StringBuilder();
        int locals = _net.getLocalVariables().size();
        if (locals > 0) {
            s.append("Local(").append(locals).append(") ");
        }
        int inputs = _net.getInputParameters().size();
        if (inputs > 0) {
            s.append("Input(").append(inputs).append(") ");
        }
        int outputs = _net.getOutputParameters().size();
        if (outputs > 0) {
            s.append("Output(").append(outputs).append(")");
        }
        if (s.length() == 0) s.append("None");
        return s.toString();
    }

}
