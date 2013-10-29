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

package org.yawlfoundation.yawl.editor.ui.resourcing.dialog.subdialog;

import org.yawlfoundation.yawl.editor.core.resourcing.YResourceHandler;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.ResourceDialog;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.panel.FilterListPanel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.resourcing.filters.AbstractFilter;
import org.yawlfoundation.yawl.resourcing.resource.AbstractResourceAttribute;
import org.yawlfoundation.yawl.resourcing.resource.Capability;
import org.yawlfoundation.yawl.resourcing.resource.OrgGroup;
import org.yawlfoundation.yawl.resourcing.resource.Position;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * @author Michael Adams
 * @date 19/07/13
 */
public class FilterDialog extends JDialog implements ActionListener {

    private AbstractFilter _selected;
    private FilterListPanel _leftPanel;
    private FilterListPanel _rightPanel;

    private List<Capability> _capabilities;
    private List<Position> _positions;
    private List<OrgGroup> _orgGroups;


    public FilterDialog(ResourceDialog owner, AbstractFilter filter) {
        super(owner);
        _selected = filter;
        setTitle("Add/Edit Filter");
        initialise();
        add(getContent(owner));
        setPreferredSize(getDimension());
        pack();
    }


    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (action.equals("Cancel")) {
            _selected = null;
        }
        else {
            updateSelectedFilter();
            ((ResourceDialog) getOwner()).itemStateChanged(null); // enables Apply button
        }
        setVisible(false);
    }


    public AbstractFilter getSelection() {
        return _selected;
    }

    private void initialise() {
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationByPlatform(true);
    }

    private JPanel getContent(ResourceDialog owner) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(5, 5, 5, 5));
        content.add(createListPanel(), BorderLayout.CENTER);
        content.add(createButtonBar(owner), BorderLayout.SOUTH);
        return content;
    }


    private JPanel createListPanel() {
        if (isCapabilityFilter()) {
            _leftPanel = new FilterListPanel("Capabilities",
                   getResourceNames(getCapabilities()));
            _leftPanel.setExpression(_selected.getParamValue("Capability"));
            _leftPanel.setSize(190, 250);
            return _leftPanel;
        }
        else {
            JPanel panel = new JPanel(new GridLayout(1,2));
            _leftPanel = new FilterListPanel("Org Groups",
                   getResourceNames(getOrgGroups()));
            _leftPanel.setExpression(_selected.getParamValue("OrgGroup"));
            panel.add(_leftPanel);

            _rightPanel = new FilterListPanel("Positions", getResourceNames(getPositions()));
            _rightPanel.setExpression(_selected.getParamValue("Position"));
            panel.add(_rightPanel);
            panel.setSize(370, 250);
            return panel;
        }
    }


    private JPanel createButtonBar(ResourceDialog owner) {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));
        panel.add(createButton("Cancel"));
        panel.add(createButton("OK"));
        return panel;
     }


    private JButton createButton(String label) {
        JButton button = new JButton(label);
        button.setActionCommand(label);
        button.setMnemonic(label.charAt(0));
        button.setPreferredSize(new Dimension(70,25));
        button.addActionListener(this);
        return button;
    }


    private void updateSelectedFilter() {
        _selected.setParams(getParams());
    }


    private Dimension getDimension() {
        int height = 380;
        if (isCapabilityFilter()) {
            return new Dimension(230, height);
        }
        else return new Dimension(440, height);
    }


    private Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        String expression = _leftPanel.getExpression();
        if (! StringUtil.isNullOrEmpty(expression)) {
            String key = isCapabilityFilter() ? "Capability" : "OrgGroup";
            params.put(key, expression);
        }
        if (_rightPanel != null) {
            expression = _rightPanel.getExpression();
            if (! StringUtil.isNullOrEmpty(expression)) {
                params.put("Position", expression);
            }
        }
        return params;
    }


    private boolean isCapabilityFilter() {
        return _selected.getName().equals("CapabilityFilter");
    }


    private List<Capability> getCapabilities() {
        if (_capabilities == null) {
            _capabilities = getResourceHandler().getCapabilities();
        }
        return _capabilities;
    }

    private List<Position> getPositions() {
        if (_positions == null) {
            _positions = getResourceHandler().getPositions();
        }
        return _positions;
    }

    private List<OrgGroup> getOrgGroups() {
        if (_orgGroups == null) {
            _orgGroups = getResourceHandler().getOrgGroups();
        }
        return _orgGroups;
    }

    private <T extends AbstractResourceAttribute> Vector<String> getResourceNames(
            java.util.List<T> attributes) {
        Vector<String> names = new Vector<String>();
        for (AbstractResourceAttribute a : attributes) {
             names.add(a.getName());
        }
        Collections.sort(names);
        return names;
    }


    private YResourceHandler getResourceHandler() {
        return SpecificationModel.getHandler().getResourceHandler();
    }

}
