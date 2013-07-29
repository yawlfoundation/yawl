package org.yawlfoundation.yawl.editor.ui.properties;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.CancellationSetModel;
import org.yawlfoundation.yawl.editor.ui.net.CancellationSetModelListener;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;

/**
 * @author Michael Adams
 * @date 23/07/12
 */
public class EventListener implements CancellationSetModelListener {

    public NetGraph graph;
    public YAWLTask task;

    public void notify(int notification, YAWLTask triggeringTask) {
        if (notification == CancellationSetModel.SET_CHANGED) {
            YAWLEditor.getPropertySheet().firePropertyChange("ViewCancelSet",
                   (triggeringTask != null && triggeringTask == task));
        }
    }


    protected void setGraph(NetGraph netGraph) {
        if (graph != null) graph.getCancellationSetModel().unsubscribe(this);
        graph = netGraph;
        graph.getCancellationSetModel().subscribe(this);
    }


    protected void setTask(YAWLTask task) { this.task = task; }

}
