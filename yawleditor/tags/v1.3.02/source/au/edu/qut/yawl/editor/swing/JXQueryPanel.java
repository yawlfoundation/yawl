/*
 * Created on 21/09/2004
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

package au.edu.qut.yawl.editor.swing;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class JXQueryPanel extends JPanel {
  
  JXQueryEditorPane xQueryEditor = new JXQueryEditorPane();

  public JXQueryPanel(String title) {
    super(new BorderLayout());
    setBorder(new CompoundBorder(new TitledBorder(title),
                                 new EmptyBorder(0,5,5,5)));
   
    add(new JScrollPane(xQueryEditor), BorderLayout.CENTER);
  }

  public JXQueryEditorPane getEditor() {
    return xQueryEditor;
  }
}
