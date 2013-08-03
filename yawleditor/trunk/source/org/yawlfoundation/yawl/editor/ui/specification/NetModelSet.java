package org.yawlfoundation.yawl.editor.ui.specification;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLCompositeTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetCellUtilities;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.ui.properties.PropertiesLoader;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationState;
import org.yawlfoundation.yawl.editor.ui.swing.undo.*;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Michael Adams
 * @date 30/07/13
 */
public class NetModelSet extends HashSet<NetGraphModel> {

    NetGraphModel rootNet;
    Publisher publisher;
    PropertiesLoader propertiesLoader;

    public NetModelSet(PropertiesLoader loader) {
        super();
        propertiesLoader = loader;
        publisher = Publisher.getInstance();
    }


    public NetGraphModel getRootNet() { return rootNet; }

    public Set<NetGraphModel> getSubNets() {
        Set<NetGraphModel> subNets = new HashSet<NetGraphModel>(this);
        subNets.remove(rootNet);
        return subNets;
    }

    public NetGraphModel get(String id) {
        for (NetGraphModel net : this) {
            if (net.getName().equals(id)) return net ;
        }
        return null;
    }

    public Set<NetGraphModel> getSortedNets() {
        return new TreeSet<NetGraphModel>(this);
    }

    public boolean setRootNet(String name) {
        return setRootNet(getNetModelFromName(name));
    }

    public boolean setRootNet(NetGraphModel newRootNet) {
        if (newRootNet != null) {
            NetGraphModel oldRootNet = rootNet;
            rootNet = newRootNet;
            oldRootNet.setIsRootNet(false);
            newRootNet.setIsRootNet(true);
            startEdits(null);
            newRootNet.postEdit(new UndoableStartingNetChange(newRootNet, oldRootNet));
            stopEdits();
            publisher.publishState(SpecificationState.NetDetailChanged);
        }
        return newRootNet != null;
    }

    public boolean add(NetGraphModel netModel) {
        startEdits(netModel);
        boolean added = addNoUndo(netModel);
        if (added && rootNet != null) {        // can be null on specification load
            rootNet.postEdit(new UndoableNetAddition(netModel));
        }
        stopEdits();
        return added;
    }

    public boolean addNoUndo(NetGraphModel netModel) {
        if (isEmpty()) {
            netModel.setIsRootNet(true);
            rootNet = netModel;
        }
        boolean added = super.add(netModel);
        if (added) {
            propertiesLoader.setGraph(netModel.getGraph());
            publisher.publishAddNetEvent();
        }
        return added;
    }


    public boolean remove(NetGraphModel netModel) {
        boolean removed = removeNoUndo(netModel);
        if (removed) {
            startEdits(netModel);
            Set<YAWLCompositeTask> changedTasks = resetUnfoldingCompositeTasks(netModel);
            NetGraphModel newStartingNet = selectAnotherStartingNet(netModel);
            rootNet.postEdit(
                    new UndoableNetDeletion(this, netModel, newStartingNet, changedTasks));
            stopEdits();
        }
        return removed;
    }

    public boolean removeNoUndo(NetGraphModel netModel) {
        boolean success = super.remove(netModel);
        if (success) {
            publisher.publishRemoveNetEvent(isEmpty());
        }
        return success;
    }


    private NetGraphModel selectAnotherStartingNet(NetGraphModel netModel) {
        if (! isEmpty() && netModel.isRootNet()) {
            netModel.setIsRootNet(false);
            for (NetGraphModel randomNet : this) {               // pick one at random
                randomNet.setIsRootNet(true);
                return randomNet;
            }
        }
        return null;                                    // no nets left to select from
    }


    public Set<YAWLCompositeTask> resetUnfoldingCompositeTasks(NetGraphModel netModel) {
        Set<YAWLCompositeTask> changedTasks = new HashSet<YAWLCompositeTask>();
        for (NetGraphModel net: this) {
            for (YAWLCompositeTask compositeTask : NetUtilities.getCompositeTasks(net)) {
                if (decomposesTo(compositeTask, netModel)) {
                    net.getGraph().setUnfoldingNet(compositeTask, null);
                    changedTasks.add(compositeTask);
                }
            }
        }
        return changedTasks;
    }


    public NetGraphModel getNetModelFromName(String name) {
        if (! StringUtil.isNullOrEmpty(name)) {
            for (NetGraphModel net: this) {
                if (net.getName().equals(name)) {
                    return net;
                }
            }
        }
        return null;
    }


    public void propagateDecompositionNameChange(YDecomposition decomposition,
                                                 String oldLabel) {
        startEdits(getRootNet());
        NetGraphModel lastNetModel = null;
        for (NetGraphModel netModel : this) {
            NetGraph net = netModel.getGraph();

            if (netModel.getDecomposition().equals(decomposition)) {
                net.getFrame().setTitle(decomposition.getID());
                netModel.postEdit(
                        new UndoableNetFrameTitleChange(
                                net.getFrame(),
                                oldLabel,
                                netModel.getName()
                        )
                );
            }
            lastNetModel = netModel;
        }

        // post the decomposition edit to the last net.
        if (lastNetModel != null) {
            lastNetModel.postEdit(
                    new UndoableDecompositionLabelChange(
                            decomposition,
                            oldLabel,
                            decomposition.getID()
                    )
            );
        }

        stopEdits();
        publisher.publishState(SpecificationState.NetDetailChanged);
    }


    public void propagateSpecificationFontSize(int oldSize, int newSize) {
        startEdits(getRootNet());
        NetGraphModel lastNetModel = null;
        for (NetGraphModel netModel : this) {
            NetCellUtilities.propogateFontChangeAcrossNet(
                    netModel.getGraph(),
                    netModel.getGraph().getFont().deriveFont((float) newSize)
            );
            lastNetModel = netModel;
        }

        // post the font size edit to the last net.
        if (lastNetModel != null) {
            lastNetModel.postEdit(new UndoableFontSizeChange(oldSize, newSize));
        }
       stopEdits();
    }


    private boolean decomposesTo(YAWLCompositeTask compositeTask, NetGraphModel netModel) {
        YDecomposition decomposition = compositeTask.getDecomposition();
        return decomposition != null &&
                decomposition.equals(netModel.getDecomposition());
    }

    private void startEdits(NetGraphModel netModel) {
        SpecificationUndoManager.getInstance().startCompoundingEdits(netModel);
    }

    private void stopEdits() {
        SpecificationUndoManager.getInstance().stopCompoundingEdits();
    }

}
