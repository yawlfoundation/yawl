package org.yawlfoundation.yawl.editor.core.resourcing;

import org.jdom2.Element;
import org.yawlfoundation.yawl.elements.YAtomicTask;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michael Adams
 * @date 27/06/13
 */
public class TaskPrivileges {

    private List<TaskPrivilege> _privileges;

    public static final List<String> PRIVILEGE_NAMES = Arrays.asList(
            "canSuspend", "canReallocateStateless", "canReallocateStateful",
            "canDeallocate", "canDelegate", "canSkip", "canPile" );


    public TaskPrivileges(YAtomicTask task) {
        init(task);
    }


    public TaskPrivilege getCanSuspendPrivilege() { return _privileges.get(0); }

    public TaskPrivilege getCanReallocateStatelessPrivilege() { return _privileges.get(1); }

    public TaskPrivilege getCanReallocateStatefulPrivilege() { return _privileges.get(2); }

    public TaskPrivilege getCanDeallocatePrivilege() { return _privileges.get(3); }

    public TaskPrivilege getCanDelegatePrivilege() { return _privileges.get(4); }

    public TaskPrivilege getCanSkipPrivilege() { return _privileges.get(5); }

    public TaskPrivilege getCanPilePrivilege() { return _privileges.get(6); }


    public TaskPrivilege getPrivilege(String name) {
        return name != null ? _privileges.get(PRIVILEGE_NAMES.indexOf(name)) : null;
    }


    public void parse(Element e) {
        if (e != null) {
            XNode node = new XNodeParser().parse(e);
            for (XNode privilegeNode : node.getChildren("privilege")) {
                String name = privilegeNode.getChildText("name");
                TaskPrivilege privilege = getPrivilege(name);
                if (privilege != null) {
                    privilege.parse(privilegeNode);
                }
            }
        }
    }


    public String toXML() {
        XNode privilegesNode = new XNode("privileges");
        for (String name : PRIVILEGE_NAMES) {
            TaskPrivilege privilege = getPrivilege(name);
            if (privilege != null) {
                privilegesNode.addChild(privilege.toXNode());
            }
        }

        // returns "" if empty because null gives "null"
        return privilegesNode.hasChildren() ? privilegesNode.toString() : "";
    }


    private void init(YAtomicTask task) {
        _privileges = new ArrayList<TaskPrivilege>();
        for (String name : PRIVILEGE_NAMES) {
            _privileges.add(new TaskPrivilege(name));
        }
    }

}
