package org.yawlfoundation.yawl.editor.ui.swing.data;

import net.sf.saxon.s9api.SaxonApiException;
import org.yawlfoundation.yawl.util.SaxonUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 8/05/12
 */
class XQueryStyledDocument extends AbstractXMLStyledDocument {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private static final SimpleXQueryParser parser = new SimpleXQueryParser();

  private String preEditorText;
  private String postEditorText;

  private String parseError = null;

  private static final IgnoreBadCharactersFilter IGNORE_BAD_CHARACTERS_FILTER
      = new IgnoreBadCharactersFilter();

  public XQueryStyledDocument(ValidityEditorPane editor) {
    super(editor);
    setDocumentFilter(IGNORE_BAD_CHARACTERS_FILTER);
    setPreAndPostEditorText("","");
  }

  public void setPreAndPostEditorText(String preEditorText, String postEditorText) {
    this.preEditorText = preEditorText;
    this.postEditorText = postEditorText;
  }

  public void checkValidity() {
    if (isValidating()) {
      if (getEditor().getText().equals("")) {
          parseError = "Query required";
        setContentValid(Validity.INVALID);
        return;
      }

      if (getEditor().getText().matches(
              "^\\s*timer\\(\\w+\\)\\s*!?=\\s*'(dormant|active|closed|expired)'\\s*$")) {
          setContentValid(Validity.VALID);
          return;
      }

      try {
        SaxonUtil.compileXQuery(preEditorText + getEditor().getText() + postEditorText);
        setContentValid(Validity.VALID);
        parseError = null;
      }
      catch (SaxonApiException e) {
        parseError = e.getMessage().split("\n")[1].trim();
        setContentValid(Validity.INVALID);
      }
    }
  }

  public List getProblemList() {
    LinkedList problemList = new LinkedList();
    problemList.add(parseError);
    return problemList;
  }
}
