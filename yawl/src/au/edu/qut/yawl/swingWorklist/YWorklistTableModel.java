/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.swingWorklist;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.engine.AbstractEngine;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.YEngineInterface;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.worklist.model.Marshaller;
import au.edu.qut.yawl.worklist.model.SpecificationData;
import au.edu.qut.yawl.worklist.model.TaskInformation;

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


    public String getOutputData(String caseIDStr, String taskID) throws JDOMException, IOException, YPersistenceException {
        Object[] row = (Object[]) _rows.get(caseIDStr + taskID);
        if (row != null && row.length > 8) {
            String outputParamsData = (String) row[8];
            String inputParamsData = (String) row[7];

            SAXBuilder builder = new SAXBuilder();
            Document outputData = builder.build(new StringReader(outputParamsData));
            Document inputData = builder.build(new StringReader(inputParamsData));

            String mergedOutputData = Marshaller.getMergedOutputData(
                    inputData.getRootElement(),
                    outputData.getRootElement());

            YEngineInterface eng =  EngineFactory.getTransactionalEngine();
            YWorkItem item = eng.getWorkItem(caseIDStr + ":" + taskID);
            
            
            String task = eng.getTaskInformation(
                    item.getSpecificationID(),
                    item.getTaskID());
            
    		TaskInformation taskInfo = Marshaller.unmarshalTaskInformation(task);

    		SpecificationData data = null;
    		String specdata = eng.getDataForSpecifications(true);
    		
    		System.out.println(specdata);
    		specdata = "<response>" + specdata + "</response>";
    		List specs = Marshaller.unmarshalSpecificationSummary(specdata);
    		
    		System.out.println("specs in the engine: " + specs.size());
    				
            for (int i = 0; i < specs.size(); i++) {
                data = (SpecificationData) specs.get(i);
                if (data.getID().equals(item.getSpecificationID())) {
                	if (data.getAsXML()==null) {
                		String specAsXML = null;
                		try {
                			specAsXML = eng.getProcessDefinition(item.getSpecificationID());
                		} catch (Exception e) {
                			throw new YPersistenceException("No such process definition");
                		}
                		data.setSpecAsXML(specAsXML);	
                	}
                }
            }
    		
            List outputParamsMP = taskInfo.getParamSchema().getOutputParams();
            List outputParamsLst = new ArrayList(outputParamsMP);
            String filteredOutputData;
            if (data.usesSimpleRootData()) {
                filteredOutputData = mergedOutputData;
            } else {
                filteredOutputData = Marshaller.filterDataAgainstOutputParams(
                        mergedOutputData, outputParamsLst);
            }
            return filteredOutputData;
        }
        return null;
    }
}
