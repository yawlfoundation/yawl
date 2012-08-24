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

package org.yawlfoundation.yawl.editor.ui.actions;

import org.yawlfoundation.yawl.editor.ui.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.ui.swing.JUtilities;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;


public class ShowAboutEditorAction extends YAWLBaseAction {


    {
        putValue(Action.SHORT_DESCRIPTION, "About this version of the YAWLEditor.");
        putValue(Action.NAME, "About...");
        putValue(Action.LONG_DESCRIPTION, "About this version of the YAWLEditor.");
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_A));
    }

    public void actionPerformed(ActionEvent event) {
        AboutEditorDialog dialog = new AboutEditorDialog();
        dialog.setVisible(true);
    }
}

class AboutEditorDialog extends AbstractDoneDialog {

    public AboutEditorDialog() {
        super("About the YAWL Editor", false, getAboutPanel(), false);
    }

    protected void makeLastAdjustments() {
        setSize(600, 500);
        setResizable(false);
        JUtilities.centerWindow(this);
    }

    private static JPanel getAboutPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.setBorder(new CompoundBorder(new EmptyBorder(12, 12, 0, 11),
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(12, 12, 11, 11))));

        JLabel message = new JLabel("<html><body>" +
                "This is version @EditorReleaseNumber@ of the YAWL Process Editor.<br><br>" +
                "The editor uses these tools:" +
                "<ul>" +
                "<li>YAWL Engine @CompatibleEngineReleaseNumber@" +
                "<li>YAWL Resource Service @CompatibleEngineReleaseNumber@" +
                "<li>JGraph @JGraphReleaseNumber@" +
                "<li>WofYAWL @WofYawlReleaseNumber@" +
                "<li>Wendy @WendyReleaseNumber@" +
                "<li>JCalendar @JCalendarReleaseNumber@</ul>" +

                "This version of the editor requires:" +
                "<ul>" +
                "<li>Java @CompatibleJavaReleaseNumber@ or later</ul>" +

                "Contributors to the editor source code:" +
                "<ul><li>@EditorContributors@</ul>" +
                "<br><center>Build Date: @BuildDate@</center>" +
                "</body></html>"
        );

        panel.add(message, BorderLayout.CENTER);
        return panel;
    }
}

