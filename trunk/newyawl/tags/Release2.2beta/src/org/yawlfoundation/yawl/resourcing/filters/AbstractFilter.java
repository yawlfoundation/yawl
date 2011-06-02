/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

import org.yawlfoundation.yawl.resourcing.AbstractSelector;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.HashMap;
import java.util.Set;

import org.jdom.Element;

/**
 * The base class for all filters.
 *
 *  Create Date: 03/08/2007. Last Date: 12/11/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 2.0
 */

public abstract class AbstractFilter extends AbstractSelector {

    // filter types
    public static final int ORGANISATIONAL_FILTER = 0;
    public static final int CAPABILITY_FILTER = 1 ;
    public static final int HISTORICAL_FILTER = 2 ;

    protected int _filterType ;

    /** Constructors */

    public AbstractFilter() { super(); }                           // for reflection

    public AbstractFilter(String name) {
        super(name) ;
    }

    public AbstractFilter(String name, HashMap<String,String> params) {
       super(name, params) ;
    }

    public AbstractFilter(String name, String description) {
       super(name, description) ;
    }


    public AbstractFilter(String name, String desc, HashMap<String,String> params) {
        _name = name ;
        _params = params ;
        _description = desc ;
    }


    /******************************************************************************/

    // GETTER & SETTER //

    public int getFilterType() { return _filterType; }

    public void setFilterType(int fType) { _filterType = fType ; }


    /******************************************************************************/

    /** @return an XML string describing this filter - used by the editor to build
     * the specification XML
     */
    public String toXML() {
        StringBuilder result = new StringBuilder("<filter>");
        result.append(super.toXML());
        result.append("</filter>");
        return result.toString();
    }

    /**
     * Instantiates a filter object (extending from this base class)
     * @param elFilter the xml extracted from a spec file describing this filter
     * @return an instantiated object of 'name' type
     */
    public static AbstractFilter unmarshal(Element elFilter) {
        AbstractFilter filter = FilterFactory.getInstance(elFilter.getChildText("name")) ;
        Element eParams = elFilter.getChild("params");
        if (eParams != null) filter.setParams(unmarshalParams(eParams));
        return filter ;
    }


    /******************************************************************************/

    /**
     * Abstract method, to be implemented by all child classes, which carries out
     * whatever filtering the class has been created to do.
     * @param resources a distribution set of Participant objects
     * @return the resultant filtered distribution set   
     */
    public abstract Set<Participant> performFilter(Set<Participant> resources) ;

}

