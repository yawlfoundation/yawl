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

    public ParticipantSet() {
        super(false);
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
