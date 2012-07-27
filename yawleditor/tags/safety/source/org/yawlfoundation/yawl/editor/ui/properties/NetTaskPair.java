package org.yawlfoundation.yawl.editor.ui.properties;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;

/**
 * @author Michael Adams
 * @date 23/07/12
 */
public class NetTaskPair {

    private YAWLTask _task;
    private NetGraph _graph;
    private String _simpleText;

    public NetTaskPair(YAWLTask task, NetGraph graph) {
        _task = task;
        _graph = graph;
    }

    public YAWLTask getTask() { return _task; }

    public void setTask(YAWLTask task) { _task = task; }


    public NetGraph getGraph() { return _graph; }

    public void setGraph(NetGraph graph) { _graph = graph; }


    public String getSimpleText() { return toString(); }

    public void setSimpleText(String text) { _simpleText = text; }


    public String toString() {
        return _simpleText != null ? _simpleText : "";
    }
}
