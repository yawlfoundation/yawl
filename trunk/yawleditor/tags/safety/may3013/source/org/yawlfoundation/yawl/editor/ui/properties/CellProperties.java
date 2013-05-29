package org.yawlfoundation.yawl.editor.ui.properties;

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YTimerParameters;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Michael Adams
 * @date 2/07/12
 */
public class CellProperties extends NetProperties {

    protected YAWLVertex vertex;

    protected static String[] DECORATOR = new String[] {"AND", "OR", "XOR", "None"};
    protected static String[] DECORATOR_POS =
            new String[] {"North", "South", "West", "East", "None"};
    protected static final int DECORATOR_POS_OFFSET = 10;
    protected static final int DEFAULT_JOIN_POS = 12;
    protected static final int DEFAULT_SPLIT_POS = 13;

    private boolean idLabelSynch;
    private boolean viewingCancelSet;

    private String currentSplitType;
    private String currentJoinType;


    public CellProperties() {
        super();
    }


    protected void setVertex(YAWLVertex v) { vertex = v; }

    protected YAWLVertex getVertex() { return vertex; }


    public String getId() { return vertex.getID(); }         // read only


    public String getLabel() { return vertex.getLabel(); }

    public void setLabel(String value) {
        graph.setElementLabel(vertex, value);
        vertex.setName(value);
        if (idLabelSynch && value != null) {
            vertex.setID(value);
        }
        firePropertyChange("id", getId());
        setDirty();
    }


    public String getDocumentation() { return vertex.getDocumentation(); }

    public void setDocumentation(String value) {
        vertex.setDocumentation(value);
        setDirty();
    }


    public boolean isIdLabelSynch() { return idLabelSynch; }

    public void setIdLabelSynch(boolean value) {
        idLabelSynch = value;
        if (idLabelSynch && getLabel() != null) {
            vertex.setID(getLabel());
            setDirty();
        }
    }


    public Color getCellFillColor() { return vertex.getBackgroundColor(); }

    public void setCellFillColor(Color value) {
        vertex.setBackgroundColor(value);
        graph.changeVertexBackground(vertex, value);
        graph.resetCancellationSet();
        setDirty();
    }


    public Point getLocation() {
        Rectangle2D rect = vertex.getBounds();
        Point point = new Point();
        point.setLocation(rect.getX(), rect.getY());
        return point;
    }

    public void setLocation(Point p) {
        Rectangle2D rect = vertex.getBounds();
        vertex.setBounds(new Rectangle2D.Double(p.getX(), p.getY(),
                rect.getWidth(), rect.getHeight()));
        graph.moveElementTo(vertex, p.getX(), p.getY());
        graph.repaint();
        setDirty();
    }


    public Font getFont() { return new Font("Arial", Font.PLAIN, 12); }

    public void setFont(Font font) {}


    public String getCustomForm() {
        URL customFormURL = ((YAWLTask) vertex).getCustomFormURL();
        return customFormURL != null ? customFormURL.toExternalForm() : null;
    }

    public void setCustomForm(String url) {
        try {
            ((YAWLTask) vertex).setCustomFormURL(url);
            setDirty();
        }
        catch (MalformedURLException mue) {
            // nothing to do - dialog checks url for wellformedness
        }
    }


    public NetTaskPair getTimer() {
        NetTaskPair pair = new NetTaskPair(getSelectedYNet(), null, (AtomicTask) vertex);
        YTimerParameters parameters = ((AtomicTask) vertex).getTimerParameters();
        pair.setSimpleText(parameters != null ? parameters.toString(): "None");
        return pair;
    }

    public void setTimer(NetTaskPair pair) {
        YTimerParameters parameters = ((AtomicTask) vertex).getTimerParameters();
        pair.setSimpleText(parameters != null ? parameters.toString(): "None");

        // nothing else to do - updates handled by dialog
    }


    public boolean getViewCancelSet() {
        return viewingCancelSet;
    }

    public void setViewCancelSet(boolean view) {
        YAWLTask cancelTask = view ? (YAWLTask) vertex : null;
        graph.changeCancellationSet(cancelTask);
        viewingCancelSet = view;
    }


    public String getIcon() {
        return ((YAWLTask) vertex).getIconPath();
    }

    public void setIcon(String path) {
        graph.setVertexIcon(vertex, path);
        setDirty();
    }


    public NetTaskPair getMiAttributes() {
        return new NetTaskPair(getSelectedYNet(), null, (YAWLTask) vertex);
    }

    public void setMiAttributes(NetTaskPair value) {
        // nothing to do - updates handled by dialog
    }


    /***********************************************************************/

    public String getDecomposition() {
        YDecomposition decomposition = ((YAWLTask) vertex).getDecomposition();
        String label = decomposition != null ? decomposition.getID() : "None";
        setReadOnly("Timer", label.equals("None"));
        setReadOnly("CustomForm", label.equals("None"));
        return label;
    }


