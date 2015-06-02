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
 * Created by Jingxin XU on 08/01/2010
 */

package org.yawlfoundation.yawl.configuration.menu.action;

import org.yawlfoundation.yawl.configuration.CPort;
import org.yawlfoundation.yawl.configuration.MultipleInstanceTaskConfigSet;
import org.yawlfoundation.yawl.configuration.ProcessConfigurationModel;
import org.yawlfoundation.yawl.configuration.element.TaskConfiguration;
import org.yawlfoundation.yawl.configuration.element.TaskConfigurationCache;
import org.yawlfoundation.yawl.configuration.menu.ResourceLoader;
import org.yawlfoundation.yawl.configuration.net.*;
import org.yawlfoundation.yawl.editor.core.controlflow.YCompoundFlow;
import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandler;
import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandlerException;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.CopyAction;
import org.yawlfoundation.yawl.editor.ui.actions.PasteAction;
import org.yawlfoundation.yawl.editor.ui.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.specification.FileOperations;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationState;
import org.yawlfoundation.yawl.elements.YTask;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;


public class ApplyProcessConfigurationAction extends YAWLSelectedNetAction {

    {
        putValue(Action.SHORT_DESCRIPTION, "Apply Process Configuration");
        putValue(Action.NAME, "Apply Process Configuration");
        putValue(Action.LONG_DESCRIPTION, "Apply Process Configuration");
        putValue(Action.SMALL_ICON, getMenuIcon("asterisk_yellow"));
    }

    private NetGraph net;
    private ConfigureSet configuredElements;
    private List<VertexContainer> vertexContainersAfterDelete;
    private Set<YAWLFlowRelation> deletedflows;
    private Set<YCompoundFlow> removedFlows;
    private Map<YAWLTask, DeconfiguredTask> configuredTaskCache;
    private static ApplyProcessConfigurationAction INSTANCE;
    private boolean selected;

    private ApplyProcessConfigurationAction() {
        selected = false;
    }

    public static ApplyProcessConfigurationAction getInstance() {
        if (INSTANCE == null) INSTANCE = new ApplyProcessConfigurationAction();
        return INSTANCE;
    }

    public void init() {
        selected = false;        
    }

    public void actionPerformed(ActionEvent event) {
        selected = ! selected;
        if (selected) {
            net = getGraph();
            net.getNetModel().beginUpdate();
            multipleInstanceConfiguration();
            cancellationSetConfiguration();
            vertexContainersAfterDelete = new ArrayList<VertexContainer>();
            deletedflows = new HashSet<YAWLFlowRelation>();
            removedFlows = new HashSet<YCompoundFlow>();
            configuredElements = new ConfigureSet(net.getNetModel());
            configuredTaskCache = cacheConfiguredTasks();
            removeElements();
            try {
                changeDecorators();
            }
            catch (YControlFlowHandlerException e) {
                // do nothing
            }
            applyHideOperation();
            removeNullPorts();
            modificationAfterAddSilentTask();
            makeTasksNonConfig();
            net.getNetModel().endUpdate();
            ProcessConfigurationModel.getInstance().setApplyState(
                            ProcessConfigurationModel.ApplyState.OFF);
            SpecificationUndoManager.getInstance().disableButtons();
        }
        else {
            net.getNetModel().beginUpdate();
            SpecificationUndoManager.getInstance().undo();
//            restoreFlows();
            for (YAWLTask task : configuredTaskCache.keySet()) {
                TaskConfiguration config =
                        TaskConfigurationCache.getInstance().get(net.getNetModel(), task);
                if (config != null) config.setConfigurable(true);
                DeconfiguredTask configured = configuredTaskCache.get(task);
                if (configured.isDeconfigured()) {
                    configured.restorePorts(task);
                }
                setDecorator(task);
                net.changeLineWidth(task);
            }
            net.getNetModel().endUpdate();
        }
        publishState();
        FileOperations.validate();   // do a validation after each apply toggle
    }


    protected ImageIcon getMenuIcon(String iconName) {
        return ResourceLoader.getImageAsIcon(iconName + ".png");
    }


