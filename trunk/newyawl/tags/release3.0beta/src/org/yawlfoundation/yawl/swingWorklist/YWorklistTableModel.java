/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.swingWorklist;

import org.jdom2.JDOMException;

import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.*;

/**
 * 
 * @author Lachlan Aldred
 * Date: 15/05/2003
 * Time: 13:51:11
 * 
 */
public class YWorklistTableModel extends AbstractTableModel {
    protected Map _rows = new TreeMap();
    private String[] _colNames;

    public YWorklistTableModel(String[] colNames) {
        _colNames = colNames;
    }


    /**
     * Returns the number of rows in the model. A
     * <code>JTable</code> uses this method to determine how many rows it
     * should display.  This method should be quick, as it
     * is called frequently during rendering.
     *
     * @return the number of rows in the model
     * @see #getColumnCount
     */
    public synchronized int getRowCount() {
        return _rows.size();
    }


    /**
     * Returns the number of columns in the model. A
     * <code>JTable</code> uses this method to determine how many columns it
     * should create and display by default.
     *
     * @return the number of columns in the model
     * @see #getRowCount
     */
    public synchronized int getColumnCount() {
        if (_colNames != null) {
            return _colNames.length;
        }
        return 0;
    }


    /**
     * Returns the value for the cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     *
     * @param	rowIndex	the row whose value is to be queried
     * @param	columnIndex 	the column whose value is to be queried
     * @return	the value Object at the specified cell
     */
    public synchronized Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < _rows.size()) {
            Object[] row = ((Object[]) new Vector(_rows.values()).get(rowIndex));
            if (row.length > columnIndex) {
                return row[columnIndex];
            }
        }
        return null;
    }


/*
    public void setValueAt(Object value, int rowIndex, int columnIndex){
        ((Object[])_rows.get(rowIndex))[columnIndex]= value;
    }
*/


    public synchronized void addRow(Object key, Object[] rowValues) {
        _rows.put(key, rowValues);
        final int position = new Vector(_rows.keySet()).indexOf(key);
        EventQueue.invokeLater(new Thread() {
            public void run() {
                fireTableRowsInserted(position, position);
            }
        });
    }


    public synchronized String getColumnName(int columnIndex) {
        if (this._colNames != null && _colNames.length > 0) {
            return _colNames[columnIndex % _colNames.length];
        } else
            return super.getColumnName(columnIndex);
    }


    public synchronized void removeRow(Object caseAndTaskID) {
        final int rowIndex = getRowIndex(caseAndTaskID);
        if (rowIndex >= 0) {
            _rows.remove(caseAndTaskID);
            EventQueue.invokeLater(new Thread() {
                public void run() {
                    fireTableRowsDeleted(rowIndex, rowIndex);
                }
            });
        }
    }


    public synchronized Class getColumnClass(int c) {
        Object o = getValueAt(0, c);
        return o != null ? o.getClass() : null;
    }


    public synchronized int getRowIndex(Object caseAndTaskID) {
        return new Vector(_rows.keySet()).indexOf(caseAndTaskID);
    }


    public String[] getColumnNames() {
        return _colNames;
    }

    public Map getRowMap() {
        return _rows;
    }

/*    public String getOutputData(String caseIDStr, String taskID) {
        Object[] row = (Object[]) _rows.get(caseIDStr + taskID);
        if (row != null && row.length > 8) {
            String outputParamsData = (String) row[8];
            String inputParamsData = (String) row[7];
            SAXBuilder builder = new SAXBuilder();
            Document finalDoc = null;
            Document outputDataDoc = null;
            try {
                finalDoc = builder.build(new StringReader(inputParamsData));
                outputDataDoc = builder.build(new StringReader(outputParamsData));
                java.util.List children = outputDataDoc.getRootElement().getContent();
                for (int i = 0; i < children.size(); i++) {
                    Object o = children.get(i);
                    if (o instanceof Element) {
                        Element child = (Element) o;
                        child.detach();
                        finalDoc.getRootElement().removeChild(child.getName());
                        finalDoc.getRootElement().addContent(child);
                    }
                }
            } catch (JDOMException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new XMLOutputter().outputString(finalDoc.getRootElement()).trim();
        }
        return null;
    }*/


    public String getOutputData(String caseIDStr, String taskID) throws JDOMException, IOException {
        String inputParamsData = null;
        String outputParamsData = null;

        Object[] row = (Object[]) _rows.get(caseIDStr + taskID);
        if (row != null && row.length > 9)
        {
            outputParamsData = (String) row[9];
            inputParamsData = (String) row[8];

            /**
             * AJH: I don't think we need to do this anymore given support for optional params. If its an OUTPUT var then you *MUST* pass it back in.
            SAXBuilder builder = new SAXBuilder();
            Document outputData = builder.build(new StringReader(outputParamsData));
            Document inputData = builder.build(new StringReader(inputParamsData));

            String mergedOutputData = Marshaller.getMergedOutputData(
                    inputData.getRootElement(),
                    outputData.getRootElement());

            YEngine eng = YEngine.getInstance();
            YWorkItem item = eng.getWorkItem(caseIDStr + ":" + taskID);
            YTask task = eng.getTaskDefinition(
                    item.getSpecificationID(),
                    item.getTaskID());
            Map outputParamsMP = task.getDecompositionPrototype().getOutputParameters();
            List outputParamsLst = new ArrayList(outputParamsMP.values());
            String filteredOutputData;
            if (task._net.getSpecification().usesSimpleRootData()) {
                filteredOutputData = mergedOutputData;
            } else {
                filteredOutputData = Marshaller.filterDataAgainstOutputParams(
                        mergedOutputData, outputParamsLst);
            }
            return filteredOutputData;
        }
        return null;
    }
        */
        }

        return outputParamsData;

    }
}