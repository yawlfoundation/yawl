package org.yawlfoundation.yawl.editor.swing.resourcing;

import org.yawlfoundation.yawl.editor.thirdparty.resourcing.ResourcingServiceProxy;
import org.yawlfoundation.yawl.resourcing.resource.Capability;
import org.yawlfoundation.yawl.resourcing.resource.OrgGroup;
import org.yawlfoundation.yawl.resourcing.resource.Position;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Author: Michael Adams
 * Creation Date: 29/07/2008
 */
public class FilterParameterValueComboBoxEditor extends AbstractCellEditor
        implements TableCellEditor {

  private static final long serialVersionUID = 1L;

  private FilterParameterTable _table;

  private FilterParameterValueComboBox cbxCapability;
  private FilterParameterValueComboBox cbxPosition;
  private FilterParameterValueComboBox cbxOrgGroup;
  private FilterParameterValueComboBox cbxSelected;

  private Map<String, String> mapCapability;
  private Map<String, String> mapPosition;
  private Map<String, String> mapOrgGroup;
  private Map<String, String> mapSelected;                                 // [name, id]


  public FilterParameterValueComboBoxEditor(FilterParameterTable table) {
    super();
    this._table = table;
    makeMaps();
  }


  public Component getTableCellEditorComponent(JTable table, Object value,
          boolean isSelected, int rowIndex, int colIndex) {

      String param = _table.getParamNameAt(rowIndex);

      if (param.equalsIgnoreCase("position")) {
          cbxSelected = cbxPosition;
          mapSelected = mapPosition;
      }
      else if (param.equalsIgnoreCase("orggroup")) {
          cbxSelected = cbxOrgGroup;
          mapSelected = mapOrgGroup;
      }
      else if (param.equalsIgnoreCase("capability")) {
          cbxSelected = cbxCapability;
          mapSelected = mapCapability;
      }
      else {
          cbxSelected = new FilterParameterValueComboBox();
          mapSelected = new HashMap<String, String>();
      }

      return cbxSelected;
  }

    
  public Object getCellEditorValue() {
      return cbxSelected.getSelectedItem();
  }

    protected void makeMaps() {
        java.util.List capabilities = ResourcingServiceProxy.getInstance().getCapabilities();
        java.util.List positions = ResourcingServiceProxy.getInstance().getPositions();
        java.util.List orgGroups = ResourcingServiceProxy.getInstance().getOrgGroups();

        mapCapability = new HashMap<String, String>();
        mapPosition = new HashMap<String, String>();
        mapOrgGroup = new HashMap<String, String>();

        int i;
        if (capabilities != null) {
            for (i=0; i<capabilities.size(); i++) {
                Capability c = (Capability) capabilities.get(i) ;
                mapCapability.put(c.getCapability(), c.getID());
            }
        }
        if (positions != null) {
            for (i=0; i<positions.size(); i++) {
                Position p = (Position) positions.get(i) ;
                mapPosition.put(p.getTitle(), p.getID());
            }
        }
        if (orgGroups != null) {
            for (i=0; i<orgGroups.size(); i++) {
                OrgGroup o = (OrgGroup) orgGroups.get(i) ;
                mapOrgGroup.put(o.getGroupName(), o.getID());
            }
        }

        cbxCapability = new FilterParameterValueComboBox(new Vector(mapCapability.keySet()));
        cbxPosition = new FilterParameterValueComboBox(new Vector(mapPosition.keySet()));
        cbxOrgGroup = new FilterParameterValueComboBox(new Vector(mapOrgGroup.keySet()));
    }


  protected String getSelectedID() {
      return mapSelected.get((String) getCellEditorValue());
  }



  class FilterParameterValueComboBox extends JComboBox implements ActionListener {

      public FilterParameterValueComboBox() { super(); }

      public FilterParameterValueComboBox(Vector items) {
          super(items);
          addActionListener(this);
      }

      public void actionPerformed(ActionEvent e) {
          int row = _table.getSelectionModel().getLeadSelectionIndex();
          if (row > -1) {
              FilterParameterValueComboBox box = (FilterParameterValueComboBox) e.getSource();
              _table.setValueAt(row, (String) box.getSelectedItem());
          }
      }
  }

}
