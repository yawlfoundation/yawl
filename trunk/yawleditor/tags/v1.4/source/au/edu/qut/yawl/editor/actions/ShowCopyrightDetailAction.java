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
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import au.edu.qut.yawl.editor.swing.JUtilities;
import au.edu.qut.yawl.editor.swing.AbstractDoneDialog;


public class ShowCopyrightDetailAction extends YAWLBaseAction {
  private static final CopyrightDialog dialog = new CopyrightDialog();

  {
    putValue(Action.SHORT_DESCRIPTION, "Show Copyright Licence...");
    putValue(Action.NAME, "Copyright Notice...");
    putValue(Action.LONG_DESCRIPTION, "Shows the editor's Copyright Licence.");
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_C));
  }
 
  public void actionPerformed(ActionEvent event) {
    dialog.setVisible(true); 
  }
}

class CopyrightDialog extends AbstractDoneDialog {

  public CopyrightDialog() {
    super("Copyright Notice", false, getCopyrightPanel(), false);
  }
  
  protected void makeLastAdjustments() {
    setSize(400,420);
    JUtilities.centerWindow(this);
  }
  
  private static JPanel getCopyrightPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    panel.setBorder(
      new CompoundBorder(new EmptyBorder(12,12,0,11),
                         new EtchedBorder())
    );

    JEditorPane editor = new JEditorPane();

    panel.add(new JScrollPane(editor),BorderLayout.CENTER);

    editor.setText(
      "Copyright to this software and its source code is granted\n" +      "under the GNU Lesser General Public Licence (v2.1).\n\n" +
      "For detail on the permissions this licence grants you,\n" +
      "please refer to <http://www.gnu.org/copyleft/lesser.html>\n\n" +      "This editor makes use of JGraph @JGraphReleaseNumber@.\n" +      "The JGraph library is covered under an LGPL copyright licence\n" +      "and is available separately from <http://www.jgraph.com/>.\n\n" +
      "This editor also makes use of code from the YAWL Engine\n" +      "and its support libraries. The engine and its support\n" +      "libraries are covered under their own individual copyright\n" +
      "licences, detail of which may be found at\n" +      "<http://www.citi.qut.edu.au/yawl/>\n\n" +
      "In accordance with this editor's copyright licence, the\n" +      "source code may also be obtained from\n" +      "<http://www.citi.qut.edu.au/yawl/>.\n"    );    
    editor.setEditable(false);

    return panel;    
  }
}

