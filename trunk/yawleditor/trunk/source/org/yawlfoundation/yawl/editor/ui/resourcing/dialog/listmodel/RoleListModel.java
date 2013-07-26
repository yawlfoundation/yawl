package org.yawlfoundation.yawl.editor.ui.resourcing.dialog.listmodel;

import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.resourcing.resource.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Adams
 * @date 21/06/13
 */
public class RoleListModel extends AbstractResourceListModel {

    private List<Role> fullList;
    private List<Role> filteredList;

    private int filterLength;


    public RoleListModel() {
        fullList = new ArrayList<Role>(
                SpecificationModel.getHandler().getResourceHandler().getRoles());
        Collections.sort(fullList);
        filteredList = new ArrayList<Role>(fullList);
        filterLength = 0;
    }


    public int getSize() {
        return filteredList.size();
    }

    public Object getElementAt(int i) {
        return filteredList.get(i).getName();
    }


    public void filter(String chars) {
        if (chars.length() == 0) {
            filteredList = new ArrayList<Role>(fullList);
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


    private List<Role> filter(List<Role> list, String chars) {
        List<Role> filtered = new ArrayList<Role>();
        for (Role p : list) {
            if (p.getName().contains(chars)) {
                filtered.add(p);
            }
        }
        return filtered;
    }
}