    private void restoreFlows() {
        for (Object cell : NetGraphModel.getRoots(net.getModel())) {
            if (cell instanceof YAWLFlowRelation) {
                YAWLFlowRelation flow = (YAWLFlowRelation) cell;
                for (YCompoundFlow compoundFlow : removedFlows) {
                    if (compoundFlow.getSource().getID().equals(flow.getSourceID()) &&
                            compoundFlow.getTarget().getID().equals(flow.getTargetID())) {
                        flow.setYFlow(compoundFlow);
                    }
                }
            }
        }
    }

    private void setDecorator(YAWLTask task) {
        YControlFlowHandler handler = SpecificationModel.getHandler().getControlFlowHandler();
        YTask yTask = task.getTask();
        try {
            if (task.hasSplitDecorator()) {
                switch (task.getSplitDecorator().getType()) {
                    case Decorator.AND_TYPE:
                        handler.setSplit(yTask, YTask._AND);
                        break;
                    case Decorator.XOR_TYPE:
                        handler.setSplit(yTask, YTask._XOR);
                        break;
                    case Decorator.OR_TYPE:
                        handler.setSplit(yTask, YTask._OR);
                        break;
                }
            }
            if (task.hasJoinDecorator()) {
                switch (task.getJoinDecorator().getType()) {
                    case Decorator.AND_TYPE:
                        handler.setJoin(yTask, YTask._AND);
                        break;
                    case Decorator.XOR_TYPE:
                        handler.setJoin(yTask, YTask._XOR);
                        break;
                    case Decorator.OR_TYPE:
                        handler.setJoin(yTask, YTask._OR);
                        break;
                }
            }
        }
        catch (YControlFlowHandlerException e) {
            // do nothing
        }
    }

    private Map<YAWLTask, DeconfiguredTask> cacheConfiguredTasks() {
        Map<YAWLTask, DeconfiguredTask> tasks = new HashMap<YAWLTask, DeconfiguredTask>();
        for (Object cell : NetGraphModel.getRoots(net.getModel())) {
            if (cell instanceof VertexContainer) {
                cell = ((VertexContainer) cell).getVertex();
            }
            if (cell instanceof YAWLTask) {
                YAWLTask task = (YAWLTask) cell;
                TaskConfiguration config = TaskConfigurationCache.getInstance().get(
                        net.getNetModel(), task);
                if (config != null && config.isConfigurable()) {
                    tasks.put(task, new DeconfiguredTask(task));
                }
            }
        }
        return tasks;
    }


    /**
     * This method handle multiple instances configuration
     */
    private void multipleInstanceConfiguration(){
        for (Object cell : NetGraphModel.getRoots(net.getModel())) {
            if (cell instanceof VertexContainer) {
                cell = ((VertexContainer) cell).getVertex();
            }
            if (cell instanceof YAWLMultipleInstanceTask) {
                YAWLTask task = (YAWLTask) cell;
                TaskConfiguration config = TaskConfigurationCache.getInstance().get(
                        net.getNetModel(), task);
                if (config != null && config.isConfigurable()) {
                    configureMultipleInstanceTask((MultipleAtomicTask) cell, config);
                }
            }
        }
    }


    private void configureMultipleInstanceTask(YAWLMultipleInstanceTask task,
                                               TaskConfiguration config) {
        MultipleInstanceTaskConfigSet configureSet = config.getConfigurationInfor();
        task.setMaximumInstances(configureSet.getReduceMax());
        task.setMinimumInstances(configureSet.getIncreaseMin());
        task.setContinuationThreshold(configureSet.getIncreaseThreshold());
        if (configureSet.isForbidDynamic()) {
            task.setInstanceCreationType(YAWLMultipleInstanceTask.STATIC_INSTANCE_CREATION);
        }
    }

