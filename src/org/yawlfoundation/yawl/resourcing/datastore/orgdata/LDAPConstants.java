/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.resourcing.datastore.orgdata;

import javax.naming.directory.SearchControls;
import java.util.ArrayList;
import java.util.List;

/**
 * Constants for the LDAPSourceExtended class.
 * This class contains constants like LDAP attribute and object class names from
 * the YAWL openLDAP schema. 
 * @author florian.quadt@rheni.de
 */
class LDAPConstants {
    static final String ATTR_NOTES = "yawlNotes";
    static final String ATTR_POSITION_REPORTS_TO = "yawlPositionReportsTo";
    static final String ATTR_POSITION_ORG_GROUP = "yawlPositionOrgGroup";
    static final String ATTR_ORGGROUP_TYPE = "yawlOrgGroupType";
    static final String ATTR_PRIVILEGE_ADMINISTRATOR = "yawlPrivilegeAdministrator";
    static final String ATTR_PRIVILEGE_CAN_CHOOSE_ITEM_TO_START = "yawlPrivilegeCanChooseItemToStart";
    static final String ATTR_PRIVILEGE_CAN_START_CONCURRENT = "yawlPrivilegeCanStartConcurrent";
    static final String ATTR_PRIVILEGE_CAN_REORDER = "yawlPrivilegeCanReorder";
    static final String ATTR_PRIVILEGE_CAN_VIEW_TEAM_ITEMS = "yawlPrivilegeCanViewTeamItems";
    static final String ATTR_PRIVILEGE_CAN_VIEW_ORG_GROUP_ITEMS = "yawlPrivilegeCanViewOrgGroupItems";
    static final String ATTR_PRIVILEGE_CAN_CHAIN_EXECUTION = "yawlPrivilegeCanChainExecution";
    static final String ATTR_PRIVILEGE_CAN_MANAGE_CASES = "yawlPrivilegeCanManageCases";
    static final String ATTR_UID = "uid";
    static final String ATTR_COMMON_NAME = "cn";
    static final String ATTR_SURNAME = "sn";
    static final String ATTR_GIVENNAME = "givenName";
    static final String ATTR_DISPLAYNAME = "displayName";
    static final String ATTR_DESCRIPTION = "description";
    static final String ATTR_UNIQUE_MEMBER = "uniqueMember";
    static final String ATTR_PASSWORD = "password";
    static final String ATTR_YAWL_INTERNAL_ID = "yawlInternalId";
    
    static final String OC_ROLE_UNIQUE_NAMES = "yawlRoleUniqueNames";
    static final String OC_CAPABILITY_UNIQUE_NAMES = "yawlCapabilityUniqueNames";
    static final String OC_POSITION_UNIQUE_NAMES = "yawlPositionUniqueNames";
    static final String OC_PARTICIPANT = "yawlParticipant";
    static final String OC_ORG_GROUP = "yawlOrgGroup";
    
    static final List<String> ORG_GROUP_TYPES;
    static final String OC_FILTER;
    static final SearchControls SUBTREE_SCOPE;
    static final SearchControls ONE_LEVEL_SCOPE;
    
    static {
        ORG_GROUP_TYPES = new ArrayList<String>();
        ORG_GROUP_TYPES.add("");
        ORG_GROUP_TYPES.add("GROUP");
        ORG_GROUP_TYPES.add("TEAM");
        ORG_GROUP_TYPES.add("UNIT");
        ORG_GROUP_TYPES.add("BRANCH");
        ORG_GROUP_TYPES.add("DIVISION");
        ORG_GROUP_TYPES.add("CLUSTER");
        ORG_GROUP_TYPES.add("DEPARTMENT");
        
        SUBTREE_SCOPE = new SearchControls();
        SUBTREE_SCOPE.setSearchScope(SearchControls.SUBTREE_SCOPE);
        ONE_LEVEL_SCOPE = new SearchControls();
        ONE_LEVEL_SCOPE.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        
        StringBuilder sb = new StringBuilder();
        sb.append("(|");
        sb.append("(objectClass=").append(OC_PARTICIPANT).append(")");
        sb.append("(objectClass=").append(OC_ORG_GROUP).append(")");
        sb.append("(objectClass=").append(OC_CAPABILITY_UNIQUE_NAMES).append(")");
        sb.append("(objectClass=").append(OC_POSITION_UNIQUE_NAMES).append(")");
        sb.append("(objectClass=").append(OC_ROLE_UNIQUE_NAMES).append(")");
        sb.append(")");
        OC_FILTER = sb.toString();
    }
}
