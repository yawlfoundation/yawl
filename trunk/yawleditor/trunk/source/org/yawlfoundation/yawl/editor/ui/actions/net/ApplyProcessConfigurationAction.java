/**
 * Created by Jingxin XU on 08/01/2010
 */

package org.yawlfoundation.yawl.editor.ui.actions.net;

import org.yawlfoundation.yawl.editor.ui.actions.CopyAction;
import org.yawlfoundation.yawl.editor.ui.actions.PasteAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.*;
import org.yawlfoundation.yawl.editor.ui.specification.ProcessConfigurationModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationState;
import org.yawlfoundation.yawl.editor.ui.swing.YAWLEditorDesktop;

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
        putValue(Action.SMALL_ICON, getPNGIcon("asterisk_yellow"));
    }

    private NetGraph net;
    private ConfigureSet configuredElements;
    private java.util.List<VertexContainer> vertexContainersAfterDelete;
    private Set<YAWLFlowRelation> deletedflows;
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
            configuredElements = new ConfigureSet(net.getNetModel());
            configuredTaskCache = cacheConfiguredTasks();
            removeElements();
  //          removeNullPorts();
            changeDecorators();
            applyHideOperation();
            removeNullPorts();
            modificationAfterAddSilentTask();
            makeTasksNonConfig();
            net.getNetModel().endUpdate();
            SpecificationUndoManager.getInstance().disableButtons();
        }
        else {
            net.getNetModel().beginUpdate();
            SpecificationUndoManager.getInstance().undo();
            for (YAWLTask task : configuredTaskCache.keySet()) {
                task.setConfigurable(true);
                DeconfiguredTask configured = configuredTaskCache.get(task);
                if (configured.isDeconfigured()) {
                    configured.restorePorts(task);
                }
                net.changeLineWidth(task);
            }
            net.getNetModel().endUpdate();
        }
        publishState();
    }


    private Map<YAWLTask, DeconfiguredTask> cacheConfiguredTasks() {
        Map<YAWLTask, DeconfiguredTask> tasks = new Hashtable<YAWLTask, DeconfiguredTask>();
        for (Object cell : NetGraphModel.getRoots(net.getModel())) {
            if (cell instanceof VertexContainer) {
                cell = ((VertexContainer) cell).getVertex();
            }
            if ((cell instanceof YAWLTask) && ((YAWLTask)cell).isConfigurable()) {
                YAWLTask task = (YAWLTask) cell;
                if (task.isConfigurable()) {
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
            if ((cell instanceof YAWLTask) && ((YAWLTask)cell).isConfigurable()) {
                if (cell instanceof YAWLMultipleInstanceTask) {
                    configureMultipleInstanceTask((MultipleAtomicTask) cell);
                }
            }
        }
    }


    private void configureMultipleInstanceTask(YAWLMultipleInstanceTask task) {
        MultipleInstanceTaskConfigSet configureSet = task.getConfigurationInfor();
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
                if (task.hasCancellationSetMembers() && (! task.isCancellationSetEnable())) {
                    net.clearSelection();
//                    net.setSelectionCell(task);
                    net.changeCancellationSet(task);

//                    ActionEvent simulateEvent = new ActionEvent(net, 1001,
//                            "View Cancellation Set");
//                    ViewCancellationSetAction viewAction =
//                            new ViewCancellationSetAction(task, net);
//                    viewAction.actionPerformed(simulateEvent);

 //                   for (YAWLCell member : task.getCancellationSet().getSetMembers()) {
 //                       net.clearSelection();
                        net.setSelectionCells(task.getCancellationSet().getSetMembers().toArray());
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
        }
    }


    /**
     * This method handles the change of the decorators
     */
    private void changeDecorators(){
        Map<YAWLTask, Integer> decoratorChanges = configuredElements.getChangedDecorators();
        for (YAWLTask task : decoratorChanges.keySet()) {
            int decoratorType = decoratorChanges.get(task);
            if (decoratorType == ConfigureSet.NO_Join) {
                int position = task.getJoinDecorator().getCardinalPosition();
                net.setJoinDecorator(task, Decorator.NO_TYPE, position);
            }
            else {
                int position = task.getSplitDecorator().getCardinalPosition();
                if (decoratorType == ConfigureSet.NO_Split) {
                    net.setSplitDecorator(task, Decorator.NO_TYPE, position);
                }
                if (decoratorType == ConfigureSet.AND_Split) {
                    net.setSplitDecorator(task, Decorator.AND_TYPE, position);
                }
                if (decoratorType == ConfigureSet.XOR_Split) {
                    net.setSplitDecorator(task, Decorator.XOR_TYPE, position);
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
                for (CPort port : task.getInputCPorts()) {
                    if (port.getConfigurationSetting().equals("hidden")) {
                        net.setElementLabel(task, "_tau");
                        break;
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
        if (task.isConfigurable()) {
            boolean hasHidePorts = false;
            boolean hasNoHidePorts = false;
            List<CPort> hidePorts = new ArrayList<CPort>();
            for (CPort port : task.getInputCPorts()) {
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
                            net.connect(start, task);
                        }
                        else if(task.getOutgoingFlowCount()==0){
                            net.connect(task, end);
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
                if (task.isConfigurable()) {
                    configuredTaskCache.get(task).setDeconfigured(true);
                    Set<CPort> removeInPorts = new HashSet<CPort>();
                    Set<CPort> removeOutPorts = new HashSet<CPort>();
                    removeNullPorts(task.getInputCPorts(), removeInPorts);
                    removeNullPorts(task.getOutputCPorts(), removeOutPorts);

                    for (CPort port : removeInPorts) {
                        task.removeInputPort(port);
                    }
                    for (CPort port : removeOutPorts) {
                        task.removeOutputPort(port);
                    }
                    task.resetCPortsID();
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
                if (task.isConfigurable()) {
                    task.setConfigurable(false);
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
            NetGraph graph = YAWLEditorDesktop.getInstance().getSelectedGraph();
            ServiceAutomatonTree automatonTree = graph.getServiceAutonomous();
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

        YAWLTask _task;
        List<CPort> inPorts;
        List<CPort> outPorts;
        List<List<String>> sourceNames;
        List<List<String>> targetNames;
        boolean deconfigured;


        public DeconfiguredTask(YAWLTask task) {
            _task = task;
            inPorts = new ArrayList<CPort>();
            outPorts = new ArrayList<CPort>();
            sourceNames = new ArrayList<List<String>>();
            targetNames = new ArrayList<List<String>>();
            for (CPort port : task.getInputCPorts()) {
                inPorts.add((CPort) port.clone());
                sourceNames.add(getSourceList(port.getFlows()));
            }
            for (CPort port : task.getOutputCPorts()) {
                outPorts.add((CPort) port.clone());
                targetNames.add(getTargetList(port.getFlows()));
            }
        }

        public void setDeconfigured(boolean b) { deconfigured = b; }

        public boolean isDeconfigured() { return deconfigured; }

        public void restorePorts(YAWLTask task) {
            task.getInputCPorts().clear();
            task.getOutputCPorts().clear();
            task.setInputCPorts(inPorts);
            task.setOutputCPorts(outPorts);
            regenerateInputCPorts(task);
            regenerateOutputCPorts(task);
        }


        public void regenerateInputCPorts(YAWLTask task) {
            if (hasJoinType(task, Decorator.XOR_TYPE)) {
                int i=0;
                for (CPort port: task.getInputCPorts()) {
                    String sourceID = sourceNames.get(i).get(0);
                    Set<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
                    for (YAWLFlowRelation flow : task.getIncomingFlows()) {
                        if (flow.getSourceVertex().getEngineId().equals(sourceID)) {
                            flows.add(flow);
                            port.setFlows(flows);
                            break;
                        }
                    }
                    i++;
                }
            }
            else {
                task.getInputCPorts().get(0).setFlows(task.getIncomingFlows());
            }
        }


        public void regenerateOutputCPorts(YAWLTask task) {
            if ((! task.hasSplitDecorator()) || hasSplitType(task, Decorator.AND_TYPE)) {
                task.getOutputCPorts().get(0).setFlows(task.getOutgoingFlows());
            }
            else if (hasSplitType(task, Decorator.XOR_TYPE)) {
                int i=0;
                for (CPort port: task.getOutputCPorts()) {
                    String targetID = targetNames.get(i).get(0);
                    Set<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
                    for (YAWLFlowRelation flow : task.getOutgoingFlows()) {
                        if (flow.getTargetVertex().getEngineId().equals(targetID)) {
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
                for (CPort port : task.getOutputCPorts()) {
                    List<String> targetIDs = targetNames.get(i);

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
                sources.add(flow.getSourceVertex().getEngineId());
            }
            return sources;
        }

        private List<String> getTargetList(Set<YAWLFlowRelation> flows) {
            List<String> targets = new ArrayList<String>();
            for (YAWLFlowRelation flow : flows) {
                targets.add(flow.getTargetVertex().getEngineId());
            }
            return targets;
        }

    }

}
