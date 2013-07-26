package org.yawlfoundation.yawl.editor.core.resourcing.validation;

/**
 * @author Michael Adams
 * @date 25/06/12
 */
public class InvalidParticipantReference extends InvalidReference {

    public InvalidParticipantReference(String pid) {
        super(pid, "Participant");
    }

}
