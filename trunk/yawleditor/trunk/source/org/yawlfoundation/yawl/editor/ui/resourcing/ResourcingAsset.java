package org.yawlfoundation.yawl.editor.ui.resourcing;

import java.util.HashMap;

public class ResourcingAsset implements Comparable {

    /* ALL yawl-specific attributes of this object and its descendants
    * are to be stored in serializationProofAttributeMap, meaning we
    * won't get problems with incompatible XML serializations as we add
    * new attributes in the future.
    */

    protected HashMap serializationProofAttributeMap = new HashMap();

    public ResourcingAsset() {}

    public ResourcingAsset(String id, String name) {
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
        if (otherObject instanceof ResourcingAsset) {
            ResourcingAsset otherParticipant = (ResourcingAsset) otherObject;
            return getId().equals(otherParticipant.getId()) &&
                    getName().equals(otherParticipant.getName());
        }
        return false;
    }

    public int compareTo(Object o) {
        if (! (o instanceof ResourcingAsset)) return 1;
        return getName().compareTo(((ResourcingAsset) o).getName());
    }
}