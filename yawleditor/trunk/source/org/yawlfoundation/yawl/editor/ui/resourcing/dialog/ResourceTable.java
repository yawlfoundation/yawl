package org.yawlfoundation.yawl.editor.ui.resourcing.dialog;

import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.tablemodel.AbstractResourceTableModel;
import org.yawlfoundation.yawl.editor.ui.swing.JSingleSelectTable;

import javax.swing.*;
import java.awt.*;

/**
* @author Michael Adams
* @date 3/08/12
*/
public class ResourceTable extends JSingleSelectTable {


    public ResourceTable(AbstractResourceTableModel model) {
        super();
        setModel(model);
        setRowHeight(getRowHeight() + 5);
        setColumnSelectionAllowed(false);
        setTableHeader(null);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);  // override
        setFillsViewportHeight(true);            // to allow drops on empty table
    }


    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setBackground(enabled ? Color.WHITE :
                UIManager.getDefaults().getColor("TextArea.inactiveBackground"));
        ((AbstractResourceTableModel) getModel()).setEnabled(enabled);
    }

}
