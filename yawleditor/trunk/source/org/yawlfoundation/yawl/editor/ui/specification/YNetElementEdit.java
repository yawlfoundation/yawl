/*
 * Copyright (c) 2004-2015 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.specification;

import org.yawlfoundation.yawl.editor.core.controlflow.YCompoundFlow;
import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandler;
import org.yawlfoundation.yawl.editor.core.identity.ElementIdentifier;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.elements.*;

import java.util.*;

/**
 * Applies undos & redos and cut & pastes of net elements and flows to the external
 * YSpecification object.
 *
 * @author Michael Adams
 * @date 26/05/15
 */
public class YNetElementEdit {

    private static final YControlFlowHandler handler =
            SpecificationModel.getHandler().getControlFlowHandler();


    public static void apply(Object[] toBeInserted, Object[] toBeRemoved) {
        if (! nullOrEmptyArray(toBeInserted)) {
            insertExternal(null, toBeInserted);
        }
        if (! nullOrEmptyArray(toBeRemoved)) {
            removeExternal(toBeRemoved);
        }
    }


    public static void delete(Object[] toBeDeleted) { cut(toBeDeleted); }


    public static void cut(Object[] toBeCut) {
        apply(null, toBeCut);
    }


    public static Map<String, String> paste(String netId, Object[] toBePasted) {
        YNet net = handler.getNet(netId);
        return net != null ? insertExternal(net, toBePasted) :
                Collections.<String, String>emptyMap();
    }


    private static Map<String, String> insertExternal(YNet net, Object[] elements) {

        // elements first, then flows
        Map<String, String> idMap = insertNetElements(net, elements);    // updated ids
        insertFlows(net, elements, idMap);
        return idMap;
    }


    private static Map<String, String> insertNetElements(YNet net, Object[] elements) {
        Map<String, String> idMap = new HashMap<String, String>();      // updated ids
        for (Object o : elements) {
            if (o instanceof VertexContainer) continue;           // ignore containers
            YExternalNetElement netElement = getNetElement(o);
            if (netElement != null) {
                String oldId = netElement.getID();
                if (net == null || pastingBack(net, netElement)) {       // pasting
                    netElement = handler.addNetElement(netElement);
                }
                else {
                    netElement = pasteNetElement(net, netElement, (YAWLVertex) o);
                }
                if (netElement != null) {                           // ! already in net?
                    idMap.put(oldId, netElement.getID());
                }
            }
        }
        return idMap;
    }



    private static void insertFlows(YNet net, Object[] elements,
                                    Map<String, String> idMap) {
        Set<YTask> affectedSources = new HashSet<YTask>();
        for (Object o : elements) {
            if (o instanceof YAWLFlowRelation) {
                YAWLFlowRelation flow = (YAWLFlowRelation) o;
                YCompoundFlow oldFlow = flow.getYFlow();
                String netId = net != null ? net.getID() : oldFlow.getNet().getID();
                String sourceId = lookup(idMap, flow.getSourceID());
                String targetId = lookup(idMap, flow.getTargetID());
                if (elementExists(netId, sourceId) && elementExists(netId, targetId)) {
                    YCompoundFlow newFlow = handler.addFlow(netId, sourceId, targetId);
                    newFlow.setDocumentation(oldFlow.getDocumentation());
                    if ((newFlow.getSource() instanceof YTask &&
                            ((YTask) newFlow.getSource()).getSplitType() != YTask._AND)) {
                        newFlow.setPredicate(oldFlow.getPredicate());
                        newFlow.setOrdering(oldFlow.getOrdering());
                        newFlow.setIsDefaultFlow(oldFlow.isDefaultFlow());
                        affectedSources.add((YTask) newFlow.getSource());
                    }
                    flow.setYFlow(newFlow);
                }
            }
        }
        if (! affectedSources.isEmpty()) rationaliseFlowOrders(affectedSources);
    }


    private static void rationaliseFlowOrders(Set<YTask> affectedSources) {
        for (YTask source : affectedSources) {
            int ordering = 0;
            int split = source.getSplitType();
            Set<YFlow> defaultFlows = new HashSet<YFlow>();
            YFlow lastFlow = null;
            List<YFlow> flowList = new ArrayList<YFlow>(source.getPostsetFlows());
            Collections.sort(flowList, new FlowOrderComparator());
            for (YFlow flow : flowList) {
                if (split == YTask._XOR) flow.setEvalOrdering(ordering++);
                if (flow.isDefaultFlow()) defaultFlows.add(flow);
                lastFlow = flow;
            }
            if (! (lastFlow == null || defaultFlows.size() == 1)) {
                for (YFlow flow : defaultFlows) {
                    flow.setIsDefaultFlow(false);
                }
                if (split == YTask._XOR) lastFlow.setXpathPredicate(null);
                lastFlow.setIsDefaultFlow(true);
            }
        }
    }


