package org.yawlfoundation.yawl.editor.ui.properties.editor;

import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YAWLServiceGateway;

import java.util.ArrayList;
import java.util.Collections;

/**
* @author Michael Adams
* @date 13/07/12
*/
public class DecompositionNameEditor extends ComboPropertyEditor {

    public DecompositionNameEditor() {
        super();
        java.util.List<String> items = new ArrayList<String>();
        for (YAWLServiceGateway gateway :
                SpecificationModel.getHandler().getControlFlowHandler().getTaskDecompositions()) {
            items.add(gateway.getID());
        }
        Collections.sort(items);
        items.add(0, "New...");
        items.add(0, "None");
        setAvailableValues(items.toArray());
    }

}
