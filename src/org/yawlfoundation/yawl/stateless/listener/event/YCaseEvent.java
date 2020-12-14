package org.yawlfoundation.yawl.stateless.listener.event;

import org.jdom2.Document;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.stateless.elements.YSpecification;
import org.yawlfoundation.yawl.stateless.elements.YTask;
import org.yawlfoundation.yawl.stateless.elements.marking.YIdentifier;

import java.util.Set;

/**
 * @author Michael Adams
 * @date 24/8/20
 */
public class YCaseEvent extends YEvent {

    private Set<YTask> _deadlockedTasks;


    public YCaseEvent(YEventType eType, YIdentifier id) {
        super(eType, id);
    }

    public YCaseEvent(YEventType eType, YIdentifier id, YSpecification spec) {
        this(eType, id, spec.getSpecificationID());
    }

    public YCaseEvent(YEventType eType, YIdentifier id, YSpecificationID specID) {
        super(eType, id);
        setSpecID(specID);
    }

    public YCaseEvent(YEventType eType, YIdentifier id, Document dataDoc) {
        super(eType, id);
        setData(dataDoc);
     }

    public YCaseEvent(YEventType eType, YIdentifier id, Set<YTask> deadlockedTasks) {
        super(eType, id);
        setDeadlockedTasks(deadlockedTasks);
     }


    public void setDeadlockedTasks(Set<YTask> tasks) { _deadlockedTasks = tasks; }

    public Set<YTask> getDeadlockedTasks() { return _deadlockedTasks; }

}
