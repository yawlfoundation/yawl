package org.yawlfoundation.yawl.editor.ui.resourcing.dialog.listmodel;

import org.yawlfoundation.yawl.editor.core.resourcing.GenericNonHumanCategory;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Adams
 * @date 21/06/13
 */
public class NonHumanResourceCategoryListModel extends AbstractResourceListModel {

    private List<GenericNonHumanCategory> fullList;
    private List<GenericNonHumanCategory> filteredList;

    private int filterLength;


    public NonHumanResourceCategoryListModel() {
        populateList();
        filteredList = new ArrayList<GenericNonHumanCategory>(fullList);
        filterLength = 0;
    }


    public int getSize() {
        return filteredList.size();
    }

    public Object getElementAt(int i) {
        GenericNonHumanCategory r = filteredList.get(i);
        return r.getListLabel();
    }


    public void filter(String chars) {
        if (chars.length() == 0) {
            filteredList = new ArrayList<GenericNonHumanCategory>(fullList);
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


    private List<GenericNonHumanCategory> filter(List<GenericNonHumanCategory> list, String chars) {
        List<GenericNonHumanCategory> filtered = new ArrayList<GenericNonHumanCategory>();
        for (GenericNonHumanCategory r : list) {
            if (r.getListLabel().contains(chars)) {
                filtered.add(r);
            }
        }
        return filtered;
    }


    private void populateList() {
        fullList = new ArrayList<GenericNonHumanCategory>();
        for (NonHumanCategory category : SpecificationModel.getHandler().getResourceHandler()
                                        .getNonHumanResourceCategories()) {
            String catName = category.getName();
            String catID = category.getID();
            fullList.add(new GenericNonHumanCategory(catID, catName));
            for (String subcat : category.getSubCategoryNames()) {
                fullList.add(new GenericNonHumanCategory(catID, catName, subcat));
            }
        }
        Collections.sort(fullList);
    }

}
