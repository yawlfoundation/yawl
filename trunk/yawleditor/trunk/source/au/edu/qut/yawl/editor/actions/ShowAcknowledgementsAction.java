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


public class ShowAcknowledgementsAction extends YAWLBaseAction {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final AcknowledgementsDialog dialog = new AcknowledgementsDialog();

  {
    putValue(Action.SHORT_DESCRIPTION, "A message of thanks from the author.");
    putValue(Action.NAME, "Acknowledgements...");
    putValue(Action.LONG_DESCRIPTION, "Shows a message of thanks from the author.");
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_A));
  }
 
  public void actionPerformed(ActionEvent event) {
    dialog.setVisible(true); 
  }
}

class AcknowledgementsDialog extends AbstractDoneDialog {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public AcknowledgementsDialog() {
    super("Acknowlegements", false, getAcknowledgementPanel(), false);
  }
  
  protected void makeLastAdjustments() {
    setSize(400,330);
    JUtilities.centerWindow(this);
  }
  
  private static JPanel getAcknowledgementPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    panel.setBorder(
      new CompoundBorder(new EmptyBorder(12,12,0,11),
                         new EtchedBorder())
    );

    JEditorPane editor = new JEditorPane();

    panel.add(new JScrollPane(editor),BorderLayout.CENTER);

    editor.setText(
      "Thanks to Dr Arthur ter Hofstede, Dr Marlon Dumas,\n" +
      "Dr Wil van der Aalst and Lachlan Aldred for their\n" +
      "feedback and encouragement on writing the editor.\n\n" +
      "Thanks to Gaundez Alder and the team for JGraph.\n" +      "You guys saved me a lot of work.\n\n" +
      "Thanks to my wife, Fiona, for tolerating my extremes\n" +
      "of time consumption lately.\n\n" +
      "And finally, thanks to the cast of hundreds who have\n" +
      "scattered excellent hints and tips on Java across the\n" +
      "Web.  Again, you've all saved me a great deal of effort."
    );    
    editor.setEditable(false);

    return panel;    
  }
}

