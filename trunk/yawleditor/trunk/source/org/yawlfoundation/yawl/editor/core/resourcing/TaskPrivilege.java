package org.yawlfoundation.yawl.editor.core.resourcing;

import org.yawlfoundation.yawl.editor.core.resourcing.entity.ParticipantSet;
import org.yawlfoundation.yawl.editor.core.resourcing.entity.RoleSet;
import org.yawlfoundation.yawl.editor.core.resourcing.validation.InvalidReference;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.util.XNode;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 27/06/13
 */
public class TaskPrivilege {

    private ParticipantSet _participants;
    private RoleSet _roles;
    private String _privilege;
    private boolean _allowAll;


    public TaskPrivilege(String privilege) {
        _privilege = privilege;
        _participants = new ParticipantSet();
        _roles = new RoleSet();
    }


    public void setAllowAll(boolean allowAll) { _allowAll = allowAll; }

    public boolean isAllowAll() { return _allowAll; }

    public boolean isAllowed() {
        return _allowAll || hasParticipants() || hasRoles();
    }

    public boolean isRestricted() {
        return (! _allowAll) && (hasParticipants() || hasRoles());
    }


    public void setPrivilege(String privilege) { _privilege = privilege; }

    public String getPrivilege() { return _privilege; }


    public ParticipantSet getParticipants() { return _participants; }

    public boolean hasParticipants() { return ! _participants.isEmpty(); }

    public void setParticipants(ParticipantSet participants) {
        _participants = participants;
    }


    public RoleSet getRoles() { return _roles; }

    public void setRoles(RoleSet roles) { _roles = roles; }

    public boolean hasRoles() { return ! _roles.isEmpty(); }


    public XNode toXNode() {
        if (! isAllowed()) {
            return null;
        }

        XNode privilege = new XNode("privilege");
        privilege.addChild("name", _privilege);
        if (_allowAll) {
            privilege.addChild("allowall", "true");
        }
        else {

            // individual participants or roles go in the 'set' child
            if (hasParticipants() || hasRoles()) {
                XNode set = privilege.addChild("set");
                for (Participant p : _participants.getAll()) {
                    set.addChild("participant", p.getID());
                }
                for (Role r : _roles.getAll()) {
                    set.addChild("role", r.getID());
                }
            }
        }
        return privilege;
    }


    public void parse(XNode node) {
        if (node != null) {
            _privilege = node.getChildText("name");
            String allowValue = node.getChildText("allowall");
            _allowAll = allowValue != null && allowValue.equalsIgnoreCase("true");

            if (! _allowAll) {
                XNode setNode = node.getChild("set");
                for (XNode itemNode : setNode.getChildren()) {
                    String id = itemNode.getText();
                    if (itemNode.getName().equals("role")) {
                        _roles.add(id);
                    }
                    else {
                        _participants.add(id);
                    }
                }
            }
        }
    }


    public Set<InvalidReference> getInvalidReferences() {
        Set<InvalidReference> invalidReferences = new HashSet<InvalidReference>();
        invalidReferences.addAll(_participants.getInvalidReferences());
        invalidReferences.addAll(_roles.getInvalidReferences());
        return invalidReferences;
    }

}
