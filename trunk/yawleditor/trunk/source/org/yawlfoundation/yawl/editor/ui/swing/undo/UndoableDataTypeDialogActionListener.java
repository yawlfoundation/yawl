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

package org.yawlfoundation.yawl.editor.ui.swing.undo;


import org.yawlfoundation.yawl.editor.ui.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.ui.util.LogWriter;
import org.yawlfoundation.yawl.editor.ui.data.document.AbstractXMLStyledDocument;
import org.yawlfoundation.yawl.editor.ui.swing.menu.DataTypeDialogToolBarMenu;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.event.ActionEvent;

/**
 * Author: Michael Adams
 * Creation Date: 12/06/2008
 */
public class UndoableDataTypeDialogActionListener implements UndoableEditListener {

    private static UndoableDataTypeDialogActionListener _me;

    private UndoAction undoAction;
    private RedoAction redoAction;
    private final UndoManager undo = new UndoManager();

    public static UndoableDataTypeDialogActionListener getInstance() {
        if (_me == null) _me = new UndoableDataTypeDialogActionListener();
        return _me;
    }
    
    public void undoableEditHappened(UndoableEditEvent e) {
            undo.addEdit(e.getEdit());
            undoAction.updateUndoState();
            redoAction.updateRedoState();
    }

    protected void validateSchema() {
        AbstractXMLStyledDocument doc =  (AbstractXMLStyledDocument)
        DataTypeDialogToolBarMenu.getEditorPane().getEditor().getDocument();
        doc.publishValidity();
    }

    public UndoAction getUndoAction() {
        if (undoAction == null) undoAction = new UndoAction();
        return undoAction;
    }

    public RedoAction getRedoAction() {
        if (redoAction == null) redoAction = new RedoAction();
        return redoAction;
    }

    /*****************************************************************************/

    class UndoAction extends YAWLBaseAction {

        {
          putValue(Action.SHORT_DESCRIPTION, " Undo the last action ");
          putValue(Action.NAME, "Undo");
          putValue(Action.LONG_DESCRIPTION, "Undoes last action");
          putValue(Action.SMALL_ICON, getPNGIcon("arrow_undo"));
          putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Z"));
        }


        public UndoAction() {
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undo.undo();
                validateSchema();
            } catch (CannotUndoException ex) {
                LogWriter.error("Unable to undo: ", ex);
            }
            updateUndoState();
            redoAction.updateRedoState();
        }

        protected void updateUndoState() {
             setEnabled(undo.canUndo());
        }
    }

    /******************************************************************************/

    class RedoAction extends YAWLBaseAction {

        {
          putValue(Action.SHORT_DESCRIPTION, " Redo the last undone action ");
          putValue(Action.NAME, "Redo");
          putValue(Action.LONG_DESCRIPTION, "Redo the last undone action");
          putValue(Action.SMALL_ICON, getPNGIcon("arrow_redo"));
          putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_R));
          putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Y"));
        }


        public RedoAction() {
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undo.redo();
                validateSchema();
            } catch (CannotRedoException ex) {
                LogWriter.error("Unable to redo: ", ex);
            }
            updateRedoState();
            undoAction.updateUndoState();
        }

        protected void updateRedoState() {
          setEnabled(undo.canRedo());
        }
    }


}
