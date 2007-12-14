/*
 * Created on 7/05/2004
 * YAWLEditor v1.0 
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
 
package org.yawlfoundation.yawl.editor.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JTable;

import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class JAlternatingRowColorTable extends JTable {

  private static final long serialVersionUID = 1L;

  static final Font  COURIER = new Font("Monospaced", Font.PLAIN, 12);

  private static final Color DEFAULT_EVEN_ROW_COLOR = Color.WHITE;
  private static final Color DEFAULT_ODD_ROW_COLOR = Color.decode("0xFAEBD7"); // Antique White

  private Color evenRowColor = DEFAULT_EVEN_ROW_COLOR;
  private Color oddRowColor  = DEFAULT_ODD_ROW_COLOR;
  
  {
    setFont(COURIER);
  }
  
  public JAlternatingRowColorTable() {
    super();
  }
  
  public JAlternatingRowColorTable(TableModel model) {
    super(model);
  }

  public void setEvenRowColor(Color evenRowColor) {
    this.evenRowColor = evenRowColor;
  }

  public void setOddRowColor(Color oddRowColor) {
    this.oddRowColor = oddRowColor;
  }

  public Color getEvenRowColor() {
    return evenRowColor;
  }

  public Color getOddRowColor() {
    return oddRowColor;
  }
  
  public Component prepareRenderer(TableCellRenderer renderer,
                                   int row, 
                                   int col) {
    
    JComponent theRenderer = (JComponent) super.prepareRenderer(renderer, row, col);
    
    theRenderer.setBackground(row %2 == 0 ? evenRowColor : oddRowColor);
    
    if (isRowSelected(row)) {
      theRenderer.setBackground(
          getSelectionBackground()
      );
    }
    
    return theRenderer;
  }
}
