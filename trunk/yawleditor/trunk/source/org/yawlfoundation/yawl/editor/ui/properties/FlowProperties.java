package org.yawlfoundation.yawl.editor.ui.properties;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetCellUtilities;

/**
 * @author Michael Adams
 * @date 2/07/12
 */
public class FlowProperties extends NetProperties {

    private YAWLFlowRelation flow;

    private static final int offset = 11;
    protected static String[] STYLES = new String[] {"Orthogonal", "Bezier", "Spline"};


    public FlowProperties() {
        super();
    }


    protected void setFlow(YAWLFlowRelation f) { flow = f; }

    protected YAWLFlowRelation getFlow() { return flow; }


    public String getSource() { return flow.getSourceID(); }      // read only

    public String getTarget() { return flow.getTargetID(); }      // read only


    public String getLineStyle() {
        return STYLES[NetCellUtilities.getFlowLineStyle(graph, flow) - offset];
    }

    public void setLineStyle(String style) {
        int pos = offset;
        for (String s : STYLES) {
            if (style.equals(s)) break;
            pos++;
        }
        NetCellUtilities.setFlowStyle(graph, flow, pos);
        setDirty();
    }
}
