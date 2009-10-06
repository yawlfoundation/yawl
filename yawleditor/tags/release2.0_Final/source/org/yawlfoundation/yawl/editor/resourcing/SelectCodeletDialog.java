/*
 * Created on 11/06/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
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

package org.yawlfoundation.yawl.editor.resourcing;

import org.yawlfoundation.yawl.editor.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.swing.resourcing.CodeletSelectTable;
import org.yawlfoundation.yawl.editor.data.WebServiceDecomposition;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SelectCodeletDialog extends AbstractDoneDialog {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private CodeletSelectTable codeletTable;
  private WebServiceDecomposition decomposition;


  public SelectCodeletDialog() {
    super("Set Codelet for Automated Decomposition", true);
    setContentPanel(getCodeletPanel());

    getDoneButton().setText("OK");
    getDoneButton().addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          decomposition.setCodelet(codeletTable.getSelectedCodeletName());
          SpecificationUndoManager.getInstance().setDirty(true);
        }
      }
    );
      
    getRootPane().setDefaultButton(getCancelButton());
  }

  protected void makeLastAdjustments() {
    setSize(600, 400);
    setResizable(false);
  }


  private JPanel getCodeletPanel() {
    codeletTable = new CodeletSelectTable();
    JScrollPane jspane =  new JScrollPane(codeletTable);
    JPanel codeletTablePanel = new JPanel();
    codeletTablePanel.setBorder(new EmptyBorder(12,12,0,11));
    codeletTablePanel.add(jspane, BorderLayout.CENTER);
    return codeletTablePanel;
  }


  public void setVisible(boolean state) {
    super.setVisible(state);
  }

    public WebServiceDecomposition getDecomposition() {
        return decomposition;
    }

    public void setDecomposition(WebServiceDecomposition decomposition) {
        this.decomposition = decomposition;
    }

    public void setSelectedCodelet() {
        String selectedCodelet =  decomposition.getCodelet();
        if (selectedCodelet != null) {
            codeletTable.setSelectedRowWithName(selectedCodelet);
        }
    }

}