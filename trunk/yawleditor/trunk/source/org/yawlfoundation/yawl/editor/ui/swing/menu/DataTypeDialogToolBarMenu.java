/*
 * Created on 06/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.ui.swing.menu;

import org.yawlfoundation.yawl.editor.core.repository.Repo;
import org.yawlfoundation.yawl.editor.ui.actions.datatypedialog.*;
import org.yawlfoundation.yawl.editor.ui.data.editorpane.AbstractXMLStyledDocument;
import org.yawlfoundation.yawl.editor.ui.data.editorpane.ValidityEditorPane;
import org.yawlfoundation.yawl.editor.ui.data.editorpane.XMLSchemaEditorPane;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryAddAction;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryGetAction;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryRemoveAction;
import org.yawlfoundation.yawl.editor.ui.swing.undo.UndoableDataTypeDialogActionListener;
import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

public class DataTypeDialogToolBarMenu extends YToolBar {

    private static DataTypeDialogToolBarMenu _me;
    private static XMLSchemaEditorPane editorPane;
    private JDialog _owner;

    private YAWLToolBarButton cutButton;
    private YAWLToolBarButton copyButton;
    private YAWLToolBarButton pasteButton;
    private YAWLToolBarButton formatButton;
    private YAWLToolBarButton repoAddButton;
    private JTextField findText;


    public DataTypeDialogToolBarMenu(JDialog owner, XMLSchemaEditorPane pane) {
        super("DataType Dialog ToolBar");
        _owner = owner;
        setEditorPane(pane);
        _me = this;
    }

    public static DataTypeDialogToolBarMenu getInstance() {
        return _me;
    }

    public static XMLSchemaEditorPane getEditorPane() {
        return editorPane;
    }

    public String getSelectedText() {
        return getEditorPane().getEditor().getSelectedText();
    }

    public void insertText(String text) {
        insertText(text, false);
    }

    public void insertText(String text, boolean reformat) {
        ValidityEditorPane pane = getEditorPane().getEditor();
        pane.replaceSelection(text);
        if (reformat) {
            int caretPos = pane.getCaretPosition();
            pane.setText(XMLUtilities.formatXML(pane.getText(), true, false));
            pane.setCaretPosition(caretPos);
        }
    }


    public static void setEditorPane(XMLSchemaEditorPane pane) {
        editorPane = pane;
        AbstractXMLStyledDocument doc =
                (AbstractXMLStyledDocument) editorPane.getEditor().getDocument();
        doc.addUndoableEditListener(UndoableDataTypeDialogActionListener.getInstance());
    }

    protected void buildInterface() {
        setMargin(new Insets(3, 2, 2, 0));
        cutButton = new YAWLToolBarButton(new CutDataTypeDialogAction());
        add(cutButton);
        copyButton = new YAWLToolBarButton(new CopyDataTypeDialogAction());
        add(copyButton);
        pasteButton = new YAWLToolBarButton(new PasteDataTypeDialogAction());
        pasteButton.setEnabled(shouldEnablePaste());
        add(pasteButton);
        addSeparator();
        add(new YAWLToolBarButton(UndoableDataTypeDialogActionListener.getInstance().getUndoAction()));
        add(new YAWLToolBarButton(UndoableDataTypeDialogActionListener.getInstance().getRedoAction()));
        addSeparator();
        add(new YAWLToolBarButton(new ToggleLineNumbersDataTypeDialogAction(this)));
        formatButton = new YAWLToolBarButton(new ReformatDataTypeDialogAction(this));
        add(formatButton);
        addSeparator();

        repoAddButton = new YAWLToolBarButton(
                new RepositoryAddAction(_owner, Repo.DataDefinition, this));
        add(repoAddButton);
        add(new YAWLToolBarButton(
                new RepositoryGetAction(_owner, Repo.DataDefinition, this)));
        add(new YAWLToolBarButton(
                new RepositoryRemoveAction(_owner, Repo.DataDefinition, this)));
        addSeparator();

        FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
        layout.setHgap(0);
        JPanel innerPanel = new JPanel(layout);
        findText = new JTextField(8);
        innerPanel.add(findText);
        innerPanel.add(new YAWLToolBarButton(new FindTextDataTypeDialogAction(this)));
        add(innerPanel);
    }

    public YAWLToolBarButton getButton(String btype) {
        if (btype.equals("cut")) return cutButton;
        if (btype.equals("copy")) return copyButton;
        if (btype.equals("paste")) return pasteButton;
        if (btype.equals("format")) return formatButton;
        return null;
    }

    public void setOnSelectionEnabled(boolean selected) {
        cutButton.setEnabled(selected);
        copyButton.setEnabled(selected);
        repoAddButton.setEnabled(selected);
    }

    public String getFindText() {
        return findText.getText();
    }


    private boolean shouldEnablePaste() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = clipboard.getContents(null);
            return contents != null &&
                contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        }
        catch (Exception e) {
            return false;
        }
    }


}