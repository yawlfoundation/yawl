package org.yawlfoundation.yawl.editor.ui.resourcing;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;

/**
 * Author: Michael Adams
 * Creation Date: 18/07/2008
 */
public class InvalidResourceReference {

    public enum ResType { Participant, Role }

    NetGraphModel _net;
    YAWLTask _task;
    ResType _type;
    Participant _participant;
    Role _role;

    public InvalidResourceReference() {}

    public InvalidResourceReference(NetGraphModel net, YAWLTask task,
                                    Participant participant) {
        _net = net;
        _task = task;
        _participant = participant;
        _role = null;
        _type = ResType.Participant;
    }

    public InvalidResourceReference(NetGraphModel net, YAWLTask task, Role role) {
        _net = net;
        _task = task;
        _participant = null;
        _role = role;
        _type = ResType.Role;
    }

    public String getMessage() {
        String name = (_type == ResType.Participant) ? _participant.getName() :
                                                       _role.getName();
        StringBuilder msg = new StringBuilder("Task '");
        msg.append(_task.getEngineLabel())
           .append("' in Net '")
           .append(_net.getName())
           .append("' references a ")
           .append(_type.name())
           .append(" named '")
           .append(name)
           .append("' that does not exist in the organisational data supplied")
           .append(" by the resource service. The reference has been removed.");

       return msg.toString();
    }

    public void removeFromDistributionList() {
        ResourceMapping mapping = _task.getResourceMapping();
        if (_type == ResType.Participant)
            mapping.getBaseUserDistributionList().remove(_participant);
        else
            mapping.getBaseRoleDistributionList().remove(_role);

    }
}
