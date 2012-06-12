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
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.util.List;

public class JProblemReportingEditorPane extends JSplitPane 
                                         implements AbstractXMLStyledDocumentValidityListener{
  private static final long serialVersionUID = 1L;

  private ValidityEditorPane editor;

  private JScrollPane  problemScrollPane;
  private JScrollPane  editorScrollPane;
  private JTextArea lineNumberArea;
//  private ProblemTable parseProblemsTable = new ProblemTable();
  private JTextArea parseProblemsArea;
  protected boolean showLineNumbers = false;

  public JProblemReportingEditorPane(ValidityEditorPane editor) {
    super(JSplitPane.VERTICAL_SPLIT);
    setEditor(editor);    
    initialise();
  }

    public JProblemReportingEditorPane(ValidityEditorPane editor, boolean lineNumbers) {
      super(JSplitPane.VERTICAL_SPLIT);
      setEditor(editor);
      setShowLineNumbers(lineNumbers);
      initialise();
    }

  private void initialise() {
    setTopComponent(buildEditorPanel());
    setBottomComponent(buildProblemPanel());
    setDividerSize(10);
    setContinuousLayout(false);
    setResizeWeight(1);

  }
  
  private JScrollPane buildEditorPanel() {
      editorScrollPane = new JScrollPane(editor);
      if (showLineNumbers) editorScrollPane.setRowHeaderView(buildLineNumberArea());
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
  
  public void requestFocus() {
    editor.requestFocus();
  }

  private JScrollPane buildProblemPanel() {
    parseProblemsArea = new JTextArea();
    parseProblemsArea.setWrapStyleWord(true);
    parseProblemsArea.setLineWrap(true);
    parseProblemsArea.setFont(getEditor().getFont());
    parseProblemsArea.setEditable(false);
    parseProblemsArea.setBackground(Color.PINK.brighter());
    problemScrollPane = new JScrollPane(parseProblemsArea);
    return problemScrollPane;
  }
  
  private void setEditor(ValidityEditorPane editor) {
    this.editor = editor;
    this.editor.acceptValiditySubscription(this);
  }
  
  public ValidityEditorPane getEditor() {
    return this.editor;
  }
  
  public String getText() {
    return editor.getText();
  }
  
  public void setText(String text) {
    editor.setText(text);
    if (editor.isContentValid()) {
      hideProblemTable();
    }
  }

    protected void setLineNumbers() {
        setLineNumbers(editor.getText());
    }

    protected void setLineNumbers(int caretPos) {
        String text = editor.getText();
        setLineNumbers(text);
        int linecount = 0;
        if (caretPos > -1) {                           // what line are we on?
            for (int i=0; i < caretPos; i++) {
                if (text.charAt(i) == '\n') {
                    linecount++;
                }
            }
        }
        String numberText = lineNumberArea.getText();     // move to same line 
        int numberCaret = 0;
        while (linecount > 0) {
            if (numberText.charAt(numberCaret++) == '\n') {
                linecount--;
            }
        }
        lineNumberArea.setCaretPosition(numberCaret);
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
  
  protected void populateProblemListTable(List problemList) {
      if (problemList != null && problemList.size() > 0) {
          parseProblemsArea.setText((String) problemList.get(0));
      }
      else parseProblemsArea.setText(null);

    setDefaultDividerLocation();
  }
  
  public void hideProblemTable() {
    problemScrollPane.setVisible(false);
    setDividerLocation(1.0);
  }
  
  public void refreshDividerLocation() {
    setDefaultDividerLocation();
  }

    private void setDefaultDividerLocation() {
        if (getHeight() > 0) {
            if (parseProblemsArea.getText() == null) {
                hideProblemTable();
            }
            else {
                problemScrollPane.setVisible(true);
                setDividerLocation(calcDividerLocation());
            }
        }
    }

    private double calcDividerLocation() {
        return 1.0 - 40.0 / getHeight();
    }
  
  public void documentValidityChanged(AbstractXMLStyledDocument.Validity documentValid) {
    if (editor.getText().equals("") && documentValid == AbstractXMLStyledDocument.Validity.VALID) {
      hideProblemTable();
      return;
    }
    
    if (documentValid == AbstractXMLStyledDocument.Validity.VALID) {
      populateProblemListTable(null);
       hideProblemTable();
    } else {
      populateProblemListTable(null);
      populateProblemListTable(
          editor.getProblemList()
      );
    }
  }
  
}
