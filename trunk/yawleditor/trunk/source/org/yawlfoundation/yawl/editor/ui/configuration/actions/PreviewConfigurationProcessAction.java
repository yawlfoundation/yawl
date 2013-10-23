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

/**
 * Created by Jingxin XU on 10/01/2010
 *
 */

package org.yawlfoundation.yawl.editor.ui.configuration.actions;

import org.jgraph.graph.GraphCell;
import org.yawlfoundation.yawl.editor.ui.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.Decorator;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLCell;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.ConfigureSet;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.ProcessConfigurationModel;
import org.yawlfoundation.yawl.editor.ui.specification.ProcessConfigurationModelListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;

public class PreviewConfigurationProcessAction extends YAWLSelectedNetAction
        implements ProcessConfigurationModelListener {

    {
        putValue(Action.SHORT_DESCRIPTION, "Preview Process Configuration");
        putValue(Action.NAME, "Preview Process Configuration");
        putValue(Action.LONG_DESCRIPTION, "Preview Process Configuration");
        putValue(Action.SMALL_ICON, getPNGIcon("preview"));

    }

    private static PreviewConfigurationProcessAction INSTANCE;

    private HashSet<YAWLCell> oldElements;
    private boolean selected;
    private boolean disabledViaSettings;
    private boolean hasOpenNetState;
    private SpecificationState lastPublishedNetState;


    // called from getInstance()
    private PreviewConfigurationProcessAction() {
        init();
        disabledViaSettings = false;
        lastPublishedNetState = SpecificationState.NoNetsExist;
        ProcessConfigurationModel.getInstance().subscribe(this);
    }

    public static PreviewConfigurationProcessAction getInstance() {
        if (INSTANCE == null) INSTANCE = new PreviewConfigurationProcessAction();
        return INSTANCE;
    }

    public void init() {
        oldElements = new HashSet<YAWLCell>();
        selected = false;
    }

    
    public void actionPerformed(ActionEvent event) {
        NetGraph net = getGraph();
        ConfigureSet configuredElements = new ConfigureSet(net.getNetModel());
        HashSet<YAWLCell> removeSet = configuredElements.getRemoveSetMembers();
        if (removeSet.isEmpty()) return;

        net.stopUndoableEdits();        
        net.getModel().beginUpdate();
        selected = ! selected;

        if (selected) {
            for (YAWLCell cell: oldElements){ // trace back the original grey out elements into black first
                setColour(net, cell, Color.BLACK);
            }

            oldElements.clear();
            for (YAWLCell cell: removeSet){
                setColour(net, cell, Color.gray);
                oldElements.add(cell);
            }
        }
        else {
            for (YAWLCell cell: removeSet){
                setColour(net, cell, Color.BLACK);
            }
        }
        net.getModel().endUpdate();
        net.startUndoableEdits();
        publishState();
    }

    private void setColour(NetGraph net, YAWLCell cell, Color colour) {
        net.changeCellForeground((GraphCell) cell, colour);
        if (cell instanceof YAWLTask) {
            YAWLTask task = (YAWLTask) cell;
            Decorator decorator = task.getJoinDecorator();
            if (decorator != null) {
                net.changeCellForeground(decorator, colour);
            }
            decorator = task.getSplitDecorator();
            if (decorator != null) {
                net.changeCellForeground(decorator, colour);
            }
        }
    }

    private void publishState() {
        if (! disabledViaSettings) {
            ProcessConfigurationModel.PreviewState state = selected ?
                    ProcessConfigurationModel.PreviewState.ON :
                    ProcessConfigurationModel.PreviewState.OFF;
            ProcessConfigurationModel.getInstance().setPreviewState(state);
        }    
    }

    public void processConfigurationModelStateChanged(
            ProcessConfigurationModel.PreviewState previewState,
            ProcessConfigurationModel.ApplyState applyState) {

        if (previewState == ProcessConfigurationModel.PreviewState.AUTO) {
            if (hasOpenNetState && (! selected)) {
                actionPerformed(null);
            }
            disabledViaSettings = true;
        }
        else if (previewState == ProcessConfigurationModel.PreviewState.OFF) {
            if (hasOpenNetState && (selected)) {
                actionPerformed(null);
            }
            disabledViaSettings = false;
        }
        setEnabled();
    }

    public void specificationStateChange(SpecificationState state) {
        lastPublishedNetState = state;
        hasOpenNetState = checkOpenState();
        if (! disabledViaSettings) {
            super.specificationStateChange(state);
        }
        else if (lastPublishedNetState == SpecificationState.NetSelected) {
            if (! selected) actionPerformed(null);
        }
        else if (! hasOpenNetState) {
            init();
        }
    }

    private boolean checkOpenState() {
        return ! ((lastPublishedNetState == SpecificationState.NoNetsExist) ||
                  (lastPublishedNetState == SpecificationState.NoNetSelected));
    }


    public void setEnabled() {
        setEnabled((! disabledViaSettings) && hasOpenNetState);
    }

}
