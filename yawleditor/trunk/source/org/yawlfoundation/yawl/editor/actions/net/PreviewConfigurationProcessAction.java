/**
 * Created by Jingxin XU on 10/01/2010
 *
 */

package org.yawlfoundation.yawl.editor.actions.net;

import org.jgraph.graph.GraphCell;
import org.yawlfoundation.yawl.editor.elements.model.Decorator;
import org.yawlfoundation.yawl.editor.elements.model.YAWLCell;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.net.ConfigureSet;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.specification.ProcessConfigurationModel;
import org.yawlfoundation.yawl.editor.specification.ProcessConfigurationModelListener;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;

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
    private SpecificationModel.State lastPublishedNetState;


    // called from getInstance()
    private PreviewConfigurationProcessAction() {
        init();
        disabledViaSettings = false;
        lastPublishedNetState = SpecificationModel.State.NO_NETS_EXIST;
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
        net.stopUndoableEdits();
        ConfigureSet configuredElements = new ConfigureSet(net.getNetModel());
        HashSet<YAWLCell> removeSet = configuredElements.getRemoveSetMembers();
        if (removeSet.isEmpty()) return;
        
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

    public void receiveSpecificationModelNotification(SpecificationModel.State state) {
        lastPublishedNetState = state;
        hasOpenNetState = checkOpenState();
        if (! disabledViaSettings) {
            super.receiveSpecificationModelNotification(state);
        }
        else if (lastPublishedNetState == SpecificationModel.State.SOME_NET_SELECTED) {
            if (! selected) actionPerformed(null);
        }
        else if (! hasOpenNetState) {
            init();
        }
    }

    private boolean checkOpenState() {
        return ! ((lastPublishedNetState == SpecificationModel.State.NO_NETS_EXIST) ||
                  (lastPublishedNetState == SpecificationModel.State.NO_NET_SELECTED));
    }


    public void setEnabled() {
        setEnabled((! disabledViaSettings) && hasOpenNetState);
    }

}
