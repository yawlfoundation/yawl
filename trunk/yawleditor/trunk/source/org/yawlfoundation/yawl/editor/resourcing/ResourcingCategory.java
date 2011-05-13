package org.yawlfoundation.yawl.editor.resourcing;

import java.util.HashMap;

public class ResourcingCategory {

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

    public boolean equals(Object otherObject) {
        if (otherObject instanceof ResourcingCategory) {
            ResourcingCategory otherRole = (ResourcingCategory) otherObject;
            return getName().equals(otherRole.getName()) && getId().equals(otherRole.getId());
        }
        return false;
    }    
}