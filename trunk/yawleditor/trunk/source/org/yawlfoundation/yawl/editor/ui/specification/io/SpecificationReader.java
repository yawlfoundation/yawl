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

package org.yawlfoundation.yawl.editor.ui.specification.io;

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.core.controlflow.YCompoundFlow;
import org.yawlfoundation.yawl.editor.core.layout.YLayout;
import org.yawlfoundation.yawl.editor.core.resourcing.YResourceHandler;
import org.yawlfoundation.yawl.editor.core.resourcing.validation.InvalidReference;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.CancellationSet;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.ui.plugin.YPluginHandler;
import org.yawlfoundation.yawl.editor.ui.specification.DecoratorUtil;
import org.yawlfoundation.yawl.editor.ui.specification.InvalidResourceReferencesDialog;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.DefaultLayoutArranger;
import org.yawlfoundation.yawl.elements.*;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SpecificationReader extends SwingWorker<Boolean, Void> {

    private static final Point DEFAULT_LOCATION = new Point(100,100);

    private final YSpecificationHandler _handler;
    private final Map<YExternalNetElement, YAWLVertex> _elementMap;

    private String _fileName;
    private String _specXML;
    private String _layoutXML;


    private SpecificationReader() {
        _handler = SpecificationModel.getHandler();
        _elementMap = new HashMap<YExternalNetElement, YAWLVertex>();
    }

    public SpecificationReader(String fileName) {
        this();
        _fileName = fileName;
    }

    public SpecificationReader(String specXML, String layoutXML) {
        this();
        _specXML = specXML;
        _layoutXML = layoutXML;
    }


    public Boolean doInBackground() {
        Publisher.getInstance().publishOpenFileEvent();
        return _fileName != null ? load(_fileName) : load(_specXML, _layoutXML);
    }


    protected void done() {
        try {
            finaliseLoad(get());
        }
        catch (Exception e) {
            finaliseLoad(false);
        }
    }

    /*****************************************************************************/

    private boolean load(String fileName) {
        return populate(loadFile(fileName));
    }


    private boolean load(String specXML, String layoutXML) {
        return populate(loadFromXML(specXML, layoutXML));
    }


    private boolean populate(boolean loaded) {
        if (loaded) {
            createEditorObjects();
            layoutElements();
            rationaliseIdentifiers();
        }
        return loaded;
    }

    private boolean loadFromXML(String specXML, String layoutXML) {
        try {
            preLoad();
            _handler.load(specXML, layoutXML);
        }
        catch (Exception e) {
            showLoadError(e.getMessage());
            return false;
        }
        return true;
    }


    private boolean loadFile(String fileName) {
        try {
            preLoad();
            _handler.load(fileName);
        }
        catch (Exception e) {
            showLoadError(e.getMessage());
            return false;
        }
        return true;
    }


    private boolean layoutElements() {
        return layoutElements(_handler.getLayout());
    }

    private boolean layoutElements(YLayout layout) {
        if (layout != null && layout.hasNets()) {
            LayoutImporter.importAndApply(layout);
            return true;
        }

        // layout has no information, revert to default layout
        removeUnnecessaryDecorators();
        new DefaultLayoutArranger().layoutSpecification();
        return false;
    }


    private void preLoad() {
        SpecificationModel.reset();
        YPluginHandler.getInstance().preOpenFile();
    }


    private void finaliseLoad(boolean loaded) {
        SpecificationUndoManager.getInstance().discardAllEdits();
        if (loaded) {
            Publisher.getInstance().publishOpenFileEvent();
            YAWLEditor.getNetsPane().setSelectedIndex(0);           // root net
            setSelectedCancellationSets();
            YPluginHandler.getInstance().postOpenFile();
            YAWLEditor.getPropertySheet().setVisible(true);
            warnOnInvalidResources();
        }
        else {
            Publisher.getInstance().publishCloseFileEvent();
        }
    }


    private void rationaliseIdentifiers() {
        Map<String, String> changes =
                _handler.getControlFlowHandler().rationaliseIdentifiers();
        if (! changes.isEmpty()) {
            _handler.getResourceHandler().updateRationalisedReferences(changes);
        }
    }


    private void createEditorObjects() {
        YAWLEditor.getPropertySheet().setVisible(false);
        importNets();
        populateNets();
    }


    private void importNets() {
        YNet rootNet = _handler.getControlFlowHandler().getRootNet();
        importNet(rootNet, true);

        // import sub-nets
        for (YNet net : _handler.getControlFlowHandler().getNets()) {
            if (! net.equals(rootNet)) {
                importNet(net, false);
            }
        }
    }


    private void importNet(YNet yNet, boolean isRoot) {
        NetGraph netGraph = new NetGraph(yNet);
        SpecificationModel.getNets().add(netGraph.getNetModel(), isRoot);
        YAWLEditor.getNetsPane().openNet(netGraph);
    }


    private void populateNets() {
        for (NetGraphModel netModel : SpecificationModel.getNets()) {
            populateNet(netModel);
        }
    }


    private void populateNet(NetGraphModel netModel) {
        YNet yNet = (YNet) netModel.getDecomposition();
        Set<YFlow> flows = new HashSet<YFlow>();
        Set<YCondition> implicitConditions = new HashSet<YCondition>();
        Set<YTask> cancellationTasks = new HashSet<YTask>();
        for (YExternalNetElement netElement : yNet.getNetElements().values()) {
            if (netElement instanceof YInputCondition) {
                addInputCondition((YInputCondition) netElement, netModel);
            }
            else if (netElement instanceof YOutputCondition) {
                addOutputCondition((YOutputCondition) netElement, netModel);
            }
            else if (netElement instanceof YTask) {
                YTask task = (YTask) netElement;
                addTask(task, netModel);
                if (! task.getRemoveSet().isEmpty()) {
                    cancellationTasks.add(task);
                }
            }
            else if (netElement instanceof YCondition) {
                YCondition condition = (YCondition) netElement;
                if (condition.isImplicit()) {
                    implicitConditions.add(condition);
                }
                else {
                    addCondition(condition, netModel);
                }
            }
            flows.addAll(netElement.getPostsetFlows());
        }
        Set<YAWLFlowRelation> editorFlows =
                addFlows(flows, implicitConditions, netModel.getGraph());
        addCancellationSets(cancellationTasks, editorFlows);
    }


    private void addInputCondition(YInputCondition yInputCondition,
                                   NetGraphModel netModel) {
        InputCondition inputCondition = new InputCondition(DEFAULT_LOCATION,
                yInputCondition);
        addElement(netModel.getGraph(), inputCondition);
        _elementMap.put(yInputCondition, inputCondition);
    }

    private void addOutputCondition(YOutputCondition yOutputCondition,
                                    NetGraphModel netModel) {
        OutputCondition outputCondition = new OutputCondition(DEFAULT_LOCATION,
                yOutputCondition);
        addElement(netModel.getGraph(), outputCondition);
        _elementMap.put(yOutputCondition, outputCondition);
    }


    private void addTask(YTask yTask, NetGraphModel netModel) {
        YAWLTask editorTask = createEditorTask(yTask);
        addElement(netModel.getGraph(), editorTask);
        DecoratorUtil.setTaskDecorators(yTask, editorTask, netModel);
        _elementMap.put(yTask, editorTask);
    }


    private YAWLTask createEditorTask(YTask engineTask) {
        if (engineTask instanceof YAtomicTask) {
            return engineTask.isMultiInstance() ?
                    new MultipleAtomicTask(DEFAULT_LOCATION, engineTask) :
                    new AtomicTask(DEFAULT_LOCATION, engineTask);
        }
        if (engineTask instanceof YCompositeTask) {
            return engineTask.isMultiInstance() ?
                    new MultipleCompositeTask(DEFAULT_LOCATION, engineTask) :
                    new CompositeTask(DEFAULT_LOCATION, engineTask);
        }
        return null;   // won't be reached, will always match one of the above
    }


    private void addElement(NetGraph graph, YAWLVertex vertex) {
        graph.addElement(vertex);
        String label = vertex.getName();
        if (label != null) graph.setElementLabel(vertex, label);
    }


    private void addCondition(YCondition engineCondition, NetGraphModel editorNet) {
        Condition editorCondition = new Condition(DEFAULT_LOCATION, engineCondition);
        addElement(editorNet.getGraph(), editorCondition);
        _elementMap.put(engineCondition, editorCondition);
    }


    private Set<YAWLFlowRelation> addFlows(Set<YFlow> flows,
                                           Set<YCondition> implicitConditions,
                                           NetGraph netGraph) {
        Set<YAWLFlowRelation> editorFlows = new HashSet<YAWLFlowRelation>();
        for (YCompoundFlow engineFlow : NetUtilities.rationaliseFlows(flows, implicitConditions)) {
            YAWLVertex sourceVertex = _elementMap.get(engineFlow.getSource());
            YAWLVertex targetVertex = _elementMap.get(engineFlow.getTarget());
            YAWLFlowRelation flow = new YAWLFlowRelation(engineFlow);
            netGraph.connect(flow, sourceVertex, targetVertex);
            editorFlows.add(flow);
        }
        return editorFlows;
    }


    private void addCancellationSets(Set<YTask> cancellationTasks,
                                     Set<YAWLFlowRelation> editorFlows) {
        for (YTask engineTask : cancellationTasks) {
            YAWLTask editorTask = (YAWLTask) _elementMap.get(engineTask);
            CancellationSet cancellationSet = new CancellationSet(editorTask);

            for (YExternalNetElement engineSetMember : engineTask.getRemoveSet()) {
                if (engineSetMember instanceof YCondition &&
                        ((YCondition) engineSetMember).isImplicit()) {
                    cancellationSet.add(getFlow(editorFlows, (YCondition) engineSetMember));
                }
                else {
                    cancellationSet.add(_elementMap.get(engineSetMember));
                }
            }
            editorTask.setCancellationSet(cancellationSet);
        }
    }


    private void setSelectedCancellationSets() {
        for (NetGraphModel graphModel : SpecificationModel.getNets()) {
            NetGraph graph = graphModel.getGraph();
            YAWLTask selectedCancellationTask =
                     graph.getCancellationSetModel().getTriggeringTask();
            if (selectedCancellationTask != null) {
                VertexContainer container =
                        (VertexContainer) selectedCancellationTask.getParent();
                if (container != null) {
                    graph.getSelectionModel().setSelectionCell(container);
                }
                else {
                    graph.getSelectionModel().setSelectionCell(selectedCancellationTask);
                }
            }
        }
    }


    private YAWLFlowRelation getFlow(Set<YAWLFlowRelation> flows, YCondition condition) {
        YExternalNetElement source = condition.getPresetElements().iterator().next();
        YExternalNetElement target = condition.getPostsetElements().iterator().next();
        for (YAWLFlowRelation flow : flows) {
            if (flow.getSourceVertex().getYAWLElement().equals(source) &&
                flow.getTargetVertex().getYAWLElement().equals(target)) {
                return flow;
            }
        }
        return null;
    }


    private void removeUnnecessaryDecorators() {
        for (NetGraphModel net : SpecificationModel.getNets())
            DecoratorUtil.removeUnnecessaryDecorators(net);
    }


    private void showLoadError(String errorMsg) {
        JOptionPane.showMessageDialog(YAWLEditor.getInstance(),
                "Failed to load specification.\n" +
                (errorMsg.length() > 0 ? "Reason: " + errorMsg : ""),
                "Specification File Load Error",
                JOptionPane.ERROR_MESSAGE);
    }


    private void warnOnInvalidResources() {
        YResourceHandler resHandler = _handler.getResourceHandler();
        if (! resHandler.hasLoadedResources()) return;

        YAWLEditor.getStatusBar().freeze();
        if (YConnector.isResourceConnected()) {
            Set<InvalidReference> invalidSet = resHandler.getInvalidReferences();
            if (! invalidSet.isEmpty()) {
                new InvalidResourceReferencesDialog(invalidSet).setVisible(true);
            }
        }
        else if (warnOnDisconnectedResourceService() == JOptionPane.YES_OPTION) {

            // full package name required to differentiate from core's FileOperations
            org.yawlfoundation.yawl.editor.ui.specification.FileOperations.close();
        }
    }


    private int warnOnDisconnectedResourceService() {
        Object[] buttonText = {"Close", "Continue"};
        return JOptionPane.showOptionDialog(
                YAWLEditor.getInstance(),
                "The loaded specification contains resource settings, but the resource\n " +
                "service is currently offline. This means that the settings cannot be\n "+
                "validated and will be LOST if the specification is saved. It is\n " +
                "suggested that the specification be closed, a valid connection to\n " +
                "the resource service be established (via the Properties menu item),\n " +
                "then the specification be reloaded.\n\n" +
                "Click the 'Close' button to close the loaded file (recommended)\n " +
                "or the 'Continue' button to keep it loaded, but with resourcing\n " +
                "settings discarded.",
                "Warning - read carefully",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                buttonText,
                buttonText[0]);
    }

}