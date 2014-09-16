/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 2/07/12
 */
public class MultiCellProperties extends NetProperties {

    protected Set<YAWLVertex> vertexSet;

    protected static final String[] DECORATOR = new String[] {"AND", "OR", "XOR", "None"};
    protected static final String[] DECORATOR_POS =
            new String[] {"North", "South", "West", "East", "None"};
    protected static final int DECORATOR_POS_OFFSET = 10;
    protected static final int DEFAULT_JOIN_POS = 12;
    protected static final int DEFAULT_SPLIT_POS = 13;

    private String currentSplitType;
    private String currentJoinType;


    public MultiCellProperties() {
        super();
        idLabelSynch = true;
    }


    protected void setCells(Object[] cells) {
        vertexSet = PropertyUtil.makeVertexSet(cells);
    }


    public String getLabel() {
        String label = null;
        for (YAWLVertex vertex : vertexSet) {
            if (label == null) label = vertex.getLabel();
            else if (! label.equals(vertex.getLabel())) return "";
        }
        return label;    // all match
    }

    public void setLabel(String value) {
        for (YAWLVertex vertex : vertexSet) {
            value = XMLUtilities.stripXMLChars(value);
            graph.setElementLabel(vertex, value);
            vertex.setName(value);
            if (idLabelSynch) updateVertexID(vertex, value);
            vertex.getVertexLabel().refreshLabelView();
        }
        graph.setSelectionCells(getParents());
        setDirty();
    }


    public String getDocumentation() {
        String doco = null;
        for (YAWLVertex vertex : vertexSet) {
            if (doco == null) doco = vertex.getDocumentation();
            else if (! doco.equals(vertex.getDocumentation())) return "";
        }
        return doco;    // all match
    }

    public void setDocumentation(String value) {
        for (YAWLVertex vertex : vertexSet) {
            vertex.setDocumentation(value);
            setDirty();
        }
    }


    public boolean isIdLabelSynch() { return idLabelSynch; }

    public void setIdLabelSynch(boolean value) {
        idLabelSynch = value;
        if (idLabelSynch) for (YAWLVertex vertex : vertexSet) {
            updateVertexID(vertex, getLabel());
        }
    }


    public Color getCellFillColor() {
        Color colour = null;
        for (YAWLVertex vertex : vertexSet) {
            if (colour == null) colour = vertex.getBackgroundColor();
            else if (!colour.equals(vertex.getBackgroundColor())) {
                return Color.WHITE;
            }
        }
        return colour;    // all match
    }

    public void setCellFillColor(Color value) {
        for (YAWLVertex vertex : vertexSet) {
            vertex.setBackgroundColor(value);
            graph.changeVertexBackground(vertex, value);
            graph.resetCancellationSet();
            setDirty();
        }
    }


    public FontColor getFont() {
        Font font = null;
        for (YAWLVertex vertex : vertexSet) {
            VertexLabel label = vertex.getVertexLabel();
            if (label == null) continue;
            if (font == null) font = label.getFont();
            else if (!font.equals(label.getFont())) {
                font = UserSettings.getDefaultFont();
                break;
            }
        }
        Color colour = null;
        for (YAWLVertex vertex : vertexSet) {
            VertexLabel label = vertex.getVertexLabel();
            if (label == null) continue;
            if (colour == null) colour = label.getForeground();
            else if (!colour.equals(label.getForeground())) {
                colour = UserSettings.getDefaultTextColour();
            }
        }

        if (font == null) font = UserSettings.getDefaultFont();
        if (colour == null) colour = UserSettings.getDefaultTextColour();
        return new FontColor(font, colour);
    }

    public void setFont(FontColor fontColor) {
        for (YAWLVertex vertex : vertexSet) {
            VertexLabel label = vertex.getVertexLabel();
            if (label != null) {
                label.setFont(fontColor.getFont());
                label.setForeground(fontColor.getColour());
                graph.setElementLabel(vertex, label.getText());
            }
        }
    }


    public String getCustomForm() {
        String urlStr = null;
        for (YAWLVertex vertex : vertexSet) {
            URL customFormURL = ((YAWLTask) vertex).getCustomFormURL();
            if (customFormURL == null) {
                return null;
            }
            else {
                if (urlStr == null) urlStr = customFormURL.toExternalForm();
                else if (! urlStr.equals(customFormURL.toExternalForm())) {
                    return null;
                }
            }
        }
        return urlStr;
    }

    public void setCustomForm(String url) {
        for (YAWLVertex vertex : vertexSet) {
            try {
                ((YAWLTask) vertex).setCustomFormURL(url);
                setDirty();
            }
            catch(MalformedURLException mue){
                // nothing to do - dialog checks url for wellformedness
            }
        }
    }


    public NetTaskPair getTimer() {
        NetTaskPair pair = new NetTaskPair(getSelectedYNet(), vertexSet);
        pair.setSimpleText("");
        return pair;
    }

