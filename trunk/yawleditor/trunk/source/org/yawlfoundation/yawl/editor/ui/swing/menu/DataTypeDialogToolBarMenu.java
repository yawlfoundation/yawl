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

package org.yawlfoundation.yawl.editor.ui.swing.menu;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.editor.core.repository.Repo;
import org.yawlfoundation.yawl.editor.ui.actions.datatypedialog.*;
import org.yawlfoundation.yawl.editor.ui.data.document.AbstractXMLStyledDocument;
import org.yawlfoundation.yawl.editor.ui.data.editorpane.ValidityEditorPane;
import org.yawlfoundation.yawl.editor.ui.data.editorpane.XMLSchemaEditorPane;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryAddAction;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryGetAction;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryRemoveAction;
import org.yawlfoundation.yawl.editor.ui.swing.undo.UndoableDataTypeDialogActionListener;
import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

public class DataTypeDialogToolBarMenu extends YToolBar {

    private static DataTypeDialogToolBarMenu _me;
    private static XMLSchemaEditorPane editorPane;
    private final JDialog _owner;

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

    public String getSelectedTextQualified() {
        String allText = getEditorPane().getText();
        if (allText != null) {
            Element e = JDOMUtil.stringToElement(allText);
            if (e != null) {
                Namespace ns = e.getNamespace();
                if (ns != null) {
                    return wrapContent(getSelectedText(), ns.getPrefix(), ns.getURI());
                }
            }
        }
        return getSelectedText();
    }

    public void insertText(String text) {
        insertText(text, false);
    }

    public void insertText(String text, boolean reformat) {
        ValidityEditorPane editor = getEditorPane().getEditor();
        prepareAndInsertText(text);
        int caretPos = editor.getCaretPosition();
        if (reformat) {
            editor.setText(XMLUtilities.formatXML(editor.getText(), true, false));
        }
        editor.setCaretPosition(Math.min(caretPos, editor.getText().length()));
    }


    private static void setEditorPane(XMLSchemaEditorPane pane) {
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
        formatButton = new YAWLToolBarButton(new ReformatDataTypeDialogAction());
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


    private String wrapContent(String content, String nsPrefix, String nsURI) {
        StringBuilder s = new StringBuilder("<temp");
        if (! (nsPrefix == null || nsURI == null)) {
            s.append(" xmlns:").append(nsPrefix).append("=\"").append(nsURI).append('\"');
        }
        s.append('>');
        s.append(content);
        s.append("</temp>");
        return s.toString();
    }


    private void prepareAndInsertText(String text) {
        if (text == null || text.isEmpty()) return;
        ValidityEditorPane editor = getEditorPane().getEditor();
        Element toInsert = JDOMUtil.stringToElement(text);
        if (toInsert != null) {
            int start = editor.getSelectionStart();
            int end =  editor.getSelectionEnd();
            int len = editor.getText().length();
            getEditorPane().setText(insertAdditionalNamespaces(toInsert));
            int offset = editor.getText().length() - len;
            editor.setSelectionStart(start + offset);
            editor.setSelectionEnd(end + offset);
        }
        editor.replaceSelection(StringUtil.unwrap(text));
    }


    private String insertAdditionalNamespaces(Element toInsert) {
        String currentText = getEditorPane().getText();
        int start = currentText.indexOf('<');
        int end = currentText.indexOf('>');
        if (start > -1 && end > -1) {
            String oldHeader = currentText.substring(start, end);
            String newHeader = insertNamespace(oldHeader, toInsert.getNamespace());
            for (Namespace ns : toInsert.getAdditionalNamespaces()) {
                newHeader = insertNamespace(newHeader, ns);
            }
            if (! newHeader.equals(oldHeader)) {
                return currentText.replaceFirst(currentText.substring(start, end), newHeader);
            }
        }
        return currentText;
    }


    private String assembleNameSpaceString(Namespace ns) {
        String prefix = ns.getPrefix();
        return prefix.isEmpty() ? null : " xmlns:" + prefix + "=\"" + ns.getURI() + "\"";
    }

    private String insertNamespace(String header, Namespace ns) {
        if (! (header == null || ns == null)) {
            String nsStr = assembleNameSpaceString(ns);
            if (! (nsStr == null || header.contains(nsStr))) {
                return header + nsStr;
            }
        }
        return header;
    }

}