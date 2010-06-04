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

package org.yawlfoundation.yawl.editor.swing.menu;

import org.yawlfoundation.yawl.editor.actions.*;
import org.yawlfoundation.yawl.editor.swing.data.AbstractXMLStyledDocument;
import org.yawlfoundation.yawl.editor.swing.data.JXMLSchemaEditorPane;
import org.yawlfoundation.yawl.editor.swing.undo.UndoableDataTypeDialogActionListener;

import java.awt.*;

public class DataTypeDialogToolBarMenu extends YAWLToolBar {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private static DataTypeDialogToolBarMenu _me ;
  private static JXMLSchemaEditorPane editorPane ;

  private YAWLToolBarButton cutButton ;
  private YAWLToolBarButton copyButton ;
  private YAWLToolBarButton pasteButton ;


  public DataTypeDialogToolBarMenu(JXMLSchemaEditorPane pane) {
    super("DataType Dialog ToolBar");
    setEditorPane(pane) ;
    _me = this;
  }

  public static DataTypeDialogToolBarMenu getInstance() {
      return _me ;
  }

    public static JXMLSchemaEditorPane getEditorPane() {
        return editorPane;
    }

    public static void setEditorPane(JXMLSchemaEditorPane pane) {
        editorPane = pane;
        AbstractXMLStyledDocument doc =
                        (AbstractXMLStyledDocument) editorPane.getEditor().getDocument();
        doc.addUndoableEditListener(UndoableDataTypeDialogActionListener.getInstance());
    }

    protected void buildInterface() {
    setMargin(new Insets(3,2,2,0));
    cutButton = new YAWLToolBarButton(new CutDataTypeDialogAction()) ;
    add(cutButton);
    copyButton = new YAWLToolBarButton(new CopyDataTypeDialogAction());    
    add(copyButton);
    pasteButton = new YAWLToolBarButton(new PasteDataTypeDialogAction());
    pasteButton.setEnabled(false);    
    add(pasteButton);
    addSeparator();
    add(new YAWLToolBarButton(UndoableDataTypeDialogActionListener.getInstance().getUndoAction()));
    add(new YAWLToolBarButton(UndoableDataTypeDialogActionListener.getInstance().getRedoAction()));
  }

  public YAWLToolBarButton getButton(String btype) {
      if (btype.equals("cut")) return cutButton ;
      if (btype.equals("copy")) return copyButton ;
      if (btype.equals("paste")) return pasteButton ;
      return null;
  }


    
}