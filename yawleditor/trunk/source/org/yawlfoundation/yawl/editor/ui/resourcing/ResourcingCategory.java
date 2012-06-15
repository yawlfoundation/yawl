package org.yawlfoundation.yawl.editor.ui.resourcing;

import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;

import java.util.ArrayList;
import java.util.List;

public class ResourcingCategory implements Comparable {

    private String _id;
    private String _name;
    protected String _subCatName;

    public ResourcingCategory() {}

    public ResourcingCategory(String id, String name) {
        setId(id);
        setName(name);
    }

    public ResourcingCategory(String id, String name, String subcat) {
        this(id, name);
        setSubcategory(subcat);
        setName(name);
    }

    public void setId(String id) { _id = id; }

    public String getId() { return _id; }


    public void setName(String name) { _name = name; }

    public String getName() { return _name; }


    public void setSubcategory(String subcat) { _subCatName = subcat; }

    public String getSubcategory() { return _subCatName; }


    public String getListLabel() {
        String label = getName();
        String subcat = getSubcategory();
        if (subcat != null) {
            if (subcat.equals("None")) subcat = "No category";
            label += " -> " + subcat;
        }
        return label;
    }


    public String getKey() {
        String key = getId();
        String subcat = getSubcategory();
        if (subcat != null) {
            key += "<>" + subcat;
        }
        return key;
    }


    public boolean equals(Object otherObject) {
        if (otherObject instanceof ResourcingCategory) {
            ResourcingCategory otherRole = (ResourcingCategory) otherObject;
            return getName().equals(otherRole.getName()) && getId().equals(otherRole.getId());
        }
        return false;
    }

    public int compareTo(Object o) {
        if (! (o instanceof ResourcingCategory)) return 1;
        return getListLabel().compareTo(((ResourcingCategory) o).getListLabel());
    }

    public static List<ResourcingCategory> convertCategories(
            List<NonHumanCategory> yCategories) {

        List<ResourcingCategory> categories = new ArrayList<ResourcingCategory>();
        for (NonHumanCategory category : yCategories) {
            String catName = category.getName();
            String catID = category.getID();
            categories.add(new ResourcingCategory(catID, catName));
            for (String subcat : category.getSubCategoryNames()) {
                categories.add(new ResourcingCategory(catID, catName, subcat));
            }
        }
        //               Collections.sort(categories);

        return categories;
    }


}