    public void setTimer(NetTaskPair pair) {
        setDirty();
        refreshCellViews(vertexSet);
    }

//    public NetTaskPair getResourcing() {
//        NetTaskPair pair = new NetTaskPair(getSelectedYNet(), vertexSet);
//        setResourcingString(pair);
//        return pair;
//    }
//
//    public void setResourcing(NetTaskPair pair) {
//        setResourcingString(pair);
//        setDirty();
//    }


    public String getIcon() {
        String path = null;
        for (YAWLVertex vertex : vertexSet) {
            String thisPath = ((YAWLTask) vertex).getIconPath();
            if (thisPath == null) return null;
            if (path == null) path = thisPath;
            else if (! thisPath.equals(path)) return  null;
        }
        return path;
    }

    public void setIcon(String path) {
        for (YAWLVertex vertex : vertexSet) {
            graph.setVertexIcon(vertex, path);
        }
        refreshCellViews(vertexSet);
        setDirty();
    }


    public NetTaskPair getMiAttributes() {
        NetTaskPair pair = new NetTaskPair(getSelectedYNet(), vertexSet);
        String shortString = null;
        for (YAWLVertex vertex : vertexSet) {
            String thisString = getMIShortString(((YAWLTask) vertex).getTask());
            if (shortString == null) shortString = thisString;
            else if (! thisString.equals(shortString)) {
                shortString = "";
                break;
            }
        }
        pair.setSimpleText(shortString);
        return pair;
    }

    public void setMiAttributes(NetTaskPair value) {
        // nothing to do - updates handled by dialog
    }


    /***********************************************************************/

    public String getDecomposition() {
        YDecomposition decomposition = getCommonDecomposition();
        String label = decomposition != null ? decomposition.getID() : "None";
        setReadOnly("miAttributes", label.equals("None"));
        if (decomposition instanceof YAWLServiceGateway) {
            boolean isReadOnly = ! requiresResourcing(decomposition);
            setReadOnly("Timer", isReadOnly);
            setReadOnly("CustomForm", isReadOnly);
        //    setReadOnly("Resourcing", isReadOnly);
        }
        return label;
    }