    private static YExternalNetElement pasteNetElement(YNet net,
                                    YExternalNetElement element, YAWLVertex vertex) {
        String netId = net.getID();
        String elementId = new ElementIdentifier(element.getID()).getName();
        YExternalNetElement newElement = null;
        if (vertex instanceof Condition) {
            newElement = handler.addCondition(netId, elementId);
        }
        if (vertex instanceof AtomicTask) {
            newElement = handler.addAtomicTask(netId, elementId);
        }
        else if (vertex instanceof MultipleAtomicTask) {
            newElement = handler.addMultipleInstanceAtomicTask(netId, elementId);
        }
        else if (vertex instanceof CompositeTask) {
            newElement = handler.addCompositeTask(netId, elementId);
        }
        else if (vertex instanceof MultipleCompositeTask) {
            newElement = handler.addMultipleInstanceCompositeTask(netId, elementId);
        }
        if (newElement != null) {
            transferProperties(element, newElement);
            vertex.setYAWLElement(newElement);
            if (vertex instanceof YAWLTask) {
                YAWLTask task = (YAWLTask) vertex;
                Decorator split = task.getSplitDecorator();
                if (split != null) split.setTask(task);
                Decorator join = task.getJoinDecorator();
                if (join != null) join.setTask(task);
            }
        }
        return newElement;
    }


    private static void transferProperties(YExternalNetElement oldElement,
                                           YExternalNetElement newElement) {
        if (! (newElement instanceof YTask)) return;
        YTask oldTask = (YTask) oldElement;
        YTask newTask = (YTask) newElement;
        newTask.setDocumentation(oldTask.getDocumentation());
        newTask.setCustomFormURI(oldTask.getCustomFormURL());
        newTask.setDecompositionPrototype(oldTask.getDecompositionPrototype());
        newTask.setDataMappingsForTaskStarting(oldTask.getDataMappingsForTaskStarting());
        newTask.setDataMappingsForTaskCompletion(oldTask.getDataMappingsForTaskCompletion());
        newTask.setJoinType(oldTask.getJoinType());
        newTask.setSplitType(oldTask.getSplitType());
        newTask.setResourcingSpecs(oldTask.getResourcingSpecs());
        newTask.setTimerParameters(oldTask.getTimerParameters());
        if (oldTask.isMultiInstance()) {
            YMultiInstanceAttributes oldMIs = oldTask.getMultiInstanceAttributes();
            newTask.setUpMultipleInstanceAttributes(oldMIs.getMinInstancesQuery(),
                    oldMIs.getMaxInstancesQuery(), oldMIs.getThresholdQuery(),
                    oldMIs.getCreationMode());
            YMultiInstanceAttributes newMIs = newTask.getMultiInstanceAttributes();
            newMIs.setMIFormalInputParam(oldMIs.getMIFormalInputParam());
            newMIs.setMIFormalOutputQuery(oldMIs.getMIFormalOutputQuery());
            newMIs.setUniqueInputMISplittingQuery(oldMIs.getMISplittingQuery());
            newMIs.setUniqueOutputMIJoiningQuery(oldMIs.getMIFormalOutputQuery());
        }
    }


    // pasting an element back to its same net, and it hasn't already been pasted back
    private static boolean pastingBack(YNet net, YExternalNetElement element) {
        return net.equals(element.getNet()) && net.getNetElement(element.getID()) == null;
    }


    private static String lookup(Map<String, String> map, String id) {
        String mapValue = map.get(id);
        return mapValue != null ? mapValue : id;
    }


    private static String getNetId(YAWLFlowRelation flow) {
        return flow.getYFlow().getNet().getID();
    }


    private static YExternalNetElement getNetElement(Object o) {
        YAWLVertex vertex = null;
        if (o instanceof VertexContainer) {
            vertex = ((VertexContainer) o).getVertex();
        }
        else if (o instanceof YAWLVertex) {
            vertex = (YAWLVertex) o;
        }
        return vertex != null ? vertex.getYAWLElement() : null;
    }


    private static boolean nullOrEmptyArray(Object[] array) {
        return array == null || array.length == 0;
    }

    private static boolean elementExists(String netId, String elementId) {
        return handler.getNetElement(netId, elementId) != null;
    }


    private static void removeExternal(Object[] elements) {

        // first the flows
        for (Object o : elements) {
            if (o instanceof YAWLFlowRelation) {
                YAWLFlowRelation flow = (YAWLFlowRelation) o;
                handler.removeFlow(getNetId(flow), flow.getSourceID(),
                        flow.getTargetID());
            }
        }

        // then the net elements
        for (Object o : elements) {
            YExternalNetElement netElement = getNetElement(o);
            if (netElement != null) {
                String id = netElement.getID();
                String netId = netElement.getNet().getID();
                if (netElement instanceof YCondition) {
                    handler.removeCondition(netId, id);
                }
                else if (netElement instanceof YCompositeTask) {
                    handler.removeCompositeTask(netId, id);
                }
                else if (netElement instanceof YAtomicTask) {
                    handler.removeAtomicTask(netId, id);
                }
            }
        }
    }


    /****************************************************************************/

    static class FlowOrderComparator implements Comparator<YFlow> {
        @Override
        public int compare(YFlow f1, YFlow f2) {
            if (f1 == null && f2 == null) return 0;
            if (f1 == null) return -1;
            if (f2 == null) return 1;
            return getOrdering(f1) - getOrdering(f2);
        }


        // a flow with no ordering can only be a default XOR flow, so must come last
        private int getOrdering(YFlow f) {
            Integer i = f.getEvalOrdering();
            return i != null ? i : Integer.MAX_VALUE;
        }
    }
}


