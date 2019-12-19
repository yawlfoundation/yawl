/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.procletService.editor.pconns;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FocusTest extends JDialog{
    JRadioButton button1;
    JRadioButton button2;
    
    public FocusTest(Frame owner){
        // create a modal dialog that will block until hidden
        super(owner, true);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        button1 = new JRadioButton("First thing");
        button1.setSelected(true);
        contentPane.add(button1);
        button2 = new JRadioButton("Second thing");
        contentPane.add(button2);
        
        ButtonGroup bGroup = new ButtonGroup();
        bGroup.add(button1);
        bGroup.add(button2);
        
        JButton btnOk = new JButton("Ok");
        btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // This will "close" the modal dialog and allow program
                // execution to continue.
                setVisible(false);
                dispose();
            }
        });
        contentPane.add(btnOk);
        pack();
    }
    
    public String showDialog(){
        // show the dialog
        // this will block until setVisible(false) occurs
        setVisible(true);
        // return whatever data is required
        return button1.isSelected() ? button1.getText() : button2.getText();
    }
    
    public static void main(String[] args) {
    	FocusTest dialog = new FocusTest(null);
        String userChoice = dialog.showDialog();
        System.out.println("User chose: "+userChoice);
    }

}
