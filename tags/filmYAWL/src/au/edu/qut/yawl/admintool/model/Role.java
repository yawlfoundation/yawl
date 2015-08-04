/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.admintool.model;

/**
 * 
 * @author Lachlan Aldred
 * Date: 23/09/2005
 * Time: 14:19:19
 */
public class Role {
    private String roleName;

    public Role() {
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;

        final Role role = (Role) o;

        if (!roleName.equals(role.roleName)) return false;

        return true;
    }

    public int hashCode() {
        return roleName.hashCode();
    }
}
