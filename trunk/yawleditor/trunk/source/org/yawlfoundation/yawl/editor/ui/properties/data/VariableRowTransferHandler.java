package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.editor.core.data.YDataHandler;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.io.IOException;

/**
 * @author Michael Adams
 * @date 13/08/12
 */
public class VariableRowTransferHandler extends TransferHandler {

    private VariableTable _table;
    private final DataFlavor _variableRowFlavor;


    public VariableRowTransferHandler(VariableTable table) {
        _table = table;
        _variableRowFlavor = new ActivationDataFlavor(VariableRow.class, "VariableRow");
    }


    protected Transferable createTransferable(JComponent c) {
        VariableTable table = (VariableTable) c;
        return new DataHandler(table.getVariables().get(table.getSelectedRow()),
                _variableRowFlavor.getMimeType());
    }

    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY;
    }


    public boolean canImport(TransferSupport support) {
        boolean ok = isTaskTable(support.getComponent()) && support.isDrop() &&
                support.isDataFlavorSupported(_variableRowFlavor) &&
                isValidTransferringRow(support);
        _table.setCursor(ok ? DragSource.DefaultCopyDrop : DragSource.DefaultCopyNoDrop);
        return ok;
    }


    public boolean importData(TransferSupport support) {
        if (! canImport(support)) return false;

        // fetch the drop location
        JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
        int row = dl.getRow();

        // fetch the data and bail if this fails
        VariableRow data;
        try {
            data = getTransferringRow(support);
        }
        catch (UnsupportedFlavorException e) {
            return false;
        }
        catch (IOException e) {
            return false;
        }

        if (! data.isValid()) {
            return false;
        }

        if (! isUniqueRowName(data.getName())) {
            return false;
        }

        int scope = (_table.getTableModel() instanceof TaskInputVarTableModel) ?
                YDataHandler.INPUT : YDataHandler.OUTPUT;

        VariableRow newVariableRow = new VariableRow(scope);
        newVariableRow.setName(data.getName());
        newVariableRow.setDataType(data.getDataType());
        newVariableRow.setDecompositionID(_table.getNetElementName());
        createMapping(data, newVariableRow);
        _table.insertRow(row, newVariableRow);

        Rectangle rect = _table.getCellRect(row, 0, false);
        _table.scrollRectToVisible(rect);
        _table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        return true;
    }


    protected void exportDone(JComponent c, Transferable t, int act) {
        _table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        _table.getTableModel().fireTableDataChanged();
    }


    private VariableRow getTransferringRow(TransferSupport support)
            throws UnsupportedFlavorException, IOException {
        return (VariableRow) support.getTransferable().getTransferData(_variableRowFlavor);
    }


    private boolean isValidTransferringRow(TransferSupport support) {
        try {
            VariableRow row = getTransferringRow(support);
            return (row != null && row.isValid());
        }
        catch (UnsupportedFlavorException e) {
            return false;
        }
        catch (IOException e) {
            return false;
        }
    }


    private boolean isUniqueRowName(String name) {
        for (VariableRow row : _table.getVariables()) {
            if (row.getName().equals(name)) {
                JOptionPane.showMessageDialog(null,
                        "A variable with the same name already exists in this table.",
                        "Transfer Error", JOptionPane.ERROR_MESSAGE);
                _table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                return false;
            }
        }
        return true;
    }


    private boolean isTaskTable(Component component) {
        return ! (((VariableTable) component).getTableModel() instanceof NetVarTableModel);
    }


    private void createMapping(VariableRow netRow, VariableRow taskRow) {
        String mapping;
        if (taskRow.isInput()) {
            mapping = createMapping(netRow);
        }
        else {
            mapping = createMapping(taskRow);
            taskRow.setNetVarForOutputMapping(netRow.getName());
        }
        taskRow.setMapping(mapping);
    }


    private String createMapping(VariableRow row) {
        StringBuilder s = new StringBuilder("/");
        s.append(row.getDecompositionID())
         .append("/")
         .append(row.getName())
         .append("/")
         .append(SpecificationModel.getHandler().getDataHandler().getXQuerySuffix(
                 row.getDataType()));
        return s.toString();
    }

}
