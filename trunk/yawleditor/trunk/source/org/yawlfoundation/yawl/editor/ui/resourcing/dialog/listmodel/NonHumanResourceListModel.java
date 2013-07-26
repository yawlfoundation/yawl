package org.yawlfoundation.yawl.editor.ui.resourcing.dialog.listmodel;

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

    private List<NonHumanResource> fullList;
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
        for (NonHumanResource r : list) {
            if (r.getName().contains(chars)) {
                filtered.add(r);
            }
        }
        return filtered;
    }
}
