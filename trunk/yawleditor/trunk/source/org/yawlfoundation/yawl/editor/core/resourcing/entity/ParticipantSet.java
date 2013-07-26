package org.yawlfoundation.yawl.editor.core.resourcing.entity;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.editor.core.resourcing.ResourceDataSet;
import org.yawlfoundation.yawl.editor.core.resourcing.validation.InvalidParticipantReference;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

/**
 * @author Michael Adams
 * @date 12/07/13
 */
public class ParticipantSet extends EntityCollection<Participant> {

    public ParticipantSet() { this(false); }

    public ParticipantSet(boolean allowDuplicates) {
        super(allowDuplicates);
    }


    public boolean add(String id) {
        boolean success = add(ResourceDataSet.getParticipant(id));
        if (! success) {
            addInvalidReference(new InvalidParticipantReference(id));
        }
        return success;
    }


    public Participant get(String id) {
        for (Participant p : getAll()) {
            if (p.getID().equals(id)) return p;
        }
        return null;
    }


    public void parse(Element e, Namespace nsYawl) {
        if (e != null) {
            for (Element eParticipant : e.getChildren("participant", nsYawl)) {
                String pid = eParticipant.getText();
                if (pid.contains(",")) addCSV(pid);
                else add(pid);
            }
        }
    }

}
