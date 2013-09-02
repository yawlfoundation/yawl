package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.propertysheet.Property;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YNet;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;

/**
* @author Michael Adams
* @date 13/07/12
*/
public class SubNetNameEditor extends ComboPropertyEditor {

    public SubNetNameEditor() {
        super();
        java.util.List<String> items = new ArrayList<String>();
        for (YNet net : SpecificationModel.getHandler().getControlFlowHandler().getSubNets()) {
            items.add(net.getID());
        }
        Collections.sort(items);
        items.add(0, "Rename...");
        items.add(0, "New...");
        items.add(0, "None");
        setAvailableValues(items.toArray());
    }


    public void rationaliseItems(Property property) {
        if ("None".equals(property.getValue())) {
            ((JComboBox) editor).removeItemAt(2);   // Rename...
        }
    }

}
