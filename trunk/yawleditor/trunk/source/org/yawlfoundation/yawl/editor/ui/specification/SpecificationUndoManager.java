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

package org.yawlfoundation.yawl.editor.ui.specification;

import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphUndoManager;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.RedoAction;
import org.yawlfoundation.yawl.editor.ui.actions.UndoAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.elements.YExternalNetElement;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import java.util.HashSet;
import java.util.Set;

/**
 * The <code>SpecificationUndoManager</code> is a special case of an <code>UndoManager</code> 
 * where the manager can be told to accept incoming edit events or not.<p>
 * The intent behind this is to switch off acceptance of edits when models relying on
 * the <code>SpecificationUndoMananger</code> need to publish a number of edits (for example, when 
 * loading an exisiting file, or supplying a set of default elements on a graph) to the manager
 * that should not be undoable by the user.<p>
 * The <code>SpecificationUndoManager</code> is also responsible for ensuring the the editor's 
 * undo and redo Actions ({@link UndoAction} and {@link RedoAction} respectively)  are 
 * appropriately enabled for the manager's current state.
 *
 * @author Lindsay Bradford
 *
 */

public class SpecificationUndoManager extends GraphUndoManager {

    private static final SpecificationUndoManager INSTANCE = new SpecificationUndoManager();

    private CompoundEdit compoundEdit;
    private int     nonAcceptanceLevel = 0;
    private boolean compoundingEdits = false;
    private boolean dirty = false ;
    private Set<YExternalNetElement> removedYNetElements;

    public static SpecificationUndoManager getInstance() {
        return INSTANCE;
    }

    private SpecificationUndoManager() {
        super();
        setLimit(500);
        removedYNetElements = new HashSet<YExternalNetElement>();
    }

    /**
     * Constructor that allows the caller to specify whether the
     * returned <code>SpecificationUndoManager</code> initially accepts edits
     * or not.
     *
     * @param acceptEdits whether to accept incoming edits
     * @see #acceptEdits
     **/

    public SpecificationUndoManager(boolean acceptEdits) {
        acceptEdits(acceptEdits);
    }

    /**
     * Toggles whether incoming undoable edits should be
     * accepted by this manager.
     *
     * @param acceptEdits whether to accept incoming edits
     */
    public void acceptEdits(boolean acceptEdits) {
        if (acceptEdits) {
            if (nonAcceptanceLevel > 0) {
                nonAcceptanceLevel --;
            }
        }
        else {
            nonAcceptanceLevel++;
        }
    }

    /**
     * Adds incoming undoable edit to the list of valid events
     * managed if we are currently accepting edits.
     *
     * @see #acceptEdits
     * @see UndoableEditEvent
     */

    public void undoableEditHappened(UndoableEditEvent event) {
        UndoableEdit edit = event.getEdit();
        if (acceptingEdits()) {
            if (compoundingEdits) {
                compoundEdit.addEdit(edit);
            }
            else {
                addEdit(edit);
            }
            setDirty(true);
            refreshButtons();
        }
    }

    /**
     * Undoes the next undoable edit and
     * refreshes the enabled states of the
     * UndoAction and RedoAction singletons.
     *
     * @see org.yawlfoundation.yawl.editor.ui.actions.UndoAction
     * @see org.yawlfoundation.yawl.editor.ui.actions.RedoAction
     */

    public void undo() {
        showFrameOfEdit(editToBeUndone());
        updateElementSet(editToBeUndone());
        if (canUndo()) super.undo();
        refreshButtons();
    }

    /**
     * Redoes the next redoable edit and
     * resets the enabled states of the
     * UndoAction and RedoAction singletons.
     *
     * @see org.yawlfoundation.yawl.editor.ui.actions.UndoAction
     * @see org.yawlfoundation.yawl.editor.ui.actions.RedoAction
     */

    public void redo() {
        showFrameOfEdit(editToBeRedone());
        updateElementSet(editToBeRedone());
        super.redo();
        refreshButtons();
    }

    /**
     * Discards all edits and
     * resets the enabled states of the
     * UndoAction and RedoAction singletons.
     *
     * @see org.yawlfoundation.yawl.editor.ui.actions.UndoAction
     * @see org.yawlfoundation.yawl.editor.ui.actions.RedoAction
     */

