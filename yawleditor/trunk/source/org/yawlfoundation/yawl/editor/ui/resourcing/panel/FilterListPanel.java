/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.resourcing.panel;

import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

/**
 * @author Michael Adams
 * @date 9/08/12
 */
public class FilterListPanel extends JPanel implements ActionListener {

    private JTextArea txtExpression;


    public FilterListPanel(String title, Vector<String> items) {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder(title));
        setContent(items);
    }


    public void setExpression(String expression) { txtExpression.setText(expression); }


    public String getExpression() {
        String expression = txtExpression.getText();
        if (endsWithOperator(expression)) {
            expression = undo(expression);               // chop the operator from end
        }
        return expression;
    }


    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (action.equals("And")) {
            if (! endsWithOperator(txtExpression.getText())) txtExpression.append(" & ");
        }
        else if (action.equals("Or")) {
            if (! endsWithOperator(txtExpression.getText())) txtExpression.append(" | ");
        }
        else if (action.equals("Clear")) {
            txtExpression.setText("");
        }
        else if (action.equals("Undo")) {
            txtExpression.setText(undo(txtExpression.getText()));
        }
    }


    private void setContent(Vector<String> items) {
        add(createList(items), BorderLayout.NORTH);
        add(createTextArea(), BorderLayout.CENTER);
        add(createToolBar(), BorderLayout.SOUTH);
    }


    private JScrollPane createList(Vector<String> items) {
        final JList list = new JList(items);
        list.setPreferredSize(new Dimension(175, 150));
        list.setDragEnabled(true);
        list.setEnabled(! items.isEmpty());

        // copies item on double-click
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2 && canAppendItem()) {
                    txtExpression.append((String) list.getSelectedValue());
                }
            }
        });

        return new JScrollPane(list);
    }


    private JPanel createTextArea() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(5,0,0,0));
        txtExpression = new JTextArea();
        txtExpression.setLineWrap(true);
        txtExpression.setWrapStyleWord(true);
        txtExpression.setTransferHandler(new FilterTransferHandler());
        JScrollPane pane = new JScrollPane(txtExpression);
        pane.setPreferredSize(new Dimension(210, 75));
        panel.add(createExpressionTitle());
        panel.add(pane);
        return panel;
    }


    private JPanel createExpressionTitle() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Filter Expression"), BorderLayout.WEST);
        return panel;
    }


    private JToolBar createToolBar() {
        MiniToolBar toolBar = new MiniToolBar(this);
        toolBar.addButton("and", "And", " And ");
        toolBar.addButton("or", "Or", " Or ");
        toolBar.addButton("undo", "Undo", " Undo ");
        toolBar.addButton("cross", "Clear", " Clear ");
        return toolBar;
    }


    private String undo(String text) {
        boolean alphaHit = false;
        int i;
        for (i=text.length()-1; i>-0; i--) {
            char c = text.charAt(i);
            if (c == '&' || c == '|') {
                if (! alphaHit) {
                    return text.substring(0, i-1);   // lop off operator and lead space
                }
                else {
                    return text.substring(0, i+2);   // lop off word, leave trailing space
                }
            }
            else if (c != ' ') {
                alphaHit = true;
            }
        }
        return "";               // no operator found, must be single word, so remove
    }


    private boolean endsWithOperator(String s) {
        for (int i=s.length()-1; i >=0; i--) {
            char c = s.charAt(i);
            if (c == '&' || c == '|') return true;
            else if (c != ' ') return false;
        }
        return false;
    }

    private boolean canAppendItem() {
        String currentText = txtExpression.getText();
        return StringUtil.isNullOrEmpty(currentText) || endsWithOperator(currentText);
    }


    /****************************************************************************/

    class FilterTransferHandler extends TransferHandler {

        public boolean importData(TransferHandler.TransferSupport support) {
            if (! canImport(support)) {
                return false;
            }
            try {
                String data = (String) support.getTransferable().getTransferData(
                        DataFlavor.stringFlavor);
                txtExpression.append(data);
                return true;
            }
            catch (Exception e) {
                return false;
            }
        }

        public boolean canImport(TransferHandler.TransferSupport support) {
            return canAppendItem();
        }

    }

}
