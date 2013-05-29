package org.yawlfoundation.yawl.editor.core.resourcing.validation;

import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;

/**
 * @author Michael Adams
 * @date 25/06/12
 */
public class InvalidParticipantReference extends InvalidReference {

    private String _participantID;

    public InvalidParticipantReference(YNet net, YTask task, String pid) {
        super(net, task);
        _participantID = pid;
    }

    public String getMessage() {
        return super.getMessage(_participantID, "participant");
    }

}
