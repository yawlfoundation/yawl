package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor;

import java.util.List;

/******************************************************************************/

public class ComboPropertyEditor extends ComboBoxPropertyEditor {

    public ComboPropertyEditor() { super(); }

    public ComboPropertyEditor(List<String> items) {
        this(items.toArray());
       }

    public ComboPropertyEditor(Object[] items) {
        this();
        setAvailableValues(items);
    }

}
