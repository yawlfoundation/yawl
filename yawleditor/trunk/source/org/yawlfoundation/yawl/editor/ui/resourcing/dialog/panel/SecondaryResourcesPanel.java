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

package org.yawlfoundation.yawl.editor.ui.resourcing.dialog.panel;

import org.yawlfoundation.yawl.editor.core.resourcing.BasicSecondaryResources;
import org.yawlfoundation.yawl.editor.core.resourcing.GenericNonHumanCategory;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.ResourceDialog;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.panel.ResourceTablePanel;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.ResourceTableType;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.tablemodel.NonHumanResourceCategoryTableModel;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.tablemodel.NonHumanResourceTableModel;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.tablemodel.ParticipantTableModel;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.tablemodel.RoleTableModel;
import org.yawlfoundation.yawl.elements.YAtomicTask;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author Michael Adams
 * @date 24/06/13
 */
public class SecondaryResourcesPanel extends AbstractResourceTabContent {

    private ParticipantTableModel _participantTableModel;
    private RoleTableModel _roleTableModel;
    private NonHumanResourceTableModel _nhrTableModel;
    private NonHumanResourceCategoryTableModel _nhrCategoryTableModel;


    public SecondaryResourcesPanel(YNet net, YAtomicTask task, ResourceDialog owner) {
        super(net, task);
        createContent(owner);
        load();
    }


    public void load() {
        BasicSecondaryResources resources = getTaskResources().getSecondary();

        _participantTableModel.setValues(new ArrayList<Participant>(
                resources.getParticipantSet().getAll()));
        _roleTableModel.setValues(new ArrayList<Role>(
                resources.getRoleSet().getAll()));
        _nhrTableModel.setValues(new ArrayList<NonHumanResource>(
                resources.getNonHumanResourceSet().getAll()));
        _nhrCategoryTableModel.setValues(new ArrayList<GenericNonHumanCategory>(
                resources.getNonHumanCategorySet().getAll()));
    }


    public void save() {
        BasicSecondaryResources resources = getTaskResources().getSecondary();

        resources.getParticipantSet().set(
                new ArrayList<Participant>(_participantTableModel.getValues()));
        resources.getRoleSet().set(
                new ArrayList<Role>(_roleTableModel.getValues()));
        resources.getNonHumanResourceSet().set(
                new ArrayList<NonHumanResource>(_nhrTableModel.getValues()));
        resources.getNonHumanCategorySet().set(
                new ArrayList<GenericNonHumanCategory>(_nhrCategoryTableModel.getValues()));
    }


    private void createContent(ResourceDialog owner) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2));
        panel.add(createParticipantTablePanel(owner));
        panel.add(createRoleTablePanel(owner));
        panel.add(createNonHumanResourceTablePanel(owner));
        panel.add(createNonHumanResourceCategoryTablePanel(owner));
        add(panel);
    }


    private ResourceTablePanel createParticipantTablePanel(ResourceDialog owner) {
        ResourceTablePanel tablePanel = createTablePanel(ResourceTableType.Participant);
        _participantTableModel = (ParticipantTableModel) tablePanel.getTableModel();
        _participantTableModel.setEnabled(true);
        _participantTableModel.setOwner(owner);
        _participantTableModel.addTableModelListener(tablePanel);
        return tablePanel;
    }


    private ResourceTablePanel createRoleTablePanel(ResourceDialog owner) {
        ResourceTablePanel tablePanel = createTablePanel(ResourceTableType.Role);
        _roleTableModel = (RoleTableModel) tablePanel.getTableModel();
        _roleTableModel.setAllowDuplicates(true);
        _roleTableModel.setOwner(owner);
        _roleTableModel.addTableModelListener(tablePanel);
        return tablePanel;
    }


    private ResourceTablePanel createNonHumanResourceTablePanel(ResourceDialog owner) {
        ResourceTablePanel tablePanel = createTablePanel(ResourceTableType.NonHumanResource);
        _nhrTableModel = (NonHumanResourceTableModel) tablePanel.getTableModel();
        _nhrTableModel.setOwner(owner);
        _nhrTableModel.addTableModelListener(tablePanel);
        return tablePanel;
    }


    private ResourceTablePanel createNonHumanResourceCategoryTablePanel(ResourceDialog owner) {
        ResourceTablePanel tablePanel =
                createTablePanel(ResourceTableType.NonHumanResourceCategory);
        _nhrCategoryTableModel =
                (NonHumanResourceCategoryTableModel) tablePanel.getTableModel();
        _nhrCategoryTableModel.setOwner(owner);
        _nhrCategoryTableModel.addTableModelListener(tablePanel);
        return tablePanel;
    }

    private ResourceTablePanel createTablePanel(ResourceTableType tableType) {
        ResourceTablePanel tablePanel = new ResourceTablePanel(tableType);
        tablePanel.showEditButton(false);
        return tablePanel;
    }

}
