/*
 * Created on 6/08/2004
 * YAWLEditor v1.01 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.yawlfoundation.yawl.editor.ui.swing.resourcing;

import org.yawlfoundation.yawl.editor.ui.client.YConnector;
import org.yawlfoundation.yawl.editor.ui.resourcing.CodeletData;
import org.yawlfoundation.yawl.editor.ui.resourcing.CodeletDataMap;
import org.yawlfoundation.yawl.editor.ui.swing.JOrderedSingleSelectTable;
import org.yawlfoundation.yawl.resourcing.codelets.CodeletInfo;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class CodeletSelectTable extends JOrderedSingleSelectTable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private static final int MAX_TABLE_HEIGHT = 290;
  private static final int CODELET_TABLE_WIDTH = 585;
  private static final int GATEWAY_TABLE_WIDTH = 515;

  public static final int CODELET = 0;
  public static final int DATA_GATEWAY = 1;  

  private List<CodeletData> codeletDataList;
  private int preferredTableWidth;
  private CodeletDataMap codeletDataMap;

    public CodeletSelectTable(int source) {
    super();
    preferredTableWidth = (source == CODELET) ? CODELET_TABLE_WIDTH : GATEWAY_TABLE_WIDTH;
    setModel(new CodeletSelectTableModel(getCodeletDataList(source)));
    setFormat();
  }

  public CodeletSelectTable(CodeletSelectTableModel model) {
    super();
    setModel(model);
    setFormat();
  }

  public void setFormat() {
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    getColumnModel().getColumn(1).setCellRenderer(WrappingCellRenderer.INSTANCE);
    getTableHeader().setResizingAllowed(false);
      
    int maxNameColWidth = getMaximumNameWidth() + 10;
    getColumn("Name").setMinWidth(maxNameColWidth);
    getColumn("Name").setPreferredWidth(maxNameColWidth);

    getColumn("Description").setMinWidth(preferredTableWidth - maxNameColWidth);

  }



  public Dimension getPreferredScrollableViewportSize() {
    return new Dimension(preferredTableWidth, getPreferredViewportHeight());
  }


  public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
    return super.prepareRenderer(renderer, row, col);
  }


  public void setCodeletDataList(List<CodeletData> codeletDataList) {
    this.codeletDataList = codeletDataList;
    updateState();
  }

  public void updateState() {
    setModel(new CodeletSelectTableModel(codeletDataList));
    setFormat();
  }

  private int getMessageWidth(String message) {
    return  getFontMetrics(getFont()).stringWidth(message) + 5;
  }


  private int getPreferredViewportHeight() {
      int fontHeight =  getFontMetrics(getFont()).getHeight() ;
      int nbrOfLines = 0;
      for (int i = 0; i < this.getRowCount(); i++) {
          String[] lines = getDescriptionAt(i).split("<br>");
          if (lines != null) nbrOfLines += lines.length;
      }
      int actualTableHeight = (fontHeight + 1) * nbrOfLines;
      return Math.min(MAX_TABLE_HEIGHT, actualTableHeight);

  }

  private int getMaximumNameWidth() {
    int maxWidth = getMessageWidth("Name-");
    for(int i = 0; i < this.getRowCount(); i++) {
      maxWidth = Math.max(maxWidth, getMessageWidth(this.getNameAt(i)));
    }
    return maxWidth;
  }

  private List<CodeletData> getCodeletDataList(int source) {
      Map<String, String> dataMap = null;
      try {
          if (source == CODELET) {
              dataMap = getCodeletMap();
          }
          else {
              dataMap = YConnector.getExternalDataGateways();
          }
      }
      catch (IOException ioe) {
          // nothing to do - proceed with null map
      }

      if (dataMap != null) {
          codeletDataMap = new CodeletDataMap(dataMap);
          return codeletDataMap.getCodeletDataAsList();
      }
      return null;
  }

    private Map<String, String> getCodeletMap() throws IOException {
        Map<String, String> map = new TreeMap<String, String>();
        for (CodeletInfo codelet : YConnector.getCodelets()) {
            map.put(codelet.getCanonicalName(), codelet.getDescription());
        }
        return map;
    }


  public CodeletSelectTableModel getCodeletModel() {
    return (CodeletSelectTableModel) getModel();
  }

  public CodeletData getCodeletAt(int row) {
    return getCodeletModel().getCodeletAt(row);
  }

  public String getNameAt(int row) {
    return getCodeletModel().getNameAt(row);
  }

    public String getCanonicalNameAt(int row) {
      return getCodeletModel().getCanonicalNameAt(row);
    }

  public String getDescriptionAt(int row) {
    return getCodeletModel().getDescriptionAt(row);
  }

  public String getSelectedCodeletName() {
      return (getSelectedRow() > -1) ? getCanonicalNameAt(getSelectedRow()) : null ;
  }

  public void setSelectedRowWithName(String codeletName) {
      int rowCount = getModel().getRowCount();
      for (int i = 0; i < rowCount; i++) {
          if (codeletName.equals(getNameAt(i))) {
              selectRow(i);
              break ;
          }
      }
  }

    
  static class WrappingCellRenderer extends DefaultTableCellRenderer {
      public static final WrappingCellRenderer INSTANCE = new WrappingCellRenderer();

      public Component getTableCellRendererComponent(JTable table, Object value,
                 boolean isSelected, boolean hasFocus, int row, int column) {
         Component c = super.getTableCellRendererComponent(table, value, isSelected,
                                                            hasFocus, row, column);
         int height = c.getPreferredSize().height;
         if (height > table.getRowHeight(row)) {
                 table.setRowHeight(row, height);
          }
          return c;
      }
  }



}