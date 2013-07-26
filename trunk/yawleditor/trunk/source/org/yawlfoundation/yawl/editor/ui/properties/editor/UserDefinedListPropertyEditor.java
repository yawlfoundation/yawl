package org.yawlfoundation.yawl.editor.ui.properties.editor;

import org.yawlfoundation.yawl.editor.ui.properties.UserDefinedAttributes;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class UserDefinedListPropertyEditor extends ComboPropertyEditor {

    public UserDefinedListPropertyEditor() {
        super();
        setAvailableValues(UserDefinedAttributes.getInstance().getSelectedListValues());
    }

}

