package org.yawlfoundation.yawl.editor.core.repository;

import java.util.List;

/**
 * @author Michael Adams
 * @date 6/08/13
 */
public class DataDefinitionRepository extends RepoMap {

    protected DataDefinitionRepository() {
        super("dataDefinitions");
    }

    /**
     * Adds a data definition to the repository
     * @param name a reference name for the data definition
     * @param description a description of it
     * @param xml the data definition XML to store in the repository
     * @return whether the add was successful
     */
    public String add(String name, String description, String xml) {
        if (! anyAreNull(name, description, xml)) {
            RepoRecord record = addRecord(new RepoRecord(name, description, xml));
            if (record != null) return record.getName();
        }
        return null;
    }


    /**
     * Gets a data definition from the repository
     * @param name a reference name for the data definition
     * @return the referenced data definition, or null if not found
     */
    public String get(String name) {
        RepoRecord record = getRecord(name);
        return record != null ? record.getValue() : null;
    }


    /**
     * Removes a data definition from the repository
     * @param name a reference name for the data definition
     * @return whether the removal was successful
     */
   public String remove(String name) {
        RepoRecord record = removeRecord(name);
        return record != null ? record.getValue() : null;
    }


    /**
     * Gets a sorted list of descriptors for all extended variable sets
     * @return A sorted list of RepoDescriptors (String pairs - name, description)
     */
    public List<RepoDescriptor> getDescriptors() {
        return super.getDescriptors();
    }

}
