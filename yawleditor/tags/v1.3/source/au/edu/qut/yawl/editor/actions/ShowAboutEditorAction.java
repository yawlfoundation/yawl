/*
 * Created on 9/10/2003
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
 *
 */

package au.edu.qut.yawl.editor.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;

import java.awt.BorderLayout;

import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import javax.swing.JPanel;
import javax.swing.JLabel;

import au.edu.qut.yawl.editor.swing.JUtilities;
import au.edu.qut.yawl.editor.swing.AbstractDoneDialog;


public class ShowAboutEditorAction extends YAWLBaseAction {
  private static final AboutEditorDialog dialog = new AboutEditorDialog();

  {
    putValue(Action.SHORT_DESCRIPTION, "About this version of the YAWLEditor.");
    putValue(Action.NAME, "About the Editor");
    putValue(Action.LONG_DESCRIPTION, "About this version of the YAWLEditor.");
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_B));
  }
 
  public void actionPerformed(ActionEvent event) {
    dialog.setVisible(true); 
  }
}

class AboutEditorDialog extends AbstractDoneDialog {

  public AboutEditorDialog() {
    super("About the editor", false, getAboutPanel(), false);
  }
  
  protected void makeLastAdjustments() {
    setSize(450,450);
    JUtilities.centerWindow(this);
  }
  
  private static JPanel getAboutPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    panel.setBorder(
      new CompoundBorder(
          new EmptyBorder(12,12,0,11),
          new CompoundBorder(
              new EtchedBorder(),
              new EmptyBorder(12,12,11,11)
          )
       )
    );

    JLabel message = new JLabel("<html><body>" +
        "This is version @EditorReleaseNumber@ of the YAWLEditor.<br><br>" + 
        "The editor incorporates and/or uses components of the following tools:" +
        "<ul><li>YAWL Engine @CompatibleEngineReleaseNumber@"+
            "<li>JGraph @JGraphReleaseNumber@"+
            "<li>Proguard @ProguardReleaseNumber@"+
            "<li>WofYAWL @WofYawlReleaseNumber@<li>Postgres 8.0</ul>" +
        "This version of the editor is compatible only with:" +
        "<ul><li>YAWL Engine @CompatibleEngineReleaseNumber@<li>WofYAWL @WofYawlReleaseNumber@</ul>" +
        "Save-file formats of previous YAWLEditor versions are not compatible with this version." +
        "</body></html>"
    );

    panel.add(message,BorderLayout.CENTER);

    return panel;    
  }
}

