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
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;

/**
 * @author Michael Adams
 * @date 12/07/13
 */
public class NonHumanResourceSet extends EntityCollection<NonHumanResource> {

    public NonHumanResourceSet() {
        super(false);
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