    /**
     * This method handles the cancellation set configuration
     */
    private void cancellationSetConfiguration() {
        for (Object cell : NetGraphModel.getRoots(net.getModel())) {
            if (cell instanceof VertexContainer) {
                cell = ((VertexContainer) cell).getVertex();
            }
            if (cell instanceof YAWLTask) {
                YAWLTask task = (YAWLTask) cell;
                TaskConfiguration config = TaskConfigurationCache.getInstance().get(
                        net.getNetModel(), task);
                if (task.hasCancellationSetMembers() && (! config.isCancellationSetEnable())) {
                    net.clearSelection();
//                    net.setSelectionCell(task);
                    net.changeCancellationSet(task);

//                    ActionEvent simulateEvent = new ActionEvent(net, 1001,
//                            "View Cancellation Set");
//                    ViewCancellationSetAction viewAction =
//                            new ViewCancellationSetAction(task, net);
//                    viewAction.actionPerformed(simulateEvent);

 //                   for (YAWLCell member : task.getCancellationSet().getMembers()) {
 //                       net.clearSelection();
                        net.setSelectionCells(task.getCancellationSet().getMembers().toArray());
                        net.removeSelectedCellsFromVisibleCancellationSet();

//                        ActionEvent Event = new ActionEvent(net, 1001, null);
//                        RemoveFromVisibleCancellationSetAction action =
//                                RemoveFromVisibleCancellationSetAction.getInstance();
//                        action.actionPerformed(Event);
 //                   }

   //                 net.clearSelection();
   //                 net.setSelectionCell(task);
     //               net.changeCancellationSet(null);

//                    viewAction.actionPerformed(simulateEvent);
                }
            }
        }
    }

    /**
     * This method removes all elements that not needed
     */
    private void removeElements(){
        HashSet<YAWLCell> removeSet = configuredElements.getRemoveSetMembers();
        removeSet.addAll(configuredElements.getRemoveVertexContainers());

        if (net != null) {
            net.removeCellsAndTheirEdges(removeSet.toArray());

            for (YAWLCell cell : removeSet) {
                if (cell instanceof YAWLFlowRelation) {
                    removedFlows.add(((YAWLFlowRelation) cell).getYFlow());
                }
            }
        }
    }


    /**
     * This method handles the change of the decorators
     */
    private void changeDecorators() throws YControlFlowHandlerException {
        YControlFlowHandler handler = SpecificationModel.getHandler().getControlFlowHandler();
        Map<YAWLTask, Integer> decoratorChanges = configuredElements.getChangedDecorators();
        for (YAWLTask task : decoratorChanges.keySet()) {
            YTask yTask = (YTask) task.getYAWLElement();
            int decoratorType = decoratorChanges.get(task);
            if (decoratorType == ConfigureSet.NO_Join) {
                int position = task.getJoinDecorator().getCardinalPosition();
                net.setJoinDecorator(task, Decorator.NO_TYPE, position);
                handler.setJoin(yTask, YTask._XOR);
            }
            else {
                int position = task.getSplitDecorator().getCardinalPosition();
                if (decoratorType == ConfigureSet.NO_Split) {
                    net.setSplitDecorator(task, Decorator.NO_TYPE, position);
                    handler.setSplit(yTask, YTask._AND);
                }
                else if (decoratorType == ConfigureSet.AND_Split) {
                    net.setSplitDecorator(task, Decorator.AND_TYPE, position);
                    handler.setSplit(yTask, YTask._AND);
                }
                else if (decoratorType == ConfigureSet.XOR_Split) {
                    net.setSplitDecorator(task, Decorator.XOR_TYPE, position);
                    handler.setSplit(yTask, YTask._XOR);
                }
            }
        }
    }

