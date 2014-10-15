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

package org.yawlfoundation.yawl.editor.ui.data.editorpane;

import org.bounce.text.LineNumberMargin;
import org.bounce.text.xml.XMLFoldingMargin;
import org.yawlfoundation.yawl.editor.ui.data.Validity;
import org.yawlfoundation.yawl.editor.ui.swing.JUtilities;
import org.yawlfoundation.yawl.editor.ui.swing.MoreDialog;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;

public class ProblemReportingEditorPane extends JPanel
        implements XMLStyledDocumentValidityListener {

    private ValidityEditorPane editor;
    private final JScrollPane editorScrollPane;
    private JTextArea errorBar;
    private JPanel rowHeader;
    private boolean showLineNumbers = true;
    private List<String> detailedErrors;


    public ProblemReportingEditorPane(ValidityEditorPane editor) {
        super();
        setEditor(editor);
        setLayout(new BorderLayout());
        editorScrollPane = new JScrollPane(editor);
        add(editorScrollPane, BorderLayout.CENTER);
        add(buildProblemPanel(), BorderLayout.SOUTH);
        createRowHeader();
    }

    public ProblemReportingEditorPane(ValidityEditorPane editor, boolean showNumbers) {
        this(editor);
        showLineNumbers = showNumbers;
        if (showNumbers) editorScrollPane.setRowHeaderView(rowHeader);
    }

    public void requestFocus() {
        editor.requestFocus();
    }


    public ValidityEditorPane getEditor() { return editor; }


    public String getText() { return editor.getText(); }

    public void setText(String text) { editor.setText(text); }


    public void toggleShowLineNumbers() {
        showLineNumbers = !showLineNumbers;
        editorScrollPane.setRowHeaderView(showLineNumbers ? rowHeader : null);
        editor.updateUI();
    }

    public void findText(String textToFind) {
        editor.getHighlighter().removeAllHighlights();
        if ((textToFind == null) || (textToFind.length() == 0) || editor.getText() == null) {
            return;
        }
        String textToSearch = editor.getText().replaceAll("\r", "");
        int caretPos = editor.getCaretPosition();
        int foundPos = StringUtil.find(textToSearch, textToFind, caretPos, true);

        if (foundPos == -1 && JOptionPane.showConfirmDialog(this,
                    "'" + textToFind + "' not found (fowards). Try from start?",
                    "Text not found", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            foundPos = StringUtil.find(textToSearch, textToFind, 0, true);
        }

        if (foundPos > -1) {
            editor.select(foundPos, foundPos + textToFind.length());
            try {
                editor.getHighlighter().addHighlight(foundPos,
                        foundPos + textToFind.length(), DefaultHighlighter.DefaultPainter);
            } catch (BadLocationException e) {
                // just don't highlight!
            }
        }
        else {
            JOptionPane.showMessageDialog(this, "'" + textToFind + "' not found.",
                                "Text not found", JOptionPane.WARNING_MESSAGE);
        }
        editor.requestFocusInWindow();
    }

    public boolean isContentValid() {
        return editor.isContentValid();
    }

    public void documentValidityChanged(Validity documentValid) {
        showProblems(editor.getProblemList());
    }


    public void showProblems(List<String> problemList) {
        if (! (problemList == null || problemList.isEmpty())) {

            // remove the dummy element name from error message
            String topMessage = problemList.get(0).replace(" 'foo_bar'", "");
            if (problemList.size() > 1) {
                topMessage += " [Click for more...]";
                problemList.remove(0);
                detailedErrors = problemList;
            }
            errorBar.setText(topMessage);
            errorBar.setCaretPosition(0);
            setProblemPanelForeground(true);
        }
        else {
            detailedErrors = null;
            errorBar.setText("OK");
            setProblemPanelForeground(false);
        }
    }

    public String getErrorBarText() { return errorBar.getText(); }

    public boolean showsErrors() {
        String text = getErrorBarText();
        return ! (text == null || text.isEmpty() || text.equals("OK"));
    }


    private JScrollPane buildProblemPanel() {
        errorBar = new JTextArea();
        errorBar.setBackground(this.getBackground());
        errorBar.setWrapStyleWord(true);
        errorBar.setLineWrap(true);
        Font font = errorBar.getFont().deriveFont(11f);
        errorBar.setFont(font);
        errorBar.setEditable(false);
        errorBar.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                showDetails();
                super.mouseClicked(mouseEvent);
            }
        });

        JScrollPane problemScrollPane = new JScrollPane(errorBar);
        problemScrollPane.setPreferredSize(
                new Dimension((int) errorBar.getPreferredSize().getWidth(),
                        font.getSize() * 3));
        problemScrollPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
        return problemScrollPane;
    }


    private void showDetails() {
        if (detailedErrors != null) {
            MoreDialog dialog = new MoreDialog(JUtilities.getWindow(this), detailedErrors);
            dialog.setLocationAdjacentTo(errorBar, errorBar.getVisibleRect());
            dialog.setVisible(true);
        }
    }


    private void createRowHeader() {
        rowHeader = new JPanel(new BorderLayout());
        rowHeader.add(new ImprovedLineNumberMargin(editor), BorderLayout.WEST);
        try {
            rowHeader.add(new ImprovedXMLFoldingMargin(editor), BorderLayout.EAST);
        }
        catch (IOException ioe) {
            // we can live without folding
        }
    }


    private void setProblemPanelForeground(boolean error) {
        errorBar.setForeground(error ? ValidityEditorPane.INVALID_COLOR
                : ValidityEditorPane.VALID_COLOR);
    }

    private void setEditor(ValidityEditorPane editor) {
        this.editor = editor;
        this.editor.acceptValiditySubscription(this);
    }


    class ImprovedLineNumberMargin extends LineNumberMargin {

        ImprovedLineNumberMargin(JTextComponent textComponent) { super(textComponent); }

        // override to add anti-aliasing to numbers
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D)g;
            RenderingHints rh = new RenderingHints(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
            g2.setRenderingHints(rh);
            super.paint(g);
        }
    }


    class ImprovedXMLFoldingMargin extends XMLFoldingMargin {

        ImprovedXMLFoldingMargin(JTextComponent textComponent) throws IOException {
            super(textComponent);
        }

        // override to add anti-aliasing
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D)g;
            RenderingHints rh = new RenderingHints(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
            g2.setRenderingHints(rh);
            super.paint(g);
        }
    }

}
