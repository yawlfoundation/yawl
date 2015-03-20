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

package org.yawlfoundation.yawl.editor.ui.resourcing.listmodel;

import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Adams
 * @date 21/06/13
 */
public class NonHumanResourceListModel extends AbstractResourceListModel {

    private final List<NonHumanResource> fullList;
    private List<NonHumanResource> filteredList;

    private int filterLength;


    public NonHumanResourceListModel() {
        fullList = new ArrayList<NonHumanResource>(
                SpecificationModel.getHandler().getResourceHandler().getNonHumanResources());
        Collections.sort(fullList);
        filteredList = new ArrayList<NonHumanResource>(fullList);
        filterLength = 0;
    }


    public int getSize() {
        return filteredList.size();
    }

    public Object getElementAt(int i) {
        NonHumanResource r = filteredList.get(i);
        return r.getName();
    }


    public void filter(String chars) {
        if (chars.length() == 0) {
            filteredList = new ArrayList<NonHumanResource>(fullList);
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


    private List<NonHumanResource> filter(List<NonHumanResource> list, String chars) {
        List<NonHumanResource> filtered = new ArrayList<NonHumanResource>();
        String mask = chars.toLowerCase();
        for (NonHumanResource r : list) {
            String name = r.getName();
            if (name != null && name.toLowerCase().contains(mask)) {
                filtered.add(r);
            }
        }
        return filtered;
    }
}