    /**
     * This method handles the hide operation
     */
    private void applyHideOperation(){
        Set<YAWLCell> seletedElements = new HashSet<YAWLCell>();
        for (Object cell : NetGraphModel.getRoots(net.getModel())) {
            if (cell instanceof VertexContainer) {
                if (((VertexContainer) cell).getVertex() instanceof YAWLTask){
                    vertexContainersAfterDelete.add((VertexContainer) cell);
                }
            }
            if (cell instanceof YAWLTask) { //This branch handles the situation when the task itself has no decomposition and have some input ports hidden
                YAWLTask task = (YAWLTask) cell;
                TaskConfiguration config = TaskConfigurationCache.getInstance().get(
                        net.getNetModel(), task);
                if (config != null) {
                    for (CPort port : config.getInputCPorts()) {
                        if (port.getConfigurationSetting().equals("hidden")) {
                            net.setElementLabel(task, "_tau");
                            break;
                        }
                    }
                }
            }
        }

        for (VertexContainer container : vertexContainersAfterDelete) {
            if (container.getVertex() instanceof YAWLTask) {
                addSilentTasks(seletedElements, container);
            }
        }
    }

    private void addSilentTasks(Set<YAWLCell> seletedElements, VertexContainer container) {
        YAWLTask task = (YAWLTask) container.getVertex();
        TaskConfiguration config = TaskConfigurationCache.getInstance().get(
                 net.getNetModel(), task);
        if (config != null && config.isConfigurable()) {
            boolean hasHidePorts = false;
            boolean hasNoHidePorts = false;
            List<CPort> hidePorts = new ArrayList<CPort>();
            for (CPort port : config.getInputCPorts()) {
                if (port.getConfigurationSetting().equals("hidden")) {
                    hasHidePorts = true;
                    hidePorts.add(port);
                }
                else{
                    hasNoHidePorts = true;
                }
            }
            if (hasHidePorts) {//when there are some hidden ports
                if (hasNoHidePorts) {// there are some hidden ports and activated ports
                    seletedElements.add(container);
                    for (YAWLFlowRelation flow : task.getOutgoingFlows()) {
                        seletedElements.add(flow);
                    }
                    for (CPort port: hidePorts) {
                        YAWLFlowRelation[] hideFlow = port.getFlows().toArray(new YAWLFlowRelation[1]);
                        if (hideFlow[0] != null) {
                            seletedElements.add(hideFlow[0]);
                            deletedflows.add(hideFlow[0]);
                        }
                    }

                    net.clearSelection();
                    net.setSelectionCells(seletedElements.toArray());

                    ActionEvent simulateCopyEvent = new ActionEvent(net, 1001, "Copy");
                    ActionEvent simulatePasteEvent = new ActionEvent(net, 1001, "Paste");
                    CopyAction copy = CopyAction.getInstance();
                    PasteAction paste = PasteAction.getInstance();
                    copy.actionPerformed(simulateCopyEvent);
                    paste.actionPerformed(simulatePasteEvent);

                    net.removeCellsAndTheirEdges(deletedflows.toArray());


                }
                else { // all input ports are hidden
                    net.setTaskDecomposition(task, null);
                    task.setDecomposition(null);
                    net.clearSelection();
                    net.setElementLabel(task, "_tau");
                }
            }
        }
    }

    /**
     * This method do further modification after adding silent task
     */
    private void modificationAfterAddSilentTask(){

        NetElementSummary netSummary = new NetElementSummary((NetGraphModel) net.getModel());
        InputCondition start = netSummary.getInputCondition();
        OutputCondition end = netSummary.getOutputCondition();

        for (Object cell : NetGraphModel.getRoots(net.getModel())) {
            YAWLTask task = null;
            if (cell instanceof VertexContainer) {
                VertexContainer container = (VertexContainer) cell;
                if (container.getVertex() instanceof YAWLTask) {
                    task = (YAWLTask) container.getVertex();
                    if (! vertexContainersAfterDelete.contains(container)) {
                        net.setTaskDecomposition(task, null);
                        net.clearSelection();
                        net.setElementLabel(task, "_tau");
                        if (task.getIncomingFlowCount() == 0) { //when it links to the start or end condition, something need to be handled
                            net.connect(start.getDefaultSourcePort(), task.getDefaultTargetPort());
                        }
                        else if(task.getOutgoingFlowCount()==0){
                            net.connect(task.getDefaultSourcePort(), end.getDefaultTargetPort());
                        }
                    }
                }
            }
            else if (cell instanceof YAWLTask) {
                task = (YAWLTask) cell;
            }
            if (task != null) {
                if (task.getIncomingFlowCount() == 1 && task.hasJoinDecorator()) {
                    int position = task.getJoinDecorator().getCardinalPosition();
                    net.setJoinDecorator(task,Decorator.NO_TYPE ,position);
                }
            }
        }
    }

