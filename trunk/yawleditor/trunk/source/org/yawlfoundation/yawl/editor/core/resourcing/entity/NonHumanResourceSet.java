package org.yawlfoundation.yawl.editor.core.resourcing.entity;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.editor.core.resourcing.ResourceDataSet;
import org.yawlfoundation.yawl.editor.core.resourcing.validation.InvalidParticipantReference;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;

/**
 * @author Michael Adams
 * @date 12/07/13
 */
public class NonHumanResourceSet extends EntityCollection<NonHumanResource> {

    public NonHumanResourceSet() { this(false); }

    public NonHumanResourceSet(boolean allowDuplicates) {
        super(allowDuplicates);
    }


    public boolean add(String id) {
        boolean success = add(ResourceDataSet.getNonHumanResource(id));
        if (! success) {
            addInvalidReference(new InvalidParticipantReference(id));
        }
        return success;
    }


    public NonHumanResource get(String id) {
        for (NonHumanResource r : getAll()) {
            if (r.getID().equals(id)) return r;
        }
        return null;
    }


    public void parse(Element e, Namespace nsYawl) {
        if (e != null) {
            for (Element eParticipant : e.getChildren("nonHumanResource", nsYawl)) {
                String id = eParticipant.getText();
                if (id.contains(",")) addCSV(id);
                else add(id);
            }
        }
    }

}
