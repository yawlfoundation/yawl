package org.yawlfoundation.yawl.editor.core.resourcing.validation;

import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;

/**
 * Author: Michael Adams
 */
public abstract class InvalidReference {

    YNet _net;
    YTask _task;

    protected InvalidReference() { }

    protected InvalidReference(YNet net, YTask task) {
        _net = net;
        _task = task;
    }


    protected String getMessage(String name, String type) {
        StringBuilder msg = new StringBuilder("Task '");
        msg.append(_task.getID())
           .append("' in Net '")
           .append(_net.getID())
           .append("' references a ")
           .append(type)
           .append(" '")
           .append(name)
           .append("' that does not exist in the organisational data supplied")
           .append(" by the resource service. The reference has been removed.");

       return msg.toString();
    }

}