    public void discardAllEdits() {
        super.discardAllEdits();
        clearYNetElementSet();
        setDirty(false);
        refreshButtons();
    }

    public void refreshButtons() {
        UndoAction.getInstance().setEnabled(canUndo());
        RedoAction.getInstance().setEnabled(canRedo());
    }

    public void disableButtons() {
        UndoAction.getInstance().setEnabled(false);
        RedoAction.getInstance().setEnabled(false);
    }

    public void startCompoundingEdits() {
        this.startCompoundingEdits(null);
    }

    /**
     * Tells the undo mamager to take all edits received
     * until <code>stopCompoundingEdits()</code> is called
     * and combine then into a single undoable edit.
     *
     */

    public void startCompoundingEdits(NetGraphModel model) {
        if (!compoundingEdits) {
            compoundingEdits = true;
            this.compoundEdit = new SpecificationCompoundEdit(model);
        }
    }

    /**
     * Tells the undo mamager to post the currently
     * compounded edits received as a single undoable edit
     * and to stop compounding edits from now on.
     *
     */

    public void stopCompoundingEdits() {
        if (compoundingEdits) {
            compoundingEdits = false;
            compoundEdit.end();
            addEdit(compoundEdit);
            refreshButtons();
            compoundEdit = null;
        }
    }


    public UndoableEdit getLastEdit() {
        return compoundEdit == null ? null :
                ((SpecificationCompoundEdit) compoundEdit).getLastEdit();
    }


    public Set<YExternalNetElement> getRemovedYNetElements() {
        return removedYNetElements;
    }

    public void clearYNetElementSet() { removedYNetElements.clear(); }


    private boolean acceptingEdits() {
        return nonAcceptanceLevel <= 0;
    }

    private void showFrameOfEdit(UndoableEdit edit) {
        if (edit == null) {
            return;
        }
        if (edit instanceof DefaultGraphModel.GraphModelEdit) {
            DefaultGraphModel.GraphModelEdit gmEdit = (DefaultGraphModel.GraphModelEdit) edit;
            if (gmEdit.getSource() instanceof NetGraphModel) {
                showFrameOfModel((NetGraphModel) gmEdit.getSource());
            }
        }
        else if (edit instanceof SpecificationCompoundEdit) {
            SpecificationCompoundEdit scEdit = (SpecificationCompoundEdit) edit;
            if (scEdit.getModel() != null) {
                showFrameOfModel(scEdit.getModel());
            }
        }
    }


    private DefaultGraphModel.GraphModelEdit getGraphModelEdit(UndoableEdit edit) {
        return (edit instanceof DefaultGraphModel.GraphModelEdit) ?
                (DefaultGraphModel.GraphModelEdit) edit : null;
    }

    private void showFrameOfModel(NetGraphModel model) {
        YAWLEditor.getNetsPane().setSelectedTab(model.getName());
    }


    private void updateElementSet(UndoableEdit edit) {
        DefaultGraphModel.GraphModelEdit gmEdit = getGraphModelEdit(edit);
        if (gmEdit != null) {
            Object[] aboutToBeRemoved = gmEdit.getInserted();
            Object[] aboutToBeInserted = gmEdit.getRemoved();
            if (aboutToBeRemoved != null) {
                for (Object o : aboutToBeRemoved) {
                    if (o instanceof YAWLVertex) {
                        removedYNetElements.add(((YAWLVertex) o).getYAWLElement());
                    }
                }
            }
            else if (aboutToBeInserted != null) {
                for (Object o : aboutToBeInserted) {
                    if (o instanceof YAWLVertex) {
                        removedYNetElements.remove(((YAWLVertex) o).getYAWLElement());
                    }
                }
            }
        }
    }

    public boolean isDirty() {
        return dirty ;
    }

    public void setDirty(boolean newValue) {
        dirty = newValue;
        if (dirty) YAWLEditor.getInstance().markTitleAsDirty();
    }

    public void removeLastUndoableEdit() {
        editToBeUndone().die();
    }


    /******************************************************************/

    public class SpecificationCompoundEdit extends CompoundEdit {
        private final NetGraphModel model;

        public SpecificationCompoundEdit(NetGraphModel model) {
            super();
            this.model = model;
        }

        public NetGraphModel getModel() {
            return this.model;
        }

        public UndoableEdit getLastEdit() { return lastEdit(); }
    }
}
