package org.yawlfoundation.yawl.editor.ui.actions.net;

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphStateListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;

import java.util.Arrays;

/**
 * @author Michael Adams
 * @date 27/09/13
 */
public class ProcessConfigurationAction extends YAWLSelectedNetAction
        implements GraphStateListener {

    protected NetGraph net;
    protected YAWLTask task;

    public ProcessConfigurationAction() {
        super();
        Publisher.getInstance().subscribe(this,
                Arrays.asList(GraphState.NoElementSelected,
                        GraphState.OneElementSelected));

    }

    public void graphSelectionChange(GraphState state, GraphSelectionEvent event) {
        if (state == GraphState.OneElementSelected) {
            Object cell = event.getCell();
            if (cell instanceof YAWLTask) {
                task = (YAWLTask) cell;
                net = getGraph();
                setEnabled(true);
                return;
            }
        }
        setEnabled(false);
    }

}
