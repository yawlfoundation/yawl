package org.yawlfoundation.yawl.editor.ui.resourcing;

import java.util.HashMap;

public class ResourcingCategory implements Comparable {

    /* ALL yawl-specific attributes of this object and its descendants
    * are to be stored in serializationProofAttributeMap, meaning we
    * won't get problems with incompatible XML serializations as we add
    * new attributes in the future.
    */

    protected HashMap serializationProofAttributeMap = new HashMap();

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

    public void setSerializationProofAttributeMap(HashMap map) {
        this.serializationProofAttributeMap = map;
    }

    public HashMap getSerializationProofAttributeMap() {
        return this.serializationProofAttributeMap;
    }

    public void setId(String id) {
        serializationProofAttributeMap.put("id", id);
    }

    public String getId() {
        return (String) serializationProofAttributeMap.get("id");
    }

    public void setName(String name) {
        serializationProofAttributeMap.put("name", name);
    }

    public String getName() {
        return (String) serializationProofAttributeMap.get("name");
    }

    public void setSubcategory(String subcat) {
        serializationProofAttributeMap.put("subcategory", subcat);
    }

    public String getSubcategory() {
        return (String) serializationProofAttributeMap.get("subcategory");
    }

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
}