    public void setDecomposition(String name) {
        YDecomposition decomposition = ((YAWLTask) vertex).getDecomposition();
        if (name.equals("None")) {
            if (decomposition == null) return;             // no change so get out
            decomposition = null;                          // drop current
        }
        else if (name.equals("New...")) {                  // create
            decomposition = createDecomposition(decomposition);
        }

        // change current to another pre-existing
        else if (decomposition == null || ! decomposition.getID().equals(name)) {
            decomposition = SpecificationModel.getInstance().getDecompositionFromLabel(name);
        }
        else return;                        // no change (name = existing) so get out

        // update
        graph.setTaskDecomposition((YAWLTask) vertex, decomposition);
        setDirty();
        Publisher.getInstance().publishState(GraphState.ElementsSelected,
                new GraphSelectionEvent(this, new Object[] {vertex}, new boolean[] {false}));
    }


    private YDecomposition createDecomposition(YDecomposition current) {
        String newName = JOptionPane.showInputDialog(
                YAWLEditor.getInstance().getPropertySheet(),
                "Please enter a name for the new Decomposition");

        return ! StringUtil.isNullOrEmpty(newName) ?
                specificationHandler.getControlFlowHandler().addTaskDecomposition(newName) :
                current;
    }


    /***********************************************************************/

    public String getSplit() {
        Decorator decorator = ((YAWLTask) vertex).getSplitDecorator();
        setReadOnly("splitPosition", decorator == null);
        setReadOnly("SplitPredicates",
                decorator == null || decorator.getType() == SplitDecorator.AND_TYPE);
        currentSplitType = decorator != null ? DECORATOR[decorator.getType()] : "None";
        return currentSplitType;
    }

    public String getJoin() {
        Decorator decorator = ((YAWLTask) vertex).getJoinDecorator();
        setReadOnly("joinPosition", decorator == null);
        currentJoinType = decorator != null ? DECORATOR[decorator.getType()] : "None";
        return currentJoinType;
    }


    public String getSplitPosition() {
        Decorator decorator = ((YAWLTask) vertex).getSplitDecorator();
        return (decorator != null) ?
                DECORATOR_POS[decorator.getCardinalPosition() - DECORATOR_POS_OFFSET] :
                "None";
    }

    public String getJoinPosition() {
        Decorator decorator = ((YAWLTask) vertex).getJoinDecorator();
        return decorator != null ?
                DECORATOR_POS[decorator.getCardinalPosition() - DECORATOR_POS_OFFSET] :
                "None";
    }

    public void setSplit(String value) {
        if (! value.equals(currentSplitType)) {
            currentSplitType = value;
            int type = getDecoratorIndex(value);
            int pos = getDecoratorPosIndex(getSplitPosition());
            if (pos == 14 && type > -1) pos = DEFAULT_SPLIT_POS;
            graph.setSplitDecorator((YAWLTask) vertex, type, pos);
            setDirty();
            setReadOnly("SplitPredicates", type == -1 ||
                    ((YAWLTask) vertex).getSplitDecorator().getType() == SplitDecorator.AND_TYPE);
            fireDecoratorPositionChange("split", type > -1 ? pos : 14);
        }
    }

    public void setJoin(String value) {
        if (! value.equals(currentJoinType)) {
            currentJoinType = value;
            int type = getDecoratorIndex(value);
            int pos = getDecoratorPosIndex(getJoinPosition());
            if (pos == 14 && type > -1) pos = DEFAULT_JOIN_POS;
            graph.setJoinDecorator((YAWLTask) vertex, type, pos);
            setDirty();
            fireDecoratorPositionChange("join", type > -1 ? pos : 14);
        }
    }

    public void setSplitPosition(String value) throws PropertyVetoException {
        if (! value.equals(getSplitPosition())) {
            validateDecoratorPosition(getSplit(), "splitPos", getSplitPosition(), value);
            int type = getDecoratorIndex(getSplit());
            int pos = getDecoratorPosIndex(value);
            graph.setSplitDecorator((YAWLTask) vertex, type, pos);
            setDirty();
        }
    }

    public void setJoinPosition(String value) throws PropertyVetoException {
        if (! value.equals(getJoinPosition())) {
            validateDecoratorPosition(getJoin(), "joinPos", getJoinPosition(), value);
            int type = getDecoratorIndex(getJoin());
            int pos = getDecoratorPosIndex(value);
            graph.setJoinDecorator((YAWLTask) vertex, type, pos);
            setDirty();
        }
    }


    public NetTaskPair getSplitPredicates() {
        return new NetTaskPair((YAWLTask) vertex, graph);
    }

    public void setSplitPredicates(NetTaskPair pair) {
        // nothing to do - predicates set by dialog
    }

    private int getDecoratorIndex(String type) {
        for (int i=0; i < 3; i++) {
            if (type.equals(DECORATOR[i])) return i;
        }
        return -1;    // "None"
    }

    private int getDecoratorPosIndex(String pos) {
        for (int i=0; i < 5; i++) {
            if (pos.equals(DECORATOR_POS[i])) return i + DECORATOR_POS_OFFSET;
        }
        return 14;    // "Nowhere"
    }

    private void fireDecoratorPositionChange(String type, int pos) {
        firePropertyChange(type + "Position", DECORATOR_POS[pos - DECORATOR_POS_OFFSET]);
    }


    private void validateDecoratorPosition(String type, String property,
                                           String oldPos, String newPos)
            throws PropertyVetoException {
        if (type.equals("None") || newPos.equals("None")) {
            throw new PropertyVetoException("Invalid selection",
                new PropertyChangeEvent(this, property, oldPos, newPos));
        }
    }

}
