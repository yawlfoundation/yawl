/*
 * Created on 15/02/2006
 * YAWLEditor v1.4
 *
 * @author Lindsay Bradford
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
 */

package org.yawlfoundation.yawl.editor.ui.swing.data;

import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.util.List;

public class JProblemReportingEditorPane extends JPanel
        implements AbstractXMLStyledDocumentValidityListener{

    private ValidityEditorPane editor;
    private JScrollPane editorScrollPane;
    private JTextArea lineNumberArea;
    private JTextArea errorBar;
    protected boolean showLineNumbers = false;

    private static final Color errorForeground = Color.RED.darker();
    private static final Color okForeground = Color.GREEN.darker();


    public JProblemReportingEditorPane(ValidityEditorPane editor) {
        super();
        setEditor(editor);
        setLayout(new BorderLayout());
        add(buildEditorPanel(), BorderLayout.CENTER);
        add(buildProblemPanel(), BorderLayout.SOUTH);
    }

    public JProblemReportingEditorPane(ValidityEditorPane editor, boolean showLineNumbers) {
        this(editor);
        setShowLineNumbers(showLineNumbers);
        if (showLineNumbers) editorScrollPane.setRowHeaderView(buildLineNumberArea());
    }


    public void requestFocus() {
        editor.requestFocus();
    }


    public JScrollPane getEditorScrollPane() { return editorScrollPane; }

    public ValidityEditorPane getEditor() { return editor; }


    public String getText() { return editor.getText(); }

    public void setText(String text) { editor.setText(text); }


    protected void setLineNumbers() { setLineNumbers(editor.getText()); }

    protected void setLineNumbers(int caretPos) {
        String text = editor.getText();
        setLineNumbers(text);
        int lineCount = 0;
        if (caretPos > -1) {                           // what line are we on?
            for (int i=0; i < caretPos; i++) {
                if (text.charAt(i) == '\n') {
                    lineCount++;
                }
            }
        }
        String numberText = lineNumberArea.getText();     // move to same line 
        int numberCaret = 0;
        while (lineCount > 0) {
            if (numberText.charAt(numberCaret++) == '\n') {
                lineCount--;
            }
        }
        lineNumberArea.setCaretPosition(numberCaret);
    }


    public void setShowLineNumbers(boolean show) {
        showLineNumbers = show;
        if (lineNumberArea != null) {
            if (show) {
                setLineNumbers(getEditor().getCaretPosition());
                editorScrollPane.setRowHeaderView(lineNumberArea);
            }
            else editorScrollPane.setRowHeaderView(null);
        }
    }

    public boolean getShowLineNumbers() { return showLineNumbers; }

    public void toggleShowLineNumbers() {
        setShowLineNumbers(! showLineNumbers);
    }

    public void findText(String textToFind) {
        editor.getHighlighter().removeAllHighlights();
        if ((textToFind == null) || (textToFind.length() == 0) || editor.getText() == null) {
            return;
        }
        String textToSearch = editor.getText().replaceAll("\r", "");
        int caretPos = editor.getCaretPosition();
        int foundPos = StringUtil.find(textToSearch, textToFind, caretPos, true);

        if (foundPos > -1) {
            editor.select(foundPos, foundPos + textToFind.length());
            try {
                editor.getHighlighter().addHighlight(foundPos,
                        foundPos + textToFind.length(), DefaultHighlighter.DefaultPainter);
            } catch (BadLocationException e) {
                // just don't highlight!
            }
        }
        editor.requestFocusInWindow();
    }

    public boolean isContentValid() {
        return editor.isContentValid();
    }

    protected void showProblems(List<String> problemList) {
        if (problemList != null && problemList.size() > 0) {
            errorBar.setText(problemList.get(0));
            setProblemPanelForeground(true);
        }
        else {
            errorBar.setText("OK");
            setProblemPanelForeground(false);
        }
    }


    public void documentValidityChanged(AbstractXMLStyledDocument.Validity documentValid) {
        showProblems(editor.getProblemList());
    }


    private JScrollPane buildEditorPanel() {
        editorScrollPane = new JScrollPane(editor);
        return editorScrollPane;
    }

    private JTextArea buildLineNumberArea() {
        lineNumberArea = new JTextArea("   1 ");
        lineNumberArea.setFont(editor.getFont());
        lineNumberArea.setBackground(new Color(230,230,230));
        lineNumberArea.setBorder(new EmptyBorder(3,0,0,0));
        lineNumberArea.setEditable(false);
        return lineNumberArea;
    }

    private JScrollPane buildProblemPanel() {
        errorBar = new JTextArea();
        errorBar.setBackground(this.getBackground());
        errorBar.setWrapStyleWord(true);
        errorBar.setLineWrap(true);
        Font font = errorBar.getFont().deriveFont(11f);
        errorBar.setFont(font);
        errorBar.setEditable(false);
        JScrollPane problemScrollPane = new JScrollPane(errorBar);
        problemScrollPane.setPreferredSize(
                new Dimension((int) errorBar.getPreferredSize().getWidth(),
                        font.getSize() * 3));
        problemScrollPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
        return problemScrollPane;
    }

    private void setProblemPanelForeground(boolean error) {
        errorBar.setForeground(error ? errorForeground : okForeground);
    }

    private void setEditor(ValidityEditorPane editor) {
        this.editor = editor;
        this.editor.acceptValiditySubscription(this);
    }

    private void setLineNumbers(String text) {
        String lineNumbers = "   1 \n";
        int nextNum = 2;
        for (char c : text.toCharArray()) {
            if (c == '\n') {
                lineNumbers += String.format("%4d \n", nextNum++);
            }
        }
        lineNumbers += String.format("%4d \n", nextNum);
        lineNumberArea.setText(lineNumbers);
    }

}
