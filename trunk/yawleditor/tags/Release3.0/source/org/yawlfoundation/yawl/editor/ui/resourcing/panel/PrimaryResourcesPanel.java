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

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.core.resourcing.BasicOfferInteraction;
import org.yawlfoundation.yawl.editor.core.resourcing.DynParam;
import org.yawlfoundation.yawl.editor.core.resourcing.TaskResourceSet;
import org.yawlfoundation.yawl.editor.ui.resourcing.AllocatorRenderer;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourceDialog;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourceTableType;
import org.yawlfoundation.yawl.editor.ui.resourcing.tablemodel.NetParamTableModel;
import org.yawlfoundation.yawl.editor.ui.resourcing.tablemodel.ParticipantTableModel;
import org.yawlfoundation.yawl.editor.ui.resourcing.tablemodel.RoleTableModel;
import org.yawlfoundation.yawl.elements.YAtomicTask;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.resourcing.AbstractSelector;
import org.yawlfoundation.yawl.resourcing.allocators.AbstractAllocator;
import org.yawlfoundation.yawl.resourcing.interactions.AbstractInteraction;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Michael Adams
 * @date 24/06/13
 */
public class PrimaryResourcesPanel extends AbstractResourceTabContent implements ItemListener {

    private ParticipantTableModel participantTableModel;
    private RoleTableModel roleTableModel;
    private NetParamTableModel netParamTableModel;
    private FiltersPanel filtersPanel;
    private ConstraintsPanel constraintsPanel;
    private JPanel offerPanelContent;
    private JPanel allocatePanelContent;
    private JCheckBox chkOffer;
    private JCheckBox chkAllocate;
    private JCheckBox chkStart;
    private JComboBox cbxAllocations;


