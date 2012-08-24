package org.yawlfoundation.yawl.editor.core.resourcing.validation;

import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;

/**
 * @author Michael Adams
 * @date 25/06/12
 */
public class InvalidRoleReference extends InvalidReference {

    private String _roleID;

    public InvalidRoleReference(YNet net, YTask task, String rid) {
        super(net, task);
        _roleID = rid;
    }

    public String getMessage() {
        return super.getMessage(_roleID, "role");
    }

}
