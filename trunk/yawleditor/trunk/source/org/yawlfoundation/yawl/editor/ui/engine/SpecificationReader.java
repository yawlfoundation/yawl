/*
 * Created on 09/02/2006
 * YAWLEditor v1.4 
 *
 * @author Lindsay Bradford
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.ui.engine;

import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.core.layout.YLayout;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.CancellationSet;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.DefaultLayoutArranger;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.unmarshal.YMetaData;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class SpecificationReader {

    private static final Point DEFAULT_LOCATION = new Point(100,100);

    private SpecificationModel _model;
    private YSpecificationHandler _handler;
    private Map<Object, Object> _engineToEditorElementMap;


    public SpecificationReader() {
        _model = SpecificationModel.getInstance();
        _handler = SpecificationModel.getHandler();
        _engineToEditorElementMap = new Hashtable<Object, Object>();
    }


    public boolean load(String fileName) {
        boolean loaded = loadFile(fileName);
        if (loaded) {
            createEditorObjects();
            layoutEditorObjects();
            finaliseLoad();
        }
        return loaded;
    }


    private boolean loadFile(String fileName) {
        try {
            _model.loadFromFile(fileName);
        }
        catch (IOException ioe) {
            String errorMsg = ioe.getMessage();
            JOptionPane.showMessageDialog(YAWLEditor.getInstance(),
                    "Failed to load specification.\n" +
                    (errorMsg.length() > 0 ? "Reason: " + errorMsg : ""),
                    "Specification File Load Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }


    private boolean layoutEditorObjects() {
        YLayout layout = _handler.getLayout();
        if (layout.hasNets()) {
            LayoutImporter.importAndApply(layout);
            return true;
        }

        // layout has no information, revert to default layout
        removeUnnecessaryDecorators();
        new DefaultLayoutArranger().layoutSpecification();
        return false;
    }


    private void finaliseLoad() {
        Publisher.getInstance().publishOpenFileEvent();
        SpecificationUndoManager.getInstance().discardAllEdits();
        ConfigurationImporter.ApplyConfiguration();
    }


    private void createEditorObjects() {
        convertEngineMetaData();
        importNets();
        populateEditorNets();
    }

    private void convertEngineMetaData() {
        YMetaData metaData = _handler.getSpecification().getMetaData();

        _model.setVersionNumber(metaData.getVersion());

        // reset version change for file open
        _model.setVersionChanged(false);
    }


    private void importNets() {
        YNet rootNet = _handler.getControlFlowHandler().getRootNet();
        importNet(rootNet);

        // import sub-nets
        for (YNet net : _handler.getControlFlowHandler().getNets()) {
            if (! net.equals(rootNet)) {
                importNet(net);
            }
        }
    }


    private NetGraphModel importNet(YNet engineNet) {
        NetGraph editorNet = new NetGraph(engineNet);
        editorNet.setName(engineNet.getID());
        NetGraphModel graphModel = editorNet.getNetModel();
        _model.getNets().addNoUndo(graphModel);

        YAWLEditor.getNetsPane().openNet(editorNet);
        return graphModel;
    }


    private void populateEditorNets() {
        for (NetGraphModel netModel : _model.getNets()) {
            populateEditorNet((YNet) netModel.getDecomposition(), netModel);
        }
    }


    private void populateEditorNet(YNet yNet, NetGraphModel editorNet) {
        EngineNetElementSummary engineNetElementSummary = new EngineNetElementSummary(yNet);

        InputCondition inputCondition = new InputCondition(DEFAULT_LOCATION,
                yNet.getInputCondition());
        addElement(editorNet.getGraph(), inputCondition);
        _engineToEditorElementMap.put(yNet.getInputCondition(), inputCondition);

        OutputCondition outputCondition = new OutputCondition(DEFAULT_LOCATION,
                yNet.getOutputCondition());
        addElement(editorNet.getGraph(), outputCondition);
        _engineToEditorElementMap.put(yNet.getOutputCondition(), outputCondition);

        populateElements(engineNetElementSummary, editorNet);
        populateFlows(engineNetElementSummary.getFlows(), editorNet);
//        removeImplicitConditions(engineNetElementSummary.getConditions(), editorNet);
        populateCancellationSetDetail(engineNetElementSummary.getTasksWithCancellationSets());
    }



    private void populateElements(EngineNetElementSummary engineNetSummary,
                                  NetGraphModel editorNet) {
        populateAtomicTasks(engineNetSummary.getAtomicTasks(), editorNet);
        populateCompositeTasks(engineNetSummary.getCompositeTasks(), editorNet);
        populateConditions(engineNetSummary.getConditions(), editorNet);
    }


    private void populateAtomicTasks(Set<YAtomicTask> engineAtomicTasks,
                                     NetGraphModel editorNet) {
        for (YAtomicTask engineAtomicTask : engineAtomicTasks) {
            YAWLAtomicTask editorAtomicTask;
            if (engineAtomicTask.isMultiInstance()) {
                editorAtomicTask = new MultipleAtomicTask(DEFAULT_LOCATION, engineAtomicTask);
            }
            else {
                editorAtomicTask = new AtomicTask(DEFAULT_LOCATION, engineAtomicTask);
            }
            addElement(editorNet.getGraph(), (YAWLTask) editorAtomicTask, engineAtomicTask);

            setTaskDecorators(engineAtomicTask, (YAWLTask) editorAtomicTask, editorNet);

            if (engineAtomicTask.getConfigurationElement() != null) {
                ConfigurationImporter.CTaskList.add((YAWLTask) editorAtomicTask);
                ConfigurationImporter.map.put(editorAtomicTask,
                        engineAtomicTask.getConfigurationElement());
                ConfigurationImporter.NetTaskMap.put(editorAtomicTask, editorNet);
            }

            _engineToEditorElementMap.put(engineAtomicTask, editorAtomicTask);
        }

    }

    private void addElement(NetGraph graph, YAWLVertex element) {
        addElement(graph, element, null);
    }


    private void addElement(NetGraph graph, YAWLVertex vertex,
                            YExternalNetElement netElement) {
        graph.addElement(vertex);

        String label = null;
        if (vertex.getName() != null) {
            label = vertex.getName();
        }
        else if (! (netElement == null || netElement.getName() == null)) {
            label = netElement.getName();
        }
        else if ((netElement instanceof YTask) &&
                ((YTask) netElement).getDecompositionPrototype() != null) {
            label = ((YTask) netElement).getDecompositionPrototype().getID();
        }
        if (label != null) graph.setElementLabel(vertex, label);
    }


    private void setTaskDecorators(YTask engineTask, YAWLTask editorTask,
                                   NetGraphModel editorNet) {
        editorNet.setJoinDecorator(editorTask, engineToEditorJoin(engineTask),
                JoinDecorator.getDefaultPosition());
        editorNet.setSplitDecorator(editorTask, engineToEditorSplit(engineTask),
                SplitDecorator.getDefaultPosition());
    }


    private void populateCompositeTasks(Set<YCompositeTask> engineCompositeTasks,
                                        NetGraphModel editorNet) {
        for (YCompositeTask engineCompositeTask : engineCompositeTasks) {
            YAWLCompositeTask editorCompositeTask;
            if (engineCompositeTask.getMultiInstanceAttributes() == null) {
                editorCompositeTask = new CompositeTask(DEFAULT_LOCATION, engineCompositeTask);
            }
            else {
                editorCompositeTask = new MultipleCompositeTask(
                        DEFAULT_LOCATION, engineCompositeTask);
            }
            addElement(editorNet.getGraph(), (YAWLTask) editorCompositeTask, engineCompositeTask);

            setTaskDecorators(engineCompositeTask, (YAWLTask) editorCompositeTask, editorNet);

            if (engineCompositeTask.getConfigurationElement() != null) {
                ConfigurationImporter.CTaskList.add((YAWLTask) editorCompositeTask);
                ConfigurationImporter.map.put(editorCompositeTask,
                        engineCompositeTask.getConfigurationElement());
                ConfigurationImporter.NetTaskMap.put(editorCompositeTask, editorNet);
            }

            _engineToEditorElementMap.put(engineCompositeTask, editorCompositeTask);
        }
    }


    /********************************************************************************/


    private void populateConditions(Set<YCondition> engineConditions,
                                    NetGraphModel editorNet) {
        for (YCondition engineCondition : engineConditions) {

            Condition editorCondition = new Condition(DEFAULT_LOCATION, engineCondition);
            addElement(editorNet.getGraph(), editorCondition, engineCondition);
            _engineToEditorElementMap.put(engineCondition, editorCondition);
        }
    }


    private void populateFlows(Set<YFlow> engineFlows, NetGraphModel editorNet) {

        for (YFlow engineFlow : engineFlows) {
            YAWLVertex sourceVertex = (YAWLVertex) _engineToEditorElementMap.get(
                    engineFlow.getPriorElement());
            YAWLVertex targetVertex = (YAWLVertex) _engineToEditorElementMap.get(
                    engineFlow.getNextElement());
            YAWLFlowRelation flow = editorNet.getGraph().connect(sourceVertex,
                    targetVertex);

            // when a default flow is exported, it has no predicate or ordering recorded
            // (because it is the _default_ flow) - so when importing from that xml,
            // a default predicate and ordering need to be reinstated.
            if (engineFlow.isDefaultFlow()) {
                if (flow.getPredicate() == null) {
                    flow.setPredicate("true()");
                }
                flow.setPriority(10000);        // ensure it's ordered last
            }
        }
    }

    private void populateCancellationSetDetail(Set<YTask> engineTasksWithCancellationSets) {
        for (YTask engineTask : engineTasksWithCancellationSets) {
            YAWLTask editorTask = (YAWLTask) _engineToEditorElementMap.get(engineTask);

            CancellationSet editorTaskCancellationSet = new CancellationSet(editorTask);

            for (YExternalNetElement engineSetMember : engineTask.getRemoveSet()) {
                YAWLCell editorSetMember = (YAWLCell) _engineToEditorElementMap.get(engineSetMember);
                editorTaskCancellationSet.addMember(editorSetMember);
            }
            editorTask.setCancellationSet(editorTaskCancellationSet);
        }
    }

    private void removeImplicitConditions(Set<YCondition> engineConditions,
                                          NetGraphModel netModel) {
        for (YCondition engineCondition : engineConditions) {
            if (engineCondition.isImplicit()) {
                Condition editorCondition = (Condition)
                        _engineToEditorElementMap.get(engineCondition);

                YAWLFlowRelation incomingFlow = editorCondition.getOnlyIncomingFlow();
                YAWLFlowRelation outgoingFlow = editorCondition.getOnlyOutgoingFlow();
                if (incomingFlow != null && outgoingFlow != null) {
                    YAWLTask sourceTask = incomingFlow.getSourceTask();
                    YAWLTask targetTask = outgoingFlow.getTargetTask();
                    if (sourceTask != null && targetTask != null) {
                        String sourcePredicate = incomingFlow.getPredicate();
                        int sourcePriority = incomingFlow.getPriority();
                        netModel.removeCells(new Object[]{editorCondition});
                        removeEngineFlow(netModel, incomingFlow);
                        removeEngineFlow(netModel, outgoingFlow);

                        YAWLFlowRelation replacementFlow =
                                netModel.getGraph().connect(sourceTask, targetTask);

                        // map predicate & priority from removed condition to new flow
                        replacementFlow.setPredicate(sourcePredicate);
                        replacementFlow.setPriority(sourcePriority);
                        _engineToEditorElementMap.put(engineCondition, replacementFlow);
                    }
                }
            }
        }
    }


    private void removeEngineFlow(NetGraphModel netModel, YAWLFlowRelation flow) {
        _handler.getControlFlowHandler().removeFlow( netModel.getName(),
                flow.getSourceID(), flow.getTargetID());

    }

    private void removeUnnecessaryDecorators() {
        for (NetGraphModel net : _model.getNets())
            removeUnnecessaryDecorators(net);
    }


    private void removeUnnecessaryDecorators(NetGraphModel editorNet) {
        for (YAWLTask editorTask : NetUtilities.getAllTasks(editorNet)) {
            if (editorTask.hasJoinDecorator() && editorTask.getIncomingFlowCount() < 2) {
                editorNet.setJoinDecorator(
                        editorTask,
                        JoinDecorator.NO_TYPE,
                        JoinDecorator.NOWHERE
                );
            }
            if (editorTask.hasSplitDecorator() && editorTask.getOutgoingFlowCount() < 2) {
                editorNet.setSplitDecorator(
                        editorTask,
                        SplitDecorator.NO_TYPE,
                        SplitDecorator.NOWHERE
                );
            }
        }
    }

    private int engineToEditorJoin(YTask engineTask) {
        switch (engineTask.getJoinType()) {
            case YTask._AND : return Decorator.AND_TYPE;
            case YTask._OR  : return Decorator.OR_TYPE;
            case YTask._XOR : return Decorator.XOR_TYPE;
        }
        return Decorator.XOR_TYPE;
    }

    private int engineToEditorSplit(YTask engineTask) {
        switch (engineTask.getSplitType()) {
            case YTask._AND : return Decorator.AND_TYPE;
            case YTask._OR  : return Decorator.OR_TYPE;
            case YTask._XOR : return Decorator.XOR_TYPE;
        }
        return Decorator.AND_TYPE;
    }

}