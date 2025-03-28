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

package org.yawlfoundation.yawl.resourcing.filters;

import org.apache.logging.log4j.LogManager;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.AbstractSelector;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.ResourceDataSet;
import org.yawlfoundation.yawl.resourcing.resource.AbstractResource;
import org.yawlfoundation.yawl.resourcing.resource.AbstractResourceAttribute;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.io.IOException;
import java.util.*;

/**
 * The base class for all filters.
 * <p/>
 * Create Date: 03/08/2007. Last Date: 12/11/2007
 *
 * @author Michael Adams (BPM Group, QUT Australia)
 * @version 2.0
 */

public abstract class AbstractFilter extends AbstractSelector {

    // filter types
    public static final int ORGANISATIONAL_FILTER = 0;
    public static final int CAPABILITY_FILTER = 1;
    public static final int HISTORICAL_FILTER = 2;

    protected int _filterType;

    /**
     * Constructors
     */

    public AbstractFilter() { super(); }                           // for reflection

    public AbstractFilter(String name) {
        super(name);
    }

    public AbstractFilter(String name, Map<String, String> params) {
        super(name, params);
    }

    public AbstractFilter(String name, String description) {
        super(name, description);
    }


    public AbstractFilter(String name, String desc, Map<String, String> params) {
        _name = name;
        _params = params;
        _description = desc;
    }


    /**
     * **************************************************************************
     */

    // GETTER & SETTER //
    public int getFilterType() { return _filterType; }

    public void setFilterType(int fType) { _filterType = fType; }


    /******************************************************************************/

    /**
     * @return an XML string describing this filter - used by the editor to build
     *         the specification XML
     */
    public String toXML() {
        StringBuilder result = new StringBuilder("<filter>");
        result.append(super.toXML());
        result.append("</filter>");
        return result.toString();
    }


    /******************************************************************************/

    protected Set<AbstractResource> parse(String key, WorkItemRecord wir) {
        String expression = getParamValue(key);
        if (expression != null) {
            List<Set<AbstractResource>> pSets = new ArrayList<Set<AbstractResource>>();
            for (String label : expression.split("[&|]")) {
                label = label.trim();
                if (label.startsWith("$")) {
                    label = getRuntimeValue(label, wir);
                }
                Set<AbstractResource> resources = new HashSet<AbstractResource>();
                if (label != null) {
                    AbstractResourceAttribute attribute = getByLabel(key, label);
                    if (attribute != null) {
                        resources = attribute.getResources();
                    }
                    else {
                        LogManager.getLogger(getClass()).warn(
                                "{} filter for {}: unknown {} '{}' in" +
                                        " filter expression. Will ignore.",
                                key, wir.getID(), key.toLowerCase(), label);
                    }
                }
                pSets.add(resources);
            }
            return evaluate(pSets, expression);
        }
        return Collections.emptySet();
    }


    protected String getRuntimeValue(String expression, WorkItemRecord wir) {

        // expression will be of the form ${varName)
        String varName = expression.substring(2, expression.indexOf('}'));       // extract varname

        String varValue = null;
        try {
            varValue = ResourceManager.getInstance().getNetParamValue(wir.getCaseID(), varName);
        }
        catch (IOException e) {
            LogManager.getLogger(getClass()).warn("In {} for {}: {}. " +
                    "Will ignore the expression token '{}' and continue",
                    getClassName(), wir.getID(), e.getMessage(), varName);
        }
        if (varValue == null) {
            LogManager.getLogger(getClass()).warn("In {} for {}: " +
                            "unknown net parameter in filter expression."  +
                    "Will ignore the expression token '{}' and continue",
                    getClassName(), wir.getID(), varName);
        }
        return varValue;
    }


    protected AbstractResourceAttribute getByLabel(String key, String label) {
        ResourceDataSet dataSet = ResourceManager.getInstance().getOrgDataSet();
        if (key.equals("Capability")) {
            return dataSet.getCapabilityByLabel(label);
        }
        if (key.equals("Position")) {
            return dataSet.getPositionByLabel(label);
        }
        if (key.equals("OrgGroup")) {
            return dataSet.getOrgGroupByLabel(label);
        }
        return null;
    }


    /**
     * Abstract method, to be implemented by all child classes, which carries out
     * whatever filtering the class has been created to do.
     *
     * @param resources a distribution set of Participant objects
     * @param wir the item to be allocated
     * @return the resultant filtered distribution set
     */
    public abstract Set<Participant> performFilter(Set<Participant> resources,
                                                   WorkItemRecord wir);

}

