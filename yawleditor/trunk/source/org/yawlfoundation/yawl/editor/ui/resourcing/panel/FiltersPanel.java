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

import org.yawlfoundation.yawl.editor.core.resourcing.BasicOfferInteraction;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourceDialog;
import org.yawlfoundation.yawl.editor.ui.resourcing.subdialog.FilterDialog;
import org.yawlfoundation.yawl.resourcing.filters.AbstractFilter;
import org.yawlfoundation.yawl.resourcing.filters.CapabilityFilter;
import org.yawlfoundation.yawl.resourcing.filters.OrgFilter;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 23/05/13
 */
public class FiltersPanel extends JPanel implements ActionListener {

    private final ResourceDialog owner;
    private JTextArea txtCapability;
    private JTextArea txtOrgStructure;

    private AbstractFilter capabilityFilter;
    private AbstractFilter orgFilter;


    public FiltersPanel(ResourceDialog owner) {
        this.owner = owner;
        capabilityFilter = new CapabilityFilter();            // default init
        orgFilter = new OrgFilter();
        setBorder(new TitledBorder("Filters"));
        addContent();
        setPreferredSize(new Dimension(210, 145));
    }


    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        AbstractFilter filter = action.equals("Capability") ? capabilityFilter :
                orgFilter;

        FilterDialog dialog = new FilterDialog(owner, filter);
        dialog.setVisible(true);
        update(dialog.getSelection());
    }

    public void load(BasicOfferInteraction offerInteraction) {
        for (AbstractFilter filter : offerInteraction.getFilterSet().getAll()) {
            update(filter);
        }
    }


    public void save(BasicOfferInteraction offerInteraction) {
        offerInteraction.getFilterSet().clear();
        if (hasValues(capabilityFilter)) {
            offerInteraction.getFilterSet().add(capabilityFilter);
        }
        if (hasValues(orgFilter)) {
            offerInteraction.getFilterSet().add(orgFilter);
        }
    }


    private boolean hasValues(AbstractFilter filter) {
        Map<String, String> params = filter.getParams();
        for (String key : params.keySet()) {
            if (! StringUtil.isNullOrEmpty(params.get(key))) {
                return true;
            }
        }
        return false;
    }


    public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        enableComponents(this, enable);

        // override background colour for non-editable text areas
        Color back = enable ? Color.WHITE :
                UIManager.getDefaults().getColor("TextArea.inactiveBackground");
        txtCapability.setBackground(back);
        txtOrgStructure.setBackground(back);
    }


    private void addContent() {
        setLayout(new BorderLayout());
        txtCapability = new JTextArea();
        txtOrgStructure = new JTextArea();
        add(createFilterPanel("Capability", txtCapability), BorderLayout.CENTER);
        JPanel panel = createFilterPanel("Org Structure", txtOrgStructure);
        panel.setBorder(new EmptyBorder(5,0,0,0));
        add(panel, BorderLayout.SOUTH);
    }


    private JPanel createFilterPanel(String title, JTextArea textArea) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createTitleBar(title), BorderLayout.NORTH);
        panel.add(createTextArea(textArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTitleBar(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createEditButton(title), BorderLayout.EAST);
        panel.add(new JLabel(title), BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane createTextArea(JTextArea textArea) {
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);
        JScrollPane pane = new JScrollPane(textArea);
        pane.setPreferredSize(new Dimension(190, 37));
        return pane;
    }


    private JToolBar createEditButton(String action) {
        MiniToolBar toolBar = new MiniToolBar(this);
        toolBar.addButton("pencil", action, " Add/Edit ");
        return toolBar;
    }


    protected void enableComponents(Container c, boolean enable) {
        for (Component child : c.getComponents()) {
            if (child instanceof Container) {
                enableComponents((Container) child, enable);
            }
            child.setEnabled(enable);
        }
    }


    private void update(AbstractFilter filter) {
        if (filter != null) {
            if (filter.getName().equals("CapabilityFilter")) {
                capabilityFilter = filter;
                String value = filter.getParamValue("Capability");
                if (!StringUtil.isNullOrEmpty(value)) {
                    updateTextArea(txtCapability, value);
                }
            }
            else if (filter.getName().equals("OrgFilter")) {
                orgFilter = filter;
                String text = "";
                String value = filter.getParamValue("OrgGroup");
                if (!StringUtil.isNullOrEmpty(value)) {
                    text = "OrgGroup=" + value;
                }
                value = filter.getParamValue("Position");
                if (!StringUtil.isNullOrEmpty(value)) {
                    if (text.length() > 0) text += "\n";
                    text += "Position=" + value;
                }
                updateTextArea(txtOrgStructure, text);
            }
        }
    }


    private void updateTextArea(JTextArea textArea, String text) {
        textArea.setEditable(true);
        textArea.setText(text);
        textArea.setCaretPosition(0);
        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);
    }

}
