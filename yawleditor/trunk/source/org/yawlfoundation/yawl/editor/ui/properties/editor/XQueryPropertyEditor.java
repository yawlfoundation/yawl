package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.ui.swing.data.DialogMode;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public abstract class XQueryPropertyEditor extends DialogPropertyEditor {

    private String currentQuery;
    private DialogMode mode;

    public XQueryPropertyEditor(DialogMode mode) {
        super(new DefaultCellRenderer());
        this.mode = mode;
    }

    public Object getValue() {
        return currentQuery;
    }

    public void setValue(Object value) {
        currentQuery = (String) value;
        ((DefaultCellRenderer) label).setValue(currentQuery);
    }

    protected void showDialog() {
  //      XQueryUpdateDialog xqDialog = new XQueryUpdateDialog(editor, mode);
     //   xqDialog.setExtendedAttribute(this);                 //todo
//        String newQuery = xqDialog.showDialog();
//        if (! (newQuery == null || newQuery.equals(currentQuery))) {
//            String oldQuery = currentQuery;
//            setValue(newQuery);
//            firePropertyChange(oldQuery, newQuery);
//        }
    }

}



