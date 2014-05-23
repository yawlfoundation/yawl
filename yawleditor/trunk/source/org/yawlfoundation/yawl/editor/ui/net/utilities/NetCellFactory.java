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

package org.yawlfoundation.yawl.editor.ui.net.utilities;

import org.yawlfoundation.yawl.editor.core.controlflow.YCompoundFlow;
import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandler;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YCondition;
import org.yawlfoundation.yawl.elements.YTask;

import java.awt.geom.Point2D;

/**
 * A Factory class, responsible for creating new net elements and inserting
 * them into a specified net.
 */

public class NetCellFactory {

    /**
     * Creates a new condition and inserts at specified point of the given net.
     * A reference to the newly created condition is returned.
     * @param net The net to insert the condition into.
     * @param point The point on the net canvas to insert the condition at.
     * @return a reference to the newly created condition.
     */

    public static Condition insertCondition(NetGraph net, Point2D point) {
        YCondition yCondition = getHandler().addCondition(net.getName(), "C");
        Condition newCondition = new Condition(point, yCondition);
        net.addElement(newCondition);
        return newCondition;
    }

    /**
     * Creates a new atomic task and inserts at the specified point of the given net.
     * A reference to the newly created task is returned.
     * @param net The net to insert the task into.
     * @param point The point on the net canvas to insert the task at.
     * @return a reference to the newly created task.
     */
    public static AtomicTask insertAtomicTask(NetGraph net, Point2D point) {
        YTask yTask = getHandler().addAtomicTask(net.getName(), "T");
        AtomicTask newTask = new AtomicTask(point, yTask);
        net.addElement(newTask);
        return newTask;
    }


    /**
     * Creates a new multiple atomic task and inserts at the specified point of the given net.
     * A reference to the newly created task is returned.
     * @param net The net to insert the task into.
     * @param point The point on the net canvas to insert the task at.
     * @return a reference to the newly created task.
     */
    public static MultipleAtomicTask insertMultipleAtomicTask(NetGraph net, Point2D point) {
        YTask yTask = getHandler().addMultipleInstanceAtomicTask(net.getName(), "T");
        MultipleAtomicTask newTask = new MultipleAtomicTask(point, yTask);
        net.addElement(newTask);
        return newTask;
    }


    /**
     * Creates a new composite task and inserts at the specified point of the given net.
     * A reference to the newly created task is returned.
     * @param net The net to insert the task into.
     * @param point The point on the net canvas to insert the task at.
     * @return a reference to the newly created task.
     */
    public static CompositeTask insertCompositeTask(NetGraph net, Point2D point) {
        YTask yTask = getHandler().addCompositeTask(net.getName(), "T");
        CompositeTask newTask = new CompositeTask(point, yTask);
        net.addElement(newTask);
        return newTask;
    }

    /**
     * Creates a new multiple composite task and inserts at the specified point of the given net.
     * A reference to the newly created task is returned.
     * @param net The net to insert the task into.
     * @param point The point on the net canvas to insert the task at.
     * @return a reference to the newly created task.
     */
    public static MultipleCompositeTask insertMultipleCompositeTask(NetGraph net, Point2D point) {
        YTask yTask = getHandler().addMultipleInstanceCompositeTask(net.getName(), "T");
        MultipleCompositeTask newTask = new MultipleCompositeTask(point, yTask);
        net.addElement(newTask);
        return newTask;
    }


    public static YAWLFlowRelation insertFlow(NetGraph net, String sourceID,
                                              String targetID) {
        YCompoundFlow yFlow = getHandler().addFlow(net.getName(), sourceID, targetID);
        return new YAWLFlowRelation(yFlow);
    }


    private static YControlFlowHandler getHandler() {
        return SpecificationModel.getHandler().getControlFlowHandler();
    }
}