    /**
     * Since there are some flows removed , some ports will have no flows and thus should be removed either.
     */
    private void removeNullPorts(){
        for (Object cell : NetGraphModel.getRoots(net.getModel())) {
            if (cell instanceof VertexContainer) {
                cell = ((VertexContainer) cell).getVertex();
            }
            if (cell instanceof YAWLTask){
                YAWLTask task = (YAWLTask) cell;
                TaskConfiguration config = TaskConfigurationCache.getInstance().get(
                        net.getNetModel(), task);
                if (config != null && config.isConfigurable()) {
                    configuredTaskCache.get(task).setDeconfigured(true);
                    Set<CPort> removeInPorts = new HashSet<CPort>();
                    Set<CPort> removeOutPorts = new HashSet<CPort>();
                    removeNullPorts(config.getInputCPorts(), removeInPorts);
                    removeNullPorts(config.getOutputCPorts(), removeOutPorts);

                    for (CPort port : removeInPorts) {
                        config.removeInputPort(port);
                    }
                    for (CPort port : removeOutPorts) {
                        config.removeOutputPort(port);
                    }
                    config.resetCPortsID();
                }
            }
        }
    }


    private void removeNullPorts(List<CPort> ports, Set<CPort> removedSet) {
        for (CPort port : ports) {
            for (YAWLFlowRelation flow : port.getFlows()) {
                if (deletedflows.contains(flow) ||
                    configuredElements.getRemoveFlows().contains(flow)) {

                    port.getFlows().remove(flow);
                }
            }
            if (port.getFlows().isEmpty()) {
                removedSet.add(port);
            }
        }
    }

    /**
     * This method make all task became non-configured after the changes
     */
    private void makeTasksNonConfig() {
        for (Object cell : NetGraphModel.getRoots(net.getModel())) {
            if (cell instanceof VertexContainer) {
                cell = ((VertexContainer) cell).getVertex();
            }
            if (cell instanceof YAWLTask){
                YAWLTask task = (YAWLTask) cell;
                TaskConfiguration config = TaskConfigurationCache.getInstance().get(
                        net.getNetModel(), task);
                if (config != null && config.isConfigurable()) {
                    config.setConfigurable(false);
                    net.changeLineWidth(task);
                }
                if (task.hasJoinDecorator()) {
                    net.changeCellForeground(task.getJoinDecorator(), Color.BLACK);
                }
                if(task.hasSplitDecorator()){
                    net.changeCellForeground(task.getSplitDecorator(),Color.BLACK);
                }
                net.changeCellForeground(task,Color.BLACK);
            }
        }
    }


    public void specificationStateChange(SpecificationState state) {
        if (state != SpecificationState.NetSelected) {
            super.specificationStateChange(state);
        }
        else {
            NetGraph graph = YAWLEditor.getNetsPane().getSelectedGraph();
            NetConfiguration netConfiguration = NetConfigurationCache.getInstance()
                    .getOrAdd(graph.getNetModel());
            ServiceAutomatonTree automatonTree = netConfiguration.getServiceAutonomous();
            setEnabled((automatonTree == null) || automatonTree.canApplyConfiguration());
        }
    }


    private void publishState() {
        ProcessConfigurationModel.ApplyState state = selected ?
                ProcessConfigurationModel.ApplyState.ON :
                ProcessConfigurationModel.ApplyState.OFF;
        ProcessConfigurationModel.getInstance().setApplyState(state);
    }


    /***********************************************************************/

    class DeconfiguredTask {

        final YAWLTask _task;
        List<CPort> inPorts;
        List<CPort> outPorts;
        List<List<String>> sourceNames;
        List<List<String>> targetNames;
        boolean deconfigured;