    public void setDecomposition(String name) {
        YDecomposition decomposition = getCommonDecomposition();
        if (name.equals("None")) {
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


    protected YDecomposition getCommonDecomposition() {
        YDecomposition common = null;
        for (YAWLVertex vertex : vertexSet) {
            YDecomposition decomposition = ((YAWLTask) vertex).getDecomposition();
            if (decomposition == null) return null;
            if (common == null) common = decomposition;
            else if (! common.equals(decomposition)) return null;
        }
        return common;
    }

    private YDecomposition createDecomposition(YDecomposition current) {
        boolean isComposite = allInstanceOf(YAWLCompositeTask.class);
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
        boolean isComposite = allInstanceOf(YAWLCompositeTask.class);
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
        String label = null;
        for (YAWLVertex vertex : vertexSet) {
            ((YAWLTask) vertex).setDecomposition(decomposition);
            if (decomposition instanceof YAWLServiceGateway) {
                graph.setTaskDecomposition((YAWLTask) vertex, decomposition);  // update labels
            } else if (decomposition instanceof YNet) {
                graph.setElementLabel(vertex, decomposition.getID());
            }
            label = updateLabel(vertex);
        }
        setDirty();
        firePropertyChange("Label", label);

        Object[] containers = getParents();
        graph.setSelectionCells(containers);
        Publisher.getInstance().publishState(GraphState.ElementsSelected,
                new GraphSelectionEvent(this, containers,
                        new boolean[] {false}));

    }

    private Object[] getParents() {
        Set<Object> parents = new HashSet<Object>();
        for (YAWLVertex vertex : vertexSet) {
            if (vertex.getParent() != null) parents.add(vertex.getParent());
        }
        return parents.toArray();
    }


    // update id if not tied to label
    private String updateLabel(YAWLVertex vertex) {
        YDecomposition decomposition = ((YAWLTask) vertex).getDecomposition();
        String label = vertex.getLabel();
        if (decomposition != null && (label == null || !label.equals(vertex.getID()))) {
            if (idLabelSynch) vertex.setID(flowHandler.checkID(decomposition.getID()));
            if (label == null) {
                String newLabel = decomposition.getName();
                if (newLabel == null) newLabel = decomposition.getID();
                vertex.setName(newLabel);
                return newLabel;
            }
        }
        return label;
    }

    private YDecomposition getDecomposition(String name) {
        return allInstanceOf(YAWLCompositeTask.class) ?
                flowHandler.getNet(name) :
                flowHandler.getTaskDecomposition(name);
    }


    /***********************************************************************/

    public String getSplit() {
        Decorator decorator = getDecorator(true);
        setReadOnly("splitPosition", decorator == null);
        currentSplitType = decorator != null ? DECORATOR[decorator.getType()] : "None";
        return currentSplitType;
    }

    public String getJoin() {
        Decorator decorator = getDecorator(false);
        setReadOnly("joinPosition", decorator == null);
        currentJoinType = decorator != null ? DECORATOR[decorator.getType()] : "None";
        return currentJoinType;
    }


    public String getSplitPosition() {
        Decorator decorator = getDecorator(true);
        return (decorator != null) ?
                DECORATOR_POS[decorator.getCardinalPosition() - DECORATOR_POS_OFFSET] :
                "None";
    }

    public String getJoinPosition() {
        Decorator decorator = getDecorator(false);
        return decorator != null ?
                DECORATOR_POS[decorator.getCardinalPosition() - DECORATOR_POS_OFFSET] :
                "None";
    }


    public void setSplit(String value) {
        int type = -1;
        int pos = -1;
        if (! value.equals(currentSplitType)) {
            for (YAWLVertex vertex : vertexSet) {
                try {
                    flowHandler.setSplit((YTask) vertex.getYAWLElement(),
                            getYTaskSplitType(value));
                    type = getDecoratorIndex(value);
                    pos = getDecoratorPosIndex(getSplitPosition());
                    if (pos == 14 && type > -1) pos = DEFAULT_SPLIT_POS;
                    graph.setSplitDecorator((YAWLTask) vertex, type, pos);
                }
                catch (YControlFlowHandlerException ycfhe) {
                    YAWLEditor.getStatusBar().setText("Error: " + ycfhe.getMessage());
                }
            }
        }
        currentSplitType = value;
        graph.setSelectionCells(getParents());
        fireDecoratorPositionChange("split", type > -1 ? pos : 14);
        setDirty();
    }

    public void setJoin(String value) {
        int type = -1;
        int pos = -1;
        if (! value.equals(currentJoinType)) {
            for (YAWLVertex vertex : vertexSet) {
                try {
                    flowHandler.setJoin((YTask) vertex.getYAWLElement(),
                            getYTaskJoinType(value));
                    type = getDecoratorIndex(value);
                    pos = getDecoratorPosIndex(getJoinPosition());
                    if (pos == 14 && type > -1) pos = DEFAULT_JOIN_POS;
                    graph.setJoinDecorator((YAWLTask) vertex, type, pos);
                }
                catch(YControlFlowHandlerException ycfhe){
                    YAWLEditor.getStatusBar().setText("Error: " + ycfhe.getMessage());
                }
            }
        }
        currentJoinType = value;
        graph.setSelectionCells(getParents());
        fireDecoratorPositionChange("join", type > -1 ? pos : 14);
        setDirty();
    }

    public void setSplitPosition(String value) throws PropertyVetoException {
        if (! value.equals(getSplitPosition())) {
            validateDecoratorPosition("splitPos", getSplitPosition(), value);
            for (YAWLVertex vertex : vertexSet) {
                int type = getDecoratorIndex(getSplit());
                int pos = getDecoratorPosIndex(value);
                graph.setSplitDecorator((YAWLTask) vertex, type, pos);
            }
            graph.setSelectionCells(getParents());
            setDirty();
        }
    }

    public void setJoinPosition(String value) throws PropertyVetoException {
        if (! value.equals(getJoinPosition())) {
            validateDecoratorPosition("joinPos", getJoinPosition(), value);
            for (YAWLVertex vertex : vertexSet) {
                int type = getDecoratorIndex(getJoin());
                int pos = getDecoratorPosIndex(value);
                graph.setJoinDecorator((YAWLTask) vertex, type, pos);
            }
            graph.setSelectionCells(getParents());
            setDirty();
        }
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
        String text = null;
        for (YAWLVertex vertex : pair.getVertexSet()) {
            TaskResourceSet resources = handler.getTaskResources(
                    pair.getNet().getID(), vertex.getID());
            if (resources == null) {
                text = "None";
                break;
            }
            if (text == null) text = resources.getInitiatorChars();
            else if (! text.equals(resources.getInitiatorChars())) {
                text = "None";
                break;
            }
        }
        pair.setSimpleText(text != null ? text : "None");
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

    private void updateVertexID(YAWLVertex vertex, String id) {
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


    private boolean allInstanceOf(Class<?> c) {
        for (YAWLVertex vertex : vertexSet) {
            if (! c.isInstance(vertex)) return false;
        }
        return true;
    }


    private Decorator getDecorator(boolean isSplit) {
        Decorator decorator = null;
        for (YAWLVertex vertex : vertexSet) {
            YAWLTask task = (YAWLTask) vertex;
            Decorator thisDecorator = isSplit ? task.getSplitDecorator() :
                    task.getJoinDecorator();
            if (thisDecorator == null) return null;
            if (decorator == null) decorator = thisDecorator;
            else if (decorator.getType() != thisDecorator.getType()) {
                return null;
            }
        }
        return decorator;
    }

}
