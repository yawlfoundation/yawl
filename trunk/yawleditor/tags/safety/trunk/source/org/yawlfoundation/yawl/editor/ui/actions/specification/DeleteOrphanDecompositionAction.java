/*
 * Created on 09/10/2003
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
 *
 */

package org.yawlfoundation.yawl.editor.ui.actions.specification;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.data.WebServiceDecomposition;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLAtomicTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

public class DeleteOrphanDecompositionAction extends YAWLOpenSpecificationAction implements TooltipTogglingWidget {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  {
    putValue(Action.SHORT_DESCRIPTION,getDisabledTooltipText());
    putValue(Action.NAME, "Delete Orphaned Decompositions...");
    putValue(Action.LONG_DESCRIPTION, "Delete Orphaned Decompositions");
    putValue(Action.SMALL_ICON, getPNGIcon("chart_organisation_delete"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_O));
    putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("O"));
  }

  public void actionPerformed(ActionEvent event) {
      DeleteOrphanDecompositionDialog dialog = new DeleteOrphanDecompositionDialog();
      dialog.setItems(getOrphanedDecompositionNames());
      dialog.setLocationRelativeTo(YAWLEditor.getInstance());
      dialog.setVisible(true);

      List<String> selectedItems = dialog.getSelectedItems();
      if (selectedItems != null) {                   // will be null if dialog cancelled
          for (String name : selectedItems) {
              SpecificationModel.getInstance().removeWebServiceDecomposition(name);
          }
      }
  }

  public String getEnabledTooltipText() {
    return " Delete decompositions that have no associated tasks ";
  }

  public String getDisabledTooltipText() {
    return " You must have a specification" +
           " open to in order to delete its orphaned decompositions ";
  }

  private Vector<String> getOrphanedDecompositionNames() {
      Set<YAWLAtomicTask> allTasks = getAllAtomicTasks();
      Vector<String> items = new Vector<String>();
      for (WebServiceDecomposition decomp :
              SpecificationModel.getInstance().getWebServiceDecompositions()) {
          if (isOrphaned(decomp, allTasks)) {
              items.add(decomp.getLabel());
          }
      }
      Collections.sort(items);
      return items;
  }

    private Set<YAWLAtomicTask> getAllAtomicTasks() {
        Set<YAWLAtomicTask> tasks = new HashSet<YAWLAtomicTask>();
        for (NetGraphModel net : SpecificationModel.getInstance().getNets()) {
             tasks.addAll(NetUtilities.getAtomicTasks(net));
        }
        return tasks;
    }


    private boolean isOrphaned(WebServiceDecomposition decompToFind,
                               Set<YAWLAtomicTask> allTasks) {
        for (YAWLAtomicTask task : allTasks) {
            WebServiceDecomposition decompOfTask = task.getWSDecomposition();
            if ((decompOfTask != null) && (decompOfTask == decompToFind)) {
                return false;
            }
        }
        return true;    // no task references the decomposition
    }
}