        public DeconfiguredTask(YAWLTask task) {
            _task = task;
            TaskConfiguration config = TaskConfigurationCache.getInstance().get(
                    net.getNetModel(), task);
            if (config != null) {
                inPorts = new ArrayList<CPort>();
                outPorts = new ArrayList<CPort>();
                sourceNames = new ArrayList<List<String>>();
                targetNames = new ArrayList<List<String>>();
                for (CPort port : config.getInputCPorts()) {
                    inPorts.add((CPort) port.clone());
                    sourceNames.add(getSourceList(port.getFlows()));
                }
                for (CPort port : config.getOutputCPorts()) {
                    outPorts.add((CPort) port.clone());
                    targetNames.add(getTargetList(port.getFlows()));
                }
            }
        }

        public void setDeconfigured(boolean b) { deconfigured = b; }

        public boolean isDeconfigured() { return deconfigured; }

        public void restorePorts(YAWLTask task) {
            TaskConfiguration config = TaskConfigurationCache.getInstance().get(
                    net.getNetModel(), task);
            if (config != null) {
                config.getInputCPorts().clear();
                config.getOutputCPorts().clear();
                config.setInputCPorts(inPorts);
                config.setOutputCPorts(outPorts);
                regenerateInputCPorts(task);
                regenerateOutputCPorts(task);
            }
        }


        public void regenerateInputCPorts(YAWLTask task) {
            TaskConfiguration config = TaskConfigurationCache.getInstance().get(
                    net.getNetModel(), task);
            if (config != null) {
                if (hasJoinType(task, Decorator.XOR_TYPE)) {
                    int i=0;
                    for (CPort port: config.getInputCPorts()) {
                        String sourceID = sourceNames.get(i).get(0);
                        Set<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
                        for (YAWLFlowRelation flow : task.getIncomingFlows()) {
                            if (flow.getSourceVertex().getID().equals(sourceID)) {
                                flows.add(flow);
                                port.setFlows(flows);
                                break;
                            }
                        }
                        i++;
                    }
                }
                else {
                    config.getInputCPorts().get(0).setFlows(task.getIncomingFlows());
                }
            }
        }


        public void regenerateOutputCPorts(YAWLTask task) {
            TaskConfiguration config = TaskConfigurationCache.getInstance().get(
                    net.getNetModel(), task);
            if (config != null) {
                if ((! task.hasSplitDecorator()) || hasSplitType(task, Decorator.AND_TYPE)) {
                    config.getOutputCPorts().get(0).setFlows(task.getOutgoingFlows());
                }
                else if (hasSplitType(task, Decorator.XOR_TYPE)) {
                    int i=0;
                    for (CPort port: config.getOutputCPorts()) {
                        String targetID = targetNames.get(i).get(0);
                        Set<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
                        for (YAWLFlowRelation flow : task.getOutgoingFlows()) {
                            if (flow.getTargetVertex().getID().equals(targetID)) {
                                flows.add(flow);
                                port.setFlows(flows);
                                break;
                            }
                        }
                        i++;
                    }
                }
                else if (hasSplitType(task, Decorator.OR_TYPE)) {
                    int i=0;
                    for (CPort port : config.getOutputCPorts()) {
                        List<String> targetIDs = targetNames.get(i);

                    }
                }
            }
        }


        private boolean hasSplitType(YAWLTask task, int type) {
            return task.hasSplitDecorator() && (task.getSplitDecorator().getType() == type);
        }

        private boolean hasJoinType(YAWLTask task, int type) {
            return task.hasJoinDecorator() && (task.getJoinDecorator().getType() == type);
        }

        private List<String> getSourceList(Set<YAWLFlowRelation> flows) {
            List<String> sources = new ArrayList<String>();
            for (YAWLFlowRelation flow : flows) {
                sources.add(flow.getSourceVertex().getID());
            }
            return sources;
        }

        private List<String> getTargetList(Set<YAWLFlowRelation> flows) {
            List<String> targets = new ArrayList<String>();
            for (YAWLFlowRelation flow : flows) {
                targets.add(flow.getTargetVertex().getID());
            }
            return targets;
        }

    }

}
