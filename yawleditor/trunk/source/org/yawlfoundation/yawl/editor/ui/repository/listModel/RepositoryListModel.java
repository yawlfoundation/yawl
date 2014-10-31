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

package org.yawlfoundation.yawl.editor.ui.repository.listModel;

import org.yawlfoundation.yawl.editor.core.repository.Repo;
import org.yawlfoundation.yawl.editor.core.repository.RepoDescriptor;
import org.yawlfoundation.yawl.editor.core.repository.YRepository;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 21/06/13
 */
public class RepositoryListModel extends AbstractListModel {

    private final List<RepoDescriptor> fullList;
    private List<RepoDescriptor> filteredList;

    private int filterLength;


    public RepositoryListModel(Repo repo) {
        fullList = getDescriptors(repo);
        filteredList = new ArrayList<RepoDescriptor>(fullList);
        filterLength = 0;
    }


    public int getSize() {
        return filteredList.size();
    }

    public Object getElementAt(int i) {
        return filteredList.get(i).getName();
    }

    public String getDescriptionAt(int i) {
        return i > -1 ? filteredList.get(i).getDescription() : "";
    }


    public void filter(String chars) {
        if (chars.length() == 0) {
            filteredList = new ArrayList<RepoDescriptor>(fullList);
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


    public List<RepoDescriptor> getSelections(int[] selectedIndices) {
        List<RepoDescriptor> selections = new ArrayList<RepoDescriptor>();
        for (int index : selectedIndices) {
                selections.add(filteredList.get(index));
        }
        return selections;
    }

    public RepoDescriptor getSelection(int selectedIndex) {
        return filteredList.get(selectedIndex);
    }


    private List<RepoDescriptor> filter(List<RepoDescriptor> list, String chars) {
        List<RepoDescriptor> filtered = new ArrayList<RepoDescriptor>();
        String mask = chars.toLowerCase();
        for (RepoDescriptor descriptor : list) {
            String name = descriptor.getName();
            if (name != null && name.toLowerCase().contains(mask)) {
                filtered.add(descriptor);
            }
        }
        return filtered;
    }


    private List<RepoDescriptor> getDescriptors(Repo repo) {
        return YRepository.getInstance().getRepository(repo).getDescriptors();
    }
}
