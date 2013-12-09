/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.core.resourcing;

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.resourcing.AbstractSelector;
import org.yawlfoundation.yawl.resourcing.constraints.AbstractConstraint;
import org.yawlfoundation.yawl.resourcing.filters.AbstractFilter;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;

import java.io.IOException;
import java.util.*;

/**
 * A secondary cache to resource service entities. Storing entities here negates them
 * having to be reloaded from the resource service each time they are required.
 * @author Michael Adams
 * @date 20/06/13
 */
public class ResourceDataSet {

    private static Map<String, Participant> _participantMap;
    private static Map<String, Role> _roleMap;
    private static Map<String, NonHumanResource> _nhrMap;
    private static List<NonHumanCategory> _nhrCategories;
    private static List<AbstractSelector> _filters;
    private static List<AbstractSelector> _constraints;
    private static List<Capability> _capabilities;
    private static List<Position> _positions;
    private static List<OrgGroup> _orgGroups;


    protected static void reset() {
        _participantMap = null;
        _roleMap = null;
        _nhrMap = null;
        _nhrCategories = null;
        _filters = null;
        _constraints = null;
        _capabilities = null;
        _positions = null;
        _orgGroups = null;
    }


    private static Map<String, Participant> getParticipantMap() {
        if (_participantMap == null) {
            _participantMap = YConnector.getParticipantMap();
        }
        return _participantMap;
    }


    public static Participant getParticipant(String id) {
        return getParticipantMap().get(id);
    }


    protected static Set<Participant> getParticipants() {
        return new HashSet<Participant>(getParticipantMap().values());
    }


    private static Map<String, Role> getRoleMap() {
        if (_roleMap == null) {
            _roleMap = YConnector.getRoleMap();
        }
        return _roleMap;
    }


    public static Role getRole(String id) {
        return getRoleMap().get(id);
    }


    protected static Set<Role> getRoles() {
        return new HashSet<Role>(getRoleMap().values());
    }


    private static Map<String, NonHumanResource> getNonHumanResourceMap() {
        if (_nhrMap == null) {
            _nhrMap = YConnector.getNonHumanResourceMap();
        }
        return _nhrMap;
    }


    public static NonHumanResource getNonHumanResource(String id) {
        return getNonHumanResourceMap().get(id);
    }


    protected static Set<NonHumanResource> getNonHumanResources() {
        return new HashSet<NonHumanResource>(getNonHumanResourceMap().values());
    }


    protected static List<NonHumanCategory> getNonHumanResourceCategories() {
        if (_nhrCategories == null) {
            try {
                _nhrCategories = YConnector.getNonHumanCategories();
            }
            catch (IOException ioe) {
                _nhrCategories = Collections.emptyList();
            }
        }
        return _nhrCategories;
    }

    public static NonHumanCategory getNonHumanResourceCategory(String id) {
        for (NonHumanCategory nhc : getNonHumanResourceCategories()) {
            if (nhc.getID().equals(id)) return nhc;
        }
        return null;
    }

    private static List<AbstractSelector> getFilters() {
        if (_filters == null) {
            try {
                _filters = YConnector.getFilters();
            }
            catch (IOException ioe) {
                _filters = Collections.emptyList();
            }
        }
        return _filters;
    }


    public static AbstractFilter getFilter(String name) {
        return (AbstractFilter) getSelector(getFilters(), name);
    }


    private static List<AbstractSelector> getConstraints() {
        if (_constraints == null) {
            try {
                _constraints = YConnector.getConstraints();
            }
            catch (IOException ioe) {
                _constraints = Collections.emptyList();
            }
        }
        return _constraints;
    }

    public static AbstractConstraint getConstraint(String name) {
        return (AbstractConstraint) getSelector(getConstraints(), name);
    }


    protected static List<Capability> getCapabilities() {
        if (_capabilities == null) {
            try {
                _capabilities = YConnector.getCapabilities();
            }
            catch (IOException ioe) {
                _capabilities = Collections.emptyList();
            }
        }
        return _capabilities;
    }


    protected static List<Position> getPositions() {
        if (_positions == null) {
            try {
                _positions = YConnector.getPositions();
            }
            catch (IOException ioe) {
                _positions = Collections.emptyList();
            }
        }
        return _positions;
    }


    protected static List<OrgGroup> getOrgGroups() {
        if (_orgGroups == null) {
            try {
                _orgGroups = YConnector.getOrgGroups();
            }
            catch (IOException ioe) {
                _orgGroups = Collections.emptyList();
            }
        }
        return _orgGroups;
    }


    private static AbstractSelector getSelector(List<AbstractSelector> list, String name) {
        if (list != null) {
            for (AbstractSelector as : list) {
                if (as.getCanonicalName().endsWith(name)) {
                    return as;
                }
            }
        }
        return null;
    }

}
