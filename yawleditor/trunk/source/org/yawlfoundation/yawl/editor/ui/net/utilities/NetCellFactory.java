package org.yawlfoundation.yawl.editor.ui.net.utilities;

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
 * @author Lindsay Bradford
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
        YCondition yCondition = getHandler().addCondition(net.getName(), "Condition");
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
        YTask yTask = getHandler().addAtomicTask(net.getName(), "AtomicTask");
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
        YTask yTask = getHandler().addMultipleInstanceAtomicTask(net.getName(),
                "MultipleAtomicTask");
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
        YTask yTask = getHandler().addCompositeTask(net.getName(), "CompositeTask");
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
        YTask yTask = getHandler().addMultipleInstanceCompositeTask(net.getName(),
                "MultipleCompositeTask");
        MultipleCompositeTask newTask = new MultipleCompositeTask(point, yTask);
        net.addElement(newTask);
        return newTask;
    }


    private static YControlFlowHandler getHandler() {
        return SpecificationModel.getHandler().getControlFlowHandler();
    }
}
