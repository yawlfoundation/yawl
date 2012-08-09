package org.yawlfoundation.yawl.editor.core.repository;

/**
 * A convenience class for transporting sets of name-description pairs for items in
 * the repository.
 *
 * @author Michael Adams
 * @date 17/06/12
 */
public class RepoDescriptor implements Comparable<RepoDescriptor> {

    private String name;
    private String description;


    public RepoDescriptor(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int compareTo(RepoDescriptor other) {
        if (other == null) return -1;
        int result = getName().compareTo(other.getName());
        return (result != 0) ? result : getDescription().compareTo(other.getDescription());
    }

}
