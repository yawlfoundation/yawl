package au.edu.qut.yawl.editor.net.utilities;

import java.awt.geom.Point2D;

import au.edu.qut.yawl.editor.elements.model.AtomicTask;
import au.edu.qut.yawl.editor.elements.model.CompositeTask;
import au.edu.qut.yawl.editor.elements.model.Condition;
import au.edu.qut.yawl.editor.elements.model.MultipleAtomicTask;
import au.edu.qut.yawl.editor.elements.model.MultipleCompositeTask;
import au.edu.qut.yawl.editor.net.NetGraph;

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
    Condition newCondition = new Condition(point);
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
    AtomicTask newTask = new AtomicTask(point);
    net.addElement(newTask);
    return newTask;
  }

  /**
   * Creates a new atomic task that renders with the specified icon 
   * and inserts at the specified point of the given net.
   * A reference to the newly created task is returned.
   * @param net The net to insert the task into.
   * @param point The point on the net canvas to insert the task at.
   * @param iconPath The filepath to the icon that the task is to render with.
   * @return a reference to the newly created task.
   */
  public static AtomicTask insertAtomicTask(NetGraph net, Point2D point, String iconPath) {
    AtomicTask newTask = new AtomicTask(point, iconPath);
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
    MultipleAtomicTask newTask = new MultipleAtomicTask(point);
    net.addElement(newTask);
    return newTask;
  }

  /**
   * Creates a new multiple atomic task that renders with the specified icon 
   * and inserts at the specified point of the given net.
   * A reference to the newly created task is returned.
   * @param net The net to insert the task into.
   * @param point The point on the net canvas to insert the task at.
   * @param iconPath The filepath to the icon that the task is to render with.
   * @return a reference to the newly created task.
   */
  public static MultipleAtomicTask insertMultipleAtomicTask(NetGraph net, Point2D point, String iconPath) {
    MultipleAtomicTask newTask = new MultipleAtomicTask(point, iconPath);
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
    CompositeTask newTask = new CompositeTask(point);
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
    MultipleCompositeTask newTask = new MultipleCompositeTask(point);
    net.addElement(newTask);
    return newTask;
  }
}
