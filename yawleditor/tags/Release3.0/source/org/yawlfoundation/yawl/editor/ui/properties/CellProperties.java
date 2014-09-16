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
import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandlerException;
import org.yawlfoundation.yawl.editor.core.data.YDataHandlerException;
import org.yawlfoundation.yawl.editor.core.exception.IllegalIdentifierException;
import org.yawlfoundation.yawl.editor.core.resourcing.TaskResourceSet;
import org.yawlfoundation.yawl.editor.core.resourcing.YResourceHandler;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.net.YAWLEditorNetPanel;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;
import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;
import org.yawlfoundation.yawl.elements.*;

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

    protected static final String[] DECORATOR = new String[] {"AND", "OR", "XOR", "None"};
    protected static final String[] DECORATOR_POS =
            new String[] {"North", "South", "West", "East", "None"};
    protected static final int DECORATOR_POS_OFFSET = 10;
    protected static final int DEFAULT_JOIN_POS = 12;
    protected static final int DEFAULT_SPLIT_POS = 13;

    private String currentSplitType;
    private String currentJoinType;


    public CellProperties() {
        super();
        idLabelSynch = true;
    }


    protected void setVertex(YAWLVertex v) { vertex = v; }

    protected YAWLVertex getVertex() { return vertex; }


    public String getId() { return vertex.getID(); }         // read only


    public String getLabel() { return vertex.getLabel(); }

    public void setLabel(String value) {
        value = XMLUtilities.stripXMLChars(value);
        graph.setElementLabel(vertex, value);
        vertex.setName(value);
        if (idLabelSynch) updateVertexID(value);
        vertex.getVertexLabel().refreshLabelView();
        graph.setSelectionCell(vertex.getParent());
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
        if (idLabelSynch) updateVertexID(getLabel());
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
        VertexContainer container = (VertexContainer) vertex.getParent();
        if (container != null) {
            graph.moveElementTo(container, p.getX(), p.getY());
        }
        else {
            graph.moveElementTo(vertex, p.getX(), p.getY());
        }
        graph.repaint();
        setDirty();
    }


    public FontColor getFont() {
        VertexLabel label = vertex.getVertexLabel();
        Font font = label != null ? label.getFont() : UserSettings.getDefaultFont();
        Color colour = label != null ? label.getForeground() :
                UserSettings.getDefaultTextColour();
        return new FontColor(font, colour);
    }

    public void setFont(FontColor fontColor) {
        VertexLabel label = vertex.getVertexLabel();
        if (label != null) {
            label.setFont(fontColor.getFont());
            label.setForeground(fontColor.getColour());
            graph.setElementLabel(vertex, label.getText());
        }
    }


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
        setDirty();
        refreshCellView(vertex);
    }

    public NetTaskPair getResourcing() {
        NetTaskPair pair = new NetTaskPair(getSelectedYNet(), null, (YAWLTask) vertex);
        setResourcingString(pair);
        return pair;
    }

    public void setResourcing(NetTaskPair pair) {
        setResourcingString(pair);
        setDirty();
    }


    public String getIcon() {
        return ((YAWLTask) vertex).getIconPath();
    }

    public void setIcon(String path) {
        graph.setVertexIcon(vertex, path);
        refreshCellView(vertex);
        setDirty();
    }


    public NetTaskPair getMiAttributes() {
        NetTaskPair pair = new NetTaskPair(getSelectedYNet(), null, (YAWLTask) vertex);
        pair.setSimpleText(getMIShortString(((YAWLTask) vertex).getTask()));
        return pair;
    }

    public void setMiAttributes(NetTaskPair value) {
        // nothing to do - updates handled by dialog
    }


    /***********************************************************************/

    public String getDecomposition() {
        YDecomposition decomposition = ((YAWLTask) vertex).getDecomposition();
        String label = decomposition != null ? decomposition.getID() : "None";
        setReadOnly("miAttributes", label.equals("None"));
        if (decomposition instanceof YAWLServiceGateway) {
            boolean isReadOnly = ! requiresResourcing(decomposition);
            setReadOnly("Timer", isReadOnly);
            setReadOnly("CustomForm", isReadOnly);
            setReadOnly("Resourcing", isReadOnly);
        }
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
            if (decomposition == null) {
                firePropertyChange("Decomposition", "None");  // cancelled so reset
            }
        }
        else if (name.equals("Rename...")) {
            renameDecomposition(decomposition);
        }

        // change current to another pre-existing
        else if (decomposition == null || ! decomposition.getID().equals(name)) {
            decomposition = getDecomposition(name);
        }
        else return;                        // no change (name = existing) so get out

        // update if there has been any change
        updateDecomposition(decomposition);
    }


    private YDecomposition createDecomposition(YDecomposition current) {
        boolean isComposite = (vertex instanceof YAWLCompositeTask);
        while (true) {
            String newName = getDecompositionNameInput("New", isComposite);
            if (newName == null) {
                break;                 // Cancelled
            }
            try {
                YDecomposition decomposition;
                String id = XMLUtilities.toValidXMLName(newName);
                if (isComposite) {
                    YAWLEditorNetPanel panel = YAWLEditor.getNetsPane().newNet(false, id);
                    decomposition = panel.getNet().getNetModel().getDecomposition();
                }
                else {
                    decomposition = flowHandler.addTaskDecomposition(id);
                }
                if (! id.equals(newName)) {
                    decomposition.setName(newName);
                }
                return decomposition;
            }
            catch (IllegalIdentifierException iie) {
                showWarning("Identifier Name Error", iie.getMessage());
            }
            catch (YControlFlowHandlerException ycfhe) {
                // do nothing, only occurs if no spec is loaded
            }
        }
        return current;
    }


    private void renameDecomposition(YDecomposition current) {
        String oldID = current.getID();
        boolean isComposite = (vertex instanceof YAWLCompositeTask);
        String newID = getDecompositionNameInput("Rename", isComposite);
        if (! (newID == null || oldID.equals(newID))) {
            try {
                newID = specHandler.checkID(XMLUtilities.toValidXMLName(newID));
                specHandler.getDataHandler().renameDecomposition(oldID, newID);
                if (isComposite) {
                    YAWLEditor.getNetsPane().renameTab(oldID, newID);
                }
                firePropertyChange("Decomposition", newID);
            }
            catch (YDataHandlerException ydhe) {
                current.setID(oldID);
            }
            catch (IllegalIdentifierException iie) {
                showWarning("Identifier Rename Error", iie.getMessage());
            }
        }
    }


    private void updateDecomposition(YDecomposition decomposition) {
        ((YAWLTask) vertex).setDecomposition(decomposition);
        if (decomposition instanceof YAWLServiceGateway) {
            graph.setTaskDecomposition((YAWLTask) vertex, decomposition);  // update labels
        }
        else if (decomposition instanceof YNet) {
            String label = decomposition.getName();
            graph.setElementLabel(vertex, label != null ? label : decomposition.getID());
        }
        setDirty();
        graph.setSelectionCell(vertex.getParent());
        Publisher.getInstance().publishState(GraphState.ElementsSelected,
                new GraphSelectionEvent(this, new Object[] {vertex}, new boolean[] {false}));

        // update id if not tied to label
        String label = getLabel();
        if (decomposition != null && (label == null || ! label.equals(getId()))) {
            firePropertyChange("id", decomposition.getID());
            if (label == null) {
                String newLabel = decomposition.getName();
                if (newLabel == null) newLabel = decomposition.getID();
                firePropertyChange("Label", newLabel);
            }
        }
    }


    private YDecomposition getDecomposition(String name) {
        return (vertex instanceof YAWLCompositeTask) ?
                flowHandler.getNet(name) :
                flowHandler.getTaskDecomposition(name);
    }


    /***********************************************************************/

    public String getSplit() {
        Decorator decorator = ((YAWLTask) vertex).getSplitDecorator();
        setReadOnly("splitPosition", decorator == null);
        setReadOnly("SplitConditions", ! shouldEnableSplitConditions());
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

    public NetTaskPair getSplitConditions() {
        YAWLTask task = (YAWLTask) vertex;
        NetTaskPair pair = new NetTaskPair(task, graph);
        if (! task.hasSplitDecorator() ||
                task.getSplitDecorator().getType() == Decorator.AND_TYPE) {
            pair.setSimpleText("n/a");
        }
        else {
            int flowCount = task.getOutgoingFlowCount();
            pair.setSimpleText(flowCount < 2 ? "None" : flowCount + " flows");
        }
        return pair;
    }

    public void setSplitConditions(NetTaskPair pair) {
        pair.setSimpleText(((YAWLTask) vertex).getOutgoingFlowCount() + " flows");
    }


    public void setSplit(String value) {
        if (! value.equals(currentSplitType)) {
            try {
                flowHandler.setSplit((YTask) vertex.getYAWLElement(),
                        getYTaskSplitType(value));
                currentSplitType = value;
                int type = getDecoratorIndex(value);
                int pos = getDecoratorPosIndex(getSplitPosition());
                if (pos == 14 && type > -1) pos = DEFAULT_SPLIT_POS;
                graph.setSplitDecorator((YAWLTask) vertex, type, pos);
                setDirty();
                setReadOnly("SplitConditions", ! shouldEnableSplitConditions());
                graph.setSelectionCell(vertex.getParent());
                fireDecoratorPositionChange("split", type > -1 ? pos : 14);
            }
            catch (YControlFlowHandlerException ycfhe) {
                YAWLEditor.getStatusBar().setText("Error: " + ycfhe.getMessage());
            }
        }
    }

    public void setJoin(String value) {
        if (! value.equals(currentJoinType)) {
            try {
                flowHandler.setJoin((YTask) vertex.getYAWLElement(),
                        getYTaskJoinType(value));
                currentJoinType = value;
                int type = getDecoratorIndex(value);
                int pos = getDecoratorPosIndex(getJoinPosition());
                if (pos == 14 && type > -1) pos = DEFAULT_JOIN_POS;
                graph.setJoinDecorator((YAWLTask) vertex, type, pos);
                setDirty();
                graph.setSelectionCell(vertex.getParent());
                fireDecoratorPositionChange("join", type > -1 ? pos : 14);
            }
            catch (YControlFlowHandlerException ycfhe) {
                YAWLEditor.getStatusBar().setText("Error: " + ycfhe.getMessage());
            }
        }
    }

    public void setSplitPosition(String value) throws PropertyVetoException {
        if (! value.equals(getSplitPosition())) {
            validateDecoratorPosition("splitPos", getSplitPosition(), value);
            int type = getDecoratorIndex(getSplit());
            int pos = getDecoratorPosIndex(value);
            graph.setSplitDecorator((YAWLTask) vertex, type, pos);
            graph.setSelectionCell(vertex.getParent());
            setDirty();
        }
    }

    public void setJoinPosition(String value) throws PropertyVetoException {
        if (! value.equals(getJoinPosition())) {
            validateDecoratorPosition("joinPos", getJoinPosition(), value);
            int type = getDecoratorIndex(getJoin());
            int pos = getDecoratorPosIndex(value);
            graph.setJoinDecorator((YAWLTask) vertex, type, pos);
            graph.setSelectionCell(vertex.getParent());
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

    private int getYTaskSplitType(String type) {
        if (type.equals("AND")) return YTask._AND;
        if (type.equals("OR")) return YTask._OR;
        return YTask._XOR;
    }

    private int getYTaskJoinType(String type) {
        if (type.equals("XOR")) return YTask._XOR;
        if (type.equals("OR")) return YTask._OR;
        return YTask._AND;
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


    private void validateDecoratorPosition(String property,
                                           String oldPos, String newPos)
            throws PropertyVetoException {
        String msg = null;
        if (newPos.equals("None")) {
            msg = "A " + property.substring(0, property.indexOf('P')) +
                    " cannot have a position value of 'None'.";
        }

        // can't have the same position for both decorators (other than 'None')
        else if ((property.equals("splitPos") && newPos.equals(getJoinPosition())) ||
                (property.equals("joinPos") && newPos.equals(getSplitPosition()))) {
            msg = "The '" + newPos + "' position is already occupied.";
        }
        if (msg != null) {
            throw new PropertyVetoException("Invalid position selection: " + msg,
                new PropertyChangeEvent(this, property, oldPos, newPos));
        }
    }


    private void setResourcingString(NetTaskPair pair) {
        YResourceHandler handler = specHandler.getResourceHandler();
        TaskResourceSet resources = handler.getTaskResources(
                pair.getNet().getID(), pair.getTask().getID());
        pair.setSimpleText(resources != null ? resources.getInitiatorChars() : "None");
    }


    private boolean requiresResourcing(YDecomposition decomposition) {
        if (! (decomposition instanceof YAWLServiceGateway)) return false;
        YAWLServiceReference service = ((YAWLServiceGateway) decomposition).getYawlService();
        return service == null && decomposition.requiresResourcingDecisions();
    }


    private String getMIShortString(YTask task) {
        YMultiInstanceAttributes attributes = task.getMultiInstanceAttributes();
        if (attributes != null) {
            StringBuilder shortString = new StringBuilder(10);
            shortString.append(attributes.getMinInstances()).append(", ");
            shortString.append(getMIValue(attributes.getMaxInstances())).append(", ");
            shortString.append(getMIValue(attributes.getThreshold())).append(", ");
            shortString.append(Character.toUpperCase(
                    attributes.getCreationMode().charAt(0)));
            return shortString.toString();
        }
        return "None";
    }


    private String getMIValue(int value) {
        return value == Integer.MAX_VALUE ? Character.toString('\u221E') : // infinity
                String.valueOf(value);
    }

    private void updateVertexID(String id) {
        if (id != null) {
            String validID = XMLUtilities.toValidXMLName(id);
            if (validID.isEmpty()) {
                validID = (vertex instanceof YAWLTask) ? "T" : "C";      // default
            }
            if (! vertex.getID().equals(validID)) {
                try {
                    validID = flowHandler.replaceID(vertex.getID(), validID);
                    if (vertex instanceof YAWLTask) {
                        specHandler.getResourceHandler().replaceID(vertex.getID(), validID);
                    }
                    vertex.setID(validID);
                    firePropertyChange("id", getId());
                    setDirty();
                }
                catch (IllegalIdentifierException iie) {
                    showWarning("Element Identifier Update Failed",
                            "Could not synch element identifier - " + iie.getMessage());
                }
            }
        }
    }


    private String getDecompositionNameInput(String title, boolean isComposite) {
        return (String) JOptionPane.showInputDialog(YAWLEditor.getInstance(),
                "Please enter a name for the new " +
                        (isComposite ? "sub-net" : "decomposition") + ":",
                title + " Decomposition",
                JOptionPane.QUESTION_MESSAGE,
                null, null,
                getLabel());

    }


    private boolean shouldEnableSplitConditions() {
        Decorator decorator = ((YAWLTask) vertex).getSplitDecorator();
        return ! (decorator == null || decorator.getType() == SplitDecorator.AND_TYPE);
//                ||
//                decorator.getFlowCount() < 2);  // can't trigger after adding new flow

    }
}
