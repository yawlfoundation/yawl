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

package org.yawlfoundation.yawl.editor.ui.resourcing.dialog.listmodel;

import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.resourcing.jsf.comparator.ParticipantNameComparator;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Adams
 * @date 21/06/13
 */
public class ParticipantListModel extends AbstractResourceListModel {

    private List<Participant> fullList;
    private List<Participant> filteredList;

    private int filterLength;


    public ParticipantListModel() {
        fullList = new ArrayList<Participant>(
                SpecificationModel.getHandler().getResourceHandler().getParticipants());
        Collections.sort(fullList, new ParticipantNameComparator());
        filteredList = new ArrayList<Participant>(fullList);
        filterLength = 0;
    }


    public int getSize() {
        return filteredList.size();
    }

    public Object getElementAt(int i) {
        Participant p = filteredList.get(i);
        return p.getLastName() + ", " + p.getFirstName();
    }


    public void filter(String chars) {
        if (chars.length() == 0) {
            filteredList = new ArrayList<Participant>(fullList);
        }
        else if (chars.length() > filterLength) {
            filteredList = filter(filteredList, chars);
        }
        else if (chars.length() < filterLength) {
            filteredList = filter(fullList, chars);
        }

        filterLength = chars.length();
        fireContentsChanged(this, 0, filteredList.size());
    }


    public List<Object> getSelections(int[] selectedIndices) {
        List<Object> selections = new ArrayList<Object>();
        for (int index : selectedIndices) {
                selections.add(filteredList.get(index));
        }
        return selections;
    }


    private List<Participant> filter(List<Participant> list, String chars) {
        List<Participant> filtered = new ArrayList<Participant>();
        for (Participant p : list) {
            if (p.getFullName().contains(chars)) {
                filtered.add(p);
            }
        }
        return filtered;
    }
}