    public PrimaryResourcesPanel(YNet net, YAtomicTask task, ResourceDialog owner) {
        super(net, task);
        createContent(owner);
        load();
    }


    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        boolean selected = e.getStateChange() == ItemEvent.SELECTED;
        if (source == chkOffer) {
            enablePanelContent(offerPanelContent, selected);
            if (selected) constraintsPanel.enableCombos();
        }
        else if (source == chkAllocate) {
            enablePanelContent(allocatePanelContent, selected);
        }
    }


    public void load() {
        enablePanelContent(offerPanelContent, false);
        enablePanelContent(allocatePanelContent, false);

        TaskResourceSet resources = getTaskResources();
        BasicOfferInteraction offerResources = resources.getOffer();

        populateInitiators(resources);

        participantTableModel.setValues(new ArrayList<Participant>(
                offerResources.getParticipantSet().getAll()));
        roleTableModel.setValues(new ArrayList<Role>(
                offerResources.getRoleSet().getAll()));
        netParamTableModel.setValues(new ArrayList<DynParam>(
                offerResources.getDynParamSet().getAll()));
        filtersPanel.load(offerResources);
        constraintsPanel.load(offerResources);

        populateAllocators(resources);

        enablePanelContent(offerPanelContent, chkOffer.isSelected());
        if (chkOffer.isSelected()) constraintsPanel.enableCombos();
        enablePanelContent(allocatePanelContent, chkAllocate.isSelected());
    }


    public void save() {
        TaskResourceSet resources = getTaskResources();
        BasicOfferInteraction offerResources = resources.getOffer();

        offerResources.getParticipantSet().set(
                new ArrayList<Participant>(participantTableModel.getValues()));
        offerResources.getRoleSet().set(
                new ArrayList<Role>(roleTableModel.getValues()));
        offerResources.getDynParamSet().set(
                new ArrayList<DynParam>(netParamTableModel.getValues()));
        filtersPanel.save(offerResources);
        constraintsPanel.save(offerResources);

        setInitiators(resources);

        if (chkAllocate.isSelected()) {
            resources.getAllocate().setAllocator(
                    (AbstractAllocator) cbxAllocations.getSelectedItem());
        }
        else {
            resources.getAllocate().clearAllocator();
        }
    }


    public String getInteractionString() {
        TaskResourceSet resources = getTaskResources();
        return resources != null ? resources.getInitiatorChars() : "";
    }


    private void createContent(ResourceDialog owner) {
        add(createOfferPanel(owner));
        add(createAllocatePanel(owner));
        add(createStartPanel(owner));
    }

    private JPanel createOfferPanel(ResourceDialog owner) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Offer"));
        chkOffer = createCheckBox("Enable System Offer", KeyEvent.VK_O, this, owner);
        panel.add(createCheckBoxPanel(chkOffer), BorderLayout.NORTH);
        panel.add(createOfferPanelContent(owner), BorderLayout.CENTER);
        return panel;
    }


    private JPanel createCheckBoxPanel(JCheckBox checkBox) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(0,10,0,0));
        panel.add(checkBox, BorderLayout.PAGE_START);
        return panel;
    }


    private JPanel createAllocatePanel(ResourceDialog owner) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Allocate"));
        chkAllocate = createCheckBox("Enable System Allocation", KeyEvent.VK_A, this, owner);
        panel.add(createCheckBoxPanel(chkAllocate), BorderLayout.NORTH);
        panel.add(createAllocatePanelContent(owner), BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(660, 90));
        return panel;
    }

    private JPanel createStartPanel(ResourceDialog owner) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Start"));
        chkStart = createCheckBox("Enable System Start", KeyEvent.VK_S, this, owner);
        panel.add(createCheckBoxPanel(chkStart), BorderLayout.PAGE_START);
        panel.setPreferredSize(new Dimension(660, 50));
        return panel;
    }

    private JPanel createOfferPanelContent(ResourceDialog owner) {
        offerPanelContent = new JPanel(new BorderLayout());
        offerPanelContent.add(createOfferResourceTables(owner), BorderLayout.NORTH);
        offerPanelContent.add(createOfferFiltersTable(owner), BorderLayout.CENTER);
        return offerPanelContent;
    }


    private JPanel createAllocatePanelContent(ResourceDialog owner) {
        allocatePanelContent = new JPanel();
        allocatePanelContent.setBorder(new EmptyBorder(5,10,5,205));
        allocatePanelContent.add(new JLabel("Allocation Strategy: "));
        cbxAllocations = new JComboBox();
        cbxAllocations.setRenderer(new AllocatorRenderer());
        cbxAllocations.setPreferredSize(new Dimension(300, 25));
        cbxAllocations.addActionListener(owner);
        allocatePanelContent.add(cbxAllocations);
        return allocatePanelContent;
    }


    private JPanel createOfferResourceTables(ResourceDialog owner) {
        JPanel panel = new JPanel();
        ResourceTablePanel tablePanel = new ResourceTablePanel(ResourceTableType.Participant);
        tablePanel.showEditButton(false);
        participantTableModel = (ParticipantTableModel) tablePanel.getTableModel();
        participantTableModel.setOwner(owner);
        participantTableModel.addTableModelListener(tablePanel);
        panel.add(tablePanel);

        tablePanel = new ResourceTablePanel(ResourceTableType.Role);
        tablePanel.showEditButton(false);
        roleTableModel = (RoleTableModel) tablePanel.getTableModel();
        roleTableModel.setOwner(owner);
        roleTableModel.addTableModelListener(tablePanel);
        panel.add(tablePanel);

        tablePanel = new ResourceTablePanel(ResourceTableType.NetParam);
        netParamTableModel = (NetParamTableModel) tablePanel.getTableModel();
        netParamTableModel.setOwner(owner);
        netParamTableModel.addTableModelListener(tablePanel);
        panel.add(tablePanel);

        return panel;
    }

    private JPanel createOfferFiltersTable(ResourceDialog owner) {
        JPanel panel = new JPanel();
        filtersPanel = new FiltersPanel(owner);
        constraintsPanel = new ConstraintsPanel(owner, getAllPrecedingTasks(getTask()));
        panel.add(filtersPanel);
        panel.add(constraintsPanel);
        return panel;
    }


    private void populateInitiators(TaskResourceSet resources) {
        chkOffer.setSelected(isSystemInitiated(resources.getOffer().getInitiator()));
        chkAllocate.setSelected(isSystemInitiated(resources.getAllocate().getInitiator()));
        chkStart.setSelected(isSystemInitiated(resources.getStart().getInitiator()));
    }


    private void populateAllocators(TaskResourceSet resources) {
        try {
            AbstractSelector defaultAllocator = null;
            java.util.List<AbstractSelector> allocators = YConnector.getAllocators();
            Collections.sort(allocators);
            for (AbstractSelector allocator : allocators) {
                if (! StringUtil.isNullOrEmpty(allocator.getDisplayName())) {
                    cbxAllocations.addItem(allocator);
                }
                if (allocator.getDisplayName().equals("Random Choice")) {
                    defaultAllocator = allocator;
                }
            }
            AbstractSelector allocator = resources.getAllocate().getAllocator();
            if (allocator == null) allocator = defaultAllocator;
            cbxAllocations.setSelectedItem(allocator);
        }
        catch (IOException ioe) {
            getLog().warn(ioe.getMessage());
        }
    }


    private boolean isSystemInitiated(int initiator) {
        return initiator == AbstractInteraction.SYSTEM_INITIATED;
    }


    private void setInitiators(TaskResourceSet resources) {
        resources.getOffer().setInitiator(getInitiatorValue(chkOffer));
        resources.getAllocate().setInitiator(getInitiatorValue(chkAllocate));
        resources.getStart().setInitiator(getInitiatorValue(chkStart));
    }


    private int getInitiatorValue(JCheckBox checkbox) {
        return checkbox.isSelected() ? AbstractInteraction.SYSTEM_INITIATED :
                AbstractInteraction.USER_INITIATED;
    }

}
