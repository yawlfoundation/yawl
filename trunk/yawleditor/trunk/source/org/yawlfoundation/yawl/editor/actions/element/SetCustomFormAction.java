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

package org.yawlfoundation.yawl.editor.actions.element;

import org.yawlfoundation.yawl.editor.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.data.Decomposition;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;

public class SetCustomFormAction extends YAWLSelectedNetAction
                                implements TooltipTogglingWidget {

  private static final long serialVersionUID = 1L;

    private YAWLTask task;
    private NetGraph graph;

  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Set Custom Form...");
    putValue(Action.LONG_DESCRIPTION, "Set the Form URI to display the task at runtime.");
    putValue(Action.SMALL_ICON, getPNGIcon("application_form"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_F));
  }

  public SetCustomFormAction(YAWLTask task, NetGraph graph) {
    super();
    this.task = task;
    this.graph = graph;
  }

  public void actionPerformed(ActionEvent event) {
    String urlStr = task.getCustomFormURL();
    if (urlStr == null) urlStr =  "http://";
    boolean done = false ;

    while (! done) {
      String result = (String) JOptionPane.showInputDialog(graph, "Custom Form URI: ",
                          "Set Custom Form URI", JOptionPane.PLAIN_MESSAGE,
                          null, null, urlStr);

      if ((result != null) && (! result.equals("http://"))) {
        try {
          new URL(result);                   // check for well formedness
          task.setCustomFormURL(result);     // passed the test
          done = true ;  
        }
        catch (MalformedURLException mfue) {
          JOptionPane.showMessageDialog(graph,
                "'" + result + "' is not a valid absolute URL. Please correct or cancel.",
                "Malformed URL",
                JOptionPane.ERROR_MESSAGE);
        }   
      }
      else done = true ;                                                  // cancelled
    }
    graph.clearSelection();
  }

    
  public String getEnabledTooltipText() {
    return " Set the URI referring to a form to display this task at runtime ";
  }

  public String getDisabledTooltipText() {
    return " You must have an task selected to set a Custom Form for it ";
  }

  public boolean shouldBeEnabled() {
    Decomposition decomp = task.getDecomposition();
    return ((decomp != null) && decomp.isManualInteraction());
  }

}